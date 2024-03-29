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
#ifndef TLS_TLSHANDSHAKEHEADER_H_
#define TLS_TLSHANDSHAKEHEADER_H_

#include "tls/TlsHandshakeType.h"
#include "tls/TlsNumbers.h"

namespace TlsTestTool {
/**
 * Type for accessing a Handshake message's header elements.
 *
 * @see RFC 5246, Section 7.4
 */
    struct TlsHandshakeHeader {
        TlsHandshakeType msgType;
        TlsUint24 length;
    };
}

#endif /* TLS_TLSHANDSHAKEHEADER_H_ */
