package micsverifier;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.micsverifier.MICSVerifier;

class TestMICSVerifier {

	@Test
	void testMICSVerifier() {
		try {

			// Input Files
			var micsFile = new File("../data/input/ExampleMICS_Server.xml");
			var certDirectory = new File("../data/input/certificates");
			ArrayList<File> certificateFileList = new ArrayList<File>();
			if (!certDirectory.exists() || !certDirectory.isDirectory()) {
				fail();
			}
			for (var file : certDirectory.listFiles()) {
				if (file.isFile()) {
					certificateFileList.add(file);
				}
			}
			var micsVerifier = prepareMICSVerifier();
			var result = micsVerifier.verifyMICS(micsFile, certificateFileList.toArray(File[]::new));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void testMICSVerifierTlsServer() {
		try {

			// Input Files
			var micsFile = new File("../data/input/ExampleMICS_Server.xml");
			var certDirectory = new File("../data/input/certificates");
			ArrayList<File> certificateFileList = new ArrayList<File>();
			if (!certDirectory.exists() || !certDirectory.isDirectory()) {
				fail();
			}
			for (var file : certDirectory.listFiles()) {
				if (file.isFile()) {
					certificateFileList.add(file);
				}
			}
			var micsVerifier = prepareMICSVerifier();
			var result = micsVerifier.verifyMICS(micsFile, certificateFileList.toArray(File[]::new));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void testMICSVerifierTlsClient() {
		try {

			// Input Files
			var micsFile = new File("../data/input/ExampleMICS_Client.xml");

			var micsVerifier = prepareMICSVerifier();
			var result = micsVerifier.verifyMICS(micsFile);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private MICSVerifier prepareMICSVerifier() throws IOException {
		LoggingConnector.getInstance("DEBUG");
		// Config Files
		var serverSpec = new File(
				"../data/specification/ApplicationSpecifications/TR-03116-4-SERVER-Specification.xml");
		var clientSpec = new File(
				"../data/specification/ApplicationSpecifications/TR-03116-4-CLIENT-Specification.xml");
		var appMapping = new File("../data/specification/ApplicationSpecificProfiles/TR-03116-4-AM-TLS-SERVER.xml");
		var appMappingClient = new File("../data/specification/ApplicationSpecificProfiles/TR-03116-4-AM-TLS-CLIENT.xml");
		var testCasesDir = new File("../data/specification/TestCases");
		var testProfiles = new File("../data/specification/TestProfiles.xml");
		var exampleGlobalConfig = new File("../data/configuration/ExampleGlobalConfig.xml");
		var tlsConfigData = new File("../data/specification/TlsConfigurationData.xml");
		var specificationDirectory = new File("../data/specification");

		// Global Config File
		Path genGlobalConfig = Files.createTempFile(null, ".xml");
		var globalConfigContent = Files.readString(exampleGlobalConfig.toPath());
		// Replace empty tls_test_tool_path with dummy value.
		globalConfigContent = Pattern.compile("<parameter id=\"tls_test_tool_path.*?parameter>",
				Pattern.DOTALL | Pattern.MULTILINE).matcher(
				globalConfigContent).replaceFirst(
				"<parameter id=\"tls_test_tool_path\"><string>/path/to/tool</string></parameter>");
		// Replace empty report dir
		var tmpReportDir = Files.createTempDirectory(null);
		globalConfigContent = Pattern.compile("<parameter id=\"report_directory.*?parameter>",
				Pattern.DOTALL | Pattern.MULTILINE).matcher(
				globalConfigContent).replaceFirst(
				"<parameter id=\"report_directory\"><string>" + tmpReportDir.toAbsolutePath() + "</string></parameter>");
		// Replace empty specification directory
		globalConfigContent = Pattern.compile("<parameter id=\"specification_directory.*?parameter>",
				Pattern.DOTALL | Pattern.MULTILINE).matcher(
				globalConfigContent).replaceFirst(
				"<parameter id=\"specification_directory\"><string>" + specificationDirectory.getAbsolutePath() + "</string></parameter>");

		Files.writeString(genGlobalConfig, globalConfigContent);


		// Initialize MICSVerifier with these files.
		return new MICSVerifier(Arrays.asList(serverSpec, clientSpec), Arrays.asList(appMapping, appMappingClient), testCasesDir,
				testProfiles, genGlobalConfig.toFile(), tlsConfigData);

	}

}
