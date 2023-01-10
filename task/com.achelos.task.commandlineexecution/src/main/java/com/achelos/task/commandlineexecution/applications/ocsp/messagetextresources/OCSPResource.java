package com.achelos.task.commandlineexecution.applications.ocsp.messagetextresources;

public enum OCSPResource {
	/** "OCSP Request Data:". */
	OCSP_REQUEST_DATA("OCSP Request Data:");


	private String message;


	/**
	 * Set the internal output message of a given enumeration.
	 *
	 * @param message the message
	 */
	OCSPResource(final String message) {
		this.message = message;
	}


	/**
	 * @return The internal message of a given enumeration.
	 */
	public String getMessage() {
		return message;
	}
}
