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
#include "ManipulationsParser.h"
#include "ManipulateClientHelloCompressionMethods.h"
#include "ManipulateServerHelloCompressionMethods.h"
#include "ManipulateClientHelloExtensions.h"
#include "ManipulateServerHelloExtensions.h"
#include "ManipulateEllipticCurveGroup.h"
#include "ManipulateHelloVersion.h"
#include "Renegotiate.h"
#include "SendApplicationData.h"
#include "SendHeartbeatRequest.h"
#include "configuration/Configuration.h"
#include "strings/HexStringHelper.h"
#include "tls/TlsEllipticCurveGroup.h"
#include "tls/TlsHashAlgorithm.h"
#include "ForceCertificateUsage.h"
#include <cstdint>
#include <limits>
#include <regex>
#include <stdexcept>
#include <string>
#include <tuple>
#include <utility>
#include <stdio.h>

namespace TlsTestTool {
    static std::smatch matchValue(const std::string &name, const std::string &value, const std::regex &regEx) {
        std::smatch valueMatch;
        if (std::regex_match(value, valueMatch, regEx)) {
            if ((regEx.mark_count() + 1) != valueMatch.size()) {
                throw std::invalid_argument{std::string{"Invalid value \""} + value + "\" for " + name};
            }
            return valueMatch;
        } else {
            throw std::invalid_argument{std::string{"Invalid value \""} + value + "\" for " + name};
        }
    }

    static uint8_t matchByte(const std::string &name, const std::string &value, const std::string &byteString) {
        const auto longValue = std::stoul(byteString, 0, 16);
        if (std::numeric_limits<uint8_t>::max() < longValue) {
            throw std::invalid_argument{
                    std::string{"Invalid byte \""} + byteString + "\" in value \"" + value + "\" for "
                    + name};
        }
        return longValue;
    }

    static std::pair<uint8_t, uint8_t> matchHexPair(const std::string &name, const std::string &value) {
        const std::regex hexPairRegEx{"\\((0x[0-9a-fA-F]{2}),(0x[0-9a-fA-F]{2})\\)"};
        const auto valueMatch = matchValue(name, value, hexPairRegEx);
        const auto firstByte = matchByte(name, value, valueMatch[1]);
        const auto secondByte = matchByte(name, value, valueMatch[2]);
        return std::make_pair(firstByte, secondByte);
    }

    static std::vector<uint8_t> matchHexString(const std::string &name, const std::string &value) {
        std::smatch valueMatch;
        if (!std::regex_match(value, valueMatch, std::regex{"^([0-9a-fA-F]{2} ?)*$"})) {
            throw std::invalid_argument{std::string{"Invalid hexadecimal string \""} + value + "\" for " + name};
        }
        const std::regex hexValueRegEx{"([0-9a-fA-F]{2})"};
        auto hexValuesBegin = std::sregex_iterator(value.cbegin(), value.cend(), hexValueRegEx);
        auto hexValuesEnd = std::sregex_iterator();
        if (0 == std::distance(hexValuesBegin, hexValuesEnd)) {
            throw std::invalid_argument{std::string{"Invalid hexadecimal string \""} + value + "\" for " + name};
        }
        std::vector<uint8_t> bytes;
        for (auto hexValue = hexValuesBegin; hexValue != hexValuesEnd; ++hexValue) {
            if (2 != hexValue->size()) {
                throw std::invalid_argument{
                        std::string{"Invalid hexadecimal value \""} + hexValue->str() + "\" in value \""
                        + value + "\" for " + name};
            }
            bytes.push_back(matchByte(name, value, (*hexValue)[1]));
        }
        return bytes;
    }


