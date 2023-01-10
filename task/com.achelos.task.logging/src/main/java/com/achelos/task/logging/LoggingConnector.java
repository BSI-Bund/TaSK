/**
 * Copyright © 2009 achelos GmbH All rights reserved.
 */
package com.achelos.task.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This class is intended to be a distributor. Various loggers can connect themselves to this class and it will send all
 * incoming messages/events to them. <br>
 * It also allows the sender to get some information about the things happened/sent through this distributor.
 */
public final class LoggingConnector extends BasicLogger {

	private final ArrayList<LogProcessor> loggers;
	private AbstractLogEntryChecker logEntryChecker;

	private static LoggingConnector instance;

	/**
	 * Constructor hider.
	 */
	private LoggingConnector() {
		loggers = new ArrayList<>();
	}


	/**
	 * Register new logger.<br />
	 *
	 * @param newLogger the new logger
	 */
	private void internalAddLogger(final List<BasicLogger> newLogger) {
		for (BasicLogger logger : newLogger) {
			LogProcessor lp = new LogProcessor(logger);
			lp.start();
			loggers.add(lp);
		}
	}

	/**
	 * Internal remove Logger command.
	 * @param loggerToRemove the logger to remove.
	 */
	private synchronized void internalRemoveLogger(BasicLogger loggerToRemove) {
		for (var logger : loggers) {
			if (logger.nestedLogger.equals(loggerToRemove)) {
				logger.stopJob();
				loggers.remove(logger);
				return;
			}
		}
	}

	/**
	 * Register new logger.<br />
	 *
	 * @param newLogger the new logger
	 */
	public static void addLogger(final List<BasicLogger> newLogger) {
		if (instance != null) {
			instance.internalAddLogger(newLogger);
		}
	}

	/**
	 * Remove a logger from this loggingconnector instance.
	 * @param loggerToRemove the logger to remove.
	 */
	public synchronized static void removeLogger(final BasicLogger loggerToRemove) {
		if (instance != null) {
			instance.internalRemoveLogger(loggerToRemove);
		}
	}



	/**
	 * Stop registered logger (threads).<br />
	 */
	private void internalStop() {
		for (LogProcessor lp : loggers) {
			lp.stopJob();
		}
	}

	/**
	 * Stop registered logger (threads).<br />
	 */
	public static void stop() {
		if (instance != null) {
			instance.internalStop();
		}
	}

	/**
	 * Distribute the debug messages.
	 *
	 * @param log the log message
	 * @see BasicLogger
	 */
	public void debug(final String log) {
		log(BasicLogger.DEBUG, log, null);
	}


	/**
	 * Distribute the error messages.
	 *
	 * @param log the log message
	 * @see BasicLogger
	 */
	public void error(final String log) {
		error(log, null);
	}


	/**
	 * Distribute the error messages, log level is {@link BasicLogger#ERROR}.
	 *
	 * @param log the log message
	 * @param t the related exception/error instance or null
	 * @see BasicLogger
	 */
	public void error(final String log, final Throwable t) {
		log(BasicLogger.ERROR, log, t);
	}


	/**
	 * Distribute the fatal error messages.
	 *
	 * @param log the log message
	 * @see BasicLogger
	 */
	public void fatal(final String log) {
		log(BasicLogger.FATAL_ERROR, log);
	}


	/**
	 * Distribute the info messages.
	 *
	 * @param log the log message
	 * @see BasicLogger
	 */
	public void info(final String log) {
		log(BasicLogger.INFO, log);
	}


	/**
	 * Distribute the warning messages.
	 *
	 * @param log the log message
	 * @see BasicLogger
	 */
	public void warning(final String log) {
		log(BasicLogger.WARNING, log);
	}


	/**
	 * Distribute the log messages.
	 *
	 * @param lvl the log level
	 * @param log the log message
	 * @see BasicLogger
	 */
	public void log(final long lvl, final String log) {
		log(lvl, log, null);
	}


