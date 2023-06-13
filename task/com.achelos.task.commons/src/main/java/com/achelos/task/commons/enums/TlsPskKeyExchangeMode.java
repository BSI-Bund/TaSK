/**
 *
 */
package com.achelos.task.commons.enums;

/**
 * Identifier for the Key Exchange Mode
 */
public enum TlsPskKeyExchangeMode {
	/** 0x01. */
	psk_ke((byte) 0x00),
	/** 0x02. */
	psk_dhe_ke((byte) 0x01);

	private final byte value;

	/**
	 * Default Constructor.
	 *
	 * @param value TLS PSK key exchange mode value.
	 */
	TlsPskKeyExchangeMode(final byte value) {
		this.value = value;
	}


	/**
	 * @return value of TLS PSK key exchange mode
	 */
	public final byte getValue() {
		return value;
	}


	/**
	 * Converts the TLS PSK key exchange mode into hexadecimal format.
	 *
	 * @return formatted hexadecimal value of TLS point format
	 */
	public final String getCommandValue() {
		return String.format("%02x", value);
	}


	@Override
	public final String toString() {
		return String.format("%s(%d)", name(), value);
	}


	/**
	 * Try to find a {@link TlsPskKeyExchangeMode} type enumeration based on the TLS PSK key exchange mode's value.
	 *
	 * @param value The supported TLS PSK key exchange mode value
	 * @return valid enumeration, if found. {@code null}, otherwise.
	 */
	public static TlsPskKeyExchangeMode valueOf(final byte value) {
		for (TlsPskKeyExchangeMode pskKeyExchangeMode : TlsPskKeyExchangeMode.values()) {
			if (pskKeyExchangeMode.value == value) {
				return pskKeyExchangeMode;
			}
		}
		return null;
	}

}
