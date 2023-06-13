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
#include "ConfigurationLoader.h"
#include "configuration/ConfigurationParser.h"
#include "configuration/CommandLineParser.h"
#include "configuration/Configuration.h"
#include "configuration/ConfigurationFileParser.h"
#include "configuration/KeyValuePair.h"
#include <fstream>
#include <stdexcept>
#include <string>
#include <vector>

namespace TlsTestTool {
    Configuration ConfigurationLoader::parse(const int argc, const char **argv) {
        const auto cmdLineArguments = Tooling::CommandLineParser::parseArgs(argc, argv);
        Configuration configuration;
        for (const auto &cmdLineArgument: cmdLineArguments) {
            if ("configFile" == cmdLineArgument.first) {
                std::ifstream configFile(cmdLineArgument.second);
                const auto configArguments = Tooling::ConfigurationFileParser::parse(configFile);
                ConfigurationParser::updateConfiguration(configuration, configArguments);
            } else {
                throw std::invalid_argument{std::string{"Unknown command line argument "} + cmdLineArgument.first};
            }
        }
        if (Configuration::NetworkMode::UNKNOWN == configuration.getMode()) {
            throw std::runtime_error{"Missing required argument mode."};
        }
        if ((Configuration::NetworkMode::CLIENT == configuration.getMode()) && configuration.getHost().empty()) {
            throw std::runtime_error{"Missing required argument host."};
        }
        if (0 == configuration.getPort()) {
            throw std::runtime_error{"Missing required argument port."};
        }
        return configuration;
    }
}
