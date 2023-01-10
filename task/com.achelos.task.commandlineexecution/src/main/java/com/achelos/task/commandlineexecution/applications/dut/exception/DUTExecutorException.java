package com.achelos.task.commandlineexecution.applications.dut.exception;

/**
 * Exception is thrown in case that an error occurs while motivating the TLS client to establish a TCP/IP connection
 * and start a TLS session.
 */
public class DUTExecutorException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	public DUTExecutorException(final String s) {
		super(s);
	}


	public DUTExecutorException(final Throwable e) {
		super(e);
	}
}
