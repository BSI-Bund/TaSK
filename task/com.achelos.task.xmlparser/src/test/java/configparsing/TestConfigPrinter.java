package configparsing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import com.achelos.task.logging.LoggingConnector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.configparsing.ConfigPrinter;

public class TestConfigPrinter {

	@BeforeAll
	static void initLogging() {
		LoggingConnector.getInstance("INFO");
	}
	@Test
	void testPrintTlsServerSpecification() {
		var xmlSpecFile= new File("../data/specification/ApplicationSpecifications/TR-03116-4-SERVER-Specification.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		
		ConfigPrinter.printTlsSpecification(spec);
	}

	@Test
	void testPrintTlsClientSpecification() {
		var xmlSpecFile= new File("../data/specification/ApplicationSpecifications/TR-03116-4-CLIENT-Specification.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);

		ConfigPrinter.printTlsSpecification(spec);
	}

	@Test
	void testPrintEIDClientTLS12Specification() {
		var xmlSpecFile= new File("../data/specification/ApplicationSpecifications/TR-03124-1-EID-CLIENT-Specification-TLS-1-2.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);

		System.out.println(ConfigPrinter.printTlsSpecification(spec));
	}

	@Test
	void testPrintEIDClientTLS2Specification() {
		var xmlSpecFile= new File("../data/specification/ApplicationSpecifications/TR-03124-1-EID-CLIENT-Specification-TLS-2.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);

		ConfigPrinter.printTlsSpecification(spec);
	}
	
	@Test
	void testTestCasePrinting() {
		var testCaseDirectory = new File("../data/specification/TestCases");
		assertTrue(testCaseDirectory.exists(), "Test file directory does not exist.");
		assertTrue(testCaseDirectory.isDirectory(), "Test file is not a directory.");
		
		var testCaseInfoMap = ConfigParser.parseTestCases(testCaseDirectory);
		
		ConfigPrinter.printTestCaseInfo(testCaseInfoMap);
		
	}
	
	@Test
	void testPrintTestProfiles() {
		var testProfilesFile = new File("../data/specification/TestProfiles.xml");
		assertTrue(testProfilesFile.exists(), "XML Test file does not exist.");

		var profileIds = ConfigParser.parseTestProfiles(testProfilesFile);
		
		ConfigPrinter.printTestProfiles(profileIds);
	}
	
	@Test
	void testPrintApplicationMapping() {
		var appMappingFile = new File("../data/specification/ApplicationSpecificProfiles/TR-03116-4-AM-TLS-SERVER.xml");
		assertTrue(appMappingFile.exists(), "XML Test file does not exist.");
		
		var appMapping = ConfigParser.parseApplicationMapping(appMappingFile);
		
		ConfigPrinter.printApplicationMapping(appMapping);
	}
}
