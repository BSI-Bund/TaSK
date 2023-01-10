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
#ifndef TLS_OPENSSL_TLSMESSAGELOGGER_H_
#define TLS_OPENSSL_TLSMESSAGELOGGER_H_

#include "tls/TlsHandshakeType.h"
#include <cstdint>
#include <vector>

namespace Tooling {
class Logger;
}
namespace TlsTestTool {
namespace OpenSsl {
/**
 * Writing of log messages from OpenSSL specific data.
 */
class TlsMessageLogger {
public:
	/**
	 * Log the given TLS handshake message.
	 * @param logger Destination of log messages
	 * @param type Type of handshake message
	 * @param isSent @c true if the message is outgoing, @c false if the message is incoming.
	 * @param msg Data of the TLS handshake message without header.
	 */
	static void logTlsHandshakeMessage(Tooling::Logger & logger, const TlsHandshakeType & type, const bool isSent,
									   const std::vector<uint8_t> & msg);
};
}
}

#endif /* TLS_OPENSSL_TLSMESSAGELOGGER_H_ */
