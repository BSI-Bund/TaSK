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
#include "TlsSession.h"
#include "TlsHelper.h"
#include "TlsLogger.h"
#include "configuration/Configuration.h"
#include "logging/Logger.h"
#include "network/TcpClient.h"
#include "openssl/ssl.h"
#include "tls/TlsExportSecretsConstant.h"
#include <algorithm>
#include <chrono>
#include <cstdint>
#include <iostream>
#include <openssl/err.h>
#include <stdexcept>
#include <stdio.h>
#include <string>
#include <vector>

namespace TlsTestTool {
    namespace OpenSsl {

        static TlsSession *tlsSession = nullptr; /*we need a pointer to TlsSession object for OpenSSL callbacks*/
        static Tooling::Logger *tlsLogger = nullptr;
        static TlsSession::tlsStatusExtensionContext tlsStatusExt; /*context for status response (OCSP Stapling)*/
        typedef struct verify_options_st {
            int depth;
            int quiet;
            int error;
            int return_error;
        } VERIFY_CB_ARGS;
        VERIFY_CB_ARGS verify_args = {-1, 0, X509_V_OK, 0};

        static void
        writeToLogger(int write_p, int version, int content_type, const void *buf, size_t len, SSL *ssl,
                      void * /*arg*/) {
            bool isClient = !SSL_is_server(ssl);
            TlsLogger::logInternalTls(tlsLogger, write_p, version, content_type, buf, len, isClient);
            TlsLogger::logTls(tlsLogger, write_p, version, content_type, buf, len, isClient);
        }

        class TlsSession::Data {
        public:
            TlsSession &tlsSession;
            std::string hostname;
            uint16_t port;
            std::vector<uint8_t> clientHelloCompressionMethods;
            std::vector<unsigned char> clientHelloExtensions;
            std::vector<unsigned char> serverHelloExtensions;
            std::vector<unsigned char> encryptedExtensionsTls13;
            uint64_t sequenceNumber;
            std::vector<uint8_t> clientRandom;
            int oldSecureRenegotiation;
            bool expectAlertMessage;
            bool renegotiationStarted;
            uint32_t waitForAlertSeconds;
            bool expectingPeerFinishedMessage;
            uint32_t tlsVersions;
            std::string tlsCipherSuites;
            std::string tlsSignatureAlgorithms;
            std::string tlsDheParams;
            std::string tlsSupportedGroups;
            unsigned int currentState;
            std::string serverNameIndication;
            bool caCertificateConfigured;
            bool verifyPeer;
            SSL_CTX *ctx = nullptr;
            SSL *ssl = nullptr;
            BIO *web = nullptr;
            BIO *out = nullptr;
            std::vector<long> flags;
            bool disabledefaultExtensions;
            std::string sessionCache;
            Configuration::HandshakeType handshakeType;
            std::string pskIdentity;
            std::string pskIdentityHint;
            std::vector<uint8_t> earlyData;
            std::vector<uint8_t> preSharedKey;
            std::string ocspResponseFile;
            TlsHandshakeState handshakeState;
            std::string caCertifcatePath;
            bool extensionExtendedMasterSecret;

            explicit Data(TlsSession &newTlsSession)
                    : tlsSession(newTlsSession),
                      hostname(),
                      port(),
                      clientHelloCompressionMethods(),
                      clientHelloExtensions(),
                      sequenceNumber(0),
                      clientRandom(),
                      oldSecureRenegotiation(0),
                      expectAlertMessage(false),
                      renegotiationStarted(false),
                      waitForAlertSeconds(10),
                      expectingPeerFinishedMessage(false),
                      tlsVersions(TLS1_2_VERSION),
                      tlsCipherSuites(OPENSSL_CIPHER_SUITES_ALL),
                      tlsSignatureAlgorithms(),
                      tlsDheParams("auto"),
                      tlsSupportedGroups(OPENSSL_SUPPORTEDGROUPS_ALL),
                      currentState(0),
                      serverNameIndication(""),
                      caCertificateConfigured(false),
                      verifyPeer(false),
                      flags(),
                      disabledefaultExtensions(1),
                      sessionCache(),
                      handshakeType(Configuration::HandshakeType::NORMAL),
                      pskIdentity(""),
                      pskIdentityHint(""),
                      earlyData(),
                      preSharedKey(),
                      ocspResponseFile(""),
                      handshakeState(TlsHandshakeState::CLIENT_HELLO),
                      extensionExtendedMasterSecret(false) {
                create_context();
            }

