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
#ifndef CONFIGURATION_CONFIGURATIONLOADER_H_
#define CONFIGURATION_CONFIGURATIONLOADER_H_

namespace TlsTestTool {
class Configuration;
/**
 * Loader for tht TLS test tool's configuration.
 *
 */
class ConfigurationLoader {
public:
	/**
	 * Factory function parsing the command line arguments and configuration arguments. The path to at least one
	 * configuration file is read from the command line. Then, the configuration arguments are parsed from the
	 * configuration files and stored in a configuration description.
	 *
	 * @param argc Number of arguments in @p argv
	 * @param argv Array of command line arguments
	 * @return Configuration description
	 * @throw std::exception Thrown, if a required argument is missing, or an error occurred during reading.
	 */
	static Configuration parse(const int argc, const char ** argv);
};
}

#endif /* CONFIGURATION_CONFIGURATIONLOADER_H_ */
