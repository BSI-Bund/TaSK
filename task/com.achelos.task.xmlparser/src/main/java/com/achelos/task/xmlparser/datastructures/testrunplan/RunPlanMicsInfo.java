package com.achelos.task.xmlparser.datastructures.testrunplan;

/**
 * Internal data structure containing MICS Information stored in the Test Run Plan file.
 */
public class RunPlanMicsInfo {
	private final String micsName;
	private final String micsDescription;
	private final String pathToMicsFile;

	/**
	 * Constructor using all parameters.
	 *
	 * @param micsName parameter
	 * @param micsDescription parameter
	 * @param pathToMicsFile parameter
	 */
	public RunPlanMicsInfo(final String micsName, final String micsDescription, final String pathToMicsFile) {
		this.micsName = micsName;
		this.micsDescription = micsDescription;
		this.pathToMicsFile = pathToMicsFile;
	}

	/**
	 * The Name of the MICS file.
	 *
	 * @return the micsName
	 */
	public String getMicsName() {
		return micsName;
	}

	/**
	 * The Description of the MICS file.
	 *
	 * @return the micsDescription
	 */
	public String getMicsDescription() {
		return micsDescription;
	}

	/**
	 * The Path to the MICS file.
	 *
	 * @return the pathToMicsFile
	 */
	public String getPathToMicsFile() {
		return pathToMicsFile;
	}
}
