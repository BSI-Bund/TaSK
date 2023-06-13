#ifndef TLSTESTTOOL_STARTTLSHANDSHAKE_H
#define TLSTESTTOOL_STARTTLSHANDSHAKE_H

#include "TcpClient.h"
#include "configuration/Configuration.h"

namespace Tooling {
    class Logger;
}
namespace TlsTestTool {

    class StartTlsHandshake {
    public:

/*error codes for TCP receive function*/
#define TIMEOUT_ERROR -2
#define SSL_CONN_EOF_ERROR -3

        /**
         * function executes a startTLS pre-handshake. Currently only works in client mode!
         * @param socket socket
         * @param logger Logger
         * @param startTlsProtocol the selected startTLS protocol
         * @param config configuration
         * @return true, if startTLS handshake was executed successfully
         */
        static bool executeStartTlsHandshake(std::shared_ptr<TcpConnection> socket, Tooling::Logger &logger,
                                             Configuration::StartTLSProtocol startTlsProtocol, Configuration &config,
                                             bool isClient);

    private:

        /**
         * Function to receive a complete StartTLS pre-handshake message
         * @param connection TcpConnection/socket
         * @param data sent data
         * @param size data size
         * @param timeout timeout
         * @return if msg was received successfully
         */
        static bool receiveStartTLSHandshakeMessage(std::shared_ptr<TcpConnection> socket, Tooling::Logger &logger,
                                                    Configuration &config, std::string &msgResult);

        /**
         * Function to perform blocking tcpReceive until peer sends data (with connection timeout)
         * @param connection TcpConnection/socket
         * @param data sent data
         * @param size
         * @param timeout
         * @return
         */
        static int tcpReceive(std::shared_ptr<TcpConnection> socket, unsigned char *data, size_t size, int timeout,
                              Tooling::Logger &logger, bool isClient);


        /**
         * Logs a startTLS pre-handshake message (either sent or received messages): messages have ASCII format in startTLS
         * @param data message data
         * @param length length of message data
         * @param logger Logger
         * @param receivedMessage true, if it is a receiving message. False, if it is sending message.
         */
        static void
        logStartTlsHandshakeMessage(char *data, size_t length, Tooling::Logger &logger, bool receivedMessage);
    };

}
#endif // TLSTESTTOOL_STARTTLSHANDSHAKE_H
