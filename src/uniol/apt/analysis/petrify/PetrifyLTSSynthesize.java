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

package uniol.apt.analysis.petrify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.ts.TransitionSystem;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.PetrifyPNParser;
import uniol.apt.io.renderer.impl.PetrifyLTSRenderer;
import uniol.apt.module.exception.FalseParameterException;
import uniol.apt.module.exception.ModuleException;
import uniol.apt.module.exception.PetrifyNotFoundException;

/**
 * Check if the given LTS is parsable, if <b>true</b> a PN in APT format is
 * created. If <b>false</b> an error message is created.
 *
 * @author Sören Dierkes
 *
 */
public class PetrifyLTSSynthesize {

	private final TransitionSystem ts_;
	private String errorMsg_;
	private PetriNet pn_;
	private String sndParameter_;

	public PetrifyLTSSynthesize(TransitionSystem ts) {
		ts_ = ts;
	}

	/**
	 * Check if the given LTS is parsable, if <b>true</b> a PN in APT format is
	 * created.
	 *
	 * @return boolean
	 * @throws IOException
	 * @throws ModuleException
	 */
	public boolean check() throws IOException, ModuleException {
		PetrifyLTSRenderer pf = new PetrifyLTSRenderer();
		String petrifyLts = pf.render(ts_);
		petrifyLts += "\n"; // this is necessary for petrify v4.2 on MacOS X

		File tmpAutFile = File.createTempFile("petrifyAut", ".aut");
		tmpAutFile.deleteOnExit();
		BufferedWriter bw = new BufferedWriter(new FileWriter(tmpAutFile));
		bw.write(petrifyLts);
		bw.close();

		Process p;
		try {
			if (sndParameter_ != null) {
				if (sndParameter_.equals("dead")) {
					p = new ProcessBuilder("petrify", "-nolog", "-p", tmpAutFile.getAbsolutePath(), "-" + sndParameter_)
							.start();
				} else {
					throw new FalseParameterException();
				}
			} else {
				p = new ProcessBuilder("petrify", "-nolog", "-p", tmpAutFile.getAbsolutePath()).start();
			}
		} catch (FalseParameterException e) {
			throw new FalseParameterException();
		} catch (Exception e) {
			throw new PetrifyNotFoundException();
		}

		BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));

		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

		String line = "";
		String net = "";
		while ((line = br.readLine()) != null) {
			net += line + "\n";
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		PetrifyPNParser ps = new PetrifyPNParser();
		try {
			pn_ = ps.parsePN(net);
		} catch (ParseException e) {
			throw new ModuleException(e);
		}

		String errorStr = error.readLine();
		if (errorStr != null) {
			errorMsg_ = errorStr;
			return false;
		}

		return true;
	}

	/**
	 * Return the Petri net
	 * @return Petri net
	 */
	public PetriNet getPN() {
		return pn_;
	}

	/**
	 * Return the error-message
	 * @return Error-message
	 */
	public String getError() {
		return errorMsg_;
	}

	/**
	 * Setter for the "dead"-parameter
	 * @param par
	 */
	public void setSndParameter(String par) {
		sndParameter_ = par;
	}
}

// vim: ft=java:noet:sw=8:sts=8:ts=8:tw=120
