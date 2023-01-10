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
#ifndef TLS_TLSSIGNATUREALGORITHM_H_
#define TLS_TLSSIGNATUREALGORITHM_H_

#include "tls/TlsNumbers.h"

namespace TlsTestTool {
/**
 * Types of TLS signature algorithms.
 *
 * @see RFC 5246, Section 7.4.1.4.1
 */
enum class TlsSignatureAlgorithm : TlsUint8 { ANONYMOUS = 0, RSA = 1, DSA = 2, ECDSA = 3 };
}

#endif /* TLS_TLSSIGNATUREALGORITHM_H_ */
