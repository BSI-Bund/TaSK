package com.achelos.task.micsverifier;

import java.io.File;
import java.net.InetAddress;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.mics.MICS;


class DUTVerifier {

	private static final Integer MAX_PORT_NUMBER = 65535; // 2^16 - 1

	/**
	 * Hidden Constructor.
	 */
	private DUTVerifier() {
		// Empty.
	}

	/**
	 * Verifies the DUT from the MICS file.
	 *
	 * @param mics {@link MICS} for the Device Under Test to verify.
	 * @return true if successfully verified. false otherwise.
	 */
	public static boolean verifyDUTFromMics(final MICS mics) {

		var logger = LoggingConnector.getInstance();
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running DUT verifier on the provided MICS file.");
		boolean result = false;

		if (mics.getApplicationType().contains("SERVER")) {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "DUT is of type: " + mics.getApplicationType());
			// Verify Server DUT
			result = verifyServerDUT(mics, logger);
		} else if (mics.getApplicationType().contains("CLIENT")) {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "DUT is of type: " + mics.getApplicationType());
			// Verify Client DUT
			result = verifyClientDUT(mics, logger);
		} else {
			logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT is of unknown type: " + mics.getApplicationType());
			result = false;
		}
		if (result) {
			logger.info(MICSVerifier.LOGGER_COMPONENT + "Successfully executed the DUT verifier on the provided MICS file.");
		} else {
			logger.info(MICSVerifier.LOGGER_COMPONENT + "Device Under Test provided in the MICS file could not be verified.");
		}
		return result;
	}

	private static boolean verifyServerDUT(final MICS mics, final LoggingConnector logger) {
		// Check Server URL
		if (mics.getServerURL() == null || mics.getServerURL().isBlank()) {
			logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT has a server type, but no server URL is provided in the MICS file.");
			return false;
		}
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Verifying server URL: " + mics.getServerURL() + ".");
		try {
			// Check if that throws any exceptions.
			InetAddress.getByName(mics.getServerURL());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Parsed server URL: " + mics.getServerURL() + "successfully verified.");
		} catch (Exception e) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a server type, but the provided server URL could not be verified.",
					e);
			return false;
		}
		// Check Server Port
		if (mics.getServerPort() == null || mics.getServerPort().isBlank()) {
			logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT has a server type, but no server port is provided in the MICS file.");
			return false;
		}
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Verifying server port: " + mics.getServerPort() + ".");
		try {
			var port = Integer.parseInt(mics.getServerPort());
			if (port < 0 || port > MAX_PORT_NUMBER) {
				logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT has a server type, but no server port could not be verified.");
				return false;
			}
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Parsed server port: " + mics.getServerPort() + "successfully verified.");
		} catch (Exception e) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a server type, but the provided server port could not be parsed to a number.",
					e);
			return false;
		}
		return true;
	}

	private static boolean verifyClientDUT(final MICS mics, final LoggingConnector logger) {
		// Check Client DUT Executable
		if (mics.getDutExecutable() == null || mics.getDutExecutable().isBlank()) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a client type, but no executable file for the DUT is provided in the MICS file.");
			return false;
		}
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Verifying TLS client executable file: " + mics.getDutExecutable());
		try {
			var executableFile = new File(mics.getDutExecutable());
			if (executableFile.length() == 0) {
				logger.error(
						MICSVerifier.LOGGER_COMPONENT + "DUT has a Client type but the provided executable file "
								+ executableFile.getAbsolutePath() + " does not exist or is empty.");
				return false;
			}
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "The provided executable file for the DUT exists and is not empty.");

			if (!executableFile.canRead()) {
				logger.error(
						MICSVerifier.LOGGER_COMPONENT + "DUT has a client type but the provided executable file is not readable.");
				return false;
			}
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "The provided executable file for the DUT is readable.");
			if (!executableFile.canExecute()) {
				logger.warning(
						MICSVerifier.LOGGER_COMPONENT + "DUT has a client type but the provided executable file does not have execute permissions. Trying to set execute permissions on the file.");
				try {
					var settingExecResult = executableFile.setExecutable(true, false);
					if (!settingExecResult) {
						logger.error(
								MICSVerifier.LOGGER_COMPONENT + "Unable to set execute permissions on provided DUT executable file.");
						return false;
					}
					logger.debug(MICSVerifier.LOGGER_COMPONENT + "Successfully set execute permissions on provided DUT executable file.");
				} catch (Exception e) {
					logger.error(MICSVerifier.LOGGER_COMPONENT + "Unable to set execute permissions on provided DUT executable file.", e);
					return false;
				}

			} else {
				logger.debug(MICSVerifier.LOGGER_COMPONENT + "The provided executable file has execute permissions.");
			}
		} catch (Exception e) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a client type but the provided executable file could not be verified.",
					e);
			return false;
		}
		// Check Dut Executable Arguments
		if (mics.getDutCallArgumentsConnect() == null) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a client type, but the DUT executable arguments provided in the MICS file are null.");
			return false;
		}
		return true;
	}
}
