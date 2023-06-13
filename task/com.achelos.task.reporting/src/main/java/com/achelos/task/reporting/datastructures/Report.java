package com.achelos.task.reporting.datastructures;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.achelos.task.logging.LoggingConnector.LogEntry;
import com.achelos.task.logging.ReportDutInfoFields;
import com.achelos.task.logging.ReportMetadataFields;

import generated.jaxb.input.ICS;
import generated.jaxb.testrunplan.TestRunPlan;
import jakarta.xml.bind.JAXBElement;

import javax.xml.namespace.QName;

/**
 * Class representing an abstract report of a TaSK Test Tool execution.
 */
public class Report {

	private final ReportMetadata metadata;
	private final ReportDutInformation dutInformation;
	private final List<TestSuiteReport> testSuites;
	private final InputParameters inputParameters;
	private final List<LogEntry> testFrameworkLogMessages;

	protected Report(final ReportMetadata metadata, final ReportDutInformation dutInformation,
			final List<TestSuiteReport> testSuites, final InputParameters inputParameters,
			final List<LogEntry> testFrameworkLogMessages) {
		this.metadata = metadata;
		this.dutInformation = dutInformation;
		this.testSuites = testSuites;
		this.inputParameters = inputParameters;
		this.testFrameworkLogMessages = testFrameworkLogMessages;
	}

	/**
	 * Returns a list of all available metadata fields in this report.
	 * @return a list of all available metadata fields in this report.
	 */
	public Set<Entry<ReportMetadataFields, String>> listAvailableMetadata() {
		if (metadata != null) {
			return metadata.listAvailableMetadata();
		}
		return null;
	}

	/**
	 * Returns a list of all available DUT Information fields in this report.
	 * @return a list of all available DUT Information fields in this report.
	 */
	public Set<Entry<ReportDutInfoFields, String>> listAvailableDutInformation() {
		if (metadata != null) {
			return dutInformation.listAvailableDutInformation();
		}
		return null;
	}

	/**
	 * Returns the InputParameters available in this report.
	 * @return the InputParameters available in this report.
	 */
	public InputParameters getInputParameters() {
		return inputParameters;
	}

	/**
	 * Returns the LogMessages which have been logged on a Test Framework level, which are stored in this report.
	 * @return the LogMessages which have been logged on a Test Framework level, which are stored in this report.
	 */
	public List<LogEntry> getTestFrameworkLogMessages() {
		return new ArrayList<>(testFrameworkLogMessages);
	}

	/**
	 * Returns a list of abstract TestSuiteReports, which are stored in this report.
	 * @return a list of abstract TestSuiteReports, which are stored in this report.
	 */
	public List<TestSuiteReport> getTestSuiteReports() {
		return new ArrayList<>(testSuites);
	}

	/**
	 * Internal class, representing Metadata of a report.
	 */
	static class ReportMetadata {
		private final HashMap<ReportMetadataFields, String> metadata;

		/**
		 * Default constructor. Stores no metadata information.
		 */
		public ReportMetadata() {
			metadata = new HashMap<>();
		}

		/**
		 * Constructor using a set of available metadata.
		 * @param metadata Metadata to store in this object.
		 */
		public ReportMetadata(final HashMap<ReportMetadataFields, String> metadata) {
			this.metadata = metadata;
		}

		/**
		 * Add metadata to this object.
		 * @param key The ReportMetadataFields of the Metadata to store.
		 * @param value The value of the Metadata to store.
		 */
		public void appendMetadata(final ReportMetadataFields key, final String value) {
			if (key != null & value != null) {
				metadata.put(key, value);
			}
		}

		/**
		 * Returns a list of all available metadata entries stored in this object.
		 * @return a list of all available metadata entries stored in this object.
		 */
		public Set<Entry<ReportMetadataFields, String>> listAvailableMetadata() {
			return metadata.entrySet();
		}

	}

	/**
	 * Internal class, representing DUT Information of a report.
	 */
	static class ReportDutInformation {
		private final HashMap<ReportDutInfoFields, String> dutInformation;

		/**
		 * Default constructor. Stores no DUT information.
		 */
		public ReportDutInformation() {
			dutInformation = new HashMap<>();
		}

