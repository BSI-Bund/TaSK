package configparsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileFilter;

import com.achelos.task.logging.LoggingConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.achelos.task.xmlparser.configparsing.ConfigParser;
import com.achelos.task.xmlparser.datastructures.tlsspecification.RestrictionLevel;


class TestConfigParser {

	@BeforeAll
	static void initLogger() {
		var logger = LoggingConnector.getInstance("INFO");
	}
	@Test
	void testParseServerSpecification() {
		var xmlSpecificationFile
				= new File("../data/specification/ApplicationSpecifications/TR-03116-4-SERVER-Specification.xml");
		assertTrue(xmlSpecificationFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecificationFile);
		assertEquals(spec.getId(), "TR-03116-4-SERVER", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}
	
	@Test
	void testParseClientSpecification() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03116-4-CLIENT-Specification.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals(spec.getId(), "TR-03116-4-CLIENT", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDClientSpecificationTLS_1_2() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03124-1-EID-CLIENT-Specification-TLS-1-2.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals(spec.getId(), "TR-03124-1-EID-CLIENT-TLS-1-2", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDClientSpecificationTLS_2() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03124-1-EID-CLIENT-Specification-TLS-2.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals(spec.getId(), "TR-03124-1-EID-CLIENT-TLS-2", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDServerSpecificationEIDASMW() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03130-1-EID-SERVER-Specification-EIDAS-MW.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals(spec.getId(), "TR-03130-1-EID-SERVER-EIDAS-MW", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDServerSpecificationECARDPSK() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03130-1-EID-SERVER-Specification-ECARD-PSK.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals("TR-03130-1-EID-SERVER-ECARD-PSK", spec.getId(), "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDServerSpecificationECARDNonPSK() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03130-1-EID-SERVER-Specification-ECARD-NONPSK.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals("TR-03130-1-EID-SERVER-ECARD-NONPSK", spec.getId(), "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDServerSpecificationEIDInterface() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03130-1-EID-SERVER-Specification-EID-INTERFACE.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals(spec.getId(), "TR-03130-1-EID-SERVER-EID-INTERFACE", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseEIDServerSpecificationSAML() {
		var xmlSpecFile
				= new File("../data/specification/ApplicationSpecifications/TR-03130-1-EID-SERVER-Specification-SAML.xml");
		assertTrue(xmlSpecFile.exists(), "XML Test file does not exist.");

		var spec = ConfigParser.parseSpecification(xmlSpecFile);
		assertEquals(spec.getId(), "TR-03130-1-EID-SERVER-SAML", "Id of parsed specification is wrong.");
		assertEquals(spec.getVersion(), "1.0", "Version of parsed specification is wrong.");

		// Check TLS versions in Spec.
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.2"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.2").getRestriction() == RestrictionLevel.REQUIRED);
		assertTrue(spec.getTlsVersionSupport().containsKey("TLSv1.3"));
		assertTrue(spec.getTlsVersionSupport()
				.get("TLSv1.3").getRestriction() == RestrictionLevel.OPTIONAL);
	}

	@Test
	void testParseTestProfiles() {
		var testProfilesFile = new File("../data/specification/TestProfiles.xml");
		assertTrue(testProfilesFile.exists(), "XML Test file does not exist.");

		var numberOfProfiles = 29;

		var profileIds = ConfigParser.parseTestProfiles(testProfilesFile);
		assertEquals(profileIds.size(), numberOfProfiles, "Number of ProfileIds does not match.");

		assertTrue(profileIds.contains("TLS_SERVER"), "Content of ProfileIds is missing.");
	}

	@Test
	void testParseApplicationMapping() {

		var appMappingFile = new File("../data/specification/ApplicationSpecificProfiles/TR-03116-4-AM-TLS-SERVER.xml");
		assertTrue(appMappingFile.exists(), "XML Test file does not exist.");

		var numberOfMandatoryProfiles = 9;
		var numberOfRecommendedProfiles = 11;
		var numberOfMandatoryIcsSections = 7;
		var numberOfOptionalIcsSections = 0;

		var appMapping = ConfigParser.parseApplicationMapping(appMappingFile);

		assertEquals(appMapping.mandatoryProfiles.size(), numberOfMandatoryProfiles,
				"Number of MandatoryProfiles does not match.");
		assertEquals(appMapping.recommendedProfiles.size(), numberOfRecommendedProfiles,
				"Number of RecommendedProfiles does not match.");
		assertEquals(appMapping.getMandatoryICSSections().size(), numberOfMandatoryIcsSections,
				"Number of MandatoryICSSections does not match.");
		assertEquals(appMapping.getOptionalICSSections().size(), numberOfOptionalIcsSections,
				"Number of OptionalICSSections does not match.");

		assertTrue(appMapping.mandatoryProfiles.contains("TLS_SERVER"), "Content of mandatoryProfiles is missing.");

		assertEquals(appMapping.baseSpecId, "TR-03116-4-APPLICATION-SPECIFIC-PARAMETERS-TLS-SERVER",
				"Content of baseSpecId is wrong or missing.");
	}

	@Test
	void testParseAllApplicationMappings() {

		var appMappingDir = new File("../data/specification/ApplicationSpecificProfiles");
		assertTrue(appMappingDir.exists(), "Application mapping directory does not exist.");
		assertTrue(appMappingDir.isDirectory(), "Application mapping directory is no directory.");

		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return false;
				}
				if (pathname.getName().endsWith(".xml")) {
					return true;
				}
				return false;
			}
		};
		var fileList = appMappingDir.listFiles(fileFilter);
		for (var appMappingFile : fileList) {
			assertTrue(appMappingFile.exists(), "Application mapping does not exist.");
			var appMapping = ConfigParser.parseApplicationMapping(appMappingFile);
			assertNotNull(appMapping);
		}

	}

	@Test
	void testTestCaseParsing() {
		var testCaseDirectory = new File("../data/specification/TestCases");
		assertTrue(testCaseDirectory.exists(), "Test file directory does not exist.");
		assertTrue(testCaseDirectory.isDirectory(), "Test file is not a directory.");

		var numberOfTestCases = 114;
		var testCaseInfoMap = ConfigParser.parseTestCases(testCaseDirectory);

		assertEquals(numberOfTestCases, testCaseInfoMap.size(), "Expected number of test cases does not match.");

	}

	@Test
	void testConfigParsing() {
		var exampleConfigPath = new File("../data/configuration/ExampleGlobalConfig.xml");
		assertTrue(exampleConfigPath.exists(), "Test GlobalConfig does not exist.");

		try {
			var configMap = ConfigParser.parseGlobalConfig(exampleConfigPath);
		} catch (IllegalArgumentException e) {
			return;
		}
		Assertions.fail();
	}
}