            ~Data() {
                if (out)
                    BIO_free(out);

                if (web != NULL)
                    BIO_free_all(web);

                if (ctx != NULL)
                    SSL_CTX_free(ctx);

                if (ssl != NULL)
                    SSL_free(ssl);
            }

            SSL_CTX *create_context() {
                const SSL_METHOD *method;
                if (tlsSession.isClient()) {
                    method = TLS_client_method();
                } else {
                    method = TLS_server_method();
                }
                ctx = SSL_CTX_new(method);
                if (!ctx) {
                    perror("Unable to create SSL context");
                    ERR_print_errors_fp(stderr);
                    exit(EXIT_FAILURE);
                }
                return ctx;
            }


            void waitForExpectedAlertMessage(const bool msgWasSent) {
                if (!expectAlertMessage || !msgWasSent) {
                    return;
                }
                tlsSession.log(__FILE__, __LINE__, "Waiting for incoming data that might contain an Alert message.");
                // Wait for at most ten seconds for data to arrive
                static const std::chrono::seconds timeout(waitForAlertSeconds);
                expectAlertMessage = false;
            }


            std::vector<uint8_t> receiveApplicationData() {
                int read = 0;
                int block_size = 1024;
                std::vector<uint8_t> buffer(static_cast<std::size_t>(block_size), static_cast<uint8_t>(0));
                SSL_read(ssl, buffer.data() + read, block_size); //SSL_read
                return buffer;
            }

        };

        TlsSession::TlsSession(TcpClient &tcpClient)
                : TlsTestTool::TlsSession::TlsSession(tcpClient), impl(std::make_unique<Data>(*this)) {
            if (tlsSession) {
                throw std::runtime_error("It is only allowed to create one instance of the TLSSession");
            }
            tlsSession = this;
        }

        TlsSession::TlsSession(TcpServer &tcpServer)
                : TlsTestTool::TlsSession::TlsSession(tcpServer), impl(std::make_unique<Data>(*this)) {
            if (tlsSession) {
                throw std::runtime_error("It is only allowed to create one instance of the TLSSession");
            }
            tlsSession = this;
        }


        TlsSession::~TlsSession() = default;

        static std::string readStreamToString(std::istream &input) {
            std::stringstream buffer;
            buffer << input.rdbuf() << '\0';
            return buffer.str();
        }

        void TlsSession::setHostnameAndPort(std::string hostname, uint16_t port) {
            impl->hostname = hostname;
            impl->port = port;
        }

        void TlsSession::setCaCertificate(std::string caCertifcatePath) {
            impl->caCertifcatePath = caCertifcatePath;
        }

        void TlsSession::setCertificate(std::istream &certificate, std::istream &privateKey) {
            const auto certificateStr = readStreamToString(certificate);
            BIO *certBio = BIO_new(BIO_s_mem());
            BIO_puts(certBio, certificateStr.c_str());
            X509 *clientCertificate = NULL;
            clientCertificate = PEM_read_bio_X509(certBio, NULL, NULL, NULL);
            if (!clientCertificate) {
                throw std::runtime_error("Unable to parse the Client Certificate.");
            }
            if (SSL_CTX_use_certificate(impl->ctx, clientCertificate) != 1) {
                throw std::runtime_error("Setting the Client Certificate failed.");
            }
            X509_free(clientCertificate);
            BIO_free(certBio);


            const auto privateKeyStr = readStreamToString(privateKey);
            BIO *keyBio = BIO_new(BIO_s_mem());
            BIO_puts(keyBio, privateKeyStr.c_str());

            EVP_PKEY *privatekey = NULL;
            PEM_read_bio_PrivateKey(keyBio, &privatekey, NULL, NULL);
            if (!privatekey) {
                throw std::runtime_error("Unable to parse the Client Private Key.");
            }
            if (SSL_CTX_use_PrivateKey(impl->ctx, privatekey) != 1) {
                throw std::runtime_error("Setting the Client Certificate private Key failed.");
            }
            EVP_PKEY_free(privatekey);
            BIO_free(keyBio);
        }