		/**
		 * Constructor using a set of available DUT Information.
		 * @param dutInformation DUT Information to store in this object.
		 */
		public ReportDutInformation(final HashMap<ReportDutInfoFields, String> dutInformation) {
			this.dutInformation = dutInformation;
		}

		/**
		 * Add some DUT Information to this object.
		 * @param key The ReportDutInfoFields of the DUT Information to store.
		 * @param value The value of the DUT Information to store.
		 */
		public void appendDutInformation(final ReportDutInfoFields key, final String value) {
			if (key != null & value != null) {
				dutInformation.put(key, value);
			}
		}

		/**
		 * Returns a list of all available DUT Information entries stored in this object.
		 * @return a list of all available DUT Information entries stored in this object.
		 */
		public Set<Entry<ReportDutInfoFields, String>> listAvailableDutInformation() {
			return dutInformation.entrySet();
		}
	}

	/**
	 * Class representing an abstract report of a Test Suite Execution.
	 */
	public static class TestSuiteReport {
		private final String testSuiteId;
		private final TestSuiteSummary summary;
		private final List<TestCaseReport> testCases;
		private final List<LogEntry> testSuiteLogMessages;

		/**
		 * Constructor using all the information stored in an TestSuiteReport.
		 * @param testSuiteId The ID of the Test Suite.
		 * @param summary The TestSuiteSummary of the executed TestCases.
		 * @param testCases The List of abstract TestCaseReports.
		 * @param testSuiteLogMessages The list of Log Messages, which have been logged on a Test Suite Level.
		 */
		public TestSuiteReport(final String testSuiteId, final TestSuiteSummary summary,
				final List<TestCaseReport> testCases,
				final List<LogEntry> testSuiteLogMessages) {
			this.testSuiteId = testSuiteId;
			this.summary = summary;
			this.testCases = new ArrayList<>(testCases);
			this.testSuiteLogMessages = new ArrayList<>(testSuiteLogMessages);

		}

		/**
		 * Returns the stored Test Suite ID.
		 * @return the stored Test Suite ID.
		 */
		public String getTestSuiteId() {
			return testSuiteId;
		}

		/**
		 * Returns the stored TestSuiteSummary.
		 * @return the stored TestSuiteSummary
		 */
		public TestSuiteSummary getSummary() {
			return summary;
		}

		/**
		 * Returns the list of Log Messages, which have been logged on a Test Suite Level.
		 * @return The list of Log Messages, which have been logged on a Test Suite Level.
		 */
		public List<LogEntry> getTestSuiteLogMessages() {
			return new ArrayList<>(testSuiteLogMessages);
		}

		/**
		 * Returns The List of abstract TestCaseReports stored in this object.
		 * @return The List of abstract TestCaseReports.
		 */
		public List<TestCaseReport> getTestCaseReports() {
			return new ArrayList<>(testCases);
		}

		/**
		 * Constructor, which takes all necessary information to generate a TestSuiteSummary object stored internally.
		 * @param testSuiteId The ID of the Test Suite.
		 * @param startTime The Start Time of the Test Suite Execution.
		 * @param endTime The End Time of the Test Suite Execution.
		 * @param testCases The List of abstract TestCaseReports.
		 * @param testSuiteLogMessages The list of Log Messages, which have been logged on a Test Suite Level.
		 */
		public TestSuiteReport(final String testSuiteId, final ZonedDateTime startTime, final ZonedDateTime endTime,
				final List<TestCaseReport> testCases, final List<LogEntry> testSuiteLogMessages) {
			Integer totalNumberOfTestcases = 0;
			Integer numberOfExecutedTestcases = 0;
			Integer numberOfPassedTestcases = 0;
			Integer numberOfFailedTestcases = 0;
			Integer numberOfTestcasesWithWarnings = 0;
			for (var testCaseReport : testCases) {
				totalNumberOfTestcases++;
				switch (testCaseReport.getTestCaseResult()) {
					case PASSED:
						numberOfExecutedTestcases++;
						numberOfPassedTestcases++;
						break;
					case PASSED_WITH_WARNINGS:
						numberOfExecutedTestcases++;
						numberOfTestcasesWithWarnings++;
						break;
					case FAILED:
						numberOfExecutedTestcases++;
						numberOfFailedTestcases++;
						break;
					case SKIPPED:
					case INCONCLUSIVE:
						break;
					default:
						// Unknown. Do nothing.
						break;
				}
			}

			this.testSuiteId = testSuiteId;
			this.testCases = new ArrayList<>(testCases);
			this.testSuiteLogMessages = new ArrayList<>(testSuiteLogMessages);
			summary = new TestSuiteSummary(totalNumberOfTestcases, numberOfExecutedTestcases,
					numberOfPassedTestcases, numberOfFailedTestcases, numberOfTestcasesWithWarnings, startTime,
					endTime);
		}
	}

