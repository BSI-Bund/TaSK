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
#include "configuration/Configuration.h"
#include "configuration/ConfigurationLoader.h"
#include "logging/LogLevel.h"
#include "logging/Logger.h"
#include "network/TcpClient.h"
#include "network/TcpServer.h"
#include "network/TimestampObserver.h"
#include "strings/HexStringHelper.h"
#include "strings/StringHelper.h"
#include "tls/TlsDiffieHellmanGroup.h"
#include "tls/TlsSession.h"
#include "tls/TlsSessionFactory.h"
#include "tls/TlsSupportedGroup.h"
#include "asio.hpp"
#include <algorithm>
#include <chrono>
#include <cstdlib>
#include <exception>
#include <fstream>
#include <iostream>
#include <regex>
#include <string>
#include <thread>
#include "tls/openssl/TlsSession.h"




#ifndef TLS_TEST_TOOL_VERSION
#define TLS_TEST_TOOL_VERSION "UNRELEASED"
#endif /* TLS_TEST_TOOL_VERSION */

static void logException(Tooling::Logger & logger, const std::string & category, const std::string & file,
						 const int line, const std::string & message, const std::exception & e) {
	logger.log(Tooling::LogLevel::HIGH, category, file, line,
			   message + ": " + Tooling::StringHelper::removeNewlines(e.what()));
}

static bool configureTlsSession(const TlsTestTool::Configuration & configuration, TlsTestTool::TlsSession & tlsSession,
								Tooling::Logger & logger) {
	if (configuration.hasTlsVersion()) {
		tlsSession.setVersion(configuration.getTlsVersion());
	}

        tlsSession.setUseSni(configuration.getTlsUseSni(), configuration.getHost());
        tlsSession.setVerifyPeer(configuration.getTlsVerifyPeer());
	tlsSession.setExtensionEncryptThenMac(configuration.getTlsEncryptThenMac());
        tlsSession.setPreSharedKey(configuration.getPreSharedKey(), configuration.getPskIdentityHint());
	if(configuration.getTlsLibrary() ==TlsTestTool::Configuration::TlsLibrary::OPENSSL) {
                tlsSession.setOcspResponderFile(configuration.getOcspResponseFile());
		tlsSession.setClientHelloExtensions(configuration.getClientHelloExtension());
	}
	if (configuration.hasTlsCipherSuites()) {
		tlsSession.setCipherSuites(configuration.getTlsCipherSuites());
	}
	if (!configuration.getTlsServerDHParams().empty()) {
		try {
			tlsSession.setServerDHParams(
					TlsTestTool::TlsDiffieHellmanGroup::getPredefined(configuration.getTlsServerDHParams()));
		} catch (const std::exception & e) {
			logException(logger, "TLS", __FILE__, __LINE__, "Configuring ServerDHParams failed", e);
			return false;
		}
	}
	if (!configuration.getTlsSecretFile().empty()) {
		try {
			tlsSession.setSecretOutput(std::make_unique<std::ofstream>(configuration.getTlsSecretFile(), std::ios::out | std::ios::app));
		} catch (const std::exception & e) {
			logException(logger, "TLS", __FILE__, __LINE__, "Configuring TLS secret file failed", e);
			return false;
		}
	}
	if (configuration.hasTlsSupportedGroups()) {
		try {
			std::vector<TlsTestTool::TlsSupportedGroupID> supportedGroupIDs;
			for(auto const& supportedGroup: configuration.getTlsSupportedGroups()) {
				supportedGroupIDs.push_back(TlsTestTool::TlsSupportedGroup::getPredefined(supportedGroup));
			}
			tlsSession.setSupportedGroups(supportedGroupIDs);
		} catch (const std::exception & e) {
			logException(logger, "TLS", __FILE__, __LINE__, "Configuring TLS SupportedGroups failed", e);
			return false;
		}
	}
	if (configuration.hasTlsSignatureSchemes()) {
		tlsSession.setSignatureSchemes(configuration.getTlsSignatureSchemes());
	}
	if (configuration.hasTlsSignatureAlgorithms()) {
		tlsSession.setSignatureAlgorithms(configuration.getTlsSignatureAlgorithms());
	}
	tlsSession.setWaitForAlertSeconds(configuration.getWaitBeforeCloseSeconds());
    
    if(configuration.getTlsLibrary() != TlsTestTool::Configuration::TlsLibrary::OPENSSL){
		tlsSession.setTcpReceiveTimeoutSeconds(configuration.getTcpReceiveTimeoutSeconds());
    }

    //set session cache
    if(configuration.getTlsLibrary() == TlsTestTool::Configuration::TlsLibrary::OPENSSL){
        tlsSession.setHandshakeType(configuration.getHandshakeType());
        if(configuration.getHandshakeType() == TlsTestTool::Configuration::HandshakeType::SESSION_RESUMPTION_WITH_TICKET ||
                configuration.getHandshakeType() == TlsTestTool::Configuration::HandshakeType::SESSION_RESUMPTION_WITH_SESSION_ID ||
                configuration.getHandshakeType() == TlsTestTool::Configuration::HandshakeType::ZERO_RTT){
            tlsSession.setSessionCache(configuration.getSessionCache());
        }
        if(configuration.getHandshakeType() == TlsTestTool::Configuration::HandshakeType::ZERO_RTT){
            tlsSession.setEarlyData(configuration.getEarlyData());
        }
    }

    return true;
}

