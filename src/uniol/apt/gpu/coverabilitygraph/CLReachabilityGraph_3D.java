package uniol.apt.gpu.coverabilitygraph;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.nio.IntBuffer;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniol.apt.analysis.invariants.InvariantCalculator.InvariantAlgorithm;

class CLReachabilityGraph_3D {

	private static PetriNet pn = null;
	private static Place[] places = null;
	private static Transition[] transitions = null;
	private static TransitionSystem ts = null;

	private static int numPlaces = 0;
	private static int numTransitions = 0;
	private static int numMarkingsDone = 0;
	private static int numMarkingsToDo = 0;

	private static CLContext context = null;
	private static CLCommandQueue queue = null;
	private static CLProgram program = null;
	private static CLKernel kernelFire = null;
	private static CLKernel kernelDedupe = null;
	private static CLKernel kernelArrange = null;

	private static CLBuffer<IntBuffer> matForward = null;
	private static CLBuffer<IntBuffer> matBackward = null;
	private static CLBuffer<IntBuffer> markings = null;
	private static CLBuffer<IntBuffer> markingsToDo = null;
	private static int capacity = 0;


	public static TransitionSystem compute(PetriNet net) throws IOException {
		try {
			if(InvariantCalculator.coveredBySInvariants(net, InvariantAlgorithm.FARKAS) == null)
				throw new IllegalArgumentException("Petri net ist not structurally bounded, thus the computation may not terminate!");
			
			init(net);
			while(numMarkingsToDo > 0) {
				fireTransitions();
				processResult();
				dedupeMarkings();
				arrangeMarkings();
				updateBuffer();
			}
		} finally {
			if(context != null)
				context.release();
		}

		return ts;
	}

	private static void init(PetriNet net) throws IOException {
		context = CLContext.create();

		pn = net;
		ts = new TransitionSystem(pn.getName());

		numPlaces = pn.getPlaces().size();
		numTransitions = pn.getTransitions().size();
		numMarkingsDone = 0;
		numMarkingsToDo = 1;
		
		places = pn.getPlaces().toArray(new Place[numPlaces]);
		transitions = pn.getTransitions().toArray(new Transition[numTransitions]);
		
		queue = context.getMaxFlopsDevice().createCommandQueue();
		program = context.createProgram(CLReachabilityGraph_3D.class.getResourceAsStream("ReachabilityGraph_3D.cl"))
			.build("-DnumTransitions="+numTransitions, "-DnumPlaces="+numPlaces);
		kernelFire = program.createCLKernel("Fire");
		kernelDedupe = program.createCLKernel("Dedupe");
		kernelArrange = program.createCLKernel("Arrange");

		matForward = context.createIntBuffer(numTransitions * numPlaces, Mem.READ_ONLY);
		matBackward = context.createIntBuffer(numTransitions * numPlaces, Mem.READ_ONLY);
		fillMatrices();
//		dumpMatrices();

		capacity = numMarkingsDone * numPlaces + numMarkingsToDo * numPlaces + numMarkingsToDo * numTransitions * numPlaces;
		markings = context.createIntBuffer(capacity * 2, Mem.READ_WRITE);
		markingsToDo = context.createIntBuffer(1, Mem.WRITE_ONLY);
		fillMarkings();
//		dumpMarkings();
		
		queue.putWriteBuffer(matForward, false)
			.putWriteBuffer(matBackward, false)
			.putWriteBuffer(markings, false)
			.putBarrier();

		kernelFire.putArgs(matForward, matBackward);
		kernelArrange.putArgs(markingsToDo);
	}

	private static void fillMatrices() {
		IntBuffer bufBackward = matBackward.getBuffer();
		IntBuffer bufForward = matForward.getBuffer();
		for (Transition t : transitions) {
			for (Place p : places) {
				int wBackward;
				int wForward;

				if(pn.containsFlow(p, t)) {
					wBackward = pn.getFlow(p, t).getWeight();
				} else {
					wBackward = 0;
				}
				if(pn.containsFlow(t, p)) {
					wForward = pn.getFlow(t, p).getWeight();
				} else {
					wForward = 0;
				}

				bufBackward.put(wBackward);
				bufForward.put(wForward);
			}
		}
		bufBackward.rewind();
		bufForward.rewind();
	}

