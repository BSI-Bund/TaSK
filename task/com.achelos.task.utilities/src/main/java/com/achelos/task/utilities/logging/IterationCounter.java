package com.achelos.task.utilities.logging;

/**
 * Representation of multiple executions of Executors during on test case.
 */
public class IterationCounter {
	private final int currentIteration;
	private final int totalNumberOfIterations;

	/**
	 * Default constructor.
	 *
	 * @param currentIteration Number of the current iteration
	 * @param totalNumberOfIterations Overall number of iterations
	 */
	public IterationCounter(final int currentIteration, final int totalNumberOfIterations) {
		this.currentIteration = currentIteration;
		this.totalNumberOfIterations = totalNumberOfIterations;
	}


	/**
	 * Gets the number of the current iteration.
	 *
	 * @return The current iteration.
	 */
	public final int getCurrentIteration() {
		return currentIteration;
	}


	/**
	 * Gets the total number of iterations.
	 *
	 * @return The total number of iterations
	 */
	public final int getTotalNumberOfIterations() {
		return totalNumberOfIterations;
	}


	@Override
	public final String toString() {
		if (0 < totalNumberOfIterations) {
			return "Iteration " + currentIteration + " of " + totalNumberOfIterations + " log output:";
		}
		return "Iteration " + currentIteration + " log output:";
	}


	/**
	 * Returns current iteration number out of total number of iterations as formatted string. Example output:
	 * -iteration-0004-of-00010
	 *
	 * @return file name suffix.
	 */
	public final String toFileNameSuffix() {
		if (0 < totalNumberOfIterations) {
			return String.format("-iteration-%04d-of-%04d", currentIteration, totalNumberOfIterations);
		}
		return String.format("-iteration-%04d", currentIteration);
	}

}
