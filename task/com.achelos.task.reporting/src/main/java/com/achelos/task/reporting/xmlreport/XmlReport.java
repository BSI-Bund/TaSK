package com.achelos.task.reporting.xmlreport;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.reporting.datastructures.Report;
import com.achelos.task.reporting.datastructures.Report.TestCaseReport;
import com.achelos.task.reporting.datastructures.Report.TestSuiteReport;
import com.achelos.task.utilities.DateTimeUtils;
import com.achelos.task.xmlparser.outputparsing.OutputPrinter;
import generated.jaxb.xmlreport.*;
import generated.jaxb.xmlreport.TaSKReport.Metadata;
import generated.jaxb.xmlreport.TaSKReport.TaSKFrameworkMessages;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;


/**
 * Class representing an XML report.
 */
public class XmlReport {

	private TaSKReport internalStructure;

	/**
	 * Hidden Constructor.
	 */
	private XmlReport() {

	}

	/**
	 * Generate a XML Report from an abstract Report object.
	 * @param report an abstract Report object.
	 * @return a XML Report instance.
	 */
	public static XmlReport fromReportInstance(final Report report) {
		var xmlReport = new XmlReport();

		// Base Structure.
		var taskReport = new TaSKReport();

		// Metadata
		{
			var metadata = new Metadata();
			for (var entry : report.listAvailableMetadata()) {
				switch (entry.getKey()) {
					case TESTER_IN_CHARGE: {
						metadata.setTesterInCharge(entry.getValue());
						break;
					}
					case DATE_OF_REPORT_GENERATION: {
						metadata.setDateOfReportGeneration(entry.getValue());
						break;
					}
					case END_OF_EXECUTION: {
						metadata.setEndOfExecution(entry.getValue());
						break;
					}
					case EXECUTION_MACHINE_NAME: {
						metadata.setExecutionMachine(entry.getValue());
						break;
					}
					case START_OF_EXECUTION: {
						metadata.setStartOfExecution(entry.getValue());
						break;
					}
					case EXECUTION_TYPE: {
						metadata.setExecutionType(entry.getValue());
						break;
					}
					default:
						break;
				}
			}
			taskReport.setMetadata(metadata);
		}

		// DUTInformation
		{
			var dutInformation = new TaSKReport.DUTInformation();
			for (var entry : report.listAvailableDutInformation()) {
				switch (entry.getKey()) {
					case TITLE: {
						dutInformation.setTitle(entry.getValue());
						break;
					}
					case APPLICATION_TYPE: {
						dutInformation.setApplicationType(entry.getValue());
						break;
					}
					case FILE: {
						dutInformation.setFile(entry.getValue());
						break;
					}
					case VERSION: {
						dutInformation.setVersion(entry.getValue());
						break;
					}
					case FINGERPRINT: {
						dutInformation.setFingerprint(entry.getValue());
						break;
					}
					case DESCRIPTION: {
						dutInformation.setDescription(entry.getValue());
						break;
					}
					default:
						break;
				}
			}
			taskReport.setDUTInformation(dutInformation);
		}

		// Input Parameters
		{
			var inputParameters = new TaSKReport.InputParameters();
			var listOfParams = inputParameters.getAny();
			var inputParamsFromReport = report.getInputParameters();
			if (inputParamsFromReport.isMicsSet()) {
				listOfParams.add(inputParamsFromReport.getMics());
			}
			if (inputParamsFromReport.isTestRunPlanSet()) {
				listOfParams.add(inputParamsFromReport.getTestRunPlan());
			}
			taskReport.setInputParameters(inputParameters);
		}

		// TaSK framework Messages
		{
			var messages = new TaSKFrameworkMessages();
			var messagesList = messages.getLogMessage();
			for (var message : report.getTestFrameworkLogMessages()) {
				var logMessage = new LogMessage();
				logMessage.setLogLevel(BasicLogger.getName(message.getLogLevel()));
				logMessage.setValue(message.getMsg());
				logMessage.setTimestamp(epochMillisecondsToTimestamp(message.getTimestamp()));
				messagesList.add(logMessage);
			}
			taskReport.setTaSKFrameworkMessages(messages);
		}

		// Test Suites
		{
			var testSuiteList = taskReport.getTestSuite();
			for (var testSuiteReport : report.getTestSuiteReports()) {
				var testSuite = getTestSuiteStructureFromReport(testSuiteReport);
				testSuiteList.add(testSuite);
			}
		}

		// Set internal XML Structure to this instance.
		xmlReport.internalStructure = taskReport;

		return xmlReport;
	}

