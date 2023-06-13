package com.achelos.task.dutmotivator;

import java.util.List;

import com.achelos.task.logging.LoggingConnector;

public class ManualDUTMotivator implements DUTMotivator {
	
	private LoggingConnector logger;
	
	public ManualDUTMotivator () {
		logger = LoggingConnector.getInstance();
	}

	@Override
	public List<String> motivateConnectionToTaSK(boolean isSessionResumption) {
		logger.info("REQUEST TO TESTER: PLEASE MANUALLY MOTIVATE THE DUT TO CONNECT TO THE SERVER !");
		return List.of("No log available from manually motivated DUT.");
	}

	@Override
	public boolean checkApplicationSpecificInspections(boolean handshakeSuccessful, List<String> logs) {
		// Check of Application Specific Inspection Instructions not possible. 
		// 'ManualDUTMotivator' can not send a DUT log.
		return true;
	}

	@Override
	public void finalizeRMI() {

	}

}