        static int new_session_cb(SSL */*s*/, SSL_SESSION *sess) {
            tlsSession->log(__FILE__, __LINE__, "Callback triggered");
            BIO *stmp = BIO_new(BIO_s_mem());
            if (!stmp) {
                tlsSession->log(__FILE__, __LINE__, "Error writing session file");
            } else {
                PEM_write_bio_SSL_SESSION(stmp, sess);

                std::vector<char> buffer(10000);
                BIO_read(stmp, &buffer[0], 10000);
                std::string stringToFillIn(&buffer[0]);
                BIO_free(stmp);

                std::replace(stringToFillIn.begin(), stringToFillIn.end(), '\n', '#');
                tlsSession->log(__FILE__, __LINE__, "sessionCache=" + stringToFillIn);
            }
            return 0;
        }

        static void keylog_callback(const SSL * /*ssl*/, const char *line) {
            std::string stringVal = line;
            tlsSession->addSecretLineToKeylogfile(stringVal);
        }

        static unsigned int psk_server_cb(SSL */*ssl*/, const char */*identity*/, unsigned char *psk,
                                          unsigned int /*max_psk_len*/) {
            auto pskSession = tlsSession->getPreSharedKey();
            memcpy(psk, pskSession.data(), pskSession.size());
            return pskSession.size();
        }

        static unsigned int psk_client_cb(SSL */*ssl*/, const char * /*hint*/, char *identity,
                                          unsigned int /*max_identity_len*/,
                                          unsigned char *psk,
                                          unsigned int /*max_psk_len*/) {
            auto pskSession = tlsSession->getPreSharedKey();
            memcpy(psk, pskSession.data(), pskSession.size());
            memcpy(identity, tlsSession->getPskIdentity().c_str(), tlsSession->getPskIdentity().length());
            return pskSession.size();
        }

        /**
         * Function to read to DER file for loading the OCSPResponseFile
         * @param filename
         * @return
         */
        static BIO *openDerFile(const char *filename) {
            BIO *returnValue;
            returnValue = BIO_new_file(filename, "rb");
            if (returnValue != NULL)
                return returnValue;

            return NULL;
        }

        /**
         *
         * Certificate Status callback. This callback is triggered when a client sends a status request in his ClientHello
        */
        static int certificateStatusCallback(SSL *s, void *arg) {

            if (SSL_is_server(s)) {
                OpenSsl::TlsSession::tlsStatusExtensionContext *statusExt = static_cast<OpenSsl::TlsSession::tlsStatusExtensionContext *>(arg);
                OCSP_RESPONSE *response = NULL;
                unsigned char *responseDer = NULL;
                int responseDerLength;

                BIO *bioDerFile = openDerFile(statusExt->ocspResponseFile);
                if (bioDerFile == NULL) {
                    OCSP_RESPONSE_free(response);
                    return SSL_TLSEXT_ERR_ALERT_FATAL;
                }
                response = d2i_OCSP_RESPONSE_bio(bioDerFile, NULL);
                BIO_free(bioDerFile);
                if (response == NULL) {
                    OCSP_RESPONSE_free(response);
                    return SSL_TLSEXT_ERR_ALERT_FATAL;
                }

                responseDerLength = i2d_OCSP_RESPONSE(response, &responseDer);
                if (responseDerLength <= 0) {
                    OCSP_RESPONSE_free(response);
                    return SSL_TLSEXT_ERR_ALERT_FATAL;
                }
                SSL_set_tlsext_status_ocsp_resp(s, responseDer, responseDerLength);

                return SSL_TLSEXT_ERR_OK;
            } else {
                const unsigned char *buffer;
                int len;
                OCSP_RESPONSE *ocspResponse;
                BIO *bioOutput = tlsSession->getBioOutput();
                len = SSL_get_tlsext_status_ocsp_resp(s, &buffer);
                BIO_puts(bioOutput, "OCSP Response: ");
                if (buffer == nullptr) {
                    tlsSession->log(__FILE__, __LINE__, "The server did not send an OCSP Response\n");
                    return 1;
                }
                ocspResponse = d2i_OCSP_RESPONSE(nullptr, &buffer, len);
                if (ocspResponse == nullptr) {
                    tlsSession->log(__FILE__, __LINE__, "ERROR: OCSP Response Message could not be parsed");
                    return 0;
                }
                tlsSession->log(__FILE__, __LINE__, "OCSP Response Message received");
                tlsSession->log(__FILE__, __LINE__, "OCSP Result: " +
                                                    std::string(OCSP_response_status_str(
                                                            OCSP_response_status(ocspResponse))));

                BIO_puts(bioOutput, "\n------------------------------\n");
                OCSP_RESPONSE_print(bioOutput, ocspResponse, 0);
                BIO_puts(bioOutput, "\n------------------------------\n");
                OCSP_RESPONSE_free(ocspResponse);
                return 1;
            }
        }

