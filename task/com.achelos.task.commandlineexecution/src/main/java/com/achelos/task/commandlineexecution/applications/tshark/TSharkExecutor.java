package com.achelos.task.commandlineexecution.applications.tshark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import com.achelos.task.commandlineexecution.applications.tshark.exception.TSharkException;
import com.achelos.task.commandlineexecution.applications.tshark.helper.TSharkParameters;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.GenericCommandLineExecution;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.logging.LoggingConnector;


/**
 * Class for running the TShark in a separate process.
 */
public class TSharkExecutor extends GenericCommandLineExecution {
	private BufferedReader stdOut;
	private File processErrorOutput = null;
	private final TSharkParameters tSharkSettings;
	private final TestRunPlanConfiguration configuration;

	/**
	 * Start a TShark process to capture network traffic.
	 *
	 * @param testCaseName The test case name.
	 * @param log The logger to use
	 * @throws FileNotFoundException if the Dumpcap output file cannot be created
	 * @throws TSharkException if TShark parameters are not correct
	 */
	public TSharkExecutor(final String testCaseName, final LoggingConnector log)
																					throws FileNotFoundException,
																					TSharkException {
		super(Executor.TSHARK, testCaseName, log);
		configuration = TestRunPlanConfiguration.getInstance();
		tSharkSettings = new TSharkParameters();
	}


	/**
	 * Method starts the Dumpcap tool (e.g. wireshark).
	 *
	 * @throws IOException
	 * @throws TSharkException
	 */
	public final void start()
			throws IOException, TSharkException {
		if (configuration.isTsharkEnabled()) {
			try {
				processErrorOutput = File.createTempFile("dumpcap_", "_standard_output.txt");
				processErrorOutput.deleteOnExit();
			} catch (IOException e) {
				logError("Error occured while creating a temporary output file", e);
				throw new FileNotFoundException("Error occured while creating a temporary output file" + e.getMessage());
			}

			final File tSharkFile = tSharkSettings.getTSharkExecutableFile();
			try {
				final List<String> command = new ArrayList<>();
				command.add(tSharkFile.getName());
				// Flush standard output
				command.add("-l");

				// Interface
				command.add("-i");
				command.add(tSharkSettings.getTSharkInterface());

				// Additional options
				final String tSharkOptions = tSharkSettings.getTSharkOptions();
				try (Scanner sc = new Scanner(tSharkOptions)) {
					final Pattern pattern = Pattern.compile("\"[^\"]*\"" + "|'[^']*'" + "|[^ ]+");
					String option;
					while ((option = sc.findInLine(pattern)) != null) {
						option = option.trim();
						if (option.startsWith("'") && option.endsWith("'")
								|| option.startsWith("\"") && option.endsWith("\"")) {
							option = option.substring(1, option.length() - 1);
						}
						command.add(option);
					}
				}

				// Output file
				command.add("-w");
				command.add(createLogFile().getCanonicalPath());

				start(command, processErrorOutput, tSharkFile.getParentFile());

				logDebug("Wait until TShark is started successfully");
				

			} catch (IOException e) {
				logError("An error occurred while executing TShark Dumpcap:  " + e.getMessage());
				throw new FileNotFoundException("An error occurred while executing TShark Dumpcap: " + e.getMessage());
			}

			try {
				// We need to wait to let TShark startup (prevent errors on
				// non-captured network traffic)
				stdOut = new BufferedReader(
						new InputStreamReader(new FileInputStream(processErrorOutput), StandardCharsets.UTF_8));

				ExecutorService executor = Executors.newSingleThreadExecutor();
				Callable<Object> task = new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							while (!stdOut.ready()) {
								Thread.sleep(100);
								if (Thread.interrupted()) {
									return null;
								}
							}

						} catch (InterruptedException e) {
							//do nothing
						}
						return null;
					}
				};
				Collection<Callable<Object>> collection = new ArrayList<Callable<Object>>();
				collection.add(task);
				executor.invokeAny(collection, 2, TimeUnit.SECONDS);

				executor.shutdown();
				try {
					if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
						executor.shutdownNow();
					}
				} catch (InterruptedException e) {
					executor.shutdownNow();
				}

				if (stdOut.ready()) {
					String startMessage = stdOut.readLine();
					if (null == startMessage) {
						return;
					}
					// startMessage == "Capturing"
					if (!startMessage.contains("Capturing") && !"Capturing".contains(startMessage)) {
						logError("An error occurred while starting TShark.");
						throw new TSharkException("Error reason: " + startMessage);
					}
				} else {
					logError("An error occurred while starting TShark.");
				}
			} catch (IOException e) {
				logError("An error occurred while starting TShark.");
				throw new TSharkException("Error reason: " + e);
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Stop the simulation by killing the process.
	 */
	@Override
	public final void stop() {
		if (configuration.isTsharkEnabled()) {
			final int delay = 1000;
			startSleepTimer(delay); // wait some time to give Wireshark the chance to collect all network traffic.
			destroy();
			while (isRunning()) {
				logDebug("TShark process is still alive.");
				startSleepTimer(delay);
			}
			super.removeShutdownHook();
		}
	}

	/**
	 * Stops the TShark executor. De-registers a previously-registered virtual-machine shutdown hook.
	 * 
	 * @see #stop()
	 */
	public void cleanAndExit() {
		if (configuration.isTsharkEnabled()) {
			stop();
			logFileCreated();
			resetLog();
		}
	}

}
