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
#ifndef MANIPULATION_MANIPULATIONSPARSER_H_
#define MANIPULATION_MANIPULATIONSPARSER_H_

#include <string>

namespace TlsTestTool {
    class Configuration;

/**
 * Parser for configuration arguments describing a manipulation.
 */
    class ManipulationsParser {
    public:
        /**
         * Factory function parsing the given name-value pair and adding a manipulation to the configuration.
         *
         * @param name Name of a name-value pair
         * @param value Value of a name-value pair
         * @param configuration Configuration description that will be extended
         * @throw std::exception Thrown, if a parsing the manipulation fails.
         */
        static void parse(const std::string &name, const std::string &value, Configuration &configuration);
    };
}

#endif /* CONFIGURATION_CONFIGURATIONFILEPARSER_H_ */
