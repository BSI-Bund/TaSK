package com.achelos.task.abstracttestsuite;

/**
 * This enumeration contains all states, a test case can be in. The order is mandatory. <br>
 * The States are:
 * <li>IDLE
 * <li>PRE_PROCESSING
 * <li>PRE_PROCESSED
 * <li>EXECUTE_USECASE
 * <li>EXECUTED_USECASE
 * <li>POST_PROCESSING
 * <li>POST_PROCESSED
 * <li>STOP
 * <li>STOPPED
 */
public enum TestCaseState {
	IDLE,
	PRE_PROCESSING,
	PRE_PROCESSED,
	EXECUTE_USECASE,
	EXECUTED_USECASE,
	POST_PROCESSING,
	POST_PROCESSED,
	STOP,
	STOPPED
}
