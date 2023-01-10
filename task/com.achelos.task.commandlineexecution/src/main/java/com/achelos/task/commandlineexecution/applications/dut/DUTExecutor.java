package com.achelos.task.commandlineexecution.applications.dut;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.GenericCommandLineExecution;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.dutcommandgenerators.DUTCommandGenerator;
import com.achelos.task.logging.BasicLogger;

/**
 * Generic motivator that calls a given command or executable with command line parameters for triggering a TLS
 * connection to a remote management server that is represented by the Test Suite. It expects that a connection to its
 * IP address and port is possible.
 */
public class DUTExecutor extends GenericCommandLineExecution {
	private final DUTCommandGenerator dutCommandGenerator;

	/**
	 * Default constructor to start the DUT. By default, It is assumed that the test case does not have iterations and
	 * shall be executed once. If the test case has multiple iterations, #start(int, int) must be used.
	 *
	 * @param testCaseName the test case name
	 * @param log the logger to use for logging.
	 * @param dutCommandGenerator Command Generator for DUT ApplicationType.
	 */
	public DUTExecutor(final String testCaseName, final BasicLogger log, final DUTCommandGenerator dutCommandGenerator) throws IOException, URISyntaxException {
		super(Executor.DUT, testCaseName, log);
		setIterationCounter(null);
		this.dutCommandGenerator = dutCommandGenerator;
	}

	/**
	 * Gets the DUT executable and DUT call arguments from the MICS XML file and starts the DUT executor.
	 * <p>
	 * If the DUT executor should be executed for a session resumption then this method reads the value of
	 * <b>{@code <ResumeConnectionArguments>}</b> provided under <b>{@code <DUTCallArguments>}</b> in the MICS XML file.
	 * If the <b>{@code <ResumeConnectionArguments>}</b> is not provided then the value of
	 * <b>{@code <StartConnectionArguments>}</b> shall be used.
	 * </p>
	 *
	 * @param isSessionResumption {@code true} if the DUT executor should be executed for a session resumption,
	 * {@code false} otherwise
	 * @throws Exception may throw an exception if invalid or no call arguments are provided in the MICS XML file. or
	 * DUT executor could not be started
	 */
	public final void start(final boolean isSessionResumption)
			throws Exception {
		// Add a small delay to let DUT to store logs to file in order in case of session resumption.
		startSleepTimer(2000);

		// Get Command to call the DUT.
		var commands = dutCommandGenerator.connectToServer(isSessionResumption);

		var logFile = createLogFile();
		start(commands, logFile, null);

	}

	/**
	 * This method should be called, If a test case has multiple iterations.
	 * <p>
	 * The iteration information is required to create log files names with iteration information. These log file name
	 * will have post fix with each iteration information and can later be accessed in the report directory.
	 * <p>
	 *
	 * @param currentIteration current iteration number.
	 * @param totalNumberOfIterations total number of iterations.
	 * @throws Exception may throw an exception if invalid or no call arguments are provided in the MICS XML file. or
	 * DUT executor could not be started
	 */
	public final void start(final boolean isSessionResumption, final int currentIteration,
			final int totalNumberOfIterations) throws Exception {
		setIterationCounter(new IterationCounter(currentIteration, totalNumberOfIterations));
		start(isSessionResumption);
	}

	@Override
	public final void stop() {
		super.stop();
		destroy();
	}

	/**
	 * Resets the DUT executor process.
	 *
	 * @see #stop()
	 */
	public void resetProperties() {
		logDebug("Reset current " + getExecutor().getName() + " configuration.");
		
		if (isRunning()) {
			int dutExecutableTimeoutSeconds = getConfiguration().getDutExecutableTimeout();
			ExecutorService executor = Executors.newSingleThreadExecutor();
			try {
				executor.invokeAny(Arrays.asList(new WaitForRunningProcess()),
						dutExecutableTimeoutSeconds,
						TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				logDebug("Waiting for the " + getExecutor().getName() + " has been interrupted.");
			} // Timeout of given time in seconds.
			executor.shutdown();
			try {
				if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
		}

		stop();
		logFileCreated();

	}
	
	private class WaitForRunningProcess implements Callable<String> {
		@Override
		public String call() throws Exception {
			try {
				while (isRunning()) {
					final int sleepTimeMilliSeconds = 100;
					Thread.sleep(sleepTimeMilliSeconds);
				}
			} catch (InterruptedException e) {
				// do nothing.
			}
			return null;
		}
	}

	/**
	 * Stops the DUT executor process after 60 seconds if not stopped already. De-registers a previously-registered
	 * virtual-machine shutdown hook.
	 *
	 * @see #stop()
	 */
	public final void cleanAndExit() {
		final int sixty = 60;
		final int thousand = 1000;
		try {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					logError("Forcibly stopping " + getExecutor().getName() + " after 60 seconds in cleanAndExit.");
					destroy();
					removeShutdownHook();
				}
			}, sixty * thousand);
			logFileCreated();
			timer.cancel();
		} catch (Exception e) {
			logFileCreated();
			logError("An error occurred while trying to process the logging dump", e);
		}
	}

}
