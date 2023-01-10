package com.achelos.task.micsverifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.util.Arrays;

import com.achelos.task.abstracttestsuite.RunState;
import com.achelos.task.abstracttestsuite.TestCaseRun;
import com.achelos.task.abstracttestsuite.TestSuiteRun;
import com.achelos.task.commons.certificatehelper.CertificateChecker;
import com.achelos.task.commons.enums.TlsVersion;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.utilities.FileUtils;
import com.achelos.task.xmlparser.configparsing.StringHelper;
import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.datastructures.tlsspecification.TlsSpecification;


class X509CertificateVerifier {
	/**
	 * Hidden Constructor.
	 */
	public static final String CERTIFICATE_CHECK_TESTSUITE_ID = "Certificate Checks";
	private final MICS mics;
	private final List<X509Certificate> certificateChain;
	private final TlsSpecification tlsSpecification;
	private final LoggingConnector logger;
	private final static String LOGGER_COMPONENT = "X509CertificateVerifier: ";

	private X509CertificateVerifier(final MICS mics, final TlsSpecification tlsSpecification,
			final File... certificateFiles) {
		logger = LoggingConnector.getInstance();
		this.mics = mics;
		this.tlsSpecification = tlsSpecification;
		certificateChain = new LinkedList<>();

		var certFactory = new CertificateFactory();
		for (var cert : mics.getCertificateChain()) {
			boolean addedCert = false;
			for (var certFile : certificateFiles) {
				if (Arrays.areEqual(getFileFingerprint(certFile), cert.getFingerprint())) {
					try {
						var certInputStream = new FileInputStream(certFile);
						var parsedCertificate = certFactory.engineGenerateCertificate(certInputStream);
						if (parsedCertificate != null && parsedCertificate instanceof X509Certificate) {
							((LinkedList<X509Certificate>) certificateChain)
									.addFirst((X509Certificate) parsedCertificate);
							addedCert = true;
							break;
						}
						throw new IllegalArgumentException("Unable to parse provided certificate Files. "
								+ (null != parsedCertificate ? parsedCertificate.getClass().getName() : ""));

					} catch (Exception e) {
						throw new IllegalArgumentException("Unable to parse provided certificate Files.", e);
					}
				}
			}
			if (!addedCert) {
				throw new IllegalArgumentException(
						"Unable to parse the certificate chain. Certificate missing for Subject: " + cert.getSubject());
			}
		}
		// Should never happen. Just in case.
		if (certificateChain.size() != mics.getCertificateChain().size()) {
			throw new RuntimeException(
					"Unable to parse the hole certificate chain. At least one certificate is missing.");
		}
	}

	/**
	 * Verifies the MICS checklist X.509 certificate checks for the MICS file according to Module 0: ICS Checklist (Chapter
	 * 6.1.1 of TR-03116-TS)
	 *
	 * @param mics the MICS file for the Device Under Test to verify the certificates for.
	 * @param certificateFiles The certificate files to check.
	 * @return true if successfully verified. false otherwise.
	 */
	public static List<TestCaseRun> verifyCertificatesFromMics(final MICS mics, final TlsSpecification tlsSpecification,
			final File... certificateFiles) {
		var logger = LoggingConnector.getInstance();
		var testCaseRunList = new ArrayList<TestCaseRun>();
		var micsChecklistTestSuite = new TestSuiteRun(CERTIFICATE_CHECK_TESTSUITE_ID,
				java.util.Arrays.asList("TLS_CERT_01", "TLS_CERT_02", "TLS_CERT_03", "TLS_CERT_04", "TLS_CERT_05",
						"TLS_CERT_06",
						"TLS_CERT_07", "TLS_CERT_08", "TLS_CERT_09", "TLS_CERT_10", "TLS_CERT_11", "TLS_CERT_12"));
		micsChecklistTestSuite.setStartTime();
		logger.tellLogger(BasicLogger.MSG_NEW_TESTSUITE, micsChecklistTestSuite);
		try {
			// Check if mics not null.
			if (mics == null) {
				throw new NullPointerException(MICSVerifier.LOGGER_COMPONENT + "Provided MICS file is \"null\".");
			}
			// Check if TlsSpecification not null.
			if (tlsSpecification == null) {
				throw new NullPointerException(MICSVerifier.LOGGER_COMPONENT + "Provided TLS specification is \"null\".");
			}

			var testCase = certificateCheck(mics, certificateFiles);
			if (testCase.getErrorCount() + testCase.getFatalErrorCount() > 0) {
				testCaseRunList.add(testCase);
				throw new Exception(MICSVerifier.LOGGER_COMPONENT + "Provided Certificates could not be verified.");
			}

			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Initializing X509CertificateVerifier.");
			var x509CertificateVerifier = new X509CertificateVerifier(mics, tlsSpecification, certificateFiles);
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Successfully initialized X509CertificateVerifier.");

			// TLS_CERT_01
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_01.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_01());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_01.");

			// TLS_CERT_02
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_02.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_02());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_02.");

