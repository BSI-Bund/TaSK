package com.achelos.task.commons.certificatehelper;

import java.io.DataInputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import com.achelos.task.commons.tools.StringTools;
import com.achelos.task.logging.LoggingConnector;


/**
 * Helper class to do checks on X.509 Certificates.
 */
public class CertificateChecker {

	private static final String OID_SUBJECT_KEY_IDENTIFIER = "2.5.29.14";

	/**
	 * Check the certificate's Subject and Subject Alternative Name for its DNS name.
	 *
	 * @param certificate X.509 certificate to check
	 * @param dnsName the certificate's DNS name
	 * @param logger Logger for output
	 * @see "BSI TR-03116-4, Section 5.1.2"
	 */
	public static void checkCertificateDnsName(final X509Certificate certificate, final String dnsName,
			final LoggingConnector logger) {
		if (null == certificate) {
			logger.error("No certificate given.");
			return;
		}
		final String searchFor = "DNS name \"" + dnsName + "\"";
		logger.debug("Search for the " + searchFor + " in the certificate's subject names.");
		boolean found = false;
		final String actualSubjectDN = certificate.getSubjectX500Principal().getName();
		if (actualSubjectDN.contains(dnsName)) {
			logger.info("Subject DN \"" + actualSubjectDN + "\" contains the " + searchFor + ".");
			found = true;
		} else {
			logger.debug("Subject DN \"" + actualSubjectDN + "\" does not contain the " + searchFor + ".");
		}
		try {
			final Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
			if (null == subjectAlternativeNames) {
				logger.debug("The certificate does not contain a Subject Alternative Name extension.");
			} else {
				logger.debug("The certificate contains a Subject Alternative Name extension.");
				for (final List<?> subjectAlternativeName : subjectAlternativeNames) {
					if (subjectAlternativeName.get(0) instanceof Integer &&
							((Integer) GeneralName.dNSName).equals(subjectAlternativeName.get(0))) {
						final String strValue = (String) subjectAlternativeName.get(1);
						if (strValue.contains(dnsName)) {
							logger.info("Subject Alternative Name extension's dNSName \"" + strValue
									+ "\" contains the " + searchFor + ".");
							found = true;
						} else {
							logger.debug("Subject Alternative Name extension's dNSName \"" + strValue
									+ "\" does not contain the " + searchFor + ".");
						}
					}
				}
			}
		} catch (final CertificateParsingException e) {
			logger.error("An error occurred while parsing the Subject Alternative Name extension", e);
		}
		if (!found) {
			logger.error("The " + searchFor
					+ " was found neither in the Subject DN nor in the Subject Alternative Name extension.");
		}
	}


