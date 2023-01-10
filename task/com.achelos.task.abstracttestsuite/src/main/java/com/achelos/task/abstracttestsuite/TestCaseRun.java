package com.achelos.task.abstracttestsuite;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.utilities.DateTimeUtils;


/**
 * Entity that provides test case run information.
 */
public class TestCaseRun {

	private String testCaseName;

	private RunState state;

	private int warningCount = 0;

	private int errorCount = 0;

	private int fatalErrorCount = 0;

	private ZonedDateTime startTimestamp;
	private ZonedDateTime stopTimestamp;

	private String testerInCharge;

	private List<String> statusMessages;

	/**
	 * Default constructor.
	 */
	public TestCaseRun() {}

	/**
	 * Constructor to use for creation of new instance to persist.
	 *
	 * @param testCaseName the related test case name
	 * @param status the last run status for the given test case
	 * @param testerInCharge tester user token
	 */
	public TestCaseRun(final String testCaseName, final RunState status, final String testerInCharge) {
		super();
		this.testCaseName = testCaseName;
		state = status;
		startTimestamp = ZonedDateTime.now();
		this.testerInCharge = testerInCharge;
		statusMessages = new ArrayList<>();
	}

	/**
	 * Gets the test case name of this test case run.
	 * @return the test case name
	 */
	public String getTestCaseName() {
		return testCaseName;
	}

	/**
	 * Gets the run state of this test case run.
	 * 
	 * @return the run state
	 * @see {@link RunState}
	 */
	public RunState getState() {
		return state;
	}

	/**
	 * Return the stored Tester in Charge.
	 *
	 * @return the stored Tester in Charge.
	 */
	public String getTesterInCharge() {
		return testerInCharge;
	}

	/**
	 * Sets the run state of this test case run.
	 * 
	 * @param newState the run state to set
	 * @see {@link RunState}
	 */

	public void setState(final RunState newState) {
		state = newState;
	}

	/**
	 * Returns the number of warnings that were reported during this test case's execution.
	 * @return number of warnings
	 */
	public int getWarningCount() {
		return warningCount;
	}

	/**
	 * Increases the number of warnings and returns the new value.
	 *
	 * @return new number of warnings
	 */

	public int increaseWarningCount() {
		return ++warningCount;
	}

	/**
	 * Returns the number of errors that were reported during this test case's execution.
	 * @return number of errors
	 */
	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * Increases the number of errors and returns the new value.
	 *
	 * @return new number of errors
	 */
	public int increaseErrorCount() {
		return ++errorCount;
	}

	/**
	 * Returns the number of fatal errors that were reported during this test case's execution.
	 * 
	 * @return number of errors
	 */
	public int getFatalErrorCount() {
		return fatalErrorCount;
	}

	/**
	 * Increases the number of fatal errors and returns the new value.
	 * An error is considered as fatal error which usually ends up in some sort of crash or exception.
	 *
	 * @return new number of fatal errors
	 */
	public int increaseFatalErrorCount() {
		return ++fatalErrorCount;
	}

	/**
	 * Return the current list of status messages contained in this {@link TestCaseRun}.
	 *
	 * @return the status messages.
	 */
	public List<String> getStatusMessages() {
		return new ArrayList<>(statusMessages);
	}

	/**
	 * Add a message to the Status Messages of this {@link TestCaseRun}.
	 *
	 * @param message The Status Message to add.
	 */
	public void addStatusMessage(final String message) {
		if (message != null && !message.isBlank()) {
			statusMessages.add(message);
		}
	}

	/**
	 * Sets the start time of the test case run.
	 *
	 * @param startTime the start time to set
	 */
	public void setStartTime(final ZonedDateTime startTime) {
		startTimestamp = startTime;
	}

	/**
	 * Returns a human-readable formatted date time for output in a log file.
	 *
	 * @return a human-readable formatted date time for output in a log file
	 */
	public final String getStartTimeFormatted() {
		return DateTimeFormatter.ofPattern(DateTimeUtils.ISO_8601_DATE_TIME_PATTERN_MILLISECONDS)
				.format(startTimestamp);
	}

	/**
	 * Return the time from the start of the test case run.
	 *
	 * @return start time of test case
	 */
	public final ZonedDateTime getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * Sets the timestamp at the end of the test case run.
	 *
	 * @param timestamp the stop time to set
	 */
	public void setStopTime(final ZonedDateTime timestamp) {
		stopTimestamp = timestamp;
	}

	/**
	 * Returns the timestamp from the end of the test case run.
	 *
	 * @return the stop time
	 */
	public final ZonedDateTime getStopTime() {
		return stopTimestamp;
	}

	/**
	 * Formats a duration given by its length in milliseconds to a printable version in the format: hh:mm:ss.ms.
	 *
	 * @return the printable version of the duration
	 */
	public String getDurationFormatted() {
		Duration d = getDuration();
		String str = String.format("%d:%02d:%02d.%03d", d.toHours(), d.toMinutesPart(), d.toSecondsPart(),
				d.toMillisPart());
		return str;
	}

	/**
	 * Returns the duration of the test case run.
	 * <p>
	 * If the run is not yet finished, the return value is {@link Duration#ZERO}
	 *
	 * @return duration of the test case run
	 */
	public final Duration getDuration() {
		Duration d;
		if (null != stopTimestamp && stopTimestamp.isAfter(startTimestamp)) {
			d = Duration.between(startTimestamp, stopTimestamp);
		} else {
			d = Duration.ZERO;
		}
		return d;
	}

}
