// Copyright (C) 2019 achelos GmbH
// All rights reserved.
package com.achelos.task.abstracttestsuite;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * Executor class that takes care of test case execution.
 */
public class TestCaseExecutor implements Runnable {
	private final AbstractTestCase testCase;
	private final TestCaseRun testCaseRun;
	private final Consumer<TestCaseRun> testCaseRunSaver;
	private final LoggingConnector logger;

	/**
	 * Creates a new instance of test case executor to execute the provided test case.
	 * 
	 * @param testCase the test case that will be executed
	 * @param testCaseRun the test case run to store the execution results into
	 * @param testCaseRunSaver operation to persist changes to the given test case run
	 */
	public TestCaseExecutor(final AbstractTestCase testCase, final TestCaseRun testCaseRun,
			final Consumer<TestCaseRun> testCaseRunSaver) {
		this.testCase = testCase;
		this.testCaseRun = testCaseRun;
		this.testCaseRunSaver = testCaseRunSaver;
		logger = LoggingConnector.getInstance();
	}

	/**
	 * Start the execution of the test case.
	 */
	@Override
	public final void run() {

		RunState runState = RunState.RUNNING;
		changeState(testCaseRun, runState);
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testCaseRun);
		logger.info("Test case: " + testCase.getTestCaseId());
		logger.info("Test case description: " + testCase.getTestCaseDescription());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCase.getTestCaseDescription());
		logger.info("Test case purpose: " + testCase.getTestCasePurpose());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCase.getTestCasePurpose());
		final TestCaseLogChecker logChecker = new TestCaseLogChecker(testCaseRun);
		LoggingConnector.setInstanceLogEntryChecker(logChecker);

		try {
			try {
				testCase.testSetUp();
				checkForFatalErrors();

				testCase.testPreprocessing();
				checkForFatalErrors();

				testCase.testExecution();
				checkForFatalErrors();

				testCase.testPostprocessing();
				checkForFatalErrors();

			} finally {
				try {
					testCase.testTearDown();
					checkForFatalErrors();
					runState = RunState.FINISHED;
				} catch (Exception e) {
					runState = RunState.CANCELED;
				}
			}
		} catch (Exception e) {
			runState = RunState.CANCELED;
		} finally {
			changeState(testCaseRun, runState);
		}
	}

	/**
	 * Check if fatal errors were logged without throwing an exception.
	 *
	 * @throws RuntimeException exception if fatal errors were logged.
	 * @see BasicLogger#FATAL_ERROR
	 */
	private void checkForFatalErrors() throws RuntimeException {
		if (testCaseRun.getFatalErrorCount() > 0) {
			throw new RuntimeException("Fatal Error occurred.");
		}
	}

	/**
	 * This method should be used to set new state for the execution test case run. Depended on the state the last flag,
	 * start and stop time are set in this method.
	 *
	 * @param tcr test case run that gets new state.
	 * @param newState the new run state
	 */
	protected void changeState(final TestCaseRun tcr, final RunState newState) {
		RunState prevState = tcr.getState();
		tcr.setState(newState);
		if (prevState == RunState.WAITING && newState == RunState.RUNNING) {
			tcr.setStartTime(ZonedDateTime.now());
		} else if (RunState.isEnded(newState)) {
			tcr.setStopTime(ZonedDateTime.now());
		}
		testCaseRunSaver.accept(tcr);
	}

}
