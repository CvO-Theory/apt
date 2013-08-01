package uniol.apt.gpu.coverabilitygraph;

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
		return "Compute a Petri net's coverability or reachability graph.\n" +
			"WARNING: This may crash your machine!";
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
		inputSpec.addOptionalParameter("graph", String.class, "cover",
			"Parameter \"cover\" to calculate the coverability graph.\n" +
			"             Parameter \"reach\" to calculate the reachability graph. Aborts if pn is not covered by a S-invariant.\n" +
			"             Parameter \"reachforce\" to calculate the reachability graph. WARNING: This may crash your machine!"
		);
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
		String graph = input.getParameter("graph", String.class);
		TransitionSystem lts;
		
		try {
			switch(graph) {
				case "cover":
					lts = CLCoverabilityGraph.compute(pn, CLCoverabilityGraph.GraphType.COVERABILITY);
					break;
				case "reach":
					lts = CLCoverabilityGraph.compute(pn, CLCoverabilityGraph.GraphType.REACHABILITY);
					break;
				case "reachforce":
					lts = CLCoverabilityGraph.compute(pn, CLCoverabilityGraph.GraphType.REACHABILITY_FORCE);
					break;
				default:
					lts = null;
					break;
			}
		} catch (Exception e) {
			throw new ModuleException(e.getMessage(), e);
		}
		
		output.setReturnValue("success", Boolean.class, (lts != null) && ((Boolean)lts.getExtension("success")));
		output.setReturnValue("lts", TransitionSystem.class, lts);
	}
}
