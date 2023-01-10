package com.achelos.task.xmlparser.inputparsing;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.xmlparsing.XmlParsing;

/**
 * Helper class used for the parsing of Input XML files.
 */
public class InputParser {

	// Hide Constructor.
	private InputParser() {
		// Do nothing.
	}

	/**
	 * Parse a TLS specification from an XML file and return the internal data structure representation of it.
	 *
	 * @param xmlFile TLS specification in XML file.
	 * @return the internal data structure representation of TLS specification
	 */
	public static MICS parseMICS(final File xmlFile) {
		var logger = LoggingConnector.getInstance();
		logger.debug("InputParser: Trying to unmarshall the MICS XML file: " + xmlFile.getAbsolutePath());
		var rawMICS = XmlParsing.unmarshallMICS(xmlFile);
		logger.tellLogger(BasicLogger.MSG_MICS, rawMICS);
		logger.debug("InputParser: Successfully unmarshalled the MICS XML file: " + xmlFile.getAbsolutePath());
		logger.debug("InputParser: Trying to parse the MICS file data into internal data structure.");
		try {
			return MICS.parseFromJaxb(rawMICS);
		} catch (Exception e) {
			logger.error("InputParser: Error occurred while parsing the MICS file data into internal data structure.", e);
			var stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			logger.debug("Stacktrace: " + stringWriter.toString());
			throw new RuntimeException("InputParser: Error occurred while parsing the MICS file data into internal data structure.", e);
		}
	}
}
