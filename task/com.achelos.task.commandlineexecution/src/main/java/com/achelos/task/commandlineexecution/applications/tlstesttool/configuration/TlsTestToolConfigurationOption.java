package com.achelos.task.commandlineexecution.applications.tlstesttool.configuration;

/**
 * Known configuration options for the TLS Test Tool.
 */
public enum TlsTestToolConfigurationOption {
	/** caCertificateFile. */
	CACERTIFICATEFILE("caCertificateFile"),
	/** certificateFile. */
	CERTIFICATEFILE("certificateFile"),
	/** earlydata */
	EARLYDATA("earlyData"),
	/** host. */
	HOST("host"),
	/** handshakeType */
	HANDSHAKETYPE("handshakeType"),
	/** logFilterRegEx. */
	LOGFILTERREGEX("logFilterRegEx"),
	/** logLevel. */
	LOGLEVEL("logLevel"),
	/** manipulateClientHelloExtensions. */
	MANIPULATECLIENTHELLOEXTENSIONS("manipulateClientHelloExtensions"),
	/** manipulateClientHelloExtensions. */
	MANIPULATESERVERHELLOEXTENSIONS("manipulateServerHelloExtensions"),
	/** manipulateEllipticCurveGroup. */
	MANIPULATEELLIPTICCURVEGROUP("manipulateEllipticCurveGroup"),
	/** manipulateHelloVersion. */
	MANIPULATEHELLOVERSION("manipulateHelloVersion"),
	/** manipulateSendHeartbeatRequest. */
	MANIPULATESENDHEARTBEATREQUEST("manipulateSendHeartbeatRequest"),
	/** manipulateRenegotiate. */
	MANIPULATERENEGOTIATE("manipulateRenegotiate"),
	/** manipulateClientHelloCompressionMethods. */
	MANIPULATECLIENTHELLOCOMPRESSIONMETHODS("manipulateClientHelloCompressionMethods"),
	/** manipulateServerHelloCompressionMethod. */
	MANIPULATESERVERHELLOCOMPRESSIONMETHOD("manipulateServerHelloCompressionMethod"),
	/** manipulateServerHelloCompressionMethod. */
	MANIPULATEFORCECERTIFICATEUSAGE("manipulateForceCertificateUsage"),
	/** mode. */
	MODE("mode"),
	/** sessionCache. */
	SESSIONCACHE("sessionCache"),
	/** sessionLifetime. */
	SESSIONLIFETIME("sessionLifetime"),
	/** tlsLibrary. */
	TLSLIBRARY("tlsLibrary"),
	/** port. */
	PORT("port"),
	/** privateKeyFile. */
	PRIVATEKEYFILE("privateKeyFile"),
	/** tlsCipherSuites. */
	TLSCIPHERSUITES("tlsCipherSuites"),
	/** tlsEncryptThenMac. */
	TLSENCRYPTTHENMAC("tlsEncryptThenMac"),
	/** tlsSecretFile. */
	TLSSECRETFILE("tlsSecretFile"),
	/** tlsServerDHParams. */
	TLSSERVERDHPARAMS("tlsServerDHParams"),
	/** tlsSignatureShemes. */
	TLSSIGNATURESCHEMES("tlsSignatureSchemes"),
	/** tlsSupportedGroups. */
	TLSSUPPORTEDGROUPS("tlsSupportedGroups"),
	/** tlsSignatureAlgorithms. */
	SIGNATUREALGORITHMS("tlsSignatureAlgorithms"),
	/** tlsVerifyPeer. */
	TLSVERIFYPEER("tlsVerifyPeer"),
	/** tlsVersion. */
	TLSVERSION("tlsVersion"),
	/** waitBeforeClose. */
	WAITBEFORECLOSE("waitBeforeClose"),
	/** tlsUseSni. */
	TLSUSESNI("tlsUseSni"),
	/** ocspResponseFile. */
	OCSPRESPONSEFILE("ocspResponseFile"),
	/** psk. */
	PSK("psk"),
	/** psk. */
	PSKIDENTITIYHINT("pskIdentityHint"),
	/** psk identity */
	PSKIDENTITY("pskIdentity");

	private final String value;

	/**
	 * Default constructor.
	 *
	 * @param value value of the configuration option.
	 */
	TlsTestToolConfigurationOption(final String value) {
		this.value = value;
	}


	@Override
	public final String toString() {
		return value;
	}
}

