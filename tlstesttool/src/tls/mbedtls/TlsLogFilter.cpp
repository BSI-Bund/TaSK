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
#include "TlsLogFilter.h"
#include "logging/Logger.h"
#include "mbedtls/ecdh.h"
#include "strings/HexStringHelper.h"
#include "strings/StringHelper.h"
#include "tls/TlsHandshakeHeader.h"
#include "tls/TlsHeartbeatMessageHeader.h"
#include "tls/TlsLogConstants.h"
#include "tls/TlsPlaintextHeader.h"
#include <cstdio>
#include <functional>
#include <regex>
#include <string>
#include <unordered_map>
#include <utility>
#include <vector>

namespace TlsTestTool {
    namespace MbedTls {

        static std::string mpiToHexString(const mbedtls_mpi &mpi) {
            std::size_t requiredSize;
            mbedtls_mpi_write_string(&mpi, 16, nullptr, 0, &requiredSize);
            std::vector<char> buffer(requiredSize + 1);
            mbedtls_mpi_write_string(&mpi, 16, buffer.data(), buffer.size(), &requiredSize);
            // Add spaces for separating the bytes
            std::stringstream stream;
            std::size_t numWritten = 0;
            for (auto digit: buffer) {
                if (0 != std::isxdigit(digit)) {
                    stream << static_cast<char>(std::tolower(digit));
                    ++numWritten;
                }
                if (2 == numWritten) {
                    stream << ' ';
                    numWritten = 0;
                }
            }
            return stream.str();
        }

