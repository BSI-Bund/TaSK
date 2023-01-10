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
#ifndef CONFIGURATION_CONFIGURATIONPARSER_H_
#define CONFIGURATION_CONFIGURATIONPARSER_H_

#include "configuration/KeyValuePair.h"
#include <vector>

namespace TlsTestTool {
class Configuration;
/**
 * Parser for configuration arguments given as key-value pairs.
 *
 */
class ConfigurationParser {
public:
	/**
	 * Factory function parsing the configuration arguments and storing them in a configuration description.
	 *
	 * @param configuration Configuration description that will be updated
	 * @param keyValuePairs Array of key-value pairs containing the configuration arguments
	 * @return Configuration description
	 * @throw std::exception Thrown, if a required argument is missing, or a error occurred during reading.
	 */
	static void updateConfiguration(Configuration & configuration,
									const std::vector<Tooling::KeyValuePair> & keyValuePairs);
};
}

#endif /* CONFIGURATION_CONFIGURATIONPARSER_H_ */
