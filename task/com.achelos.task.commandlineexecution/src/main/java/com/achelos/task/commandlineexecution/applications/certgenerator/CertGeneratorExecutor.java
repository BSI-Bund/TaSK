package com.achelos.task.commandlineexecution.applications.certgenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.logging.LoggingConnector;


/**
 * A class to generates CRL/OCSP certificates on runtime.
 */
public class CertGeneratorExecutor {
	private static final String SCRIPT_RESOURCE_PATH = "generate_crl_ocsp_certificates.sh";
	private static final String CONFIGURATION_TEMPLATE_RESOURCE_PATH = "certificate_template.cnf";
	private final Path script;
	private final Path confTemplate;
	private final LoggingConnector logger;
	private final TestRunPlanConfiguration configuration;
	private Process process;

	/**
	 * Default constructor that prepares for generating the OCSP/CRL certificates on runtime.
	 * It reads the certificate generator script file and certificate template file from resources.
	 * 
	 * @param logger the {@link LoggingConnector} instance to use for logging.
	 * @param configuration the global configuration file.
	 * @throws IOException may throw an exception if unable to create temporary files or script file does not exist. 
	 */
	private CertGeneratorExecutor(final LoggingConnector logger,
			final TestRunPlanConfiguration configuration) throws IOException {
		this.logger = logger;
		this.configuration = configuration;
		script = Files.createTempFile(null, ".sh");
		confTemplate = Files.createTempFile(null, ".cnf",
				PosixFilePermissions.asFileAttribute(
						PosixFilePermissions.fromString("rw-rw-r--")));
		try (InputStream embeddedConfTemplate
				= this.getClass().getClassLoader().getResourceAsStream(CONFIGURATION_TEMPLATE_RESOURCE_PATH)) {
			Files.copy(embeddedConfTemplate, confTemplate, StandardCopyOption.REPLACE_EXISTING);
		}
		try (InputStream embeddedScript = this.getClass().getClassLoader().getResourceAsStream(SCRIPT_RESOURCE_PATH)) {
			Files.copy(embeddedScript, script, StandardCopyOption.REPLACE_EXISTING);
		}
		Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr--"));
	}

	/**
	 * Generates the certificate for OCSP/CRL request.
	 * This method uses the OCSP responder port, CRL responder port, OpenSSL executable path and 
	 * CLR OCSP certificate directory from global configuration file to execute the script.
	 *  
	 * 
	 * @throws IOException throws exception if fails to execute the script on runtime.
	 */
	private void execute()
			throws IOException {

		final ArrayList<String> command = new ArrayList<>();
		// Script File Path
		command.add(script.toAbsolutePath().toString());

		// Certificate base path
		var cert_basepath = configuration.getCrlOcspCertDirectory();
		command.add(cert_basepath.getAbsolutePath());

		// Template
		var templatePath = confTemplate.toAbsolutePath().toString();
		command.add(templatePath);

		// CA base path
		var ca_basepath = configuration.getTlsTestToolCertificatesPath();
		command.add(ca_basepath);

		// OCSP Port
		var ocsp_port = configuration.getOcspResponderPort();
		command.add(Integer.toString(ocsp_port));

		// CRL Port
		var crl_port = configuration.getCrlResponderPort();
		command.add(Integer.toString(crl_port));

		// OpenSSL Executable Path
		var opensslExecPath = configuration.getOpenSSLExecutable();
		command.add(opensslExecPath);

		// Execute
		process = Runtime.getRuntime().exec(command.toArray(new String[0]));
	}

	/**
	 * Prints the execution results.
	 */
	private void printResults() {
		if (process == null) {
			return;
		}
		try (BufferedReader reader
				= new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				logger.debug("CertGeneratorExecutor: " + line);
			}
		} catch (Exception err) {
			logger.debug("CertGeneratorExecutor: Error when generating certificates: " + err.getMessage());
		}
	}

	/**
	 * It reads the certificate generator script file and certificate template file from resources.
	 * Generates the certificate for OCSP/CRL request.
	 * This method uses the OCSP responder port, CRL responder port, OpenSSL executable path and
	 * CLR OCSP certificate directory from global configuration file to execute the script.
	 * Prints the execution results into debug output.
	 *
	 * @param logger the {@link LoggingConnector} instance to use for logging.
	 * @param configuration the global configuration file.
	 * @throws Exception if fails to execute the script on runtime.
	 * 	May throw an exception if unable to create temporary files or script file does not exist.
	 */
	public static void generateOcspCrlCertificates(final LoggingConnector logger,
													final TestRunPlanConfiguration configuration) throws IOException {
		var crlOcspCertDir = configuration.getCrlOcspCertDirectory();
		if (null == crlOcspCertDir) {
			throw new IOException("Parameter 'CrlOcspCertDirectory' is empty. Check configuration!");
		}
		if (isDirAndNotEmpty(crlOcspCertDir)) {
			logger.debug("CertGeneratorExecutor: Generation skipped since certificates were generated previously.");
		} else {
			var certGenerator = new CertGeneratorExecutor(logger, configuration);
			certGenerator.execute();
			certGenerator.printResults();
		}

	}
	
	/**
	 * Workaround for spotbugs.
	 * 
	 * @param dir
	 * @return true, if dir exists, is a directory and contains at least one file. 
	 * 			Otherwise false
	 */
	private static boolean isDirAndNotEmpty(File dir) {

		if (! (null != dir &&
				dir.exists() && 
				dir.isDirectory())) 
			return false;
		
		String[] files = dir.list();
		if (null == files || 
				files.length == 0)
			return false;
		
		return true;
	}


}