        void TlsSession::configureOpensslContext() {
            bool isClient = impl->tlsSession.isClient();
            bool isServer = !isClient;

            if (isServer) {
                if (impl->verifyPeer) {
                    SSL_CTX_set_verify(impl->ctx, SSL_VERIFY_PEER, NULL);
                    if (!SSL_CTX_load_verify_file(impl->ctx, impl->caCertifcatePath.c_str())) {
                        throw std::runtime_error("Error while loading CA file SSL_CTX_load_verify_file.");
                    }
                    SSL_CTX_set_client_CA_list(impl->ctx, SSL_load_client_CA_file(impl->caCertifcatePath.c_str()));

                }
                if (impl->handshakeType == Configuration::HandshakeType::SESSION_RESUMPTION_WITH_SESSION_ID) {
                    //if server supports session ID, then we disable session tickets
                    SSL_CTX_set_options(impl->ctx, SSL_OP_NO_TICKET);
                }
                int s_server_session_id_context = 1; /* anything will do */

                SSL_CTX_set_session_id_context(impl->ctx, (const unsigned char *) &s_server_session_id_context,
                                               sizeof(s_server_session_id_context));

                if (impl->ocspResponseFile != std::string("")) {
                    tlsStatusExt.ocspResponseFile = (char *) impl->ocspResponseFile.c_str();
                    SSL_CTX_set_tlsext_status_arg(impl->ctx, &tlsStatusExt);
                    SSL_CTX_set_tlsext_status_cb(impl->ctx, certificateStatusCallback);
                }
                if (impl->serverHelloExtensions.size() > 0) {
                    char exten[impl->serverHelloExtensions.size() + 1];
                    memcpy(&exten[0], impl->serverHelloExtensions.data(), impl->serverHelloExtensions.size());
                    exten[impl->serverHelloExtensions.size()] = '\0';
                    int serverHelloVersion = SSL_EXT_TLS1_2_SERVER_HELLO;
                    if (impl->tlsVersions == TLS1_3_VERSION) {
                        serverHelloVersion = SSL_EXT_TLS1_3_SERVER_HELLO;
                    }
                    SSL_CTX_set_overwrite_client_hello_ext(impl->ctx, &exten[0], impl->serverHelloExtensions.size() + 1,
                                                           serverHelloVersion);//reinterpret_cast<char *>(impl->clientHelloExtensions.data()),impl->clientHelloExtensions.size() );
                }
                if (impl->encryptedExtensionsTls13.size() > 0) {
                    char exten[impl->encryptedExtensionsTls13.size() + 1];
                    memcpy(&exten[0], impl->encryptedExtensionsTls13.data(), impl->encryptedExtensionsTls13.size());
                    exten[impl->encryptedExtensionsTls13.size()] = '\0';
                    SSL_CTX_set_overwrite_client_hello_ext(impl->ctx, &exten[0],
                                                           impl->encryptedExtensionsTls13.size() + 1,
                                                           SSL_EXT_TLS1_3_ENCRYPTED_EXTENSIONS);//reinterpret_cast<char *>(impl->clientHelloExtensions.data()),impl->clientHelloExtensions.size() );
                }
            } else if (isClient) {
                SSL_CTX_set_tlsext_status_cb(impl->ctx, certificateStatusCallback);
                if (impl->verifyPeer && impl->caCertificateConfigured) {
                    // Verify server certificate!
                    // Since this is the default case, there is nothing to do here.
                } else {
                    // Do not verify server certificate!
                    SSL_CTX_set_verify(impl->ctx, SSL_VERIFY_NONE, NULL);
                }
                SSL_CTX_set_session_cache_mode(impl->ctx, SSL_SESS_CACHE_CLIENT
                                                          | SSL_SESS_CACHE_NO_INTERNAL_STORE);

                if (impl->clientHelloExtensions.size() > 0) {
                    char exten[impl->clientHelloExtensions.size() + 1];
                    memcpy(&exten[0], impl->clientHelloExtensions.data(), impl->clientHelloExtensions.size());
                    exten[impl->clientHelloExtensions.size()] = '\0';
                    SSL_CTX_set_overwrite_client_hello_ext(impl->ctx, &exten[0], impl->clientHelloExtensions.size() + 1,
                                                           SSL_EXT_CLIENT_HELLO);//reinterpret_cast<char *>(impl->clientHelloExtensions.data()),impl->clientHelloExtensions.size() );
                }
            }
            SSL_CTX_set_min_proto_version(impl->ctx, impl->tlsVersions);
            SSL_CTX_set_max_proto_version(impl->ctx, impl->tlsVersions);

            SSL_CTX_set_dh_auto(impl->ctx, 1);

            if (impl->tlsSupportedGroups.length() > 0) {
                log(__FILE__, __LINE__, std::string{"Set Supported groups: "} + impl->tlsSupportedGroups);
                SSL_CTX_set1_groups_list(impl->ctx, impl->tlsSupportedGroups.c_str());
            }

            if (impl->tlsSignatureAlgorithms.length() > 0) {
                log(__FILE__, __LINE__,
                    std::string{"Set Supported Signature Algorithms/Schemes: "} + impl->tlsSignatureAlgorithms);
                SSL_CTX_set1_sigalgs_list(impl->ctx, impl->tlsSignatureAlgorithms.c_str());
            }

            SSL_CTX_set_keylog_callback(impl->ctx, keylog_callback);

            SSL_CTX_set_msg_callback(impl->ctx, writeToLogger);

            /*set overwrite extensions if set*/

            //Extensions neceassray for TLS 1.2 handshake: signature algorithms
            //Extensions neceassray for TLS 1.3 handshake: supported groups, signature algorithms, supported versions

            SSL_CTX_sess_set_new_cb(impl->ctx, new_session_cb);
        }

