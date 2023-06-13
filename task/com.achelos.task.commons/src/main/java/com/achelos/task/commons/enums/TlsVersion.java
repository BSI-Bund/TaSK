package com.achelos.task.commons.enums;

import javax.management.InvalidAttributeValueException;


/**
 * Enumeration for all known TLS versions.
 */
public enum TlsVersion {
	/**
	 * SSL V3.0.
	 */
	SSL_V3_0((byte) 0x03, (byte) 0x00, "SSL 3.0", "SSLv2/v3"),
	/**
	 * TLS V1.0.
	 */
	TLS_V1_0((byte) 0x03, (byte) 0x01, "TLS 1.0", "TLSv1.0"),
	/**
	 * TLS V1.1.
	 */
	TLS_V1_1((byte) 0x03, (byte) 0x02, "TLS 1.1", "TLSv1.1"),
	/**
	 * TLS V1.2.
	 */
	TLS_V1_2((byte) 0x03, (byte) 0x03, "TLS 1.2", "TLSv1.2"),
	/**
	 * TLS V1.3.
	 */
	TLS_V1_3((byte) 0x03, (byte) 0x04, "TLS 1.3", "TLSv1.3");

	private final byte major;
	private final byte minor;
	private final String name;
	private final String alternativeName;

	/**
	 * Constructor.
	 *
	 * @param major version
	 * @param minor version
	 * @param name The protocol name.
	 */
	TlsVersion(final byte major, final byte minor, final String name, final String alternativeName) {
		this.major = major;
		this.minor = minor;
		this.name = name;
		this.alternativeName = alternativeName;
	}


	/**
	 * Method returns the value as string representation.
	 *
	 * @return the corresponding string representation
	 */
	public String getTlsVersionValue() {
		return String.format("(%d,%d)", major, minor);
	}


	/**
	 * Method returns TLS version representation for manipulation of the TLS library.
	 *
	 * @return the corresponding hex string representation
	 */
	public String getTlsVersionManipulationValue() {
		return String.format("(0x%02x,0x%02x)", major, minor);
	}


	/**
	 * Method returns the hex string representation.
	 *
	 * @return the corresponding hex string representation
	 */
	public String getTlsVersionHexString() {
		return String.format("%02x %02x", major, minor);
	}


	/**
	 * Method searches a given {@link TlsVersion} element in string representation and returns the element.
	 *
	 * @param string representation of the element value
	 * @return the corresponding {@link TlsVersion} enumeration
	 * @throws InvalidAttributeValueException if string doesn't match an enumeration value.
	 */
	public static TlsVersion getElement(final String string) throws InvalidAttributeValueException {

		switch (string.toUpperCase().trim()) {
			case "SSLV2/V3":
			case "SSL 3.0":
			case "SSL3.0":
			case "SSLV3.0":
				return SSL_V3_0;
			case "TLS 1.0":
			case "TLS1.0":
			case "TLSV1.0":
				return TLS_V1_0;
			case "TLS 1.1":
			case "TLS1.1":
			case "TLSV1.1":
				return TLS_V1_1;
			case "TLS 1.2":
			case "TLS1.2":
			case "TLSV1.2":
				return TLS_V1_2;
			case "TLS 1.3":
			case "TLS1.3":
			case "TLSV1.3":
				return TLS_V1_3;
			default:
				throw new InvalidAttributeValueException("The given value: " + string
						+ " is not a valid TLS version! Please use  [TLSv1.3, TLSv1.2, TLSv1.1, TLSv1.0 or SSLv3.0]");
		}
	}


	public String getName() {
		return alternativeName;
	}

	public String getLegacyName() {
		return name;
	}


	/**
	 * Gets and returns the {@link TlsVersion} object by major and minor version.
	 *
	 * @param major the major version.
	 * @param minor the minor version.
	 * @return {@link TlsVersion} object if found, null otherwise.
	 */
	public static TlsVersion getTlsVersion(final byte major, final byte minor) {
		for (final TlsVersion version : values()) {
			if (major == version.getMajor() && minor == version.getMinor()) {
				return version;
			}
		}
		return null;
	}


	/**
	 * Getter for the major TLS version.
	 *
	 * @return the major TLS version
	 */
	public byte getMajor() {
		return major;
	}


	/**
	 * Getter for the minor TLS version.
	 *
	 * @return the minor TLS version
	 */
	public byte getMinor() {
		return minor;
	}

}
