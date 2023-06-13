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
#include "ConfigurationParser.h"
#include "configuration/Configuration.h"
#include "manipulation/ManipulationsParser.h"
#include "strings/HexStringHelper.h"
#include <regex>
#include <stdexcept>
#include <string>

namespace TlsTestTool {
    static const std::string manipulatePrefix{"manipulate"};

    void ConfigurationParser::updateConfiguration(Configuration &configuration,
                                                  const std::vector<Tooling::KeyValuePair> &keyValuePairs) {
        bool tlsLibrarySet = false;
        for (const auto &keyValuePair: keyValuePairs) {
            const std::string &name = keyValuePair.first;
            const std::string &value = keyValuePair.second;
            if (name == "tlsLibrary") {
                if (value == "mbed TLS") {
                    configuration.setTlsLibrary(Configuration::TlsLibrary::MBED_TLS);
                } else if (value == "TLS_ATTACKER") {
                    configuration.setTlsLibrary(Configuration::TlsLibrary::TLS_ATTACKER);
                } else if (value == "GnuTLS") {
                    configuration.setTlsLibrary(Configuration::TlsLibrary::GNUTLS);
                } else if (value == "OpenSSL") {
                    configuration.setTlsLibrary(Configuration::TlsLibrary::OPENSSL);
                } else {
                    throw std::invalid_argument{std::string{"Unknown TLS library "} + value};
                }
                tlsLibrarySet = true;
                break;
            }
        }
        //set default TLS-Library here (or set default value in Config Constructor)
        if (!tlsLibrarySet) {
            configuration.setTlsLibrary(Configuration::TlsLibrary::OPENSSL);
        }

        for (const auto &keyValuePair: keyValuePairs) {
            const std::string &name = keyValuePair.first;
            const std::string &value = keyValuePair.second;
            if (name == "tlsLibrary") {
                continue;
            }
            if (name == "mode") {
                if (value == "client") {
                    configuration.setMode(Configuration::NetworkMode::CLIENT);
                } else if (value == "server") {
                    configuration.setMode(Configuration::NetworkMode::SERVER);
                } else {
                    throw std::invalid_argument{std::string{"Unknown mode "} + value};
                }
            } else if (name == "host") {
                configuration.setHost(value);
            } else if (name == "port") {
                configuration.setPort(std::stoul(value));
            } else if (name == "listenTimeout") {
                configuration.setListenTimeoutSeconds(std::stoul(value));
            } else if (name == "waitBeforeClose") {
                configuration.setWaitBeforeCloseSeconds(std::stoul(value));
            } else if (name == "receiveTimeout") {
                configuration.setTcpReceiveTimeoutSeconds(std::stoul(value));
            } else if (name == "sessionLifetime") {
                configuration.setSessionLifetime(std::stoi(value));
            } else if (name == "logLevel") {
                if (value == "high") {
                    configuration.setLogLevel(Tooling::LogLevel::HIGH);
                } else if (value == "medium") {
                    configuration.setLogLevel(Tooling::LogLevel::MEDIUM);
                } else if (value == "low") {
                    configuration.setLogLevel(Tooling::LogLevel::LOW);
                } else if (value == "off") {
                    configuration.setLogLevel(Tooling::LogLevel::OFF);
                } else {
                    throw std::invalid_argument{std::string{"Unknown log level "} + value};
                }
            } else if (name == "logFilterRegEx") {
                configuration.setLogFilterRegEx(value);
            } else if (name == "caCertificateFile") {
                configuration.setCaCertificateFile(value);
            } else if (name == "certificateFile") {
                configuration.setCertificateFile(value);
            } else if (name == "privateKeyFile") {
                configuration.setPrivateKeyFile(value);
            } else if (name == "tlsVersion") {
                const std::regex numberPairRegEx{"\\(([0-9]+),([0-9]+)\\)"};
                std::smatch valueMatch;
                if (std::regex_match(value, valueMatch, numberPairRegEx)) {
                    if (3 != valueMatch.size()) {
                        throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                    }
                    const auto major = std::stoul(valueMatch[1]);
                    const auto minor = std::stoul(valueMatch[2]);
                    if (3 != major) {
                        throw std::invalid_argument{std::string{"Invalid major version for "} + name + " " + value};
                    }
                    if ((4 < minor)) {
                        throw std::invalid_argument{std::string{"Invalid minor version for "} + name + " " + value};
                    }
                    configuration.setTlsVersion(
                            std::make_pair(static_cast<uint8_t>(major), static_cast<uint8_t>(minor)));
                } else {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
            } else if (name == "tlsUseSni") {
                if (value == "true") {
                    configuration.setTlsUseSni(true);
                } else if (value == "false") {
                    configuration.setTlsUseSni(false);
                } else {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
            } else if (name == "tlsVerifyPeer") {
                if (value == "true") {
                    configuration.setTlsVerifyPeer(true);
                } else if (value == "false") {
                    configuration.setTlsVerifyPeer(false);
                } else {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
            } else if (name == "tlsEncryptThenMac") {
                if (value == "true") {
                    configuration.setTlsEncryptThenMac(true);
                } else if (value == "false") {
                    configuration.setTlsEncryptThenMac(false);
                } else {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
            } else if (name == "tlsExtendedMasterSecret") {
                if (value == "true") {
                    configuration.setTlsExtendedMasterSecret(true);
                } else if (value == "false") {
                    configuration.setTlsExtendedMasterSecret(false);
                } else {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
            } else if (name == "tlsCipherSuites") {
                const std::regex hexPairRegEx{"\\((0x[0-9a-fA-F]{2}),(0x[0-9a-fA-F]{2})\\)"};
                auto hexPairsBegin = std::sregex_iterator(value.begin(), value.end(), hexPairRegEx);
                auto hexPairsEnd = std::sregex_iterator();
                if (0 == std::distance(hexPairsBegin, hexPairsEnd)) {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
                configuration.clearTlsCipherSuites();
                for (auto hexPair = hexPairsBegin; hexPair != hexPairsEnd; ++hexPair) {
                    auto valueMatch = *hexPair;
                    if (3 != valueMatch.size()) {
                        throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                    }
                    const std::string upperStr{valueMatch[1]};
                    const auto upper = std::stoul(upperStr, 0, 16);
                    if (255 < upper) {
                        throw std::invalid_argument{
                                std::string{"Invalid upper byte "} + upperStr + " for " + name + " in "
                                + value};
                    }
                    const std::string lowerStr{valueMatch[2]};
                    const auto lower = std::stoul(lowerStr, 0, 16);
                    if (255 < lower) {
                        throw std::invalid_argument{
                                std::string{"Invalid lower byte "} + lowerStr + " for " + name + " in "
                                + value};
                    }
                    configuration.addTlsCipherSuite(
                            std::make_pair(static_cast<uint8_t>(upper), static_cast<uint8_t>(lower)));
                }
            } else if (name == "tlsServerDHParams") {
                configuration.setTlsServerDHParams(value);
            } else if (name == "tlsSecretFile") {
                configuration.setTlsSecretFile(value);
            } else if (name == "tlsSupportedGroups") {
                const std::regex supportedGroupRegEx{"[0-9a-zPX\\-]+"};
                auto supportedGroupBegin = std::sregex_iterator(value.begin(), value.end(), supportedGroupRegEx);
                auto supportedGroupEnd = std::sregex_iterator();
                if (0 == std::distance(supportedGroupBegin, supportedGroupEnd)) {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
                configuration.clearTlsSupportedGroups();
                for (auto supportedGroup = supportedGroupBegin; supportedGroup != supportedGroupEnd; ++supportedGroup) {
                    std::smatch supportedGroupMatch = *supportedGroup;
                    configuration.addTlsSupportedGroup(supportedGroupMatch.str());
                }
            } else if (name == "tlsSignatureSchemes") {
                const std::regex hexPairRegEx{"\\((0x[0-9a-fA-F]{2}),(0x[0-9a-fA-F]{2})\\)"};
                auto hexPairsBegin = std::sregex_iterator(value.begin(), value.end(), hexPairRegEx);
                auto hexPairsEnd = std::sregex_iterator();
                if (0 == std::distance(hexPairsBegin, hexPairsEnd)) {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
                configuration.clearTlsSignatureSchemes();
                for (auto hexPair = hexPairsBegin; hexPair != hexPairsEnd; ++hexPair) {
                    auto valueMatch = *hexPair;
                    if (3 != valueMatch.size()) {
                        throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                    }
                    const std::string upperStr{valueMatch[1]};
                    const auto upper = std::stoul(upperStr, 0, 16);
                    if (255 < upper) {
                        throw std::invalid_argument{
                                std::string{"Invalid upper byte "} + upperStr + " for " + name + " in "
                                + value};
                    }
                    const std::string lowerStr{valueMatch[2]};
                    const auto lower = std::stoul(lowerStr, 0, 16);
                    if (255 < lower) {
                        throw std::invalid_argument{
                                std::string{"Invalid lower byte "} + lowerStr + " for " + name + " in "
                                + value};
                    }
                    configuration.addTlsSignatureScheme(
                            std::make_pair(static_cast<uint8_t>(upper), static_cast<uint8_t>(lower)));
                }
            } else if (name == "tlsSignatureAlgorithms") {
                const std::regex numberPairRegEx{"\\(([0-9]+),([0-9]+)\\)"};
                auto numberPairsBegin = std::sregex_iterator(value.begin(), value.end(), numberPairRegEx);
                auto numberPairsEnd = std::sregex_iterator();
                if (0 == std::distance(numberPairsBegin, numberPairsEnd)) {
                    throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                }
                configuration.clearTlsSignatureAlgorithms();
                for (auto numberPair = numberPairsBegin; numberPair != numberPairsEnd; ++numberPair) {
                    auto valueMatch = *numberPair;
                    if (3 != valueMatch.size()) {
                        throw std::invalid_argument{std::string{"Invalid value for "} + name + " " + value};
                    }
                    const auto signature = std::stoul(valueMatch[1]);
                    if (3 < signature) {
                        throw std::invalid_argument{std::string{"Invalid signature version for "} + name + " " + value};
                    }
                    const auto hash = std::stoul(valueMatch[2]);
                    if ((6 < hash)) {
                        throw std::invalid_argument{std::string{"Invalid hash version for "} + name + " " + value};
                    }
                    configuration.addTlsSignatureAlgorithm(
                            std::make_pair(static_cast<uint8_t>(signature), static_cast<uint8_t>(hash)));
                }
            }
                //Values for Session Resumption
            else if (name == "sessionCache") {
                configuration.setSessionCache(value);
            } else if (name == "earlyData") {
                configuration.setEarlyData(Tooling::HexStringHelper::hexStringToByteArray(value));
            } else if (name == "psk") {
                configuration.setPreSharedKey(Tooling::HexStringHelper::hexStringToByteArray(value));
            } else if (name == "pskIdentity") {
                configuration.setPskIdentity(value);
            } else if (name == "pskIdentityHint") {
                configuration.setPskIdentityHint(value);
            } else if (name == "handshakeType") {
                if (value == "normal") {
                    configuration.setHandshakeType(Configuration::HandshakeType::NORMAL);
                } else if (value == "resumptionWithSessionID") {
                    configuration.setHandshakeType(Configuration::HandshakeType::SESSION_RESUMPTION_WITH_SESSION_ID);
                } else if (value == "resumptionWithSessionTicket") {
                    configuration.setHandshakeType(Configuration::HandshakeType::SESSION_RESUMPTION_WITH_TICKET);
                } else if (value == "zeroRTT") {
                    configuration.setHandshakeType(Configuration::HandshakeType::ZERO_RTT);
                } else {
                    throw std::invalid_argument{std::string{"Unknown handshakeType argument "} + name};
                }
            } else if (name == "ocspResponseFile") {
                configuration.setOcspResponseFile(value);
            } else if (name == "startTLSProtocol") {
                if (value == "smtp") {
                    configuration.setStartTlsProtocol(Configuration::StartTLSProtocol::SMTP);
                } else if (value == "imap") {
                    configuration.setStartTlsProtocol(Configuration::StartTLSProtocol::IMAP);
                } else if (value == "pop3") {
                    configuration.setStartTlsProtocol(Configuration::StartTLSProtocol::POP3);
                } else if (value == "ftp") {
                    configuration.setStartTlsProtocol(Configuration::StartTLSProtocol::FTP);
                } else {
                    throw std::invalid_argument{std::string{"Invalid StartTLS protocol: "} + value};
                }
            } else if (name.substr(0, manipulatePrefix.size()) == manipulatePrefix) {
                ManipulationsParser::parse(name, value, configuration);
            } else {
                throw std::invalid_argument{std::string{"Unknown argument "} + name};
            }
        }
    }
}
