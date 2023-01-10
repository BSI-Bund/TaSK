package com.achelos.task.commons.enums;

/**
 * Public enumeration holds all supported TLS Point formats.
 */
public enum TlsPointFormats {
	/** 0x00. */
	uncompressed((byte) 0x00),
	/** 0x01. */
	ansiX962_compressed_prime((byte) 0x01),
	/** 0x02. */
	ansiX962_compressed_char2((byte) 0x02);

	private final byte value;

	/**
	 * Default Constructor.
	 *
	 * @param value TLS point format value.
	 */
	TlsPointFormats(final byte value) {
		this.value = value;
	}


	/**
	 * @return value of TLS point format
	 */
	public final byte getValue() {
		return value;
	}


	/**
	 * Converts the TLS point format into hexadecimal format.
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
	 * Try to find a {@link TlsPointFormats} type enumeration based on the tls point formats's value.
	 *
	 * @param value The supported TLS point format value
	 * @return valid enumeration, if found. {@code null}, otherwise.
	 */
	public static TlsPointFormats valueOf(final byte value) {
		for (TlsPointFormats pointFormat : TlsPointFormats.values()) {
			if (pointFormat.value == value) {
				return pointFormat;
			}
		}
		return null;
	}
}
