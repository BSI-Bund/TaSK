package com.achelos.task.xmlparser.datastructures.tlsspecification;

/**
 * Internal data structure representing a TLS Feature in an Application Specification.
 */
public class TlsFeature {

	/**
	 * The reference of this feature specification.
	 */
	public String reference;
	/**
	 * The identifier of this feature specification.
	 */
	public String identifier;
	/**
	 * The RestrictionLevel of this feature specification.
	 */
	public RestrictionLevel restriction;

	/**
	 * Constructor setting the reference, identifer and restriction of this feature.
	 * @param reference reference of this feature specification.
	 * @param identifier identifier of this feature specification.
	 * @param restriction RestrictionLevel of this feature specification.
	 */
	public TlsFeature(final String reference, final String identifier, final RestrictionLevel restriction) {
		this.reference = reference;
		this.identifier = identifier;
		this.restriction = restriction;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("Identifier: ");
		sb.append(identifier);
		sb.append(" Restriction: ");
		sb.append(restriction.getText());
		sb.append(" Reference: ");
		sb.append(reference);
		return sb.toString();
	}

}
