package inputparsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.inputparsing.InputParser;


class TestInputParser {
	
	@BeforeAll
	static void initLogging() {
		LoggingConnector.getInstance("DEBUG");
	}

	@Test
	void testMICSServerParsing() {
		var exampleMICSPath = new File("../data/input/ExampleMICS_Server.xml");
		assertTrue(exampleMICSPath.exists(), "Test MICS does not exist.");

		var mics = InputParser.parseMICS(exampleMICSPath);

		assertEquals("Test Application - TLS Server", mics.getTitle(), "Parsed MICS file is wrong.");
	}
	
	@Test
	void testMICSClientParsing() {
		var exampleMICSPath = new File("../data/input/ExampleMICS_Client.xml");
		assertTrue(exampleMICSPath.exists(), "Test MICS file does not exist.");

		var mics = InputParser.parseMICS(exampleMICSPath);

		assertEquals("Test Application - TLS Client", mics.getTitle(), "Parsed MICS file is wrong.");
	}
}
