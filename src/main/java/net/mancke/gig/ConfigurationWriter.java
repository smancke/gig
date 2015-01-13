package net.mancke.gig;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.mancke.gig.model.Fig;
import net.mancke.gig.model.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * The Profile Writer is able to create a shell script with variables
 * for the configuration options of the docker command.

 * @author smancke
 */
public class ConfigurationWriter {

	private Fig fig;
	private String projectName;

	/**
	 * Create the ProfileWriter with the proejct name and the 
	 * fig configuration. 
	 * 
	 * See {@link http://www.fig.sh/yml.html} for further information on the config format. 
	 * 
	 * @param projectName The project name, to use as variable prefix
	 * @param config The fig yaml configuration as input stream
	 * @throws IOException
	 */
	public ConfigurationWriter(String projectName, InputStream config) throws IOException {
		this.projectName = projectName;
		YAMLFactory yamlFactory = new YAMLFactory();
		ObjectMapper mapper = new ObjectMapper();
		fig = mapper.readValue(yamlFactory.createParser(config), Fig.class);
	}

	/**
	 * Write the config script to the print writer.
	 * 
	 * @param out the output destination
	 */
	public void write(PrintStream out) {
//		writeScriptHeader(out);
		fig.forEach((name, service) -> {
			String serviceName = projectName + "_" + name;
			
			writeImageName(out, serviceName, service);
			writeRunOptions(out, serviceName, service);
		});		
		
		writeServiceList(out, fig);
	}

//	private void writeScriptHeader(PrintStream out) {
//		out.println("#!/bin/bash\n\n");
//		out.println("script_name=`realpath $BASH_SOURCE`");
//		out.println("current_dir=`dirname $script_name`");
//		out.println("\n\n");
//	}

	private void writeRunOptions(PrintStream out, String serviceName,
			Service service) {		
		List<String> arguments = new ArrayList<String>();
		
		service.getPorts().forEach(portMapping -> {
			arguments.add("-p"); 
			arguments.add(portMapping);				
		});
		service.getVolumes().forEach(mvolumeMapping -> { 
			arguments.add("-v");
			
			if (mvolumeMapping.startsWith("/")) {
				arguments.add(mvolumeMapping);
			} else {
				// relative path definition
				arguments.add("`pwd`/" + mvolumeMapping);
			}
		});			
		service.getLinks().forEach(linkedContainer -> {
			arguments.add("--link");
			arguments.add(projectName +"_"+ linkedContainer +":"+ projectName +"_"+ linkedContainer +" ");
		});

		service.getEnvironment().forEach((k,v) -> {
			arguments.add("-e");
			if (v != null && ! v.isEmpty()) {
				arguments.add(k + "=" + v);
			} else {
				arguments.add(k + "=$" + k);
			}
		});
		
		arguments.add(service.getImage());
		out.print(arrayDefinition(serviceName, arguments));
		out.println();
	}

	private void writeImageName(PrintStream out, String serviceName,
			Service service) {
		out.println(serviceName + "_image=" +service.getImage());
	}

	private void writeServiceList(PrintStream out, Fig fig) {
		List<String> serviceNames = new ArrayList<String>();
		fig.forEach((name, service) -> serviceNames.add(0, projectName + "_" + name));
		out.print(arrayDefinition("all_gig_services", serviceNames));
		out.println("\n");
	}

	private String arrayDefinition(String serviceName, List<String> arguments) {
		StringBuilder sb = new StringBuilder();
		sb.append(serviceName+"=(\\\n        ");
		arguments.forEach(arg -> {
			sb.append(" ");
			boolean specialChars= ! arg.matches("^[a-zA-Z0-9\\-/:\\.]+$");
			if (specialChars) {
				sb.append("\"");
			}
			sb.append(arg.replace("'", "\\'"));	
			if (specialChars) {
				sb.append("\"");
			}
			if (!arg.startsWith("-")) {
				sb.append("\n        ");
			}
		});
		sb.append(")\n");	
		return sb.toString();
	}

}
