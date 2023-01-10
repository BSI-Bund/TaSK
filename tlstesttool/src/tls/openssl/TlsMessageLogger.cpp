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
#include "TlsMessageLogger.h"
#include "logging/Logger.h"
#include "strings/HexStringHelper.h"
#include "tls/TlsHandshakeType.h"
#include "tls/TlsLogConstants.h"
#include <iterator>

namespace TlsTestTool {
namespace OpenSsl {

static void logServerHelloOrHelloRetryMessage(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
	auto ptr = msg.cbegin();
	// ProtocolVersion legacy_version
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}
	ptr += 2;
	// Random random
	if (std::distance(ptr, msg.cend()) < 32) {
        logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                   std::string{TlsLogConstants::SERVERHELLO_SERVERVERSION} + '='
                           + Tooling::HexStringHelper::byteArrayToHexString({msg.cbegin(), msg.cbegin() + 2}));
        return;
	}

    /*
    For reasons of backward compatibility with middleboxes (see
    Appendix D.4), the HelloRetryRequest message uses the same structure
    as the ServerHello, but with Random set to the special value of the
    SHA-256 of "HelloRetryRequest":

      CF 21 AD 74 E5 9A 61 11 BE 1D 8C 02 1E 65 B8 91
      C2 A2 11 16 7A BB 8C 5E 07 9E 09 E2 C8 A8 33 9C

    Upon receiving a message with type server_hello, implementations MUST
    first examine the Random value and, if it matches this value, process
    it as described in Section 4.1.4 (of rfc8446)).
    */
    std::vector<uint8_t> helloRetryRequestRandom = {0xCF, 0x21, 0xAD, 0x74, 0xE5, 0x9A, 0x61, 0x11,
                                                    0xBE, 0x1D, 0x8C, 0x02, 0x1E, 0x65, 0xB8, 0x91,
                                                    0xC2, 0xA2, 0x11, 0x16, 0x7A, 0xBB, 0x8C, 0x5E,
                                                    0x07, 0x9E, 0x09, 0xE2, 0xC8, 0xA8, 0x33, 0x9C};

    std::string helloRetryRequestRandomString(Tooling::HexStringHelper::byteArrayToHexString({helloRetryRequestRandom.cbegin(), helloRetryRequestRandom.cend()}));
    std::string receivedRandom(Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 32}));
    bool bServerHello = (0 != receivedRandom.compare(helloRetryRequestRandomString));

    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               (bServerHello ? std::string{TlsLogConstants::SERVERHELLO_SERVERVERSION}
                       :std::string{TlsLogConstants::HELLORETRY_SERVERVERSION}) + '='
                       + Tooling::HexStringHelper::byteArrayToHexString({msg.cbegin(), msg.cbegin() + 2}));

	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               (bServerHello ? std::string{TlsLogConstants::SERVERHELLO_RANDOM}
                       :std::string{TlsLogConstants::HELLORETRY_RANDOM}) + '='
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 32}));
	ptr += 32;
	// opaque legacy_session_id_echo<0..32>
	if (std::distance(ptr, msg.cend()) < 1) {
		return;
	}
	const TlsUint8 sessionIdLength = *ptr++;
	if (std::distance(ptr, msg.cend()) < sessionIdLength) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               (bServerHello ? std::string{TlsLogConstants::SERVERHELLO_SESSIONID}
                       :std::string{TlsLogConstants::HELLORETRY_SESSIONID}) + '='
                       + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + sessionIdLength}));
	ptr += sessionIdLength;
	// CipherSuite cipher_suite
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               (bServerHello ? std::string{TlsLogConstants::SERVERHELLO_CIPHERSUITE}
                       :std::string{TlsLogConstants::HELLORETRY_CIPHERSUITE}) + '='
                       + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 2}));
	ptr += 2;
	// uint8 legacy_compression_method
	if (std::distance(ptr, msg.cend()) < 1) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               (bServerHello ? std::string{TlsLogConstants::SERVERHELLO_COMPRESSIONMETHOD}
                       :std::string{TlsLogConstants::HELLORETRY_COMPRESSIONMETHOD}) + '='
                       + Tooling::HexStringHelper::byteArrayToHexString({*ptr++}));
	// Extension extensions<6..2^16-1>
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}
	const TlsUint16 extensionsLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, msg.cend()) < extensionsLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               (bServerHello ? std::string{TlsLogConstants::SERVERHELLO_EXTENSIONS}
                       :std::string{TlsLogConstants::HELLORETRY_EXTENSIONS}) + '='
                       + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + extensionsLength.get()}));
}

static void logEncryptedExtensionsMessage(Tooling::Logger & logger, const std::vector<uint8_t> & msg){
	auto ptr = msg.cbegin();
	// ProtocolVersion legacy_version

	// Extension extensions<6..2^16-1>
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}
	const TlsUint16 extensionsLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, msg.cend()) < extensionsLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   (std::string{TlsLogConstants::ENCRYTPED_EXTENSIONS}) + '='
			   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + extensionsLength.get()}));
}


