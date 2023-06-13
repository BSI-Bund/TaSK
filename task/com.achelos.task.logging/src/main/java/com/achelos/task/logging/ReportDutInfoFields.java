package com.achelos.task.logging;

/**
 * Enum containing all Fields of DUT Information which can be stored in a report.
 */
public enum ReportDutInfoFields {

	TITLE("Title"),
	APPLICATION_TYPE("ApplicationType"),
	VERSION("Version"),
	DESCRIPTION("Description"),
	FILE("File"),
	FINGERPRINT("Fingerprint");

	private String fieldString;

	ReportDutInfoFields(final String fieldString) {
		this.fieldString = fieldString;
	}

	/**
	 * Return the FieldString of the Enum Object.
	 * @return the FieldString of the Enum Object.
	 */
	public String getFieldString() {
		return fieldString;
	}
}
