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
#include "TlsLogFilter.h"
#include "logging/Logger.h"
#include "mbedtls/ctr_drbg.h"
#include "mbedtls/debug.h"
#include "mbedtls/entropy.h"
#include "mbedtls/error.h"
#include "mbedtls/pk.h"
#include "mbedtls/pk_internal.h"
#include "mbedtls/ssl.h"
#include "mbedtls/ssl_internal.h"
#include "mbedtls/version.h"
#include "network/TcpClient.h"
#include "network/TcpServer.h"
#include "strings/HexStringHelper.h"
#include "tls/TlsMessage.h"
#include <algorithm>
#include <chrono>
#include <cstdint>
#include <sstream>
#include <stdexcept>
#include <string>
#include <vector>
#include <thread>

namespace TlsTestTool {
namespace MbedTls {

static Tooling::LogLevel convertLogLevel(int level) {
	if (3 < level) {
		return Tooling::LogLevel::LOW;
	} else if (2 < level) {
		return Tooling::LogLevel::MEDIUM;
	} else if (0 < level) {
		return Tooling::LogLevel::HIGH;
	} else {
		return Tooling::LogLevel::OFF;
	}
}

static TlsHandshakeState convertState(const int mbedTlsState) {
	switch (mbedTlsState) {
		case MBEDTLS_SSL_HELLO_REQUEST:
			return TlsHandshakeState::HELLO_REQUEST;
		case MBEDTLS_SSL_CLIENT_HELLO:
			return TlsHandshakeState::CLIENT_HELLO;
		case MBEDTLS_SSL_SERVER_HELLO:
			return TlsHandshakeState::SERVER_HELLO;
		case MBEDTLS_SSL_SERVER_CERTIFICATE:
			return TlsHandshakeState::SERVER_CERTIFICATE;
		case MBEDTLS_SSL_SERVER_KEY_EXCHANGE:
			return TlsHandshakeState::SERVER_KEY_EXCHANGE;
		case MBEDTLS_SSL_CERTIFICATE_REQUEST:
			return TlsHandshakeState::CERTIFICATE_REQUEST;
		case MBEDTLS_SSL_SERVER_HELLO_DONE:
			return TlsHandshakeState::SERVER_HELLO_DONE;
		case MBEDTLS_SSL_CLIENT_CERTIFICATE:
			return TlsHandshakeState::CLIENT_CERTIFICATE;
		case MBEDTLS_SSL_CLIENT_KEY_EXCHANGE:
			return TlsHandshakeState::CLIENT_KEY_EXCHANGE;
		case MBEDTLS_SSL_CERTIFICATE_VERIFY:
			return TlsHandshakeState::CERTIFICATE_VERIFY;
		case MBEDTLS_SSL_CLIENT_CHANGE_CIPHER_SPEC:
			return TlsHandshakeState::CLIENT_CHANGE_CIPHER_SPEC;
		case MBEDTLS_SSL_CLIENT_FINISHED:
			return TlsHandshakeState::CLIENT_FINISHED;
		case MBEDTLS_SSL_SERVER_CHANGE_CIPHER_SPEC:
			return TlsHandshakeState::SERVER_CHANGE_CIPHER_SPEC;
		case MBEDTLS_SSL_SERVER_FINISHED:
			return TlsHandshakeState::SERVER_FINISHED;
		case MBEDTLS_SSL_FLUSH_BUFFERS:
			return TlsHandshakeState::INTERNAL_1;
		case MBEDTLS_SSL_HANDSHAKE_WRAPUP:
			return TlsHandshakeState::INTERNAL_2;
		case MBEDTLS_SSL_HANDSHAKE_OVER:
			return TlsHandshakeState::HANDSHAKE_DONE;
		case MBEDTLS_SSL_SERVER_NEW_SESSION_TICKET:
		case MBEDTLS_SSL_SERVER_HELLO_VERIFY_REQUEST_SENT:
		default:
			throw std::invalid_argument{"Unsupported TLS handshake state"};
	}
}

static int convertState(const TlsHandshakeState tlsState) {
	switch (tlsState) {
		case TlsHandshakeState::HELLO_REQUEST:
			return MBEDTLS_SSL_HELLO_REQUEST;
		case TlsHandshakeState::CLIENT_HELLO:
			return MBEDTLS_SSL_CLIENT_HELLO;
		case TlsHandshakeState::SERVER_HELLO:
			return MBEDTLS_SSL_SERVER_HELLO;
		case TlsHandshakeState::SERVER_CERTIFICATE:
			return MBEDTLS_SSL_SERVER_CERTIFICATE;
		case TlsHandshakeState::SERVER_KEY_EXCHANGE:
			return MBEDTLS_SSL_SERVER_KEY_EXCHANGE;
		case TlsHandshakeState::CERTIFICATE_REQUEST:
			return MBEDTLS_SSL_CERTIFICATE_REQUEST;
		case TlsHandshakeState::SERVER_HELLO_DONE:
			return MBEDTLS_SSL_SERVER_HELLO_DONE;
		case TlsHandshakeState::CLIENT_CERTIFICATE:
			return MBEDTLS_SSL_CLIENT_CERTIFICATE;
		case TlsHandshakeState::CLIENT_KEY_EXCHANGE:
			return MBEDTLS_SSL_CLIENT_KEY_EXCHANGE;
		case TlsHandshakeState::CERTIFICATE_VERIFY:
			return MBEDTLS_SSL_CERTIFICATE_VERIFY;
		case TlsHandshakeState::CLIENT_CHANGE_CIPHER_SPEC:
			return MBEDTLS_SSL_CLIENT_CHANGE_CIPHER_SPEC;
		case TlsHandshakeState::CLIENT_FINISHED:
			return MBEDTLS_SSL_CLIENT_FINISHED;
		case TlsHandshakeState::SERVER_CHANGE_CIPHER_SPEC:
			return MBEDTLS_SSL_SERVER_CHANGE_CIPHER_SPEC;
		case TlsHandshakeState::SERVER_FINISHED:
			return MBEDTLS_SSL_SERVER_FINISHED;
		case TlsHandshakeState::INTERNAL_1:
			return MBEDTLS_SSL_FLUSH_BUFFERS;
		case TlsHandshakeState::INTERNAL_2:
			return MBEDTLS_SSL_HANDSHAKE_WRAPUP;
		case TlsHandshakeState::HANDSHAKE_DONE:
			return MBEDTLS_SSL_HANDSHAKE_OVER;
		default:
			throw std::invalid_argument{"Unsupported TLS handshake state"};
	}
}

static std::string errorToString(const int errorCode) {
	std::array<char, 1024> str;
	mbedtls_strerror(errorCode, str.data(), str.size());
	return {str.data(), str.size()};
}

static void assertSuccess(const std::string & functionName, const int result) {
	if (0 != result) {
		throw std::runtime_error(functionName + " failed: " + errorToString(result));
	}
}

static void writeToLogger(void * context, int level, const char * file, int line, const char * str) {
	auto logger = reinterpret_cast<Tooling::Logger *>(context);
	logger->log(convertLogLevel(level), "mbedTLS", file, line, str);
}

static void parseCertificate(std::istream & certificateInput, mbedtls_x509_crt * certificateOutput) {
	std::stringstream buffer;
	buffer << certificateInput.rdbuf() << '\0';
	const std::string data{buffer.str()};
	const auto result = mbedtls_x509_crt_parse(certificateOutput, reinterpret_cast<const unsigned char *>(data.c_str()),
											   data.size());
	assertSuccess("mbedtls_x509_crt_parse", result);
}

static void parsePrivateKey(std::istream & privateKeyInput, mbedtls_pk_context * privateKeyOutput) {
	std::stringstream buffer;
	buffer << privateKeyInput.rdbuf() << '\0';
	const std::string data{buffer.str()};
	const auto result = mbedtls_pk_parse_key(privateKeyOutput, reinterpret_cast<const unsigned char *>(data.c_str()),
											 data.size(), nullptr, 0);
	assertSuccess("mbedtls_pk_parse_key", result);
}

class TlsSession::Data {
public:
	TlsSession & tlsSession;
	std::vector<int> tlsCipherSuites;
	std::vector<mbedtls_ecp_group_id> tlsEllipticCurveGroups;
	std::vector<uint8_t> helloCompressionMethods;
	std::vector<uint8_t> clientHelloExtensions;
        std::vector<uint8_t> serverHelloExtensions;
        std::vector<uint8_t> preSharedKey;
	uint64_t sequenceNumber;
	std::vector<uint8_t> clientRandom;
	int oldSecureRenegotiation;
	bool expectAlertMessage;
	bool renegotiationStarted;
	uint32_t waitForAlertSeconds;
	uint32_t tcpReceiveTimeoutSeconds;
	mbedtls_ctr_drbg_context ctrDrbg;
	mbedtls_entropy_context entropy;
	mbedtls_ssl_config conf;
	mbedtls_ssl_context ssl;
	mbedtls_x509_crt caCertificate;
	mbedtls_x509_crt certificate;
	mbedtls_pk_context privateKey;
	bool disableDefaultExtensions;

