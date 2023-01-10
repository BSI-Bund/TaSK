package com.achelos.task.xmlparser.configparsing;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper class, which is used to add Attributes, HashMaps, and Lists to a StringBuilder object.
 */
public class StringHelper {
	// Hide Constructor.
	private StringHelper() {
		// Empty.
	}

	/**
	 * Add a representation of an HashMap to a StringBuilder.
	 * @param sb The StringBuilder to add the HashMap to.
	 * @param name The Name which shall specify the HashMap in the StringBuilder.
	 * @param hashMap The HashMap to append to the StringBuilder
	 * @param indentLevel The level of indentation the HashMap shall have.
	 */
	public static void appendHashMapToStringBuilder(final StringBuilder sb, final String name,
			final HashMap<String, ?> hashMap,
			final int indentLevel) {
		var indent = getIndentation(indentLevel);
		var indentPlusOne = getIndentation(indentLevel + 1);
		sb.append(indent);
		sb.append(name);
		sb.append(": ");
		sb.append(System.lineSeparator());
		for (var entry : hashMap.entrySet()) {
			sb.append(indentPlusOne);
			sb.append(entry.getKey());
			sb.append(": ");
			sb.append(entry.getValue().toString());
			sb.append(System.lineSeparator());
		}
	}

	/**
	 * Add a representation of an Attribute to a StringBuilder.
	 * @param sb The StringBuilder to add the HashMap to.
	 * @param name The Name which shall specify the Attribute in the StringBuilder.
	 * @param value The Value of the Attribute.
	 */
	public static void appendAttrToStringBuilder(final StringBuilder sb, final String name, final String value) {
		appendAttrToStringBuilder(sb, name, value, 1);
	}

	/**
	 * Add a representation of an Attribute to a StringBuilder.
	 * @param sb The StringBuilder to add the HashMap to.
	 * @param name The Name which shall specify the Attribute in the StringBuilder.
	 * @param value The Value of the Attribute.
	 * @param indentLevel The level of indentation the Attribute shall have.
	 */
	public static void appendAttrToStringBuilder(final StringBuilder sb, final String name, final String value,
			final int indentLevel) {
		sb.append(getIndentation(indentLevel));
		sb.append(name);
		sb.append(": ");
		sb.append(value);
		sb.append(System.lineSeparator());
	}

	/**
	 * Add a representation of a list to a StringBuilder.
	 * @param sb The StringBuilder to add the HashMap to.
	 * @param name The Name which shall specify the list in the StringBuilder.
	 * @param listOfValues The list of values to append to the StringBuilder
	 * @param indentLevel The level of indentation the list shall have.
	 */
	public static void appendListToStringBuilder(final StringBuilder sb, final String name, final List<?> listOfValues,
			final int indentLevel) {
		var indent = getIndentation(indentLevel);
		var indentPlusOne = getIndentation(indentLevel + 1);
		sb.append(indent);
		sb.append(name);
		sb.append(": ");
		sb.append(System.lineSeparator());
		for (var value : listOfValues) {
			sb.append(indentPlusOne);
			sb.append(value);
			sb.append(System.lineSeparator());
		}
	}

	/**
	 * Parse a duration from its string representation.
	 * @param durationAsString string representation of a duration
	 * @return The represented duration.
	 */
	public static Duration getDurationFromString(final String durationAsString) {
		if (durationAsString == null || durationAsString.isBlank()) {
			throw new IllegalArgumentException("Cannot parse Duration from String. Is Empty.");
		}
		var matcher = Pattern.compile("((\\d*)\\s*h)?\\s*((\\d*)\\s*m)?\\s*((\\d*)\\s*s)?").matcher(durationAsString);
		if (matcher.matches()) {
			var duration = Duration.ZERO;
			if (matcher.group(2) != null) {
				duration = duration.plusHours(Long.parseLong(matcher.group(2), 10));
			}
			if (matcher.group(4) != null) {
				duration = duration.plusMinutes(Long.parseLong(matcher.group(4), 10));
			}
			if (matcher.group(6) != null) {
				duration = duration.plusSeconds(Long.parseLong(matcher.group(6), 10));
			}
			return duration;
		}
		try {
			var duration = Duration.parse(durationAsString);
			if (duration == null) {
				throw new Exception("Cannot parse duration from string: " + durationAsString);
			}
			return duration;
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot parse duration from string: " + durationAsString);
		}
	}

	/**
	 * Combine Signature Algorithm and Hash Algorithm into a single internal string representation.
	 * @param signAlgo Signature Algorithm
	 * @param HashAlgo Hash Algorithm
	 * @return single internal string representation '<signAlgo>With<hashAlgo>'
	 */
	public static String combineSignAndHashAlgorithm(final String signAlgo, final String HashAlgo) {
		return signAlgo.toLowerCase() + "With" + HashAlgo.toLowerCase();
	}

	private static String getIndentation(final int indentLevel) {
		if (indentLevel <= 0) {
			return "";
		}
		StringBuilder indentBuilder = new StringBuilder();
		for (int i = 0; i < indentLevel; i++) {
			indentBuilder.append('\t');
		}
		return indentBuilder.toString();
	}
}
