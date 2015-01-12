package net.mancke.gig;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;

public class ScriptWriter {

	public ScriptWriter() {
	}

	public void writeScriptHeader(PrintStream out) {
		out.println("#!/bin/bash");
		out.println();
	}
	
	public void writeGigScript(PrintStream out) throws IOException {
		writeResource(out, "gig.sh");		
	}

	public void writeConfigIncludes(PrintStream out) throws IOException {
		writeResource(out, "configIncludes.sh");		
	}

	private void writeResource(PrintStream out, String resource)
			throws IOException {
		InputStream is = getClass().getResourceAsStream(resource);
		if (is == null) {
			throw new RuntimeException("can not find resource for gig script: "+ resource);
		}
		IOUtils.copy(is, out);
	}
	
	public void writeProjectName(PrintStream out, String projectName) {
		out.println("gig_project_name="+projectName);		
		out.println("\n");
	}
}
