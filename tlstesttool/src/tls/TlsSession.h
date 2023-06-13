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
#ifndef TLS_TLSSESSION_H_
#define TLS_TLSSESSION_H_

#include "tls/TlsCipherSuite.h"
#include "tls/TlsDiffieHellmanGroup.h"
#include "tls/TlsEllipticCurveGroup.h"
#include "tls/TlsHandshakeState.h"
#include "tls/TlsHashAlgorithm.h"
#include "tls/TlsSignatureAndHashAlgorithm.h"
#include "tls/TlsSignatureScheme.h"
#include "tls/TlsSupportedGroup.h"
#include "tls/TlsVersion.h"
#include "configuration/Configuration.h"
#include <cstdint>
#include <functional>
#include <iosfwd>
#include <memory>
#include <utility>
#include <vector>

namespace Tooling {
    class Logger;
}
namespace TlsTestTool {
    class TcpClient;

    class TcpServer;

    class TcpConnection;

    class TlsSession;

    using TlsCallbackFunction = std::function<void(TlsSession &)>;

/**
 * Encapsulation of structures of a TLS session.
 */
    class TlsSession {
    public:
        /**
         * Construct a TLS client session.
         * @param tcpClient Connected TCP/IP client that will be used to send and receive data.
         */
        TlsSession(TcpClient &tcpClient);

        /**
         * Construct a TLS server session.
         * @param tcpClient TCP/IP server that will be used to send and receive data.
         */
        TlsSession(TcpServer &tcpServer);

        /**
         * Free the TLS session's data.
         */
        virtual ~TlsSession();

        /**
         * Access the TCP/IP socket associated with the TLS session.
         * @return TCP/IP socket that is be used to send and receive data
         */
        std::shared_ptr<TlsTestTool::TcpConnection> getSocket();

        /**
         * Configure the CA certificate and the private key to be used by this TLS session to verify peer certificates.
         * @param caCertificate The CA certificate is read from the stream as PEM- or DER-encoded data.
         */
        virtual void setCaCertificate(std::string certifcatePath) = 0;

        /**
         * Configure the certificate and the private key to be used by this TLS session. In case this TLS session is used as
         * a server, the given certificate should be a server certificate. In case this TLS session is used as a client, the
         * given certificate should be a client certificate.
         * @param certificate The certificate is read from the stream as PEM- or DER-encoded data.
         * @param privateKey The private key is read from the stream as PEM- or DER-encoded data.
         */
        virtual void setCertificate(std::istream &certificate, std::istream &privateKey) = 0;


        /**
             * Configure the hostname and port number
             * @param hostname The hostname or IP address.
         * @param port The portnu,mber.
             */
        virtual void setHostnameAndPort(std::string hostname, uint16_t port) = 0;

        /**
         * Perform a TLS handshake. The underlying TCP/IP connection has to exist already.
         */
        virtual void performHandshake() = 0;

        /**
         * Perform a stop of the TLS handshake. The underlying TCP/IP connection has to exist already.
         */
        virtual void performHandshakeStep() = 0;

        /**
         * Send that given data as application data over the existing TLS session.
         * @param data Data to send
         */
        virtual void sendApplicationData(const std::vector<uint8_t> &data) = 0;

        /**
         * Receive  application data over the existing TLS session.
         * @return Data that has been received
         */
        virtual std::vector<uint8_t> receiveApplicationData() = 0;

        /**
         * Send that given data as early data over the existing TLS session.
         * @param data Data to send
         */
        virtual void sendEarlyData(const std::vector<uint8_t> &data) = 0;

        /**
         * Perform a TLS renegotiation.
         */
        virtual void renegotiate() = 0;

        /**
         * Close the TLS session by sending a close_notify alert.
         */
        virtual void close() = 0;

        /**
         * Return the current state of the TLS session.
         * @return Current TLS handshake state
         */
        virtual TlsHandshakeState getState() const = 0;

        /**
         * Manipulate the current state of the TLS session.
         * @param manipulatedState New TLS handshake state
         */
        virtual void setState(TlsHandshakeState manipulatedState) = 0;

        /**
         * Return the current TLS version, if negotiated.
         * @return TLS version, if negotiated. (0, 0), otherwise.
         */
        virtual TlsVersion getVersion() const = 0;

        /**
         * Restrict the TLS version that will be negotiated.
         * @param TLS version to negotiate.
         */
        virtual void setVersion(const TlsVersion &version) = 0;

        /**
         * Enable or disable the use of the TLS server_name extension.
         * @param useSni If @c true, the server_name extension is added to the ClientHello message.
         * If @c false, the ClientHello message is sent without the server_name extension.
         * @param host The DNS name.
         */
        virtual void setUseSni(const bool useSni, const std::string &host) = 0;