static bool configureCertificates(const TlsTestTool::Configuration & configuration,
								  TlsTestTool::TlsSession & tlsSession, Tooling::Logger & logger) {
	if (!configuration.getCaCertificateFile().empty()) {
		try {
			std::ifstream caCertificateFile(configuration.getCaCertificateFile());
			tlsSession.setCaCertificate(caCertificateFile);
		} catch (const std::exception & e) {
			logException(logger, "TLS", __FILE__, __LINE__, "Loading CA certificate file failed", e);
			return false;
		}
	}
	if (!configuration.getCertificateFile().empty() && !configuration.getPrivateKeyFile().empty()) {
		try {
			std::ifstream certificateFile(configuration.getCertificateFile());
			std::ifstream privateKeyFile(configuration.getPrivateKeyFile());
			tlsSession.setCertificate(certificateFile, privateKeyFile);
		} catch (const std::exception & e) {
			logException(logger, "TLS", __FILE__, __LINE__, "Loading certificate and private key files failed", e);
			return false;
		}
	}
	return true;
}

static bool checkTcpConnection(std::shared_ptr<TlsTestTool::TcpConnection> connection,
							   Tooling::Logger & logger) {
	if (connection->isClosed()) {
		logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__, "TCP/IP connection is closed.");
		return false;
	} else {
		return true;
	}
}

static void waitForClosedTcpConnection(const TlsTestTool::Configuration & configuration,
									   std::shared_ptr<TlsTestTool::TcpConnection> connection, Tooling::Logger & logger) {
	static const std::chrono::seconds timeout(configuration.getWaitBeforeCloseSeconds());
	logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
			   "Wait at most " + std::to_string(timeout.count()) + " s for closing of the TCP/IP connection.");
	const auto timeStart = std::chrono::steady_clock::now();
	while (checkTcpConnection(connection, logger)) {
		if (timeout < (std::chrono::steady_clock::now() - timeStart)) {
			logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__, "TCP/IP connection is still open.");
			break;
		}
	}
}

static void configureCallbacks(const TlsTestTool::Configuration & configuration, TlsTestTool::TlsSession & tlsSession, Tooling::Logger & logger) {
	tlsSession.registerPreStepCallback([&](TlsTestTool::TlsSession & session) {
		for (auto & manipulation : configuration.getManipulations()) {
			manipulation->executePreStep(session);
		}
		// It is necessary to check the connection so that the handlers that are ready to run are executed
		// in the isClosed function with poll() or poll_one().
		checkTcpConnection(session.getSocket(), logger);
	});
	tlsSession.registerPostStepCallback([&](TlsTestTool::TlsSession & session) {
		for (auto & manipulation : configuration.getManipulations()) {
			manipulation->executePostStep(session);
		}
		// It is necessary to check the connection so that the handlers that are ready to run are executed
		// in the isClosed function with poll() or poll_one().
		checkTcpConnection(session.getSocket(), logger);
	});
}