	Data(TlsSession & newTlsSession)
			: tlsSession(newTlsSession),
			  tlsCipherSuites(),
			  tlsEllipticCurveGroups(),
			  helloCompressionMethods(),
			  clientHelloExtensions(),
                          serverHelloExtensions(),
                          preSharedKey(),
			  sequenceNumber(0),
			  clientRandom(),
			  oldSecureRenegotiation(0),
			  expectAlertMessage(false),
			  renegotiationStarted(false),
			  waitForAlertSeconds(10),
			  tcpReceiveTimeoutSeconds(120),
			  disableDefaultExtensions(true){
		mbedtls_ctr_drbg_init(&ctrDrbg);
		mbedtls_entropy_init(&entropy);
		mbedtls_ssl_init(&ssl);
		mbedtls_ssl_config_init(&conf);

		const std::string pers{"tls_test_tool"};
		assertSuccess("mbedtls_ctr_drbg_seed",
					  mbedtls_ctr_drbg_seed(&ctrDrbg, mbedtls_entropy_func, &entropy,
											reinterpret_cast<const unsigned char *>(pers.c_str()), pers.length()));

		assertSuccess("mbedtls_ssl_config_defaults",
					  mbedtls_ssl_config_defaults(&conf,
												  tlsSession.isClient() ? MBEDTLS_SSL_IS_CLIENT : MBEDTLS_SSL_IS_SERVER,
												  MBEDTLS_SSL_TRANSPORT_STREAM, MBEDTLS_SSL_PRESET_DEFAULT));

		mbedtls_ssl_conf_rng(&conf, mbedtls_ctr_drbg_random, &ctrDrbg);
		mbedtls_ssl_conf_authmode(&conf, MBEDTLS_SSL_VERIFY_NONE);
		if(disableDefaultExtensions) {
			mbedtls_ssl_conf_session_tickets(&conf, MBEDTLS_SSL_SESSION_TICKETS_DISABLED);
			mbedtls_ssl_conf_extended_master_secret(&conf, MBEDTLS_SSL_EXTENDED_MS_DISABLED);
			mbedtls_ssl_conf_encrypt_then_mac(&conf, MBEDTLS_SSL_ETM_DISABLED);
		}

		assertSuccess("mbedtls_ssl_setup", mbedtls_ssl_setup(&ssl, &conf));

		mbedtls_ssl_set_bio(&ssl, this, tcpSend, tcpReceive, nullptr);

		mbedtls_x509_crt_init(&caCertificate);
		mbedtls_x509_crt_init(&certificate);
		mbedtls_pk_init(&privateKey);

	}

