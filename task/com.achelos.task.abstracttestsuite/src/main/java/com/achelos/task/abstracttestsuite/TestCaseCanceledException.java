package com.achelos.task.abstracttestsuite;

/**
 * Exception causing test case cancel. This exception is designed for canceling running test cases and has not to be
 * cached in a test case implementation. We may need this for REST API, if user wants to cancel the test case(s).
 */
public class TestCaseCanceledException extends Exception {


	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public final String toString() {
		return "Test case has been canceled: " + getMessage();
	}
}
