/**
 * Copyright (C) 2009-2018 achelos GmbH All rights reserved.
 */
package com.achelos.task.abstracttestsuite;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * This class is intended to build the foundation of every test case. It contains the execution control and provides
 * abstract methods, which must be implemented, to get a full functional test case.
 */
public abstract class AbstractTestCase implements IStepExecution {

	/**
	 * The current test case state.
	 */
	protected TestCaseState currentState;

	/**
	 * Name of the test case.
	 */
	private String testCaseId;


	/**
	 * Description of the test case.
	 */
	private String testCaseDescription;

	private String testCasePurpose;

	protected final String getTestCaseDescription() {
		return testCaseDescription;
	}

	protected final void setTestCaseDescription(final String testCaseDescription) {
		this.testCaseDescription = testCaseDescription;
	}

	protected final String getTestCasePurpose() {
		return testCasePurpose;
	}

	protected final void setTestCasePurpose(final String testCasePurpose) {
		this.testCasePurpose = testCasePurpose;
	}

	/**
	 * Access to logger.
	 */
	protected LoggingConnector logger;

	/**
	 * RunPlan configuration and the MICS file data.
	 */
	protected TestRunPlanConfiguration configuration;

	/**
	 * Performs basic test case initialization.
	 */
	public AbstractTestCase() {
		currentState = TestCaseState.IDLE;
		logger = LoggingConnector.getInstance();
		configuration = TestRunPlanConfiguration.getInstance();
	}


	/**
	 * Method to provide specific exception logging.
	 *
	 * @param e exception to log
	 */
	private void logFatalException(final Exception e) {
		if (e == null) {
			logger.debug("Exception logging was called without an exception.");
			return;
		}
		logger.log(BasicLogger.FATAL_ERROR, "Exception (" + e.getClass().getSimpleName() + ").", e);
	}

	/**
	 * Gets the test case id.
	 * @return the id of the test case.
	 */
	public final String getTestCaseId() {
		return testCaseId;
	}

	protected void setTestCaseId(final String testCaseId) {
		this.testCaseId = testCaseId;
	}

	/**
	 * This method will be called before the actual {@link #preProcessing()} starts. It could be used to auto-generate
	 * the calls, used to establish a connection to the card.
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected void prepareEnvironment() throws Exception {}

	/**
	 * The pre-processing code has to be placed in this method.
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected abstract void preProcessing() throws Exception;

	/**
	 * The code of the actual use/test case should be placed in this method.
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected abstract void executeUsecase() throws Exception;

	/**
	 * The code for the post-processing should be placed in this method.
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected abstract void postProcessing() throws Exception;

	/**
	 * This method may hold code, which can/should be used to clean up a test case (and things it had done), in order to
	 * get a clean and defined state, even if the test case fails.
	 *
	 * @throws Exception if anything goes wrong
	 */
	protected abstract void cleanAndExit() throws Exception;

	/**
	 * Gets the name of the person executing the test.
	 * @return name of the person executing the test.
	 */
	public final String getTesterInCharge() {
		return configuration.getTesterInCharge();
	}

	/**
	 * Performs actions necessary when test case is idle.
	 *
	 * @throws Exception exception on test case setup.
	 */
	public void testSetUp() throws Exception {
		logger.tellLogger(BasicLogger.MSG_TC_PREPARETERMINAL_BEGIN, getTestCaseId());
		try {
			prepareEnvironment();
		} catch (TestCaseCanceledException e) {
			logger.log(BasicLogger.INFO, e.toString());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logFatalException(e);
			throw e;
		} finally {
			logger.tellLogger(BasicLogger.MSG_TC_PREPARETERMINAL_END, getTestCaseId());
		}
	}

	/**
	 * Performs actions necessary when test case starts the pre-processing.
	 *
	 * @throws Exception exception on test case pre-processing.
	 */
	public void testPreprocessing() throws Exception {

		currentState = TestCaseState.PRE_PROCESSING;
		logger.tellLogger(BasicLogger.MSG_TC_STATE_CHANGED, currentState.name());

		logger.tellLogger(BasicLogger.MSG_TC_PREPROCESSING_EXECUTION_BEGIN, getTestCaseId());
		try {
			preProcessing();
		} catch (TestCaseCanceledException e) {
			logger.log(BasicLogger.INFO, e.toString());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logFatalException(e);
			throw e;
		} finally {
			setState(TestCaseState.PRE_PROCESSED);
			logger.tellLogger(BasicLogger.MSG_TC_PREPROCESSING_EXECUTION_END, getTestCaseId());
		}
	}

