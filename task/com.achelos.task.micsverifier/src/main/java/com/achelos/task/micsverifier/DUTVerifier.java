package com.achelos.task.micsverifier;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.bouncycastle.util.IPAddress;

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

		var dUTApplicationType = mics.getApplicationType();

		if (dUTApplicationType.contains("CLIENT")) {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "DUT is of Client type: " + mics.getApplicationType());
			// Verify Client DUT
			result = verifyClientDUT(mics, logger);
		} else if (dUTApplicationType.contains("SERVER")) {
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "DUT is of TLS Server type: " + mics.getApplicationType());
			// Verify Server DUT
			result = verifyServerDUT(mics, logger);
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
			boolean isValidUrl = checkServerURL(mics.getServerURL());
			if(!isValidUrl) {
				logger.error(
						MICSVerifier.LOGGER_COMPONENT + "DUT has a server type, but the provided server URL could not be verified.");
				return false;
			}
			
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
	
	private static boolean checkServerURL(String serverURL) {

			if (serverURL == null) {
				return false;
			} 
			
			if (serverURL.isBlank() || IPAddress.isValid(serverURL)) {
				return true;
			}
			
			if (!serverURL.startsWith("https://") && !serverURL.startsWith("http://")) {
				serverURL = "http://" + serverURL;
			}
			try {
				new URL(serverURL).toURI();
				return true;
			} catch (MalformedURLException | URISyntaxException e) {
				return false;
			}

		
	}

	private static boolean verifyClientDUT(final MICS mics, final LoggingConnector logger) {
		// Check Client DUT RMI Hostname
		if ((mics.getDutRMIURL() == null || mics.getDutRMIURL().isBlank())) {
			logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT has a Client type, but no DUT Client RMI Hostname is provided in the MICS file.");
			return false;
		}

		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Verifying RMI Hostname: " + mics.getDutRMIURL() + ".");
		try {
			// Check if that throws any exceptions.
			checkServerURL(mics.getDutRMIURL());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Parsed RMI Hostname " + mics.getDutRMIURL() + "successfully verified.");
		} catch (Exception e) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a Client type, but the provided DUT Client RMI Hostname could not be verified.",
					e);
			return false;
		}
		// Check Client DUT RMI Port
		if (mics.getDutRMIPort() == null || mics.getDutRMIPort().isBlank()) {
			logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT has a Client type, but no DUT Client RMI port is provided in the MICS file.");
			return false;
		}
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Verifying DUT Client RMI port: " + mics.getDutRMIPort() + ".");
		try {
			var port = Integer.parseInt(mics.getDutRMIPort());
			if (port < 0 || port > MAX_PORT_NUMBER) {
				logger.error(MICSVerifier.LOGGER_COMPONENT + "DUT has a Client type, but no DUT Client RMI port could not be verified.");
				return false;
			}
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Parsed DUT Client RMI port: " + mics.getDutRMIPort() + "successfully verified.");
		} catch (Exception e) {
			logger.error(
					MICSVerifier.LOGGER_COMPONENT + "DUT has a Client type, but the provided DUT Client RMI port could not be parsed to a number.",
					e);
			return false;
		}
		return true;
	}

}
