package com.achelos.task.commons.helper;

import java.util.regex.Pattern;


/**
 * Class contains methods to validate DNS, IP addresses and ports.
 */
public final class URLValidator {
	/**
	 * Private constructor.
	 */
	private URLValidator() {
		// Private constructor.
	}

	private static final Pattern DOMAIN_NAME_PATTERN_VALIDATOR
			= Pattern.compile("^(localhost)|((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$");
	private static final String BYTE_REG_EX = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final Pattern IPV4ADDRESS_PATTERN_VALIDATOR = Pattern
			.compile("^" + BYTE_REG_EX + "\\." + BYTE_REG_EX + "\\." + BYTE_REG_EX + "\\." + BYTE_REG_EX + "$");
	private static final int PORT_MIN = 1;
	private static final int PORT_MAX = 65535;

	/**
	 * Method validates a given string as DNS entry.
	 *
	 * @param potDNS DNS to be matched
	 * @return true if DNS is valid.
	 */
	private static boolean isValidDNS(final String potDNS) {
		return DOMAIN_NAME_PATTERN_VALIDATOR.matcher(potDNS).matches();
	}


	/**
	 * Check, if the given string contains a quad-dotted IPv4 address with decimal numbers (e.g., 192.168.17.42).
	 *
	 * @param ipAddress String to check
	 * @return {@code true}, if the given string contains an IP address. {@code false}, otherwise.
	 */
	public static boolean isValidIPv4Address(final String ipAddress) {
		return IPV4ADDRESS_PATTERN_VALIDATOR.matcher(ipAddress).matches();
	}


	/**
	 * Method checks if a given string is either a valid DNS or IP address.
	 *
	 * @param potAddress the address to be matched.
	 * @return true if input is valid IPv4Address OR valid DNS, false otherwise.
	 */
	public static boolean isValidAddress(final String potAddress) {
		if (isValidIPv4Address(potAddress) || isValidDNS(potAddress)) {
			return true;
		}
		return false;
	}


	/**
	 * Method checks if a given string is a valid port (1-65535).
	 *
	 * @param potPort the port to be checked.
	 * @return true if port if valid, false otherwise.
	 */
	public static boolean isValidPort(final String potPort) {
		final int potPortNumber = Integer.parseInt(potPort);
		if (potPortNumber >= PORT_MIN && potPortNumber <= PORT_MAX) {
			return true;
		}
		return false;
	}
}