			// TLS_CERT_03
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_03.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_03());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_03.");

			// TLS_CERT_04
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_04.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_04());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_04.");

			// TLS_CERT_05
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_05.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_05());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_05.");

			// TLS_CERT_06
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_06.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_06());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_06.");

			// TLS_CERT_07
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_07.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_07());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_07.");

			// TLS_CERT_08
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_08.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_08());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_08.");

			// TLS_CERT_09
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_09.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_09());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_09.");

			// TLS_CERT_10
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_10.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_10());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_10.");

			// TLS_CERT_11
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_11.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_11());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_11.");

			// TLS_CERT_12
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Running the test case TLS_CERT_12.");
			testCaseRunList.add(x509CertificateVerifier.TLS_CERT_12());
			logger.debug(MICSVerifier.LOGGER_COMPONENT + "Finished running the test case TLS_CERT_12.");

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error(MICSVerifier.LOGGER_COMPONENT + "An error occurred while executing TLS certificate tests", e);
		} finally {
			micsChecklistTestSuite.setEndTime();
			logger.tellLogger(BasicLogger.MSG_TESTSUITE_ENDED, micsChecklistTestSuite);
		}

		return testCaseRunList;
	}

	private static TestCaseRun certificateCheck(final MICS mics, final File... certificateFiles) {
		var logger = LoggingConnector.getInstance();
		final String testCaseName = "Check Certificates for TLS_CERT";
		final String testCaseDescription = "Check Certificates for TLS_CERT";
		final String testCasePurpose = "Check whether Certificates are provided.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);
		logger.debug("X509CertificateVerifier: Checking whether Certificates are provided.");

		boolean result = true;
		// Check if (enough) certificateFiles have been provided.
		if (mics.getCertificateChain().size() > certificateFiles.length) {
			logger.error("X509CertificateVerifier: Not enough certificate files have been provided. Required: "
					+ mics.getCertificateChain().size());
			result = false;
			testRun.increaseErrorCount();
		} else {
			// Check if provided certificates are not null and exist.
			for (var file : certificateFiles) {
				if (file == null) {
					logger.error("X509CertificateVerifier: Provided certificate file is \"null\".");
					result = false;
					testRun.increaseErrorCount();
				}
				if (!file.exists() || file.isDirectory()) {
					logger.error(
							"X509CertificateVerifier: Provided certificate file is a directory or does not exists: "
									+ file.getAbsolutePath());
					result = false;
					testRun.increaseErrorCount();
				}
			}

		}

		var certFactory = new CertificateFactory();
		for (var cert : mics.getCertificateChain()) {
			boolean addedCert = false;
			for (var certFile : certificateFiles) {
				if (Arrays.areEqual(getFileFingerprint(certFile), cert.getFingerprint())) {
					try {
						var certInputStream = new FileInputStream(certFile);
						var parsedCertificate = certFactory.engineGenerateCertificate(certInputStream);
						if (parsedCertificate != null && parsedCertificate instanceof X509Certificate) {
							addedCert = true;
							break;
						}
						logger.error("X509CertificateVerifier: Unable to parse provided certificate Files. "
								+ (null != parsedCertificate ? parsedCertificate.getClass().getName() : ""));
						result = false;
						testRun.increaseErrorCount();

					} catch (Exception e) {
						logger.error("X509CertificateVerifier: Unable to parse provided certificate Files.", e);
						result = false;
						testRun.increaseErrorCount();
					}
				}
			}
			if (!addedCert) {
				logger.error(
						"X509CertificateVerifier: Unable to parse the certificate chain. Certificate missing for Subject: "
								+ cert.getSubject());
				result = false;
				testRun.increaseErrorCount();
			}
		}
		if (result) {
			logger.info(
					"X509CertificateVerifier: Certificates have been provided.");
		}


		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}


	/*
	 * Check that the public keys in the complete certificate chain are of conformant key lengths according to the
	 * application-specific requirements.
	 */
	private TestCaseRun TLS_CERT_01() {
		final String testCaseName = "TLS_CERT_01";
		final String testCaseDescription = "TLS_CERT_01 in TR-03116-TS";
		final String testCasePurpose
				= "The public keys in the complete certificate chain are of conformant key lengths according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);
		logDebug(
				"Checking that the public keys in the complete certificate chain are of conformant key lengths according to the application-specific requirements.");
		var wrongCertList = new ArrayList<String>();

		for (var cert : certificateChain) {
			try {
				Integer keyLength;
				String algorithm;
				if (cert.getPublicKey() instanceof RSAPublicKey) {
					var key = (RSAPublicKey) cert.getPublicKey();
					keyLength = key.getModulus().bitLength();
					algorithm = "RSA";
				} else if (cert.getPublicKey() instanceof ECPublicKey) {
					var key = (ECPublicKey) cert.getPublicKey();
					keyLength = key.getParams().getOrder().bitLength();
					algorithm = "ECDSA";
				} else if (cert.getPublicKey() instanceof DSAPublicKey) {
					var key = (DSAPublicKey) cert.getPublicKey();
					keyLength = key.getParams().getP().bitLength();
					algorithm = "DSA";
				} else {
					wrongCertList.add("Unable to calculate key length of unknown type: "
							+ cert.getPublicKey().getClass().getName() + " for certificate "
							+ cert.getSubjectX500Principal().getName());
					continue;
				}
				logDebug("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
						+ " has an key of  " + algorithm + " and a key length of " + keyLength + ".");
				try {
					var minKeyLength = tlsSpecification.getTlsMinimumKeyLength().get(algorithm);
					boolean keyLengthAllowed = false;
					for (var length : minKeyLength.minimumKeyLengths) {
						if (!length.useUntil.trim().endsWith("+")) {
							var useUntil = Integer.parseInt(length.useUntil.trim());
							try {
								var currentYear = Calendar.getInstance().get(Calendar.YEAR);
								if (useUntil < currentYear) {
									continue;
								}
							} catch (Exception e) {
								throw new IllegalArgumentException("Unable to query current Year.", e);
							}
						}
						if (length.minimumKeyLength <= keyLength) {
							logDebug("The key length of " + keyLength
									+ " is longer than the required length of " + length.minimumKeyLength);
							keyLengthAllowed = true;
							break;
						}
					}
					if (!keyLengthAllowed) {
						wrongCertList.add("Key length " + keyLength.toString() + " of certificate with SubjectDN "
								+ cert.getSubjectX500Principal().getName() + " does not match the requirements.");
					}
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					wrongCertList.add("No Specified minimal Key Length for algorithm: " + algorithm);
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				wrongCertList.add(
						"Unable to calculate key length of unknown type: " + cert.getPublicKey().getClass().getName()
								+ " for certificate " + cert.getSubjectX500Principal().getName());
			}
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo(
					"All public keys in the certificate chain are of conformant key lengths according to the application-specific requirements.");
			reportResult("TLS_CERT_01", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo(
					"Some public keys in the certificate chain are of non-conformant key lengths according to the application-specific requirements.");
			reportResult("TLS_CERT_01", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * The signature algorithms and hash algorithms used in the complete certificate chain are conformant according to
	 * the application-specific requirements
	 */
	private TestCaseRun TLS_CERT_02() {
		final String testCaseName = "TLS_CERT_02";
		final String testCaseDescription = "TLS_CERT_02 in TR-03116-TS";
		final String testCasePurpose
				= "The signature algorithms and hash algorithms used in the complete certificate chain are conformant according to the application-specific requirements.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"Checking that the signature algorithms and hash algorithms used in the complete certificate chain are conformant according to the application-specific requirements.");
		var wrongCertList = new ArrayList<String>();

		for (var cert : certificateChain) {
			boolean allowed = false;
			try {
				var sigAlgName = cert.getSigAlgName();
				logDebug("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
						+ " has an signature algorithm of  " + sigAlgName);
				String sigAlg;
				String hashAlg;
				if (sigAlgName.contains("RSA")) {
					sigAlg = "RSA";
				} else if (sigAlgName.contains("ECDSA")) {
					sigAlg = "ECDSA";
				} else if (sigAlgName.contains("DSA")) {
					sigAlg = "DSA";
				} else {
					wrongCertList.add("Unknown Signature Algorithm" + sigAlgName + "in certificate with SubjectDN: "
							+ cert.getSubjectX500Principal().getName());
					continue;
				}
				hashAlg = sigAlgName.split("with")[0];

				for (var tlsVersion : mics.getSupportedTlsVersions()) {
					if (tlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_2)) {
						for (var allowedSignAlg : tlsSpecification.getTlsv1_2Spec().getSignAlgorithmSupport()
								.values()) {
							if (allowedSignAlg.getDescription()
									.equalsIgnoreCase(StringHelper.combineSignAndHashAlgorithm(sigAlg, hashAlg))) {
								logDebug(
										"The signature algorithm is allowed according to the application-specific requirements.");
								allowed = true;
								break;
							}
						}
					} else if (tlsVersion.getTlsVersion().equals(TlsVersion.TLS_V1_3)) {
						for (var allowedCertSignAlg : tlsSpecification.getTlsv1_3Spec()
								.getCertificateSignAlgorithmSupport().values()) {
							if (allowedCertSignAlg.getDescription().startsWith(sigAlgName.toLowerCase())
									&& allowedCertSignAlg.getDescription().endsWith(hashAlg.toLowerCase())) {
								logDebug(
										"The signature algorithm is allowed according to the application-specific requirements.");
								allowed = true;
								break;
							}
						}
					}
					if (allowed) {
						break;
					}
				}
				if (!allowed) {
					logDebug(
							"The signature algorithm is not allowed according to the application-specific requirements.");
					wrongCertList.add("Signature Algorithm of certificate with SubjectDN "
							+ cert.getSubjectX500Principal().getName() + " is not allowed.");
				}
			} catch (Exception e) {
				wrongCertList
						.add("Unable to verify certificate with SubjectDN " + cert.getSubjectX500Principal().getName());
			}
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo(
					"All signature algorithms in the certificate chain are allowed according to the application-specific requirements.");
			reportResult("TLS_CERT_02", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo(
					"Some signature algorithms in the certificate chain are not allowed according to the application-specific requirements.");
			reportResult("TLS_CERT_02", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * None of the certificates in the chain contains any wild cards in the CommonName of the Subject or in the
	 * SubjectAltName extension.
	 */
	private TestCaseRun TLS_CERT_03() {
		final String testCaseName = "TLS_CERT_03";
		final String testCaseDescription = "TLS_CERT_03 in TR-03116-TS";
		final String testCasePurpose
				= "None of the certificates in the chain contains any wild cards in the CommonName of the Subject or in the SubjectAltName extension.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"Checking that none of the certificates in the chain contains any wildcards in the CommonName of the Subject or in the SubjectAltName extension.");
		var wrongCertList = new ArrayList<String>();

		for (var cert : certificateChain) {
			try {
				var subjectName = new JcaX509CertificateHolder(cert).getSubject();
				var commonNames = subjectName.getRDNs(BCStyle.CN);
				for (var cn : commonNames) {
					logDebug("Certificate contains CommonName: " + cn.getFirst().getValue());
					if (IETFUtils.valueToString(cn.getFirst().getValue()).contains("*")) {
						logDebug("The CommonName contains a wildcard.");
						wrongCertList.add("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
								+ " contains wildcard in CommonName.");
					} else {
						logDebug("The CommonName does not contain any wildcards.");
					}
				}

				var altNames = cert.getSubjectAlternativeNames();
				if (altNames != null) {
					for (var altName : altNames) {
						if (altName.size() == 2 && altName.get(0) instanceof Integer
								&& altName.get(1) instanceof String) {
							// var altNameType = (Integer) altName.get(0);
							var altNameValue = (String) altName.get(1);
							logDebug("The Certificate contains a SubjectAlternativeName with value: "
									+ altNameValue);
							if (altNameValue.contains("*")) {
								logDebug("The SubjectAlternativeName contains a wildcard.");
								wrongCertList
										.add("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
												+ " contains wildcard in SAN " + altNameValue);
							} else {
								logDebug(
										"The SubjectAlternativeName does not contain any wildcards.");
							}
						} else {
							wrongCertList
									.add("Unable to verify Subject Alternative Names for certificate with SubjectDN "
											+ cert.getSubjectX500Principal().getName());
						}
					}
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				wrongCertList
						.add("Unable to verify certificate with SubjectDN " + cert.getSubjectX500Principal().getName());
			}
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo(
					"None of the certificates in the chain contains any wildcards in the CommonName of the Subject or in the SubjectAltName extension.");
			reportResult("TLS_CERT_03", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo(
					"Some of the certificates in the chain contain a wildcard in the CommonName of the Subject or in the SubjectAltName extension.");
			reportResult("TLS_CERT_03", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * All certificates in the chain contain revocation information, i.e. a CRL Distribution Point extension or an
	 * AuthorityInfoAccess extension. Access to this information must also be verified (e.g. CRL retrievable, no broken
	 * links).
	 */
	private TestCaseRun TLS_CERT_04() {
		final String testCaseName = "TLS_CERT_04";
		final String testCaseDescription = "TLS_CERT_04 in TR-03116-TS";
		final String testCasePurpose
				= "All certificates in the chain contain revocation information, i.e. a CRL Distribution Point extension or an AuthorityInfoAccess extension. Access to this information must also be verified (e.g. CRL retrievable, no broken links).";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		boolean result = true;

		logDebug("Checking that all certificates in the chain contain revocation information.");
		var wrongCertList = new ArrayList<String>();

		for (var cert : certificateChain) {
			boolean verified = false;
			try {
				var cRLDistributionPoint = cert.getExtensionValue(Extension.cRLDistributionPoints.toString());
				if (cRLDistributionPoint != null) {
					var crlDistPoint = CRLDistPoint
							.getInstance(JcaX509ExtensionUtils.parseExtensionValue(cRLDistributionPoint));
					for (var distPoint : crlDistPoint.getDistributionPoints()) {
						if (distPoint.getDistributionPoint() != null) {
							var distPointName = distPoint.getDistributionPoint();
							logDebug("Certificate with SubjectDN "
									+ cert.getSubjectX500Principal().getName()
									+ "does contain a CRLDistributionPoint.");
							if (distPointName.getType() == DistributionPointName.FULL_NAME) {
								for (var generalName : GeneralNames.getInstance(distPointName.getName()).getNames()) {
									if (generalName.getTagNo() == GeneralName.uniformResourceIdentifier) {
										var url = new URL(ASN1IA5String.getInstance(generalName.getName()).getString());
										logDebug("CRLDistributionPoint contains URI instance: "
												+ url.toString());
										try {
											var connection = url.openConnection();
											connection.connect();
										} catch (Exception e) {
											logDebug("Could not verify CRLDistributionPoint.");
											continue;
										}
										logDebug("CRLDistributionPoint was verified.");
										verified = true;
										break;
									}
								}
							}
						}
					}
				}
				var authorityInfoAccess = cert.getExtensionValue(Extension.authorityInfoAccess.toString());
				if (authorityInfoAccess != null) {
					var aia = AuthorityInformationAccess
							.getInstance(JcaX509ExtensionUtils.parseExtensionValue(authorityInfoAccess));
					for (var accessDescription : aia.getAccessDescriptions()) {
						var accessMethod = accessDescription.getAccessMethod();
						if (accessMethod.equals(AccessDescription.id_ad_caIssuers)) {
							var accessLocationGN = accessDescription.getAccessLocation();
							if (accessLocationGN.getTagNo() == GeneralName.uniformResourceIdentifier) {
								var url = new URL(ASN1IA5String.getInstance(accessLocationGN.getName()).getString());
								try {
									var connection = url.openConnection();
									connection.connect();
								} catch (Exception e) {
									continue;
								}
								verified = true;
								break;
							}

						} else if (accessMethod.equals(AccessDescription.id_ad_ocsp)) {
							logDebug("Certificate with SubjectDN "
									+ cert.getSubjectX500Principal().getName()
									+ "does contain a OCSP Responder in the AIA extension.");
							var accessLocationGN = accessDescription.getAccessLocation();
							if (accessLocationGN.getTagNo() == GeneralName.uniformResourceIdentifier) {
								var url = new URL(ASN1IA5String.getInstance(accessLocationGN.getName()).getString());
								logDebug("OCSP Responder contains URI instance: " + url.toString());
								try {
									var connection = url.openConnection();
									connection.connect();
								} catch (Exception e) {
									logDebug("OCSP Responder could not be verified.");
									continue;
								}
								logDebug("OCSP Responder was verified.");
								verified = true;
								break;
							}
						} else {
							continue;
						}
					}
				}

				// } catch (Exception e) {
			} catch (IOException e) {
				result = false;
				var message = "Unable to verify certificate with SubjectDN " + cert.getSubjectX500Principal().getName();
				testRun.addStatusMessage(message);
				testRun.increaseFatalErrorCount();
				wrongCertList.add(message);
			}
			if (!verified) {
				if (cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
					var message = "No accessible revocation information available for certificate with SubjectDN "
							+ cert.getSubjectX500Principal().getName()
							+ ". Manual verification necessary to check if this is allowed.";
					testRun.addStatusMessage(message);
					testRun.increaseWarningCount();
					logInfo(message);
					wrongCertList.add(message);
				} else {
					result = false;
					var message = "No accessible revocation information available for certificate with SubjectDN "
							+ cert.getSubjectX500Principal().getName();
					testRun.addStatusMessage(message);
					testRun.increaseErrorCount();
					logInfo(message);
					wrongCertList.add(message);
				}
			}
		}

		if (wrongCertList.isEmpty()) {
			logInfo("All certificates in the chain contain revocation information.");
			reportResult("TLS_CERT_04", result);
		} else {
			logInfo("Some certificates in the chain do not contain revocation information.");
			reportResult("TLS_CERT_04", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * None of the certificates in the chain are revoked.
	 */
	private TestCaseRun TLS_CERT_05() {
		final String testCaseName = "TLS_CERT_05";
		final String testCaseDescription = "TLS_CERT_05 in TR-03116-TS";
		final String testCasePurpose = "None of the certificates in the chain are revoked";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		var wrongCertList = CertificateChecker.performCertificateRevocationCheck(certificateChain, logger);
		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo("None of the certificates in the chain are revoked.");
			reportResult("TLS_CERT_05", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo("Some of the certificates in the chain are revoked.");
			reportResult("TLS_CERT_05", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * The end entity certificate contains a keyUsage extension marked as critical with the following values: •
	 * digitalSignature=true • keyCertSign=false • cRLSign=false
	 */
	private TestCaseRun TLS_CERT_06() {
		final String testCaseName = "TLS_CERT_06";
		final String testCaseDescription = "TLS_CERT_06 in TR-03116-TS";
		final String testCasePurpose
				= "The end entity certificate contains a keyUsage extension marked as critical with the following values: digitalSignature=true, keyCertSign=false, cRLSign=false";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"Checking that the end entity certificate contains a keyUsage extension marked as critical with the following values: digitalSignature=true, keyCertSign=false, cRLSign=false.");
		var wrongCertList = new ArrayList<String>();

		var endEntityCert = certificateChain.get(0);
		try {
			// Check that KeyUsage Extension is critical.
			if (!endEntityCert.getCriticalExtensionOIDs().contains(Extension.keyUsage.toString())) {
				wrongCertList.add("KeyUsage Extension in end entity certificate is not marked critical.");
			} else {
				// Check that • digitalSignature=true • keyCertSign=false •cRLSign=false are
				// set.
				var keyUsage = endEntityCert.getKeyUsage();
				int digitalSignature = 0;
				int keyCertSign = 5;
				int cRLSign = 6;
				if (!keyUsage[digitalSignature]) {
					wrongCertList.add(
							"KeyUsage \"digitalSignature\" of end entity certificate is false, but should be true.");
				}
				if (keyUsage[keyCertSign]) {
					wrongCertList
							.add("KeyUsage \"keyCertSign\" of end entity certificate is true, but should be false.");
				}
				if (keyUsage[cRLSign]) {
					wrongCertList.add("KeyUsage \"cRLSign\" of end entity certificate is true, but should be false.");
				}
			}
		} catch (Exception e) {
			wrongCertList.add(
					"Unable to verify certificate with SubjectDN " + endEntityCert.getSubjectX500Principal().getName());
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo("End Entity Certificate contains the correct Key Usage Extension value.");
			reportResult("TLS_CERT_06", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo("End Entity Certificate does not contain the correct Key Usage Extension value.");
			reportResult("TLS_CERT_06", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * All CA certificates in the chain contain a keyUsage extension marked as critical with the following values: •
	 * keyCertSign=true • cRLSign=true
	 */
	private TestCaseRun TLS_CERT_07() {
		final String testCaseName = "TLS_CERT_07";
		final String testCaseDescription = "TLS_CERT_07 in TR-03116-TS";
		final String testCasePurpose
				= "All CA certificates in the chain contain a keyUsage extension marked as critical with the following values: keyCertSign=true, cRLSign=true";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"Checking that all CA certificates in the chain contain a keyUsage extension marked as critical with the following values: keyCertSign=true, RLSign=true.");
		var wrongCertList = new ArrayList<String>();

		for (int i = 1; i < certificateChain.size(); i++) {
			var caCert = certificateChain.get(i);
			try {
				// Check that KeyUsage Extension is critical.
				if (!caCert.getCriticalExtensionOIDs().contains(Extension.keyUsage.toString())) {
					wrongCertList.add("KeyUsage Extension in end entity certificate is not marked critical.");
				} else {
					// Check that • keyCertSign=true • cRLSign=true are set.
					var keyUsage = caCert.getKeyUsage();
					int keyCertSign = 5;
					int cRLSign = 6;
					if (!keyUsage[keyCertSign]) {
						wrongCertList.add("KeyUsage \"keyCertSign\" of CA certificate is false, but should be true.");
					}
					if (!keyUsage[cRLSign]) {
						wrongCertList.add("KeyUsage \"cRLSign\" of CA certificate is false, but should be true.");
					}
				}
			} catch (Exception e) {
				wrongCertList.add(
						"Unable to verify certificate with SubjectDN " + caCert.getSubjectX500Principal().getName());
			}
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo("All CA Certificate contains the correct Key Usage Extension value.");
			reportResult("TLS_CERT_07", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo("Some CA Certificate does not contain the correct Key Usage Extension value.");
			reportResult("TLS_CERT_07", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * The end entity certificate contains an Extended Key Usage extension with the value “id-kp-serverAuth” or
	 * “id-kp-clientAuth” respectively
	 */
	private TestCaseRun TLS_CERT_08() {
		final String testCaseName = "TLS_CERT_08";
		final String testCaseDescription = "TLS_CERT_08 in TR-03116-TS";
		final String testCasePurpose
				= "Thee end entity certificate contains an Extended Key Usage extension with the value \"id-kp-serverAuth\" or \"id-kp-clientAuth\" respectively";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"Checking that the end entity certificate contains an Extended Key Usage extension with the value \"id-kp-serverAuth\" or \"id-kp-clientAuth\" respectively.");
		var wrongCertList = new ArrayList<String>();

		var endEntityCert = certificateChain.get(0);
		try {
			// Check ExtendedKeyUsage contains "id-kp-serverAuth" or "id-kp-clientAuth".
			var extKeyUsage = endEntityCert.getExtendedKeyUsage();
			var requiredExtKeyUsage = mics.getApplicationType().contains("SERVER")
					? KeyPurposeId.id_kp_serverAuth.toString()
					: KeyPurposeId.id_kp_clientAuth.toString();

			if (!extKeyUsage.contains(requiredExtKeyUsage)) {
				wrongCertList.add(
						"ExtendedKeyUsage of end entity certificate does not contain value " + requiredExtKeyUsage);
			} else {
				logDebug("ExtendedKeyUsage of end entity certificate does contain value " + requiredExtKeyUsage);
			}

		} catch (Exception e) {
			wrongCertList.add(
					"Unable to verify certificate with SubjectDN " + endEntityCert.getSubjectX500Principal().getName());
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo(
					"End Entity Certificate contains the correct Extended Key Usage Extension value.");
			reportResult("TLS_CERT_08", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo(
					"End Entity Certificate contains the correct Extended Key Usage Extension value.");
			reportResult("TLS_CERT_08", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * In case of a TLS server certificate, it is applicable for all (sub-)domain names as stated in Table 17.
	 */
	private TestCaseRun TLS_CERT_09() {
		final String testCaseName = "TLS_CERT_09";
		final String testCaseDescription = "TLS_CERT_09 in TR-03116-TS";
		final String testCasePurpose
				= "In case of a TLS server certificate, it is applicable for all (sub-)domain names as stated in Table 17.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"In case of a TLS server certificate, checking that it is applicable for all (sub-)domain names stated in Table 17.");
		var wrongCertList = new ArrayList<String>();

		if (!mics.getApplicationType().contains("SERVER")) {
			logDebug("The MICS file is of a client type.");
			logInfo("The test case is not applicable for application type of TLS client.");
			reportResult("TLS_CERT_09", true);
			testRun.addStatusMessage("The test case not applicable for application type of TLS client.");
			testRun.setState(RunState.FINISHED);
			testRun.setStopTime(ZonedDateTime.now());
			logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);
			return testRun;
		}
		logDebug("The MICS file is of a server type.");
		var endEntityCert = certificateChain.get(0);
		try {
			var requiredDnsNames = mics.getDomainNameList();
			var altNames = endEntityCert.getSubjectAlternativeNames();
			for (var requiredDnsName : requiredDnsNames) {
				logDebug("Found required (sub-)domain name: " + requiredDnsName);
				var fulfilled = false;
				if (altNames != null) {
					for (var altName : altNames) {
						if (altName.size() == 2 && altName.get(0) instanceof Integer
								&& altName.get(1) instanceof String) {
							var altNameType = (Integer) altName.get(0);
							var altNameValue = (String) altName.get(1);
							if (altNameType.equals(2)) {
								if (requiredDnsName.equals(altNameValue)) {
									logDebug(
											"Domain name is included in Subject Alternative Name Extension.");
									fulfilled = true;
									break;
								}
							} else {
								continue;
							}

						} else {
							wrongCertList.add("Unable to verify DNS Names for certificate with SubjectDN "
									+ endEntityCert.getSubjectX500Principal().getName());
						}
					}
					if (!fulfilled) {
						logDebug(
								"Domain name is not included in Subject Alternative Name Extension.");
						wrongCertList.add(
								"Missing required DNS Name " + requiredDnsName + " for certificate with SubjectDN "
										+ endEntityCert.getSubjectX500Principal().getName());
					}
				}
			}
		} catch (Exception e) {
			wrongCertList.add("Unable to verify certificate with SubjectDN "
					+ endEntityCert.getSubjectX500Principal().getName());
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo(
					"Each (sub-)domain name listed in Table 17 is included in TLS server certificate.");
			reportResult("TLS_CERT_09", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo(
					"Some (sub-)domain name listed in Table 17 is not included in TLS server certificate.");
			reportResult("TLS_CERT_09", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * All CA certificates in the chain contain a BasicConstraints extension marked as critical. This extension must
	 * have the field “pathLenConstraint” with a reasonable small value.
	 */
	private TestCaseRun TLS_CERT_10() {
		final String testCaseName = "TLS_CERT_10";
		final String testCaseDescription = "TLS_CERT_10 in TR-03116-TS";
		final String testCasePurpose
				= "All CA certificates in the chain contain a BasicConstraints extension marked as critical. This extension must have the field \"pathLenConstraint\" with a reasonable small value.";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				"Checking that all CA certificates in the chain contain a BasicConstraints extension marked as critical. Checking that this extension has the field \"pathLenConstraint\" set, with a reasonably small value.");
		var wrongCertList = new ArrayList<String>();

		for (int i = 1; i < certificateChain.size(); i++) {
			var caCert = certificateChain.get(i);
			// Check if BasicConstraints is marked critical.
			if (!caCert.getCriticalExtensionOIDs().contains(Extension.basicConstraints.toString())) {
				logDebug(
						"BasicConstraints extension is either missing or not marked critical in certificate with SubjectDN: "
								+ caCert.getSubjectX500Principal().getName());
				var message = "BasicConstraints extension in CA certificate is not marked critical.";
				wrongCertList.add(message);
				testRun.addStatusMessage(message);
				testRun.increaseErrorCount();
			} else {
				logDebug(
						"BasicConstraints extension is present and marked critical in certificate with SubjectDN: "
								+ caCert.getSubjectX500Principal().getName());
				var basicConstraints = caCert.getBasicConstraints(); // Basically is pathLen.
				if (caCert.getSubjectX500Principal().equals(caCert.getIssuerX500Principal())) {
					if (basicConstraints == Integer.MAX_VALUE) {
						var message
								= "PathLenConstraint field in BasicConstraint not present in Root CA Certificate with SubjectDN: "
										+ caCert.getSubjectX500Principal().getName();
						logDebug(message);
						var statusMessage = "Manual verification required: " + message;
						testRun.addStatusMessage(statusMessage);
						logInfo(statusMessage);
						testRun.increaseWarningCount();
					} else {
						var message = "PathLenConstraint field in BasicConstraint present with value "
								+ basicConstraints + " in Root CA Certificate with SubjectDN: "
								+ caCert.getSubjectX500Principal().getName();
						logger.debug(message);
						var statusMessage = "Manual verification required: " + message;
						testRun.addStatusMessage(statusMessage);
						logInfo(statusMessage);
						testRun.increaseWarningCount();
					}
				} else {
					if (basicConstraints == Integer.MAX_VALUE) {
						var message
								= "PathLenConstraint field in BasicConstraint not present in Intermediate CA Certificate with SubjectDN: "
										+ caCert.getSubjectX500Principal().getName();
						logDebug(message);
						var statusMessage = "Manual verification required: " + message;
						testRun.addStatusMessage(statusMessage);
						logInfo(statusMessage);
						testRun.increaseWarningCount();
					} else {
						var message = "PathLenConstraint field in BasicConstraint present with value "
								+ basicConstraints + " in Intermediate CA Certificate with SubjectDN: "
								+ caCert.getSubjectX500Principal().getName();
						logDebug(message);
						var statusMessage = "Manual verification required: " + message;
						testRun.addStatusMessage(statusMessage);
						logInfo(statusMessage);
						testRun.increaseWarningCount();
					}
				}
			}
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo(
					"All CA certificates in the chain contain a BasicConstraints extension marked as critical.");
			reportResult("TLS_CERT_10", result);
		} else {
			logInfo(
					"Some CA certificates in the chain contain no or an invalid BasicConstraints extension.");
			reportResult("TLS_CERT_10", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * All certificates in the chain must not exceed the maximal validity duration: • For root CA certificates 6 years •
	 * For intermediate certificates 5 years • For end entity certificates 3 years
	 */
	private TestCaseRun TLS_CERT_11() {
		final String testCaseName = "TLS_CERT_11";
		final String testCaseDescription = "TLS_CERT_11 in TR-03116-TS";
		final String testCasePurpose
				= "All certificates in the chain must not exceed the maximal validity duration: For root CA certificates 6 years, For intermediate certificates 5 years, For end entity certificates 3 years";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug(
				MICSVerifier.LOGGER_COMPONENT + "Checking that all certificates in the chain must not exceed the maximal validity duration: for root CA certificates 6 years, for intermediate certificates 5 years, for end entity certificates 3 year");
		var wrongCertList = new ArrayList<String>();

		for (var cert : certificateChain) {
			var certNotBefore = Calendar.getInstance();
			certNotBefore.setTime(cert.getNotBefore());
			var certNotAfter = Calendar.getInstance();
			certNotAfter.setTime(cert.getNotAfter());
			int yearDifference = certNotAfter.get(Calendar.YEAR) - certNotBefore.get(Calendar.YEAR);
			if (certNotBefore.get(Calendar.DAY_OF_YEAR) >= certNotAfter.get(Calendar.DAY_OF_YEAR)) {
				yearDifference--;
			}
			if (cert.getBasicConstraints() >= 0) {
				// CAs
				if (cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
					// Root CA
					if (yearDifference >= 6) {
						logDebug("Validity of Root CA Certificate with SubjectDN "
								+ cert.getSubjectX500Principal().getName()
								+ " exceeds the maximal duration of 6 years.");
						wrongCertList.add(
								"Validity of Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
										+ " exceeds the maximal duration of 6 years.");
					} else {
						logDebug("Root CA has validity period of " + yearDifference + " full years.");
					}
				} else {
					// Sub CA
					if (yearDifference >= 5) {
						logDebug("Validity of Intermediate CA Certificate with SubjectDN "
								+ cert.getSubjectX500Principal().getName()
								+ " exceeds the maximal duration of 5 years.");
						wrongCertList.add(
								"Validity of Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
										+ " exceeds the maximal duration of 5 years.");
					} else {
						logDebug("Intermediate CA has validity period of " + yearDifference
								+ " full years.");
					}
				}
			} else {
				// End Entity
				if (yearDifference >= 3) {
					logDebug("Validity of end entity certificate with SubjectDN "
							+ cert.getSubjectX500Principal().getName() + " exceeds the maximal duration of 3 years.");
					wrongCertList.add("Validity of Certificate with SubjectDN "
							+ cert.getSubjectX500Principal().getName() + " exceeds the maximal duration of 3 years.");
				} else {
					logDebug("End Entity certificate has validity period of " + yearDifference
							+ " full years.");
				}
			}

		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo("All certificates in the chain do not exceed the maximal validity duration.");
			reportResult("TLS_CERT_11", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo("Some certificates in the chain exceed the maximal validity duration.");
			reportResult("TLS_CERT_11", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	/*
	 * All certificates in the chain must be valid (the current date is between the values from the notBefore and
	 * notAfter fields)
	 */
	private TestCaseRun TLS_CERT_12() {
		final String testCaseName = "TLS_CERT_12";
		final String testCaseDescription = "TLS_CERT_12 in TR-03116-TS";
		final String testCasePurpose
				= "All certificates in the chain must be valid (the current date is between the values from the notBefore and notAfter fields).";
		var testRun = new TestCaseRun(testCaseName, RunState.RUNNING, "MICS Verifier");
		logger.tellLogger(BasicLogger.MSG_NEW_TESTCASE, testRun);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_DESCRIPTION, testCaseDescription);
		logger.tellLogger(BasicLogger.MSG_TESTCASE_PURPOSE, testCasePurpose);

		logDebug("Checking that all certificates in the chain must be valid currently.");
		var wrongCertList = new ArrayList<String>();

		for (var cert : certificateChain) {
			try {
				cert.checkValidity();
				logDebug("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
						+ " is currently valid.");
			} catch (Exception e) {
				logDebug("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
						+ " is not valid currently. Certificate notBefore: " + cert.getNotBefore().toString()
						+ ". Certificate notAfter: " + cert.getNotBefore().toString() + ". Current Date: "
						+ new Date().toString());
				wrongCertList.add("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
						+ " is not valid currently.");
			}
		}

		var result = wrongCertList.isEmpty();
		if (result) {
			logInfo("All certificates in the chain are currently valid be valid.");
			reportResult("TLS_CERT_12", result);
		} else {
			for (var s : wrongCertList) {
				testRun.addStatusMessage(s);
				testRun.increaseErrorCount();
			}
			logInfo("Some certificates in the chain are not valid currently.");
			reportResult("TLS_CERT_12", result, wrongCertList);
		}

		testRun.setState(RunState.FINISHED);
		testRun.setStopTime(ZonedDateTime.now());
		logger.tellLogger(BasicLogger.MSG_TESTCASE_ENDED, testRun);

		return testRun;
	}

	private void reportResult(final String testCase, final Boolean result, final List<String> additionalInformation) {
		reportResult(testCase, result, additionalInformation.toArray(String[]::new));
	}

	private void reportResult(final String testCase, final Boolean result, final String... additionalInformation) {
		var reportMessage = getResultString(testCase, result, additionalInformation);
		reportResult(reportMessage);
	}

	private void reportResult(final String reportMessage) {
		logInfo(reportMessage);
	}

	private void logError(final String msg, final Throwable t) {
		logger.error(LOGGER_COMPONENT + msg, t);
	}

	private void logError(final String msg) {
		logger.error(LOGGER_COMPONENT + msg);
	}

	private void logWarning(final String msg) {
		logger.warning(LOGGER_COMPONENT + msg);
	}

	private void logInfo(final String msg) {
		logger.info(LOGGER_COMPONENT + msg);
	}

	private void logDebug(final String msg) {
		logger.debug(LOGGER_COMPONENT + msg);
	}

	private static String getResultString(final String testCase, final Boolean result,
			final String... additionalInformation) {
		var sb = new StringBuilder();
		sb.append("[" + testCase + "]: ");
		var resultString = result ? "Successful" : "Failure";
		sb.append(resultString);
		for (var s : additionalInformation) {
			sb.append(System.lineSeparator());
			sb.append(s);
			sb.append(";");
		}
		return sb.toString();
	}

	private static byte[] getFileFingerprint(final File fileToHash) {
		return FileUtils.getFileFingerprint(fileToHash);
	}

}
