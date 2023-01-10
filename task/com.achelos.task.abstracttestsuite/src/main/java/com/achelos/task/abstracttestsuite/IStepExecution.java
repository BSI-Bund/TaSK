package com.achelos.task.abstracttestsuite;

/**
 * Base for test cases and test fragments.
 */
public interface IStepExecution {


	/**
	 * Base step method.
	 *
	 * @param idPrefix prefix of the step.
	 * @param id of the step
	 * @param description of the step
	 * @param result which will be expected
	 * @throws TestCaseCanceledException exception if the test case was canceled by user
	 */
	void step(String idPrefix, Integer id, String description, String result)
			throws TestCaseCanceledException;

}
