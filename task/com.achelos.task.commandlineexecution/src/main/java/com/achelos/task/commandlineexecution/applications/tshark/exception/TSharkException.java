package com.achelos.task.commandlineexecution.applications.tshark.exception;

/**
 * Exception is thrown if case of missing or wrong TShark configuration.
 */
public class TSharkException extends Exception {
	private static final long serialVersionUID = 7023537674653183758L;

	/**
	 * Default constructor.
	 *
	 * @param exc Exception message.
	 */
	public TSharkException(final String exc) {
		super(exc);
	}
}
