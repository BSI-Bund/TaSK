/*
* TLS-Test Tool
* The TLS Test Tool checks the TLS configuration and compliance with the protocol specification for TLS servers and clients.
*
* Licensed under EUPL-1.2-or-later.
*
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at the LICENSE.md file or visit
*
* https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
#ifndef TLS_MBEDTLS_TLSSESSION_H_
#define TLS_MBEDTLS_TLSSESSION_H_

#include "tls/TlsSession.h"
#include <memory>

namespace TlsTestTool {
namespace MbedTls {
/**
 * Implementation of TlsSession using mbed TLS.
 */
class TlsSession : public TlsTestTool::TlsSession {
public:
	/**
	 * Construct a TLS client session.
	 * @param tcpClient Connected TCP/IP client that will be used to send and receive data.
	 */
	TlsSession(TcpClient & tcpClient);

	/**
	 * Construct a TLS server session.
	 * @param tcpClient TCP/IP server that will be used to send and receive data.
	 */
	TlsSession(TcpServer & tcpServer);

	/**
	 * Free the TLS session's data.
	 */
	virtual ~TlsSession() override;

	/**
	 * Configure the CA certificate and the private key to be used by this TLS session to verify peer certificates.
	 * @param caCertificate The CA certificate is read from the stream as PEM- or DER-encoded data.
	 */
	virtual void setCaCertificate(std::istream & caCertificate) override;

	/**
	 * Configure the certificate and the private key to be used by this TLS session. In case this TLS session is used as
	 * a server, the given certificate should be a server certificate. In case this TLS session is used as a client, the
	 * given certificate should be a client certificate.
	 * @param certificate The certificate is read from the stream as PEM- or DER-encoded data.
	 * @param privateKey The private key is read from the stream as PEM- or DER-encoded data.
	 */
	virtual void setCertificate(std::istream & certificate, std::istream & privateKey) override;

	/**
	 * Perform a TLS handshake. The underlying TCP/IP connection has to exist already.
	 */
	virtual void performHandshake() override;

	/**
	 * Perform a stop of the TLS handshake. The underlying TCP/IP connection has to exist already.
	 */
	virtual void performHandshakeStep() override;

	/**
	 * Send that given data as application data over the existing TLS session.
	 * @param data Data to send
	 */
	virtual void sendApplicationData(const std::vector<uint8_t> & data) override;

	/**
	 * Receive  application data over the existing TLS session.
	 * @return Data that has been received
	 */
	virtual std::vector<uint8_t> receiveApplicationData() override;

	/**
	 * Send that given data as early data over the existing TLS session.
	 * @param data Data to send
	 */
	virtual void sendEarlyData(const std::vector<uint8_t> & data) override;

	/**
	 * Perform a TLS renegotiation.
	 */
	virtual void renegotiate() override;

	/**
	 * Close the TLS session by sending a close_notify alert.
	 */
	virtual void close() override;

	/**
	 * Return the current state of the TLS session.
	 * @return Current TLS handshake state
	 */
	virtual TlsHandshakeState getState() const override;

	/**
	 * Manipulate the current state of the TLS session.
	 * @param manipulatedState New TLS handshake state
	 */
	void setState(TlsHandshakeState manipulatedState) override;

	/**
	 * Return the current TLS version, if negotiated.
	 * @return TLS version, if negotiated. (0, 0), otherwise.
	 */
	virtual TlsVersion getVersion() const override;

	/**
	 * Restrict the TLS version that will be negotiated.
	 * @param TLS version to negotiate.
	 */
	void setVersion(const TlsVersion & version) override;
    
    /**
	 * Configure the hostname and port number
	 * @param hostname The hostname or IP address.
     * @param port The portnu,mber.
	 */
    virtual void setHostnameAndPort(std::string hostname, uint16_t port) override;

    /**
     * Enable or disable the use of the TLS server_name extension.
     * @param useSni If @c true, the server_name extension is added to the ClientHello message.
     * @param host The DNS name.
     * Not implemented for mbed. Use the common mechanism to add extension data.
     */
    virtual void setUseSni(const bool useSni, const std::string & host) override;

    /**
	 * Enable or disable certificate verifciation.
	 * @param verifyPeer If @c true, a valid peer certificate is required. If no valid peer certificate is presented,
	 * the TLS handshake is aborted. If @c false, a peer certificate is not verified.
	 */
	virtual void setVerifyPeer(const bool verifyPeer) override;

	/**
	 * Set the list of supported TLS cipher suites.
	 * @param List of TLS cipher suites
	 */
	virtual void setCipherSuites(const std::vector<TlsCipherSuite> & cipherSuites) override;

	/**
	 * Configure the Diffie-Hellman group that will be used
	 * @param dhGroup Diffie-Hellmann group containing prime and generator
	 */
	virtual void setServerDHParams(const TlsDiffieHellmanGroup & dhGroup) override;

