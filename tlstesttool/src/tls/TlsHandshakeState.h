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
#ifndef TLS_TLSHANDSHAKESTATE_H_
#define TLS_TLSHANDSHAKESTATE_H_

namespace TlsTestTool {
/**
 * Enumeration for the state of a TLS handshake. The enumerators represent the action that is expected next. For
 * example, @c TlsHandshakeState::CLIENT_HELLO means for the client that it has to send a ClientHello message next, and
 * for the server that it expects to receive a ClientHello message next, respectively.
 * @see RFC 5246, Figure 1 (https://tools.ietf.org/html/rfc5246#page-36)
 */
enum class TlsHandshakeState {
	HELLO_REQUEST,
	CLIENT_HELLO,
	SERVER_HELLO,
	END_OF_EARLY_DATA,
	ENCRYPTED_EXTENSIONS,
	HELLO_RETRY_REQUEST,
	SERVER_CERTIFICATE,
	SERVER_KEY_EXCHANGE,
	CERTIFICATE_REQUEST,
	SERVER_HELLO_DONE,
	CLIENT_CERTIFICATE,
	CLIENT_KEY_EXCHANGE,
	CERTIFICATE_VERIFY,
	CLIENT_CHANGE_CIPHER_SPEC,
	CLIENT_FINISHED,
	SERVER_CHANGE_CIPHER_SPEC,
	SERVER_FINISHED,
	INTERNAL_1,
	INTERNAL_2,
	HANDSHAKE_DONE
};
}

#endif /* TLS_TLSHANDSHAKESTATE_H_ */
