package com.achelos.task.abstracttestsuite;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Entity that provides test suite run information.
 */
public class TestSuiteRun {

	private final String testSuiteIdentifier;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;

	private final List<String> testCaseIdentifierList;

	/**
	 * Default constructor to create a new test suite run object.
	 * 
	 * @param testSuiteIdentifier the test suite identifier
	 * @param testCaseIdentifierList the test case identifier list
	 */
	public TestSuiteRun(final String testSuiteIdentifier, final List<String> testCaseIdentifierList) {
		this.testSuiteIdentifier = testSuiteIdentifier;
		this.testCaseIdentifierList = new ArrayList<>(testCaseIdentifierList);
	}

	/**
	 * Set the start time of this test suite run.
	 * 
	 * @param startTime the start time to set
	 */
	public void setStartTime(final ZonedDateTime startTime) {
		this.startTime = startTime;
	}

	/**
	 * Sets the current date-time from the system clock in the default time-zone and sets as a start time of this
	 * test suite run.
	 * @see {@link ZonedDateTime#now()}
	 */
	public void setStartTime() {
		setStartTime(ZonedDateTime.now());
	}

	/**
	 * Set the end time of this test suite run.
	 * 
	 * @param endTime the end time to set
	 */
	public void setEndTime(final ZonedDateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * Sets the current date-time from the system clock in the default time-zone and sets as an end time of this
	 * test suite run.
	 * @see {@link ZonedDateTime#now()}
	 */
	public void setEndTime() {
		setEndTime(ZonedDateTime.now());
	}

	/**
	 * Gets the list by constructing a new order list containing the test case identifier elements.
	 * 
	 * @return the ordered list containing the test case identifier elements
	 */
	public List<String> getTestCases() {
		return new LinkedList<>(testCaseIdentifierList);
	}

	/**
	 * Returns the test suite identifier.
	 * 
	 * @return the test suite identifier
	 */
	public String getTestSuiteIdentifier() {
		return testSuiteIdentifier;
	}

	/**
	 * Returns the start time of this test suite run. 
	 * @return the start time of this test suite run.
	 */
	public ZonedDateTime getStartTime() {
		return startTime;
	}

	/**
	 * Returns the end time of this test suite run. 
	 * @return the end time of this test suite run.
	 */
	public ZonedDateTime getEndTime() {
		return endTime;
	}


}
