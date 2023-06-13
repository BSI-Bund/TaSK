package com.achelos.task.commons.helper;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Helper class for the handling of Exceptions.
 */
public final class ExceptionHelper {

	/**
	 * Hidden constructor. Static class.
	 */
	private ExceptionHelper() {
		// Empty.
	}

	/**
	 * Gets the stack trace as a String for provided Exception e.
	 *
	 * @param e Exception to get the stack trace from.
	 * @return stack trace as a String for provided Exception e.
	 */
	public static String getStacktraceAsString(final Exception e) {
		var stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
}
