package com.achelos.task.tr03116ts.testcases.b.b2.gp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.achelos.task.abstracttestsuite.AbstractTestCase;
import com.achelos.task.commandlineexecution.applications.tlstesttool.TlsTestToolExecutor;
import com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources.TestToolResource;
import com.achelos.task.commandlineexecution.applications.tshark.TSharkExecutor;
import com.achelos.task.commons.enums.TlsCipherSuite;
import com.achelos.task.commons.enums.TlsNamedCurves;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.configuration.TlsTestToolCertificateTypes;
import com.achelos.task.logging.MessageConstants;
import com.achelos.task.tr03116ts.testfragments.TFClientCertificate;
import com.achelos.task.tr03116ts.testfragments.TFTCPIPNewConnection;
import com.achelos.task.tr03116ts.testfragments.TFTLSClientHello;
import com.achelos.task.tr03116ts.testfragments.TFTLSVersionCheck;

import static com.achelos.task.commons.enums.TlsVersion.TLS_V1_3;


/**
 * Testcase TLS_B2_GP_07_T - Perfect forward secrecy cipher suites.
 * <p>
 * This positive test evaluates the ability of the DUT to establish a TLS connection with valid parameters using named
 * DHE groups and PFS. The test is carried out for the TLS version [TLS_VERSION], the PFS-cipher suite [CIPHERSUITE] and
 * the domain parameters [GROUP].
 * <p>
 * The test MUST be repeated for each combination of TLS version [TLS_VERSION], PFS algorithm [CIPHERSUITE] using DHE
 * and DHE parameters [GROUP] supported by the DUT for incoming TLS connections.
 */
public class TLS_B2_GP_07_T extends AbstractTestCase {

	private static final String TEST_CASE_ID = "TLS_B2_GP_07_T";
	private static final String TEST_CASE_DESCRIPTION = "Perfect forward secrecy cipher suites";
	private static final String TEST_CASE_PURPOSE
			= "This positive test evaluates the ability of the DUT to establish a TLS connection "
					+ "with valid parameters using named DHE groups and PFS. The test is carried out "
					+ "for the TLS version [TLS_VERSION], the PFS-cipher suite [CIPHERSUITE] and the domain parameters "
					+ "[GROUP].";

	private TlsTestToolExecutor testTool = null;
	private TSharkExecutor tShark = null;
	private final TFTLSClientHello tfClientHello;
	private final TFTCPIPNewConnection tFTCPIPNewConnection;
	private final TFTLSVersionCheck tFTLSVersionCheck;
	private final TFClientCertificate tfClientCertificate;

	public TLS_B2_GP_07_T() {
		setTestCaseId(TEST_CASE_ID);
		setTestCaseDescription(TEST_CASE_DESCRIPTION);
		setTestCasePurpose(TEST_CASE_PURPOSE);

		tfClientHello = new TFTLSClientHello(this);
		tFTCPIPNewConnection = new TFTCPIPNewConnection(this);
		tFTLSVersionCheck = new TFTLSVersionCheck(this);
		tfClientCertificate = new TFClientCertificate(this);
	}

	@Override
	protected final void prepareEnvironment() throws Exception {
		testTool = new TlsTestToolExecutor(getTestCaseId(), logger);
		tShark = new TSharkExecutor(getTestCaseId(), logger);
		tShark.start();
	}

	/**
	 * <h2>Precondition</h2>
	 * <ul>
	 * <li>DUT services are online without any known disturbances.
	 * <li>The DUT is accepting TLS connections.
	 * </ul>
	 */
	@Override
	protected final void preProcessing() throws Exception {
		
	}