static void logClientHelloMessage(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
	auto ptr = msg.cbegin();
	// ProtocolVersion legacy_version
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}

	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
		std::string{TlsLogConstants::CLIENTHELLO_CLIENTVERSION} + '='
		+ Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 2}));
	ptr += 2;

	// Random random
	if (std::distance(ptr, msg.cend()) < 32) {
        return;
	}

	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			std::string{TlsLogConstants::CLIENTHELLO_RANDOM} + '='
			+ Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 32}));
	ptr += 32;
	// opaque legacy_session_id_echo<0..32>
	if (std::distance(ptr, msg.cend()) < 1) {
		return;
	}
	const TlsUint8 sessionIdLength = *ptr++;
	if (std::distance(ptr, msg.cend()) < sessionIdLength) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			std::string{TlsLogConstants::CLIENTHELLO_SESSIONID} + '='
            + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + sessionIdLength}));
	ptr += sessionIdLength;

	// CipherSuite cipher_suites
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}
	const TlsUint16 cipherSuitesLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, msg.cend()) < cipherSuitesLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
		std::string{TlsLogConstants::CLIENTHELLO_CIPHERSUITES} + '='
		+ Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + cipherSuitesLength.get()}));
	ptr += cipherSuitesLength.get();

	// uint8 legacy_compression_method
	if (std::distance(ptr, msg.cend()) < 1) {
		return;
	}
	const TlsUint8 compressionLength = *ptr++;
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			std::string{TlsLogConstants::CLIENTHELLO_COMPRESSIONMETHODS} + '='
            + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + compressionLength}));
    ptr += compressionLength;

    // Extension extensions<6..2^16-1>
	if (std::distance(ptr, msg.cend()) < 2) {
		return;
	}
	const TlsUint16 extensionsLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, msg.cend()) < extensionsLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			std::string{TlsLogConstants::CLIENTHELLO_EXTENSIONS} + '='
            + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + extensionsLength.get()}));
}

static void logCertificateMessageTls12(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
    auto ptr = msg.cbegin();
    // ASN.1Cert certificate_list<0..2^24-1>
    const TlsUint24 certificateListLength(*ptr, *(ptr + 1), *(ptr + 2));
    ptr += sizeof(TlsUint24);
    if (std::distance(ptr, msg.cend()) < certificateListLength.get()) {
        return;
    }
    uint32_t numCertificate = 0;
    for (std::size_t certificateDataRead = 0; certificateDataRead < certificateListLength.get();) {
        // opaque ASN.1Cert<1..2^24-1>
        const TlsUint24 certificateLength(*ptr, *(ptr + 1), *(ptr + 2));
        ptr += sizeof(TlsUint24);
        if (std::distance(ptr, msg.cend()) < certificateLength.get()) {
            return;
        }
        logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                   "Certificate.certificate_list[" + std::to_string(numCertificate) + "]="
                           + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + certificateLength.get()}));
        ptr += certificateLength.get();
        certificateDataRead += sizeof(TlsUint24) + certificateLength.get();
        ++numCertificate;
    }
    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               "Certificate.certificate_list.size=" + std::to_string(numCertificate));
}

static void logCertificateMessageTls13(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
    auto ptr = msg.cbegin();
    uint8_t certType = *(ptr);
    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               "Certificate.CertificateType=" + std::to_string(certType));
    ptr += sizeof(uint8_t);

    // CertificateEntry certificate_list<0..2^24-1>;
    const TlsUint24 certificateListLength(*(ptr), *(ptr + 1), *(ptr + 2));
    ptr += sizeof(TlsUint24);
    if (std::distance(ptr, msg.cend()) < certificateListLength.get()) {
        return;
    }
    uint32_t numCertificate = 0;
    for (std::size_t certificateDataRead = 0; certificateDataRead < certificateListLength.get();) {
        // opaque cert_data<1..2^24-1> or opaque ASN1_subjectPublicKeyInfo<1..2^24-1>
        const TlsUint24 length(*ptr, *(ptr + 1), *(ptr + 2));
        ptr += sizeof(TlsUint24);
        if (std::distance(ptr, msg.cend()) < length.get()) {
            return;
        }
        logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                   "Certificate.certificate_list[" + std::to_string(numCertificate) + "]="
                           + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + length.get()}));
        ptr += length.get();
        certificateDataRead += sizeof(TlsUint24) + length.get();

        // Extension extensions<6..2^16-1>
        if (std::distance(ptr, msg.cend()) < 2) {
            return;
        }
        const TlsUint16 extensionsLength(*ptr, *(ptr + 1));
        ptr += 2;
        if (std::distance(ptr, msg.cend()) < extensionsLength.get()) {
            return;
        }
        logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                   "Certificate.certificate_list[" + std::to_string(numCertificate) + "].extensions_list="
                           + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + extensionsLength.get()}));
        certificateDataRead += sizeof(TlsUint16) + extensionsLength.get();
        ptr += extensionsLength.get();
        ++numCertificate;
    }
    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               "Certificate.certificate_list.size=" + std::to_string(numCertificate));
 }

