package com.achelos.task.commandlineexecution.applications.ocsp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.achelos.task.commandlineexecution.applications.certgenerator.CertGeneratorExecutor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.GenericCommandLineExecution;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.LoggingConnector;


/**
 * A class to handles the OCSP request.
 */
public class OCSPRequestExecutor extends GenericCommandLineExecution {

	/**
	 * Default constructor.
	 * 
	 * @param testCaseName the test case name
	 * @param logger the logger to use
	 */
	public OCSPRequestExecutor(final String testCaseName, final LoggingConnector logger)	throws IOException,
																					URISyntaxException {
		super(Executor.OCSPREQUEST, testCaseName, logger);
		CertGeneratorExecutor.generateOcspCrlCertificates(logger, getConfiguration());
	}

	/**
	 * Starts the OCSP request executor on the port of the OCSP responder as specified in the global configuration file.
	 * 
	 * @param cipherSuite the cipher suite to get CRL/OCSP directory with matching key type.
	 * @param certType the certificate type to select. 
	 * @throws Exception may throws an exception if OCSP request executor is unable to start.
	 */
	public final void start(final TlsCipherSuite cipherSuite, final TlsTestToolCertificateTypes certType)
			throws Exception {
		final List<String> command = new ArrayList<>();

		String path = getConfiguration().getCrlOcspCertDirectoryWithMatchingKeyType(cipherSuite).getAbsolutePath();
		var port = getConfiguration().getOcspResponderPort();
		var opensslExecutable = getConfiguration().getOpenSSLExecutable();

		command.add(opensslExecutable);
		command.add("ocsp");

		command.add("-CAfile");
		command.add(path + "/root-ca/certs/root-ca.pem");

		command.add("-issuer");
		command.add(path + "/root-ca/certs/root-ca.pem");

		command.add("-cert");
		command.add(getConfiguration().getCrlOcspCertificate(cipherSuite, certType)[0]);

		command.add("-url");
		command.add("http://127.0.0.1:" + port);

		command.add("-resp_text");

		command.add("-noverify");

		command.add("-respout");


		File file = createLogFile();
		command.add(file.getCanonicalPath());


		final File logFile
				= new File(Paths.get(getConfiguration().getReportDirectory().getAbsolutePath(), getTestCaseName(),
						getTestCaseName() + "_ocsp_request" + ".log").toString());
		var mkdirResult = logFile.getParentFile().mkdirs();
		if (!mkdirResult) {
			logDebug("Unable to create log file directory: " + logFile);
		}
		
		start(command, logFile, null);

	}

	@Override
	public final void stop() {
		super.stop();
		destroy();
	}

	/**
	 * Stops the OCSP request process after 60 seconds if not stopped already. De-registers a previously-registered
	 * virtual-machine shutdown hook.
	 * 
	 * @see #stop()
	 */
	public final void cleanAndExit() {
		final int sixty = 60;
		final int thousand = 1000;
		try {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					logError("Forcibly stopping " + getExecutor().getName() + " after 60 seconds in cleanAndExit.");
					destroy();
					removeShutdownHook();
				}
			}, sixty * thousand);
			logFileCreated();
			timer.cancel();
		} catch (Exception e) {
			logFileCreated();
			logError("An error occurred while trying to process the logging dump", e);
		}
	}

	/**
	 * @return the output file.
	 */
	public final File getOutputFile() {
		return getLogFile();
	}

}
