package com.achelos.task.commandlineexecution.genericcommandlineexecution;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * A generic class to log info, debug and error messages. Uses {@link Executor#getName()} to display executor
 * information as log prefix in the log messages.
 */
public class Logging {
	private final BasicLogger logger;
	private final String logPrefix; // Used in log entries
	private final Executor executor;

	/**
	 * Default constructor.
	 * @param logger The logger to use to log messages.
	 * @param executor Currently running executor.
	 */
	public Logging(final BasicLogger logger, final Executor executor) {
		this.logger = LoggingConnector.getInstance();
		this.executor = executor;
		logPrefix = executor.getName() + ": ";
	}

	/**
	 * Returns the information the executor that is currently running this process. e.g. OCSP, CRL, TLS Test Tool etc.
	 *
	 * @return {@link Executor}.
	 */
	protected final Executor getExecutor() {
		return executor;
	}

	/**
	 * @return the logger
	 */
	protected BasicLogger getLogger() {
		return logger;
	}

	/**
	 * Writes an error log message.
	 *
	 * @param message Message that will be logged.
	 */
	protected void logError(final String message) {
		logError(message, null);
	}


	/**
	 * Writes an error log message with exception.
	 *
	 * @param message Message that will be logged.
	 * @param e an exception (which may be null, as well).
	 */
	protected void logError(final String message, final Throwable e) {
		getLogger().log(System.currentTimeMillis(), BasicLogger.ERROR, logPrefix + message, e);
	}


	/**
	 * This is method is used to control the attached loggers.
	 *
	 * @param topic the topic of the message
	 * @param value a generic value which may be used for that topic (maybe null as well)
	 */
	protected void tellLogger(final String topic, final Object value) {
		getLogger().tellLogger(topic, value);

	}

	/**
	 * Write a log message.
	 *
	 * @param logLevel The log level.
	 * @param message Message that will be logged.
	 * @param throwable Exception that will be logged.
	 */
	protected void log(final long logLevel, final String message, final Throwable throwable) {
		getLogger().log(System.currentTimeMillis(), logLevel, logPrefix + message, throwable);

	}


	/**
	 * Write a log message.
	 *
	 * @param logLevel The log level.
	 * @param message Message that will be logged.
	 */
	protected void log(final long logLevel, final String message) {
		log(logLevel, message, null);
	}


	/**
	 * Write a info log message.
	 *
	 * @param message Message that will be logged.
	 */
	protected void logInfo(final String message) {
		getLogger().log(System.currentTimeMillis(), BasicLogger.INFO, logPrefix + message, null);
	}


	/**
	 * Write a debug log message.
	 *
	 * @param message Message that will be logged.
	 */
	protected void logDebug(final String message) {
		getLogger().log(System.currentTimeMillis(), BasicLogger.DEBUG, logPrefix + message, null);
	}

}