	~Data() {
		mbedtls_pk_free(&privateKey);
		mbedtls_x509_crt_free(&certificate);
		mbedtls_x509_crt_free(&caCertificate);

		mbedtls_ssl_config_free(&conf);
		mbedtls_ssl_free(&ssl);
		mbedtls_entropy_free(&entropy);
		mbedtls_ctr_drbg_free(&ctrDrbg);
	}

	bool isDataReadable() {
		return (0 < tlsSession.getSocket()->available());
	}

	void tryToReadAlertMessage(bool forceRead) {
		// Prevent Alert from being ignored that are received before sending a ServerHello
		const auto oldMinorVersion = conf.max_minor_ver;
		conf.max_minor_ver = MBEDTLS_SSL_MINOR_VERSION_3;
		while (isDataReadable()) {
			tlsSession.log(__FILE__, __LINE__, "Checking for Alert message in received data.");
			const auto result = mbedtls_ssl_fetch_input(&ssl, 5);
			const bool msgHeaderIndicatesAlert = (0 == result) && (MBEDTLS_SSL_MSG_ALERT == ssl.in_hdr[0]);
			if (msgHeaderIndicatesAlert || forceRead) {
				const auto result = mbedtls_ssl_read_record(&ssl);
				if (MBEDTLS_ERR_SSL_FATAL_ALERT_MESSAGE == result) {
					tlsSession.log(__FILE__, __LINE__, "Fatal Alert message received.");
					break;
				} else if (MBEDTLS_ERR_SSL_INVALID_RECORD == result) {
					tlsSession.log(__FILE__, __LINE__, "Invalid TLS record received.");
					tlsSession.log(__FILE__, __LINE__, "Stop searching for Alert message.");
					break;
				}
			} else if ((0 == result) && (MBEDTLS_SSL_MSG_APPLICATION_DATA == ssl.in_hdr[0])) {
				tlsSession.log(__FILE__, __LINE__, "Skipping application data in received data.");
				receiveApplicationData();
			} else {
				break;
			}
		}
		conf.max_minor_ver = oldMinorVersion;
	}

