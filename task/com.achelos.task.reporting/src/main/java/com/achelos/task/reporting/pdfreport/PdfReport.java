package com.achelos.task.reporting.pdfreport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;

import com.achelos.task.logging.LoggingConnector;


/**
 * Class used for the handling of PDF Reports.
 */
public class PdfReport {
	private static final String RESOURCES_XLS = "pdfreport_stylesheet.xsl";

	/**
	 * Convert an XML Report File into an PDF Report and store it in the same directory.
	 * @param xmlReport The XML Report file to convert into a PDF report.
	 * @throws IOException If an error occurs.
	 * @throws FOPException If an error occurs.
	 * @throws TransformerException If an error occurs.
	 */
	public static void convertToPDF(final File xmlReport) throws IOException, FOPException, TransformerException {
		// the XSL FO file
		var xsltAsStream = PdfReport.class.getClassLoader().getResourceAsStream(RESOURCES_XLS);
		convertToPDF(xmlReport, xsltAsStream);
	}

	/**
	 * Convert an XML Report File into an PDF Report and store it in the same directory. Uses an alternate XSLT File to generate the PDF report.
	 * @param xmlReport The XML Report file to convert into a PDF report.
	 * @param xsltFile The alternate XSLT stylesheet file to be used to generate the PDF report.
	 * @throws IOException If an error occurs.
	 * @throws FOPException If an error occurs.
	 * @throws TransformerException If an error occurs.
	 */
	public static void convertToPDF(final File xmlReport, final File xsltFile) throws IOException, FOPException, TransformerException {
		// the XSL FO file
		var xsltAsStream = new FileInputStream(xsltFile);
		convertToPDF(xmlReport, xsltAsStream);
	}

	private static void convertToPDF(final File xmlReport, InputStream xsltAsStream) throws IOException, FOPException, TransformerException {
		// The XML Report File
		var xmlSource = new StreamSource(xmlReport);

		// Apache FOP Usage:
		var fopFactory = FopFactory.newInstance(new File(".").toURI());
		var foUserAgent = fopFactory.newFOUserAgent();
		foUserAgent.getEventBroadcaster().addEventListener(new LoggingEventListener());

		// Setup output
		var pdfReportName = xmlReport.getAbsolutePath().replace(".xml", ".pdf");
		var out = new java.io.FileOutputStream(pdfReportName);

		try (out) {
			// Construct fop with desired output format
			Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, foUserAgent, out);

			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xsltAsStream));

			// Resulting SAX events (the generated FO) must be piped through to
			// FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			// That's where the XML is first transformed to XSL-FO and then
			// PDF is created
			transformer.transform(xmlSource, res);
		} catch (Exception e) {
			LoggingConnector.getInstance().error("Unable to write PDF Report", e);
		}
	}


	/** A simple event listener that ignores non errors. */
	private static class LoggingEventListener implements EventListener {

		private final LoggingConnector logger;

		public LoggingEventListener() {
			logger = LoggingConnector.getInstance();
		}

		/** {@inheritDoc} */
		@Override
		public void processEvent(final Event event) {
			EventSeverity severity = event.getSeverity();
			if (severity == EventSeverity.ERROR || severity == EventSeverity.FATAL) {
				String msg = EventFormatter.format(event);
				logger.error("An error occurred while generating the PDF report: " + msg);
			}
		}
	}

}
