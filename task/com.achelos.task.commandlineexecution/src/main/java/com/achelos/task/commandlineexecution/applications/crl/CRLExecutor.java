package com.achelos.task.commandlineexecution.applications.crl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.commandlineexecution.applications.certgenerator.CertGeneratorExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.RunLogger;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.logging.LoggingConnector;

/**
 * A class to start CRL server to perform CRL request.
 */
public class CRLExecutor extends RunLogger {

	/**
	 * Default constructor.
	 * 
	 * @param testCaseName the test case name
	 * @param logger the logger to use
	 */
	public CRLExecutor(final String testCaseName, final LoggingConnector logger)	throws IOException,
																					URISyntaxException {
		super(testCaseName, Executor.CRL, logger);
		CertGeneratorExecutor.generateOcspCrlCertificates(logger, getConfiguration());
	}

	/**
	 * Starts the CRL server on the port of the CRL responder as specified in the global configuration file.
	 * 
	 * @param cipherSuite the cipher suite to get CRL/OCSP directory with matching key type.
	 * @throws Exception may throws an exception if CRL server is unable to start.
	 */
	public void start(final TlsCipherSuite cipherSuite)
			throws Exception {

		final List<String> command = new ArrayList<>();

		String path = getConfiguration().getCrlOcspCertDirectoryWithMatchingKeyType(cipherSuite).getAbsolutePath();
		path += "/root-ca/crl/";
		var port = getConfiguration().getCrlResponderPort();

		// command.add("cd " + path);
		command.add("python3");
		command.add("-u");
		command.add("-m");
		command.add("http.server");
		command.add(Integer.toString(port));


		start(command, null, new File(path));
	}

	/**
	 * Starts the CRL server with provided command line arguments.
	 * 
	 * @param commands the cipher suite to get CRL/OCSP directory with matching key type.
	 * @throws Exception may throws an exception if CRL executor is unable to start.
	 */
	public void start(final String commands)
			throws Exception {

		final List<String> command = new ArrayList<>(Arrays.asList(commands.split(" ")));


		start(command, null, null);
	}

	/**
	 * Method stops the CRL Executor.
	 */
	@Override
	public final void stop() {
		final int delay = 1000;
		startSleepTimer(delay);
		destroy();
		while (isRunning()) {
			logDebug("CRL process is still alive.");
			startSleepTimer(delay);
		}
		super.removeShutdownHook();
	}

	@Override
	public void logEndOfIteration(final Writer writer) throws IOException {
		// Do nothing.

	}


}
