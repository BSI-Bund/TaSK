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
#include "TcpClient.h"
#include "TcpConnection.h"
#include "asio.hpp"

namespace TlsTestTool {

    class TcpClient::Data {
    public:
        asio::io_context io_context;
        std::shared_ptr<TcpConnection> tcpConnection;

        Data() : io_context(), tcpConnection(std::make_shared<TcpConnection>(&io_context)) {
        }
    };

    TcpClient::TcpClient() : impl(std::make_unique<Data>()) {
    }

    TcpClient::~TcpClient() = default;

    void TcpClient::connect(const std::string &host, const std::string &port) {
        asio::ip::tcp::resolver resolver(impl->io_context);
        asio::connect(impl->tcpConnection->getSocket(), resolver.resolve(host, port));
    }

    std::shared_ptr<TcpConnection> TcpClient::getTcpConnection() {
        return impl->tcpConnection;
    }
}
