package com.achelos.task.xmlparser.xmlparsing;

import java.io.File;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import generated.jaxb.testrunplan.TestRunPlan;
import generated.jaxb.xmlreport.TaSKReport;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

/**
 * Helper class used for the creation of XML files.
 */
public class XmlPrinting {

	/**
	 * Hidden Constructor.
	 */
	private XmlPrinting() {
		// Empty.
	}

	public static void printXmlReport(final TaSKReport taskReport, final File fileToWrite)
			throws JAXBException, SAXException {
		var schemaStream = Constants.getResourceAsStream(Constants.RESOURCE_TASK_REPORT_XSD);

		JAXBContext jc = JAXBContext
				.newInstance("generated.jaxb.xmlreport:generated.jaxb.input:generated.jaxb.testrunplan");
		var marshaller = jc.createMarshaller();

		// Set Schema
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(new StreamSource(schemaStream));
		marshaller.setSchema(schema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		// fileToWrite
		// Try to make parents
		var parent = fileToWrite.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new RuntimeException("Failed to create output folder");
			}
		}

		marshaller.marshal(taskReport, fileToWrite);
	}

	public static void printTestRunPlanXml(final TestRunPlan testRunPlan, final File fileToWrite)
			throws JAXBException, SAXException {
		marshallObject(testRunPlan, Constants.RESOURCE_TEST_RUN_PLAN_XSD, fileToWrite);
	}

	private static <T> void marshallObject(final T object, final String pathToSchema, final File fileToWrite)
			throws JAXBException, SAXException {
		marshallObject(object, Constants.getResourceAsStream(pathToSchema), fileToWrite);
	}

	private static <T> void marshallObject(final T object, final InputStream schemaStream, final File fileToWrite)
			throws JAXBException, SAXException {
		// Create Marshaller from Class type *.class.
		JAXBContext jc = JAXBContext.newInstance(object.getClass());
		var marshaller = jc.createMarshaller();

		// Set Schema
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(new StreamSource(schemaStream));
		marshaller.setSchema(schema);
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		// fileToWrite
		// Try to make parents
		var parent = fileToWrite.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				throw new RuntimeException("not a valid directory");
			}
		}

		marshaller.marshal(object, fileToWrite);
	}
}