	/**
	 * Distribute the log messages.
	 *
	 * @param lvl the log level
	 * @param log the log message
	 * @param t the related exception/error instance or null
	 * @see BasicLogger
	 */
	public void log(final long lvl, final String log, final Throwable t) {
		log(System.currentTimeMillis(), lvl, log, t);
	}


	@Override
	public void log(final long timestamp, final long lvl, final String log, final Throwable t) {
		LogEntry entry = new LogEntry(timestamp, lvl, log, t);
		if (null != logEntryChecker) {
			entry = logEntryChecker.updateErrorsAndWarnings(entry);
		}

		for (LogProcessor lp : loggers) {
			lp.add(entry);
		}
	}


	/**
	 * Distribute the "tell logger" messages.
	 *
	 * @see BasicLogger#tellLogger(String, Object)
	 */
	@Override
	public void tellLogger(final String topic, final Object value) {
		TellLoggerEntry entry = new TellLoggerEntry(topic, value);
		if (null != logEntryChecker) {
			entry = logEntryChecker.resetLogEntryChecker(entry);
		}

		for (LogProcessor lp : loggers) {
			lp.add(entry);
		}
	}


	/**
	 * Gets the instance of logging connector, Creates new if null.
	 *
	 * @return the {@link LoggingConnector} instance.
	 */
	public static synchronized LoggingConnector getInstance() {
		if (null == instance) {
			final LoggingConnector singleton = new LoggingConnector();
			final ArrayList<BasicLogger> loggers = new ArrayList<>();
			loggers.add(new StdOutLogger());
			singleton.internalAddLogger(loggers);
			// Set static field after it is fully initialized
			instance = singleton;
		}
		return instance;
	}

	public static LoggingConnector getInstance(final String logVerbosity) {
		if (null == instance) {
			getInstance();
			instance.setLogVerbosity(logVerbosity);
		}
		return instance;
	}

	/**
	 * This function is desired to control test case execution logs!
	 *
	 * @param logEntryChecker the logging checker to register
	 * @throws IllegalStateException if a logging checker is already registered
	 */
	private void setLogEntryChecker(final AbstractLogEntryChecker logEntryChecker) {
		if (null != this.logEntryChecker) {
			throw new IllegalStateException("A log entry checker instance is already set!");
		}
		this.logEntryChecker = logEntryChecker;
	}

	/**
	 * This function is desired to control test case execution logs!
	 *
	 * @param logEntryChecker the logging checker to register
	 * @throws IllegalStateException if a logging checker is already registered
	 */
	public static void setInstanceLogEntryChecker(final AbstractLogEntryChecker logEntryChecker) {
		if (instance != null) {
			instance.setLogEntryChecker(logEntryChecker);
		}
	}


	/**
	 * Reset the current log check - set it to null, but only if the given checker is really the current checker.
	 *
	 * @param lEChecker the logging checker to remove
	 * @throws IllegalStateException if the given logging checker is not the registered one
	 */
	private void resetLogEntryChecker(final AbstractLogEntryChecker lEChecker) {
		if (lEChecker != logEntryChecker) {
			throw new IllegalStateException(
					"The logging checker is not the current checker and cannot be deregistered!");
		}
		logEntryChecker = null;
	}

	/**
	 * Reset the current log check - set it to null, but only if the given checker is really the current checker.
	 *
	 * @param lEChecker the logging checker to remove
	 * @throws IllegalStateException if the given logging checker is not the registered one
	 */
	public static void resetInstanceLogEntryChecker(final AbstractLogEntryChecker lEChecker) {
		if (instance != null) {
			instance.resetLogEntryChecker(lEChecker);
		}
	}

	/**
	 * Base class for {@link LogEntry} and {@link TellLoggerEntry} logging entries.
	 */
	public abstract static class AbstractLoggingEntry {
		private final String msg;

		AbstractLoggingEntry(final String msg) {
			this.msg = msg;
		}

		/**
		 * @return the log message
		 */
		public String getMsg() {
			return msg;
		}
	}