	/**
	 * An internal class, representing a summary of an Test Suite Execution.
	 */
	public static class TestSuiteSummary {
		private final Integer totalNumberOfTestcases;

		private final Integer numberOfExecutedTestcases;
		private final Integer numberOfPassedTestcases;
		private final Integer numberOfFailedTestcases;
		private final Integer numberOfTestcasesWithWarnings;
		private final ZonedDateTime startTime;
		private final ZonedDateTime endTime;

		/**
		 * Constructor using all necesarry information.
		 * @param totalNumberOfTestcases The total number of test cases.
		 * @param numberOfExecutedTestcases The number of executed test cases.
		 * @param numberOfPassedTestcases The number of passed test cases.
		 * @param numberOfFailedTestcases The number of failed test cases.
		 * @param numberOfTestcasesWithWarnings The number of test cases with warnings.
		 * @param startTime The start time of the test suite.
		 * @param endTime The end time of the test suite.
		 */
		public TestSuiteSummary(final Integer totalNumberOfTestcases, final Integer numberOfExecutedTestcases,
				final Integer numberOfPassedTestcases, final Integer numberOfFailedTestcases,
				final Integer numberOfTestcasesWithWarnings,
				final ZonedDateTime startTime, final ZonedDateTime endTime) {
			this.totalNumberOfTestcases = totalNumberOfTestcases;
			this.numberOfExecutedTestcases = numberOfExecutedTestcases;
			this.numberOfPassedTestcases = numberOfPassedTestcases;
			this.numberOfFailedTestcases = numberOfFailedTestcases;
			this.numberOfTestcasesWithWarnings = numberOfTestcasesWithWarnings;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		/**
		 * Returns the total number of test cases.
		 * @return the the total number of test cases.
		 */
		public Integer getTotalNumberOfTestcases() {
			return totalNumberOfTestcases;
		}

		/**
		 * Returns the number of executed test cases.
		 * @return the numberOfExecutedTestcases
		 */
		public Integer getNumberOfExecutedTestcases() {
			return numberOfExecutedTestcases;
		}

		/**
		 * Returns the number of passed test cases.
		 * @return the numberOfPassedTestcases
		 */
		public Integer getNumberOfPassedTestcases() {
			return numberOfPassedTestcases;
		}

		/**
		 * Returns the number of failed test cases.
		 * @return the numberOfFailedTestcases
		 */
		public Integer getNumberOfFailedTestcases() {
			return numberOfFailedTestcases;
		}

		/**
		 * Returns the number of test cases with warnings.
		 * @return the numberOfTestcasesWithWarnings
		 */
		public Integer getNumberOfTestcasesWithWarnings() {
			return numberOfTestcasesWithWarnings;
		}

		/**
		 * Returns the start time of the test suite execution.
		 * @return the startTime
		 */
		public ZonedDateTime getStartTime() {
			return startTime;
		}

		/**
		 * Returns the end time of the test suite execution.
		 * @return the endTime
		 */
		public ZonedDateTime getEndTime() {
			return endTime;
		}
	}

	/**
	 * Enum representing the possible test case results.
	 */
	public enum TestCaseResult {
		PASSED,
		PASSED_WITH_WARNINGS,
		FAILED,
		SKIPPED,
		INCONCLUSIVE;
	}

