package com.achelos.task.xmlparser.datastructures.testrunplan;

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
	private String dutRMIURL;
	private String dutRMIPort;
	private boolean useStartTLS;
	private Integer dutEIDClientPort;
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
		testConfiguration.dutURL = rawTestConfig.getURL() != null ? rawTestConfig.getURL() : "";
		testConfiguration.dutPort = rawTestConfig.getPort() != null ? rawTestConfig.getPort() : "";
		testConfiguration.dutRMIURL = rawTestConfig.getRMIURL() != null ? rawTestConfig.getRMIURL() : "";
		testConfiguration.dutRMIPort = rawTestConfig.getRMIPort() != null ? rawTestConfig.getRMIPort() : "1099";
		testConfiguration.dutEIDClientPort = rawTestConfig.getEIDClientPort() != null ?  rawTestConfig.getEIDClientPort() : 24727;
		testConfiguration.useStartTLS = rawTestConfig.isStartTLS() != null ? rawTestConfig.isStartTLS() : false;

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
	 * Returns the DUT eID-Client port which is stored in this TestConfiguration.
	 * @return the DUT eID-Client port which is stored in this TestConfiguration.
	 */
	public Integer getDutEIDClientPort() {
		return dutEIDClientPort;
	}

	/**
	 * Returns the use Start TLS flag which is stored in this TestConfiguration.
	 * @return the use Start TLS flag which is stored in this TestConfiguration.
	 */
	public boolean useStartTLS() {
		return  this.useStartTLS;
	}

	/**
	 * Return the stored DUT Capabilities.
	 * @return the stored DUT Capabilities.
	 */
	public List<DUTCapabilities> getDutCapabilities() {
		return new ArrayList<>(dutCapabilities);
	}

	/**
	 * Return the DUT RMI URL of the Test Run Configuration.
	 * @return the DUT RMI URL of the Test Run Configuration.
	 */
	public String getDutRMIURL() {
		return dutRMIURL;
	}

	/**
	 * Return the DUT RMI Port of the Test Run Configuration.
	 * @return the DUT RMI Port of the Test Run Configuration.
	 */
	public String getDutRMIPort() {
		return dutRMIPort;
	}
}