static bool prepareTlsSession(const TlsTestTool::Configuration & configuration, TlsTestTool::TlsSession & tlsSession, Tooling::Logger & logger) {
	tlsSession.setLogger(logger);
	if (!configureTlsSession(configuration, tlsSession, logger)) {
		return false;
	}
	if(configuration.getTlsLibrary() != TlsTestTool::Configuration::TlsLibrary::OPENSSL) {
		configureCallbacks(configuration, tlsSession, logger);
	}
	if (!configureCertificates(configuration, tlsSession, logger)) {
		return false;
	}
	if(configuration.getTlsLibrary() == TlsTestTool::Configuration::TlsLibrary::OPENSSL){
		dynamic_cast<TlsTestTool::OpenSsl::TlsSession&>(tlsSession).configureOpensslContext();
	}
	return true;
}

static void executeTlsSession(const TlsTestTool::Configuration & configuration, TlsTestTool::TlsSession & tlsSession,
							  std::shared_ptr<TlsTestTool::TcpConnection> connection, Tooling::Logger & logger) {
	TlsTestTool::TimestampObserver timestampObserver(connection, logger);
	connection->registerObserver(timestampObserver);
	try {
		for (auto & manipulation : configuration.getManipulations()) {
			manipulation->executePreHandshake(tlsSession);
		}
		tlsSession.performHandshake();
		for (auto & manipulation : configuration.getManipulations()) {
			manipulation->executePostHandshake(tlsSession);
		}
	} catch (const std::exception & e) {
		logException(logger, "TLS", __FILE__, __LINE__, "TLS handshake failed", e);
		waitForClosedTcpConnection(configuration, connection, logger);
		return;
	}
	if (!checkTcpConnection(connection, logger)) {
		return;
	}
	try {
		static const std::chrono::seconds timeout(1);
		const auto timeStart = std::chrono::steady_clock::now();
		while (0 == connection->available()) {
			if (timeout < (std::chrono::steady_clock::now() - timeStart)) {
				break;
			}
		}
		if (0 < connection->available()) {
			const auto data = tlsSession.receiveApplicationData();
			logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
					   "Application data received: " + Tooling::HexStringHelper::byteArrayToHexString(data));
		}
	} catch (const std::exception & e) {
		logException(logger, "TLS", __FILE__, __LINE__, "Receiving application data failed", e);
		waitForClosedTcpConnection(configuration, connection, logger);
		return;
	}
	try {
		/*if a session lifetime is configured, we keep the connection alive until the lifetime is expired or the server  closes the connection*/
		const std::chrono::seconds sessionLifetime(configuration.getSessionLifetime());
		if (configuration.getSessionLifetime() > 0) {
			const auto timeStart = std::chrono::steady_clock::now();
			while (sessionLifetime > (std::chrono::steady_clock::now() - timeStart)) {
				/*check if DUT closed the connection*/
				if (connection->isClosed()) {
					logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
							   "The DUT closed the connection before the session lifetime expired");
					return;
				}
				if (0 < connection->available()) {
					std::flush(std::cout);
					try {
						const auto data = tlsSession.receiveApplicationData();
						logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
								   "Application data received: " + Tooling::HexStringHelper::byteArrayToHexString(data));
					}catch(const std::runtime_error & e){
						/*if connection was closed while receiving application data, then continue other throw exception*/
						if(std::string("connection was closed gracefully.") != e.what()){
							throw e;
						}
					}
				}
				std::this_thread::sleep_for(std::chrono::milliseconds(100));
			}
			if (!connection->isClosed()) {
				logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
						   "The DUT did not close the connection before the session lifetime expired"); //TOOD replace before with after
			}
		}
		tlsSession.close();
	}
	catch (const std::exception & e) {
		logException(logger, "TLS", __FILE__, __LINE__, "Closing failed", e);
		waitForClosedTcpConnection(configuration, connection, logger);
		return;
	}
	try {
		waitForClosedTcpConnection(configuration, connection, logger);
		connection->close();
	} catch (const std::exception & e) {
		logException(logger, "Network", __FILE__, __LINE__, "Closing failed", e);
		return;
	}
}