        void TlsSession::setupSession() {
            bool isClient = impl->tlsSession.isClient();
            bool isServer = !isClient;
            int res;

            impl->ssl = SSL_new(impl->ctx);
            SSL_set_fd(impl->ssl, getSocket()->getSocketFileDesriptor());
            // since socket functionality in OpenSSL does not always work as perfectly as with MbedTLS, we set a timeout for receiving to avoid endless waiting/blocking
            struct timeval tv;
            tv.tv_sec = 5; // 5 seconds
            tv.tv_usec = 0;
            setsockopt(getSocket()->getSocketFileDesriptor(), SOL_SOCKET, SO_RCVTIMEO, (const char *) &tv, sizeof tv);

            log(__FILE__, __LINE__,
                std::string{"Set cipher suites: "} + impl->tlsCipherSuites + ":EMPTY-RENEGOTIATION-INFO-SCSV");
            if (impl->tlsVersions == TLS1_3_VERSION) {
                res = SSL_set_ciphersuites(impl->ssl, impl->tlsCipherSuites.c_str());
            } else {
                res = SSL_set_cipher_list(impl->ssl, impl->tlsCipherSuites.c_str());
            }
            if (res != 1) {
                throw std::runtime_error("Set ciphersuites failed");
            }

            if (isServer) {
            } else if (isClient) {
                //all config options (cipher+ssl_options) have to work for client+server, try to use SSL_CTX API functions
                if (!impl->sessionCache.empty()) {
                    SSL_SESSION *sess;
                    std::replace(impl->sessionCache.begin(), impl->sessionCache.end(), '#', '\n');

                    BIO *stmp = BIO_new(BIO_s_mem());
                    BIO_puts(stmp, impl->sessionCache.c_str());
                    sess = PEM_read_bio_SSL_SESSION(stmp, NULL, 0, NULL);
                    BIO_free(stmp);
                    if (!sess) {
                        throw std::runtime_error("Unable to parse the Session Cache");
                    }
                    if (!SSL_set_session(impl->ssl, sess)) {
                        log(__FILE__, __LINE__, "Error: can't set sessionCache");
                    }
                    SSL_SESSION_free(sess);
                }

                if ((impl->disabledefaultExtensions && impl->clientHelloExtensions.size() <=
                                                       0)) {/*overwrite extensions and setting flags does not work at the same time*/
                    long flags = SSL_OP_NO_COMPRESSION | SSL_OP_NO_ENCRYPT_THEN_MAC |
                                 SSL_OP_NO_EXTENDED_MASTER_SECRET;

                    //disable sessionTicket extension except we want to resume with session ticket
                    if (impl->handshakeType != Configuration::HandshakeType::SESSION_RESUMPTION_WITH_TICKET) {
                        flags |= SSL_OP_NO_TICKET;
                    }
                    SSL_set_options(impl->ssl, flags);

                }

                const char *sni = ((impl->serverNameIndication == "") ? nullptr : impl->serverNameIndication.c_str());
                res = SSL_set_tlsext_host_name(impl->ssl, sni);
                if (res != 1) {
                    throw std::runtime_error("Set sni failed");
                }
                impl->out = BIO_new_fp(stdout, BIO_NOCLOSE);

                if (impl->out == NULL) {
                    throw std::runtime_error("BIO_new_fp(stdout, BIO_NOCLOSE); failed");
                }
            }
        }


