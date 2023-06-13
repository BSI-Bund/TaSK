package com.achelos.task.commons.enums;

/**
 * Enumeration for all known AlertLevel values.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5246#section-7.2">TLS 1.2 - Alert Protocol</a>
 */
public enum TlsAlertLevel {
	/** 1. */
	warning((byte) 1),
	/** 2. */
	fatal((byte) 2);

	private final byte level;

	/**
	 * Default constructor.
	 *
	 * @param level The alert level.
	 */
	TlsAlertLevel(final byte level) {
		this.level = level;
	}


	/**
	 * Return the level's value.
	 *
	 * @return Value
	 */
	public final byte toNumber() {
		return level;
	}


	/**
	 * Return the level's value as hexadecimal string.
	 *
	 * @return String with one hexadecimal encoded byte (e.g., "02")
	 */
	public final String toHexString() {
		return String.format("%02x", level);
	}


	@Override
	public final String toString() {
		return String.format("%s(%d)", name(), level);
	}
}
