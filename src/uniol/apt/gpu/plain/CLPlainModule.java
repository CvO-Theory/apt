package uniol.apt.gpu.plain;

import uniol.apt.module.AbstractModule;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * This module checks if a Petri net is plain using OpenCL.
 * @author Dennis-Michael Borde
 */
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
		outputSpec.addReturnValue("plain", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		Boolean plain;
		try {
			plain = CLPlain.compute(pn);
		} catch (Exception e) {
			throw new ModuleException(e.getMessage(), e);
		}
		output.setReturnValue("plain", Boolean.class, plain);
	}
}