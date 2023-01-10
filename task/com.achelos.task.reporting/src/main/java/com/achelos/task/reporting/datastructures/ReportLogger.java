package com.achelos.task.reporting.datastructures;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.achelos.task.abstracttestsuite.RunState;
import com.achelos.task.abstracttestsuite.TestCaseRun;
import com.achelos.task.abstracttestsuite.TestSuiteRun;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector.LogEntry;
import com.achelos.task.logging.ReportDutInfoFields;
import com.achelos.task.logging.ReportMetadataFields;
import com.achelos.task.reporting.datastructures.Report.InputParameters;
import com.achelos.task.reporting.datastructures.Report.ReportDutInformation;
import com.achelos.task.reporting.datastructures.Report.ReportMetadata;
import com.achelos.task.reporting.datastructures.Report.TestCaseReport;
import com.achelos.task.reporting.datastructures.Report.TestCaseResult;
import com.achelos.task.reporting.datastructures.Report.TestSuiteReport;
import com.achelos.task.utilities.DateTimeUtils;

import generated.jaxb.input.ICS;
import generated.jaxb.testrunplan.TestRunPlan;


/**
 * This logger stores messages to into internal data structures to create reports (e.g. XML or PDF) afterwards.
 */
public class ReportLogger extends BasicLogger {

	private final ReportMetadata metadata;
	private final ReportDutInformation dutInformation;
	private final List<TestSuiteReport> testSuites;
	private InputParameters inputParameters;
	private final List<LogEntry> testFrameworkLogMessages;

	private TestSuiteReportBuilder currentTestSuite;

	/**
	 * Default constructor. Initializes empty internal data structures.
	 */
	public ReportLogger() {
		super();
		metadata = new ReportMetadata();
		dutInformation = new ReportDutInformation();
		testSuites = new LinkedList<>();
		inputParameters = null;
		testFrameworkLogMessages = new LinkedList<>();

		currentTestSuite = null;
	}

	/**
	 * Generate an abstract report object with the stored information of this logger.
	 * @return an abstract report object with the stored information of this logger.
	 */
	public Report generateReport() {
		// Add ReportGenerationDate to Metadata
		metadata.appendMetadata(ReportMetadataFields.DATE_OF_REPORT_GENERATION,
				DateTimeUtils.getISOFormattedTimeStamp());

		return new Report(metadata, dutInformation, testSuites, inputParameters, testFrameworkLogMessages);
	}

	@Override
	public void log(final long timestamp, final long lvl, final String log, final Throwable t) {
		if (lvl == BasicLogger.DEBUG) {
			return;
		}
		var logEntry = new LogEntry(timestamp, lvl, log, t);
		if (isCurrentTestSuiteActive()) {
			currentTestSuite.appendLogEntry(logEntry);
		} else {
			testFrameworkLogMessages.add(logEntry);
		}

	}

