package com.achelos.task.restimpl.server;

import com.achelos.task.abstractinterface.TaskExecutionParameters;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.inputparsing.InputParser;
import com.achelos.task.xmlparser.runplanparsing.RunPlanParser;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TaskRequestEntry {

	private Path requestDir;
	private final UUID uuid;
	private File micsFile;
	private File testRunplanFile;
	private List<File> serverCertificateChain;
	private boolean ignoreMicsVerification;

	private File clientAuthCertChain;
	private File clientAuthKeyFile;

	protected TaskRequestEntry(final UUID uuid) {
		this.uuid = uuid;
	}
	
	public TaskRequestEntry(final UUID uuid,
							final File testRunplanFile,
							final List<InputStream> clientAuthCertChain,
							final File clientAuthKeyFile) throws IOException {
		this.uuid = uuid;
		if (testRunplanFile == null || !testRunplanFile.exists()) {
			throw new RuntimeException("TaSKRequestHandler: TRP file does not exist.");
		}
		// Verify TestRunplan
		var runPlan = RunPlanParser.parseRunPlan(testRunplanFile);
		if (!runPlan.getDUTApplicationType().toUpperCase().contains("TR-03124-1-EID-CLIENT-TLS")
				&& !runPlan.getDUTApplicationType().toUpperCase().contains("SERVER")) {
			throw new IllegalArgumentException("TestRunplan provided for invalid DUT Application Type.");
		}


		requestDir = Files.createTempDirectory(uuid.toString());
		this.testRunplanFile = Files.copy(testRunplanFile.toPath(), requestDir.resolve(testRunplanFile.getName())).toFile();
		if (clientAuthCertChain != null && !clientAuthCertChain.isEmpty()) {
			this.clientAuthCertChain = Files.createTempFile(null, ".pem").toFile();
			try (OutputStream out = Files.newOutputStream(this.clientAuthCertChain.toPath(), StandardOpenOption.WRITE)) {
				for (var certFile : clientAuthCertChain) {
					if (certFile != null) {
						certFile.transferTo(out);
					}
				}
			}
		}
		if (clientAuthKeyFile != null && clientAuthKeyFile.exists()) {
			this.clientAuthKeyFile = Files.copy(clientAuthKeyFile.toPath(), requestDir.resolve(clientAuthKeyFile.getName())).toFile();
		}

	}
	
	public TaskRequestEntry(final UUID uuid,
							final File micsFile,
							final List<InputStream> serverCertificateChain,
							final boolean ignoreMicsVerification,
							final List<InputStream> clientAuthCertChain,
							final File clientAuthKeyFile) throws IOException {
		this.uuid = uuid;
		requestDir = Files.createTempDirectory(uuid.toString());
		if (micsFile == null || !micsFile.exists()) {
			throw new RuntimeException("TaSKRequestHandler: MICS file does not exist.");
		}
		// Verify TestRunplan
		var mics = InputParser.parseMICS(micsFile);
		if (!mics.getApplicationType().toUpperCase().contains("TR-03124-1-EID-CLIENT-TLS")
				&& !mics.getApplicationType().toUpperCase().contains("SERVER")) {
			throw new IllegalArgumentException("MICS provided for invalid DUT Application Type.");
		}

		this.micsFile = Files.copy(micsFile.toPath(), requestDir.resolve(micsFile.getName())).toFile();
		if (serverCertificateChain != null && !serverCertificateChain.isEmpty()) {
			this.serverCertificateChain = new ArrayList<>();
			var cert_number = 0;
			for (var certFile : serverCertificateChain) {
				if (certFile != null) {
					var fileName = "server_cert_chain_" + Integer.toString(cert_number);
					Files.copy(certFile, requestDir.resolve(fileName));
					this.serverCertificateChain.add(requestDir.resolve(fileName).toFile());
					cert_number++;
				}
			}
		}
		this.ignoreMicsVerification = ignoreMicsVerification;
		if (clientAuthCertChain != null && !clientAuthCertChain.isEmpty()) {
			this.clientAuthCertChain = Files.createTempFile(null, ".pem").toFile();
			try (OutputStream out = Files.newOutputStream(this.clientAuthCertChain.toPath(), StandardOpenOption.WRITE)) {
				for (var certFile : clientAuthCertChain) {
					if (certFile != null) {
						certFile.transferTo(out);
					}
				}
			}
		}
		if (clientAuthKeyFile != null && clientAuthKeyFile.exists()) {
			this.clientAuthKeyFile = Files.copy(clientAuthKeyFile.toPath(), requestDir.resolve(clientAuthKeyFile.getName())).toFile();
		}
	}
	
	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}
	/**
	 * @return the micsFile
	 */
	public File getMicsFile() {
		return micsFile;
	}
	/**
	 * @return the certificateFileList
	 */
	public List<File> getServerCertificateChain() {
		return new ArrayList<>(serverCertificateChain);
	}
	/**
	 * @return the ignoreMicsVerification
	 */
	public boolean ignoreMicsVerification() {
		return ignoreMicsVerification;
	}
	/**
	 * @return the testRunplanFile
	 */
	public File getTestRunplanFile() {
		return testRunplanFile;
	}


	/**
	 * @return the clientAuth Certificate Chain
	 */
	public File getClientAuthCertChain() {
		return clientAuthCertChain;
	}

	/**
	 *
	 * @return the clientAuth key file
	 */
	public File getClientAuthKeyFile() {
		return clientAuthKeyFile;
	}

	/**
	 * Return a TaSKExecutionParameter object containing the data of this TaSKRequestEntry.
	 * @param logger The logger which shall be used when executing task.
	 * @param configFile The configurationFile which shall be used when executing task.
	 * @param reportDirectory The Report Directory which shall be used when executing task.
	 * @return A TaSKExecutionParameter object containing the data of this TaSKRequestEntry.
	 */
	public TaskExecutionParameters toTaskExecutionParameters(final LoggingConnector logger, final File configFile, final String reportDirectory) {
		var clientAuthCertChain = this.clientAuthCertChain == null ? null : this.clientAuthCertChain.getAbsolutePath();
		var clientAuthKeyFile = this.clientAuthKeyFile == null ? null : this.clientAuthKeyFile.getAbsolutePath();

		if (testRunplanFile != null) {
			return new TaskExecutionParameters(logger,
				testRunplanFile,
				configFile,
				reportDirectory,
				clientAuthCertChain,
				clientAuthKeyFile);
		} else {
			return new TaskExecutionParameters(logger,
					configFile,
					micsFile,
					serverCertificateChain,
					ignoreMicsVerification,
					reportDirectory,
					clientAuthCertChain,
					clientAuthKeyFile);

		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TaskRequestEntry requestEntry = (TaskRequestEntry) o;
		return Objects.equals(this.uuid, requestEntry.getUuid());
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid);
	}
}