	/**
	 * Configure the elliptic curve groups that will be supported
	 * @param ellipticCurveGroups List of allowed elliptic curve groups in priority order
	 */
	virtual void setEllipticCurveGroups(const std::vector<TlsEllipticCurveGroupID> & ellipticCurveGroups) override;

	/**
	 * Configure the supported groups that will be supported
	 * @param supportedGroups List of allowed supported groups in priority order
	 */
	virtual void setSupportedGroups(const std::vector<TlsSupportedGroupID> & supportedGroups) override;

	/**
	 * Configure the signature schemes that will be supported
	 * @param signatureSchemes List of allowed signature schemes in priority order
	 */
	virtual void setSignatureSchemes(const std::vector<TlsSignatureScheme> & signatureSchemes) override;

    /**
	 * Configure the signature algorithms that will be supported
	 * @param signatureAlgorithms List of allowed signature algorithms in priority order
	 */
	virtual void setSignatureAlgorithms(const std::vector<TlsSignatureAndHashAlgorithm> & signatureAlgorithms) override;
    
	/**
	 * Overwrite the field ClientHello.compression_methods with a given value.
	 * @param List of bytes that will be sent in ClientHello.compression_methods
	 */
	virtual void setHelloCompressionMethods(const std::vector<uint8_t> & clientHelloCompressionMethods) override;

	/**
	 * Overwrite the field ClientHello.extensions with a given value.
	 * @param List of bytes that will be sent in ClientHello.extensions
	 */
	virtual void setClientHelloExtensions(const std::vector<uint8_t> & clientHelloExtensions) override;

    /**
     * Overwrite the field ServerHello.extensions with a given value.
     * @param List of bytes that will be sent in ServerHello.extensions
     */
    virtual void setServerHelloExtensions(const std::vector<uint8_t> & serverHelloExtensions) override;


    /**
     * set the PreShared Key
     * @param List of bytes that will be used as PSK
     */
    virtual void setPreSharedKey(const std::vector<uint8_t> & preSharedKey, const std::string pskIdentityHint) override;

	/**
	 * Configure the usage of the encrypt-then-MAC extension.
	 * @param enable If @c true, it will be advertised in a Hello message. If @c false, it will not be used.
	 */
	virtual void setExtensionEncryptThenMac(const bool enable) override;

	/**
	 * Send s Tls record immediately.
	 * @param msgtype the record type.
	 * @param msglen the length of record data to send.
	 * @param data the record data.
	 */
	virtual void sendRecord(const uint8_t type, const std::size_t msglen, const uint8_t * data) override;

	/**
	 * Overwrite negotiated elliptic curve group with the given group on server side before sending ServerKeyExchange
	 * message.
	 * @param ellipticCurve Value used for overwriting the field
	 */
	virtual void overwriteEllipticCurveGroup(TlsEllipticCurveGroupID ellipticCurve) override;

	/**
	 * Overwrite ClientHello.client_version or ServerHello.server_version with the given version.
	 * @param version Value used for overwriting the field
	 */
	virtual void overwriteHelloVersion(const TlsVersion & version) override;

	/**
	 * Attach a logger that will be used for log output.
	 * @param logger Log that will receive log entries.
	 */
	virtual void setLogger(Tooling::Logger & logger) override;

	/**
	 * Set the time which specifies how many seconds should be waited for an alert from peer.
	 * @param timeout The waiting time in seconds.
	 */
	virtual void setWaitForAlertSeconds(const uint32_t timeout) override;

	/**
	 * Set the time which specifies how many seconds should be waited for receiving TCP packets.
	 * @param timeout The waiting time in seconds.
	 */
	virtual void setTcpReceiveTimeoutSeconds(const uint32_t timeout) override;

	/**
	 * Sets the early data to the TLS session that can be sent is sent by the client the 0-RTT handshake
	 *
	 * ONLY works for TLS 1.3
	 * @param data earlyData
	 */
    virtual void setEarlyData(std::vector<uint8_t> data) override;
	/**
	 * Sets the sessionCache to the TlsSession that can be used for the client to resume the session
	 * @param cache sessionCache formatted as string
	 */
    virtual void setSessionCache(std::string cache) override;

    virtual void forceCertificateUsage() override;

        /**
             * set the handshake typ of the current TlsSession
             * @param type
             */
    virtual void setHandshakeType(Configuration::HandshakeType type) override;
    /**
         * sets the ocsp responder file for the server used to send the CertificateStatus
         * @param line
         */
    virtual void setOcspResponderFile(std::string responderFile) override;
    /**
     * Sets Up the current TlsSession. This function is called directly before the TLS handshake of the session is executed.
     */
    virtual void setupSession() override ;
    /**
     * Clean the current TlsSession. This function is called directly after the TLS handshake of the session is executed.
     */
    virtual void cleanSession() override;



private:
	class Data;
	//! Use pimpl idiom.
	std::unique_ptr<Data> impl;
};
}
}

#endif /* TLS_MBEDTLS_TLSSESSION_H_ */
