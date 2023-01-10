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
#include "TlsLogger.h"
#include "TlsMessageLogger.h"
#include "logging/Logger.h"
#include "openssl/ssl.h"
#include "strings/StringHelper.h"
#include "tls/TlsLogConstants.h"
#include <cstdio>
#include <string>
#include <vector>
#include <iomanip>

namespace TlsTestTool {
namespace OpenSsl {

#define CONTENT_TYPE_CHANGE_CIPHER_SPEC 20
#define CONTENT_TYPE_ALERT 21
#define CONTENT_TYPE_HANDSHAKE 22


void TlsLogger::logInternalTls(Tooling::Logger * logger, int write_p, int version, int content_type, const void * buf, size_t len) {
	// This code is taken from the internal libressl function and only slightly changed to use the logger
	const char *str_write_p, *str_version, *str_content_type = "",
	    *str_details1 = "", *str_details2 = "";

	str_write_p = write_p ? ">>>" : "<<<";

	/* XXX convert to using ssl_get_version */
	switch (version) {
	case SSL2_VERSION:
		str_version = "SSL 2.0";
		break;
	case SSL3_VERSION:
		str_version = "SSL 3.0 ";
		break;
	case TLS1_VERSION:
		str_version = "TLS 1.0 ";
		break;
	case TLS1_1_VERSION:
		str_version = "TLS 1.1 ";
		break;
	case TLS1_2_VERSION:
		str_version = "TLS 1.2 ";
		break;
	case TLS1_3_VERSION:
		str_version = "TLS 1.3 ";
		break;
	case DTLS1_VERSION:
		str_version = "DTLS 1.0 ";
		break;
	default:
		str_version = "???";
	}

	if (version == SSL2_VERSION) {
		str_details1 = "???";

		if (len > 0) {
			/* XXX magic numbers */
			switch ((static_cast<const uint8_t *>(buf)[0])) {
			case 0:
				str_details1 = ", ERROR:";
				str_details2 = " ???";
				if (len >= 3) {
					//unsigned err = (((const unsigned char *) buf)[1] << 8) + ((const unsigned char *) buf)[2];
					unsigned err = ((static_cast<const uint8_t *>(buf)[1]) << 8) + (static_cast<const unsigned char *>( buf)[2]);

					switch (err) {
					case 0x0001:
						str_details2 = " NO-CIPHER-ERROR";
						break;
					case 0x0002:
						str_details2 = " NO-CERTIFICATE-ERROR";
						break;
					case 0x0004:
						str_details2 = " BAD-CERTIFICATE-ERROR";
						break;
					case 0x0006:
						str_details2 = " UNSUPPORTED-CERTIFICATE-TYPE-ERROR";
						break;
					}
				}
				break;
			case 1:
				str_details1 = ", CLIENT-HELLO";
				break;
			case 2:
				str_details1 = ", CLIENT-MASTER-KEY";
				break;
			case 3:
				str_details1 = ", CLIENT-FINISHED";
				break;
			case 4:
				str_details1 = ", SERVER-HELLO";
				break;
			case 5:
				str_details1 = ", SERVER-VERIFY";
				break;
			case 6:
				str_details1 = ", SERVER-FINISHED";
				break;
			case 7:
				str_details1 = ", REQUEST-CERTIFICATE";
				break;
			case 8:
				str_details1 = ", CLIENT-CERTIFICATE";
				break;
			}
		}
	}
	if (version == SSL3_VERSION || version == TLS1_VERSION ||
	    version == TLS1_1_VERSION || version == TLS1_2_VERSION ||
	    version == TLS1_3_VERSION || version == DTLS1_VERSION) {
		/* XXX magic numbers are in ssl3.h */
		switch (content_type) {
		case 20:
			str_content_type = "ChangeCipherSpec";
			break;
		case 21:
			str_content_type = "Alert";
			break;
		case 22:
			str_content_type = "Handshake";
			break;
		}

		if (content_type == 21) {	/* Alert */
			str_details1 = ", ???";

			if (len == 2) {
				switch ((static_cast<const uint8_t *>(buf)[0])) {
				case 1:
					str_details1 = ", warning";
					break;
				case 2:
					str_details1 = ", fatal";
					break;
				}

				str_details2 = " ???";
				switch ((static_cast<const uint8_t *>(buf)[1])) {
				case 0:
					str_details2 = " close_notify";
					break;
				case 10:
					str_details2 = " unexpected_message";
					break;
				case 20:
					str_details2 = " bad_record_mac";
					break;
				case 21:
					str_details2 = " decryption_failed";
					break;
				case 22:
					str_details2 = " record_overflow";
					break;
				case 30:
					str_details2 = " decompression_failure";
					break;
				case 40:
					str_details2 = " handshake_failure";
					break;
				case 42:
					str_details2 = " bad_certificate";
					break;
				case 43:
					str_details2 = " unsupported_certificate";
					break;
				case 44:
					str_details2 = " certificate_revoked";
					break;
				case 45:
					str_details2 = " certificate_expired";
					break;
				case 46:
					str_details2 = " certificate_unknown";
					break;
				case 47:
					str_details2 = " illegal_parameter";
					break;
				case 48:
					str_details2 = " unknown_ca";
					break;
				case 49:
					str_details2 = " access_denied";
					break;
				case 50:
					str_details2 = " decode_error";
					break;
				case 51:
					str_details2 = " decrypt_error";
					break;
				case 60:
					str_details2 = " export_restriction";
					break;
				case 70:
					str_details2 = " protocol_version";
					break;
				case 71:
					str_details2 = " insufficient_security";
					break;
				case 80:
					str_details2 = " internal_error";
					break;
				case 90:
					str_details2 = " user_canceled";
					break;
				case 100:
					str_details2 = " no_renegotiation";
					break;
				case 110:
					str_details2 = " unsupported_extension";
					break;
				case 111:
					str_details2 = " certificate_unobtainable";
					break;
				case 112:
					str_details2 = " unrecognized_name";
					break;
				case 113:
					str_details2 = " bad_certificate_status_response";
					break;
				case 114:
					str_details2 = " bad_certificate_hash_value";
					break;
				case 115:
					str_details2 = " unknown_psk_identity";
					break;
				}
			}
		}
		if (content_type == 22) {	/* Handshake */
			str_details1 = "???";

			if (len > 0) {
				switch ((static_cast<const uint8_t *>(buf)[0])) {
				case 0:
					str_details1 = ", HelloRequest";
					break;
				case 1:
					str_details1 = ", ClientHello";
					break;
				case 2:
					str_details1 = ", ServerHello";
					break;
				case 3:
					str_details1 = ", HelloVerifyRequest";
					break;
				case 4:
					str_details1 = ", NewSessionTicket";
					break;
				case 5:
					str_details1 = ", EndOfEarlyData";
					break;
				case 8:
					str_details1 = ", EncryptedExtensions";
					break;
				case 11:
					str_details1 = ", Certificate";
					break;
				case 12:
					str_details1 = ", ServerKeyExchange";
					break;
				case 13:
					str_details1 = ", CertificateRequest";
					break;
				case 14:
					str_details1 = ", ServerHelloDone";
					break;
				case 15:
					str_details1 = ", CertificateVerify";
					break;
				case 16:
					str_details1 = ", ClientKeyExchange";
					break;
				case 20:
					str_details1 = ", Finished";
					break;
				case 22:
					str_details1 = ", CertificateStatus";
					break;
				case 24:
					str_details1 = ", KeyUpdate";
					break;
				}
			}
		}
	}
	std::stringbuf buffer;
	std::ostream os (&buffer);

	os << str_write_p;
	os << " ";
	os << str_version;
	os << str_content_type;
	os << " ";
	os << "[length ";
	os << std::setfill('0') << std::setw(4) << std::hex << static_cast<unsigned long>(len);
	os << "]";
	os << str_details1;
	os << str_details2;

	logger->log(Tooling::LogLevel::LOW, "\tOpenSSL", __FILE__, __LINE__, buffer.str());
	buffer.str("");

	if (len > 0) {
		size_t num, i;
		os << "   ";
		num = len;

		for (i = 0; i < num; i++) {
			if (i % 16 == 0 && i > 0) {
				logger->log(Tooling::LogLevel::LOW, "\tOpenSSL", __FILE__, __LINE__, buffer.str());
				buffer.str("");
				os << "   ";
			}
			os << " ";
			unsigned char c = static_cast<const uint8_t *>(buf)[i];
			os << std::setfill('0') << std::setw(2) << std::hex << static_cast<int>(c);
		}
		// log pending bytes
		if (buffer.str() != "") {
			logger->log(Tooling::LogLevel::LOW, "\tOpenSSL", __FILE__, __LINE__, buffer.str());
			buffer.str("");
		}
		if (i < len) {
			logger->log(Tooling::LogLevel::LOW, "\tOpenSSL", __FILE__, __LINE__, " ...");
		}
	}
}

void TlsLogger::logTls(Tooling::Logger * logger, int write_p, int version, int content_type, const void * buf, size_t len) {

	if (version == SSL3_VERSION || version == TLS1_VERSION ||
	    version == TLS1_1_VERSION || version == TLS1_2_VERSION ||
	    version == TLS1_3_VERSION || version == DTLS1_VERSION) {

		switch (content_type) {
		case CONTENT_TYPE_CHANGE_CIPHER_SPEC:
			logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
						(write_p ? TlsLogConstants::CHANGECIPHERSPEC_TX : TlsLogConstants::CHANGECIPHERSPEC_RX_VALID));
			break;
		case CONTENT_TYPE_ALERT:
			if (!write_p) {
				logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__, TlsLogConstants::ALERT_RX);
				if (len == 2) {
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								Tooling::StringHelper::formatInt(TlsLogConstants::ALERT_LEVEL, static_cast<const uint8_t *>(buf)[0]));
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								Tooling::StringHelper::formatInt(TlsLogConstants::ALERT_DESCRIPTION, static_cast<const uint8_t *>(buf)[1]));
				}
			}
			break;
		case CONTENT_TYPE_HANDSHAKE:
			if (len > 0) {
				TlsHandshakeType handshakeType = static_cast<TlsHandshakeType>(static_cast<const uint8_t *>(buf)[0]);
				const unsigned char * data = static_cast<const uint8_t *>((buf));
				data += 4; // set handshake message data offset (handshake_type[1] + lenght_bytes[3])
				TlsMessageLogger::logTlsHandshakeMessage(*logger, handshakeType, write_p, std::vector<uint8_t>(data, data + (len - 4)));

				switch (handshakeType) {
				case TlsHandshakeType::HELLO_REQUEST:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::HELLORETRY_TX : TlsLogConstants::HELLORETRY_RX_VALID));
					break;
				case TlsHandshakeType::CLIENT_HELLO:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::CLIENTHELLO_TX : TlsLogConstants::CLIENTHELLO_RX_VALID));
					break;
				case TlsHandshakeType::SERVER_HELLO:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::SERVERHELLO_TX : TlsLogConstants::SERVERHELLO_RX_VALID));
					break;
				case TlsHandshakeType::HELLO_VERIFY_REQUEST:
					break;
				case TlsHandshakeType::NEW_SESSION_TICKET:
					break;
				case TlsHandshakeType::END_OF_EARLY_DATA:
					break;
				case TlsHandshakeType::ENCRYPTED_EXTENSION:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
						(write_p ? TlsLogConstants::ENCRYPTEDEXTENSIONS_TX : TlsLogConstants::ENCRYPTEDEXTENSIONS_RX_VALID));
					break;
				case TlsHandshakeType::CERTIFICATE:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
						(write_p ? TlsLogConstants::CERTIFICATE_TX : TlsLogConstants::CERTIFICATE_RX_VALID));
					break;
				case TlsHandshakeType::SERVER_KEY_EXCHANGE:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::SERVERKEYEXCHANGE_TX : TlsLogConstants::SERVERKEYEXCHANGE_RX_VALID));
					break;
				case TlsHandshakeType::CERTIFICATE_REQUEST:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::CERTIFICATEREQUEST_TX : TlsLogConstants::CERTIFICATEREQUEST_RX_VALID));
					break;
				case TlsHandshakeType::SERVER_HELLO_DONE:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::SERVERHELLODONE_TX : TlsLogConstants::SERVERHELLODONE_RX_VALID));
					break;
				case TlsHandshakeType::CERTIFICATE_VERIFY:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::CERTIFICATEVERIFY_TX : TlsLogConstants::CERTIFICATEVERIFY_RX_VALID));
					break;
				case TlsHandshakeType::CLIENT_KEY_EXCHANGE:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::CLIENTKEYEXCHANGE_TX : TlsLogConstants::CLIENTKEYEXCHANGE_RX_VALID));
					break;
				case TlsHandshakeType::FINISHED:
					logger->log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								(write_p ? TlsLogConstants::FINISHED_TX : TlsLogConstants::FINISHED_RX_VALID));
					break;
				case TlsHandshakeType::CERTIFICATE_STATUS:
					break;
				case TlsHandshakeType::KEY_UPDATE:
					break;
				}
			}
			break;
		}
	}
}
}
}
