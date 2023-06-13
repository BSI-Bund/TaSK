package com.achelos.task.xmlparser.datastructures.tlsspecification;

/**
 * Internal data structure representing information on a HandshakeMode in an Application Specification.
 */
public class HandshakeMode {
	/**
	 * The identifier of the HandshakeMode.
	 */
	public String handshakeIdentifier;
	/**
	 * The RestrictionLevel of the HandshakeMode.
	 */
	public RestrictionLevel restriction;
	/**
	 * String representing a statement when the mode shall be supported.
	 */
	public String whenSupported;
	/**
	 * The reference of this setting.
	 */
	public String reference;

	/**
	 * Constructor setting all information about a specific HandshakeMode.
	 * @param handshakeIdentifier The identifier of the HandshakeMode.
	 * @param restriction he RestrictionLevel of the HandshakeMode.
	 * @param whenSupported String representing a statement when the mode shall be supported.
	 * @param reference The reference of this setting.
	 */
	public HandshakeMode(final String handshakeIdentifier, final RestrictionLevel restriction,
			final String whenSupported, final String reference) {
		this.handshakeIdentifier = handshakeIdentifier;
		this.restriction = restriction;
		this.whenSupported = whenSupported;
		this.reference = reference;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("Identifier: ");
		sb.append(handshakeIdentifier);
		sb.append(" Restriction: ");
		sb.append(restriction.getText());
		if (whenSupported != null && !whenSupported.isBlank()) {
			sb.append(" WhenSupported: ");
			sb.append(whenSupported);
		}
		sb.append(" Reference: ");
		sb.append(reference);
		return sb.toString();
	}


}
