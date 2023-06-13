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
#ifndef CONFIGURATION_COMMANDLINEPARSER_H_
#define CONFIGURATION_COMMANDLINEPARSER_H_

#include "configuration/KeyValuePair.h"
#include <vector>

namespace Tooling {
/**
 * Parser for command line arguments.
 */
    class CommandLineParser {
    public:
        /**
         * Factory function parsing the command line arguments and extracting key-value pairs. Arguments of the form
         * "--key=value" are accepted.
         *
         * @param argc Number of arguments in @p argv
         * @param argv Array of arguments
         * @return Array of key-value pairs
         * @throw std::exception Thrown, if an argument with unexpected format is found.
         */
        static std::vector<KeyValuePair> parseArgs(const int argc, const char **argv);
    };
}

#endif /* CONFIGURATION_COMMANDLINEPARSER_H_ */
