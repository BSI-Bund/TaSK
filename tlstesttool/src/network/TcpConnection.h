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
#ifndef NETWORK_TCPCONNECTION_H
#define NETWORK_TCPCONNECTION_H

#include "AbstractSocketObserver.h"
#include <asio/ip/tcp.hpp>

namespace TlsTestTool {
class TcpClient;
/**
 * TcpConnection represents a Tcp connection for either client/server
 * It mainly consists of a socket.
 */
class TcpConnection {
public:
	class Data;
	/**
	 * Creates a new TcpConnection by opening a new socket
	 */
	TcpConnection(asio::io_context* io_context);

	/**
	 * Free the TCP/IP server socket.
	 */
	~TcpConnection();

	/**
	 * Close an open connection.
	 *
	 * @throw std::exception Thrown on failure, e.g., no open connection.
	 */
	void close();

	/**
	 * Write a block of characters to the TCP/IP client socket.
	 *
	 * @param data Data block to write.
	 * @return Number of bytes written, if successful. Zero, otherwise.
	 * @throw std::exception Thrown on failure.
	 */
	std::size_t write(const std::vector<char> & data);

	/**
	 * Read a block of characters from the TCP/IP client socket.
	 *
	 * @param length Number of bytes to read. The call will return only successfully, if this number of bytes have been
	 * read.
	 * @return Data block that has been read.
	 * @throw std::exception Thrown on failure.
	 */
	std::vector<char> read(std::size_t length);

	/**
	 * Get the number of bytes that are available for reading.
	 *
	 * @return Number of bytes that can be read.
	 * @throw std::exception Thrown on failure.
	 */
	std::size_t available() const;

	/**
	 * Check, if the connection is closed.
	 *
	 * @return @code true, if the socket is not connected. @code false, if a connection exists.
	 * @throw std::exception Thrown on failure.
	 */
	bool isClosed();

	/**
	 * Get the IP address of a connected peer.
	 *
	 * @return IP address in dotted decimal format.
	 * @throw std::exception Thrown on failure, e.g., no open connection.
	 */
	std::string getRemoteIpAddress() const;

	/**
	 * Get the TCP port of a connected peer.
	 *
	 * @return TCP port number
	 * @throw std::exception Thrown on failure, e.g., no open connection.
	 */
	uint16_t getRemoteTcpPort() const;

	/**
	 * Returns the socket file descriptor from currently used socket
	 * @return file descriptor of socket
	 */

	int getSocketFileDesriptor();

	/**
     * Register an observer that will be notified on blocks that are written or read.
     * @param observer Observer to register
	 */
	void registerObserver(AbstractSocketObserver & observer);

	asio::ip::tcp::socket & getSocket();

private:
	//! Use pimpl idiom.
	std::unique_ptr<Data> impl;

};
}
#endif /* NETWORK_TCPCONNECTION_H*/
