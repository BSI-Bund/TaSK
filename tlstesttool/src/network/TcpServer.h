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
#ifndef NETWORK_TCPSERVER_H_
#define NETWORK_TCPSERVER_H_

#include "TcpConnection.h"
#include "asio/ip/tcp.hpp"

namespace TlsTestTool {
    class TcpClient;

/**
 * TCP/IP server socket working with a single client connection.
 */
    class TcpServer {
    public:
        class Data;

        /**
         * Construct a non-connected TCP/IP server socket.
         */
        TcpServer();

        /**
         * Free the TCP/IP server socket.
         */
        ~TcpServer();

        /**
         * Bind the TCP/IP server socket and listen for incoming connections.
         *
         * @param port TCP port number to listen to.
         * @throw std::exception Thrown on failure.
         */
        void listen(uint16_t port);


        /**
         * Perform pending tasks on the TCP/IP server socket. This function has to be called regularly from the event loop.
         */
        void work();

        /**
         * Get the currently active TcpConnection
         *
         * @return
         */
        std::shared_ptr<TcpConnection> &getActiveTcpConnection();

        /**
         * Returns the acceptor of the TcpServer
         * @return acceptor
         */
        asio::ip::tcp::acceptor &getAcceptor();

        /**
         * Add a new TcpConnection to server
         * @return new added tcpConnection
         */
        std::shared_ptr<TcpConnection> addNewTcpConnection();


    private:
        //! Use pimpl idiom.
        std::unique_ptr<Data> impl;

    };
}

#endif /* NETWORK_TCPSERVER_H_ */
