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
		InputStream is = getClass().getResourceAsStream("gig.sh");
		if (is == null) {
			throw new RuntimeException("can not find resource for gig script: 'gig.sh'");
		}
		IOUtils.copy(is, out);		
	}
}
