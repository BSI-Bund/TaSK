package com.achelos.task.abstractinterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.achelos.task.logging.LoggingConnector;

public class TaskExecutionParameters {
    private final TaskExecutionMode executionMode;
    // General Parameters
    private final LoggingConnector logger;
    private final File configFile;
    private final String reportDirectory;
    private final String clientAuthCertChain;
    private final String clientAuthKeyFile;

    private final String certValidationRootCAValue;

    // ExecutionMode specific parameters
    private final File testRunPlanFile;
    private final File micsFile;
    private final List<File> certificateFileList;
    private final boolean ignoreMicsVerification;
    private boolean onlyGenerateTRP = false;

    /**
     * Constructor for ExecutionMode MICS with run plan execution.
     * @param logger
     * @param configFile
     * @param micsFile
     * @param certificateFileList
     * @param ignoreMicsVerification
     * @param reportDirectory
     * @param clientAuthCertChain
     * @param clientAuthKeyFile
     */
    public TaskExecutionParameters(final LoggingConnector logger,
                                   final File configFile,
                                   final File micsFile,
                                   final List<File> certificateFileList,
                                   final boolean ignoreMicsVerification,
                                   final String reportDirectory,
                                   final String clientAuthCertChain,
                                   final String clientAuthKeyFile,
                                   final String certValidationRootCAValue) {
        this.logger = LoggingConnector.getInstance();
        this.configFile = configFile;
        this.certificateFileList = certificateFileList != null ? new ArrayList<>(certificateFileList) : new ArrayList<>();
        this.ignoreMicsVerification = ignoreMicsVerification;
        this.executionMode = TaskExecutionMode.MICS;
        this.micsFile = micsFile;
        this.reportDirectory = reportDirectory;
        this.clientAuthCertChain = clientAuthCertChain;
        this.clientAuthKeyFile = clientAuthKeyFile;
        this.certValidationRootCAValue = certValidationRootCAValue;

        // Unused parameters for this execution mode
        this.testRunPlanFile = null;
    }
    
    /**
     * Constructor for ExecutionMode MICS with optional run plan execution.
     * Only generates the test run plan if param onlyGenerateTRP is true.
     * 
     * @param logger
     * @param configFile
     * @param micsFile
     * @param certificateFileList
     * @param ignoreMicsVerification
     * @param onlyGenerateTRP if true, run plan execution will be skipped after generating the TRP.
     * @param reportDirectory
     * @param clientAuthCertChain
     * @param clientAuthKeyFile
     */
    public TaskExecutionParameters(final LoggingConnector logger,
                                   final File configFile,
                                   final File micsFile,
                                   final List<File> certificateFileList,
                                   final boolean ignoreMicsVerification,
                                   final boolean onlyGenerateTRP,
                                   final String reportDirectory,
                                   final String clientAuthCertChain,
                                   final String clientAuthKeyFile,
                                   final String certValidationRootCAValue) {
    	this(logger, configFile, micsFile, certificateFileList, ignoreMicsVerification, 
    			reportDirectory, clientAuthCertChain, clientAuthKeyFile, certValidationRootCAValue);
    	this.onlyGenerateTRP = onlyGenerateTRP;
    }

    /**
     * Constructor for ExecutionMode TRP
     * 
     * @param logger
     * @param testRunPlanFile
     * @param configFile
     * @param reportDirectory
     * @param clientAuthCertChain
     * @param clientAuthKeyFile
     */
    public TaskExecutionParameters(final LoggingConnector logger,
                                   final File testRunPlanFile,
                                   final File configFile,
                                   final String reportDirectory,
                                   final String clientAuthCertChain,
                                   final String clientAuthKeyFile,
                                   final String certValidationRootCAValue) {
        this.logger = LoggingConnector.getInstance();
        this.configFile = configFile;
        this.testRunPlanFile = testRunPlanFile;
        this.executionMode = TaskExecutionMode.TRP;
        this.reportDirectory = reportDirectory;
        this.clientAuthCertChain = clientAuthCertChain;
        this.clientAuthKeyFile = clientAuthKeyFile;
        this.certValidationRootCAValue = certValidationRootCAValue;

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
    public String getCertificateValidationCA() {
        return certValidationRootCAValue;
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

    public boolean isOnlyGenerateTRP() {
		return onlyGenerateTRP;
	}

	public static enum TaskExecutionMode {
        TRP,
        MICS;
    }
}
