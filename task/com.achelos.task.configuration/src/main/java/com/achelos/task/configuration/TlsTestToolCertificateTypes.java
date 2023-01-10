package com.achelos.task.configuration;

/**
 * Enum representing the different possible Certificate References
 *  for Test Cases coming from chapter 4.2 of the TR-03116-TS.
 */
public enum TlsTestToolCertificateTypes {
    /**
     * A correct and valid certificate chain that matches the test domain that is
     * used in the tests. Depending on the use case, it may require a DSA, RSA or a
     * ECDSA key and different hash functions. The DUT MUST be configured to
     * accept this chain when presented.
     */
    CERT_DEFAULT("CERT_DEFAULT"),
    /**
     * A certificate chain with an end-entity certificate that matches the test
     * domain but contains an invalid signature.
     */
    CERT_INVALID_SIG("CERT_INVALID_SIG"),
    /**
     * A certificate chain with an end-entity certificate that matches the test
     * domain but is expired.
     */
    CERT_EXPIRED("CERT_EXPIRED"),
    /**
     * A certificate chain with an end-entity certificate that matches the test
     * domain but is revoked
     */
    CERT_REVOKED("CERT_REVOKED"),
    /**
     * A correct and valid certificate chain that does not match the name of the
     * test domain that is used in the tests in the Subject Alternative Name
     * Extension of the end-entity certificate.
     */
    CERT_INVALID_DOMAIN_NAME_SAN("CERT_INVALID_DOMAIN_NAME_SAN"),
    /**
     * A correct and valid certificate chain that does not match the name of the
     * test domain that is used in the tests. The certificate does not contain a
     * SubjectAltName of type dNSName, i.e. the server's identity is given in the
     * common name of the certificate.
     */
    CERT_INVALID_DOMAIN_NAME_CN("CERT_INVALID_DOMAIN_NAME_CN"),
    /**
     * A certificate chain with an end-entity certificate with a flawed encoding. In
     * particular, a byte is added to a valid certificate to break the ASN.1 structure.
     */
    CERT_INVALID_STRUCTURE("CERT_INVALID_STRUCTURE"),
    /**
     * A correct and valid certificate chain that matches the client that is used in
     * the tests. Depending on the use case, it may require a DSA, RSA or a ECDSA
     * key and different hash functions.
     */
    CERT_DEFAULT_CLIENT("CERT_DEFAULT_CLIENT"),
    /**
     * A certificate chain with an end-entity certificate that matches the client but
     * contains an invalid signature.
     */
    CERT_INVALID_SIG_CLIENT("CERT_INVALID_SIG_CLIENT"),
    /**
     * A correct and valid certificate chain that matches the test domain but with a
     * key length not conforming to application requirements (see Table 20).
     * Depending on the use case, it may contain a DSA, RSA or an ECDSA key.
     */
    CERT_SHORT_KEY("CERT_SHORT_KEY");
    private final String relativePathName;

    TlsTestToolCertificateTypes(final String relativePathName) {
        this.relativePathName = relativePathName;
    }

    /**
     * Retrieve the relative file name of the specified certificate type used by the TLS Test Tool.
     * @return the relative file name of the specified certificate type used by the TLS Test Tool.
     */
    public String getRelativePathName() {
        return relativePathName;
    }
}
