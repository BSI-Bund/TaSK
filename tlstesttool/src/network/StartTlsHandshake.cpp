#include "StartTlsHandshake.h"
#include "logging/LogLevel.h"
#include "logging/Logger.h"
#include <chrono>
#include <iostream>
#include <thread>
#include "network/TcpConnection.h"

namespace TlsTestTool {

    bool StartTlsHandshake::executeStartTlsHandshake(std::shared_ptr<TcpConnection> socket, Tooling::Logger &logger,
                                                     Configuration::StartTLSProtocol startTlsProtocol,
                                                     Configuration &config, bool isClient) {

        //These are the client messages for the different StartTLS protocols
        const std::string SMTP_INIITAL_SERVER_MSG = "220 mail.example.com SMTP service ready\r\n";
        const std::string SMTP_INIITAL_CLIENT_MSG = "EHLO mail.example.com\r\n";
        const std::string SMTP_SECOND_SERVER_MSG = "250-mail.example.com offers a warm hug of welcome\r\n";
        const std::string SMTP_THIRD_SERVER_MSG = "250 STARTTLS\r\n";
        const std::string SMTP_SECOND_CLIENT_MSG = "STARTTLS\r\n";
        const std::string SMTP_FINAL_SERVER_MSG = "220 GO AHEAD\r\n";

        /*STARTTLS for IMAP on server side does not work at the moment!*/
        const std::string IMAP_INITIAL_CLIENT_MSG = "CAPABILITY\r\n";
        const std::string IMAP_INITIAL_SERVER_MSG = "CAPABILITY IMAP4rev1 STARTTLS LOGINDISABLED\r\n";
        const std::string IMAP_SECOND_SERVER_MSG = "a OK CAPABILITY completed\r\n";
        const std::string IMAP_SECOND_CLIENT_MSG = "a STARTTLS\r\n";
        const std::string IMAP_FINAL_SERVER_MSG = "a OK BEGIN TLS NEGOTIATION\r\n";

        const std::string POP3_INITIAL_SERVER_MSG = "+OK Service Ready\r\n";
        const std::string POP3_INITIAL_CLIENT_MSG = "STLS\r\n";
        const std::string POP3_FINAL_SERVER_MSG = "+OK Begin TLS negotiation\r\n";

        const std::string FTP_INITIAL_SERVER_MSG = "211-Extensions supported\r\nAUTH TLS\r\n211 END\r\n";
        const std::string FTP_INITIAL_CLIENT_MSG = "AUTH TLS\r\n";
        const std::string FTP_FINAL_SERVER_MSG = "234 AUTH command ok. Initializing TLS connection->\r\n";

        std::vector<std::vector<std::string>> outputMessages;

        std::vector<std::vector<std::string>> smtpServerVector{
                {SMTP_INIITAL_SERVER_MSG},
                {SMTP_SECOND_SERVER_MSG, SMTP_THIRD_SERVER_MSG},
                {SMTP_FINAL_SERVER_MSG}
        };

        std::vector<std::vector<std::string>> smtpClientVector{
                {SMTP_INIITAL_CLIENT_MSG},
                {SMTP_SECOND_CLIENT_MSG}
        };

        std::vector<std::vector<std::string>> imapServerVector{
                {IMAP_INITIAL_SERVER_MSG},
                {IMAP_SECOND_SERVER_MSG},
                {IMAP_FINAL_SERVER_MSG}
        };

        std::vector<std::vector<std::string>> imapClientVector{
                {IMAP_INITIAL_CLIENT_MSG},
                {IMAP_SECOND_CLIENT_MSG},
        };

        std::vector<std::vector<std::string>> pop3ServerVector{
                {POP3_INITIAL_SERVER_MSG},
                {POP3_FINAL_SERVER_MSG}
        };

        std::vector<std::vector<std::string>> pop3ClientVector{
                {POP3_INITIAL_CLIENT_MSG},
        };

        std::vector<std::vector<std::string>> ftpServerVector{
                {FTP_INITIAL_SERVER_MSG},
                {FTP_FINAL_SERVER_MSG}
        };

        std::vector<std::vector<std::string>> ftpClientVector{
                {FTP_INITIAL_CLIENT_MSG},
        };

        bool sentInitialMessage = false;
        if (startTlsProtocol == Configuration::StartTLSProtocol::SMTP) {
            sentInitialMessage = !isClient;
            if (isClient) {
                outputMessages = smtpClientVector;
            } else {
                outputMessages = smtpServerVector;
            }
        } else if (startTlsProtocol == Configuration::StartTLSProtocol::IMAP) {
            sentInitialMessage = isClient;
            if (isClient) {
                outputMessages = imapClientVector;
            } else {
                outputMessages = imapServerVector;
            }
        } else if (startTlsProtocol == Configuration::StartTLSProtocol::POP3) {
            sentInitialMessage = !isClient;
            if (isClient) {
                outputMessages = pop3ClientVector;
            } else {
                outputMessages = pop3ServerVector;
            }
        } else if (startTlsProtocol == Configuration::StartTLSProtocol::FTP) {
            sentInitialMessage = !isClient;
            if (isClient) {
                outputMessages = ftpClientVector;
            } else {
                outputMessages = ftpServerVector;
            }
        } else if (startTlsProtocol == Configuration::StartTLSProtocol::NONE) {
            logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
                       "StartTLSProtocol::NONE is selected.\r\n A correct StartTLSProtocol must be selected to execute a startTLS handshake");
            return false;
        } else {
            logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
                       "No StartTLSProtocol is selected.\r\n A correct StartTLSProtocol must be selected to execute a startTLS handshake");
            return false;
        }
        logger.log(Tooling::LogLevel::
                   HIGH, "Network", __FILE__, __LINE__, "StartTLS handshake started");

        bool handshakeFinished = false;
        bool sentMessage = sentInitialMessage;
        while (!handshakeFinished) {
            if (!sentMessage) {
                std::string receiveMessage;
                if (!receiveStartTLSHandshakeMessage(socket, logger, config, receiveMessage)) {
                    logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
                               "StartTLS handshake was not executed successfully");
                    return false;
                }
            } else {
                std::vector<std::string> messages = outputMessages[0];
                for (std::string message: messages) {
                    std::vector<char> initialMsg(message.begin(), message.end());
                    socket->write(initialMsg);
                    logStartTlsHandshakeMessage(initialMsg.data(), initialMsg.size(), logger, false);
                }
                outputMessages.erase(outputMessages.begin());
            }
            sentMessage = !sentMessage;

            if (isClient && outputMessages.empty() && sentMessage) {
                handshakeFinished = true;
            }
            if (!isClient && outputMessages.empty()) {
                handshakeFinished = true;
            }
            std::flush(std::cout);
        }

        logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__, "StartTLS handshake finished successfully");

        return true;
    }

    bool
    StartTlsHandshake::receiveStartTLSHandshakeMessage(std::shared_ptr<TcpConnection> socket, Tooling::Logger &logger,
                                                       Configuration &config, std::string &msgResult) {
        int isClient = false;
        if (config.getMode() == Configuration::NetworkMode::CLIENT) {
            isClient = true;
        }
        const size_t MAX_SIZE = 1000; //1Kb should be sufficient
        unsigned char dataVal[MAX_SIZE];
        int result = tcpReceive(socket, dataVal, MAX_SIZE, config.getTcpReceiveTimeoutSeconds(), logger, isClient);
        if (result < 0) {
            logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
                       "StartTLS message could not be received correctly");
            return false;
        }
        std::string msg(dataVal, dataVal + result);
        msgResult.append(msg);
        logStartTlsHandshakeMessage(reinterpret_cast<char *>(dataVal), result, logger, true);
        return true;
    }


    int
    StartTlsHandshake::tcpReceive(std::shared_ptr<TcpConnection> socket, unsigned char *data, size_t size, int timeout,
                                  Tooling::Logger &logger, bool isClient) {

        static const std::chrono::seconds receiveTimeout(timeout);
        const auto timeStart = std::chrono::steady_clock::now();
        while (0 == socket->available()) {
            if (receiveTimeout < (std::chrono::steady_clock::now() - timeStart)) {
                logger.log(Tooling::LogLevel::LOW, "Network", __FILE__, __LINE__, "Timeout Error");
                return TIMEOUT_ERROR;
            }
            /* Perform two checks here to circumvent a timing problem in isClosed where isReadable and nothingToRead are
                     * both true when data is incoming in multiple TCP fragments */
            if (isClient) {
                if (socket->isClosed()) {
                    logger.log(Tooling::LogLevel::LOW, "Network", __FILE__, __LINE__, "SSL_CONN_EOF Error");
                    return SSL_CONN_EOF_ERROR;
                }
            } else {
                if (socket->isClosed()) {
                    logger.log(Tooling::LogLevel::LOW, "Network", __FILE__, __LINE__, "SSL_CONN_EOF Error");
                    return SSL_CONN_EOF_ERROR;
                }
            }
            // Take small breaks to save resources.
            std::this_thread::sleep_for(std::chrono::milliseconds(20));
        }

        int dataSize = std::min(socket->available(), size);
        const auto receivedData = socket->read(dataSize);
        std::copy(receivedData.begin(), receivedData.end(), data);
        return receivedData.size();
    }


    void StartTlsHandshake::logStartTlsHandshakeMessage(char *data, size_t length, Tooling::Logger &logger,
                                                        bool receivedMsg) {
        std::string msg(data, data + length);
        if (receivedMsg) {
            logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__, "StartTLS message received: " + msg);
        } else {
            logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__, "StartTLS message sent: " + msg);
        }
    }
}
