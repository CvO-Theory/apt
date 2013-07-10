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

public class CLPlain {
	public static Boolean compute(PetriNet pn) throws IOException {
		Boolean plain = Boolean.TRUE;

		if(!pn.getEdges().isEmpty()) {
			CLContext context = CLContext.create();
			try {
				CLDevice device = context.getMaxFlopsDevice();
				CLCommandQueue queue = device.createCommandQueue();

				int count = pn.getEdges().size();				// Length of arrays to process
				int localWorkSize = min(device.getMaxWorkGroupSize(), 4);	// Local work size dimensions
				int globalWorkSize = roundUp(localWorkSize, count);		// rounded up to the nearest multiple of the localWorkSize

				CLProgram program = context.createProgram(CLPlain.class.getResourceAsStream("IsPlain.cl")).build();

				CLBuffer<IntBuffer> weights = context.createIntBuffer(count, Mem.READ_ONLY);
				CLBuffer<ByteBuffer> result = context.createByteBuffer(1, Mem.READ_WRITE);

				fillWeights(weights.getBuffer(), pn);
				result.getBuffer().put((byte) 1);
				result.getBuffer().rewind();

				CLKernel kernel = program.createCLKernel("IsPlain");
				kernel.putArgs(weights, result).putArg(count);

				queue.putWriteBuffer(weights, false)
					.putWriteBuffer(result, false)
					.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
					.putReadBuffer(result, true);

				plain = (result.getBuffer().get() == (byte) 1);
			} finally {
				context.release();
			}
		}

		return plain;
	}

	private static void fillWeights(IntBuffer buffer, PetriNet pn) {
		Set<Flow> flows = pn.getEdges();
		for (Flow f : flows) {
			buffer.put(f.getWeight());
		}
	        buffer.rewind();
	}

	private static int roundUp(int groupSize, int globalSize) {
		int r = globalSize % groupSize;
		if (r == 0) {
			return globalSize;
		} else {
			return globalSize + groupSize - r;
		}
	}
}
