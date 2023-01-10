package com.achelos.task.xmlparser.configparsing;

import java.io.File;
import java.io.FileFilter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.achelos.task.xmlparser.datastructures.applicationmapping.AppMapping;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigChecker;
import com.achelos.task.xmlparser.datastructures.configuration.GlobalConfigParameter;
import com.achelos.task.xmlparser.datastructures.testcase.TestCaseInfo;
import com.achelos.task.xmlparser.datastructures.tlsspecification.TlsSpecification;
import com.achelos.task.xmlparser.xmlparsing.XmlParsing;

/**
 * Helper class, used for the parsing of configuration files.
 */
public class ConfigParser {

	// Hide Constructor.
	private ConfigParser() {
		// Empty.
	}


	/**
	 * Parse a Global Config XML file and return a map of internal data structure representation of configurations.
	 *
	 * @param xmlFile GlobalConfig XML file
	 * @return a map of internal data structure representation of configurations
	 */
	public static HashMap<String, GlobalConfigParameter> parseGlobalConfig(final File xmlFile) {
		var config = new HashMap<String, GlobalConfigParameter>();
		var rawGlobalConfig = XmlParsing.unmarshallGlobalConfig(xmlFile);
		for (var parameter : rawGlobalConfig.getParameters().getParameter()) {
			var configParam = GlobalConfigParameter.parseConfigParameterFromJaxb(parameter);
			config.put(configParam.getId(), configParam);
		}
		GlobalConfigChecker.checkGlobalConfig(config);
		return config;
	}

	/**
	 * Parse a TLS specification from an XML file and return the internal data structure representation of it.
	 *
	 * @param xmlFile TLS specification in XML file.
	 * @return the internal data structure representation of {@link TlsSpecification}
	 */
	public static TlsSpecification parseSpecification(final File xmlFile) {
		var rawTlsSpec = XmlParsing.unmarshallTlsSpecification(xmlFile);
		return TlsSpecification.parseFromJaxb(rawTlsSpec);
	}

	/**
	 * Parse a list of TLS specifications XML files and return a list of internal data structure representation of them.
	 *
	 * @param xmlFileList the list of TLS specifications XML files
	 * @return a list of internal data structure representation of {@link TlsSpecification}
	 */
	public static List<TlsSpecification> parseSpecificationList(final List<File> xmlFileList) {
		var listOfSpecs = new ArrayList<TlsSpecification>();
		for (var xmlFile : xmlFileList) {
			listOfSpecs.add(parseSpecification(xmlFile));
		}
		return listOfSpecs;
	}

	/**
	 * Parse a TestProfiles XML file and return the ProfileIds as a list of Strings.
	 *
	 * @param xmlFile TestProfiles XML file
	 * @return contained ProfileIds as a list of Strings.
	 */
	public static List<String> parseTestProfiles(final File xmlFile) {
		var rawTestProfiles = XmlParsing.unmarshallTestProfiles(xmlFile);
		return rawTestProfiles.getProfileId();
	}

	/**
	 * Parse an ApplicationMapping from an XML file and return the internal data structure representation of it.
	 *
	 * @param xmlFile ApplicationMapping in XML file.
	 * @return the internal data structure representation of ApplicationMapping
	 */
	public static AppMapping parseApplicationMapping(final File xmlFile) {
		var rawAppMap = XmlParsing.unmarshallApplicationMapping(xmlFile);
		return AppMapping.parseFromJaxb(rawAppMap);
	}

	/**
	 * Parse a list of ApplicationMapping XML files and return a list of internal data structure representation of them.
	 *
	 * @param xmlFileList list of ApplicationMappings XML files
	 * @return a list of internal data structure representation of ApplicationMapping
	 */
	public static List<AppMapping> parseApplicationMappingList(final List<File> xmlFileList) {
		var listOfAppMappings = new ArrayList<AppMapping>();
		for (var xmlFile : xmlFileList) {
			listOfAppMappings.add(parseApplicationMapping(xmlFile));
		}
		return listOfAppMappings;
	}

