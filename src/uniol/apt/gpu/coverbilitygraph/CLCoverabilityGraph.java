package uniol.apt.gpu.coverbilitygraph;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import uniol.apt.adt.pn.Marking;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.adt.ts.State;
import uniol.apt.adt.ts.TransitionSystem;

class CLCoverabilityGraph {

	private static PetriNet pn = null;
	private static Place[] places = null;
	private static Transition[] transitions = null;
	private static TransitionSystem ts = null;

	static int numPlaces = 0;
	static int numTransitions = 0;

	static int sizeInfo = 0;
	static int sizeEdge = 0;
	static int sizeVertex = 0;
	static int sizeFireResult = 0;

	static int capacityInfo = 0;
	static int capacityEdges = 0;
	static int capacityVertices = 0;
	static int capacityFireResults = 0;
	
	static int numMarkingsDone = 0;
	static int numMarkingsToDo = 1;
	static int numEdges = 0;
	static int numVertices = 1;

	private static CLContext context = null;
	private static CLCommandQueue queue = null;
	private static CLProgram program = null;
	private static CLKernel kernelUpdate = null;
	private static CLKernel kernelFire = null;
	private static CLKernel kernelCheckCover = null; 
	private static CLKernel kernelDedupeFireResults = null;
	private static CLKernel kernelDedupeVertices = null;
	private static CLKernel kernelConvert = null;

	private static CLBuffer<ByteBuffer> bufferInfo = null;
	private static CLBuffer<ByteBuffer> bufferEdges = null;
	private static CLBuffer<ByteBuffer> bufferVertices = null;
	private static CLBuffer<ByteBuffer> bufferFireResults = null;
	private static CLBuffer<IntBuffer> bufferMatForward = null;
	private static CLBuffer<IntBuffer> bufferMatBackward = null;


