package com.achelos.task.commons.enums;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all available TLS Test Tool modes.
 */
public enum TlsTestToolMode {
	/** client mode. */
	client("client"),
	/** server mode. */
	server("server");

	private final String mode;

	/**
	 * Default constructor.
	 *
	 * @param mode Test Tool mode.
	 */
	TlsTestToolMode(final String mode) {
		this.mode = mode;
	}


	/**
	 * @return the mode value
	 */
	public final String getValue() {
		return mode;
	}


	/**
	 * Method searches for a responding enumeration value.
	 *
	 * @param string Test Tool mode.
	 * @return TlsTestToolMode enumeration object
	 * @throws InvalidAttributeValueException If the given value is not a valid TLS Test Tool mode.
	 */
	public static TlsTestToolMode getElement(final String string) throws InvalidAttributeValueException {
		for (TlsTestToolMode mode : TlsTestToolMode.values()) {
			if (mode.getValue().equals(string)) {
				return mode;
			}
		}
		throw new InvalidAttributeValueException("The given value: " + string + " is not a valid TLS Test Tool mode!");
	}
}