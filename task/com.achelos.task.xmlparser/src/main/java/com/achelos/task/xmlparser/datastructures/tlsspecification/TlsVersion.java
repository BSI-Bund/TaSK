package com.achelos.task.xmlparser.datastructures.tlsspecification;

/**
 * Internal Data Structure representing information on a TLS Version in an Application Specification.
 */
public class TlsVersion {

	// Attributes
	private final RestrictionLevel restriction;
	// public String tlsVersion;
	private com.achelos.task.commons.enums.TlsVersion tlsVersion;

	/**
	 * Constructor setting the TLS Version name and Restriction Level.
	 * @param restrictionLevel The RestrictionLevel of this TLS Version.
	 * @param tlsVersionName The name of the TLS version, used to match an enum.
	 * @throws IllegalArgumentException if the provided TLS Version name is not valid.
	 */
	public TlsVersion(final RestrictionLevel restrictionLevel,
			final String tlsVersionName) throws IllegalArgumentException {
		if (tlsVersionName == null || tlsVersionName.isBlank()) {
			throw new IllegalArgumentException("Invalid TLS version string: \"null\".");
		}
		try {
			tlsVersion = com.achelos.task.commons.enums.TlsVersion.getElement(tlsVersionName);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid TLS version string: " + tlsVersionName, e);
		}
		restriction = restrictionLevel != null ? restrictionLevel : RestrictionLevel.OPTIONAL;
	}

	@Override
	public String toString() {
		return tlsVersion + " is " + restriction.getText();
	}

	/**
	 * Returns the RestrictionLevel of this TLS Version.
	 * @return the RestrictionLevel of this TLS Version.
	 */
	public RestrictionLevel getRestriction() {
		return restriction;
	}

	/**
	 * Returns the TLS Version enum value of this TLS Version.
	 * @return the TLS Version enum value of this TLS Version.
	 */
	public com.achelos.task.commons.enums.TlsVersion getTlsVersionEnum() {
		return tlsVersion;
	}
}