static void setUpAndExecuteTlsSession(std::shared_ptr<TlsTestTool::TlsSession> tlsSession,TlsTestTool::Configuration &configuration,
									  std::shared_ptr<TlsTestTool::TcpConnection> tcpConnection,Tooling::Logger &logger){
	tlsSession->setupSession();
	executeTlsSession(configuration, *tlsSession, tcpConnection, logger);
	tlsSession->cleanSession();
}

static void startServerAcceptHandler(std::shared_ptr<TlsTestTool::TlsSession> tlsSession,TlsTestTool::Configuration &configuration,
									 TlsTestTool::TcpServer &server,Tooling::Logger &logger, bool sessionResumption,bool& serverHandledConnections){
	server.getAcceptor().async_accept((server.getActiveTcpConnection()->getSocket()),
									  [tlsSession, &configuration, &server, &logger, sessionResumption, &serverHandledConnections](std::error_code /*error*/) {
		logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
				   "TCP/IP connection from " + server.getActiveTcpConnection()->getRemoteIpAddress() + ':'
						   + std::to_string(server.getActiveTcpConnection()->getRemoteTcpPort()) + " received.");
		setUpAndExecuteTlsSession(tlsSession, configuration, server.getActiveTcpConnection(), logger);
		std::cout.flush();
		if(sessionResumption) {
			auto secondConnection = server.addNewTcpConnection();
			logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
					   "Initial handshake finished. Wait for resumption handshake.");
			std::cout.flush();
			startServerAcceptHandler(tlsSession, configuration, server, logger, false, serverHandledConnections);
		}else{
			serverHandledConnections=true;
			logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
					   "Server handled all connections");
			std::cout.flush();
		}
	});
}