    void ManipulationsParser::parse(const std::string &name, const std::string &value, Configuration &configuration) {

        if (name == "manipulateClientHelloCompressionMethods") {
            const auto bytes = matchHexString(name, value);
            configuration.addManipulation(std::make_unique<ManipulateClientHelloCompressionMethods>(bytes));
        } else if (name == "manipulateServerHelloCompressionMethod") {
            const auto bytes = matchHexString(name, value);
            configuration.addManipulation(std::make_unique<ManipulateServerHelloCompressionMethods>(bytes));
        } else if (name == "manipulateClientHelloExtensions") {
            if (configuration.getTlsLibrary() != Configuration::TlsLibrary::OPENSSL) {
                //delete delimiter
                std::string copyValue = value;
                std::string result;
                std::remove_copy(copyValue.begin(), copyValue.end(), std::back_inserter(result), ':');
                const auto bytes = matchHexString(name, result);
                configuration.addManipulation(std::make_unique<ManipulateClientHelloExtensions>(bytes));
            }

            if (configuration.getTlsLibrary() == Configuration::TlsLibrary::OPENSSL) {
                configuration.setClientHelloExtension(std::vector<unsigned char>(value.begin(), value.end()));
            }
        } else if (name == "manipulateHelloVersion") {
            const auto manipulatedVersion = matchHexPair(name, value);
            configuration.addManipulation(std::make_unique<ManipulateHelloVersion>(manipulatedVersion));
        } else if (name == "manipulateRenegotiate") {
            configuration.addManipulation(std::make_unique<Renegotiate>());
        } else if (name == "manipulateSendHeartbeatRequest") {
            const auto splitPosFirst = value.find_first_of(',');
            if (std::string::npos == splitPosFirst) {
                throw std::invalid_argument{std::string{"Invalid value \""} + value + "\" for " + name};
            }
            const auto when = value.substr(0, splitPosFirst);
            if (when != "beforeHandshake" && when != "afterHandshake") {
                throw std::invalid_argument{std::string{"Invalid when \""} + when + "\" in value \"" + value + "\" for "
                                            + name};
            }
            const auto splitPosLast = value.find_last_of(',');
            const auto payloadLengthStr = value.substr(splitPosFirst + 1, splitPosLast);
            const auto payloadLength = std::stoul(payloadLengthStr, nullptr, 10);
            if (65535 < payloadLength) {
                throw std::invalid_argument{
                        std::string{"Invalid payloadLength \""} + payloadLengthStr + "\" in value \""
                        + value + "\" for " + name};
            }
            const auto payload = Tooling::HexStringHelper::hexStringToByteArray(value.substr(splitPosLast + 1));
            configuration.addManipulation(
                    std::make_unique<SendHeartbeatRequest>(when, static_cast<uint16_t>(payloadLength), payload));
        } else if (name == "manipulateSendTlsApplicationData") {
            const auto splitPos = value.find_first_of(',');
            if (std::string::npos == splitPos) {
                throw std::invalid_argument{std::string{"Invalid value \""} + value + "\" for " + name};
            }
            const auto countStr = value.substr(0, splitPos);
            const auto numberSendData = std::stoull(countStr, 0, 10);
            if (1 > numberSendData) {
                throw std::invalid_argument{std::string{"Invalid count \""} + countStr + "\" in value \"" + value
                                            + "\" for " + name};
            }
            const auto applicationData = Tooling::HexStringHelper::hexStringToByteArray(value.substr(splitPos + 1));
            configuration.addManipulation(
                    std::make_unique<SendApplicationData>(static_cast<uint64_t>(numberSendData), applicationData));
        } else if (name == "manipulateEllipticCurveGroup") {
            const auto ellipticCurve = TlsEllipticCurveGroup::getPredefined(value);
            configuration.addManipulation(std::make_unique<ManipulateEllipticCurveGroup>(ellipticCurve));
        } else if (name == "manipulateServerHelloExtensions") {

            if (configuration.getTlsLibrary() != Configuration::TlsLibrary::OPENSSL) {
                //delete delimiter
                std::string copyValue = value;
                std::string result;
                std::remove_copy(copyValue.begin(), copyValue.end(), std::back_inserter(result), ':');
                const auto bytes = matchHexString(name, result);
                configuration.addManipulation(std::make_unique<ManipulateServerHelloExtensions>(bytes));
            }
            if (configuration.getTlsLibrary() == Configuration::TlsLibrary::OPENSSL) {
                configuration.setServerHelloExtension(std::vector<unsigned char>(value.begin(), value.end()));
            }
        } else if (name == "manipulateEncryptedExtensionsTls13") {
            if (configuration.getTlsLibrary() == Configuration::TlsLibrary::OPENSSL) {
                configuration.setEncryptedExtensionsTls13(std::vector<unsigned char>(value.begin(), value.end()));
            }
        } else if (name == "manipulateForceCertificateUsage") {
            configuration.addManipulation(std::make_unique<ForceCertificateUsage>());
        } else {
            throw std::invalid_argument{std::string{"Unknown manipulation "} + name};
        }
    }
}
