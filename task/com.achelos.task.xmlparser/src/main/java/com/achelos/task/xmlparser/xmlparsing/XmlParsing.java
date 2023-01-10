package com.achelos.task.xmlparser.xmlparsing;

import java.io.File;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.achelos.task.logging.LoggingConnector;

import generated.jaxb.configuration.ApplicationMapping;
import generated.jaxb.configuration.Configuration;
import generated.jaxb.configuration.TLSSpecification;
import generated.jaxb.configuration.TestCase;
import generated.jaxb.configuration.TestProfiles;
import generated.jaxb.configuration.TlsConfigurationData;
import generated.jaxb.input.ICS;
import generated.jaxb.testrunplan.TestRunPlan;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 * Helper class used for the parsing of XML files.
 */
public class XmlParsing {
	/**
	 * Hidden Constructor.
	 */
	private XmlParsing() {
		// Empty.
	}

	/**
	 * Try to unmarshall the TLSSpecification provided as File parameter.
	 *
	 * @param xmlFile TLSSpecification XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the XML Structure otherwise.
	 */
	public static TLSSpecification unmarshallTlsSpecification(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_TLS_SPEC_XSD, TLSSpecification.class);
	}

	/**
	 * Try to unmarshall the TestProfiles provided as File parameter.
	 *
	 * @param xmlFile TestProfiles XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the XML Structure otherwise.
	 */
	public static TestProfiles unmarshallTestProfiles(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_TEST_PROFILE_XSD, TestProfiles.class);
	}

	/**
	 * Try to unmarshall the ApplicationMapping provided as File parameter.
	 *
	 * @param xmlFile ApplicationMapping XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the XML Structure otherwise.
	 */
	public static ApplicationMapping unmarshallApplicationMapping(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_APP_MAPPING_XSD, ApplicationMapping.class);
	}

	/**
	 * Try to unmarshall a TestCase provided as File parameter.
	 *
	 * @param xmlFile TestCase XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the TestCase otherwise.
	 */
	public static TestCase unmarshallTestCase(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_TESTCASE_XSD, TestCase.class);
	}

	/**
	 * Try to unmarshall a GlobalConfig provided as File parameter.
	 *
	 * @param xmlFile GlobalConfig configuration XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the GlobalConfig configuration
	 * otherwise.
	 */
	public static Configuration unmarshallGlobalConfig(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_GLOBAL_CONFIG_XSD, Configuration.class);
	}

	/**
	 * Try to unmarshall a TlsConfigurationData table provided as File parameter.
	 *
	 * @param xmlFile TlsConfigurationData configuration XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the TlsConfigurationData
	 * configuration otherwise.
	 */
	public static TlsConfigurationData unmarshallTlsConfigurationData(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_TLS_CONFIG_DATA_XSD, TlsConfigurationData.class);
	}

	/**
	 * Try to unmarshall a MICS file provided as a file parameter.
	 *
	 * @param xmlFile the MICS XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the MICS file otherwise.
	 */
	public static ICS unmarshallMICS(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_MICS_XSD, ICS.class);
	}

	/**
	 * Try to unmarshall the TestRunPlan provided as File parameter.
	 *
	 * @param xmlFile TestRunPlan XML File.
	 * @return null if an error occurred, a generated JAXB class representation of the XML Structure otherwise.
	 */
	public static TestRunPlan unmarshallTestRunPlan(final File xmlFile) {
		return unmarshallObject(xmlFile, Constants.RESOURCE_TEST_RUN_PLAN_XSD, TestRunPlan.class);
	}

	private static <T> T unmarshallObject(final File xmlFile, final String pathToSchema, final Class<T> type) {
		return unmarshallObject(xmlFile, Constants.getResourceAsStream(pathToSchema), type);
	}

	private static <T> T unmarshallObject(final File xmlFile, final InputStream schemaStream, final Class<T> type) {
		var logger = LoggingConnector.getInstance();
		try {
			// Create Unmarshaller from Class type *.class.
			JAXBContext jc = JAXBContext.newInstance(type);
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			// Set Schema
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = sf.newSchema(new StreamSource(schemaStream));
			unmarshaller.setSchema(schema);

			// Create xmlStreamReader from StreamSource (important for keeping location of
			// file correct)
			var xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StreamSource(xmlFile));

			// Parse the input into JAXB Classes and return TLS specification
			var spec = unmarshaller.unmarshal(xmlStreamReader, type);
			return spec.getValue();
		} catch (JAXBException e) {
			logger.error("An error occurred while unmarshalling the XML object: " + xmlFile.getAbsolutePath(), e);
			return null;
		} catch (XMLStreamException e) {
			logger.error("An error occurred while reading the XML object: " + xmlFile.getAbsolutePath(), e);
			return null;
		} catch (SAXException e) {
			logger.error("An error occurred while reading the schema file.", e);
			return null;
		}
	}

}