	private static void dumpMatrices() {
		IntBuffer bufBackward = matBackward.getBuffer();
		IntBuffer bufForward = matForward.getBuffer();
		bufBackward.rewind();
		bufForward.rewind();
		for(int i=0; i<numTransitions; ++i) {
			System.out.print("[ ");
			for(int j=0; j<numPlaces; ++j) {
				System.out.print('-');
				System.out.print(bufBackward.get());
				System.out.print("/+");
				System.out.print(bufForward.get());
				System.out.print(' ');
			}
			System.out.println(']');
		}
		bufBackward.rewind();
		bufForward.rewind();
	}

	private static void fillMarkings() {
		IntBuffer bufMarkings = markings.getBuffer();
		Marking initMarking = pn.getInitialMarkingCopy();
		for (Place p : places) {
			int token = initMarking.getToken(p).getValue();
			bufMarkings.put(token);
		}
		while(bufMarkings.hasRemaining()) {
			bufMarkings.put(-1);
		}
		bufMarkings.rewind();
		
		ts.setInitialState(ts.createState(readMarking()));
		bufMarkings.rewind();
	}

	private static void dumpMarkings() {
		IntBuffer bufMarkings = markings.getBuffer();
		bufMarkings.rewind();
		while(bufMarkings.hasRemaining()) {
			System.out.println(readMarking());
		}
		bufMarkings.rewind();
	}
	
	private static String readMarking() {
		boolean valid = true;
		IntBuffer bufMarkings = markings.getBuffer();
		StringBuilder name = new StringBuilder();
		name.append("[ ");
		for (int i = 0; i < numPlaces; ++i) {
			int token = bufMarkings.get();
			if(valid && (token >= 0)) {
				name.append(token);
				name.append(' ');
			} else {
				valid = false;
			}
		}
		name.append("]");
		return valid ? name.toString() : null;
	}
	
	private static void fireTransitions() {
		kernelFire.setArg(2, markings)
			.setArg(3, numMarkingsDone)
			.setArg(4, numMarkingsToDo);
		queue.put3DRangeKernel(kernelFire, 0,0,0, numPlaces,numTransitions,numMarkingsToDo, numPlaces,1,1);
		// read the result before duplicates are removed
		queue.putReadBuffer(markings, true);
	}
	
	private static void processResult() {
//		dumpMarkings();

		State[] from = new State[numMarkingsToDo];
		
		markings.getBuffer().position(numMarkingsDone*numPlaces);
		for(int i=0; i<numMarkingsToDo; ++i) {
			from[i] = ts.getNode(readMarking());
		}
		
		for(int i=0; i<numMarkingsToDo; ++i) {
			for(int j=0; j<numTransitions; ++j) {
				String name = readMarking();
				if(name == null)
					continue;
				
				State to = null;
				if(ts.containsState(name)) {
					to = ts.getNode(name);
				} else {
					to = ts.createState(name);
				}
				
				ts.createArc(from[i], to, transitions[j].getLabel());
			}
		}
		
		markings.getBuffer().rewind();
	}

	private static void dedupeMarkings() {
		kernelDedupe.setArg(0, markings)
			.setArg(1, numMarkingsDone)
			.setArg(2, numMarkingsToDo);
		int count = numMarkingsDone+numMarkingsToDo+numMarkingsToDo*numTransitions;
		int offset = 0;
		do {
			int size = Math.min(count, 512);
			queue.put3DRangeKernel(kernelDedupe, 0,offset,0, numPlaces,size,numMarkingsToDo*numTransitions, numPlaces,1,1);
			queue.finish();
			count -= size;
			offset += size;
		} while(count > 0);
	}
	
	private static void arrangeMarkings() {
		kernelArrange.setArg(1, markings)
			.setArg(2, numMarkingsDone)
			.setArg(3, numMarkingsToDo);
		queue.putTask(kernelArrange);
		// read how many new markings there are
		queue.putReadBuffer(markingsToDo, true);
	}
	
	private static void updateBuffer() {
		numMarkingsDone += numMarkingsToDo;
		numMarkingsToDo = markingsToDo.getBuffer().get();
		markingsToDo.getBuffer().rewind();

		capacity = (numMarkingsDone + numMarkingsToDo + numMarkingsToDo * numTransitions) * numPlaces;
		if(markings.getCLCapacity() < capacity) {
			CLBuffer<IntBuffer> oldMarkings = markings;
			markings = context.createIntBuffer(capacity * 2, Mem.READ_WRITE);
			queue.putCopyBuffer(oldMarkings, markings)
				.putBarrier();
			oldMarkings.release();
		}
	}
}
