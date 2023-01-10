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
#ifndef TLS_OPENSSL_TLSLOGGER_H_
#define TLS_OPENSSL_TLSLOGGER_H_

#include <cstdio>

namespace Tooling {
class Logger;
}
namespace TlsTestTool {
namespace OpenSsl {
/**
 * Implementation of TlsLog for LibreSSL specific log messages and general log messages.
 */
class TlsLogger {
public:
	/**
	 * Log OpenSSL specific log messages.
	 * This code is taken from the internal libressl function and only slightly changed to use the logger.
	 * @param logger Destination of log messages
	 * @param write_p The direction
	 * @param version The TLS version
	 * @param content_type The content type
	 * @param buf The buffer
	 * @param len The buffer length 
	 */
	static void logInternalTls(Tooling::Logger * logger, int write_p, int version, int content_type, const void * buf, size_t len);

   /**
	* Log general log messages.
	* @param logger Destination of log messages
	* @param write_p The direction
	* @param version The TLS version
	* @param content_type The content type
	* @param buf The buffer
	* @param len The buffer length
	*/
   static void logTls(Tooling::Logger * logger, int write_p, int version, int content_type, const void * buf, size_t len);
};
}
}

#endif /* TLS_OPENSSL_TLSLOGGER_H_ */