	/**
	 * An abstract test case report.
	 */
	public static class TestCaseReport {
		private final String testCaseId;
		private final String description;
		private final String purpose;
		private final TestCaseResult tcResult;
		private final ZonedDateTime startTime;
		private final ZonedDateTime endTime;
		private final List<LogEntry> logMessages;

		/**
		 * Returns the test cases ID.
		 * @return the testCaseId
		 */
		public String getTestCaseId() {
			return testCaseId;
		}

		/**
		 * Returns the description of the test case.
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Returns the purpose of the test case.
		 * @return the purpose
		 */
		public String getPurpose() {
			return purpose;
		}

		/**
		 * Returns the start time of the test case execution.
		 * @return the startTime
		 */
		public ZonedDateTime getStartTime() {
			return startTime;
		}

		/**
		 * Returns the end time of the test case execution.
		 * @return the endTime
		 */
		public ZonedDateTime getEndTime() {
			return endTime;
		}

		/**
		 * Returns a list of log messages, which have been logged in the test case execution.
		 * @return the list of log messages, which have been logged in the test case execution.
		 */
		public List<LogEntry> getLogMessages() {
			return new ArrayList<>(logMessages);
		}

		/**
		 * Constructor, stored all information about a test case execution.
		 * @param testCaseId The ID of the test case.
		 * @param description The description of the test case.
		 * @param purpose The purpose of the test case.
		 * @param tcResult The result of the test case.
		 * @param startTime The start time of the test case execution.
		 * @param endTime The end time of the test case execution.
		 * @param logMessages The list of log messages logged during the test case execution.
		 */
		public TestCaseReport(final String testCaseId, final String description, final String purpose,
				final TestCaseResult tcResult,
				final ZonedDateTime startTime, final ZonedDateTime endTime, final List<LogEntry> logMessages) {
			this.testCaseId = testCaseId;
			this.description = description;
			this.purpose = purpose;
			this.tcResult = tcResult;
			this.startTime = startTime;
			this.endTime = endTime;
			this.logMessages = new ArrayList<>(logMessages);
		}

		/**
		 * Return the result of the test case execution.
		 * @return the result of the test case execution.
		 */
		public TestCaseResult getTestCaseResult() {
			return tcResult;
		}

	}

	/**
	 * Class used to specify input parameters.
	 */
	public static class InputParameters {
		/**
		 * Only one of mics and testRunPlan can be used as input, so only one can be non-null here.
		 */
		final ICS mics;
		final TestRunPlan testRunPlan;

		/**
		 * Constructor using the Input Parameter of type MICS.
		 * @param mics The raw ICS which has been parsed.
		 */
		public InputParameters(final ICS mics) {
			this(mics, null);
		}

		/**
		 * Constructor using the Input Parameter of type Test Run Plan.
		 * @param testRunPlan The raw TestRunPlan which has been parsed.
		 */
		public InputParameters(final TestRunPlan testRunPlan) {
			this(null, testRunPlan);
		}

		private InputParameters(final ICS mics, final TestRunPlan testRunplan) {
			this.testRunPlan = testRunplan;
			this.mics = mics;
		}

		/**
		 * Check if an MICS Input Parameter is set.
		 * @return Boolean indicating, if an MICS Input Parameter is set.
		 */
		public boolean isMicsSet() {
			return mics != null;
		}

		/**
		 * Check if an TestRunPlan Input Parameter is set.
		 * @return Boolean indicating, if an TestRunPlan Input Parameter is set.
		 */
		public boolean isTestRunPlanSet() {
			return testRunPlan != null;
		}

		/**
		 * Get the ICS Input Parameter stored in this object.
		 * @return the ICS Input Parameter stored in this object.
		 */
		public JAXBElement<ICS> getMics() {
			return new JAXBElement<>(new QName("ICS"), ICS.class, mics);
		}

		/**
		 * Get the TestRunPlan Input Parameter stored in this object.
		 * @return the TestRunPlan Input Parameter stored in this object.
		 */
		public JAXBElement<TestRunPlan> getTestRunPlan() {
			return new JAXBElement<>(new QName("TestRunPlan"), TestRunPlan.class, testRunPlan);
		}
	}
}
