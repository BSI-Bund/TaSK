package com.achelos.task.commandlineexecution.genericcommandlineexecution;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.utilities.logging.LogBean;


/**
 * This class is intended to for executors. It starts a simulation in a new process, handles the output from the process
 * as it arrives and stores the logs to a file if required.
 */
public class GenericCommandLineExecution extends Logging {

	private Process process;
	private Thread shutdownHookThread = null;
	private final List<String> logList = new ArrayList<>();
	private ArrayList<LogBean> logBeanList = new ArrayList<>();
	private long exitValue = -1;
	protected boolean processLoggingOutputDone = false;
	private File outputFile = null;
	private final String testCaseName;
	private final TestRunPlanConfiguration configuration;
	private IterationCounter iterationCounter = null;

	/**
	 * PROCESS_EXIT_VALUE_OK: 0.
	 */
	protected static final long PROCESS_EXIT_VALUE_OK = 0;
	/**
	 * Queue for passing log lines from receiver thread to test suite. Implementation MUST be thread-safe!
	 */
	private final Queue<String> logQueue;
	private Thread logQueueProducer;

	/**
	 * Default constructor to start simulation in a new process.
	 *
	 * @param executor Currently running executor.
	 * @param testCaseName Currently running test case name.
	 * @param logger The logger to use to log messages.
	 */
	public GenericCommandLineExecution(final Executor executor, final String testCaseName, final BasicLogger logger) {
		super(logger, executor);
		logQueue = new ConcurrentLinkedQueue<>();
		this.testCaseName = testCaseName;
		configuration = TestRunPlanConfiguration.getInstance();
		iterationCounter = null;
	}