	/**
	 * Checks if the certificates in the certificate chain have been revoked.
	 *
	 * @param certificateChain X.509 certificate chain to check
	 * @param logger Logger for output
	 * @return a list of failed certificate checks
	 */
	public static ArrayList<String> performCertificateRevocationCheck(final List<X509Certificate> certificateChain,
			final LoggingConnector logger) {

		logger.debug("Checking that none of the certificates in the chain are revoked.");

		var wrongCertList = new ArrayList<String>();
		for (int i = 0; i < certificateChain.size(); i++) {
			var cert = certificateChain.get(i);
			boolean revoked = false;
			try {
				var cRLDistributionPoint = cert.getExtensionValue(Extension.cRLDistributionPoints.toString());
				if (cRLDistributionPoint != null) {
					var crlDistPoint = CRLDistPoint
							.getInstance(JcaX509ExtensionUtils.parseExtensionValue(cRLDistributionPoint));
					for (var distPoint : crlDistPoint.getDistributionPoints()) {
						if (distPoint.getDistributionPoint() != null) {
							var distPointName = distPoint.getDistributionPoint();
							if (distPointName.getType() == DistributionPointName.FULL_NAME) {
								for (var generalName : GeneralNames.getInstance(distPointName.getName()).getNames()) {
									if (generalName.getTagNo() == GeneralName.uniformResourceIdentifier) {
										logger.debug("Certificate with SubjectDN "
												+ cert.getSubjectX500Principal().getName()
												+ "does contain a CRLDistributionPoint.");
										var url = new URL(ASN1IA5String.getInstance(generalName.getName()).getString());
										logger.debug("CRLDistributionPoint contains URI instance: "
												+ url.toString());
										try {
											logger.debug(
													"Trying to request CRL from CRLDistributionPoint location: "
															+ url.toString());
											var connection = url.openConnection();
											connection.connect();
											var certFactory = new CertificateFactory();
											var inputStream = new DataInputStream(connection.getInputStream());
											var crl = certFactory.engineGenerateCRL(inputStream);
											logger.debug("Successfully retrieved CRL");
											logger.debug("Checking whether certificate is on CRL.");
											revoked = crl.isRevoked(cert);
											if (revoked) {
												logger.debug("Certificate is on CRL.");
											} else {
												logger.debug("Certificate is not on CRL.");
											}
											break;

										} catch (RuntimeException e) {
											// avoid to mask severe bugs
											throw e;
										} catch (Exception e) {
											logger.debug(
													"Unable to retrieve CRL from CRLDistributionPoint location: "
															+ url.toString());
											revoked = true;
											break;
										}
									}
								}
								if (revoked) {
									break;
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
						if (!accessMethod.equals(AccessDescription.id_ad_ocsp)) {
							continue;
						}
						logger.debug("Certificate with SubjectDN "
								+ cert.getSubjectX500Principal().getName()
								+ "does contain a OCSP Responder in the AIA extension.");
						var accessLocationGN = accessDescription.getAccessLocation();
						if (accessLocationGN.getTagNo() == GeneralName.uniformResourceIdentifier) {
							var url = new URL(ASN1IA5String.getInstance(accessLocationGN.getName()).getString());
							logger.debug("OCSP Responder contains URI instance: " + url.toString());
							try {
								logger.debug("MICS Verifier: Generating OCSP request.");
								var ocspReq = generateOcspRequest(cert, certificateChain.get(i + 1));
								logger.debug("Sending OCSP Request.");
								var ocspResponse = sendOcspRequest(url, ocspReq);
								logger.debug("Verifying OCSP Response.");
								revoked = ocspResponse.getStatus() != OCSPResp.SUCCESSFUL;
								if (revoked) {
									logger.debug("OCSP Response code was not successful.");
								} else {
									logger.debug("OCSP Response code was successful.");
								}
							} catch (Exception e) {
								logger.warning(e.toString());
								revoked = true;
								break;
							}
						}
					}
				}

			} catch (RuntimeException e) {
				// avoid to mask severe bugs
				throw e;
			} catch (Exception e) {
				wrongCertList
						.add("Unable to verify certificate with SubjectDN " + cert.getSubjectX500Principal().getName());
			}
			if (revoked) {
				wrongCertList.add("Certificate with SubjectDN " + cert.getSubjectX500Principal().getName()
						+ " has been revoked.");
			}
		}
		var result = wrongCertList.isEmpty();
		if (result) {
			logger.info("None of the certificates in the chain are revoked.");
		} else {
			logger.error("Some of the certificates in the chain are revoked.");
		}
		return wrongCertList;
	}

	private static OCSPReq generateOcspRequest(final X509Certificate subject, final X509Certificate issuer) {
		var digestCalcProvBuilder = new JcaDigestCalculatorProviderBuilder();
		DigestCalculatorProvider digestCalcProv;
		try {
			digestCalcProv = digestCalcProvBuilder.build();
			var digestCalculator = digestCalcProv.get(CertificateID.HASH_SHA1);
			var id = new CertificateID(digestCalculator, new JcaX509CertificateHolder(issuer),
					subject.getSerialNumber());
			var builder = new OCSPReqBuilder();
			builder.addRequest(id);
			return builder.build();
		} catch (Exception e) {
			throw new RuntimeException("Generation of OCSP request failed.", e);
		}
	}

	private static OCSPResp sendOcspRequest(final URL url, final OCSPReq ocspReq) {
		try {
			HttpClient httpClient = HttpClient.newHttpClient();
			var requestBuilder = HttpRequest.newBuilder(url.toURI());
			requestBuilder.POST(BodyPublishers.ofByteArray(ocspReq.getEncoded()));
			requestBuilder.header("Content-Type", "application/ocsp-request");
			requestBuilder.header("Accept", "application/ocsp-response");
			var request = requestBuilder.build();
			var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() / 100 != 2) {
				throw new RuntimeException("Received non-successful HTTP response code: " + response.statusCode());
			}
			var ocspResponse = new OCSPResp(response.body());
			return ocspResponse;
		} catch (Exception e) {
			throw new RuntimeException("An error occurred while retrieving response for OCSP Request.", e);
		}
	}


	/**
	 * Perform checks on the certificate's signature.
	 *
	 * @param certificate X.509 certificate to check
	 * @param sigAlgo The signature algorithm
	 * @param logger Logger for output
	 * @see "BSI TR-03116-4, Section 5.1.4"
	 */
	public static final void assertCertificateSignature(final X509Certificate certificate,
			final TlsSignatureAlgorithmWithHash sigAlgo, final LoggingConnector logger) {
		if (null == certificate) {
			logger.error("No certificate given.");
			return;
		}
		logger.info("Performing signature checks on the certificate with Subject DN \""
				+ certificate.getSubjectX500Principal().getName() + "\".");
		final String actualHashSignatureAlgorithm = certificate.getSigAlgName();
		if (!actualHashSignatureAlgorithm.contains("with")) {
			logger.error("Unsupported actual hash and signature algorithm: " + actualHashSignatureAlgorithm);
			return;
		}
		final String[] parts = actualHashSignatureAlgorithm.split("with");
		if (2 != parts.length) {
			logger.error("Unsupported actual hash and signature algorithm: " + actualHashSignatureAlgorithm);
			return;
		}

		final String actualSignatureAlgorithm = parts[1];
		final String expectedSignatureAlgorithm = sigAlgo.getSignatureAlgorithm().name().toLowerCase();
		logger.info("Expected signature algorithm: " + expectedSignatureAlgorithm);

		if (expectedSignatureAlgorithm.equals(actualSignatureAlgorithm.toLowerCase())) {
			logger.info("Actual signature algorithm: " + actualSignatureAlgorithm.toLowerCase());
		} else {
			logger.error("Actual signature algorithm: " + actualSignatureAlgorithm.toLowerCase());
		}

		final String actualHashAlgorithm = parts[0];
		final String expectedHashAlgorithm = sigAlgo.getHashAlgorithm().name().toLowerCase();
		logger.info("Expected hash algorithm: " + expectedHashAlgorithm);

		if (expectedHashAlgorithm.equals(actualHashAlgorithm.toLowerCase())) {
			logger.info("Actual hash algorithm: " + actualHashAlgorithm.toLowerCase());
		} else {
			logger.error("Actual hash algorithm: " + actualHashAlgorithm.toLowerCase());
		}
	}


	/**
	 * Perform certified CA checks on the certificate.
	 *
	 * @param certificate X.509 certificate to check
	 * @param subject Subject
	 * @param notAfter NotAfter validity date
	 * @param notBefore NotBefore validity date
	 * @param ski Subject Key Identifier
	 * @param serialNumber Serial Number
	 * @param logger Logger for output
	 */
	public static final void assertCertifiedCA(final X509Certificate certificate,
			final String subject,
			final String notAfter,
			final String notBefore,
			final String ski,
			final String serialNumber,
			final LoggingConnector logger) {
		if (null == certificate) {
			logger.error("No certificate given.");
			return;
		}

		var dutSubject = certificate.getSubjectX500Principal().getName();

		logger.info("Performing Certified CA checks on the certificate with Subject DN \""
				+ dutSubject + "\".");

		if (subject.equals(dutSubject)) {
			logger.info("The Subject matches the ICS.");
		} else {
			logger.error("The Subject does not matches the ICS. Actual: " + dutSubject
					+ " Expected: " + subject);
		}

		var dutNotAfter = certificate.getNotAfter().toString();
		if (notAfter.equals(dutNotAfter)) {
			logger.info("The notAfter validity date matches the ICS.");
		} else {
			logger.error("The notAfter validity date does not matches the ICS. Actual: " + dutNotAfter
					+ " Expected: " + notAfter);
		}

		var dutNotBefore = certificate.getNotBefore().toString();
		if (notBefore.equals(dutNotBefore)) {
			logger.info("The notBefore validity date matches the ICS.");
		} else {
			logger.error("The notBefore validity date does not matches the ICS. Actual: " + dutNotBefore
					+ " Expected: " + notBefore);
		}

		var raw_ski = StringTools.toHexString(certificate.getExtensionValue(OID_SUBJECT_KEY_IDENTIFIER));
		var dutSki = raw_ski.substring(8); // Ignore the first 4 bytes
		if (ski.equals(dutSki)) {
			logger.info("The Subject Key Identifier matches the ICS.");
		} else {
			logger.error("The Subject Key Identifier does not matches the ICS. Actual: " + dutSki
					+ " Expected: " + ski);
		}
		var dutSerialNumber = certificate.getSerialNumber().toString(16);
		if (serialNumber.equals(dutSerialNumber)) {
			logger.info("The Serial Number matches the ICS.");
		} else {
			logger.error("The Serial Number does not matches the ICS. Actual: " + dutSerialNumber
					+ " Expected: " + serialNumber);
		}
	}


	/**
	 * Checks on the certificates have the correct signature.
	 *
	 * @param certificateChain X.509 certificate chain to check
	 * @param logger Logger for output
	 * @see "BSI TR-03116-4, Section 5.1.4"
	 */
	public static final void verifyCertificateSignature(final List<X509Certificate> certificateChain,
			final LoggingConnector logger) {

		if (certificateChain.size() < 2) {
			logger.error("At least two certificates are expected.");
			return;
		}

		// check signatures
		int invalidSignature = 0;
		for (int i = 0; i < certificateChain.size() - 1; i++) {
			var cert = certificateChain.get(i);
			var intermediateCA = certificateChain.get(i + 1);

			try {
				cert.verify(intermediateCA.getPublicKey());
				logger.debug(
						"Valid signature for certificate with SubjectDN " + cert.getSubjectX500Principal().getName());
			} catch (Exception e) {
				logger.error(
						"Invalid signature for certificate with SubjectDN " + cert.getSubjectX500Principal().getName());
				invalidSignature++;
			}
		}

		if (invalidSignature == 0) {
			logger.info("All the certificates have the correct signature.");
		}
	}


	/**
	 * Checks on the certificates have the correct validity.
	 *
	 * @param certificateChain X.509 certificate chain to check
	 * @param logger Logger for output
	 * @see "BSI TR-03116-4, Section 5.1.4"
	 */
	public static final void verifyCertificateValidity(final List<X509Certificate> certificateChain,
			final LoggingConnector logger) {

		if (certificateChain.isEmpty()) {
			logger.error("At least one certificate is expected.");
			return;
		}

		// check signatures
		int invalid = 0;
		for (var cert : certificateChain) {

			try {
				cert.checkValidity();
			} catch (CertificateNotYetValidException e) {
				logger.error("Certificate is not valid before: " + formatDate(cert.getNotBefore()));
				invalid++;
			} catch (CertificateExpiredException e) {
				logger.error("Certificate is only valid until: " + formatDate(cert.getNotAfter()));
				invalid++;
			}

		}

		if (invalid == 0) {
			logger.info("All the certificates are valid.");
		}
	}


	private static String formatDate(final Date date) {
		String formatted = DateFormat.getDateTimeInstance().format(date);
		return formatted;
	}
}