        static void logHandshakeMessage(Tooling::Logger &logger, const TlsVersion &version, const uint8_t *data,
                                        const std::size_t size) {
            if (sizeof(TlsHandshakeHeader) > size) {
                return;
            }
            const auto handshakeHeader = reinterpret_cast<const TlsHandshakeHeader *>(data);
            data += sizeof(TlsHandshakeHeader);
            if (TlsHandshakeType::CERTIFICATE == handshakeHeader->msgType) {
                if ((sizeof(TlsHandshakeHeader) + handshakeHeader->length.get()) > size) {
                    return;
                }
                const auto certificateListLength = reinterpret_cast<const TlsUint24 *>(data)->get();
                data += sizeof(TlsUint24);
                uint32_t numCertificate = 0;
                for (std::size_t certificateDataRead = 0; certificateDataRead < certificateListLength;) {
                    const auto certificateLength = reinterpret_cast<const TlsUint24 *>(data)->get();
                    data += sizeof(TlsUint24);
                    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                               "Certificate.certificate_list[" + std::to_string(numCertificate) + "]="
                               + Tooling::HexStringHelper::byteArrayToHexString({data, data + certificateLength}));
                    data += certificateLength;
                    certificateDataRead += sizeof(TlsUint24) + certificateLength;
                    ++numCertificate;
                }
                logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                           "Certificate.certificate_list.size=" + std::to_string(numCertificate));
            } else if (TlsHandshakeType::CERTIFICATE_REQUEST == handshakeHeader->msgType) {
                if ((sizeof(TlsHandshakeHeader) + handshakeHeader->length.get()) > size) {
                    return;
                }
                const auto certificateTypesLength = *data;
                data += sizeof(TlsUint8);
                logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                           "CertificateRequest.certificate_types="
                           + Tooling::HexStringHelper::byteArrayToHexString({data, data + certificateTypesLength}));
                data += certificateTypesLength;
                if (TLS_VERSION_TLS_1_2 == version) {
                    // supported_signature_algorithms is only contained in TLS 1.2
                    const auto supportedSignatureAlgorithmsLength = reinterpret_cast<const TlsUint16 *>(data)->get();
                    data += sizeof(TlsUint16);
                    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                               "CertificateRequest.supported_signature_algorithms="
                               + Tooling::HexStringHelper::byteArrayToHexString(
                                       {data, data + supportedSignatureAlgorithmsLength}));
                    data += supportedSignatureAlgorithmsLength;
                }
                const auto certificateAuthoritiesLength = reinterpret_cast<const TlsUint16 *>(data)->get();
                data += sizeof(TlsUint16);
                logger.log(
                        Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                        "CertificateRequest.certificate_authorities="
                        + Tooling::HexStringHelper::byteArrayToHexString({data, data + certificateAuthoritiesLength}));
                data += certificateAuthoritiesLength;
            }
        }

        static void logTlsRecord(Tooling::Logger &logger, const uint8_t *data, const std::size_t size) {
            if (sizeof(TlsPlaintextHeader) > size) {
                return;
            }
            const auto plaintextHeader = reinterpret_cast<const TlsPlaintextHeader *>(data);
            if (TlsContentType::HANDSHAKE == plaintextHeader->type) {
                logHandshakeMessage(logger, plaintextHeader->version, data + sizeof(TlsPlaintextHeader),
                                    size - sizeof(TlsPlaintextHeader));
            }
        }

        static void logHeartbeatMessage(Tooling::Logger &logger, const uint8_t *data, const std::size_t size) {
            if (sizeof(HeartbeatMessageHeader) > size) {
                return;
            }
            auto const *heartbeatMessageHeader = reinterpret_cast<const HeartbeatMessageHeader *>(data);
            const uint8_t *payloadBegin = data + sizeof(HeartbeatMessageHeader);
            const uint8_t *payloadEnd =
                    data + sizeof(HeartbeatMessageHeader) + heartbeatMessageHeader->payload_length.get();
            logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                       "Heartbeat data size including padding=" + std::to_string(size));
            logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                       "Heartbeat.type=" + std::to_string(heartbeatMessageHeader->type));
            logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                       "Heartbeat.payload_length=" + std::to_string(heartbeatMessageHeader->payload_length.get()));
            logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                       "Heartbeat.payload_data=" +
                       Tooling::HexStringHelper::byteArrayToHexString({payloadBegin, payloadEnd}));
        }

        static void logHeartbeatRecord(Tooling::Logger &logger, const uint8_t *data, const std::size_t size) {
            if (sizeof(TlsPlaintextHeader) > size) {
                return;
            }
            const auto plaintextHeader = reinterpret_cast<const TlsPlaintextHeader *>(data);
            if (TlsContentType::HEARTBEAT == plaintextHeader->type) {
                logHeartbeatMessage(logger, data + sizeof(TlsPlaintextHeader), size - sizeof(TlsPlaintextHeader));
            }
        }

        static const std::unordered_map<std::string, std::string> logStringTranslation{
                // Receiving
                {"=> parse client hello\n",           TlsLogConstants::CLIENTHELLO_RX},
                {"<= parse client hello\n",           TlsLogConstants::CLIENTHELLO_RX_VALID},
                {"bad client hello message\n",        TlsLogConstants::CLIENTHELLO_RX_BAD},
                {"<= parse server hello\n",           TlsLogConstants::SERVERHELLO_RX_VALID},
                {"bad server hello message\n",        TlsLogConstants::SERVERHELLO_RX_BAD},
                {"<= parse certificate\n",            TlsLogConstants::CERTIFICATE_RX_VALID},
                {"bad certificate message\n",         TlsLogConstants::CERTIFICATE_RX_BAD},
                {"<= parse server key exchange\n",    TlsLogConstants::SERVERKEYEXCHANGE_RX_VALID},
                {"bad server key exchange message\n", TlsLogConstants::SERVERKEYEXCHANGE_RX_BAD},
                {"got a certificate request\n",       TlsLogConstants::CERTIFICATEREQUEST_RX_VALID},
                {"bad certificate request message\n", TlsLogConstants::CERTIFICATEREQUEST_RX_BAD},
                {"<= parse server hello done\n",      TlsLogConstants::SERVERHELLODONE_RX_VALID},
                {"bad server hello done message\n",   TlsLogConstants::SERVERHELLODONE_RX_BAD},
                {"<= parse client key exchange\n",    TlsLogConstants::CLIENTKEYEXCHANGE_RX_VALID},
                {"bad client key exchange\n",         TlsLogConstants::CLIENTKEYEXCHANGE_RX_BAD},
                {"<= parse certificate verify\n",     TlsLogConstants::CERTIFICATEVERIFY_RX_VALID},
                {"bad certificate verify message\n",  TlsLogConstants::CERTIFICATEVERIFY_RX_BAD},
                {"<= parse change cipher spec\n",     TlsLogConstants::CHANGECIPHERSPEC_RX_VALID},
                {"bad change cipher spec message\n",  TlsLogConstants::CHANGECIPHERSPEC_RX_BAD},
                {"<= parse finished\n",               TlsLogConstants::FINISHED_RX_VALID},
                {"bad finished message\n",            TlsLogConstants::FINISHED_RX_BAD},
                // Transmitting
                {"<= write client hello\n",           TlsLogConstants::CLIENTHELLO_TX},
                {"<= write server hello\n",           TlsLogConstants::SERVERHELLO_TX},
                {"<= write certificate\n",            TlsLogConstants::CERTIFICATE_TX},
                {"<= write server key exchange\n",    TlsLogConstants::SERVERKEYEXCHANGE_TX},
                {"<= write certificate request\n",    TlsLogConstants::CERTIFICATEREQUEST_TX},
                {"<= write server hello done\n",      TlsLogConstants::SERVERHELLODONE_TX},
                {"<= write client key exchange\n",    TlsLogConstants::CLIENTKEYEXCHANGE_TX},
                {"<= write certificate verify\n",     TlsLogConstants::CERTIFICATEVERIFY_TX},
                {"<= write change cipher spec\n",     TlsLogConstants::CHANGECIPHERSPEC_TX},
                {"<= write finished\n",               TlsLogConstants::FINISHED_TX},
        };

        static void translateString(Tooling::Logger &logger, const std::string &message) {
            auto stringTranslation = logStringTranslation.find(message);
            if (logStringTranslation.cend() != stringTranslation) {
                logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__, stringTranslation->second);
            }
        }

        static const std::vector<std::pair<std::regex, std::string>> logRegExTranslation{
                {std::regex{"server hello, received ciphersuite: ([0-9a-f]{2})([0-9a-f]{2})\n"},
                                                                                  std::string{
                                                                                          TlsLogConstants::SERVERHELLO_CIPHERSUITE} +
                                                                                  "=$1 $2\n"},
                {std::regex{"server hello, chosen ciphersuite: ([0-9a-f]{2})([0-9a-f]{2})\n"},
                                                                                  std::string{
                                                                                          TlsLogConstants::SERVERHELLO_CIPHERSUITE} +
                                                                                  "=$1 $2\n"},
                {std::regex{"got an alert message, type: \\[[0-9]+:[0-9]+\\]\n"}, TlsLogConstants::ALERT_RX},
                {std::regex{
                        "padding_length: ([0-9a-f]{2})\n"},                       "Finished.GenericBlockCipher.padding_length=$1\n"},
        };

        static void translateRegEx(Tooling::Logger &logger, const std::string &message) {
            for (const auto &regExTranslation: logRegExTranslation) {
                std::smatch match;
                if (std::regex_match(message, match, regExTranslation.first)) {
                    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                               match.format(regExTranslation.second));
                }
            }
        }

        static const std::vector<std::pair<std::regex, std::string>> logRegExDecToHexTranslation{
                {std::regex{"server hello, compress alg.: ([0-9]+)\n"},
                                                                                    std::string{
                                                                                            TlsLogConstants::SERVERHELLO_COMPRESSIONMETHOD} +
                                                                                    "=%02x\n"},
                {std::regex{"got an alert message, type: \\[([0-9]+):[0-9]+\\]\n"}, TlsLogConstants::ALERT_LEVEL},
                {std::regex{"got an alert message, type: \\[[0-9]+:([0-9]+)\\]\n"}, TlsLogConstants::ALERT_DESCRIPTION},
                {std::regex{"Server used HashAlgorithm ([0-9]+)\n"},
                                                                                    TlsLogConstants::SERVERKEYEXCHANGE_SIGNEDPARAMS_ALGORITHM_HASH},
                {std::regex{"Server used SignatureAlgorithm ([0-9]+)\n"},
                                                                                    TlsLogConstants::SERVERKEYEXCHANGE_SIGNEDPARAMS_ALGORITHM_SIGNATURE},
        };

        static void translateRegExDecToHex(Tooling::Logger &logger, const std::string &message) {
            for (const auto &regExTranslation: logRegExDecToHexTranslation) {
                std::smatch match;
                if (std::regex_match(message, match, regExTranslation.first)) {
                    auto number = std::stoul(match[1]);
                    auto formatString = regExTranslation.second;
                    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                               Tooling::StringHelper::formatInt(formatString, number));
                }
            }
        }

        static const std::unordered_map<std::string, std::string> dumpInterception{
                {"client hello, version",                TlsLogConstants::CLIENTHELLO_CLIENTVERSION},
                {"client hello, random bytes",           TlsLogConstants::CLIENTHELLO_RANDOM},
                {"client hello, session id",             TlsLogConstants::CLIENTHELLO_SESSIONID},
                {"client hello, ciphersuitelist",        TlsLogConstants::CLIENTHELLO_CIPHERSUITES},
                {"client hello, compression",            TlsLogConstants::CLIENTHELLO_COMPRESSIONMETHODS},
                {"client hello, extensions",             TlsLogConstants::CLIENTHELLO_EXTENSIONS},
                {"server hello, version",                TlsLogConstants::SERVERHELLO_SERVERVERSION},
                {"server hello, random bytes",           TlsLogConstants::SERVERHELLO_RANDOM},
                {"server hello, session id",             TlsLogConstants::SERVERHELLO_SESSIONID},
                {"server hello, extensions",             TlsLogConstants::SERVERHELLO_EXTENSIONS},
                {"server key exchange",                  "ServerKeyExchange"},
                {"signature",                            TlsLogConstants::SERVERKEYEXCHANGE_SIGNEDPARAMS_SIGNATURE},
                {"md5_hash",                             TlsLogConstants::SERVERKEYEXCHANGE_SIGNEDPARAMS_MD5HASH},
                {"sha_hash",                             TlsLogConstants::SERVERKEYEXCHANGE_SIGNEDPARAMS_SHAHASH},
                {"premaster secret",                     TlsLogConstants::CLIENTKEYEXCHANGE_EXCHANGEKEYS_PREMASTERSECRET},
                {"master secret",                        TlsLogConstants::CLIENTKEYEXCHANGE_EXCHANGEKEYS_MASTERSECRET},
                {"input record from network",            "TLS Record"},
                {"remaining content in record",          "Handshake Message"},
                {"new session ticket, ticket",           "NewSessionTicket.ticket"},
                {"heartbeat input record after decrypt", "Heartbeat Record"},
        };