	/**
	 * Performs actions necessary after the pre-processing step and starts the execution step.
	 *
	 * @throws Exception exception on test case execution.
	 */
	public final void testExecution() throws Exception {
		currentState = TestCaseState.EXECUTE_USECASE;
		logger.tellLogger(BasicLogger.MSG_TC_STATE_CHANGED, currentState.name());

		logger.tellLogger(BasicLogger.MSG_TC_USECASE_EXECUTION_BEGIN, getTestCaseId());
		try {
			try {
				executeUsecase();
			} finally {
				setState(TestCaseState.EXECUTED_USECASE);
				logger.tellLogger(BasicLogger.MSG_TC_USECASE_EXECUTION_END, getTestCaseId());
			}
		} catch (TestCaseCanceledException e) {
			logger.log(BasicLogger.INFO, e.toString());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logFatalException(e);
			throw e;
		}
	}


	/**
	 * Performs actions necessary after the execution step and starts the post-processing.
	 *
	 * @throws Exception exception on test case post-processing.
	 */
	public final void testPostprocessing() throws Exception {
		currentState = TestCaseState.POST_PROCESSING;
		logger.tellLogger(BasicLogger.MSG_TC_STATE_CHANGED, currentState.name());

		logger.tellLogger(BasicLogger.MSG_TC_POSTPROCESSING_EXECUTION_BEGIN, getTestCaseId());
		try {
			try {
				postProcessing();
			} finally {
				logger.tellLogger(BasicLogger.MSG_TC_POSTPROCESSING_EXECUTION_END, getTestCaseId());
			}
		} catch (TestCaseCanceledException e) {
			logger.log(BasicLogger.INFO, e.toString());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logFatalException(e);
			throw e;
		}
		setState(TestCaseState.POST_PROCESSED);
		logger.tellLogger(BasicLogger.MSG_TC_STATE_CHANGED, currentState.name());
	}


	/**
	 * Performs actions necessary after a stopped or finished test run.
	 *
	 * @throws Exception exception on test case post-processing.
	 */
	public void testTearDown() throws Exception {
		logger.tellLogger(BasicLogger.MSG_TC_STOPPED_BEGIN, getTestCaseId());
		try {
			try {
				cleanAndExit();
			} finally {
				logger.tellLogger(BasicLogger.MSG_TC_STOPPED_END, getTestCaseId());
			}
		} catch (TestCaseCanceledException e) {
			logger.log(BasicLogger.INFO, e.toString());
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			logFatalException(e);
			throw e;
		}
		setState(TestCaseState.STOPPED);
		logger.tellLogger(BasicLogger.MSG_TC_STATE_CHANGED, currentState.name());
	}

	/**
	 * Sets the test case's state to be the specified state.
	 * @param newState the new status to set.
	 * @see {@link TestCaseState}.
	 */
	public final void setState(final TestCaseState newState) {
		if (currentState.ordinal() <= newState.ordinal()) {
			currentState = newState;

		}

	}

	@Override
	public final void step(final String idPrefix, final Integer id, final String description, final String result)
			throws TestCaseCanceledException {
		String stepNr = idPrefix == null ? id.toString() : idPrefix + "." + id;

		StringBuilder message = new StringBuilder();
		message.append("Step " + stepNr + ": " + description);
		if (null != result && !result.isBlank()) {
			message.append(" - Expected Result: " + result);
		}

		logger.log(BasicLogger.STEP, message.toString());
	}

	/**
	 * Logs the step information.
	 *
	 * @param id of the step
	 * @param description of the step
	 * @param result which will be expected
	 * @throws TestCaseCanceledException exception if the test case was canceled by user
	 */
	public final void step(final Integer id, final String description, final String result)
			throws TestCaseCanceledException {
		step(null, id, description, result);
	}

}
