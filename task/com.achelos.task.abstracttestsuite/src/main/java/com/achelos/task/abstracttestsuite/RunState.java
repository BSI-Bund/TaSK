package com.achelos.task.abstracttestsuite;


/**
 * Enumeration for tracking all possible states during the execution of a test case. The state is stored in a test case
 * run. The initial state is {@link #WAITING}.
 */
public enum RunState {
	/**
	 * Initial state of a test case run. The execution of a test case has been started and the test case run that has
	 * been created is {@code WAITING} for its execution to begin.
	 * <li>If the execution begins, the state will change to {@link #RUNNING}.
	 * <li>If the execution is canceled, the test case run will be deleted.
	 */
	WAITING,
	/**
	 * Temporary state of a test case run. The test case run is currently being executed.
	 * <li>If the execution is canceled, either by stopping the execution or by an exception thrown by the test case,
	 * the state will change to {@link #CANCELED}.
	 * <li>If the execution completes, the state will change to {@link #FINISHED}.
	 */
	RUNNING,
	/**
	 * Final state of a test case run. The test case run has been executed without premature termination and reached the
	 * end of the test case.
	 */
	FINISHED,
	/**
	 * Final state of a test case run. The test case run has been executed and terminated prematurely without reaching
	 * the end of the test case.
	 */
	CANCELED;

	/**
	 * Check if the test case run is in a final state, meaning that the state is {@link #FINISHED} or {@link #CANCELED}.
	 *
	 * @param state The state to check.
	 * @return {@code true} if and only if the given state is a final state.
	 */
	public static boolean isEnded(final RunState state) {
		return FINISHED == state || CANCELED == state;
	}
}
