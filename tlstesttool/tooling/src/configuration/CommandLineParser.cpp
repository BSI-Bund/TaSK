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
#include "configuration/CommandLineParser.h"
#include "configuration/KeyValuePair.h"
#include <regex>
#include <stdexcept>
#include <string>

namespace Tooling {
    std::vector<KeyValuePair> CommandLineParser::parseArgs(const int argc, const char **argv) {
        if (1 > argc) {
            throw std::invalid_argument("At least one argument expected.");
        }
        const std::regex argRegEx{"--([a-zA-Z]+)=(.+)"};
        std::vector<KeyValuePair> keyValuePairs;
        keyValuePairs.reserve(argc - 1);
        // Ignore first argument: program name
        for (int i = 1; i < argc; ++i) {
            const char *arg = argv[i];
            std::cmatch argMatch;
            if (std::regex_match(arg, argMatch, argRegEx)) {
                if (3 != argMatch.size()) {
                    throw std::invalid_argument{std::string{"Invalid argument "} + arg};
                }
                keyValuePairs.emplace_back(argMatch[1], argMatch[2]);
            } else {
                throw std::invalid_argument{std::string{"Invalid argument "} + arg};
            }
        }
        return keyValuePairs;
    }
}