	void waitForExpectedAlertMessage(const bool msgWasSent) {
		if (!expectAlertMessage || !msgWasSent) {
			return;
		}
		tlsSession.log(__FILE__, __LINE__, "Waiting for incoming data that might contain an Alert message.");
		// Wait for at most ten seconds for data to arrive
		static const std::chrono::seconds timeout(waitForAlertSeconds);
		const auto timeStart = std::chrono::steady_clock::now();
		while (!isDataReadable()) {
			if (timeout < (std::chrono::steady_clock::now() - timeStart)) {
				break;
			}
			// Take small breaks to save resources.
			std::this_thread::sleep_for(std::chrono::milliseconds(20));
		}
		tryToReadAlertMessage(false);
		expectAlertMessage = false;
	}

	std::vector<uint8_t> receiveApplicationData() {
		int read = 0;
		int block_size = 1024;
		int result;
		std::vector<uint8_t> buffer(static_cast<std::size_t>(block_size), static_cast<uint8_t>(0));

		while (true) {
			result = mbedtls_ssl_read(&ssl, buffer.data() + read, block_size);
			if ((MBEDTLS_ERR_SSL_WANT_READ == result) || (MBEDTLS_ERR_SSL_WANT_WRITE == result)) {
				continue;
			}

			if (MBEDTLS_ERR_SSL_PEER_CLOSE_NOTIFY == result && 0 < read) {
				tlsSession.log(__FILE__, __LINE__, "Connection was closed gracefully.");
			} else if (MBEDTLS_ERR_SSL_PEER_CLOSE_NOTIFY == result) {
				throw std::runtime_error("connection was closed gracefully.");
			} else if (0 == result && 0 < read) {
				tlsSession.log(__FILE__, __LINE__, "Connection was reset by peer.");
			} else if (0 == result) {
				throw std::runtime_error("connection was reset by peer.");
			} else if (0 > result) {
				throw std::runtime_error("mbedtls_ssl_read failed: " + errorToString(result));
			} else {
				read += result;
			}

			// check if more data is available
			if (block_size == result) {
				buffer.resize(read + block_size);
				continue;
			}

			buffer.resize(read);
			return buffer;
		}
	}

	static int tcpSend(void * context, const unsigned char * data, size_t size) {
		TlsSession::Data * dataContainer = reinterpret_cast<TlsSession::Data *>(context);
		if (dataContainer->renegotiationStarted && !dataContainer->tlsSession.isClient()) {
			dataContainer->tryToReadAlertMessage(false);
		}
		std::shared_ptr<TcpConnection> connection = dataContainer->tlsSession.getSocket();
		return connection->write({reinterpret_cast<const char *>(data), reinterpret_cast<const char *>(data) + size});
	}

