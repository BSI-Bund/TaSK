package com.achelos.task.xmlparser.outputparsing;

import java.io.File;

import com.achelos.task.xmlparser.xmlparsing.XmlPrinting;

import generated.jaxb.xmlreport.TaSKReport;

/**
 * Helper class, which is used to print XML output files.
 */
public class OutputPrinter {

	/**
	 * Print a TaSKReport into an XML file and.
	 *
	 * @param report report to write into XML file.
	 * @param fileToWrite File to write the XML report to.
	 */
	public static void printXmlReport(final TaSKReport report, final File fileToWrite) {
		try {
			XmlPrinting.printXmlReport(report, fileToWrite);
		} catch (Exception e) {
			throw new RuntimeException("XML report printing: Unable to print TaSK report XML.", e);
		}
	}
}
