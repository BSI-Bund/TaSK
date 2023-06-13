package com.achelos.task.xmlparser.datastructures.tlsspecification;

/**
 * Internal data structure representing a PSK Mode in an Application Specification.
 */
public class PSKMode extends SpecDetails {
	/**
	 * Constructor setting all information regarding this PSK Mode.
	 * @param restriction The RestrictionLevel of the instance.
	 * @param description The description of the instance.
	 * @param identifierValue The identifier of the instance.
	 * @param reference The reference of this instance.
	 * @param useUntil The UseUntil value of this instance.
	 * @param priority The priority of this value.
	 */
	public PSKMode(final RestrictionLevel restriction, final String description, final String identifierValue,
			final String reference,
			final String useUntil, final int priority) {
		super(restriction, description, identifierValue, reference, useUntil, priority);
	}

}