	static int tcpReceive(void * context, unsigned char * data, size_t size) {
		TlsSession::Data * dataContainer = reinterpret_cast<TlsSession::Data *>(context);
		std::shared_ptr<TcpConnection> connection = dataContainer->tlsSession.getSocket();
		static const std::chrono::seconds receiveTimeout(dataContainer->tcpReceiveTimeoutSeconds);
		const auto timeStart = std::chrono::steady_clock::now();
		while (0 == connection->available()) {
			if (receiveTimeout < (std::chrono::steady_clock::now() - timeStart)) {
				return MBEDTLS_ERR_SSL_TIMEOUT;
			}
			/* Perform two checks here to circumvent a timing problem in isClosed where isReadable and nothingToRead are
			 * both true when data is incoming in multiple TCP fragments */
			if (dataContainer->tlsSession.isClient()) {
				if (connection->isClosed()) {
					return MBEDTLS_ERR_SSL_CONN_EOF;
				}
			} else {
				if (connection->isClosed() && connection->isClosed()) {
					return MBEDTLS_ERR_SSL_CONN_EOF;
				}
			}
			// Take small breaks to save resources.
			std::this_thread::sleep_for(std::chrono::milliseconds(20));
		}
		const auto receivedData = connection->read(std::min(connection->available(), size));
		std::copy(receivedData.begin(), receivedData.end(), data);
		return receivedData.size();
	}
};

TlsSession::TlsSession(TcpClient & tcpClient)
		: TlsTestTool::TlsSession::TlsSession(tcpClient), impl(std::make_unique<Data>(*this)) {
}

TlsSession::TlsSession(TcpServer & tcpServer)
		: TlsTestTool::TlsSession::TlsSession(tcpServer), impl(std::make_unique<Data>(*this)) {
}

TlsSession::~TlsSession() = default;

void TlsSession::setCaCertificate(std::istream & caCertificate) {
	parseCertificate(caCertificate, &impl->caCertificate);
	mbedtls_ssl_conf_ca_chain(&impl->conf, &impl->caCertificate, nullptr);
}

void TlsSession::setCertificate(std::istream & certificate, std::istream & privateKey) {
	parseCertificate(certificate, &impl->certificate);
	parsePrivateKey(privateKey, &impl->privateKey);
	const auto result = mbedtls_ssl_conf_own_cert(&impl->conf, &impl->certificate, &impl->privateKey);
	assertSuccess("mbedtls_ssl_conf_own_cert", result);
}

void TlsSession::performHandshake() {



        try {
		while (TlsHandshakeState::HANDSHAKE_DONE != getState()) {
			performHandshakeStep();
            if (getSocket()->isClosed()) {
				log(__FILE__, __LINE__, "Handshake aborted.");
				return;
			}
		}
	} catch (const std::exception &) {
		impl->expectAlertMessage = true;
		impl->waitForExpectedAlertMessage(true);
		throw;
	}
	log(__FILE__, __LINE__, "Handshake successful.");
	log(__FILE__, __LINE__, std::string{"Protocol: "} + mbedtls_ssl_get_version(&impl->ssl));
	log(__FILE__, __LINE__, std::string{"Cipher suite: "} + mbedtls_ssl_get_ciphersuite(&impl->ssl));
}

void TlsSession::performHandshakeStep() {
	onPreStep();
	const auto currentState = getState();
	bool expectingPeerFinishedMessage = false;
	if ((isClient() && (TlsHandshakeState::SERVER_FINISHED == currentState))
		|| (!isClient() && (TlsHandshakeState::CLIENT_FINISHED == currentState))) {
		expectingPeerFinishedMessage = true;
	}
	if (isSecrectInformationCollected() && (TlsHandshakeState::CLIENT_KEY_EXCHANGE == currentState)) {
		const auto randomPtr = impl->ssl.handshake->randbytes;
		const auto randomLen = sizeof(impl->ssl.handshake->randbytes);
		impl->clientRandom = std::vector<uint8_t>(randomPtr, randomPtr + randomLen / 2);
	}
	if (TlsMessage::isSent(isClient(), currentState)) {
		impl->tryToReadAlertMessage(false);
	}

	int result;
	while (0 != (result = mbedtls_ssl_handshake_step(&impl->ssl))) {
		if ((MBEDTLS_ERR_SSL_WANT_READ != result) && (MBEDTLS_ERR_SSL_WANT_WRITE != result)) {
			impl->tryToReadAlertMessage(true);
			throw std::runtime_error("mbedtls_ssl_handshake_step failed: " + errorToString(result));
		}
	}
	if (expectingPeerFinishedMessage) {
		const auto ivLen = impl->ssl.transform_in->ivlen;
		const auto ivPtr = impl->ssl.transform_in->iv_dec;
		const auto ivStr = Tooling::HexStringHelper::byteArrayToHexString({ivPtr, ivPtr + ivLen});
		log(__FILE__, __LINE__, std::string{"Finished.GenericBlockCipher.IV="} + ivStr);
	}
	if (isSecrectInformationCollected() && !impl->clientRandom.empty() && expectingPeerFinishedMessage) {
		const auto masterPtr = impl->ssl.session_negotiate->master;
		const auto masterLen = sizeof(impl->ssl.session_negotiate->master);
		provideSecrectInformation(impl->clientRandom, {masterPtr, masterPtr + masterLen});
	}
	impl->waitForExpectedAlertMessage(TlsMessage::isSent(isClient(), currentState));
	onPostStep();
}

void TlsSession::sendApplicationData(const std::vector<uint8_t> & data) {
	std::size_t length = data.size();
	int result;
	for (std::size_t written = 0; written < length; written += result) {
		while (0 >= (result = mbedtls_ssl_write(&impl->ssl, data.data() + written, length - written))) {
			if ((MBEDTLS_ERR_SSL_WANT_READ != result) && (MBEDTLS_ERR_SSL_WANT_WRITE != result)) {
				throw std::runtime_error("mbedtls_ssl_write failed: " + errorToString(result));
			}
		}
	}
	impl->waitForExpectedAlertMessage(true);
}

std::vector<uint8_t> TlsSession::receiveApplicationData() {
	return impl->receiveApplicationData();
}

void TlsSession::sendEarlyData(const std::vector<uint8_t> & /*data*/) {
	throw std::runtime_error("Function not supported with mbed TLS");
}

void TlsSession::renegotiate() {


	/* if overwrite extensions is used, then the renegotiation extension is overwritten in the renegotiation.
	 * Thus, we need to add the renegtioation info extension manually*/
	if(impl->clientHelloExtensions.size()>0){
		std::vector<uint8_t> renegInfoExt = {0xFF, 0x01, 0x00};

		std::vector<uint8_t> renegInfoExtSizes = { };
		renegInfoExtSizes.push_back(impl->ssl.verify_data_len + 1);
		renegInfoExtSizes.push_back(impl->ssl.verify_data_len);

		impl->clientHelloExtensions.insert(impl->clientHelloExtensions.end(), renegInfoExt.begin(), renegInfoExt.end());
		impl->clientHelloExtensions.insert(impl->clientHelloExtensions.end(), renegInfoExtSizes.begin(), renegInfoExtSizes.end());
		impl->clientHelloExtensions.insert(impl->clientHelloExtensions.end() , impl->ssl.own_verify_data, impl->ssl.own_verify_data + impl->ssl.verify_data_len);

		setClientHelloExtensions(impl->clientHelloExtensions);
	}

	try {
		log(__FILE__, __LINE__, "Reading data before performing renegotiation.");
		impl->expectAlertMessage = true;
		impl->waitForExpectedAlertMessage(true);
	} catch (const std::exception &) {
	}
	mbedtls_ssl_conf_renegotiation(&impl->conf, MBEDTLS_SSL_RENEGOTIATION_ENABLED);
	impl->renegotiationStarted = true;
	int result;
	while (0 != (result = mbedtls_ssl_renegotiate(&impl->ssl))) {
		if ((MBEDTLS_ERR_SSL_WANT_READ != result) && (MBEDTLS_ERR_SSL_WANT_WRITE != result)) {
			impl->tryToReadAlertMessage(true);
			throw std::runtime_error("mbedtls_ssl_renegotiate failed: " + errorToString(result));
		}
	}
}

void TlsSession::close() {
	log(__FILE__, __LINE__, "Closing the TLS session.");
	static const std::chrono::seconds timeout(3);
	const auto timeStart = std::chrono::steady_clock::now();

	while (!getSocket()->isClosed()) {
        impl->tryToReadAlertMessage(true);
		if (timeout < (std::chrono::steady_clock::now() - timeStart)) {
			break;
		}
	}

	int result;
	do {
		result = mbedtls_ssl_close_notify(&impl->ssl);
	} while (MBEDTLS_ERR_SSL_WANT_WRITE == result);
	impl->tryToReadAlertMessage(true);
}

TlsHandshakeState TlsSession::getState() const {
	return convertState(impl->ssl.state);
}

void TlsSession::setState(TlsHandshakeState manipulatedState) {
	impl->ssl.state = convertState(manipulatedState);
	impl->expectAlertMessage = true;
}

TlsVersion TlsSession::getVersion() const {
	return std::make_pair(impl->ssl.major_ver, impl->ssl.minor_ver);
}

void TlsSession::setVersion(const TlsVersion & version) {
	mbedtls_ssl_conf_min_version(&impl->conf, version.first, version.second);
	mbedtls_ssl_conf_max_version(&impl->conf, version.first, version.second);
}

void TlsSession::setUseSni(const bool useSni, const std::string & host) {
    if (useSni) {
        log(__FILE__, __LINE__, "Using SNI.");
        assertSuccess("mbedtls_ssl_set_hostname", mbedtls_ssl_set_hostname(&impl->ssl, host.c_str()));
    } else {
        log(__FILE__, __LINE__, "Not using SNI.");
    }
    return;
}

void TlsSession::setVerifyPeer(const bool verifyPeer) {
    mbedtls_ssl_conf_authmode(&impl->conf, verifyPeer ? MBEDTLS_SSL_VERIFY_REQUIRED : MBEDTLS_SSL_VERIFY_NONE);
}

void TlsSession::setCipherSuites(const std::vector<TlsCipherSuite> & cipherSuites) {
	impl->tlsCipherSuites.resize(cipherSuites.size() + 1);
	std::transform(cipherSuites.cbegin(), cipherSuites.cend(), impl->tlsCipherSuites.begin(),
				   [](const TlsCipherSuite & cipherSuite) { return (cipherSuite.first << 8) | cipherSuite.second; });
	// Zero terminator as last element
	impl->tlsCipherSuites.back() = 0;
	mbedtls_ssl_conf_ciphersuites(&impl->conf, impl->tlsCipherSuites.data());
}

void TlsSession::setServerDHParams(const TlsDiffieHellmanGroup & dhGroup) {
	mbedtls_ssl_conf_dh_param(&impl->conf, dhGroup.getPrime(), dhGroup.getGenerator());
}

void TlsSession::setEllipticCurveGroups(const std::vector<TlsEllipticCurveGroupID> & ellipticCurveGroups) {
	impl->tlsEllipticCurveGroups.resize(ellipticCurveGroups.size() + 1);
	std::transform(ellipticCurveGroups.cbegin(), ellipticCurveGroups.cend(), impl->tlsEllipticCurveGroups.begin(),
				   [](TlsEllipticCurveGroupID ellipticCurveGroup) {
					   const mbedtls_ecp_curve_info * curve_info =
							   mbedtls_ecp_curve_info_from_tls_id(ellipticCurveGroup);
					   if (nullptr != curve_info) {
						   return curve_info->grp_id;
					   } else {
						   throw std::invalid_argument("Unknown or unsupported elliptic curve group \""
													   + std::to_string(ellipticCurveGroup) + "\"");
					   }
				   });
	// Zero terminator as last element
	impl->tlsEllipticCurveGroups.back() = MBEDTLS_ECP_DP_NONE;
	mbedtls_ssl_conf_curves(&impl->conf, impl->tlsEllipticCurveGroups.data());
}

void TlsSession::setHostnameAndPort(std::string /*hostname*/, uint16_t /*port*/) {
    throw std::runtime_error("Function setHostnameAndPort not supported with mbed TLS");
}
        
void TlsSession::setSupportedGroups(const std::vector<TlsSupportedGroupID> & /*ellipticCurveGroups*/) {
	throw std::runtime_error("Function not supported with mbed TLS");
}

void TlsSession::setSignatureSchemes(const std::vector<TlsSignatureScheme> & /*signatureSchemes*/) {
	throw std::runtime_error("Function not supported with mbed TLS");
}

void TlsSession::setSignatureAlgorithms(const std::vector<TlsSignatureAndHashAlgorithm> & /*signatureAlgorithms*/){
    throw std::runtime_error("Function not supported with mbed TLS");
}

void TlsSession::setHelloCompressionMethods(const std::vector<uint8_t> & helloCompressionMethods) {
	impl->helloCompressionMethods = helloCompressionMethods;
	impl->conf.overwrite_compression_methods = impl->helloCompressionMethods.data();
	impl->conf.overwrite_compression_methods_len = impl->helloCompressionMethods.size();
}

void TlsSession::setClientHelloExtensions(const std::vector<uint8_t> & clientHelloExtensions) {
	impl->clientHelloExtensions = clientHelloExtensions;
	impl->conf.overwrite_extensions = impl->clientHelloExtensions.data();
	impl->conf.overwrite_extensions_len = impl->clientHelloExtensions.size();
}

void TlsSession::setServerHelloExtensions(const std::vector<uint8_t> & serverHelloExtensions) {
    impl->serverHelloExtensions = serverHelloExtensions;
    impl->conf.overwrite_extensions = impl->serverHelloExtensions.data();
    impl->conf.overwrite_extensions_len = impl->serverHelloExtensions.size();
}

void TlsSession::setPreSharedKey(const std::vector<uint8_t> &preSharedKey, const std::string /*pskIdentityHint*/) {
    impl->preSharedKey = preSharedKey;
    if(!preSharedKey.empty()) {
        const char *psk_identity = "Client_identity";
        assertSuccess("mbedtls_ssl_conf_psk", mbedtls_ssl_conf_psk(&impl->conf, preSharedKey.data(),
                                                                   preSharedKey.size(),
                                                                   (const unsigned char *) psk_identity,
                                                                   strlen(psk_identity)));
    }
}

void TlsSession::setExtensionEncryptThenMac(const bool enable) {
	mbedtls_ssl_conf_encrypt_then_mac(&impl->conf, enable ? MBEDTLS_SSL_ETM_ENABLED : MBEDTLS_SSL_ETM_DISABLED);
}

void TlsSession::sendRecord(const uint8_t type, const std::size_t msglen, const uint8_t * data) {
	impl->ssl.out_msgtype = type;
	impl->ssl.out_msglen = msglen;
	for (std::size_t i = 0; i < msglen; i++) {
		impl->ssl.out_msg[i] = data[i];
	}
	auto result = mbedtls_ssl_write_record(&impl->ssl);
	assertSuccess("mbedtls_ssl_write_record", result);
	result = mbedtls_ssl_read_record(&impl->ssl);
	assertSuccess("mbedtls_ssl_read_record", result);
}

void TlsSession::overwriteEllipticCurveGroup(TlsEllipticCurveGroupID ellipticCurve) {
	impl->conf.overwrite_elliptic_curve = 1;
	impl->conf.elliptic_curve = ellipticCurve;
}

void TlsSession::overwriteHelloVersion(const TlsVersion & version) {
	impl->conf.overwrite_hello_version = 1;
	impl->conf.hello_version[0] = version.first;
	impl->conf.hello_version[1] = version.second;
}

void TlsSession::setLogger(Tooling::Logger & logger) {
	TlsTestTool::TlsSession::setLogger(logger);
	TlsLogFilter::registerInstances(logger);
	mbedtls_debug_set_threshold(9999);
	mbedtls_ssl_conf_dbg(&impl->conf, &writeToLogger, &logger);
	log(__FILE__, __LINE__, "Using patched " MBEDTLS_VERSION_STRING_FULL ".");
}

void TlsSession::setWaitForAlertSeconds(uint32_t timeout) {
	impl->waitForAlertSeconds = timeout;
}

void TlsSession::setTcpReceiveTimeoutSeconds(uint32_t timeout) {
	impl->tcpReceiveTimeoutSeconds = timeout;
}

void TlsSession::setEarlyData(std::vector<uint8_t> /*data*/) {
    throw std::runtime_error("Function not supported with mbed TLS");
}

void TlsSession::setSessionCache(std::string /*cache*/) {
    throw std::runtime_error("Function not supported with mbed TLS");
}

void TlsSession::setHandshakeType(Configuration::HandshakeType /*type*/) {
}

void TlsSession::forceCertificateUsage() {
    impl->conf.force_certificate_usage = 1;
}

void TlsSession::setOcspResponderFile(std::string /*responderFile*/) {
    throw std::runtime_error("setOcspResponderFile Function not supported with mbed TLS");
}

void TlsSession::setupSession() {
	/*for mbedTLS there is nothing to do here*/
}

void TlsSession::cleanSession(){
	/*for mbedTLS there is nothing to do here*/
}



}
}
