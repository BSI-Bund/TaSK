package com.achelos.task.commons.enums;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all available TLS Test Tool libraries.
 */
public enum TlsTestToolTlsLibrary {
	/**
	 * mbed TLS library.
	 */
	MBED_TLS("mbed TLS"),
	/**
	 * OpenSSL TLS library.
	 */
	OpenSSL("OpenSSL");

	private final String tlsLibrary;

	/**
	 * Constructor.
	 *
	 * @param tlsLibrary The TLS library
	 */
	TlsTestToolTlsLibrary(final String tlsLibrary) {
		this.tlsLibrary = tlsLibrary;
	}


	/**
	 * Get the value.
	 *
	 * @return the TLS library value
	 */
	public String getValue() {
		return tlsLibrary;
	}


	/**
	 * Method searches for a responding enumeration value.
	 *
	 * @param string TLS library in string representation.
	 * @throws InvalidAttributeValueException if string doesn't match an enumeration value.
	 * @return Enumeration representation of TLS library.
	 */
	public static TlsTestToolTlsLibrary getElement(final String string) throws InvalidAttributeValueException {
		for (TlsTestToolTlsLibrary tlsLibrary : TlsTestToolTlsLibrary.values()) {
			if (tlsLibrary.getValue().equals(string)) {
				return tlsLibrary;
			}
		}
		throw new InvalidAttributeValueException(
				"The given value: " + string + " is not a valid TLS Test Tool TLS library!");
	}

}