        /**
             * Enable or disable certificate verifciation.
             * @param verifyPeer If @c true, a valid peer certificate is required. If no valid peer certificate is presented,
             * the TLS handshake is aborted. If @c false, a peer certificate is not verified.
             */
        virtual void setVerifyPeer(const bool verifyPeer) = 0;

        /**
         * Set the list of supported TLS cipher suites.
         * @param List of TLS cipher suites
         */
        virtual void setCipherSuites(const std::vector<TlsCipherSuite> &cipherSuites) = 0;

        /**
         * Configure the Diffie-Hellman group that will be used
         * @param dhGroup Diffie-Hellmann group containing prime and generator
         */
        virtual void setServerDHParams(const TlsDiffieHellmanGroup &dhGroup) = 0;

        /**
         * Configure the elliptic curve groups that will be supported
         * @param ellipticCurveGroups List of allowed elliptic curve groups in priority order
         */
        virtual void setEllipticCurveGroups(const std::vector<TlsEllipticCurveGroupID> &ellipticCurveGroups) = 0;

        /**
         * Configure the supported groups that will be supported
         * @param supportedGroups List of allowed supported groups in priority order
         */
        virtual void setSupportedGroups(const std::vector<TlsSupportedGroupID> &supportedGroups) = 0;

        /**
         * Configure the signature schemes that will be supported
         * @param signatureSchemes List of allowed signature schemes in priority order
         */
        virtual void setSignatureSchemes(const std::vector<TlsSignatureScheme> &signatureSchemes) = 0;

        /**
             * Configure the signature algorithms that will be supported
             * @param signatureAlgorithms List of allowed signature algorithms in priority order
             */
        virtual void setSignatureAlgorithms(const std::vector<TlsSignatureAndHashAlgorithm> &signatureAlgorithms) = 0;

        /**
         * Overwrite the field ClientHello.compression_methods with a given value.
         * @param List of bytes that will be sent in ClientHello.compression_methods
         */
        virtual void setHelloCompressionMethods(const std::vector<uint8_t> &clientHelloCompressionMethods) = 0;

        /**
         * Overwrite the field ClientHello.extensions with a given value.
         * @param List of bytes that will be sent in ClientHello.extensions
         */
        virtual void setClientHelloExtensions(const std::vector<uint8_t> &clientHelloExtensions) = 0;

        /**
         * Overwrite the field ServerHello.extensions with a given value.
         * @param List of bytes that will be sent in ServerHello.extensions
         */
        virtual void setServerHelloExtensions(const std::vector<uint8_t> &serverHelloExtensions) = 0;

        /**
         * Overwrite the field ServerHello.extensions with a given value.
         * @param List of bytes that will be sent in ServerHello.extensions
         */
        virtual void setEncryptedExtensionsTls13(const std::vector<uint8_t> &encryptedExtensions) = 0;

        /**
        * set the PreShared Key
        * @param List of bytes that will be used as PSK
        */
        virtual void setPreSharedKey(const std::vector<uint8_t> &preSharedKey, const std::string pskIdentity,
                                     const std::string pskIdentityHint) = 0;

        /**
         * Configure the usage of the encrypt-then-MAC extension.
         * @param enable If @c true, it will be advertised in a Hello message. If @c false, it will not be used.
         */
        virtual void setExtensionEncryptThenMac(const bool enable) = 0;

        /**
         * Configure the usage of the Extended Master Secret extension.
         * @param enable If @c true, it will be advertised in a Hello message. If @c false, it will not be used.
         */
        virtual void setExtensionExtendedMasterSecret(const bool enable) = 0;

        /**
         * Check whether this TLS session is a TLS client or a TLS server session.
         * @return @code true, if this is a TLS client session. @code false, otherwise.
         */
        bool isClient() const;

        /**
         * Send s Tls record immediately.
         * @param msgtype the record type.
         * @param msglen the length of record data to send.
         * @param data the record data.
         */
        virtual void sendRecord(const uint8_t type, const std::vector<u_int8_t> data) = 0;

        /**
         * Overwrite negotiated elliptic curve group with the given group on server side before sending ServerKeyExchange
         * message.
         * @param ellipticCurve Value used for overwriting the field
         */
        virtual void overwriteEllipticCurveGroup(TlsEllipticCurveGroupID ellipticCurve) = 0;

        /**
         * Overwrite ClientHello.client_version or ServerHello.server_version with the given version.
         * @param version Value used for overwriting the field
         */
        virtual void overwriteHelloVersion(const TlsVersion &version) = 0;

        /**
         * Attach a logger that will be used for log output.
         * @param logger Log that will receive log entries.
         */
        virtual void setLogger(Tooling::Logger &logger);

