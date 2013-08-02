package uniol.apt.gpu.clinfo;

import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLPlatform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CLInfo {
	private static List<CLDevice> cldevices = null;
	
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
