package com.achelos.task.commandlineexecution.genericcommandlineexecution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.utilities.logging.LogBean;


/**
 * Class for managing the logs in a separate process.
 */
public abstract class RunLogger extends GenericCommandLineExecution {

	/**
	 * Start a simulation in a new process.
	 *
	 * @param testCaseName Test case name that is going to be executed.
	 * @param executor Current running executor.
	 * @param log Logger to use for logging.
	 */
	public RunLogger(final String testCaseName, final Executor executor, final BasicLogger log) {
		super(executor, testCaseName, log);
	}


	/**
	 * Method stops the process and then processes queued logs.
	 */
	@Override
	protected void stop() {
		if (isNull()) {
			return;
		}
		super.stop();
		processLogQueueAndCleanUp();
	}


	/**
	 * Method handles the logging output. Please note: This method encapsulates the already existing methods for
	 * processing the output, and it is not expected that these methods are used anymore.
	 *
	 * @return list containing {@link LogBean} or null
	 * @throws IOException
	 */
	protected final ArrayList<LogBean> processLoggingOutput() throws IOException {
		return processLoggingOutput(true, false);
	}


	/**
	 * This method tells whether the message string matches the given regular expression from all log entries
	 * e.g. "TCP/IP connection to (.*) established."
	 *
	 * @param expectedMessage where the string may consist of regular expressions
	 * @return true, if the message string matches the given regular expression
	 * @throws IOException
	 */
	public final boolean assertMessageMatchLogged(final String expectedMessage)
			throws IOException {
		return assertMessageMatchLogged(expectedMessage, BasicLogger.ERROR);
	}


	/**
	 * This method tells whether the message string matches the given regular expression from all log entries
	 * e.g. "TCP/IP connection to (.*) established."
	 *
	 * @param expectedMessage where the string may consist of regular expressions
	 * @param logLevel The log level in case that the regular expression was not found taken from
	 * {@link LoggingConnector}
	 * @return true, if the message string matches the given regular expression
	 * @throws IOException
	 */
	public final boolean assertMessageMatchLogged(final String expectedMessage, final long logLevel)
			throws IOException {
		boolean messageLogged = false;
		logExpectedMessage(expectedMessage);
		final LogBean actualMessage = findMessageMatch(expectedMessage);
		if (null == actualMessage) {
			logMessageNotFound(expectedMessage, logLevel);
		} else {
			messageLogged = true;
			logActualMessage(actualMessage);
		}
		return messageLogged;
	}


	/**
	 * Searches the log for the given message and returns either the found element or null.
	 *
	 * @param message input to search for within list of logging data
	 * @return found element or null.
	 * @throws IOException
	 */
	protected final LogBean findMessage(final String message) throws IOException {
		return findMessage(message, false, true);
	}


	/**
	 * Searches the log for the given message and returns either the found element or null.
	 *
	 * @param message input to search for within list of logging data
	 * @param handleNoLogAsError This method allows the user to decide if a missing log leads to an error or not e.g.
	 * due to an expected TLS process abortion.
	 * @return found element or null.
	 * @throws IOException
	 */
	protected final LogBean findMessage(final String message, final boolean handleNoLogAsError)
			throws IOException {
		return findMessage(message, false, handleNoLogAsError);
	}


	/**
	 * This method tells whether the message string matches the given regular expression.
	 *
	 * @param message the message string
	 * @return found element or null.
	 * @throws IOException
	 */
	protected LogBean findMessageMatch(final String message)
			throws IOException {
		return findMessage(message, true, true);
	}


	/**
	 * Searches the log for the given message and returns either the found element or null.
	 *
	 * @param message input to search for within list of logging data.
	 * @param match indicates that this method matches the given regular expression.
	 * @param handleNoLogAsError This method allows the user to decide if a missing log leads to an error or not e.g.
	 * due to an expected TLS process abortion.
	 * @return found element or null.
	 * @throws IOException
	 */
	protected final LogBean findMessage(final String message, final boolean match, final boolean handleNoLogAsError)
			throws IOException {

		// Fetch the current global log bean list if it has not been done yet
		if (getLogBeanList().isEmpty() && !isNull()) {
			setLogBeanList(LogBean.convertToLogBeanList(processLogQueue()));
		}

		// Search the given message in the current global log bean list
		for (final LogBean item : getLogBeanList()) {
			if ((match && item.getMessage().matches(message)) || (!match && item.getMessage().contains(message))) {
				return item;
			}
		}

		// Cancel here, if the process does not run anymore,
		// because the current global log bean list is already complete in this case
		if (isNull()) {
			return null;
		}
		List<Future<LogBean>> future;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			int maxWaitTime = getConfiguration().getMaximumWaitTimeForReadingLogMessage();
			logDebug("Search message: \"" + message + "\" in " + maxWaitTime + " seconds.");
			future = executor.invokeAll(Arrays.asList(new SearchLogMessage(match, message)), maxWaitTime,
					TimeUnit.SECONDS);
			executor.shutdown();
			try {
			    if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
			    	executor.shutdownNow();
			    } 
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
			if (future.get(0).isCancelled()) {
				logDebug("Timed out while searching the message: \"" + message + "\" in " + maxWaitTime + " seconds.");
				return null;
			}
			LogBean logBean = future.get(0).get();
			if (logBean != null) {
				return logBean;
			}
		} catch (InterruptedException | ExecutionException e1) {
			return null;
		} // Timeout of given time in seconds.


