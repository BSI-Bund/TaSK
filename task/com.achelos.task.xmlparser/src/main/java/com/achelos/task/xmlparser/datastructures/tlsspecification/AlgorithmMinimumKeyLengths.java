package com.achelos.task.xmlparser.datastructures.tlsspecification;

import java.util.List;

/**
 * Internal data structure representing information about the Minimal Key Length of an Algorithm in an Application Specification.
 */
public class AlgorithmMinimumKeyLengths {

	/**
	 * The reference of this setting.
	 */
	public String reference;
	/**
	 * The Algorithm Name.
	 */
	public String algorithmName;
	/**
	 * The Minimal Key Length value.
	 */
	public List<KeyLengthSpecifier> minimumKeyLengths;

	/**
	 * Constructor setting information about the Minimal Key Length of an Algorithm.
	 * @param reference The reference of this setting.
	 * @param algorithmName The Algorithm Name.
	 * @param minimumKeyLengths The Minimal Key Length value.
	 */
	public AlgorithmMinimumKeyLengths(final String reference, final String algorithmName,
			final List<KeyLengthSpecifier> minimumKeyLengths) {
		this.reference = reference;
		this.algorithmName = algorithmName;
		this.minimumKeyLengths = minimumKeyLengths;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("Algorithm: ");
		sb.append(algorithmName);
		sb.append(" Allowed Key Lengths: ");
		sb.append(minimumKeyLengths.toString());
		sb.append(" Reference: ");
		sb.append(reference);
		return sb.toString();
	}

	/**
	 * Internal class representing a minimal key length value for an algorithm.
	 */
	public static class KeyLengthSpecifier {
		/**
		 * The UseUntil value of this key length.
		 */
		public String useUntil;
		/**
		 * The Minimal Key Length.
		 */
		public Integer minimumKeyLength;

		/**
		 * Constructor setting the useUntil and minimal key length value.
		 * @param useUntil The UseUntil value of this key length.
		 * @param minimumKeyLength The Minimal Key Length.
		 */
		public KeyLengthSpecifier(final String useUntil, final Integer minimumKeyLength) {
			this.useUntil = useUntil;
			this.minimumKeyLength = minimumKeyLength;
		}

		@Override
		public String toString() {
			return Integer.toString(minimumKeyLength) + " - Use until: " + useUntil;
		}
	}
}
