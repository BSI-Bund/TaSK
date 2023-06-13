package com.achelos.task.restimpl.server;

import com.achelos.task.abstractinterface.TaskTestTool;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.reporting.datastructures.ReportLogger;
import com.achelos.task.reporting.pdfreport.PdfReport;
import com.achelos.task.reporting.xmlreport.XmlReport;
import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameterNames;
import com.achelos.task.xmlparser.datastructures.testrunplan.TestRunPlanData;
import com.achelos.task.xmlparser.runplanparsing.RunPlanParser;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class TesttoolRunner implements Runnable{

	//private TaskTestTool taskTesttoolInstance;
	
	private volatile boolean canceled;
	private final HashMap<String, GlobalConfigParameter> globalConfiguration;
	private final File globalConfigFile;
	private final LoggingConnector logger;
	
	public TesttoolRunner(final File globalConfigFile) {
		this.canceled = false;
		this.logger = LoggingConnector.getInstance();
		this.globalConfigFile = globalConfigFile;
		try {
			globalConfiguration = ConfigParser.parseGlobalConfig(globalConfigFile);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("An error occurred while parsing the global configuration File: " + e.getMessage(), e);
		}

		if (globalConfiguration.isEmpty()) {
			throw new RuntimeException("An error occurred while parsing the global configuration file. Empty global configuration provided." + globalConfigFile);
		}
		if (globalConfiguration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName()) == null) {
			throw new IllegalArgumentException("Unspecified required global configuration file: "
					+ GlobalConfigParameterNames.ReportDirectory.getParameterName());
		}
	}

	@Override
	public void run() {

		while (!canceled) {
			// Poll for next Execution Request.
			TaskRequestEntry request = TesttoolRequestResource.getNextTaskRequest();
			if (request == null) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					continue;
				}
				continue;
			}
			try {
				executeTaskRequest(request);
			} catch (Exception e) {
				// Log Error
				logger.error("An error occured while handling TaSK Execution Request: " + e.getMessage(), e);
				continue;
			} finally {
				TesttoolRequestResource.popExecutedTaskRequest();
			}
		}

	}
	
	public void cancel() {
		this.canceled = true;
	}

	private void executeTaskRequest(final TaskRequestEntry requestEntry) {
		if (requestEntry.getTestRunplanFile() != null) {
			executeTrpMode(requestEntry);
		} else {
			executeMicsMode(requestEntry);
		}
	}

	private void executeMicsMode(final TaskRequestEntry requestEntry) {
		var reportLogger = new ReportLogger();
		LoggingConnector.addLogger(List.of(reportLogger));
		try {
			// Input Files
			var micsFile = requestEntry.getMicsFile();
			if (!micsFile.exists()) {
				throw new RuntimeException("Declared MICS file " + micsFile.getAbsolutePath() + " does not exist.");
			}

			// Ignore MICS verification
			//boolean ignoreMicsVerification = requestEntry.ignoreMicsVerification();

			// Certificate Files
			//List<File> certificateFileList = requestEntry.getServerCertificateChain();

			var reportDir = globalConfiguration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName())
					.getValueAsString();
			var reportDirectory = Paths.get(reportDir, requestEntry.getUuid().toString()).toString();

			var taskExecuteParameters = requestEntry.toTaskExecutionParameters(null, globalConfigFile, reportDirectory);
			TaskTestTool.executeTaskTestTool(taskExecuteParameters);


			reportGeneration(reportLogger, reportDirectory);
		} finally {
			LoggingConnector.removeLogger(reportLogger);
		}
	}

	private void executeTrpMode(final TaskRequestEntry requestEntry) {
		var reportLogger = new ReportLogger();
		LoggingConnector.addLogger(List.of(reportLogger));
		try {
			// Check if file exists.
			var testRunPlanFile = requestEntry.getTestRunplanFile();
			if (!testRunPlanFile.exists()) {
				throw new RuntimeException("Declared test run plan file " + testRunPlanFile.getAbsolutePath() +  " does not exist.");
			}
			try {
				TestRunPlanData testRunPlan = RunPlanParser.parseRunPlan(testRunPlanFile);
			} catch (Exception e) {
				throw new RuntimeException("An error occurred while trying to parse test run plan file.", e);
			}

			var reportDir = globalConfiguration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName())
					.getValueAsString();
			var reportDirectory = Paths.get(reportDir, requestEntry.getUuid().toString()).toString();

			var taskExecuteParameters = requestEntry.toTaskExecutionParameters(null, globalConfigFile, reportDirectory);
			TaskTestTool.executeTaskTestTool(taskExecuteParameters);

			reportGeneration(reportLogger, reportDirectory);

		} catch (Exception e) {
			throw e;
		} finally {
			LoggingConnector.removeLogger(reportLogger);
		}
	}

	private void reportGeneration(final ReportLogger reportLogger,
								  final String reportDirectory) {
		File xmlReportFileName;
		try {
			var xmlReport = XmlReport.fromReportInstance(reportLogger.generateReport());
			xmlReportFileName = new File(reportDirectory, "Report.xml");
			xmlReport.writeToFile(xmlReportFileName);
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while generating XML Report.", e);
		}
		try {
			if (globalConfiguration.containsKey(GlobalConfigParameterNames.PdfReportStylesheet.getParameterName())) {
				var styleSheetFile = new File(globalConfiguration.get(GlobalConfigParameterNames.PdfReportStylesheet.getParameterName()).getValueAsString());
				PdfReport.convertToPDF(xmlReportFileName, styleSheetFile);
			} else {
				PdfReport.convertToPDF(xmlReportFileName);
			}
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while generating PDF Report.", e);
		}
	}
	
}
