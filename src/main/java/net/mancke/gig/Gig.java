package net.mancke.gig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Gig is a simple starter for docker small projects.
 * It is inspired by docker fig and is based on the same configuration.
 * 
 * How does gig work:
 * <ul>
 * 	<li>Gig transforms a fig configuration into a set of shell variables</li>
 *  <li>The shell variables are sourced by a control shell script which provides service functions like start|stop|..<li>
 *  <li>Development: All operations can be executed directly by the shell by the java wrapper program</li>
 *  <li>Production: The shell scripts can be generated and used as pure bash scripts.</li>
 * </ul>
 * 
 * @author smancke
 */
public class Gig {

	private static final String CMD_HELP = "help";
	private static final String CMD_GENERATE = "generate";
	
	// our defaults
	private String projectName = new File(System.getProperty("user.dir")).getAbsoluteFile().getName();
	private String figFile = "fig.yml";
	private String command = CMD_HELP;
	private List<String> arguments = new ArrayList<String>();
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		new Gig().run(args);
	}

	public void run(String[] args) throws IOException {
		readArgs(args);
		if (CMD_GENERATE.equals(command)) {
			generateCombined();
		} else {
			printHelp();
		}
	}

	private void printHelp() {
		PrintStream out = System.out;
		out.println("gig [option].. command [arguments]..\n");
		out.println("options are:");
		out.printf("  %-10s%-60s\n", "-f", "project configuration file. (default: fig.yml)");
		out.printf("  %-10s%-60s\n", "-p", "project name. (default: work dir)");
		
		out.println("\ncommands are:");
		out.printf("  %-10s%-60s\n", "generate [file]", "generate a project shell script to [file] or stdout ");
		out.printf("  %-10s%-60s\n", "help", "print this help message");
	}

	private void generateCombined() throws IOException {
		PrintStream  out = System.out;
		if (arguments.size() > 0) {
			out = new PrintStream(arguments.get(0));
		}

		ScriptWriter scriptWriter = new ScriptWriter();
		scriptWriter.writeScriptHeader(out);
		scriptWriter.writeProjectName(out, projectName);
		scriptWriter.writeConfigIncludes(out);
		new ConfigurationWriter(projectName, figConfig()).write(out);		
		scriptWriter.writeGigScript(out);
		
		if (arguments.size() > 0) {
			out.close();
		}
	}

	private InputStream figConfig() throws FileNotFoundException {
		File configFile = new File(figFile);
		if (! configFile.canRead()) {
			throw new IllegalArgumentException("can not read fig configuration file: "+ figFile);
		}
		return new FileInputStream(configFile);
	}

	private void generate(String configFile) throws IOException {		 
		File script = new File("gig.profile");
		PrintStream out = new PrintStream(script);
		new ConfigurationWriter(projectName, new FileInputStream(configFile)).write(out);
		out.close();
	}

	private void readArgs(String[] args) {
		List<String> argList = new LinkedList<String>();
		argList.addAll(Arrays.asList(args));
		boolean foundCommand = false;
		while (argList.size() > 0) {
			String arg = unshift(argList);
			if (! foundCommand && "-f".equals(arg)) {
				figFile = unshift(argList);				
			} else if (! foundCommand && "-p".equals(arg)) {
				projectName = unshift(argList);				
			} else if (! foundCommand && "-h".equals(arg) || "--help".equals(arg)) {
				command = CMD_HELP;
				foundCommand = true;
			} else if (! foundCommand) {
				command = arg;
				foundCommand = true;				
			} else {
				arguments.add(arg);
			}			
		}
	}

	private String unshift(List<String> argList) {
		if (argList.size() == 0)
			throw new IllegalArgumentException("to few parameters");
		return argList.remove(0);
	}
}