package com.achelos.task.xmlparser.datastructures.configuration;

/**
 * Enum containing all available GlobalConfiguration Parameters.
 */
public enum GlobalConfigParameterNames {

	TesterInCharge("tester_in_charge", true, GlobalConfigParameter.ConfigParameterType.STRING, null),
	TlsTestToolPath("tls_test_tool_path", true, GlobalConfigParameter.ConfigParameterType.STRING, null),
	TlsTestToolPort("tls_test_tool_port", false, GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("8080", GlobalConfigParameter.ConfigParameterType.INT, "tls_test_tool_port")),
	TlsTestToolLogLevel("tls_test_tool_logLevel", true, GlobalConfigParameter.ConfigParameterType.STRING, null),
	TlsTestToolCertificatesPath("tls_test_tool_certificates_path", false,
			GlobalConfigParameter.ConfigParameterType.STRING, null),
	TlsTestToolWaitBeforeClose("tls_test_tool_wait_before_close", false, GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("5", GlobalConfigParameter.ConfigParameterType.INT,
					"tls_test_tool_wait_before_close")),
	MaximumWaitTimeForReadingLogMessage("maximum_wait_time_for_reading_log_message", false,
			GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("60", GlobalConfigParameter.ConfigParameterType.INT,
					"maximum_wait_time_for_reading_log_message")),
	ReportDirectory("report_directory", true, GlobalConfigParameter.ConfigParameterType.STRING, null),
	PdfReportStylesheet("pdf_report_stylesheet", false, GlobalConfigParameter.ConfigParameterType.STRING, null),
	SpecificationDirectory("specification_directory", true, GlobalConfigParameter.ConfigParameterType.STRING, null),
	TestSuiteJars("additional_testsuite_jars", false, GlobalConfigParameter.ConfigParameterType.STRING, null),
	TSharkEnabled("tshark_enabled", false, GlobalConfigParameter.ConfigParameterType.BOOLEAN,
			new GlobalConfigParameter("false", GlobalConfigParameter.ConfigParameterType.BOOLEAN, "tshark_enabled")),
	TSharkInterface("tshark_interface", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("", GlobalConfigParameter.ConfigParameterType.STRING, "tshark_interface")),
	TSharkOptions("tshark_options", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("", GlobalConfigParameter.ConfigParameterType.STRING, "tshark_options")),
	TSharkPath("tshark_path", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("", GlobalConfigParameter.ConfigParameterType.STRING, "tshark_path")),
	OpenSSLExecutablePath("openssl_path", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("openssl", GlobalConfigParameter.ConfigParameterType.STRING, "openssl_path")),
	OcspResponderPort("ocsp_responder_port", false, GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("9080", GlobalConfigParameter.ConfigParameterType.INT, "ocsp_responder_port")),
	CrlResponderPort("crl_responder_port", false, GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("8081", GlobalConfigParameter.ConfigParameterType.INT, "crl_responder_port")),
	RestApiHost("rest_api_host", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("task.achelos.de", GlobalConfigParameter.ConfigParameterType.STRING, "rest_api_host")),
	RestApiPort("rest_api_port", false, GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("8088", GlobalConfigParameter.ConfigParameterType.INT, "rest_api_port")),
	RestApiCredentials("rest_api_credentials", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("", GlobalConfigParameter.ConfigParameterType.STRING, "rest_api_credentials")),
	RestApiCredentialPassword("rest_api_credential_pass", false, GlobalConfigParameter.ConfigParameterType.STRING,
			new GlobalConfigParameter("", GlobalConfigParameter.ConfigParameterType.STRING, "rest_api_credential_pass")),
	DutExecutableTimeout("dut_executable_timeout", false, GlobalConfigParameter.ConfigParameterType.INT,
			new GlobalConfigParameter("5", GlobalConfigParameter.ConfigParameterType.INT, "dut_executable_timeout_milliseconds"));

	private final String parameterName;
	private final boolean mandatory;
	private final GlobalConfigParameter.ConfigParameterType type;
	private final GlobalConfigParameter defaultValue;

	GlobalConfigParameterNames(final String parameterName, final boolean mandatory,
			final GlobalConfigParameter.ConfigParameterType type, final GlobalConfigParameter defaultValue) {
		this.parameterName = parameterName;
		this.mandatory = mandatory;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the Parameter Name of an enum value.
	 * @return the Parameter Name of an enum value.
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Returns the Information whether the Parameter is mandatory.
	 * @return Information whether the Parameter is mandatory.
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Returns the required type of the Parameter.
	 * @return the required type of the Parameter.
	 */
	public GlobalConfigParameter.ConfigParameterType getType() {
		return type;
	}

	/**
	 * Returns the stored default value of a Global Config Parameter.
	 * @return the stored default value of a Global Config Parameter or null if none is set.
	 */
	public GlobalConfigParameter getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Get the global configuration parameter names corresponding to the specified parameterName String.
	 *
	 * @param parameterName the specified parameterName String to get the {@link GlobalConfigParameterNames} Object for.
	 * @return the {@link GlobalConfigParameterNames} Object for the specified parameterName String, or null if none exists.
	 */
	public static GlobalConfigParameterNames getConfigFromId(final String parameterName) {
		for (var configParameterName : GlobalConfigParameterNames.values()) {
			if (configParameterName.getParameterName().equals(parameterName)) {
				return configParameterName;
			}
		}
		return null;
	}
}
