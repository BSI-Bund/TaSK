package com.achelos.task.logging;

/**
 * Enum containing all Fields of Metadata which can be stored in a report.
 */
public enum ReportMetadataFields {

	TESTER_IN_CHARGE("TesterInCharge"),
	DATE_OF_REPORT_GENERATION("DateOfReportGeneration"),
	EXECUTION_MACHINE_NAME("ExecutionMachineName"),
	EXECUTION_TYPE("ExecutionType"),
	START_OF_EXECUTION("StartOfExecution"),
	END_OF_EXECUTION("EndOfExecution");

	private final String fieldString;

	ReportMetadataFields(final String fieldString) {
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