		// Indicate that the log bean list is already complete here
		processLoggingOutput(handleNoLogAsError, true);

		// The TLS tool has terminated and the given message was not found
		return null;
	}

	private class SearchLogMessage implements Callable<LogBean> {
		private final boolean match;
		private final String message;

		SearchLogMessage(final boolean match, final String message) {
			this.match = match;
			this.message = message;
		}

		@Override
		public LogBean call() throws Exception {
			do {
				// If the process does not run anymore, wait until the process is completely
				// terminated and run this loop one last time
				if (!isRunning()) {
					try {
						waitFor();
					} catch (final InterruptedException e) {
						logInfo("Waiting for the " + getExecutor().getName() + " has been interrupted.");
					}
				}

				// Fetch the new log bean entries ...
				final ArrayList<LogBean> tlsLogBeanNew = LogBean.convertToLogBeanList(processLogQueue());

				// ... And add them to the global log bean list
				addLogBeanList(tlsLogBeanNew);

				// Search the given message only in the new fetched log bean entries
				for (final LogBean item : tlsLogBeanNew) {
					if ((match && item.getMessage().matches(message))
							|| (!match && item.getMessage().contains(message))) {
						return item;
					}
				}
				try {
					// Allow the thread to perform a context-switch. Avoid non-busy wait.
					final int sleepTimeMilliSeconds = 100;
					Thread.sleep(sleepTimeMilliSeconds);
				} catch (InterruptedException e) {
					throw e;
				}
			} while (isRunning() || hasQueueLogs());
			return null;
		}
	}

	/**
	 * Method searches for the closest message even if the full expectation is not match to return valuable information
	 * e.g. differing alert messages.
	 *
	 * @param message input to search for within list of logging data
	 * @return found element or null.
	 * @throws IOException
	 */
	protected final LogBean findClosestMessage(final String message) throws IOException {
		final String[] valuePair = message.split("=");
		if (valuePair.length != 2) {
			return null;
		}

		return findMessage(valuePair[0]);
	}


	/**
	 * Get the value of a key-value-pair in the log and returns the found value or null in case that nothing was found.
	 *
	 * @param key The Key value
	 * @param logLevel The log level in case that nothing was found taken from {@link LoggingConnector}
	 * @return found value or null.
	 * @throws IOException
	 */
	protected final String getValue(final String key, final long logLevel) throws IOException {
		logDebug("Searching for '" + key + "'");
		final LogBean logEntry = findMessage(key);

		return getValue(key, logEntry, logLevel);

	}


	/**
	 * @return the value of a key-value-pair in the log and returns the found value or null in case that nothing was
	 * found.
	 * @param key The Key value
	 * @throws IOException
	 */
	protected final String getValue(final String key) throws IOException {
		return getValue(key, BasicLogger.ERROR);
	}


	private String getValue(final String key, final LogBean logEntry, final long logLevel) {
		if (logEntry != null) {
			final String[] valuePair = logEntry.getMessage().split("=");

			if (valuePair.length == 2) {
				logDebug("Found value: " + valuePair[1]);
				return valuePair[1];
			}
			if (logEntry.getMessage().compareTo(key + "=") == 0) {
				logDebug("Found an empty value.");
				return null;
			} else {
				log(logLevel, "Couldn't find the key: " + key + ". The expected syntax differs from the found one.");
			}
		}
		log(logLevel, "Couldn't find the key: " + key);
		return null;
	}


	/**
	 * Get the value of a key-value-pair in the log and returns the found value or null in case that nothing was found.
	 * In case that an element was not found simple an 'info' is generated in order to don't have any impact on the
	 * test case execution result.
	 *
	 * @param key The Key value to search for.
	 * @return found value or null.
	 * @throws IOException
	 */
	protected final String getOptionalValue(final String key) throws IOException {
		return getValue(key, BasicLogger.INFO);
	}


	/**
	 * Searches the log for the given message and returns either all found elements matching the input message or null.
	 *
	 * @param message input to search for within list of logging data
	 * @return found element or null.
	 * @throws IOException
	 */
	protected final List<LogBean> findMessages(final String message)
			throws IOException {
		final List<LogBean> foundMessages = new ArrayList<>();
		processLoggingOutput();
		for (final LogBean item : getLogBeanList()) {
			if (item.getMessage().contains(message)) {
				foundMessages.add(item);
			}
		}
		if (foundMessages.isEmpty()) {
			return null;
		}
		return foundMessages;
	}


	/**
	 * Method searches a given message identifier within gathered logging and returns the proper finding or the closest
	 * message which was found.
	 *
	 * @param expectedMessage The message to search for
	 * @param logLevel The log level in case that the message was not found taken from {@link LoggingConnector}
	 * @return true, if the message was found
	 * @throws IOException
	 */
	public final boolean assertMessageLogged(final String expectedMessage, final long logLevel) throws IOException {
		boolean messageLogged = false;
		logExpectedMessage(expectedMessage);
		final LogBean actualMessage = findMessage(expectedMessage);
		if (null == actualMessage) {
			logMessageNotFound(expectedMessage, logLevel);
			final LogBean closestMessage = findClosestMessage(expectedMessage);
			if (null != closestMessage) {
				log(logLevel, "Actual log message found (" + closestMessage.getTimestampString() + "): "
						+ closestMessage.getMessage());
			}
		} else {
			messageLogged = true;
			logActualMessage(actualMessage);
		}
		return messageLogged;
	}

	/**
	 * Write a log line for the given expected message.
	 *
	 * @param expectedMessage that will be shown.
	 */
	public void logExpectedMessage(final String expectedMessage) {
		logInfo("Expected log message: " + expectedMessage);
	}

	/**
	 * Write a log line for the given log bean containing its time stamp and message.
	 *
	 * @param actualMessage Log bean that will be shown
	 */
	public final void logActualMessage(final LogBean actualMessage) {
		if (actualMessage != null) {
			if (actualMessage.getTimestamp() != null) {
				logInfo("Actual log message: (" + actualMessage.getTimestampString() + "): "
						+ actualMessage.getMessage());
			} else {
				logInfo("Actual log message: (" + actualMessage.toString() + ")");
			}
		}
	}

	/**
	 * Write a log line "Expected log message not found." for the given log level.
	 *
	 * @param expectedMessage The expected log message.
	 * @param logLevel The log level.
	 */
	public void logMessageNotFound(final String expectedMessage, final long logLevel) {
		log(logLevel, "Expected log message \"" + expectedMessage + "\" not found.");
	}


	/**
	 * Method searches a given message identifier within gathered logging and returns the proper finding or the closest
	 * message which was found.
	 *
	 * @param expectedMessage The message to search for
	 * @return true, if the message was found
	 * @throws IOException
	 */
	public final boolean assertMessageLogged(final String expectedMessage) throws IOException {
		return assertMessageLogged(expectedMessage, BasicLogger.ERROR);
	}


	/**
	 * Method searches a given message identifier within gathered logging and returns the proper finding or informs that
	 * nothing was found.
	 *
	 * @param expectedMessage The message to search for
	 * @throws IOException
	 */
	public final void infoMessageLogged(final String expectedMessage) throws IOException {
		logExpectedMessage(expectedMessage);
		final LogBean actualMessage = findMessage(expectedMessage);
		if (null == actualMessage) {
			logInfo("Log message \"" + expectedMessage + "\" not found.");
		} else {
			logActualMessage(actualMessage);
		}
	}


	/**
	 * Method handles a whole list of log beans and writes the information persistent to the file system.
	 *
	 * @param list The log history filled with information during test case iterations
	 * @throws IOException
	 */
	protected void writeLogsToFile(final ArrayList<LogBean> list) throws IOException {
		if (null == list) {
			return;
		}

		File logFile = createLogFile();
		boolean append = getLogFile().exists();
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(logFile, append), StandardCharsets.UTF_8))) {
			for (final LogBean item : list) {
				final String logLine = item.toString();
				// Write information to the file
				writer.write(logLine);
				writer.write(System.lineSeparator());
			}
			logFileCreated();
			logEndOfIteration(writer);
		}
	}

	/**
	 * Perform clean up at the end of a test case (e.g., stopping services, logging information) and writing logs to
	 * file.
	 */
	public final void cleanAndExit() {

		final int sixty = 60;
		final int thousand = 1000;
		try {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					logError("Forcibly stopping " + getExecutor().getName() + " after 60 seconds in clean and exit.");
					stop();
				}
			}, sixty * thousand);
			// Do not handle empty log dump as error when running Executor as DUT
			writeLogsToFile(processLoggingOutput(true, false));
			timer.cancel();
		} catch (IOException e) {
			try {
				writeLogsToFile(LogBean.convertToLogBeanList(getLogList()));
			} catch (IOException e1) {
				// Do nothing.
			} finally {
				logError("An error occurred while trying to process the logging dump", e);
				clearLogList();
			}
		}
		resetLog();
	}


	/**
	 * Method writes iteration information in case a test case has multiple executions.
	 *
	 * @param writer the writer to write information
	 * @throws IOException may throw an exception if unable to write the information.
	 */
	protected abstract void logEndOfIteration(Writer writer) throws IOException;

}