/*
 * Intercept lines like
 *
 * dumping 'server hello, version' (2 bytes)
 * 0000:  03 03
 *
 * or
 *
 * dumping 'server hello, random bytes' (32 bytes)
 * 0000:  16 52 57 ea af a6 f5 3f b9 2b e0 34 da e2 c3 e7  .RW....?.+.4....
 * 0010:  6b 80 cf 3f 1e b4 74 04 d7 68 49 2a dd 6a a4 76  k..?..t..hI*.j.v
 *
 * There are at most 16 bytes per line.
 */
        static void interceptHexDump(Tooling::Logger &logger, const std::string &message) {
            static const std::regex dumpingHeaderRegEx{"dumping '([^']+)' \\(([0-9]+) bytes\\)\n"};
            static const std::regex hexDumpRegEx{"[0-9a-f]{4}:  (([0-9a-f]{2} ){1,16}) .*\n"};
            static long numLinesToCollect{0};
            static std::string collectedBytes{};
            static std::string finalOutput{};
            // Check, if a string in the dumping header is defined to be intercepted
            std::smatch dumpingHeaderMatch;
            if (std::regex_match(message, dumpingHeaderMatch, dumpingHeaderRegEx)) {
                auto interception = dumpInterception.find(dumpingHeaderMatch[1]);
                if (dumpInterception.cend() != interception) {
                    // Instruct to intercept the next lines
                    const auto numBytes = std::stoul(dumpingHeaderMatch[2]);
                    numLinesToCollect = (numBytes + 15) / 16;
                    collectedBytes.clear();
                    finalOutput = interception->second;
                }
            }
            std::smatch hexDumpMatch;
            if ((0 < numLinesToCollect) && std::regex_match(message, hexDumpMatch, hexDumpRegEx)) {
                // Append the current hex dump line
                collectedBytes += hexDumpMatch[1];
                --numLinesToCollect;
            }
            if ((0 == numLinesToCollect) && !finalOutput.empty()) {
                if (finalOutput == "ServerKeyExchange") {
                    auto byteArray = Tooling::HexStringHelper::hexStringToByteArray(collectedBytes);
                    mbedtls_ecdh_context ecdhContext;
                    mbedtls_ecdh_init(&ecdhContext);
                    const unsigned char *arrayStart = byteArray.data();
                    mbedtls_ecdh_read_params(&ecdhContext, &arrayStart, arrayStart + byteArray.size());
                    auto curveInfo = mbedtls_ecp_curve_info_from_grp_id(ecdhContext.grp.id);
                    if (nullptr != curveInfo) {
                        logger.log(
                                Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                                Tooling::StringHelper::formatInt(
                                        TlsLogConstants::SERVERKEYEXCHANGE_PARAMS_CURVEPARAMS_NAMEDCURVE,
                                        curveInfo->tls_id));
                        logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
                                   std::string{TlsLogConstants::SERVERKEYEXCHANGE_PARAMS_PUBLIC} + "04 "
                                   + mpiToHexString(ecdhContext.Qp.X) + mpiToHexString(ecdhContext.Qp.Y));
                    }
                    mbedtls_ecdh_free(&ecdhContext);
                } else if (finalOutput == "TLS Record") {
                    const auto byteArray = Tooling::HexStringHelper::hexStringToByteArray(collectedBytes);
                    logTlsRecord(logger, byteArray.data(), byteArray.size());
                } else if (finalOutput == "Handshake Message") {
                    const auto byteArray = Tooling::HexStringHelper::hexStringToByteArray(collectedBytes);
                    logHandshakeMessage(logger, logger.getTlsVersion(), byteArray.data(), byteArray.size());
                } else if (finalOutput == "Heartbeat Record") {
                    const auto byteArray = Tooling::HexStringHelper::hexStringToByteArray(collectedBytes);
                    logHeartbeatRecord(logger, byteArray.data(), byteArray.size());
                } else {
                    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__, finalOutput + '=' + collectedBytes);
                }
                finalOutput.clear();
                collectedBytes.clear();
            }
        }

        static const std::unordered_map<std::string, std::string> bitsInterception{
                {"DHM: P ", TlsLogConstants::SERVERKEYEXCHANGE_PARAMS_DHP},
                {"DHM: G ", TlsLogConstants::SERVERKEYEXCHANGE_PARAMS_DHG},
                {"DHM: GY", TlsLogConstants::SERVERKEYEXCHANGE_PARAMS_DHYS},
        };

