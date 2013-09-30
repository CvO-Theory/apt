package uniol.apt.gpu.clinfo;

import com.jogamp.common.JogampRuntimeException;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;
import com.jogamp.opencl.util.JOCLVersion;
import java.util.List;
import java.util.Map;
import uniol.apt.module.AbstractModule;
import uniol.apt.module.Category;
import uniol.apt.module.ModuleInput;
import uniol.apt.module.ModuleInputSpec;
import uniol.apt.module.ModuleOutput;
import uniol.apt.module.ModuleOutputSpec;
import uniol.apt.module.exception.ModuleException;

/**
 * This module prints verbose information about the system's OpenCL support.
 * @author Dennis-Michael Borde
 */
public class CLInfoModule extends AbstractModule {

	@Override
	public String getName() {
		return "cl_info";
	}

	@Override
	public String getTitle() {
		return "OpenCL Information";
	}

	@Override
	public Category[] getCategories() {
		return new Category[]{Category.GPU};
	}

	@Override
	public String getShortDescription() {
		return "Print verbose information about the system's OpenCL support.";
	}

	@Override
	public String getLongDescription() {
		return getShortDescription();
	}

	@Override
	public void require(ModuleInputSpec inputSpec) {
		inputSpec.addOptionalParameter("what", String.class, "all", "What information should be printed. Possible values are: jogamp, opencl, devices, all");
	}

	@Override
	public void provide(ModuleOutputSpec outputSpec) {
		outputSpec.addReturnValue("out", String.class, "Information about the system's OpenCL support.", ModuleOutputSpec.PROPERTY_RAW);
	}

	@Override
	public void run(ModuleInput input, ModuleOutput output) throws ModuleException {
		try {
			CLPlatform.initialize();
		} catch (JogampRuntimeException ex) {
			throw new ModuleException("OpenCL could not be initialized.", ex);
		}

		// switch on what information to print
		switch (input.getParameter("what", String.class)) {
			case "all":
				output.setReturnValue("out", String.class, System.lineSeparator() + JOCLVersion.getAllVersions() + System.lineSeparator() + createInfoText());
				break;
			case "jogamp":
				output.setReturnValue("out", String.class, System.lineSeparator() + JOCLVersion.getAllVersions());
				break;
			case "opencl":
				output.setReturnValue("out", String.class, System.lineSeparator() + createInfoText());
				break;
			case "devices":
				output.setReturnValue("out", String.class, System.lineSeparator() + createDeviceList());
				break;
		}

	}
	
	/**
	 * Returns a string containing a tabular with all CL-Devices.
	 * @return A string.
	 */
	private String createDeviceList() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ID] Device\n===========\n");
		
		List<CLDevice> devices = CLInfo.enumCLDevices();
		for(int i=0; i<devices.size(); ++i) {
			CLDevice d = devices.get(i);
			sb.append("[");
			sb.append(String.format("%2d", i+1));
			sb.append("] ");
			sb.append(d.getName());
			sb.append('\n');
		}

		return sb.toString();
	}

	/**
	 * Returns a string containing a table with all properties of all CL-Devices.
	 * @retun A string.
	 */
	private String createInfoText() {
		StringBuilder sb = new StringBuilder();

		CLPlatform[] platforms = CLPlatform.listCLPlatforms();
		for (CLPlatform p : platforms) {
			fillTable(sb, p.getProperties(), '=');
			CLDevice[] devices = p.listCLDevices();
			for (CLDevice d : devices) {
				fillTable(sb, d.getProperties(), '-');
			}
		}

		return sb.toString();
	}

	private void fillTable(StringBuilder sb, Map<String, String> properties, char underline) {
		boolean isHeader = true;
		for (String key : properties.keySet()) {
			if (isHeader) {
				String header = properties.get(key);
				sb.append(System.lineSeparator()).append(header).append(System.lineSeparator());
				for (int i = 0; i < header.length(); ++i) {
					sb.append(underline);
				}
			} else {
				sb.append(key).append(" = ");
				sb.append(properties.get(key));
			}
			sb.append(System.lineSeparator());
			isHeader = false;
		}
	}
}
