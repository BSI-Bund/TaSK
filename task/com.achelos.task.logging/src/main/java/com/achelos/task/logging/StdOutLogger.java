package com.achelos.task.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.achelos.task.utilities.DateTimeUtils;


/**
 * This logger prints messages to standard out.
 */
class StdOutLogger extends BasicLogger {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			DateTimeUtils.ISO_8601_DATE_TIME_PATTERN_MILLISECONDS);

	@Override
	public final void log(final long timestamp, final long lvl, final String log, final Throwable t) {

		String newLog = log.replaceAll("\\r?\\n", " ");

		String verbosity = LoggingConnector.getInstance().getLogVerbosity();

		if ("INFO".equals(verbosity) && lvl != DEBUG || "VERBOSE".equals(verbosity)) {

			System.out.println(dateFormat.format(new Date(timestamp)) + " " + BasicLogger.getName(lvl) + " MSG: "
					+ newLog + getExceptionAsString(t));

		} else if (LoggingConnector.getInstance().getLogVerbosity().equals("DEBUG") || "VERBOSE".equals(verbosity)) {
			System.out.println(dateFormat.format(new Date(timestamp)) + " " + BasicLogger.getName(lvl) + " MSG: "
					+ newLog + (null == t ? "" : " EXC:" + getStacktraceAsString(t)));
		}
	}

	@Override
	public void tellLogger(final String topic, final Object value) {
		String verbosity = LoggingConnector.getInstance().getLogVerbosity();
		if ("VERBOSE".equals(verbosity)) {
			System.out.println(topic + " : " + value);
		}
	}

	/**
	 * Gets the stack trace as String for provided Exception e.
	 *
	 * @param e Exception to get the stack trace from.
	 * @return stack trace as String for provided Exception e.
	 */
	private static String getStacktraceAsString(final Throwable e) {
		var stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	/**
	 * Gets the message as String for provided Exception e.
	 *
	 * @param e Exception to get the stack trace from.
	 * @return Message as String for provided Exception e.
	 */
	private static String getExceptionAsString(final Throwable e) {
		if (e == null) {
			return "";
		}
		if (e.getMessage() != null) {
			return " EXC: " + e.getMessage();
		}
		if (e.getCause() != null) {
			return getExceptionAsString(e.getCause());
		}
		return " Stacktrace: " + getStacktraceAsString(e);
	}
}
