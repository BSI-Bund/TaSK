package com.achelos.task.restimpl.server;

import com.achelos.task.abstractinterface.TaskExecutionParameters;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.datastructures.testrunplan.TestRunPlanData;
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
	private boolean onlyGenerateTRP;

	private File clientAuthCertChain;
	private File clientAuthKeyFile;
	private File certificateValidationRootCA;

	protected TaskRequestEntry(final UUID uuid) {
		this.uuid = uuid;
	}
	
	public TaskRequestEntry(final UUID uuid,
							final File testRunplanFile,
							final List<InputStream> clientAuthCertChain,
							final File clientAuthKeyFile,
							final File certificateValidationRootCA) throws IOException {
		this.uuid = uuid;
		
		// basic plausibility checks
		if (testRunplanFile == null || !testRunplanFile.exists()) {
			throw new RuntimeException("TaSKRequestHandler: TRP file does not exist.");
		}
		
		TestRunPlanData runPlanData = RunPlanParser.parseRunPlan(testRunplanFile);
		// do not allow manual DUT execution
		if (runPlanData.getDutRMIURL().isEmpty()) {
			throw new RuntimeException("TaSKRequestHandler: RMI URL is not defined in TestRunPlan.");
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
		if (certificateValidationRootCA != null && certificateValidationRootCA.exists()) {
			this.certificateValidationRootCA = Files.copy(certificateValidationRootCA.toPath(), requestDir.resolve(certificateValidationRootCA.getName())).toFile();
		}

	}
	
	public TaskRequestEntry(final UUID uuid,
							final File micsFile,
							final List<InputStream> serverCertificateChain,
							final boolean ignoreMicsVerification, 
							final boolean onlyGenerateTRP,
							final List<InputStream> clientAuthCertChain,
							final File clientAuthKeyFile,
							final File certificateValidationRootCA) throws IOException {
		this.uuid = uuid;
		requestDir = Files.createTempDirectory(uuid.toString());
		
		// basic plausibility checks
		if (micsFile == null || !micsFile.exists()) {
			throw new RuntimeException("TaSKRequestHandler: MICS file does not exist.");
		}
		
		MICS micsData = InputParser.parseMICS(micsFile);
		// do not allow manual DUT execution
		if (micsData.getDutRMIURL().isEmpty()) {
			throw new RuntimeException("TaSKRequestHandler: RMI URL is not defined in MICS.");
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
		this.onlyGenerateTRP = onlyGenerateTRP;
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

		if (certificateValidationRootCA != null && certificateValidationRootCA.exists()) {
			this.certificateValidationRootCA = Files.copy(certificateValidationRootCA.toPath(), requestDir.resolve(certificateValidationRootCA.getName())).toFile();
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
	 * @return true, if execution shall be skipped
	 */
	public boolean onlyGenerateTRP() {
		return onlyGenerateTRP;
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
		var certificateValidationRootCA = this.certificateValidationRootCA == null ? null : this.certificateValidationRootCA.getAbsolutePath();

		if (testRunplanFile != null) {
			return new TaskExecutionParameters(logger,
					testRunplanFile,
					configFile,
					reportDirectory,
					clientAuthCertChain,
					clientAuthKeyFile,
					certificateValidationRootCA);
		} else {
			return new TaskExecutionParameters(logger,
					configFile,
					micsFile,
					serverCertificateChain,
					ignoreMicsVerification,
					onlyGenerateTRP,
					reportDirectory,
					clientAuthCertChain,
					clientAuthKeyFile,
					certificateValidationRootCA);

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