	@Override
	public void tellLogger(final String topic, final Object value) {
		switch (topic) {
			case BasicLogger.MSG_NEW_TESTCASE: {
				String testCaseName = null;
				ZonedDateTime startTime = null;
				if (value instanceof TestCaseRun) {
					testCaseName = ((TestCaseRun) value).getTestCaseName();
					startTime = ((TestCaseRun) value).getStartTimestamp();
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: test case shall be started, but no startTime or test case name has been provided.",
							null);
					testCaseName = "Unknown Testcase";
					startTime = ZonedDateTime.now();
				}
				if (isCurrentTestSuiteActive()) {
					if (isTestCaseActive()) {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: New test case shall be started, but the latest test case has not been marked as finished.",
								null);
						try {
							currentTestSuite.finalizeTestCase(startTime);
						} catch (Exception e) {
							testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
									"Logging: Error occurred while generating test case report.", e));
						}
					}
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: New test case shall be started, but no test suite was marked active.", null);
					currentTestSuite = new TestSuiteReportBuilder("Unknown TestSuite", startTime);
				}
				try {
					currentTestSuite.startTestCase(testCaseName, startTime);
				} catch (Exception e) {
					testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
							"Logging: Error occurred while generating test case report.", e));
				}
				break;
			}
			case BasicLogger.MSG_TESTCASE_ENDED: {
				ZonedDateTime endTime = null;
				TestCaseResult tcResult = TestCaseResult.INCONCLUSIVE;
				List<LogEntry> statusMessages = new LinkedList<>();
				if (value instanceof TestCaseRun) {
					var tcr = (TestCaseRun) value;
					endTime = tcr.getStopTime();
					var tcrStatusMessages = tcr.getStatusMessages();
					var statusMessageLevel = BasicLogger.INFO;
					if (RunState.isEnded(tcr.getState())) {
						if (tcr.getState() == RunState.FINISHED) {
							if (tcr.getErrorCount() + tcr.getFatalErrorCount() > 0) {
								tcResult = TestCaseResult.FAILED;
								statusMessageLevel = BasicLogger.ERROR;
							} else if (tcr.getWarningCount() > 0) {
								tcResult = TestCaseResult.PASSED_WITH_WARNINGS;
								statusMessageLevel = BasicLogger.WARNING;
							} else {
								tcResult = TestCaseResult.PASSED;
							}
							if (tcrStatusMessages != null) {
								for (var message : tcrStatusMessages) {
									statusMessages.add(new LogEntry(endTime.toEpochSecond() * 1000, statusMessageLevel,
											message, null));
								}
							}
						} else {
							tcResult = TestCaseResult.FAILED;
						}
					}
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: test case shall be started, but no startTime or test case name has been provided.",
							null);
					endTime = ZonedDateTime.now();
				}
				if (isCurrentTestSuiteActive()) {
					if (isTestCaseActive()) {
						try {
							for (var logEntry : statusMessages) {
								currentTestSuite.appendLogEntry(logEntry);
							}
							currentTestSuite.finalizeTestCase(endTime, tcResult);
						} catch (Exception e) {
							testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
									"Logging: Error occurred while generating test case report.", e));
						}
					} else {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: test case shall be marked finished, but no test case was marked active.",
								null);
					}
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: test case shall be marked finished, but no test suite was marked active.", null);
				}
				break;
			}
			case BasicLogger.MSG_NEW_TESTSUITE: {
				String testSuiteIdentifier = null;
				ZonedDateTime startTime = null;
				if (value instanceof TestSuiteRun) {
					startTime = ((TestSuiteRun) value).getStartTime();
					testSuiteIdentifier = ((TestSuiteRun) value).getTestSuiteIdentifier();
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: New test suite shall be started, but no start time has been provided.", null);
					startTime = ZonedDateTime.now();
					testSuiteIdentifier = "Unknown Test Suite";
				}
				if (isCurrentTestSuiteActive()) {
					if (isTestCaseActive()) {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: New test suite shall be started, but an the latest test suite and test case has not been marked as finished.",
								null);
						try {
							currentTestSuite.finalizeTestCase(startTime);
						} catch (Exception e) {
							testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
									"Logging: Error occurred while generating test case report.", e));
						}
					} else {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: New test suite shall be started, but an the latest test suite has not been marked as finished.",
								null);
					}
					finalizeCurrentTestSuite(startTime);
				}
				startNewTestSuite(testSuiteIdentifier, startTime);
				break;
			}
			case BasicLogger.MSG_TESTSUITE_ENDED: {
				ZonedDateTime endTime = null;
				if (value instanceof TestSuiteRun) {
					endTime = ((TestSuiteRun) value).getEndTime();
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: Test suite shall be marked finished, but no end time has been provided.", null);
					endTime = ZonedDateTime.now();
				}
				if (isCurrentTestSuiteActive()) {
					if (isTestCaseActive()) {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: Test suite shall be marked as finished, but an the latest test case has not been marked as finished.",
								null);
						try {
							currentTestSuite.finalizeTestCase(endTime);
						} catch (Exception e) {
							testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
									"Logging: Error occurred while generating test case report.", e));
						}
					}
					finalizeCurrentTestSuite(endTime);
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: Test suite shall be marked as finished, but no test suite is marked as active.",
							null);
				}
				break;
			}
			case BasicLogger.MSG_TESTCASE_DESCRIPTION: {
				if (isCurrentTestSuiteActive()) {
					if (isTestCaseActive()) {
						if (value instanceof String) {
							currentTestSuite.currentTestCase.setDescription((String) value);
						} else {
							log(System.currentTimeMillis(), BasicLogger.WARNING,
									"Logging: test case Description provided, but is of invalid type.", null);
						}
					} else {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: test case Description provided, but no test case is marked as active.", null);
					}
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: test case Description provided, but no test suite is marked as active.", null);
				}
				break;
			}
			case BasicLogger.MSG_TESTCASE_PURPOSE: {
				if (isCurrentTestSuiteActive()) {
					if (isTestCaseActive()) {
						if (value instanceof String) {
							currentTestSuite.currentTestCase.setPurpose((String) value);
						} else {
							log(System.currentTimeMillis(), BasicLogger.WARNING,
									"Logging: test case purpose provided, but is of invalid type.", null);
						}
					} else {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: test case purpose provided, but no test case is marked as active.", null);
					}
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: test case purpose provided, but no test suite is marked as active.", null);
				}
				break;
			}
			case BasicLogger.MSG_TESTRUNPLAN: {
				if (value instanceof TestRunPlan) {
					inputParameters = new InputParameters((TestRunPlan) value);
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: TestRunPlan should have been set, but the data is of wrong type.", null);
				}
				break;
			}
			case BasicLogger.MSG_MICS: {
				if (value instanceof ICS) {
					inputParameters = new InputParameters((ICS) value);
				} else {
					log(System.currentTimeMillis(), BasicLogger.WARNING,
							"Logging: The MICS file should have been set, but the data is of wrong type.", null);
				}
				break;
			}
			case BasicLogger.MSG_METADATA: {
				if (value instanceof Map.Entry<?, ?>) {
					var key = ((Map.Entry<?, ?>) value).getKey();
					var entry = ((Map.Entry<?, ?>) value).getValue();
					if (key instanceof ReportMetadataFields && entry instanceof String) {
						metadata.appendMetadata((ReportMetadataFields) key, (String) entry);
					} else if (key instanceof ReportDutInfoFields && entry instanceof String) {
						dutInformation.appendDutInformation((ReportDutInfoFields) key, (String) entry);
					} else {
						log(System.currentTimeMillis(), BasicLogger.WARNING,
								"Logging: Metadata should have been set, but the data is of wrong type.", null);
					}
				}
				break;
			}
			default:
				break;
		}

	}

	/**
	 * Check whether a test suite is currently active.
	 *
	 * @return true if a test suite is currently active.
	 */
	private boolean isCurrentTestSuiteActive() {
		return currentTestSuite != null;
	}

	/**
	 * Check whether the currently active test suite has a currently active test case.
	 *
	 * @return True iff. the currently active test suite has a currently active test case.
	 */
	private boolean isTestCaseActive() {
		if (currentTestSuite != null) {
			return currentTestSuite.currentTestCase != null;
		}
		return false;
	}

	/**
	 * Finalize the current test suite report builder, generate the report of the test suite and add it to the list of
	 * test suite reports.
	 *
	 * @param endTime The end time of the current test suite.
	 */
	private void finalizeCurrentTestSuite(final ZonedDateTime endTime) {
		if (isCurrentTestSuiteActive()) {
			currentTestSuite.setEndTime(endTime);
			try {
				testSuites.add(currentTestSuite.generateReport());
			} catch (Exception e) {
				testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
						"Logging: Error generating test suite report.", e));
			}
			currentTestSuite = null;
		}
	}

	/**
	 * Start a new test suite report builder as the current test suite. Use the test suite identifier and Start time
	 * provided.
	 *
	 * @param testSuiteId Identifier of new Test Suite.
	 * @param startTime Start time of new Test Suite.
	 */
	private void startNewTestSuite(final String testSuiteId, final ZonedDateTime startTime) {
		if (isCurrentTestSuiteActive()) {
			testFrameworkLogMessages.add(new LogEntry(System.currentTimeMillis(), ERROR,
					"Logging: New test suite shall be started, even though the current test Suite is not marked as finished yet. Aborting",
					null));
		} else {
			currentTestSuite = new TestSuiteReportBuilder(testSuiteId, startTime);
		}
	}

	/**
	 * Internal class, used to build a test suite report.
	 */
	private static class TestSuiteReportBuilder {
		private final String testSuiteId;
		private final ZonedDateTime startTime;
		private ZonedDateTime endTime;
		private final List<TestCaseReport> testCases;
		private final List<LogEntry> testSuiteLogMessages;

		private TestCaseReportBuilder currentTestCase;

		/**
		 * Constructor using test suite identifier and start time.
		 *
		 * @param testSuiteId Identifier of Test Suite.
		 * @param startTime Start time of Test Suite.
		 */
		protected TestSuiteReportBuilder(final String testSuiteId, final ZonedDateTime startTime) {
			this.testSuiteId = testSuiteId;
			this.startTime = startTime;
			endTime = null;
			testCases = new LinkedList<>();
			testSuiteLogMessages = new LinkedList<>();

			currentTestCase = null;
		}

		/**
		 * Set the endTime of the test suite.
		 *
		 * @param endTime
		 */
		public void setEndTime(final ZonedDateTime endTime) {
			this.endTime = endTime;
		}

		/**
		 * Generate a TestSuiteReport with the currently contained information.
		 *
		 * @return a TestSuiteReport with the currently contained information.
		 */
		public TestSuiteReport generateReport() {
			if (endTime == null) {
				throw new IllegalStateException(
						"Command received to generate report for an unfinished TestSuite. EndTime is not set. Aborting.");
			}
			return new TestSuiteReport(testSuiteId, startTime, endTime, testCases, testSuiteLogMessages);
		}

		public void startTestCase(final String tcName, final ZonedDateTime startTime) {
			if (currentTestCase != null) {
				throw new IllegalStateException(
						"Command received to start new test case, but old test case has not finished yet. Aborting.");
			}
			currentTestCase = new TestCaseReportBuilder(tcName, startTime);
		}

		public void finalizeTestCase(final ZonedDateTime endTime) {
			if (currentTestCase == null) {
				throw new IllegalStateException(
						"Command received to finalize test case report, but no test case report is active. Aborting.");
			}
			currentTestCase.setEndTime(endTime);
			testCases.add(currentTestCase.generateReport());
			currentTestCase = null;
		}

		public void finalizeTestCase(final ZonedDateTime endTime, final TestCaseResult tcResult) {
			if (currentTestCase == null) {
				throw new IllegalStateException(
						"Command received to finalize test case report, but no test case report is active. Aborting.");
			}
			currentTestCase.setTCResult(tcResult);
			finalizeTestCase(endTime);
		}

		public void appendLogEntry(final LogEntry logEntry) {
			if (currentTestCase != null) {
				currentTestCase.appendLogEntry(logEntry);
			} else {
				testSuiteLogMessages.add(logEntry);
			}
		}

	}

	/**
	 * Internal class used to generate test case reports.
	 */
	private static class TestCaseReportBuilder {
		private final String testCaseId;
		private String description;
		private String purpose;
		private TestCaseResult tcResult;
		private final ZonedDateTime startTime;
		private ZonedDateTime endTime;
		private final List<LogEntry> logMessages;

		/**
		 * Constructor using TestCaseName and StartTime.
		 *
		 * @param tcName Name/Identifier of TestCase
		 * @param startTime Start time of the TestCase
		 */
		protected TestCaseReportBuilder(final String tcName, final ZonedDateTime startTime) {
			testCaseId = tcName;
			description = "Unknown description";
			purpose = "Unknown purpose";
			tcResult = TestCaseResult.INCONCLUSIVE;
			this.startTime = startTime;
			endTime = null;
			logMessages = new LinkedList<>();
		}

		/**
		 * Set the description of the test case.
		 *
		 * @param description the description of the TestCase.
		 */
		public void setDescription(final String description) {
			this.description = description;
		}

		/**
		 * Set the purpose of the test case.
		 *
		 * @param purpose the purpose of the TestCase.
		 */
		public void setPurpose(final String purpose) {
			this.purpose = purpose;
		}

		/**
		 * Set the Result of the test case.
		 *
		 * @param tcResult Result of the test case.
		 */
		public void setTCResult(final TestCaseResult tcResult) {
			this.tcResult = tcResult;
		}

		/**
		 * Set the end time of the test case.
		 *
		 * @param endTime end time of the test case.
		 */
		public void setEndTime(final ZonedDateTime endTime) {
			this.endTime = endTime;
		}

		/**
		 * Append LogEntry to Log Messages list.
		 *
		 * @param logEntry log entry to append to log messages list.
		 */
		public void appendLogEntry(final LogEntry logEntry) {
			logMessages.add(logEntry);
		}

		public TestCaseReport generateReport() {
			return new TestCaseReport(testCaseId, description, purpose, tcResult, startTime, endTime, logMessages);
		}
	}

}
