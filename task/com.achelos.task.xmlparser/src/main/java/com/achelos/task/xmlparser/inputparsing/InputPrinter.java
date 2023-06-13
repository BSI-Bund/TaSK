package com.achelos.task.xmlparser.inputparsing;

import com.achelos.task.xmlparser.datastructures.mics.MICS;

/**
 * Helper class used for the generation of string representations of Input parameters.
 */
public class InputPrinter {

	// Hidden Constructor
	private InputPrinter() {
		// Empty.
	}

	/**
	 * Generate a String representation of the provided MICS.
	 * @param mics the MICS to generate the String representation for.
	 * @return a String representation of the provided MICS.
	 */
	public static String printMICS(final MICS mics) {
		return mics.toString();
	}

}
