package uniol.apt.gpu.plain;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;

import uniol.apt.module.AbstractModule;
import static java.lang.System.*;
import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.Math.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

public class CLPlainModule extends AbstractModule {

	@Override
	public String getName() {
		return "cl_plain";
	}

	@Override
	public String getTitle() {
		return "Plain";
	}

	@Override
	public String getShortDescription() {
		return "Check if a Petri net is plain using OpenCL.";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.GPU};

	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addParameter("pn", PetriNet.class, "The Petri net that should be examined");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("plain", Boolean.class, "True, if the given Petri net is plain.", ModuleOutputSpec.PROPERTY_SUCCESS);
		outputSpec.addReturnValue("time", Double.class, "Calculation time in [ms].");
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);

		CLContext context = CLContext.create();
		try {
			CLDevice device = context.getMaxFlopsDevice();
			CLCommandQueue queue = device.createCommandQueue();

			int count = pn.getEdges().size();				// Length of arrays to process
			int localWorkSize = min(device.getMaxWorkGroupSize(), 4);	// Local work size dimensions
			int globalWorkSize = roundUp(localWorkSize, count);		// rounded up to the nearest multiple of the localWorkSize

			CLProgram program = context.createProgram(CLPlainModule.class.getResourceAsStream("IsPlain.cl")).build();

			CLBuffer<IntBuffer> weights = context.createIntBuffer(count, READ_ONLY);
			CLBuffer<ByteBuffer> result = context.createByteBuffer(1, READ_WRITE);

			fillWeights(weights.getBuffer(), pn);
			result.getBuffer().put((byte) 1);
		        result.getBuffer().rewind();

			CLKernel kernel = program.createCLKernel("IsPlain");
			kernel.putArgs(weights, result).putArg(count);

			long time = nanoTime();
			queue.putWriteBuffer(weights, false)
				.putWriteBuffer(result, false)
				.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
				.putReadBuffer(result, true);
			time = nanoTime() - time;

			output.setReturnValue("plain", Boolean.class, result.getBuffer().get() == (byte) 1);
			output.setReturnValue("time", Double.class, time/1000000.);
		} catch (IOException ex) {
			throw new ModuleException("OpenCL kernel file not found.", ex);
		} finally {
			context.release();
		}

	}

	private void fillWeights(IntBuffer buffer, PetriNet pn) {
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