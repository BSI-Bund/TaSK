package com.achelos.task.commons.enums;

/**
 * Public enumeration holds all supported TLS Server Name types.
 */
public enum TlsServerNameType {
	/** 00. */
	host_name("00"),
	/** FF. */
	unknown("FF");

	private final String value;

	/**
	 * Default constructor.
	 *
	 * @param manipulationValue server name type.
	 */
	TlsServerNameType(final String manipulationValue) {
		value = manipulationValue;
	}


	/**
	 * @return the server name type.
	 */
	public final String getCommandValue() {
		return value;
	}


	@Override
	public final String toString() {
		return value;
	}

}
