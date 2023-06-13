package com.achelos.task.commons.enums;

/**
 * Public enumeration holds all supported TLS Heartbeat values.
 */
public enum TlsHeartbeat {
	/** "01". */
	peer_allowed_to_send("01"),
	/** "02". */
	peer_not_allowed_to_send("02"),
	/** "FF". */
	unknown("FF");

	private final String value;

	/**
	 * Default constructor.
	 *
	 * @param manipulationValue The Heartbeat value.
	 */
	TlsHeartbeat(final String manipulationValue) {
		value = manipulationValue;
	}


	/**
	 * @return The value.
	 */
	public final String getCommandValue() {
		return value;
	}


	@Override
	public final String toString() {
		return value;
	}

}
