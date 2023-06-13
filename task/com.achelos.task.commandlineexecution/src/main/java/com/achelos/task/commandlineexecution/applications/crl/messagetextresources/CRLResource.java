package com.achelos.task.commandlineexecution.applications.crl.messagetextresources;

public enum CRLResource {

	/** "GET /root-ca.crl". */
	GET_ROOT_CA_CRL("GET /root-ca.crl");

	private String message;


	/**
	 * Set the internal output message of a given enumeration.
	 *
	 * @param message the message
	 */
	CRLResource(final String message) {
		this.message = message;
	}


	/**
	 * @return The internal message of a given enumeration.
	 */
	public String getMessage() {
		return message;
	}
}
