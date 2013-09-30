package uniol.apt.gpu.coverabilitygraph;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
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
import uniol.apt.analysis.invariants.InvariantCalculator;
import uniol.apt.analysis.invariants.InvariantCalculator.InvariantAlgorithm;
import uniol.apt.gpu.clinfo.CLInfo;

/**
 * This is the logic class used by the CLCoverabilityGraphModule class. It calculates the coverability graph of a Petri net.
 * @author Dennis-Michael Borde
 */
public class CLCoverabilityGraph {
	/**
	 * Algorithm modes.
	 */
	public static enum GraphType {
		COVERABILITY, // coverability graph
		REACHABILITY, // reachability graph (checks if net is covered by an S-invariant first).
		REACHABILITY_FORCE // reachability graph (no check is performed, system may crash).
	};
	
	private static final int OMEGA = -1; // this must be consistent with CoverabilityGraph.cl
	private static final int NAN   = -2; // this must be consistent with CoverabilityGraph.cl

	private static PetriNet pn = null;
	private static Place[] places = null;
	private static Transition[] transitions = null;
	private static TransitionSystem ts = null;

	static int numPlaces = 0;
	static int numTransitions = 0;

	// size info about the structs in the device program.
	static int sizeInfo = 0;
	static int sizeEdge = 0;
	static int sizeVertex = 0;
	static int sizeFireResult = 0;

	// how much memory is allicated for each buffer
	static int capacityInfo = 0;
	static int capacityEdges = 0;
	static int capacityVertices = 0;
	static int capacityFireResults = 0;
	
	static int numMarkingsDone = 0; // count of vertices visited
	static int numMarkingsToDo = 1; // count of vertices unvisited
	static int numEdges = 0; // count of edges
	static int numVertices = 1; // count of vertices
	static int skipNextKernelCall = 0; // true, if the next call to a kernel may be skiped

	private static CLContext context = null;
	private static CLDevice device = null;
	private static CLCommandQueue queue = null;
	private static CLProgram program = null;
	private static CLKernel kernelUpdate = null;
	private static CLKernel kernelFire = null;
	private static CLKernel kernelCheckCover = null;
	private static CLKernel kernelCheckCallDedupeFireResults = null;
	private static CLKernel kernelDedupeFireResults = null;
	private static CLKernel kernelDedupeVertices = null;
	private static CLKernel kernelConvert = null;

	private static CLBuffer<ByteBuffer> bufferInfo = null;
	private static CLBuffer<ByteBuffer> bufferEdges = null;
	private static CLBuffer<ByteBuffer> bufferVertices = null;
	private static CLBuffer<ByteBuffer> bufferFireResults = null;
	private static CLBuffer<IntBuffer> bufferMatForward = null;
	private static CLBuffer<IntBuffer> bufferMatBackward = null;

	/**
	 * The main-function of this module. It calculates the coverability graph on device deviceId for the Petri net
	 * net using the algorithm mode graph.
	 * @param deviceId The ID of the device to use (@see CLInfo).
	 * @param net The Petri net to process.
	 * @param graph The algoritm mode (@see GraphType).
	 * @return A TransitionSystem holding the graph.
	 * @throws IOException If the device program source is not found -- should not happen.
	 */
	public static TransitionSystem compute(Integer deviceId, PetriNet net, GraphType graph) throws IOException {
		try {
			if(graph == GraphType.REACHABILITY) {
				// check if net is covered by an S-invariant.
				if(InvariantCalculator.coveredBySInvariants(net, InvariantAlgorithm.FARKAS) == null)
					throw new IllegalArgumentException(
						"The Petri net is not covered by a S-invariant. " +
						"Thus it may not be structurally bounded such that " +
						"the computation may not terminate!"
					);
			}
			
			init(deviceId, net); // initializes the device
			while(numMarkingsToDo > 0) { // as long as there are unvisited vertices
				fire(); // fire for all unvisited vertices all transitions.
				if(graph == GraphType.COVERABILITY)
					checkCover(); // check, if the new markings cover another on its path from M0.
				dedupeFireResults(); // mark duplicates among the fireresults.
				dedupeVertices(); // mark duplicates among the vertices.
				convert(); // convert the rest of the markings to vertices.
				updateBuffer(); // do some memory management.
			}
			readCoverabilityGraph(); // read the result from the device.
		} finally {
			if(context != null)
				context.release();
		}

		return ts;
	}

