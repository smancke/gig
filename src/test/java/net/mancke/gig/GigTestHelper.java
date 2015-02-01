package net.mancke.gig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class GigTestHelper {

	private File originalDir;
	private File targetDir;

	public GigTestHelper(String workingDir) {
		originalDir = new File(System.getProperty("user.dir")); 
		targetDir = new File(originalDir, workingDir);		
	}

	public Response run(String[] args) {
		
		Response response = new Response();
		ByteArrayOutputStream bosOut = new ByteArrayOutputStream();
		ByteArrayOutputStream bosErr = new ByteArrayOutputStream();
		
		workdir();
		try {
			new Gig().run(args, new PrintStream(bosOut), new PrintStream(bosErr));
		} catch (GigExitException e) {
			assertThat(e.getExitcode()).isGreaterThan(0);
			response.setExitcode(e.getExitcode());			
		} finally {		
			home();
		}
		
		response.setGigOutput(new String(bosOut.toByteArray()));
		response.setGigErrOutput(new String(bosOut.toByteArray()));
		return response;
	}

	private void home() {
		System.setProperty("user.dir", originalDir.getAbsolutePath());
	}

	private void workdir() {
		System.setProperty("user.dir", targetDir.getAbsolutePath());
	}
	
	public class Response {

		private int exitcode;
		private String gigOutput;
		private String gigErrOutput;
		
		public void setExitcode(int exitcode) {
			this.exitcode = exitcode;
		}

		public Response noErrors() {
			assertThat(exitcode)
			.describedAs("gig exited with code %d, and message stderr: \n%s stdout: \n%s", exitcode, gigErrOutput, gigOutput)
			.isEqualTo(0);
			return this;
		}

//		public Response expectServiceDown(String serviceName) {
//			assertThat(stateOf(serviceName).equals("absent")
//						|| stateOf(serviceName).equals("exited"))
//						.describedAs("service '%s' should be down, but is '%s'", serviceName, stateOf(serviceName))
//						.isTrue();
//			return this;
//		}

		public Response expectService(String serviceName, String state) {
			assertThat(stateOf(serviceName))
									.describedAs(serviceName)
									.isEqualTo(state);
			return this;
		}
		
		private String stateOf(String serviceName) {
			Pattern regex = Pattern.compile(serviceName +"\\s+(\\w+)");
			Matcher m = regex.matcher(gigOutput);
			if (m.find()) {
			    return m.group(1);
			}
			throw new RuntimeException("can not find service state for "+ serviceName +" in: "+gigOutput);
		}

		public String getGigOutput() {
			return gigOutput;
		}

		public void setGigOutput(String gigOutput) {
			this.gigOutput = gigOutput;
		}

		public String getGigErrOutput() {
			return gigErrOutput;
		}

		public void setGigErrOutput(String gigErrOutput) {
			this.gigErrOutput = gigErrOutput;
		}
	}
}
