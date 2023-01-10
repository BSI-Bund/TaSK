package com.achelos.task.xmlparser.datastructures.tlsspecification;

/**
 * Abstract class representing a basic specification information in an Application Specification.
 * This is subclassed by the more specific data fields, such as e.g. CipherSuites.
 */
public abstract class SpecDetails {

	protected RestrictionLevel restriction;
	protected String description;
	protected String identifierValue;
	protected String reference;
	protected String useUntil;
	protected int priority;

	/**
	 * Constructor setting all information regarding this specification detail.
	 * @param restriction The RestrictionLevel of the instance.
	 * @param description The description of the instance.
	 * @param identifierValue The identifier of the instance.
	 * @param reference The reference of this instance.
	 * @param useUntil The UseUntil value of this instance.
	 * @param priority The priority of this value.
	 */
	public SpecDetails(final RestrictionLevel restriction, final String description, final String identifierValue,
			final String reference,
			final String useUntil, final int priority) {
		this.restriction = restriction;
		this.description = description;
		this.identifierValue = identifierValue;
		this.reference = reference;
		this.useUntil = useUntil;
		this.priority = priority;
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("Description: ");
		sb.append(description);
		sb.append(" Restriction: ");
		sb.append(restriction.getText());
		sb.append(" Identifier: ");
		sb.append(identifierValue);
		sb.append(" Reference: ");
		sb.append(reference);
		sb.append(" UseUntil: ");
		sb.append(useUntil);
		sb.append(" Priority: ");
		sb.append(Integer.toString(priority));
		return sb.toString();
	}

	/**
	 * Return the stored RestrictionLevel.
	 * @return the restriction
	 */
	public RestrictionLevel getRestriction() {
		return restriction;
	}

	/**
	 * Return the stored Description.
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the stored Identifier.
	 * @return the identifierValue
	 */
	public String getIdentifierValue() {
		return identifierValue;
	}

	/**
	 * Return the stored Reference value.
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * Return the stored Use Until value.
	 * @return the useUntil
	 */
	public String getUseUntil() {
		return useUntil;
	}

	/**
	 * Return the stored Priority Level.
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

}