        void TlsSession::performHandshake() {
            //configure early data
            if (impl->tlsVersions == TLS1_3_VERSION && impl->handshakeType == Configuration::HandshakeType::ZERO_RTT) {
                SSL_SESSION_set_max_early_data(SSL_get0_session(impl->ssl), 16000);
                sendEarlyData(impl->earlyData);
            }
            try {
                int result = -1;
                int resultCode = -1;

                do {
                    if (isClient()) {
                        result = SSL_connect(impl->ssl);
                        resultCode = SSL_get_error(impl->ssl, result);
                    } else {
                        result = SSL_accept(impl->ssl);
                        resultCode = SSL_get_error(impl->ssl, result);
                    }
                } while ((SSL_ERROR_WANT_READ == resultCode) || (SSL_ERROR_WANT_WRITE == resultCode));

                if (result < 0) {
                    unsigned long ssl_err = ERR_get_error();
                    char *buffer = ERR_error_string(ssl_err, NULL);
                    std::string resultString = buffer;
                    log(__FILE__, __LINE__, "Openssl Handshake Error: " + resultString);
                    std::cout.flush();
                    throw std::invalid_argument(resultString);
                }
            } catch (const std::exception &) {
                impl->expectAlertMessage = true;
                impl->waitForExpectedAlertMessage(true);
                throw;
            }
            impl->handshakeState = TlsHandshakeState::HANDSHAKE_DONE;

            log(__FILE__, __LINE__, "Handshake successful.");
            std::cout.flush();
        }

        void TlsSession::performHandshakeStep() {
            throw std::runtime_error("Function performHandshakeStep not supported with OpenSSL");
        }

        void TlsSession::sendApplicationData(const std::vector<uint8_t> &data) {
            std::size_t length = data.size();
            int result;
            for (std::size_t written = 0; written < length; written += result) {
                while (0 >= (result = SSL_write(impl->ssl, data.data() + written, length - written))) {
                    if (0 > result) {
                        throw std::runtime_error("tls_write failed: " /*+ *tls_error(impl->ssl)*/);
                    }
                }
            }
            impl->waitForExpectedAlertMessage(true);
        }

        std::vector<uint8_t> TlsSession::receiveApplicationData() {
            return impl->receiveApplicationData();
        }

        void TlsSession::sendEarlyData(const std::vector<uint8_t> &data) {
            int counter = 1000;
            size_t written = 0;
            while (!SSL_write_early_data(impl->ssl, data.data(), data.size(), &written)) {
                int result = SSL_get_error(impl->ssl, 0);
                switch (result) {
                    case SSL_ERROR_WANT_WRITE:
                    case SSL_ERROR_WANT_ASYNC:
                    case SSL_ERROR_WANT_READ:
                        /* Just keep trying - busy waiting */
                        continue;
                    default:
                        counter++;
                }
                if (counter > 1000) {
                    log(__FILE__, __LINE__, "Write early data failed");
                    break;
                }
            }
        }

        void TlsSession::renegotiate() {
            int result = -1;
            int resultCode = -1;
            do {
                result = SSL_renegotiate(impl->ssl);
                resultCode = SSL_get_error(impl->ssl, result);
            } while ((SSL_ERROR_WANT_READ == resultCode) || (SSL_ERROR_WANT_WRITE == resultCode));
            if (result < 0) {
                unsigned long ssl_err = ERR_get_error();
                char *buffer = ERR_error_string(ssl_err, NULL);
                std::string resultString = buffer;
                log(__FILE__, __LINE__, "Openssl Handshake Error: " + resultString);
                std::cout.flush();
            }

            receiveApplicationData();
        }

        void TlsSession::close() {
            log(__FILE__, __LINE__, "OpenSSL final state before close: " + std::string(SSL_state_string(impl->ssl)));
            log(__FILE__, __LINE__, "Closing the TLS session.");
            if (std::string(SSL_state_string(impl->ssl)) == "SSLOK") {
                log(__FILE__, __LINE__, "No error occured after handshake finished.");
            }
            SSL_shutdown(impl->ssl);
        }

