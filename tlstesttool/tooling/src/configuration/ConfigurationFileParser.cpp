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
#include "ConfigurationFileParser.h"
#include <iostream>
#include <regex>
#include <stdexcept>
#include <string>

namespace Tooling {
    std::vector<KeyValuePair> ConfigurationFileParser::parse(std::istream &input) {
        if (!input.good()) {
            throw std::invalid_argument("Cannot read input.");
        }
        const std::regex argRegEx{"([a-zA-Z0-9._]+)"};
        std::vector<KeyValuePair> keyValuePairs;
        while (input.good()) {
            std::string line;
            std::getline(input, line);
            // Ignore empty lines and lines starting with #
            if (line.empty() || ('#' == line.front())) {
                continue;
            }
            const auto splitPos = line.find_first_of('=');
            if (std::string::npos == splitPos) {
                throw std::invalid_argument{std::string{"Invalid argument "} + line};
            }
            const auto argument = line.substr(0, splitPos);
            std::smatch argMatch;
            if (std::regex_match(argument, argMatch, argRegEx)) {
                if (2 != argMatch.size()) {
                    throw std::invalid_argument{std::string{"Invalid argument "} + line};
                }
                keyValuePairs.emplace_back(argument, line.substr(splitPos + 1));
            } else {
                throw std::invalid_argument{std::string{"Invalid argument "} + line};
            }
        }
        return keyValuePairs;
    }
}
