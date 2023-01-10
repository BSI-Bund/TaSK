package com.achelos.task.commandlineexecution.applications.tshark.helper;

import java.io.File;

import com.achelos.task.commandlineexecution.applications.tshark.exception.TSharkException;
import com.achelos.task.configuration.TestRunPlanConfiguration;


/**
 * Access to global parameters specific to the usage of TShark.
 */
public class TSharkParameters {
	private final TestRunPlanConfiguration configuration;

	/**
	 * Create a new instance.
	 *
	 */
	public TSharkParameters() {
		this.configuration = TestRunPlanConfiguration.getInstance();
	}


	/**
	 * Check the global parameters to determine if TShark should be used to create a network traffic dump.
	 *
	 * @return {@code true}, if a network traffic dump should be created. {@code false}, if not.
	 */
	public final boolean isTSharkEnabled() {
		return configuration.isTsharkEnabled();
	}


	/**
	 * Get the capture interface to use with TShark.
	 *
	 * @return Capture interface (e.g., "2", "eth0")
	 */
	public final String getTSharkInterface() {
		return configuration.getTsharkInterface();
	}


	/**
	 * Get additional options to TShark.
	 *
	 * @return TShark options (e.g., "-P -t ad")
	 */
	public final String getTSharkOptions() {
		return configuration.getTsharkOptions();
	}


	/**
	 * Get the path to the TShark executable.
	 *
	 * @return TShark executable path
	 * @throws TSharkException if the configured path is invalid
	 */
	public final File getTSharkExecutableFile() throws TSharkException {
		String tsharkExecutable = configuration.getTsharkPath();
		if (null == tsharkExecutable || tsharkExecutable.isBlank()) {
			throw new TSharkException("Path to tshark executable is missing in configuration.");
		}

		final File tsharkExecutableFile = new File(configuration.getTsharkPath());
		return tsharkExecutableFile;
	}

}
