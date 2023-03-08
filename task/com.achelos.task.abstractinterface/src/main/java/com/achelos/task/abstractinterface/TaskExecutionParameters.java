package com.achelos.task.abstractinterface;

import com.achelos.task.logging.LoggingConnector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TaskExecutionParameters {
    private final TaskExecutionMode executionMode;
    // General Parameters
    private final LoggingConnector logger;
    private final File configFile;
    private final String reportDirectory;
    private final String clientAuthCertChain;
    private final String clientAuthKeyFile;

    // ExecutionMode specific parameters
    private final File testRunPlanFile;
    private final File micsFile;
    private final List<File> certificateFileList;
    private final boolean ignoreMicsVerification;

    public TaskExecutionParameters(final LoggingConnector logger,
                                   final File configFile,
                                   final File micsFile,
                                   final List<File> certificateFileList,
                                   final boolean ignoreMicsVerification,
                                   final String reportDirectory,
                                   final String clientAuthCertChain,
                                   final String clientAuthKeyFile) {
        this.logger = LoggingConnector.getInstance();
        this.configFile = configFile;
        this.certificateFileList = certificateFileList != null ? new ArrayList<>(certificateFileList) : new ArrayList<>();
        this.ignoreMicsVerification = ignoreMicsVerification;
        this.executionMode = TaskExecutionMode.MICS;
        this.micsFile = micsFile;
        this.reportDirectory = reportDirectory;
        this.clientAuthCertChain = clientAuthCertChain;
        this.clientAuthKeyFile = clientAuthKeyFile;

        // Unused parameters for this execution mode
        this.testRunPlanFile = null;
    }

    public TaskExecutionParameters(final LoggingConnector logger,
                                   final File testRunPlanFile,
                                   final File configFile,
                                   final String reportDirectory,
                                   final String clientAuthCertChain,
                                   final String clientAuthKeyFile) {
        this.logger = LoggingConnector.getInstance();
        this.configFile = configFile;
        this.testRunPlanFile = testRunPlanFile;
        this.executionMode = TaskExecutionMode.TRP;
        this.reportDirectory = reportDirectory;
        this.clientAuthCertChain = clientAuthCertChain;
        this.clientAuthKeyFile = clientAuthKeyFile;

        // Unused parameters for this execution mode
        this.ignoreMicsVerification = false;
        this.certificateFileList = null;
        this.micsFile = null;
    }

    public TaskExecutionMode getExecutionMode() {
        return executionMode;
    }

    public LoggingConnector getLogger() {
        return LoggingConnector.getInstance();
    }

    public File getConfigFile() {
        return configFile;
    }

    public String getReportDirectory() {
        return reportDirectory;
    }

    public String getClientAuthCertChain() {
        return clientAuthCertChain;
    }

    public String getClientAuthKeyFile() {
        return clientAuthKeyFile;
    }

    public File getTestRunPlanFile() {
        return testRunPlanFile;
    }

    public File getMicsFile() {
        return micsFile;
    }

    public List<File> getCertificateFileList() {
        return new ArrayList<>(certificateFileList);
    }

    public boolean isIgnoreMicsVerification() {
        return ignoreMicsVerification;
    }

    public static enum TaskExecutionMode {
        TRP,
        MICS;
    }
}
