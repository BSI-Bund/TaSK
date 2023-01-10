package com.achelos.task.commons.enums;

import java.util.Arrays;

import javax.management.InvalidAttributeValueException;


/**
 * Public enumeration holds all system log level information.
 */
public enum TlsTestToolLogLevel {
	/** high. */
	high("high"),
	/** medium. */
	medium("medium"),
	/** low. */
	low("low"),
	/** off. */
	off("off");

	private final String level;

	/**
	 * Default Constructor.
	 *
	 * @param level The log level.
	 */
	TlsTestToolLogLevel(final String level) {
		this.level = level;
	}


	/**
	 * @return the string representation of a given element.
	 */
	public String getValue() {
		return level;
	}


	/**
	 * Method searches a given LogLevel element in string representation and returns the corresponding Loglevel
	 * enumeration.
	 *
	 * @param string the log level.
	 * @return the corresponding Loglevel enumeration.
	 * @throws InvalidAttributeValueException
	 */
	public static TlsTestToolLogLevel getElement(final String string) throws InvalidAttributeValueException {
		for (final TlsTestToolLogLevel logLevel : TlsTestToolLogLevel.values()) {
			if (logLevel.getValue().equalsIgnoreCase(string)) {
				return logLevel;
			}
		}
		throw new InvalidAttributeValueException("Invalid value: " + string + " -"
				+ " Choose from " + Arrays.asList(TlsTestToolLogLevel.values()).toString());
	}
}