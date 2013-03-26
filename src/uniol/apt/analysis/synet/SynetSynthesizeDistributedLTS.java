/*-
 * APT - Analysis of Petri Nets and labeled Transition systems
 * Copyright (C) 2012-2013  Members of the project group APT
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package uniol.apt.analysis.synet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.Arc;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.impl.exception.FormatException;
import uniol.apt.io.parser.impl.synet.SynetPNParser;
import uniol.apt.io.renderer.impl.SynetRenderer;
import uniol.apt.module.exception.SynetNotFoundException;

/**
 * Creates to a given labeled transition system a petrinet with locations.
 *
 * @author Sören
 *
 */
public class SynetSynthesizeDistributedLTS {

	private TransitionSystem ts_;
	private PetriNet pn_;
	private String errorMsg_;
	private boolean location_;

	public SynetSynthesizeDistributedLTS(TransitionSystem ts) {
		ts_ = ts;
		location_ = false;
	}

	/**
	 * Check if the given labeled transition system is synthesizable by Synet.
	 *
	 * @return <true> if synthesizable.
	 * @throws SynetNotFoundException
	 * @throws IOException
	 * @throws FormatException
	 */
	public boolean check() throws SynetNotFoundException, IOException, FormatException {
		SynetRenderer synetRen = new SynetRenderer();
		String ltsSynetFormat = synetRen.render(ts_);

		File tmpAutFile = File.createTempFile("synetAut", ".aut");
		BufferedWriter bw = new BufferedWriter(new FileWriter(tmpAutFile));
		bw.write(ltsSynetFormat);
		bw.close();

		File tmpDisFile = File.createTempFile("synetDis", ".dis");
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(tmpDisFile));
		bw2.write(getDisString());
		bw2.close();

		File tmpSaveFile = File.createTempFile("synetNet", ".net");

		Process p;// -r uses a new algorithm -o creates an output file -d is the
					// option for distributed nets with locations
		if (location_) {
			try {
				p = new ProcessBuilder("synet", "-r", "-o", tmpSaveFile.getAbsolutePath(), "-d",
						tmpDisFile.getAbsolutePath(), tmpAutFile.getAbsolutePath()).start();
			} catch (Exception e) {
				throw new SynetNotFoundException();
			}
		} else {
			try {
				p = new ProcessBuilder("synet", "-r", "-o", tmpSaveFile.getAbsolutePath(), tmpAutFile.getAbsolutePath()).start();
			} catch (Exception e) {
				throw new SynetNotFoundException();
			}
		}

		BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		pn_ = SynetPNParser.getPetriNet(new FileInputStream(tmpSaveFile.getAbsolutePath()));

		String errorStr = error.readLine();
		if (errorStr != null) {
			errorMsg_ = errorStr;
			return false;
		}
		return true;
	}

	/**
	 * Creates a String with locations out of the Apt-format into Synet-format.
	 *
	 * @return Location String in .dis-format.
	 */
	private String getDisString() {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> labelMem = new ArrayList<String>(0);

		Set<Arc> edges = ts_.getEdges();
		for (Arc e : edges) {
			if (e.getExtension("location") != null && !labelMem.contains(e.getLabel())) {
				sb.append("(" + e.getLabel() + "," + e.getExtension("location").toString().replace("\"", "") + ")");
				sb.append("\n");
				labelMem.add(e.getLabel());
				location_ = true;
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a petrinet which is synthesized out of an location file and an
	 * labeled transition system by Synet.
	 *
	 * @return PetriNet
	 */
	public PetriNet getPN() {
		return pn_;
	}

	/**
	 * Error string, created by Synet.
	 *
	 * @return String
	 */
	public String getError() {
		return errorMsg_;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
