package com.achelos.task.micsverifier;

/**
 * An Exception class indicating a MICS Parsing Error/Exception.
 */
public class MicsParserException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for this Exception Type.
	 * @param string The Exception error message.
	 * @param e The nested Exception.
	 */
	public MicsParserException(final String string, final Exception e) {
		super(string, e);
	}

}