	/**
	 * Initializes the device deviceId for the computation of the coverability graph of Petri net net.
	 * @param deviceId The ID of the device to use.
	 * @param net The Petri net to process.
	 * @throws IOException If the source of the device program is not found -- should not happen.
	 */
	private static void init(Integer deviceId, PetriNet net) throws IOException {
		pn = net;
		ts = new TransitionSystem(pn.getName());
		ts.putExtension("success", Boolean.TRUE);

		numPlaces = pn.getPlaces().size();
		numTransitions = pn.getTransitions().size();
		
		places = pn.getPlaces().toArray(new Place[numPlaces]);
		transitions = pn.getTransitions().toArray(new Transition[numTransitions]);
		
		// select the device from ID.
		try {
			device = CLInfo.enumCLDevices().get(deviceId-1);
			context = CLContext.create(device);
		} catch(IndexOutOfBoundsException e) {
			if(deviceId > 0) { // because 0 := auto
				System.err.println(String.format("Invalid ID: %d. I will pick one for you.", deviceId));
			}
			context = CLContext.create();
			device = context.getMaxFlopsDevice();
		}
		System.err.println("OpenCL device: " + device.getName());
				
		// compile the device program.
		program = context.createProgram(CLCoverabilityGraph.class.getResourceAsStream("CoverabilityGraph.cl"))
			.build("-DnumTransitions="+numTransitions, "-DnumPlaces="+numPlaces);
		queue = device.createCommandQueue();

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
		queue.finish();
		
		kernelInit.release();
		bufferInitialMarking.release();
		
		// create kernel
		kernelUpdate = program.createCLKernel("Update");
		kernelUpdate.putArg(bufferInfo);
		kernelFire = program.createCLKernel("Fire");
		kernelFire.putArg(bufferInfo);
		kernelCheckCover = program.createCLKernel("CheckCover");
		kernelCheckCover.putArg(bufferInfo);
		kernelCheckCallDedupeFireResults = program.createCLKernel("CheckCallDedupeFireResults");
		kernelCheckCallDedupeFireResults.putArg(bufferInfo);
		kernelDedupeFireResults = program.createCLKernel("DedupeFireResults");
		kernelDedupeFireResults.putArg(bufferInfo);
		kernelDedupeVertices = program.createCLKernel("DedupeVertices");
		kernelDedupeVertices.putArg(bufferInfo);
		kernelConvert = program.createCLKernel("Convert");
		kernelConvert.putArg(bufferInfo);
	}

