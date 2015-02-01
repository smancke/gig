package net.mancke.gig;

public class GigExitException extends Exception {

	private int exitcode;

	public GigExitException(String message, int exitcode) {
		super(message);
		this.exitcode = exitcode;
	}

	public int getExitcode() {
		return exitcode;
	}
}
