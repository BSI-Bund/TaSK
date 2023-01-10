/**
 * Copyright © 2009 achelos GmbH All rights reserved.
 */
package com.achelos.task.logging;

/**
 * This class is the BasicLogger. All loggers, reporters and stuff like that have to be derived from this class.
 */
public abstract class BasicLogger {

	/**
	 * DEBUG: The standard "low-level" debug level.
	 */
	public static final long DEBUG = 1L << 0;

	/**
	 * INFO: A message, interesting for the user, but not really necessary.
	 */
	public static final long INFO = 1L << 1;

	/**
	 * ERROR: A "normal" (non-fatal) error.
	 */
	public static final long ERROR = 1L << 2;

	/**
	 * FATAL_ERROR: A fatal error which usually ends up in some sort of crash.
	 */
	public static final long FATAL_ERROR = 1L << 3;

	/**
	 * WARNING: A Warning.
	 */
	public static final long WARNING = 1L << 6;

	/**
	 * LOW_LEVEL_STEP: Responses on the lowest level.
	 */
	public static final long STEP = 1L << 17;

	/**
	 * The "Tell Logger"-Code, used to indicate, that a new test case has been started.
	 */
	public static final String MSG_NEW_TESTCASE = "NEW_TESTCASE";

	/**
	 * The "Tell Logger"-Code, used to indicate, that a test cases state has changed.
	 */
	public static final String MSG_TC_STATE_CHANGED = "TC_STATE_CHANGED";

	/**
	 * The "Tell Logger"-Code, used to indicate, that a test case has ended.
	 */
	public static final String MSG_TESTCASE_ENDED = "TC_ENDED";

	/**
	 * The "Tell Logger"-Code, used to indicate, that a new test suite has been started.
	 */
	public static final String MSG_NEW_TESTSUITE = "NEW_TESTSUITE";

	/**
	 * The "Tell Logger"-Code, used to indicate, that a test suite has ended.
	 */
	public static final String MSG_TESTSUITE_ENDED = "TESTSUITE_ENDED";

	/**
	 * The "Tell Logger"-Code, used to add the TestCase Description to the logger.
	 */
	public static final String MSG_TESTCASE_DESCRIPTION = "TESTCASE_DESCRIPTION";
	/**
	 * The "Tell Logger"-Code, used to add the TestCase Purpose to the logger.
	 */
	public static final String MSG_TESTCASE_PURPOSE = "TESTCASE_PURPOSE";

	/**
	 * The "Tell Logger"-Code, used to add the TestRunPlan to the logger.
	 */
	public static final String MSG_TESTRUNPLAN = "TEST_RUNPLAN";

	/**
	 * The "Tell Logger"-Code, used to add the MICS to the logger.
	 */
	public static final String MSG_MICS = "MICS";

	/**
	 * The "Tell Logger"-Code, used to add the Metadata to the logger.
	 */
	public static final String MSG_METADATA = "LOG_METADATA";

	/**
	 * The following messages are used to indicate the beginning and end of certain text blocks used in templates for
	 * log creation.
	 */

	/** Used for template. */
	public static final String MSG_TC_PREPARETERMINAL_END = "MSG_TC_PREPARETERMINAL_END";

	/** Used for template. */
	public static final String MSG_TC_PREPARETERMINAL_BEGIN = "MSG_TC_PREPARETERMINAL_BEGIN";

	/** Used for template. */
	public static final String MSG_TC_PREPROCESSING_EXECUTION_BEGIN = "MSG_TC_PREPROCESSING_EXECUTION_BEGIN";

	/** Used for template. */
	public static final String MSG_TC_PREPROCESSING_EXECUTION_END = "MSG_TC_PREPROCESSING_EXECUTION_END";

	/** Used for template. */
	public static final String MSG_TC_USECASE_EXECUTION_BEGIN = "MSG_TC_USECASE_EXECUTION_BEGIN";

	/** Used for template. */
	public static final String MSG_TC_USECASE_EXECUTION_END = "MSG_TC_USECASE_EXECUTION_END";

	/** Used for template. */
	public static final String MSG_TC_POSTPROCESSING_EXECUTION_BEGIN = "MSG_TC_POSTPROCESSING_EXECUTION_BEGIN";

	/** Used for template. */
	public static final String MSG_TC_POSTPROCESSING_EXECUTION_END = "MSG_TC_POSTPROCESSING_EXECUTION_END";

	/** Used for template. */
	public static final String MSG_TC_STOPPED_BEGIN = "MSG_TC_STOPPED_BEGIN";

	/** Used for template. */
	public static final String MSG_TC_STOPPED_END = "MSG_TC_STOPPED_END";

	/**
	 * Selected log verbosity in TestConfig.xml.
	 */
	private String logVerbosity;

	/**
	 * Log a message.
	 *
	 * @param timestamp time stamp
	 * @param lvl the log level
	 * @param log the message
	 * @param t an exception or error (which may be null, as well)
	 */
	public abstract void log(long timestamp, long lvl, String log, Throwable t);

	/**
	 * This is method is used to control the attached loggers. Information transmitted with this method usually are not
	 * shown in a view but are used to control it.<br>
	 * If, for example, the viewer should be cleared for every test case, the tellLogger has to be overwritten in order
	 * to "listen" to a MSG_NEW_TESTCASE and clear the content if he sees that message.<br>
	 * It is also intended to use this method for very specific communication of a few test cases with a special viewer.
	 * In such a case, the developer of a test case has only to make an arrangement with the developer of the
	 * specialized viewer without any need of changing the rest of the test system.
	 *
	 * @param topic the topic of the message
	 * @param value a generic value which may be used for that topic (may be null as well)
	 */
	public abstract void tellLogger(String topic, Object value);

	/**
	 * Returns the name of the log level.<br>
	 * <i>Beware, because of the hard coding of the log level, this method has to be altered if the log levels
	 * change.</i>
	 *
	 * @param lvl log level
	 * @return the log levels name
	 */
	public static final String getName(final long lvl) {
		if (lvl == DEBUG) {
			return "DEBUG";
		}
		if (lvl == INFO) {
			return "INFO";
		}
		if (lvl == ERROR) {
			return "ERROR";
		}
		if (lvl == FATAL_ERROR) {
			return "FATAL_ERROR";
		}
		if (lvl == WARNING) {
			return "WARNING";
		}
		if (lvl == STEP) {
			return "STEP";
		}
		return null;
	}

	/**
	 * Returns the name of the log verbosity.<br>
	 *
	 * @return the log verbosity as a string
	 */
	public String getLogVerbosity() {
		return logVerbosity;
	}

	/**
	 * @param logVerbosity the logVerbosity to set
	 */
	public void setLogVerbosity(final String logVerbosity) {
		this.logVerbosity = logVerbosity;
	}

}
