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
#ifndef LOGGING_LOGLEVEL_H_
#define LOGGING_LOGLEVEL_H_

namespace Tooling {
/**
 * Enumeration for different log levels.
 */
enum class LogLevel {
	//! No output.
	OFF,
	//! Little debug output (e.g., print actions that are performed).
	LOW,
	//! Medium amount of debug output (e.g., additional output of sizes of received packages).
	MEDIUM,
	//! Much debug output (e.g., additional hex dumps).
	HIGH
};
}

#endif /* LOGGING_LOGLEVEL_H_ */