	/**
	 * <h2>TestStep 1</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The tester connects to the DUT.
	 * </ol>
	 * <h3>Description</h3>
	 * <ol>
	 * <li>The TLS ClientHello offers the TLS version [TLS_VERSION].
	 * <li>The TLS ClientHello offers only the cipher suite [CIPHERSUITE].
	 * <li>The TLS ClientHello offers the named groups extensions according to [GROUP].
	 * <li>The presence of the signature_algorithms extension depends on the used TLS version. In case it is sent, all
	 * algorithms that are supported according to the ICS are listed.
	 * <li>The TLS ClientHello does not contain further extensions which are not required to conduct the TLS handshake.
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the ClientHello.
	 * <li>It requests client authentication.
	 * </ul>
	 * <h2>TestStep 2</h2>
	 * <h3>Command</h3>
	 * <ol>
	 * <li>The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].
	 * </ol>
	 * <h3>ExpectedResult</h3>
	 * <ul>
	 * <li>The DUT accepts the TLS connection.
	 * <li>The TLS protocol is executed without errors and the channel is established.
	 * <li>The [GROUP] is used for the DHE key agreement.
	 * </ul>
	 */
	@Override
	protected final void executeUsecase() throws Exception {

		logger.info("START: " + getTestCaseId());
		logger.info(getTestCaseDescription());

		/* all supported TLS versions */
		var tlsVersions = configuration.getSupportedTLSVersions();
		if (null == tlsVersions || tlsVersions.isEmpty()) {
			logger.error(MessageConstants.NO_SUPPORTED_TLS_VERSIONS);
			return;
		}
		logger.debug(MessageConstants.SUPPORTED_TLS_VERSIONS);
		for (TlsVersion tlsVersion : tlsVersions) {
			logger.debug(tlsVersion.getName());
		}

		// repeat test for each supported TLS version
		int iterationCount = 1;
		int totalNumberOfIterations = findOutTotalNumberOfIterations(tlsVersions);
		for (TlsVersion tlsVersion : tlsVersions) {

			/* supported ECC algorithm cipher suites */
			var cipherSuites = configuration.getSupportedPFSCipherSuites(tlsVersion);
			logger.debug("Supported PFS Cipher suites:");
			for (TlsCipherSuite cipherSuite : cipherSuites) {
				logger.debug(cipherSuite.getName());
			}

			if (cipherSuites.size() == 0) {
				logger.error("No supported PFS cipher suites found.");
				continue;
			}

			/** Supported Groups */
			var ffdheSupportedGroups
					= configuration.filterSupportedGroupsToFFDHEGroups(tlsVersion);
			var eccSupportedGroups
					= configuration.filterSupportedGroupsToEllipticCurveGroups(tlsVersion);

			if ((null == ffdheSupportedGroups || ffdheSupportedGroups.isEmpty())
					&& (null == eccSupportedGroups || eccSupportedGroups.isEmpty())) {
				logger.error("No supported groups found.");
				continue;
			}
			logger.debug("Supported FFHDE Groups:");
			for (TlsNamedCurves group : ffdheSupportedGroups) {
				logger.debug(group.name());
			}
			logger.debug("Supported ECC Groups:");
			for (TlsNamedCurves group : eccSupportedGroups) {
				logger.debug(group.name());
			}

			// repeat test for all supported pfs cipher suites
			for (TlsCipherSuite cipherSuite : cipherSuites) {

				List<TlsNamedCurves> supportedGroups= null;
				if(tlsVersion.compareTo(TlsVersion.TLS_V1_2)<=0){
					boolean isECDHECipherSuite = TlsCipherSuite.filterByName("TLS_ECDHE_").contains(cipherSuite);
					if (isECDHECipherSuite) {
						supportedGroups = eccSupportedGroups;
					} else {
						supportedGroups = ffdheSupportedGroups;
					}
				}else /*TLS 1.3*/{
					supportedGroups = Stream.concat(eccSupportedGroups.stream(), ffdheSupportedGroups.stream()).toList();
				}

				for (TlsNamedCurves supportedGroup : supportedGroups) {
					
					tfClientCertificate.executeSteps("1",
							"The TLS client supplies the valid certificate chain [CERT_DEFAULT_CLIENT].",
							Arrays.asList(),
							testTool, tlsVersion, TlsTestToolCertificateTypes.CERT_DEFAULT);
					
					tfClientHello.executeSteps("2", "The TLS ClientHello offers the TLS version " + tlsVersion.getName()
							+ ", cipher suite " + cipherSuite.getName() + " and named groups extension "
							+ supportedGroup.name()
							+ " .", null, testTool, tlsVersion, cipherSuite, supportedGroup);
					testTool.start(iterationCount, totalNumberOfIterations);
					iterationCount++;

					tFTCPIPNewConnection.executeSteps("3", "", Arrays.asList(), testTool);
					
					step(4, "Check if the DUT requests client authentication.", "The DUT requests client authentication.");
					testTool.assertMessageLogged(TestToolResource.CertificateRequest_valid);
					testTool.assertMessageLogged(TestToolResource.Certificate_transmitted);

					tFTLSVersionCheck.executeSteps("5", "",
							Arrays.asList("tlsVersion=" + tlsVersion.getName(), "isSupported=true"), testTool,
							tlsVersion, true);


					step(6, "Check if the TLS protocol is executed without errors and the channel is established.",
							"The TLS protocol is executed without errors and the channel is established.");
					testTool.assertMessageLogged(TestToolResource.Handshake_successful);
					
					testTool.resetProperties();
				}
			}
		}
	}
	
	private int findOutTotalNumberOfIterations(final List<TlsVersion> tlsVersions) {
		int counter = 0;
		for (TlsVersion tlsVersion : tlsVersions) {
			var cipherSuites = configuration.getSupportedPFSCipherSuites(tlsVersion);
			/** Supported Groups */
			var ffdheSupportedGroups
					= configuration.filterSupportedGroupsToFFDHEGroups(tlsVersion);
			var eccSupportedGroups
					= configuration.filterSupportedGroupsToEllipticCurveGroups(tlsVersion);
			for (TlsCipherSuite cipherSuite : cipherSuites) {
				if (tlsVersion.compareTo(TlsVersion.TLS_V1_2)<=0) {

					boolean isECDHECipherSuite = TlsCipherSuite.filterByName("TLS_ECDHE_").contains(cipherSuite);
					List<TlsNamedCurves> supportedGroups;
					if (isECDHECipherSuite) {
						supportedGroups = eccSupportedGroups;
					} else {
						supportedGroups = ffdheSupportedGroups;
					}
					counter += supportedGroups.size();
				}else if (tlsVersion== TLS_V1_3){
					counter+= configuration.getSupportedGroups(TLS_V1_3).size();
				}
			}
		}
		return counter;
	}

	@Override
	protected void postProcessing() throws Exception {

	}

	@Override
	protected final void cleanAndExit() {
		testTool.cleanAndExit();
		tShark.cleanAndExit();
	}

}