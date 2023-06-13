package com.achelos.task.xmlparser.datastructures.tlsspecification;

/**
 * Internal data structure representing a CipherSuite in an Application Specification.
 */
public class CipherSuite extends SpecDetails {

	private final String type;

	/**
	 * Constructor setting all information regarding this CipherSuite.
	 * @param restriction The RestrictionLevel of the instance.
	 * @param description The description of the instance.
	 * @param identifierValue The identifier of the instance.
	 * @param reference The reference of this instance.
	 * @param useUntil The UseUntil value of this instance.
	 * @param priority The priority of this value.
	 * @param type The type of the CipherSuite.
	 */
	public CipherSuite(final RestrictionLevel restriction, final String description, final String identifierValue,
					   final String reference,
					   final String useUntil, final int priority, final String type) {
		super(restriction, description, identifierValue, reference, useUntil, priority);
		this.type = type;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder(super.toString());
		if (type != null && !type.isBlank()) {
			sb.append(" Type: ");
			sb.append(type);
		}
		return sb.toString();
	}

	/**
	 * Return the Type of the CipherSuite.
	 * @return the Type of the CipherSuite.
	 */
	public String getType() {
		return type;
	}


}
