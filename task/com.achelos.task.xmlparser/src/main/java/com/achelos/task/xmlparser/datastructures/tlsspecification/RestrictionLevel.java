package com.achelos.task.xmlparser.datastructures.tlsspecification;

import generated.jaxb.configuration.Usetype;

/**
 * An Enum representing different restriction level which might appear in an Application Specification.
 */
public enum RestrictionLevel {
	REQUIRED("REQUIRED"),
	OPTIONAL("OPTIONAL"),
	ATLEAST_ONE("ATLEASTONE"),
	FORBIDDEN("FORBIDDEN");

	private String restrictionLevelName;

	RestrictionLevel(final String restrictionLevelName) {
		this.restrictionLevelName = restrictionLevelName;
	}

	/**
	 * Return the String representation of thee RestrictionLevel.
	 * @return the String representation of thee RestrictionLevel.
	 */
	public String getText() {
		return restrictionLevelName;
	}

	/**
	 * Get the restrictionLevel enum value for the provided string representation.
	 * @param restrictionString string representation of restrictionLevel enum
	 * @return The corresponding restrictionLevel enum value.
	 */
	public static RestrictionLevel fromRestrictionLevelName(final String restrictionString) {
		for (var resLevel : RestrictionLevel.values()) {
			if (resLevel.restrictionLevelName.equalsIgnoreCase(restrictionString)) {
				return resLevel;
			}
		}
		throw new IllegalArgumentException("Unknown restrictionString: " + restrictionString);
	}

	/**
	 * Get the restrictionLevel enum value for the provided Usetype value.
	 * @param use Usetype value.
	 * @return The corresponding restrictionLevel enum value, or OPTIONAL if null was provided.
	 */
	public static RestrictionLevel fromUseType(final Usetype use) {
		if (use == null) {
			return OPTIONAL;
		}
		return fromRestrictionLevelName(use.value());
	}
}
