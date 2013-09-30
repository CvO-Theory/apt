package uniol.apt.gpu.clinfo;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class holds information about the OpenCL environment.
 * @author Dennis-Michael Borde
 */
public class CLInfo {
	private static List<CLDevice> cldevices = null;
	
	/**
	 * Retruns a list with all usable CL-Devices.
	 * @return An unmodifiable list.
	 */
	public static List<CLDevice> enumCLDevices() {
		if(cldevices == null) {
			cldevices = new ArrayList<>();
			CLPlatform[] platforms = CLPlatform.listCLPlatforms();
			for(CLPlatform p : platforms) {
				CLDevice[] devices = p.listCLDevices();
				cldevices.addAll(Arrays.asList(devices));
			}
		}
		return java.util.Collections.unmodifiableList(cldevices);
	}
}
