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
#ifndef TLS_TLSSESSIONFACTORY_H_
#define TLS_TLSSESSIONFACTORY_H_

#include "configuration/Configuration.h"
#include <memory>

namespace TlsTestTool {
class TcpClient;
class TcpServer;
class TlsSession;
/**
 * Factory that creates a TlsSession implementation based on a string identifier.
 */
class TlsSessionFactory {
public:
	/**
	 * Create a TLS client session.
	 * @param tlsLibrary Identifier to select the TLS library.
	 * @param tcpClient Connected TCP/IP client that will be used to send and receive data.
	 * @return TlsSession implementation
	 */
	static std::shared_ptr<TlsSession> createClientSession(const Configuration::TlsLibrary & tlsLibrary,
														   TcpClient & tcpClient);

	/**
	 * Create a TLS server session.
	 * @param tlsLibrary Identifier to select the TLS library.
	 * @param tcpServer TCP/IP server that will be used to send and receive data.
	 * @return TlsSession implementation
	 */
	static std::shared_ptr<TlsSession> createServerSession(const Configuration::TlsLibrary & tlsLibrary,
														   TcpServer & tcpServer);
};
}

#endif /* TLS_TLSSESSIONFACTORY_H_ */
