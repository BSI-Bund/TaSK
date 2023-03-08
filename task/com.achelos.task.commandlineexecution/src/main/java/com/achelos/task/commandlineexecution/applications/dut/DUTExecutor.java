package com.achelos.task.commandlineexecution.applications.dut;

import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.IterationCounter;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.RunLogger;
import com.achelos.task.dutcommandgenerators.DUTCommandGenerator;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.utilities.logging.LogBean;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Generic motivator that calls a given command or executable with command line parameters for triggering a TLS
 * connection to a remote management server that is represented by the Test Suite. It expects that a connection to its
 * IP address and port is possible.
 */
public class DUTExecutor extends RunLogger {
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
		super(testCaseName,Executor.DUT, log);
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

		start(commands, null, null);

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
	public void resetProperties() throws IOException {
		logDebug("Reset current " + getExecutor().getName() + " configuration.");
		processLoggingOutput();
		if (null != getIterationCounter() && processLoggingOutputDone) {
			writeLogsToFile((ArrayList<LogBean>) getLogBeanList().clone());
		}
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
		resetLog();
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

	@Override
	public final void logEndOfIteration(final Writer writer) throws IOException {
		if (null != getIterationCounter()) {
			logInfo("End iteration " + getIterationCounter().getCurrentIteration() + " of "
					+ getIterationCounter().getTotalNumberOfIterations() + ".");
		}

	}

	/**
	 * Checks the Application Specific Inspection Instructions
	 * @param handshakeSuccessful Information whether the handshake was successful.
	 * @return Information whether the application specific inspection instructions are fulfilled.
	 * @throws IOException In case of an IO Error.
	 */
	public final boolean checkApplicationSpecificInspectionInstructions(boolean handshakeSuccessful) throws IOException {
		var searchString = dutCommandGenerator.applicationSpecificInspectionSearchString(handshakeSuccessful);
		if (searchString == null || searchString.isBlank()) {
			return true;
		}
		var logBean = findMessageMatch(searchString);
		var appSpecInspInstrSuccessful = dutCommandGenerator.applicationSpecificInspection(handshakeSuccessful, logBean);
		if (!appSpecInspInstrSuccessful) {
			logError("Unable to verify Application Specific Inspection Instructions.");
		} else {
			logInfo("Successfully verified Application Specific Inspection Instructions.");
		}
		return appSpecInspInstrSuccessful;
	}

	/*
	/**
	 * Stops the DUT executor process after 60 seconds if not stopped already. De-registers a previously-registered
	 * virtual-machine shutdown hook.
	 *
	 * @see #stop()
	 *
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

			//writeLogsToFile(processLoggingOutput(true, false));


			logFileCreated();
			timer.cancel();
		} catch (Exception e) {
			logFileCreated();
			logError("An error occurred while trying to process the logging dump", e);
		}
	}
	*/

}