	public static TransitionSystem compute(PetriNet net) throws IOException {
		try {
			init(net);
			while(numMarkingsToDo > 0) {
				fire();
				checkCover();
				dedupeFireResults();
				dedupeVertices();
				convert();
				updateBuffer();
			}
			readCoverabilityGraph();
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
		
		places = pn.getPlaces().toArray(new Place[numPlaces]);
		transitions = pn.getTransitions().toArray(new Transition[numTransitions]);
		
		queue = context.getMaxFlopsDevice().createCommandQueue();
		program = context.createProgram(CLCoverabilityGraph.class.getResourceAsStream("CoverabilityGraph.cl"))
			.build("-DnumTransitions="+numTransitions, "-DnumPlaces="+numPlaces);

		// read structure size information to calculate memory requirements
		readSizeInfo();

		// create buffer objects
		calcCapacities();
		bufferInfo = context.createByteBuffer(capacityInfo, Mem.READ_WRITE);
		bufferEdges = context.createByteBuffer(2 * capacityEdges, Mem.READ_WRITE);
		bufferVertices = context.createByteBuffer(2 * capacityVertices, Mem.READ_WRITE);
		bufferFireResults = context.createByteBuffer(2 * capacityFireResults, Mem.READ_WRITE);
		bufferMatForward = context.createIntBuffer(numTransitions * numPlaces, Mem.READ_ONLY);
		bufferMatBackward = context.createIntBuffer(numTransitions * numPlaces, Mem.READ_ONLY);
		fillMatrices();

		// call Init-Kernel
		CLBuffer<IntBuffer> bufferInitialMarking = context.createIntBuffer(numPlaces, Mem.READ_ONLY);
		fillInitialMarking(bufferInitialMarking);
		
		CLKernel kernelInit = program.createCLKernel("Init");
		kernelInit.putArgs(bufferInfo, bufferEdges, bufferVertices, bufferFireResults, bufferMatForward, bufferMatBackward, bufferInitialMarking);
		queue.putTask(kernelInit);
		
		kernelInit.release();
		bufferInitialMarking.release();
		
		// create kernel
		kernelUpdate = program.createCLKernel("Update");
		kernelUpdate.putArg(bufferInfo);
		kernelFire = program.createCLKernel("Fire");
		kernelFire.putArg(bufferInfo);
		kernelCheckCover = program.createCLKernel("CheckCover");
		kernelCheckCover.putArg(bufferInfo);
		kernelDedupeFireResults = program.createCLKernel("DedupeFireResults");
		kernelDedupeFireResults.putArg(bufferInfo);
		kernelDedupeVertices = program.createCLKernel("DedupeVertices");
		kernelDedupeVertices.putArg(bufferInfo);
		kernelConvert = program.createCLKernel("Convert");
		kernelConvert.putArg(bufferInfo);
	}

	private static void fillMatrices() {
		IntBuffer bufBackward = bufferMatBackward.getBuffer();
		IntBuffer bufForward = bufferMatForward.getBuffer();
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
		queue.putWriteBuffer(bufferMatBackward, false);
		queue.putWriteBuffer(bufferMatForward, false);
	}

	private static void fillInitialMarking(CLBuffer<IntBuffer> bufferInitialMarking) {
		IntBuffer buf = bufferInitialMarking.getBuffer();
		Marking initialMarking = pn.getInitialMarkingCopy();
		for (Place p : places) {
			int token = initialMarking.getToken(p).getValue();
			buf.put(token);
		}
		buf.rewind();
		queue.putWriteBuffer(bufferInitialMarking, false);
	}

	private static void fire() {
		queue.put2DRangeKernel(kernelFire, 0,0, numTransitions,numMarkingsToDo, numTransitions,1);
		queue.finish();
	}
	
	private static void checkCover() {
		queue.put2DRangeKernel(kernelCheckCover, 0,0, numTransitions,numMarkingsToDo, numTransitions,1);
		queue.finish();
	}
	
	private static void dedupeFireResults() {
		for(int id=0, count=numMarkingsToDo*numTransitions-1; count>0; ++id,--count) {
			int localSize = Math.min(count, 128);
			int globalSize = (int)(Math.ceil((double)count / (double)localSize) * (double)localSize);
			kernelDedupeFireResults.setArg(1, id);
			queue.put1DRangeKernel(kernelDedupeFireResults, id+1, globalSize, localSize);
			queue.finish();
		}
	}
	
	private static void dedupeVertices() {
		queue.put2DRangeKernel(kernelDedupeVertices, 0,0, numTransitions*numMarkingsToDo,numVertices, numTransitions,1);
		queue.finish();
	}
	
	private static void convert() {
		queue.putTask(kernelConvert);
		queue.finish();
	}
	
	private static void updateBuffer() {
		readStatusInfo();
		calcCapacities();
		boolean update = false;
		if(bufferEdges.getCLCapacity() < capacityEdges) {
			bufferEdges = reallocByteBuffer(bufferEdges, 2*capacityEdges);
			update = true;
		}
		if(bufferVertices.getCLCapacity() < capacityVertices) {
			bufferVertices = reallocByteBuffer(bufferVertices, 2*capacityVertices);
			update = true;
		}
		if(bufferFireResults.getCLCapacity() < capacityFireResults) {
			bufferFireResults = reallocByteBuffer(bufferFireResults, 2*capacityFireResults);
			update = true;
		}
		if(update) {
			kernelUpdate.setArg(1, bufferEdges);
			kernelUpdate.setArg(2, bufferVertices);
			kernelUpdate.setArg(3, bufferFireResults);
			queue.putTask(kernelUpdate);
		}
	}
	
	private static void calcCapacities() {
		capacityInfo = sizeInfo;
		capacityEdges = (numEdges + numMarkingsToDo*numTransitions) * sizeEdge;
		capacityVertices = (numVertices + numMarkingsToDo*numTransitions) * sizeVertex;
		capacityFireResults = numMarkingsToDo*numTransitions * sizeFireResult;
	}
	
	private static CLBuffer<ByteBuffer> reallocByteBuffer(CLBuffer<ByteBuffer> buffer, int capacity) {
		CLBuffer<ByteBuffer> result = context.createByteBuffer(capacity, Mem.READ_WRITE);
		queue.putCopyBuffer(buffer, result).finish();
		buffer.release();
		return result;
	}

	private static void readSizeInfo() {
		CLBuffer<IntBuffer> bufferSizes = context.createIntBuffer(4, Mem.WRITE_ONLY);

		CLKernel kernelGetSizes = program.createCLKernel("GetSizes");
		kernelGetSizes.putArg(bufferSizes);
		queue.putTask(kernelGetSizes).putReadBuffer(bufferSizes, true);
		
		IntBuffer buf = bufferSizes.getBuffer();
		sizeInfo = buf.get();
		sizeEdge = buf.get();
		sizeVertex = buf.get();
		sizeFireResult = buf.get();
		
		kernelGetSizes.release();
		bufferSizes.release();
	}
	
	private static void readStatusInfo() {
		queue.putReadBuffer(bufferInfo, true);
		ByteBuffer buf = bufferInfo.getBuffer();
		numMarkingsDone = buf.getInt();
		numMarkingsToDo = buf.getInt();
		numEdges = buf.getInt();
		numVertices = buf.getInt();
		buf.rewind();
	}
	
	private static void readCoverabilityGraph() {
		queue.putReadBuffer(bufferVertices, false);
		queue.putReadBuffer(bufferEdges, false);
		queue.finish();
		
		readVertices();
		readEdges();
		ts.setInitialState(ts.getNode("s0"));
	}
	
	private static void readVertices() {
		ByteBuffer buffer = bufferVertices.getBuffer();
		for(int id=0; id<numVertices; ++id) {
			State state = ts.createState("s"+id);
			// read marking
			StringBuilder marking = new StringBuilder();
			marking.append("[ ");
			for (int i = 0; i < numPlaces; ++i) {
				int token = buffer.getInt();
				marking.append((token >= 0) ? token : "ω");
				marking.append(' ');
			}
			marking.append("]");
			state.putExtension("comment", marking.toString());
			// skip parent
			buffer.getInt();
			// read edges pointer and count
			state.putExtension("idEdges", buffer.getInt());
			state.putExtension("numEdges", buffer.getInt());
		}
	}
		
	private static void readEdges() {
		ByteBuffer buffer = bufferEdges.getBuffer();
		for(State src : ts.getNodes()) {
			// seek edges list
			buffer.position(((Integer)src.getExtension("idEdges")) * sizeEdge);
			for(int i=((Integer)src.getExtension("numEdges")); i>0; --i) {
				// read transition id and target vertex id
				int idTransition = buffer.getInt();
				int idTarget = buffer.getInt();
				// create arc
				ts.createArc(src, ts.getNode("s"+idTarget), transitions[idTransition].getLabel());
			}
		}
	}		
}