	/**
	 * Write the XML Report into a File.
	 * @param xmlReportFile The File to write the XML Report into.
	 */
	public void writeToFile(final File xmlReportFile) {
		OutputPrinter.printXmlReport(internalStructure, xmlReportFile);
	}

	private static String epochMillisecondsToTimestamp(final long epochMilliseconds) {
		try {
			var date = new Date(epochMilliseconds);
			DateFormat formatter = new SimpleDateFormat(DateTimeUtils.ISO_8601_DATE_TIME_PATTERN);
			formatter.setTimeZone(TimeZone.getDefault());
			return formatter.format(date);
		} catch (Exception e) {
			return "An error occurred while calculating the Timestamp";
		}
	}

	private static String zonedDateTimeToTimestamp(final ZonedDateTime dateTime) {
		try {
			var formatter = DateTimeFormatter.ofPattern(DateTimeUtils.ISO_8601_DATE_TIME_PATTERN);
			return dateTime.format(formatter);
		} catch (Exception e) {
			return "An error occurred while calculating Timestamp";
		}
	}

	private static TestSuite getTestSuiteStructureFromReport(final TestSuiteReport testSuiteReport) {
		var testSuite = new TestSuite();

		// Name or Identifier
		{
			testSuite.setTestSuiteIdentifier(testSuiteReport.getTestSuiteId());
		}

		// Summary
		{
			var summary = new Summary();
			var summaryFromReport = testSuiteReport.getSummary();

			summary.setNoOfExecTestcases(summaryFromReport.getNumberOfExecutedTestcases().toString());
			summary.setNoOfFailedTestcases(summaryFromReport.getNumberOfFailedTestcases().toString());
			summary.setNoOfPassedTestcases(summaryFromReport.getNumberOfPassedTestcases().toString());
			summary.setNoOfTestcasesWithWarnings(summaryFromReport.getNumberOfTestcasesWithWarnings().toString());
			summary.setTotalNoOfTestcases(summaryFromReport.getTotalNumberOfTestcases().toString());

			summary.setStartTime(zonedDateTimeToTimestamp(summaryFromReport.getStartTime()));
			summary.setEndTime(zonedDateTimeToTimestamp(summaryFromReport.getEndTime()));

			testSuite.setSummary(summary);
		}

		// Test suite log messages
		{
			var testSuiteMessages = new TestSuite.TestSuiteMessages();

			var messagesList = testSuiteMessages.getLogMessage();
			for (var message : testSuiteReport.getTestSuiteLogMessages()) {
				var logMessage = new LogMessage();
				logMessage.setLogLevel(BasicLogger.getName(message.getLogLevel()));
				logMessage.setValue(message.getMsg());
				logMessage.setTimestamp(epochMillisecondsToTimestamp(message.getTimestamp()));
				messagesList.add(logMessage);
			}


			testSuite.setTestSuiteMessages(testSuiteMessages);
		}

		// TestCases
		{
			var testCases = new TestSuite.TestCases();
			var testCasesList = testCases.getTestCase();
			for (var testCaseReport : testSuiteReport.getTestCaseReports()) {
				var testCase = getTestCaseStructureFromReport(testCaseReport);
				testCasesList.add(testCase);
			}

			testSuite.setTestCases(testCases);
		}

		return testSuite;
	}

	private static TestCase getTestCaseStructureFromReport(final TestCaseReport testCaseReport) {
		var testCase = new TestCase();

		// Test Case Name/Identifier
		testCase.setTestCaseId(testCaseReport.getTestCaseId());

		// Purpose
		testCase.setPurpose(testCaseReport.getPurpose());

		// Description
		testCase.setDescription(testCaseReport.getDescription());

		// Start Time
		testCase.setStartTime(zonedDateTimeToTimestamp(testCaseReport.getStartTime()));

		// End Time
		testCase.setEndTime(zonedDateTimeToTimestamp(testCaseReport.getEndTime()));

		// Result
		testCase.setResult(testCaseReport.getTestCaseResult().name());

		// TestCase Log Messages
		{
			var testCaseMessages = new TestCase.LogMessages();

			var messagesList = testCaseMessages.getLogMessage();
			for (var message : testCaseReport.getLogMessages()) {
				var logMessage = new LogMessage();
				logMessage.setLogLevel(BasicLogger.getName(message.getLogLevel()));
				logMessage.setValue(message.getMsg());
				logMessage.setTimestamp(epochMillisecondsToTimestamp(message.getTimestamp()));
				messagesList.add(logMessage);
			}

			testCase.setLogMessages(testCaseMessages);
		}

		return testCase;
	}

}
