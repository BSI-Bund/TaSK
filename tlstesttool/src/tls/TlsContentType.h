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
#ifndef TLS_TLSCONTENTTYPE_H_
#define TLS_TLSCONTENTTYPE_H_

#include "tls/TlsNumbers.h"

namespace TlsTestTool {
/**
 * Types of TLS records.
 *
 * @see RFC 5246, Section 6.2.1
 */
enum class TlsContentType : TlsUint8 {
	CHANGE_CIPHER_SPEC = 20,
	ALERT = 21,
	HANDSHAKE = 22,
	APPLICATION_DATA = 23,
	/**
	 * heartbeat content type (24)
	 * @see http://tools.ietf.org/html/rfc6520#section-6
	 */
	HEARTBEAT = 24
};
}

#endif /* TLS_TLSCONTENTTYPE_H_ */
