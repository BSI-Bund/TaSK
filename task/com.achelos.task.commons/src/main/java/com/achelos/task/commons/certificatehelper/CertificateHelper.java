package com.achelos.task.commons.certificatehelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import com.achelos.task.commons.tools.StringTools;


/**
 * Collection of functions to work with X.509 certificates.
 */
public final class CertificateHelper {

	/**
	 * Prevent instantiation.
	 */
	private CertificateHelper() {}


	/**
	 * Parse a hexadecimal string as a X.509 certificate.
	 *
	 * @param hexCertificateData Binary data of a X.509 certificate given as a hexadecimal string
	 * @return X.509 certificate object
	 * @throws IllegalArgumentException if certificateData cannot be parsed as a X.509 certificate
	 */
	public static X509Certificate parseData(final String hexCertificateData) {
		return parseData(StringTools.toByteArray(hexCertificateData));
	}


	/**
	 * Parse binary data as a X.509 certificate.
	 *
	 * @param certificateData Binary data of a X.509 certificate
	 * @return X.509 certificate object
	 * @throws IllegalArgumentException if certificateData cannot be parsed as a X.509 certificate
	 */
	public static X509Certificate parseData(final byte[] certificateData) {
		return parseData(new ByteArrayInputStream(certificateData));
	}


	/**
	 * Parse an input stream as a X.509 certificate.
	 *
	 * @param certificateStream Stream that will be used to read binary data of a X.509 certificate
	 * @return X.509 certificate object
	 * @throws IllegalArgumentException if certificateData cannot be parsed as a X.509 certificate
	 */
	public static X509Certificate parseData(final InputStream certificateStream) {
		Certificate certificate = null;
		try {
			certificate = CertificateFactory.getInstance("X.509").generateCertificate(certificateStream);
		} catch (CertificateException e) {
			throw new IllegalArgumentException("Input data cannot be interpreted as X.509 Certificate", e);
		}
		if (!(certificate instanceof X509Certificate)) {
			throw new IllegalArgumentException("Input data do not hold a valid X.509 Certificate");
		}
		return (X509Certificate) certificate;
	}
}
