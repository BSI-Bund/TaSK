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
#include "TlsSessionFactory.h"
#include "configuration/Configuration.h"
#include "openssl/TlsSession.h"
#include "mbedtls/TlsSession.h"
#include <stdexcept>

namespace TlsTestTool {
std::shared_ptr<TlsSession> TlsSessionFactory::createClientSession(
		const Configuration::TlsLibrary & tlsLibrary, TcpClient & tcpClient) {
	switch (tlsLibrary) {
 	case Configuration::TlsLibrary::OPENSSL:
 		return std::make_shared<OpenSsl::TlsSession>(tcpClient);
	case Configuration::TlsLibrary::MBED_TLS:
		return std::make_shared<MbedTls::TlsSession>(tcpClient);
	default:
		throw std::invalid_argument("Unknown TLS library requested");
	};
}

std::shared_ptr<TlsSession> TlsSessionFactory::createServerSession(
		const Configuration::TlsLibrary &tlsLibrary, TcpServer &tcpServer) {
	switch (tlsLibrary) {
 	case Configuration::TlsLibrary::OPENSSL:
 		return std::make_shared<OpenSsl::TlsSession>(tcpServer);
	case Configuration::TlsLibrary::MBED_TLS:
		return std::make_shared<MbedTls::TlsSession>(tcpServer);
	default:
		throw std::invalid_argument("Unknown TLS library requested");
	};
}
}