        /**
         * Set an output stream that the master_secret in the NSS Key Log Format will be written to.
         * @param output Output stream.
         * @see https://developer.mozilla.org/en-US/docs/Mozilla/Projects/NSS/Key_Log_Format
         */
        void setSecretOutput(std::unique_ptr<std::ostream> &&output);

        /**
         * Attach a callback function that is called before a TLS handshake step is executed. For example, it can be used to
         * perform manipulations on this TLS session.
         * @param callback Callback function that receives a TLS session as parameter.
         */
        void registerPreStepCallback(TlsCallbackFunction &&callback);

        /**
         * Attach a callback function that is called after a TLS handshake step has been executed. For example, it can be
         * used to extract information from this TLS session.
         * @param callback Callback function that receives a TLS session as parameter.
         */
        void registerPostStepCallback(TlsCallbackFunction &&callback);

        /**
         * Set the time which specifies how many seconds should be waited for an alert from peer.
         * @param timeout The waiting time in seconds.
         */
        virtual void setWaitForAlertSeconds(const uint32_t timeout) = 0;

        /**
         * Set the time which specifies how many seconds should be waited for receiving TCP packets.
         * @param timeout The waiting time in seconds.
         */
        virtual void setTcpReceiveTimeoutSeconds(const uint32_t timeout) = 0;

        /**
         * Sets the early data to the TLS session that can be sent is sent by the client the 0-RTT handshake
         *
         * ONLY works for TLS 1.3
         * @param data earlyData
         */
        virtual void setEarlyData(std::vector<uint8_t> data) = 0;

        /**
         * Sets the sessionCache to the TlsSession that can be used for the client to resume the session
         * @param cache sessionCache formatted as string
         */
        virtual void setSessionCache(std::string cache) = 0;

        virtual void forceCertificateUsage() = 0;

        /**
	 * Adds a new line with a session secret to the configured KeylogFile
	 * @param line
	 */
        virtual void addSecretLineToKeylogfile(std::string &line);

        /**
         * set the handshake typ of the current TlsSession
         * @param type
         */
        virtual void setHandshakeType(Configuration::HandshakeType type) = 0;

        /**
         * sets the ocsp responder file for the server used to send the CertificateStatus
         * @param line
         */
        virtual void setOcspResponderFile(std::string line) = 0;

        /**
         * Sets Up the current TlsSession. This function is called directly before the TLS handshake of the session is executed.
         */
        virtual void setupSession() = 0;

        /**
         * Clean the current TlsSession. This function is called directly after the TLS handshake of the session is executed.
         */
        virtual void cleanSession() = 0;

        /**
         * Write a log message.
         * @param file File name of the message's origin.
         * @param line Line number of the message's origin.
         * @param message Message that should be written to the log.
         */
        void log(const std::string &file, const int line, const std::string &message);


    protected:
        /**
         * Access the logger.
         * @return The logger
         */
        Tooling::Logger &getLogger();

        /**
         * Call attached callback functions before a TLS handshake step is executed.
         */
        void onPreStep();

        /**
         * Call attached callback functions after a TLS handshake step has been executed.
         */
        void onPostStep();

        /**
         * Check whether secret information should be collected.
         * @return @code true, if secrets should be collected. @code false, otherwise.
         * @see setSecretOutput
         * @see provideSecrectInformation
         */
        bool isSecrectInformationCollected();

        /**
         * Provide the master secrect of a TLS 1.2 session.
         * @param clientRandom The 32 bytes random value of the ClientHello message. It is used to identify the TLS session.
         * @param masterSecret The 48 bytes of the TLS session's master secret.
         * @see setSecretOutput
         * @see isSecrectInformationCollected
         */
        void provideSecrectInformation(const std::vector<uint8_t> &clientRandom,
                                       const std::vector<uint8_t> &masterSecret);

        /**
         * Outputs the sessions secret for a TLS session (can be used for either TLS 1.2 or 1.3)
         *
         * OUTPUT: PREFIX||" "||CLIENT_RANDOM||" "||SECRET
         *
         * @param prefix Prefix Label which is written before clientRandom and secret
         * @param clientRandom The 32 bytes random value of the ClientHello message. It is used to identify the TLS session.
         * @param secret Session secret of current TLS session, the size of the secret depends on the used TLS version
         */
        virtual void provideSecrectInformation(const std::string prefix, const std::vector<uint8_t> &clientRandom,
                                               const std::vector<uint8_t> &secret);

    private:
        class Data;

        //! Use pimpl idiom.
        std::unique_ptr<Data> impl;
    };
}

#endif /* TLS_TLSSESSION_H_ */