	/**
	 * Parse a list of TestCase XML files and return a list of internal data structure representation of them.
	 *
	 * @param xmlFileList list of TestCase XML files
	 * @return a list of internal data structure representation of TestCases
	 */
	public static List<TestCaseInfo> parseTestCaseList(final List<File> xmlFileList) {
		var listOfTestCases = new ArrayList<TestCaseInfo>();
		for (var xmlFile : xmlFileList) {
			listOfTestCases.addAll(parseTestCases(xmlFile));
		}
		return listOfTestCases;
	}

	/**
	 * Parse a list of TestCase XML files and return a list of internal data structure representation of them.
	 *
	 * @param directoryPath Path to a directory containing TestCase XML files
	 * @return a list of internal data structure representation of contained TestCases
	 */
	public static List<TestCaseInfo> parseTestCases(final File directoryPath) {
		var testCaseList = new ArrayList<TestCaseInfo>();

		if (!directoryPath.isDirectory()) {
			var testCaseInfo = parseTestCase(directoryPath);
			testCaseList.add(testCaseInfo);
			return testCaseList;
		}

		FileFilter fileFilter = pathname -> {
			if (pathname.isDirectory()) {
				return true;
			}
			return pathname.getName().endsWith(".xml");
		};

		var fileList = directoryPath.listFiles(fileFilter);
		if (fileList != null) {
			for (var file : fileList) {
				if (file == null) {
					continue;
				}
				if (file.isDirectory()) {
					var subDirTestCaseList = parseTestCases(file);
					testCaseList.addAll(subDirTestCaseList);
				} else {
					var testCaseInfo = parseTestCase(file);
					testCaseList.add(testCaseInfo);
				}
			}
		}

		return testCaseList;
	}

	/**
	 * Parse a list of TLS version Strings out of a TlsConfigurationData XML file.
	 *
	 * @param xmlFile a TlsConfigurationData XML file
	 * @return a list of TLS version Strings out of the TlsConfigurationData XML file.
	 */
	public static List<String> parseTlsVersionsFromConfigData(final File xmlFile) {
		var rawConfigData = XmlParsing.unmarshallTlsConfigurationData(xmlFile);
		return rawConfigData.getTlsVersions().getTlsVersion();
	}

	/**
	 * Parse a list of EllipticCurves Strings out of a TlsConfigurationData XML file.
	 *
	 * @param xmlFile a TlsConfigurationData XML file
	 * @return a list of EllipticCurves Strings out of the TlsConfigurationData XML file.
	 */
	public static List<String> parseNamedCurvesFromConfigData(final File xmlFile) {
		var rawConfigData = XmlParsing.unmarshallTlsConfigurationData(xmlFile);
		return rawConfigData.getNamedCurves().getNamedCurve();
	}

	/**
	 * Parse a list of RSA/DSA/DHE KeyLengths out of a TlsConfigurationData XML file.
	 *
	 * @param xmlFile a TlsConfigurationData XML file
	 * @return a list of RSA/DSA/DHE KeyLengths out of the TlsConfigurationData XML file.
	 */
	public static List<BigInteger> parseKeyLengthsFromConfigData(final File xmlFile) {
		var rawConfigData = XmlParsing.unmarshallTlsConfigurationData(xmlFile);
		return rawConfigData.getRSADSADHEKeyLengths().getRSADSADHEKeyLength();
	}

	/**
	 * Parse a TestCase from an XML file and return the internal data structure representation of it.
	 *
	 * @param xmlFile TestCase in XML file.
	 * @return the internal data structure representation of TestCase
	 */
	private static TestCaseInfo parseTestCase(final File xmlFile) {
		var rawAppMap = XmlParsing.unmarshallTestCase(xmlFile);
		return TestCaseInfo.parseFromJaxb(rawAppMap);
	}

}
