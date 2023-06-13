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
#include "TcpServer.h"
#include "TcpConnection.h"
#include "asio/io_context.hpp"
#include "configuration/Configuration.h"


namespace TlsTestTool {

    class TcpServer::Data {
    public:
        asio::io_context io_context;
        asio::ip::tcp::acceptor acceptor;
        std::vector<std::shared_ptr<TcpConnection>> tcpConnections; /*for evey connected client a new tcp connection is created*/
        int activeTcpConnectionIndex;

        TlsTestTool::Configuration *configuration;
        Tooling::Logger *logger;
        TlsTestTool::TlsSession *tlsSession;

        Data()
                : io_context(), acceptor(io_context), tcpConnections(), activeTcpConnectionIndex(0), configuration(),
                  logger(), tlsSession() {
            /*add the default tcpConnection*/
            tcpConnections.push_back(std::make_shared<TcpConnection>(&io_context));
        }
    };

    TcpServer::TcpServer() : impl(std::make_unique<Data>()) {
    }

    TcpServer::~TcpServer() = default;

    void TcpServer::listen(uint16_t port) {
        const asio::ip::tcp::endpoint endpoint(asio::ip::tcp::v4(), port);
        impl->acceptor.open(endpoint.protocol());
        impl->acceptor.set_option(asio::ip::tcp::acceptor::reuse_address(true));
        impl->acceptor.bind(endpoint);
        impl->acceptor.listen();
    }

    void TcpServer::work() {
        impl->io_context.poll();
    }

    asio::ip::tcp::acceptor &TcpServer::getAcceptor() {
        return impl->acceptor;
    }

    std::shared_ptr<TcpConnection> &TcpServer::getActiveTcpConnection() {
        return impl->tcpConnections[impl->activeTcpConnectionIndex];
    }

    std::shared_ptr<TcpConnection> TcpServer::addNewTcpConnection() {
        auto newConnection = std::make_shared<TcpConnection>(&impl->io_context);
        impl->tcpConnections.push_back(newConnection);
        impl->activeTcpConnectionIndex++;
        return newConnection;
    }
}
