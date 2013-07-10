package uniol.apt.gpu.coverbilitygraph;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.module.Category;
import uniol.apt.module.Module;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

public class CLCoverabilityGraphModule implements Module {

	@Override
	public String getName() {
		return "cl_coverability_graph";
	}

	@Override
	public String getTitle() {
		return "Coverability Graph";
	}

	@Override
	public String getShortDescription() {
		return "Compute a Petri net's coverability graph.";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription();
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
		outputSpec.addReturnValue("lts", TransitionSystem.class,
			ModuleOutputSpec.PROPERTY_FILE, ModuleOutputSpec.PROPERTY_RAW);
		outputSpec.addReturnValue("success", Boolean.class, ModuleOutputSpec.PROPERTY_SUCCESS);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		PetriNet pn = input.getParameter("pn", PetriNet.class);
		TransitionSystem lts;
		try {
			lts = CLCoverabilityGraph.compute(pn);
		} catch (Exception e) {
			throw new ModuleException(e.getMessage(), e);
		}
		output.setReturnValue("success", Boolean.class, lts != null);
		output.setReturnValue("lts", TransitionSystem.class, lts);
	}
}
