package inputparsing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.inputparsing.InputParser;
import com.achelos.task.xmlparser.inputparsing.InputPrinter;


class TestInputPrinter {

	@Test
	void testMICSPrinting() {
		LoggingConnector.getInstance("INFO");
		var exampleMICSPath = new File("../data/input/ExampleMICS_Server.xml");
		assertTrue(exampleMICSPath.exists(), "Test MICS file does not exist.");

		var mics = InputParser.parseMICS(exampleMICSPath);

		InputPrinter.printMICS(mics);
	}
}
