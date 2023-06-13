package com.achelos.task.commandlineexecution.applications.ocsp;

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
 * A class to handle OCSP request.
 */
public class OCSPServerExecutor extends RunLogger {

	/**
	 * Default constructor.
	 * 
	 * @param testCaseName the test case name
	 * @param logger the logger to use
	 */
	public OCSPServerExecutor(final String testCaseName, final LoggingConnector logger) throws IOException {
		super(testCaseName, Executor.OCSPSERVER, logger);
		CertGeneratorExecutor.generateOcspCrlCertificates(logger, getConfiguration());
	}

	/**
	 * Starts the OCSP executor on the port of the OCSP responder as specified in the global configuration file.
	 * 
	 * @param cipherSuite the cipher suite to get CRL/OCSP directory with matching key type.
	 * @throws Exception may throws an exception if OCSP executor is unable to start.
	 */
	public void start(final TlsCipherSuite cipherSuite)
			throws Exception {

		final List<String> command = new ArrayList<>();

		String path = getConfiguration().getCrlOcspCertDirectoryWithMatchingKeyType(cipherSuite).getAbsolutePath();
		var port = getConfiguration().getOcspResponderPort();
		var opensslExecutable = getConfiguration().getOpenSSLExecutable();

		command.add(opensslExecutable);
		command.add("ocsp");
		command.add("-port");
		command.add(Integer.toString(port));
		command.add("-index");
		command.add(path + "/root-ca/db/index");
		command.add("-rsigner");
		command.add(path + "/ocsp-certificate/certs/ocsp-signing.pem");
		command.add("-rkey");
		command.add(path + "/ocsp-certificate/private/ocsp-signing.pem");
		command.add("-CA");
		command.add(path + "/root-ca/certs/root-ca.pem");
		command.add("-text");

		start(command, null, null);
	}

	/**
	 * Starts the COSP executor with provided command line arguments.
	 * 
	 * @param commands the cipher suite to get CRL/OCSP directory with matching key type.
	 * @throws Exception may throws an exception if OCSP executor is unable to start.
	 */
	public void start(final String commands)
			throws Exception {

		final List<String> command = new ArrayList<>(Arrays.asList(commands.split(" ")));


		start(command, null, null);
	}

	/**
	 * Method stops the OCSP Executor.
	 */
	@Override
	public final void stop() {
		stopLogQueueProducer();
		final int delay = 1000;
		startSleepTimer(delay);
		destroy();
		while (isRunning()) {
			logDebug("OCSP process is still alive.");
			startSleepTimer(delay);
		}
		super.removeShutdownHook();
	}

	@Override
	protected void logEndOfIteration(final Writer writer) throws IOException {
		// Do nothing

	}

}