	/**
	 * Logging entry generated on {@link LoggingConnector#log(long, long, String, Throwable)} invocation.
	 */
	public static class LogEntry extends AbstractLoggingEntry {
		private final long timestamp;
		private final long logLevel;
		private final Throwable cause;

		public LogEntry(final long timestamp, final long logLevel, final String msg, final Throwable e) {
			super(msg);
			this.timestamp = timestamp;
			this.logLevel = logLevel;
			cause = e;
		}


		/**
		 * @return log time in milliseconds
		 */
		public long getTimestamp() {
			return timestamp;
		}

		/**
		 * @return the log level
		 */
		public long getLogLevel() {
			return logLevel;
		}

		/**
		 * @return the log cause exception/error
		 */
		private Throwable getCause() {
			return cause;
		}
	}

	/**
	 * Logging entry generated on {@link LoggingConnector#tellLogger(String, Object)} invocation.
	 */
	public static class TellLoggerEntry extends AbstractLoggingEntry {
		private final Object value;

		public TellLoggerEntry(final String msg, final Object value) {
			super(msg);
			this.value = value;
		}

		/**
		 * @return entry value
		 */
		public Object getValue() {
			return value;
		}
	}

	/**
	 * The Checker can be registered as logging intercepter to manipulate, prevent logging entries.
	 */
	public abstract static class AbstractLogEntryChecker {
		/**
		 * @param entry "log" entry to check.
		 * @return - the given entry by default<br>
		 */
		public LogEntry updateErrorsAndWarnings(final LogEntry entry) {
			return entry;
		}


		/**
		 * @param entry "tellLogger" entry to check
		 * @return - the given entry by default<br>
		 */
		public TellLoggerEntry resetLogEntryChecker(final TellLoggerEntry entry) {
			return entry;
		}
	}

	/**
	 * {@link BasicLogger} wrapper which is responsible for synchronous or asynchronous processing of the log entries.
	 */
	private class LogProcessor extends Thread {
		private final BasicLogger nestedLogger;
		private final BlockingQueue<AbstractLoggingEntry> logs;
		private volatile boolean isEnabled;
		private final AtomicBoolean isIdle = new AtomicBoolean(true);

		/**
		 * @param nestedLogger the nested logger
		 */
		LogProcessor(final BasicLogger nestedLogger) {
			super(LogProcessor.class.getSimpleName() + " for " + nestedLogger.getClass().getSimpleName());
			this.nestedLogger = nestedLogger;
			logs = new LinkedBlockingQueue<>();
			isEnabled = true;
		}

		/**
		 * Depended on isSynchronic() status of the nested logger, the log entry will be directly processed or queued to
		 * proceed it asynchronously.
		 *
		 * @param le log entry to process
		 */
		void add(final AbstractLoggingEntry le) {
			isIdle.set(false);
			logs.add(le);
		}

		/**
		 * Stops the processor tread.
		 */
		void stopJob() {
			isEnabled = false;
			interrupt();
		}

		/**
		 * @param entry log entry to pass to the nested logger.
		 */
		private void processLogEntry(final AbstractLoggingEntry entry) {
			if (entry instanceof LogEntry) {
				final LogEntry le = (LogEntry) entry;
				nestedLogger.log(le.getTimestamp(), le.getLogLevel(), le.getMsg(), le.getCause());
			} else if (entry instanceof TellLoggerEntry) {
				final TellLoggerEntry le = (TellLoggerEntry) entry;
				nestedLogger.tellLogger(le.getMsg(), le.getValue());
			} else {
				throw new RuntimeException("Unknown subclass of AbstractLoggingEntry");
			}
		}


		@Override
		public synchronized void start() {
			super.start();
		}


		@Override
		public void run() {
			while (isEnabled) {
				try {
					isIdle.set(logs.isEmpty());
					// Remove the proceed entry from the queue, wait till it gets new entries if empty
					AbstractLoggingEntry entry = logs.take();
					isIdle.set(false);
					processLogEntry(entry);
				} catch (InterruptedException e) {
					// Next, check the loop condition
				}
			}
		}
	}

}