int main(int argc, char ** argv) {
	// Make sure not to flush stdout on '\n' for increased performance.
	std::cout.sync_with_stdio(false);
	Tooling::Logger logger(std::cout);
	logger.setColumnSeparator("\t");
	logger.setLogLevel(Tooling::LogLevel::HIGH);
	TlsTestTool::Configuration configuration;
	try {
		configuration = TlsTestTool::ConfigurationLoader::parse(argc, const_cast<const char **>(argv));
	} catch (const std::exception & e) {
		logException(logger, "Tool", __FILE__, __LINE__, "Parsing the configuration failed", e);
		return EXIT_FAILURE;
	}
	logger.log(Tooling::LogLevel::HIGH, "Tool", __FILE__, __LINE__, "TLS Test Tool version " TLS_TEST_TOOL_VERSION);

	logger.setLogLevel(configuration.getLogLevel());
	logger.setTlsVersion(configuration.getTlsVersion());
	for (auto & manipulation : configuration.getManipulations()) {
		manipulation->setLogger(logger);
	}
	if (!configuration.getLogFilterRegEx().empty()) {
		try {
			const std::regex filterRegEx{configuration.getLogFilterRegEx()};
			logger.addLogFilter([filterRegEx](Tooling::Logger & logger, const Tooling::LogLevel, const std::string &,
											  const std::string & message) {
				std::smatch match;
				if (std::regex_match(message, match, filterRegEx)) {
					logger.log(Tooling::LogLevel::HIGH, "Tool", __FILE__, __LINE__, "Matched message: " + message);
				}
			});
		} catch (const std::exception & e) {
			logException(logger, "Tool", __FILE__, __LINE__, "Configuring the log filter regular expression failed", e);
			return EXIT_FAILURE;
		}
	}

	TlsTestTool::Configuration::TlsLibrary tlsLibrary = configuration.getTlsLibrary();
	if (tlsLibrary == TlsTestTool::Configuration::TlsLibrary::UNKNOWN) {
		logger.log(Tooling::LogLevel::HIGH, "Tool", __FILE__, __LINE__,
				   "tlsLibrary not set, using MBED_TLS as default.");
		tlsLibrary = TlsTestTool::Configuration::TlsLibrary::MBED_TLS;
	}

	if (TlsTestTool::Configuration::NetworkMode::CLIENT == configuration.getMode()) {
		TlsTestTool::TcpClient client;
		try {
			client.connect(configuration.getHost(), std::to_string(configuration.getPort()));
			logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
					   "TCP/IP connection to " + client.getTcpConnection()->getRemoteIpAddress() + ':'
							   + std::to_string(client.getTcpConnection()->getRemoteTcpPort()) + " established.");
		} catch (const std::exception & e) {
			logException(logger, "Network", __FILE__, __LINE__,
						 "TCP/IP connection to " + configuration.getHost() + ':'
								 + std::to_string(configuration.getPort()) + " failed",
						 e);
			return EXIT_FAILURE;
		}

		auto tlsSession = TlsTestTool::TlsSessionFactory::createClientSession(tlsLibrary, client);
		if (!prepareTlsSession(configuration, *tlsSession, logger)) {
			return EXIT_FAILURE;
		}
		setUpAndExecuteTlsSession(tlsSession, configuration, client.getTcpConnection(), logger);

	} else if (TlsTestTool::Configuration::NetworkMode::SERVER == configuration.getMode()) {
		TlsTestTool::TcpServer server;
		try {
			server.listen(configuration.getPort());
			logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
					   "Waiting for TCP/IP connection on port " + std::to_string(configuration.getPort()) + '.');
		} catch (const std::exception & e) {
			logException(logger, "Network", __FILE__, __LINE__,
						 "Listening on port " + std::to_string(configuration.getPort()) + " failed", e);
			return EXIT_FAILURE;
		}
		auto tlsSession = TlsTestTool::TlsSessionFactory::createServerSession(tlsLibrary, server);
		if (!prepareTlsSession(configuration, *tlsSession, logger)) {
			return EXIT_FAILURE;
		}
		const std::chrono::seconds timeout(configuration.getListenTimeoutSeconds());
		const auto timeStart = std::chrono::steady_clock::now();
		// Flush output once before entering main loop.
		std::cout.flush();
		bool serverHandledConnections = false;
		bool sessionResumption = configuration.getHandshakeType() == TlsTestTool::Configuration::HandshakeType::SESSION_RESUMPTION_WITH_TICKET ||
				configuration.getHandshakeType() == TlsTestTool::Configuration::HandshakeType::SESSION_RESUMPTION_WITH_SESSION_ID;
		if(sessionResumption && configuration.getTlsLibrary()==TlsTestTool::Configuration::TlsLibrary::MBED_TLS){
			logger.log(Tooling::LogLevel::HIGH, "TLS", __FILE__, __LINE__,
						 "Session Resumption in server mode is not supported in Mbed TLS");
			return EXIT_FAILURE;
		}
		startServerAcceptHandler(tlsSession, configuration, server, logger, sessionResumption, serverHandledConnections);
		/*while loop to keep server alive and waiting for new client connections*/
		while (true) {
			std::this_thread::sleep_for(std::chrono::milliseconds(100));
			server.work();
			if ((0 != timeout.count()) && (timeout < (std::chrono::steady_clock::now() - timeStart))) {
				logger.log(Tooling::LogLevel::HIGH, "Network", __FILE__, __LINE__,
						   "Listen timeout after " + std::to_string(timeout.count()) + " s.");
				break;
			}
			if(serverHandledConnections){
				std::this_thread::sleep_for(std::chrono::milliseconds(100));
				break;
			}
		}
	}
	logger.log(Tooling::LogLevel::HIGH, "Tool", __FILE__, __LINE__, "TLS Test Tool exiting.");
	return EXIT_SUCCESS;
}