	/**
	 * Starts a simulation in a new process.
	 * <p>
	 * The output file and the working directory for the simulation may be set. If the output file is provided, then all
	 * the logs will be stored directly to the output file. If it is null, then the output received from the process
	 * will be stored to find find message etc.
	 * </p>
	 * <p>
	 * By default the process uses default working directory of the current process but it can also be set manually. Use
	 * null for default.
	 * </p>
	 * <p>
	 * Note: This constructor also spawns a thread that handles output from the process as it arrives, and terminates
	 * when the input stream closes (process termination).
	 * </p>
	 *
	 * @param command The commands to execute.
	 * @param outputFile The output file if the output should be written directly to file, null otherwise.
	 * @param workingDirectory The working directory of process or null.
	 * @throws IOException Throws IOException is thrown if process failed to start.
	 */
	protected void start(final List<String> command, final File outputFile, final File workingDirectory)
			throws IOException {
		logDebug("Starting " + getExecutor().getName() + " with parameters : " + command);
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(command);
		if (workingDirectory != null) {
			processBuilder.directory(workingDirectory);
			logDebug(getExecutor().getName() + " working directory : " + workingDirectory);
		}

		processBuilder.redirectErrorStream(true);

		if (outputFile != null) {
			processBuilder.redirectOutput(Redirect.appendTo(outputFile));
		}

		process = processBuilder.start();

		shutdownHookThread = new Thread() {
			@Override
			public void run() {
				if (process != null) {
					process.destroy();
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHookThread);
		if (outputFile == null) {
			/* Start output handler thread */
			LogQueueProducer prod = new LogQueueProducer(process.getInputStream(), logQueue, getLogger());
			logQueueProducer = new Thread(prod, getExecutor().getName() + " for " + getTestCaseName());
			logQueueProducer.start();
		}

		if (!process.isAlive()) {
			logError("Unable to start " + getExecutor().getName() + " for " + getTestCaseName() + ". Got exit value: "
					+ process.exitValue());
			throw new IOException("Unable to start " + getExecutor().getName() + " for " + getTestCaseName()
					+ ". Got exit value: " + process.exitValue());
		}
	}


	/**
	 * @return {@link List} containing log messages parsed so far. Must not be modified.
	 */
	protected List<String> getLogList() {
		return Collections.unmodifiableList(logList);
	}

	/**
	 * A runnable that reads lines from an {@link InputStream} and appends them to a {@link Queue}. Note that you MUST
	 * use a thread-safe queue implementation if you want to run the {@link LogQueueProducer} in one thread and consume
	 * the lines in another.
	 */
	private static class LogQueueProducer implements Runnable {
		private final BufferedReader reader;
		private final Queue<String> queue;
		private final BasicLogger log;

		/**
		 * @param source the stream to read from
		 * @param queue append read lines to this queue, MUST be thread-safe if the {@link LogQueueProducer} is run in a
		 * separate thread
		 * @param log
		 */
		LogQueueProducer(final InputStream source, final Queue<String> queue, final BasicLogger log) {
			reader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8));
			this.queue = queue;
			this.log = log;
		}


		/**
		 * Read lines from the reader as they become available and push them into the queue.
		 */
		@Override
		public void run() {
			String line;
			try {
				line = reader.readLine();
				while (line != null) {
					queue.add(line);
					line = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				log.log(System.currentTimeMillis(), BasicLogger.ERROR, "An error occurred while reading log into queue:", e);
				e.printStackTrace();
			}
		}

	}

	/**
	 * Fetch standard error and standard output of the simulation and append available lines to the internal list of log
	 * lines.
	 *
	 * @return the list containing the lines read, may be empty if no new data was available.
	 */
	protected final List<String> processLogQueue() {
		final List<String> newMessages = new LinkedList<>();
		while (!logQueue.isEmpty()) {
			final String s = logQueue.poll();
			newMessages.add(s);
		}
		logList.addAll(newMessages);
		return newMessages;
	}


	/**
	 * Method handles the logging output. Please note: This method encapsulates the already existing methods for
	 * processing the output, and it is not expected that these methods are used anymore.
	 *
	 * @param handleNoLogAsError This method allows the user to decide if a missing log leads to an error or not e.g.
	 * due to an expected TLS Test Tool abortion.
	 * @param tlsLogListComplete Indicates that the log list is already complete.
	 * @return list containing {@link LogBean} or null
	 * @throws IOException
	 */
	protected ArrayList<LogBean> processLoggingOutput(final boolean handleNoLogAsError,
			final boolean tlsLogListComplete)
			throws IOException {
		if (processLoggingOutputDone) {
			return getLogBeanList();
		}
		waitForProcess();
		if (getLogList().isEmpty()) {
			return null;
		}

		// Second step check the process exit value
		logInfo(getExecutor().getName() + " exit value = " + exitValue);
		if (PROCESS_EXIT_VALUE_OK == exitValue || exitValue == 143 || exitValue == 1 || exitValue == 127) { // 143 SIGTERM signal for CRL Server
			if (!tlsLogListComplete) {
				setLogBeanList(LogBean.convertToLogBeanList(getLogList()));
			}
			clearLogList();
			processLoggingOutputDone = true;
			return getLogBeanList();
		}
		// Log output of the process as error
		for (final String logEntry : getLogList()) {
			if (handleNoLogAsError) {
				logError(logEntry.replaceAll("\t", "    "));
			} else {
				logInfo(logEntry.replaceAll("\t", "    "));
			}
		}
		if (handleNoLogAsError) {
			throw new IOException("The execution of the " + getExecutor().getName() + " failed with the following exit code: " + exitValue );
		}
		return null;

	}


	/**
	 * Clears current logs.
	 */
	protected void clearLogList() {
		logList.clear();
	}


	/**
	 * Method blocks the calling thread until process finishes operation.
	 *
	 * @throws IOException
	 */
	private void waitForProcess() throws IOException {
		if (isNull()) {
			return;
		}
		try {
			waitFor();
		} catch (final InterruptedException e) {
			logInfo("Waiting for the " + getExecutor().getName() + " has been interrupted.");
		}
		processLogQueueAndCleanUp();
	}


	/**
	 * Retrieves and sets the log messages, set the exit code of the child process, de-registers the registered shutdown
	 * hook and releases the object.
	 */
	protected void processLogQueueAndCleanUp() {
		// Check if the process has been finished in the meantime due to a
		// timeout or so
		if (!isNull()) {
			exitValue = exitValue();
			processLogQueue();
			removeShutdownHook(); // This is important for releasing the object
			setNull();
		}
	}


	/**
	 * Stop the simulation by killing the process and remove shutdown hook.
	 */
	protected void stop() {
		destroy();
		removeShutdownHook();
	}


	/**
	 * Stops the simulation by killing the process.
	 */
	protected void destroy() {
		if (!isNull()) {
			process.destroy();
			try {
				process.waitFor(5000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logError(getExecutor().getName() + " process interrupted.");
			}
		}
	}


	/**
	 * Removes the shutdown hook.
	 */
	protected final void removeShutdownHook() {
		if (null != shutdownHookThread && Runtime.getRuntime().removeShutdownHook(shutdownHookThread)) {
			shutdownHookThread = null;
		}
	}


	/**
	 * @return exit code of the process.
	 * @throws IllegalThreadStateException if the process has not exited yet
	 */
	protected final long exitValue() {
		long exitCode = process.exitValue();
		logDebug("Reading exit code of " + getExecutor().getName() + ": " + exitCode);
		return exitCode;
	}


	/**
	 * Returns {@code true} if the process is not NULL and still running.
	 *
	 * @return {@code true} if the process is not NULL and currently running, false otherwise.
	 */
	protected boolean isRunning() {
		if (!isNull()) {
			return process.isAlive();
		}
		return false;
	}


	/**
	 * Return true if the process is null, false otherwise.
	 */
	protected boolean isNull() {
		return process == null;
	}


	/**
	 * Sets the process to null.
	 */
	protected void setNull() {
		process = null;
	}


	/**
	 * Wait for the process to terminate.
	 *
	 * @return the exit status of the process
	 * @throws InterruptedException if the current threat is interrupted while waiting
	 */
	protected final int waitFor() throws InterruptedException {
		return process.waitFor();
	}


	/**
	 * Wait for the process to terminate.
	 *
	 * @param timeout The maximum time to wait.
	 * @param unit the time unit of the timeout argument
	 * @return If the subprocess has already terminated then this method returns immediately with the value true. If the
	 * process has not terminated and the timeout value is less than, or equal to, zero, then this method returns
	 * immediately with the value false.
	 * @throws InterruptedException InterruptedException if the current threat is interrupted while waiting
	 */
	protected final boolean waitFor(final long timeout, final TimeUnit unit) throws InterruptedException {
		return process.waitFor(timeout, unit);
	}


	/**
	 * @return The log bean list.
	 */
	protected final ArrayList<LogBean> getLogBeanList() {
		return new ArrayList<>(logBeanList);
	}


	/**
	 * Clears log bean list.
	 */
	protected final void clearLogBeanList() {
		logBeanList.clear();
	}


	/**
	 * Adds log bean to log bean list.
	 *
	 * @param logBean The log bean item to add.
	 */
	protected void addLogBean(final LogBean logBean) {
		logBeanList.add(logBean);
	}


	/**
	 * Adds all LogBean's to log bean list.
	 *
	 * @param logBeanList The log beans to add.
	 */
	protected void addLogBeanList(final ArrayList<LogBean> logBeanList) {
		this.logBeanList.addAll(logBeanList);

	}


	/**
	 * Sets the {@link LogBean} list.
	 *
	 * @param logBeanList The log beans list to set.
	 */
	protected void setLogBeanList(final ArrayList<LogBean> logBeanList) {
		this.logBeanList = logBeanList;

	}

	/**
	 * <p>
	 * Start sleeping - just causes the Thread, to sleep as long, as the given time says (or an InterruptedException is
	 * caught).
	 *
	 * @param milliseconds number of milliseconds to sleep (negative values will be treated as 0)
	 */
	protected void startSleepTimer(final long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Method resets the log output. e.g. clears log bean list and set the value of processLoggingOutputDone to false.
	 */
	protected final void resetLog() {
		clearLogBeanList();
		processLoggingOutputDone = false;
	}


	/**
	 * Returns <b> true </b> if log queue has some logs that needs to be process.
	 *
	 * @return
	 */
	protected boolean hasQueueLogs() {
		return !logQueue.isEmpty();
	}


	/**
	 * @return the output file.
	 */
	protected File getLogFile() {
		return outputFile;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	/**
	 * Create a filename for log files from test case name and log file suffix.
	 *
	 * @return The output file where logs will be written.
	 * @throws IOException
	 */
	protected File createLogFile() throws IOException {
		String iterationSuffix = getIterationSuffix();
		var file = Paths
				.get(configuration.getReportDirectory().getAbsolutePath(), testCaseName, testCaseName
						+ getExecutor().getLogFileName() + iterationSuffix + getExecutor().getFileExtension())
				.toFile();
		var mkdirResult = file.getParentFile().mkdirs();
		if (!mkdirResult) {
			logDebug("Unable to create log file directory: " + file);
		}

		outputFile = file;
		return file;
	}

	private String getIterationSuffix() {
		return null == iterationCounter ? "" : iterationCounter.toFileNameSuffix();
	}


	protected void logFileCreated() {
		if (null != getLogFile() && getLogFile().exists()) {
			logInfo("Following external file(s) produced during test case execution:");
			logInfo(getExecutor().getName() + " log: " + getLogFile().getAbsolutePath());
			tellLogger(getExecutor().getName() + " log: ", getLogFile().toString());
			outputFile = null;
		}
	}

	protected TestRunPlanConfiguration getConfiguration() {
		return configuration;
	}


	/**
	 * @return the test case name
	 */
	protected String getTestCaseName() {
		return testCaseName;
	}


	protected void stopLogQueueProducer() {
		logDebug("Stopping " + logQueueProducer.getName() + ".");
		logQueueProducer.stop();
		logDebug("Stopped " + logQueueProducer.getName() + ".");
	}

	/**
	 * @return the iteration counter
	 */
	protected IterationCounter getIterationCounter() {
		return iterationCounter;
	}


	/**
	 * @param iterationCounter the iteration counter to set
	 */
	protected void setIterationCounter(final IterationCounter iterationCounter) {
		this.iterationCounter = iterationCounter;
	}

}