	/**
	 * Fills the buffer for forward and backward matrices (column first).
	 */
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
		queue.finish();
	}

	/**
	 * Fills the buffer bufferInitialMarking with the initial marking of the Petri net.
	 * @param bufferInitialMarking The buffer the initial marking is written to.
	 */
	private static void fillInitialMarking(CLBuffer<IntBuffer> bufferInitialMarking) {
		IntBuffer buf = bufferInitialMarking.getBuffer();
		Marking initialMarking = pn.getInitialMarkingCopy();
		for (Place p : places) {
			int token = initialMarking.getToken(p).getValue();
			buf.put(token);
		}
		buf.rewind();
		queue.putWriteBuffer(bufferInitialMarking, false);
		queue.finish();
	}

	/**
	 * Runs the Fire-Kernel.
	 * WARNING: This may crash, if there are 2^31 unvisited markings or more.
	 */
	private static void fire() {
		queue.put2DRangeKernel(kernelFire, 0,0, numTransitions,numMarkingsToDo, numTransitions,1);
		queue.finish();
	}
	
	/**
	 * Runs the CheckCover-Kernel.
	 * WARNING: This may crash, if there are 2^31 unvisited markings or more.
	 */
	private static void checkCover() {
		queue.put2DRangeKernel(kernelCheckCover, 0,0, numTransitions,numMarkingsToDo, numTransitions,1);
		queue.finish();
	}
	
	/**
	 * Runs the DedupeFireResults-Kernel.
	 */
	private static void dedupeFireResults() {
		// first, compare result0 with all other, than result1 with all other and so on.
		for(int id=0, count=numMarkingsToDo*numTransitions-1; count>0; ++id,--count) {
			// check if a call may be skiped for the current result.
			kernelCheckCallDedupeFireResults.setArg(1, id);
			queue.putTask(kernelCheckCallDedupeFireResults);
			queue.finish();
			readStatusInfo();
			// if not ...
			if(skipNextKernelCall == 0) {
				// do only 2^20 comparisons at a time.
				kernelDedupeFireResults.setArg(1, id);
				for(int offset=0, tmpCount = count; tmpCount > 0; ) {
					int localSize = Math.min(tmpCount, 128);
					int globalSize = (tmpCount < (1024*1024)) ? (int)(Math.ceil((double)tmpCount/(double)localSize)*(double)localSize) : (1024*1024);
					queue.put1DRangeKernel(kernelDedupeFireResults, id+1 + offset, globalSize, localSize);
					queue.finish();
					offset += globalSize;
					tmpCount -= globalSize;
				}
			}
		}
	}
	
	/**
	 * Runs the DedupeVertices-Kernel.
	 */
	private static void dedupeVertices() {
		// if there are too much visited vertices, a simple call will crash.
		// so let's do only 2^20 comparisons at a time.
		int countResults = numTransitions*numMarkingsToDo;
		for(int offsetResults = 0; countResults > 0;) {
			int localSizeResults = Math.min(countResults, 8);
			int globalSizeResults = (countResults < 1024) ? (int)(Math.ceil((double)countResults/(double)localSizeResults)*(double)localSizeResults) : 1024;
			int countVertices = numVertices;
			for(int offsetVertices = 0; countVertices > 0;) {
				int localSizeVertices = Math.min(countVertices, 8);
				int globalSizeVertices = (countVertices < 1024) ? (int)(Math.ceil((double)countVertices/(double)localSizeVertices)*(double)localSizeVertices) : 1024;
				queue.put2DRangeKernel(kernelDedupeVertices, offsetResults,offsetVertices, globalSizeResults,globalSizeVertices, localSizeResults,localSizeVertices);
				queue.finish();
				offsetVertices += globalSizeVertices;
				countVertices -= globalSizeVertices;
			}
			offsetResults += globalSizeResults;
			countResults -= globalSizeResults;
		}
	}
	
	/**
	 * Runs the Convert-Kernel.
	 */
	private static void convert() {
		queue.putTask(kernelConvert);
		queue.finish();
	}
	
	/**
	 * Recalculates buffer sizes, reallocates memory and calls Update-Kernel, if nescessary.
	 * Since this would happen very often, twice the memory needed is allocated.
	 */
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
			queue.finish();
		}
	}
	
	/**
	 * Calculate the minimal needed memory on the device.
	 */
	private static void calcCapacities() {
		capacityInfo = sizeInfo;
		capacityEdges = (numEdges + numMarkingsToDo*numTransitions) * sizeEdge;
		capacityVertices = (numVertices + numMarkingsToDo*numTransitions) * sizeVertex;
		capacityFireResults = numMarkingsToDo*numTransitions * sizeFireResult;
	}
	
	/**
	 * Reallocates a buffer to new given size (data is moved). The old buffer will be released.
	 * @param buffer The Buffer to reallocate memory for.
	 * @param capacity The new capacity.
	 * @return The new buffer.
	 */
	private static CLBuffer<ByteBuffer> reallocByteBuffer(CLBuffer<ByteBuffer> buffer, int capacity) {
		CLBuffer<ByteBuffer> result = context.createByteBuffer(capacity, Mem.READ_WRITE);
		queue.putCopyBuffer(buffer, result);
		queue.finish();
		buffer.release();
		return result;
	}

	private static void readSizeInfo() {
		CLBuffer<IntBuffer> bufferSizes = context.createIntBuffer(4, Mem.WRITE_ONLY);

		CLKernel kernelGetSizes = program.createCLKernel("GetSizes");
		kernelGetSizes.putArg(bufferSizes);
		queue.putTask(kernelGetSizes).putReadBuffer(bufferSizes, false);
		queue.finish();
		
		IntBuffer buf = bufferSizes.getBuffer();
		sizeInfo = buf.get();
		sizeEdge = buf.get();
		sizeVertex = buf.get();
		sizeFireResult = buf.get();
		
		kernelGetSizes.release();
		bufferSizes.release();
	}
	
	private static void readStatusInfo() {
		queue.putReadBuffer(bufferInfo, false);
		queue.finish();
		ByteBuffer buf = bufferInfo.getBuffer();
		numMarkingsDone = buf.getInt();
		numMarkingsToDo = buf.getInt();
		numEdges = buf.getInt();
		numVertices = buf.getInt();
		skipNextKernelCall = buf.getInt();
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
				switch(token) {
					case OMEGA:
						marking.append("ω");
						break;
					case NAN:
						marking.append("NaN");
						ts.putExtension("success", Boolean.FALSE);
						break;
					default:
						marking.append(token);
						break;
				}
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
