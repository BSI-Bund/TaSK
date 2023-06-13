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
#ifndef NETWORK_TCPCLIENT_H_
#define NETWORK_TCPCLIENT_H_

#include "TcpConnection.h"
#include <cstdlib>
#include <string>


namespace TlsTestTool {
    class AbstractSocketObserver;

/**
 * TCP/IP client socket.
 */
    class TcpClient {
    public:
        class Data;

    private:
        //! Use pimpl idiom.
        std::unique_ptr<Data> impl;

    public:
        /**
         * Construct a non-connected TCP/IP client socket.
         */
        TcpClient();

        /**
         * Free the TCP/IP client socket.
         */
        ~TcpClient();

        /**
         * Connect the socket to the given host and port.
         *
         * @param IP address or host name of the host to connect to.
         * @param port TCP port number of the service to connect to.
         * @throw std::exception Thrown on failure.
         */
        void connect(const std::string &host, const std::string &port);

        /**
         * Return the TcpConnection of the client
         * @return tcpConnection
         */
        std::shared_ptr<TcpConnection> getTcpConnection();
    };
}

#endif /* NETWORK_TCPCLIENT_H_ */
