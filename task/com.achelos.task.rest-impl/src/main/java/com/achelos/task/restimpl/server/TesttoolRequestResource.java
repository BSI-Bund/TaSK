package com.achelos.task.restimpl.server;

import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameterNames;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TesttoolRequestResource {

	private static volatile String reportDir;
	/**
	 * Enum representing different Execution states.
	 */
	public static enum ExecutionStatus {
		/**
		 * Execution state: Scheduled
		 */
		SCHEDULED,
		/**
		 * Execution state: Currently Running
		 */
		RUNNING,
		/**
		 * Execution state: Aborted
		 */
		ABORTED,
		/**
		 * Execution state: Finished
		 */
		FINISHED,
		/**
		 * Execution state: Unknown
		 */
		UNKNOWN;
	}

	protected static synchronized void initReportDir(File globalConfigFile) {
		HashMap<String, GlobalConfigParameter> globalConfiguration;
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
		TesttoolRequestResource.reportDir = globalConfiguration.get(GlobalConfigParameterNames.ReportDirectory.getParameterName())
				.getValueAsString();
	}

	static final private Queue<TaskRequestEntry> queue = new ConcurrentLinkedQueue<>();
	static final private int QUEUE_MAX_SIZE = 5;


	/**
	 * Queue the executionRequest for the TaSK Test Runner to execute it.
	 * @param executionRequest the execution request to schedule.
	 * @return Returns true if the execution request has been scheduled, and false if the queue is full.
	 */
	public static synchronized boolean queueExecution(TaskRequestEntry executionRequest) {
		if (queue.size() >= QUEUE_MAX_SIZE) {
			return false;
		} else {
			return queue.add(executionRequest);
		}
	}

	/**
	 * Retrieve the execution state of the executionRequest with the specified UUID.
	 * @param uuid the UUID of the executionRequest to get the execution state for.
	 * @return the execution state of the executionRequest with the specified UUID.
	 */
	public static synchronized ExecutionStatus getStatus(final UUID uuid) {
		var nextInQueue = queue.peek();
		if (nextInQueue != null) {
			if (nextInQueue.equals(new TaskRequestEntry(uuid))) {
				return ExecutionStatus.RUNNING;
			} else {
				if (queue.contains(new TaskRequestEntry(uuid))) {
					return ExecutionStatus.SCHEDULED;
				}
			}
		}

		// if in results folder: return ExecutionStatus.FINISHED
		var reportDirectory = getResultPath(uuid);
		if (reportDirectory.exists() && reportDirectory.isDirectory()) {
			var reportFile = new File(reportDirectory, "Report.xml");
			if (reportFile.exists()) {
				return ExecutionStatus.FINISHED;
			} else {
				return ExecutionStatus.ABORTED;
			}
		} else {
			return ExecutionStatus.UNKNOWN;
		}
	}

	public static synchronized TaskRequestEntry getNextTaskRequest() {
		return queue.peek();
	}

	public static synchronized TaskRequestEntry popExecutedTaskRequest() {
		return queue.poll();
	}

	/**
	 * Retrieve the result path of the executionRequest with the specified UUID.
	 * @param uuid the UUID of the executionRequest to get the result path for.
	 * @return the result path of the executionRequest with the specified UUID.
	 */
	public static synchronized String getResultPathString(final UUID uuid) {
		var reportDirectory = getResultPath(uuid);
		if (reportDirectory.exists() && reportDirectory.isDirectory()) {
			return reportDirectory.getAbsolutePath();
		} else {
			return "";
		}
	}

	private static synchronized File getResultPath(final UUID uuid) {
		return Paths.get(reportDir, uuid.toString()).toFile();
	}

}
