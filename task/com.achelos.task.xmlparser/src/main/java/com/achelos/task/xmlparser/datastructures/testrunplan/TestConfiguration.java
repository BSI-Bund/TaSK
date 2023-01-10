package com.achelos.task.xmlparser.datastructures.testrunplan;

import com.achelos.task.xmlparser.datastructures.common.ApplicationSpecificData;

import generated.jaxb.testrunplan.TestRunPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * A internal data structure representing the Test Configuration section of a TestRunPlan.
 */
public class TestConfiguration {
	private String dutApplicationType;
	private String dutURL;
	private String dutPort;
	private String dutExecutable;
	private String dutCallArgumentsConnect;
	private String dutCallArgumentsReconnect;
	private Integer dutEIDClientPort;
	private ApplicationSpecificData applicationSpecificData;
	private List<DUTCapabilities> dutCapabilities;


	/**
	 * Empty default constructor.
	 */
	private TestConfiguration() {
		// Empty.
	}

	/**
	 * Parse the JAXB internal representation of an TestConfiguration coming from an TestRunPlan file into the internal data structure.
	 * @return the internal data structure containing the information of the provided TestConfiguration.
	 */
	public static TestConfiguration parseFromTRPJaxb(final TestRunPlan.TestConfiguration rawTestConfig) {
		var testConfiguration = new TestConfiguration();
		testConfiguration.dutApplicationType = rawTestConfig.getApplicationType();
		testConfiguration.dutURL = rawTestConfig.getURL();
		testConfiguration.dutPort = rawTestConfig.getPort();
		testConfiguration.dutExecutable = rawTestConfig.getDUTExecutable();
		testConfiguration.dutCallArgumentsConnect = rawTestConfig.getDUTCallArguments().getStartConnectionArguments();
		testConfiguration.dutCallArgumentsReconnect
				= rawTestConfig.getDUTCallArguments().getResumeConnectionArguments();
		testConfiguration.dutEIDClientPort = rawTestConfig.getDUTCallArguments().getEIDClientPort() != null ?  rawTestConfig.getDUTCallArguments().getEIDClientPort() : 24727;
		testConfiguration.applicationSpecificData
				= ApplicationSpecificData.parseFromTRPJaxb(rawTestConfig.getApplicationSpecificData());
		var dutCapablities = rawTestConfig.getDUTCapabilities();
		testConfiguration.dutCapabilities = new ArrayList<>();
		if (dutCapablities != null) {
			for (var capability : dutCapablities.getCapability()) {
				try {
					var dutCapability = DUTCapabilities.valueOf(capability);
					testConfiguration.dutCapabilities.add(dutCapability);
				} catch (Exception ignored) {
					// Ignore
				}
			}
		}
		return testConfiguration;
	}

	/**
	 * Returns the DUT ApplicationType which is stored in this TestConfiguration.
	 * @return the DUT ApplicationType which is stored in this TestConfiguration.
	 */
	public String getDutApplicationType() {
		return dutApplicationType;
	}

	/**
	 * Returns the DUT URL which is stored in this TestConfiguration.
	 * @return the DUT URL which is stored in this TestConfiguration.
	 */
	public String getDutURL() {
		return dutURL;
	}

	/**
	 * Returns the DUT Port which is stored in this TestConfiguration.
	 * @return the DUT Port which is stored in this TestConfiguration.
	 */
	public String getDutPort() {
		return dutPort;
	}

	/**
	 * Returns the DUT Executable which is stored in this TestConfiguration.
	 * @return the DUT Executable which is stored in this TestConfiguration.
	 */
	public String getDutExecutable() {
		return dutExecutable;
	}

	/**
	 * Returns the DUT Call Argument for Connection which is stored in this TestConfiguration.
	 * @return the DUT Call Argument for Connection which is stored in this TestConfiguration.
	 */
	public String getDutCallArgumentsConnect() {
		return dutCallArgumentsConnect;
	}

	/**
	 * Returns the DUT Call Argument for Reconnection which is stored in this TestConfiguration,
	 * or the DUT Call Argument for Connection, if none is provided.
	 * @return the DUT Call Argument for Reconnection which is stored in this TestConfiguration.
	 */
	public String getDutCallArgumentsReconnect() {
		if (dutCallArgumentsReconnect == null || dutCallArgumentsReconnect.isEmpty()) {
			return dutCallArgumentsConnect;
		}
		return dutCallArgumentsReconnect;
	}
	/**
	 * Returns the DUT eID-Client port which is stored in this TestConfiguration.
	 * @return the DUT eID-Client port which is stored in this TestConfiguration.
	 */
	public Integer getDutEIDClientPort() {
		return dutEIDClientPort;
	}

	/**
	 * Returns the stored ApplicationSpecificData of this TestConfiguration.
	 * @return the stored ApplicationSpecificData of this TestConfiguration.
	 */
	public ApplicationSpecificData getApplicationSpecificData() {
		return applicationSpecificData;
	}

	/**
	 * Return the stored DUT Capabilities.
	 * @return the stored DUT Capabilities.
	 */
	public List<DUTCapabilities> getDutCapabilities() {
		return new ArrayList<>(dutCapabilities);
	}
}