static void logCertificateMessage(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
    auto ptrBegin = msg.cbegin();
    auto ptrEnd = msg.cend();
    uint32_t len = ptrEnd - ptrBegin;
    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               "Certificate message data="
                       + Tooling::HexStringHelper::byteArrayToHexString({ptrBegin, ptrBegin + len}));
    // The certificate messages differ in format between TLS 1.2 (starts with 3 byte length field))
    // and TLS 1.3 (starts with 1 byte key type followed by 3 bytes length field)
    auto ptr = msg.cbegin();
    auto ptrCertListTls12 = ptr + sizeof(TlsUint24);
    auto ptrCertListTls13 = ptr + sizeof(uint8_t) + sizeof(TlsUint24);

    const TlsUint24 certificateListLengthTls12(*ptr, *(ptr + 1), *(ptr + 2));
    const TlsUint24 certificateListLengthTls13(*(ptr + 1), *(ptr + 2), *(ptr + 3));
    if (std::distance(ptrCertListTls12, msg.cend()) == certificateListLengthTls12.get()) {
        return logCertificateMessageTls12(logger, msg);
    } else if(std::distance(ptrCertListTls13, msg.cend()) == certificateListLengthTls13.get())  {
        return logCertificateMessageTls13(logger, msg);
    }
}

static void logNewSessionTicketMessage(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
    auto ptrBegin = msg.cbegin();
    auto ptrEnd = msg.cend();
    uint32_t len = ptrEnd - ptrBegin;
    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               "NewSessionTicket message data="
                       + Tooling::HexStringHelper::byteArrayToHexString({ptrBegin, ptrBegin + len}));

    auto ptr = ptrBegin;
    if (std::distance(ptr, ptrEnd) < 4) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "NewSessionTicket.ticket_lifetime="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 4}));
	ptr += 4;
	if (std::distance(ptr, ptrEnd) < 4) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "NewSessionTicket.ticket_age_add="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 4}));
	ptr += 4;
	const TlsUint8 nonceLength = *ptr++;
	if (std::distance(ptr, ptrEnd) < nonceLength) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "NewSessionTicket.ticket_nonce="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + nonceLength}));
	ptr += nonceLength;
	const TlsUint16 ticketLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, ptrEnd) < ticketLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "NewSessionTicket.ticket="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + ticketLength.get()}));
	ptr += ticketLength.get();
	const TlsUint16 extensionsLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, ptrEnd) < extensionsLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "NewSessionTicket.extensions="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + extensionsLength.get()}));
}

static void logCertificateVerifyMessage(Tooling::Logger & logger, const std::vector<uint8_t> & msg) {
    auto ptrBegin = msg.cbegin();
    auto ptrEnd = msg.cend();
    uint32_t len = ptrEnd - ptrBegin;
    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
               "CertificateVerify message data="
                       + Tooling::HexStringHelper::byteArrayToHexString({ptrBegin, ptrBegin + len}));

    auto ptr = ptrBegin;
    if (std::distance(ptr, ptrEnd) < 2) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "CertificateVerify.algorithm="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + 2}));
	ptr += 2;
	const TlsUint16 signatureLength(*ptr, *(ptr + 1));
	ptr += 2;
	if (std::distance(ptr, ptrEnd) < signatureLength.get()) {
		return;
	}
	logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
			   "CertificateVerify.signature="
					   + Tooling::HexStringHelper::byteArrayToHexString({ptr, ptr + signatureLength.get()}));
}

void TlsMessageLogger::logTlsHandshakeMessage(Tooling::Logger & logger, const TlsHandshakeType & type,
											  const bool /*isSent*/, const std::vector<uint8_t> & msg) {
    if (TlsHandshakeType::SERVER_HELLO == type) {
        logServerHelloOrHelloRetryMessage(logger, msg);
    } else if (TlsHandshakeType::CLIENT_HELLO == type) {
		logClientHelloMessage(logger, msg);
	} else if (TlsHandshakeType::CERTIFICATE == type) {
		logCertificateMessage(logger, msg);
    } else if (TlsHandshakeType::NEW_SESSION_TICKET == type) {
        logNewSessionTicketMessage(logger, msg);
    } else if (TlsHandshakeType::CERTIFICATE_VERIFY == type) {
        logCertificateVerifyMessage(logger, msg);
    }
	else if (TlsHandshakeType::ENCRYPTED_EXTENSION == type) {
		logEncryptedExtensionsMessage(logger, msg);
	} else {
        auto ptrBegin = msg.cbegin();
        auto ptrEnd = msg.cend();
        uint32_t len = ptrEnd - ptrBegin;
        char buf[8];
        sprintf(buf, "0x%x",static_cast<int>(type) );
        logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                   "tlsHandshakeMessage type =" +  std::string(buf) + " data ="
                           + Tooling::HexStringHelper::byteArrayToHexString({ptrBegin, ptrBegin + len}));
    }
}
}
}
