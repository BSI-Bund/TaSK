package com.achelos.task.dutexecution;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.dutmotivator.DUTMotivator;
import com.achelos.task.dutmotivator.ManualDUTMotivator;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.utilities.logging.IterationCounter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DUTExecutor {
    private final static String LOGFILE_NAME = "_dut";
    private final static String LOGFILE_EXTENSION = ".log";
    private final static String LOGGER_COMPONENT = "DUTExecutor: ";
    private final DUTMotivator dutMotivator;
    private Future<List<String>> executionOutput;
    private final List<String> dutExecutorOutput;
    private final LoggingConnector loggingConnector;
    private IterationCounter iterationCounter;
    private final TestRunPlanConfiguration configuration;
    private final String testCaseName;

    /**
     * The Constructor of the DUTExecutor. Requires a DUTMotivator instance.
     * @param testCaseName The name of the TestCase in which this DUTExecutor is used. Used for Logging purposes.
     * @param logger LoggingConnector Instance.
     * @param dutMotivator DUTMotivator instance.
     */
    public DUTExecutor(final String testCaseName, final BasicLogger logger, final DUTMotivator dutMotivator) {
        this.dutMotivator = dutMotivator;
        this.loggingConnector = LoggingConnector.getInstance();
        this.dutExecutorOutput = new ArrayList<>();
        this.configuration = TestRunPlanConfiguration.getInstance();
        this.testCaseName = testCaseName;
    }

    /**
     * Execute the DUTMotivators connectToServer method in a separate thread.
     * @param isSessionResumption Whether the connection shall be established with session resumption.
     */
    public void start(final boolean isSessionResumption) {
        var executor = Executors.newSingleThreadExecutor();
        executionOutput = executor.submit(() -> dutMotivator.motivateConnectionToTaSK(isSessionResumption));
    }

    /**
     * Execute the DUTMotivators connectToServer method in a separate thread.
     * @param isSessionResumption Whether the connection shall be established with session resumption.
     * @param currentIteration The current iteration counter.
     * @param totalNumberOfIterations The max. iteration counter.
     */
    public void start(final boolean isSessionResumption,  final int currentIteration,
                      final int totalNumberOfIterations) {
        this.iterationCounter = new IterationCounter(currentIteration, totalNumberOfIterations);
        this.start(isSessionResumption);
    }

    /**
     * Clean and Exit the execution.
     */
    public void cleanAndExit() {
       cleanAndExit(true);
    }

    /**
     * Clean and Exit the execution.
     */
    public void cleanAndExit(boolean lastIteration) {
        writeOutputFile();
        interrupt(lastIteration);
        executionOutput = null;
        dutExecutorOutput.clear();
    }

    /**
     * Create a filename for log files from test case name and log file suffix.
     *
     * @return The output file where logs will be written.
     */
    private File createLogFile() {
        String iterationSuffix = iterationCounter != null? iterationCounter.toFileNameSuffix() : "";
        var file = Paths
                .get(configuration.getReportDirectory().getAbsolutePath(), testCaseName, testCaseName
                        + LOGFILE_NAME + iterationSuffix + LOGFILE_EXTENSION)
                .toFile();
        var mkdirResult = file.getParentFile().mkdirs();
        if (!mkdirResult) {
            logDebug("Unable to create log file directory: " + file);
        }
        return file;
    }

    private void interrupt(boolean lastIteration) {
        if (executionOutput != null && !executionOutput.isDone()) {
            executionOutput.cancel(true);
        }
    }

    private void writeOutputFile() {
        File logFile = createLogFile();
        // Write dutExecutorOutput into logFile.
        try {
            writeLogsToFile(logFile, dutExecutorOutput);
        } catch (Exception e) {
            logError("Unable to write logfile", e);
        }
        // Write executionOutput
        if (executionOutput != null) {
            try {
                var returnValue = executionOutput.get(60, TimeUnit.SECONDS);
                writeLogsToFile(logFile, returnValue);
            } catch (Exception e) {
                try {
                    writeLogsToFile(logFile, Arrays.asList("Unable to retrieve log of DUT.", "DUT was interrupted.", e.getMessage()));
                } catch (Exception e1) {
                    logError("Unable to write DUT logfile", e1);
                }
            }
        } else {
            try {
                writeLogsToFile(logFile, List.of("Unable to retrieve log of DUT."));
            } catch (Exception e) {
                logError("Unable to write DUT logfile", e);
            }
        }
    }

    private void writeLogsToFile(final File logFile, final List<String> logList) throws IOException {

        boolean append = logFile.exists();
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(logFile, append), StandardCharsets.UTF_8))) {
            for (final String logLine : logList) {
                // Write information to the file
                writer.write(logLine);
                writer.write(System.lineSeparator());
            }
            if (null != iterationCounter) {
                logInfo("End iteration " + iterationCounter.getCurrentIteration() + " of "
                        + iterationCounter.getTotalNumberOfIterations() + ".");
            }
        }
    }

    /**
     * Checks the Application Specific Inspection Instructions
     * @param handshakeSuccessful Information whether the handshake was successful.
     * @return Information whether the application specific inspection instructions are fulfilled.
     * @throws IOException In case of an IO Error.
     */
    public final boolean checkApplicationSpecificInspectionInstructions(boolean handshakeSuccessful) throws IOException {
        // Retrieve the DUT Logs
        List<String> logOfDut = null;
        try {
            logOfDut = executionOutput.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            logError("Unable to retrieve logs of DUT. Cannot verify Application Specific Inspection Instructions.");
            return false;
        }
        if (logOfDut == null) {
            logError("Cannot find matching Application Specific Inspection Instruction log message.");
            return false;
        }
        // Check the ApplicationSpecificInspectionInstructions
        var appSpecInspInstrSuccessful = dutMotivator.checkApplicationSpecificInspections(handshakeSuccessful, logOfDut);
        
        if (dutMotivator instanceof ManualDUTMotivator) {
        	logWarning("Application Specific Inspection Instructions not available from manually motivated DUT.");
        } else {
	        if (!appSpecInspInstrSuccessful) {
	            logError("Unable to verify Application Specific Inspection Instructions.");
	        } else {
	            logInfo("Successfully verified Application Specific Inspection Instructions.");
	        }
        }
        return appSpecInspInstrSuccessful;
    }

    /**
     * Resets the DUT executor process.
     */
    public void resetProperties() throws IOException {
        cleanAndExit(false);
    }

    private void logError(final String msg, final Throwable t) {
        loggingConnector.error(LOGGER_COMPONENT + msg, t);
    }

    private void logError(final String msg) {
        loggingConnector.error(LOGGER_COMPONENT + msg);
    }

    private void logWarning(final String msg) {
        loggingConnector.warning(LOGGER_COMPONENT + msg);
    }

    private void logInfo(final String msg) {
        loggingConnector.info(LOGGER_COMPONENT + msg);
    }

    private void logDebug(final String msg) {
        loggingConnector.debug(LOGGER_COMPONENT + msg);
    }

}
