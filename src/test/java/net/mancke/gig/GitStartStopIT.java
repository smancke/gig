package net.mancke.gig;

import org.junit.Before;
import org.junit.Test;


public class GitStartStopIT {

	GigTestHelper gigTestHelper = new GigTestHelper("./example");
	
	@Before
	public void setUp() {
		gig("rm");
	}
	
	@Test
	public void startStopAllServices() {
		gig("start");
		allServicesAre("up");
		gig("stop");
		allServicesAre("exited");
		gig("rm");
		allServicesAre("absent");
	}

	@Test
	public void startStopIndependent() {
		
		gig("start", "gig_nginx_backend");
		servicesStatusIs("nginx_backend", "up");
		servicesStatusIs("nginx_frontend", "absent");

		gig("start", "gig_nginx_frontend");
		servicesStatusIs("nginx_frontend", "up");

		gig("stop", "gig_nginx_frontend");
		servicesStatusIs("nginx_backend", "up");
		servicesStatusIs("nginx_frontend", "exited");

		gig("rm", "gig_nginx_frontend");
		servicesStatusIs("nginx_backend", "up");
		servicesStatusIs("nginx_frontend", "absent");
	}

	private void servicesStatusIs(String service, String state) {
		gig("status")
		.noErrors()
		.expectService(service, state);
	}

	private void allServicesAre(String state) {
		gig("status")
		.noErrors()
		.expectService("nginx_frontend", state)
		.expectService("nginx_backend", state);		
	}

	private GigTestHelper.Response gig(String... cmd) {
		return gigTestHelper
				.run(cmd)
				.noErrors();
	}
}
