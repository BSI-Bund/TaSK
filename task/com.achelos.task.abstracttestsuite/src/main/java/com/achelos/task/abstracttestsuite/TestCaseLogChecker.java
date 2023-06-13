package com.achelos.task.abstracttestsuite;


import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.logging.LoggingConnector.AbstractLogEntryChecker;
import com.achelos.task.logging.LoggingConnector.LogEntry;
import com.achelos.task.logging.LoggingConnector.TellLoggerEntry;


/**
 * Class for checking the logging inside an execution of a test case.
 */
public class TestCaseLogChecker extends AbstractLogEntryChecker {

	private final TestCaseRun testCaseRun;


	/**
	 * Constructor. Reads out the stop condition.
	 *
	 * @param testCaseRun the test case run that will be executed
	 */
	public TestCaseLogChecker(final TestCaseRun testCaseRun) {
		this.testCaseRun = testCaseRun;
	}

	/**
	 * Increments the test case run warnings, errors and fatal errors base on log level. {@link BasicLogger#ERROR}
	 * {@link BasicLogger#WARNING}. Distributes the log afterwards.
	 *
	 * @param entry the incoming log entry
	 */
	@Override
	public final LogEntry updateErrorsAndWarnings(final LogEntry entry) {
		LogEntry resultEntry = entry;
		Long newLvl = entry.getLogLevel();

		if (newLvl == BasicLogger.WARNING) {
			testCaseRun.increaseWarningCount();
		} else if (newLvl == BasicLogger.ERROR) {
			testCaseRun.increaseErrorCount();
		} else if (newLvl == BasicLogger.FATAL_ERROR) {
			testCaseRun.increaseFatalErrorCount();
		}

		return resultEntry;
	}

	@Override
	public final TellLoggerEntry resetLogEntryChecker(final TellLoggerEntry entry) {
		if (BasicLogger.MSG_TESTCASE_ENDED.equals(entry.getMsg())) {
			LoggingConnector.resetInstanceLogEntryChecker(this);
		}
		return super.resetLogEntryChecker(entry);
	}

}