        TlsHandshakeState TlsSession::getState() const {
            return impl->handshakeState;
        }

        void TlsSession::setState(TlsHandshakeState /*manipulatedState*/) {
            log(__FILE__, __LINE__, "Please implement setState()");
        }

        TlsVersion TlsSession::getVersion() const {
            throw std::runtime_error("Function getVersion not supported with OpenSSL");
        }

        void TlsSession::setVersion(const TlsVersion &version) {
            impl->tlsVersions = getOpenSSLVersionFormat(version);
        }

        uint32_t TlsSession::getOpenSSLVersionFormat(const TlsVersion &version) {
            if (version == TLS_VERSION_SSL_3_0) {
                return SSL3_VERSION;
            } else if (version == TLS_VERSION_TLS_1_0) {
                return TLS1_VERSION;
            } else if (version == TLS_VERSION_TLS_1_1) {
                return TLS1_1_VERSION;
            } else if (version == TLS_VERSION_TLS_1_2) {
                return TLS1_2_VERSION;
            } else if (version == TLS_VERSION_TLS_1_3) {
                return TLS1_3_VERSION;
            }

            return SSL3_VERSION;
        }

        void TlsSession::setUseSni(const bool useSni, const std::string &host) {
            if (useSni) {
                impl->serverNameIndication = host;
            } else {
                impl->serverNameIndication = "";
            }
        }

        void TlsSession::setVerifyPeer(const bool verifyPeer) {
            impl->verifyPeer = verifyPeer;
        }

        void TlsSession::setCipherSuites(const std::vector<TlsCipherSuite> &cipherSuites) {
            impl->tlsCipherSuites = "";
            for (const auto &cipherSuite: cipherSuites) {
                std::string separator = ((impl->tlsCipherSuites.empty()) ? "" : ":");

                if ((cipherSuite.first == 0x00) && (cipherSuite.second == 0xff)) {
                    // EMPTY-RENEGOTIATION-INFO-SCSV is sent by default
                    continue;
                }

                try {
                    impl->tlsCipherSuites += (separator + TlsHelper::getInternalCipherSuite(cipherSuite));
                } catch (const std::exception &e) {
                    log(__FILE__, __LINE__, std::string{""} + e.what());
                }
            }
        }

        void TlsSession::setServerDHParams(const TlsDiffieHellmanGroup & /*dhGroup*/) {
            throw std::runtime_error("Function setServerDHParams not supported with OpenSSL");
        }

        void TlsSession::setEllipticCurveGroups(const std::vector<TlsEllipticCurveGroupID> & /*ellipticCurveGroups*/) {
            throw std::runtime_error("Function setEllipticCurveGroups not supported with OpenSSL");
        }

        void TlsSession::setSupportedGroups(const std::vector<TlsSupportedGroupID> &supportedGroups) {
            impl->tlsSupportedGroups = "";
            for (auto const &supportedGroup: supportedGroups) {
                std::string separator = ((impl->tlsSupportedGroups.empty()) ? "" : ":");

                try {
                    impl->tlsSupportedGroups += (separator + TlsHelper::getInternalSupportedGroup(supportedGroup));
                } catch (const std::exception &e) {
                    log(__FILE__, __LINE__, std::string{""} + e.what());
                }
            }
        }

        void TlsSession::setSignatureAlgorithms(const std::vector<TlsSignatureAndHashAlgorithm> &signatureAlgorithms) {
            impl->tlsSignatureAlgorithms = "";
            for (auto const &signatureAlgorithm: signatureAlgorithms) {
                std::string separator = ((impl->tlsSignatureAlgorithms.empty()) ? "" : ":");

                try {
                    impl->tlsSignatureAlgorithms += (separator +
                                                     TlsHelper::getInternalSignatureAlgorithm(signatureAlgorithm));
                } catch (const std::exception &e) {
                    log(__FILE__, __LINE__, std::string{""} + e.what());
                }
            }
        }

        void TlsSession::setHelloCompressionMethods(const std::vector<uint8_t> & /*clientHelloCompressionMethods*/) {
            throw std::runtime_error("Function setHelloCompressionMethods not supported with OpenSSL");
        }

        void TlsSession::setClientHelloExtensions(const std::vector<uint8_t> &clientHelloExtensions) {
            impl->clientHelloExtensions = clientHelloExtensions;
        }

