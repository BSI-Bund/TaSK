// Copyright (C) 2019 achelos GmbH
// All rights reserved.
package com.achelos.task.abstracttestsuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * Test case execution controller.
 */
public final class ExecutionControl {

	private static ExecutionControl instance;

	/**
	 * @return the ExecutionControl singleton instance.
	 */
	public static ExecutionControl getInstance() {
		if (instance == null) {
			instance = new ExecutionControl();
		}
		return instance;
	}

	/**
	 * Private constructor for the singleton class.
	 */
	private ExecutionControl() {}

	/**
	 * Create new {@link TestCaseRun} instance with a state {@link RunState#WAITING} for the given test case and test
	 * session parameters.
	 *
	 * @param testCase The test case to create a new test case run.
	 * @return created {@link TestCaseRun} object.
	 */
	private TestCaseRun createTestCaseRun(final AbstractTestCase testCase) {
		final TestCaseRun testCaseRun = new TestCaseRun(testCase.getTestCaseId(),
				RunState.WAITING, testCase.getTesterInCharge());
		return testCaseRun;
	}

	/**
	 * Queue the given test cases for execution.
	 *
	 * @param tcs the test cases to execute
	 * @return list of test case runs if the test case execution is successfully queued, null otherwise.
	 */
	public List<TestCaseRun> addAll(final Collection<AbstractTestCase> tcs) {
		if (tcs == null || tcs.isEmpty()) {
			return null;
		}
		final LinkedHashMap<TestCaseRun, AbstractTestCase> run2tc = new LinkedHashMap<>(tcs.size());
		final List<Runnable> executorJobs = new ArrayList<>(tcs.size());

		for (AbstractTestCase tc : tcs) {
			TestCaseRun tcr = createTestCaseRun(tc);
			run2tc.put(tcr, tc);
		}


		try {
			// 3. Create execution job (and put it into the job group) for every test case run
			run2tc.forEach((run, tc) -> executorJobs.add(createExecutorJob(tc, run)));
			// 4. Start all the jobs
			executorJobs.forEach(Runnable::run);
			return new ArrayList<>(run2tc.keySet());
		} catch (Exception e) {
			System.err.println("Couldn't queue test case executions" + e);
		}
		return null;
	}

	/**
	 * @param tc the test case to execute
	 * @return test case run if the test case execution is successfully queued, null otherwise.
	 */
	public TestCaseRun add(final AbstractTestCase tc) {
		final TestCaseRun testCaseRun = createTestCaseRun(tc);
		try {
			final Runnable job = createExecutorJob(tc, testCaseRun);
			job.run();
			return testCaseRun;
		} catch (Exception e) {
			System.err.println("Couldn't queue execution for: " + testCaseRun + e);
		}
		return null;
	}

	/**
	 * Create a job for executing a test case.
	 *
	 * @param testCase The test case that will be executed
	 * @param testCaseRun The test case run to store the execution results into
	 * @return Job that will run the test case executor
	 */
	private Runnable createExecutorJob(final AbstractTestCase testCase, final TestCaseRun testCaseRun) {
		final Consumer<TestCaseRun> testCaseRunSaver = tcr -> {
			try {
				if (RunState.isEnded(testCaseRun.getState())) {

					var logger = LoggingConnector.getInstance();
					logger.info("===============================================");
					logger.info("Test case name: " + testCaseRun.getTestCaseName());
					if (testCaseRun.getErrorCount() + testCaseRun.getFatalErrorCount() > 0) {
						logger.info("Test case Result: FAILED");
					} else if (testCaseRun.getWarningCount() > 0) {
						logger.info("Test case Result: WARNING");
					} else {
						logger.info("Test case Result: PASSED");
					}

					logger.info("Started at: " + testCaseRun.getStartTimeFormatted());
					logger.info("Duration: " + testCaseRun.getDurationFormatted());
					logger.info("Fatal errors: " + testCaseRun.getFatalErrorCount());
					logger.info("Errors: " + testCaseRun.getErrorCount());
					logger.info("Warnings: " + testCaseRun.getWarningCount());
					logger.info("===============================================");

					logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testCaseRun);
				}
			} catch (Exception e) {
				System.err.println("Cannot print test case run summary" + e);
			}
		};

		final Runnable executorJob
				= new ExecutorJob("Execution of test case " + testCase.getTestCaseId(), testCase, testCaseRun,
						testCaseRunSaver);

		return executorJob;
	}

	/**
	 * Special type of job that is used to execute a test case run.
	 */
	private static class ExecutorJob implements Runnable {
		private final TestCaseExecutor executor;

		/**
		 * Create a job that will store a {@link TestCaseExecutor} instance.
		 *
		 * @param name The job's name
		 * @param testCase The test case that will be executed
		 * @param testCaseRun The test case run to store the execution results into
		 * @param testCaseRunSaver Operation to persist changes to the given test case run
		 * @see TestCaseExecutor#TestCaseExecutor(AbstractTestCase, TestCaseRun, Consumer)
		 */
		ExecutorJob(final String name, final AbstractTestCase testCase, final TestCaseRun testCaseRun,
				final Consumer<TestCaseRun> testCaseRunSaver) {
			executor = new TestCaseExecutor(testCase, testCaseRun, testCaseRunSaver);
		}

		@Override
		public void run() {
			executor.run();

		}
	}
}
