package uniol.apt.gpu.plain;

import com.jogamp.opencl.CLMemory.Mem;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import static java.lang.Math.*;

/**
 * This is the logic class used by the CLPlainModule class. It checks whether a given Petri net is plain.
 * @author Dennis-Michael Borde
 */
public class CLPlain {
	
	/**
	 * The main-function of the module. Checks whether the Petri net pn is plain.
	 * @param pn The Petri net to process.
	 * @return true, if the Petri net is plain.
	 * @throws IOException If the device program source is not found -- should not happen.
	 */
	public static Boolean compute(PetriNet pn) throws IOException {
		Boolean plain = Boolean.TRUE; // we assume the net is plain.

		// there is nothing to do, if there are no arcs in the net.
		if(!pn.getEdges().isEmpty()) {
			// create a context ...
			CLContext context = CLContext.create();
			try {
				// ... with the fastest device.
				CLDevice device = context.getMaxFlopsDevice();
				CLCommandQueue queue = device.createCommandQueue();

				int count = pn.getEdges().size();				// Length of arrays to process
				int localWorkSize = min(device.getMaxWorkGroupSize(), 128);	// Local work size dimensions
				int globalWorkSize = roundUp(localWorkSize, count);		// rounded up to the nearest multiple of the localWorkSize

				// create the buffer objects, one to hold all edge weights ... 
				CLBuffer<IntBuffer> weights = context.createIntBuffer(count, Mem.READ_ONLY);
				// and one to hold the result.
				CLBuffer<ByteBuffer> result = context.createByteBuffer(1, Mem.READ_WRITE);

				// fill the buffer with all input data.
				fillWeights(weights.getBuffer(), pn);
				result.getBuffer().put((byte) 1); // we assume, the net is plain.
				result.getBuffer().rewind();

				// get the device program code, compile it, get the kernel and set all needed parameters.
				CLProgram program = context.createProgram(CLPlain.class.getResourceAsStream("IsPlain.cl")).build();
				CLKernel kernel = program.createCLKernel("IsPlain");
				kernel.putArgs(weights, result).putArg(count);

				// transfer data to device, run kernel und read result.
				queue.putWriteBuffer(weights, false)
					.putWriteBuffer(result, false)
					.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
					.putReadBuffer(result, true);

				// check the result and return.
				plain = (result.getBuffer().get() == (byte) 1);
			} finally {
				context.release();
			}
		}

		return plain;
	}

	/**
	 * Fills a buffer with all edge weights of a Petri net.
	 * @param buffer The buffer to fill.
	 * @param pn The Petri net with the edges.
	 */
	private static void fillWeights(IntBuffer buffer, PetriNet pn) {
		Set<Flow> flows = pn.getEdges();
		for (Flow f : flows) {
			buffer.put(f.getWeight());
		}
	        buffer.rewind();
	}

	/**
	 * Calculates the smallest multiple of groupSize that is greater than globalSize.
	 * @param groupSize A number (intended to be the local work size of an OpenCL kernel call).
	 * @param globalSize  A number (intended to be the global work size of an OpenCL kernel call).
	 * @return A number.
	 */
	private static int roundUp(int groupSize, int globalSize) {
		int r = globalSize % groupSize;
		if (r == 0) {
			return globalSize;
		} else {
			return globalSize + groupSize - r;
		}
	}
}
