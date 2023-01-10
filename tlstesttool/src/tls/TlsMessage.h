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
#ifndef TLS_TLSMESSAGE_H_
#define TLS_TLSMESSAGE_H_

#include "tls/TlsHandshakeState.h"
#include <stdexcept>
#include <string>

namespace TlsTestTool {
/**
 * Conversion functions between TLS message names as strings and enumerators for TLS handshake states. Additional
 * functions to classify TLS handshake states.
 *
 * @see https://tools.ietf.org/html/rfc5246#section-7.1
 * @see https://tools.ietf.org/html/rfc5246#section-7.4
 * @see https://tools.ietf.org/html/rfc5246#section-10
 */
namespace TlsMessage {
/**
 * Convert a TLS handshake state to the name of a TLS message.
 * @param handshakeState TLS handshake state
 * @return Name of the corresponding TLS message
 */
inline std::string fromHandshakeState(const TlsHandshakeState handshakeState) {
	switch (handshakeState) {
		case TlsHandshakeState::HELLO_REQUEST:
			return "HelloRequest";
		case TlsHandshakeState::CLIENT_HELLO:
			return "ClientHello";
		case TlsHandshakeState::SERVER_HELLO:
			return "ServerHello";
		case TlsHandshakeState::CLIENT_CERTIFICATE:
		case TlsHandshakeState::SERVER_CERTIFICATE:
			return "Certificate";
		case TlsHandshakeState::SERVER_KEY_EXCHANGE:
			return "ServerKeyExchange";
		case TlsHandshakeState::CERTIFICATE_REQUEST:
			return "CertificateRequest";
		case TlsHandshakeState::SERVER_HELLO_DONE:
			return "ServerHelloDone";
		case TlsHandshakeState::CLIENT_KEY_EXCHANGE:
			return "ClientKeyExchange";
		case TlsHandshakeState::CERTIFICATE_VERIFY:
			return "CertificateVerify";
		case TlsHandshakeState::CLIENT_FINISHED:
		case TlsHandshakeState::SERVER_FINISHED:
			return "Finished";
		case TlsHandshakeState::CLIENT_CHANGE_CIPHER_SPEC:
		case TlsHandshakeState::SERVER_CHANGE_CIPHER_SPEC:
			return "ChangeCipherSpec";
		case TlsHandshakeState::HANDSHAKE_DONE:
			return "Application";
		default:
			throw std::invalid_argument{"Unsupported TLS handshake state"};
	}
}

/**
 * Determine, if a TLS message is sent in the current TLS handshake state.
 * @param isClient If @c true, the answer is generated for a TLS client. If @c false, it is generated for a TLS
 * server.
 * @param currentState TLS handshake state
 * @return @c true, if and only if a TLS message is sent in the current TLS handshake state
 */
inline bool isSent(const bool isClient, const TlsHandshakeState currentState) {
	if (isClient) {
		return (TlsHandshakeState::CLIENT_HELLO == currentState)
				|| (TlsHandshakeState::CLIENT_CERTIFICATE == currentState)
				|| (TlsHandshakeState::CLIENT_KEY_EXCHANGE == currentState)
				|| (TlsHandshakeState::CERTIFICATE_VERIFY == currentState)
				|| (TlsHandshakeState::CLIENT_CHANGE_CIPHER_SPEC == currentState)
				|| (TlsHandshakeState::CLIENT_FINISHED == currentState);
	} else {
		return (TlsHandshakeState::SERVER_HELLO == currentState)
				|| (TlsHandshakeState::SERVER_CERTIFICATE == currentState)
				|| (TlsHandshakeState::SERVER_KEY_EXCHANGE == currentState)
				|| (TlsHandshakeState::CERTIFICATE_REQUEST == currentState)
				|| (TlsHandshakeState::SERVER_HELLO_DONE == currentState)
				|| (TlsHandshakeState::SERVER_CHANGE_CIPHER_SPEC == currentState)
				|| (TlsHandshakeState::SERVER_FINISHED == currentState);
	}
}
}
}

#endif /* TLS_TLSMESSAGE_H_ */