/*
 * Intercept lines like
 *
 * value of 'DHM: G ' (2 bits) is:
 *  02
 *
 * or
 *
 * value of 'DHM: GY' (4096 bits) is:
 *  dd 6d 24 9d 25 29 ab 44 83 b4 9c 31 21 b0 86 0e
 *  a6 c9 99 84 e0 fb 72 6e 29 fe 1f d3 24 36 b9 97
 *  e9 c6 d6 9d b2 8c c8 a0 ff 73 f3 b3 03 69 53 67
 * ...
 *
 * There are at most 16 bytes per line.
 */
        static void interceptBits(Tooling::Logger &logger, const std::string &message) {
            static const std::regex bitsHeaderRegEx{"value of '([^']+)' \\(([0-9]+) bits\\) is:\n"};
            static const std::regex bitsRegEx{" (([0-9a-f]{2} ?){1,16})\n"};
            static std::string finalOutput{};
            if (!finalOutput.empty()) {
                std::smatch bitsMatch;
                if (std::regex_match(message, bitsMatch, bitsRegEx)) {
                    // Append the current bits line
                    finalOutput += bitsMatch[1];
                    finalOutput += ' ';
                } else {
                    logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__, finalOutput);
                    finalOutput.clear();
                }
            }
            // Check, if a string in the bits header is defined to be intercepted
            std::smatch bitsHeaderMatch;
            if (finalOutput.empty() && std::regex_match(message, bitsHeaderMatch, bitsHeaderRegEx)) {
                auto interception = bitsInterception.find(bitsHeaderMatch[1]);
                if (bitsInterception.cend() != interception) {
                    // Instruct to intercept the next lines
                    finalOutput = interception->second + '=';
                }
            }
        }

        void TlsLogFilter::registerInstances(Tooling::Logger &logger) {
            using namespace std::placeholders;
            logger.addLogFilter(std::bind(&translateString, _1, _4));
            logger.addLogFilter(std::bind(&translateRegEx, _1, _4));
            logger.addLogFilter(std::bind(&translateRegExDecToHex, _1, _4));
            logger.addLogFilter(std::bind(&interceptHexDump, _1, _4));
            logger.addLogFilter(std::bind(&interceptBits, _1, _4));
        }
    }
}