        void TlsSession::setExtensionEncryptThenMac(const bool enable) {
            if (!enable) {
                impl->flags.push_back(SSL_OP_NO_ENCRYPT_THEN_MAC);
            }
        }

        void TlsSession::setExtensionExtendedMasterSecret(const bool enable) {
            impl->extensionExtendedMasterSecret = enable;
        }

        void TlsSession::overwriteHelloVersion(const TlsVersion &version) {
            SSL_CTX_set_overwrite_hello_version(impl->ctx, 1, getOpenSSLVersionFormat(version));
        }

        void TlsSession::setLogger(Tooling::Logger &logger) {
            TlsTestTool::TlsSession::setLogger(logger);
            tlsLogger = &logger;
            log(__FILE__, __LINE__, "Using patched "  OPENSSL_VERSION_TEXT  ".");
        }

        void TlsSession::setWaitForAlertSeconds(uint32_t timeout) {
            impl->waitForAlertSeconds = timeout;
        }

        void TlsSession::setEarlyData(std::vector<uint8_t> data) {
            impl->earlyData = data;
        }

        void TlsSession::setSessionCache(std::string cache) {
            impl->sessionCache = cache;
        }

        void TlsSession::forceCertificateUsage() {
            SSL_CTX_set_manipulateForceCertificateUsage(impl->ctx, true);
        }


        void TlsSession::setSignatureSchemes(const std::vector<TlsSignatureScheme> &signatureSchemes) {
            impl->tlsSignatureAlgorithms = "";
            for (auto const &signature: signatureSchemes) {
                std::string separator = ((impl->tlsSignatureAlgorithms.empty()) ? "" : ":");

                try {
                    impl->tlsSignatureAlgorithms += (separator + TlsHelper::getInternalSignatureScheme(signature));
                } catch (const std::exception &e) {
                    log(__FILE__, __LINE__, std::string{""} + e.what());
                }
            }
        }

        void TlsSession::setServerHelloExtensions(const std::vector<uint8_t> &serverHelloExtensions) {
            impl->serverHelloExtensions = serverHelloExtensions;
        }

        void TlsSession::setEncryptedExtensionsTls13(const std::vector<uint8_t> &encryptedExtensions) {
            impl->encryptedExtensionsTls13 = encryptedExtensions;
        }

        void TlsSession::setPreSharedKey(const std::vector<uint8_t> &preSharedKey, const std::string pskIdentity,
                                         const std::string pskIdentityHint) {
            impl->preSharedKey = preSharedKey;
            impl->pskIdentity = pskIdentity;
            impl->pskIdentityHint = pskIdentityHint;
            if (isClient()) {
                SSL_CTX_set_psk_client_callback(impl->ctx, psk_client_cb);
            } else {
                SSL_CTX_set_psk_server_callback(impl->ctx, psk_server_cb);
                if (!pskIdentityHint.empty()) {
                    SSL_CTX_use_psk_identity_hint(impl->ctx, pskIdentityHint.c_str());
                }
            }
        }

        void TlsSession::sendRecord(const uint8_t type, const std::vector<u_int8_t> data) {
            SSL_CTX_set_handshake_type(impl->ctx, 1, type);
            sendApplicationData(data);
        }

        void TlsSession::overwriteEllipticCurveGroup(TlsEllipticCurveGroupID /*ellipticCurve*/) {
            throw std::runtime_error("Function overwriteEllipticCurveGroup not supported with OpenSSL");
        }

        void TlsSession::setTcpReceiveTimeoutSeconds(uint32_t /*timeout*/) {
            throw std::runtime_error("Function setTcpReceiveTimeoutSeconds not supported with OpenSSL");
        }

        void TlsSession::setHandshakeType(Configuration::HandshakeType type) {
            impl->handshakeType = type;
        }

        void TlsSession::cleanSession() {
            SSL_shutdown(impl->ssl);
            SSL_free(impl->ssl);
            impl->ssl = NULL;
        }

        std::vector<u_int8_t> TlsSession::getPreSharedKey() const {
            return impl->preSharedKey;
        }

        std::string TlsSession::getPskIdentity() const {
            return impl->pskIdentity;
        }

        BIO *TlsSession::getBioOutput() const {
            return impl->out;
        }

        void TlsSession::setOcspResponderFile(std::string responderFile) {
            impl->ocspResponseFile = responderFile;
        }
    }
}