package com.achelos.task.abstracttestsuite;

import java.util.List;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * This class is intended to build the foundation of every test fragment. It contains the execution control and provides
 * abstract methods, which must be implemented in subclasses.
 */
public abstract class AbstractTestFragment implements IStepExecution {
	/**
	 * The parent step executable {@link AbstractTestCase} or {@link AbstractTestFragment} implementation).
	 */
	protected final IStepExecution parentStepExec;
	/**
	 * Name of the test fragment.
	 */
	protected String testFragmentName = "";
	/**
	 * Access to logger.
	 */
	protected LoggingConnector logger;

	/**
	 * Constructor with test fragment parent caller and test fragment name parameters. This constructor should be called
	 * from all implementations from the {@link AbstractTestFragment} class.
	 *
	 * @param stepExec the {@link #parentStepExec}
	 * @param testFragmentName the test fragment name.
	 */
	protected AbstractTestFragment(final IStepExecution stepExec, final String testFragmentName) {
		parentStepExec = stepExec;
		this.testFragmentName = testFragmentName;
		logger = LoggingConnector.getInstance();
	}

	/**
	 * The code of the actual use case should be placed in this method. The supper call is necessary (generates the
	 * first/start step log)!
	 *
	 * @param prefix the parent step number to use as id/number prefix
	 * @param result the expected result string
	 * @param strParams list of input parameters in following string format: paramName=paramValue - the list can be null
	 * @param params object-values of the related test fragment input parameters - can be null
	 * @return caller relevant information
	 * @throws Exception if something goes wrong
	 */
	protected Object executeSteps(final String prefix,
			final String result, final List<String> strParams, final Object... params) throws Exception {
		String inputParams = strParams == null ? "" : strParams.toString().replaceFirst("\\[(.*)\\]", "$1");

		StringBuilder message = new StringBuilder();
		message.append("Step " + prefix + ": " + testFragmentName);
		if (null != result && !result.isBlank()) {
			message.append(" - Expected Result: " + result);
		}
		if (null != inputParams && !inputParams.isBlank()) {
			message.append(" - Input Parameter(s): " + inputParams);
		}

		logger.log(BasicLogger.STEP, message.toString());
		return null;
	}

	/**
	 * @param idPrefix the parent step number to use as id/number prefix
	 * @param id of the step
	 * @param description of the step
	 * @param result the expected result string
	 * @throws TestCaseCanceledException exception if the test case was canceled by user
	 */
	@Override
	public final void step(final String idPrefix, final Integer id, final String description, final String result)
			throws TestCaseCanceledException {
		parentStepExec.step(idPrefix, id, description, result);
	}

}
