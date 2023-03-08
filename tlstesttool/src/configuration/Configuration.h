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
#ifndef CONFIGURATION_CONFIGURATION_H_
#define CONFIGURATION_CONFIGURATION_H_

#include "logging/LogLevel.h"
#include "manipulation/Manipulation.h"
#include <cstdint>
#include <memory>
#include <string>
#include <utility>
#include <vector>

namespace TlsTestTool {
/**
 * Configuration description container.
 */
class Configuration {
public:
	enum class NetworkMode {
		//! Run the test tool as TCP/IP client.
		CLIENT,
		//! Run the test tool as TCP/IP server.
		SERVER,
		//! Mode has not been set.
		UNKNOWN
	};

    enum class TlsLibrary {
        //! Use mbed TLS as TLS library.
        MBED_TLS,
        //! Use TLS-Attacker as TLS library.
        TLS_ATTACKER,
        //! Use GnuTLS as TLS library.
        GNUTLS,
        //! Use OpenSSL as TLS library.
        OPENSSL,
        //! TLS implementstion has not been set.
        UNKNOWN
    };

    enum class HandshakeType {
        //! normal full handshake
        NORMAL,
        //! session resumption handshake with stored session cache
        SESSION_RESUMPTION_WITH_SESSION_ID,
        SESSION_RESUMPTION_WITH_TICKET,
        //! 0-RTT handshake (works only in TLS 1.3),
        ZERO_RTT
    };

    Configuration()
            : mode(NetworkMode::UNKNOWN),
              tlsLibrary(TlsLibrary::UNKNOWN),
              handshakeType(HandshakeType::NORMAL),
			  host(),
			  port(0),
			  listenTimeoutSeconds(60),
			  waitBeforeCloseSeconds(10),
			  tcpReceiveTimeoutSeconds(120),
			  logLevel(Tooling::LogLevel::OFF),
			  caCertificateFile(),
			  certificateFile(),
			  privateKeyFile(),
			  tlsVersion(std::make_pair(3, 3)), //defaultValue Tls 1.2
              tlsUseSni(false),
              tlsVerifyPeer(false),
              tlsEncryptThenMac(false),
              manipulations(),
              timeoutTlsSessionTicket(0),
              sessionCache(),
              earlyData(),
              sessionLifetime(0),
              preSharedKey(),
              pskIdentity(),
              pskIdentityHint(),
              ocspResponseFile("")
	{
	}

	NetworkMode getMode() const {
		return mode;
	}

	void setMode(const NetworkMode newMode) {
		mode = newMode;
	}

        TlsLibrary getTlsLibrary() const {
            return tlsLibrary;
    }

        void setTlsLibrary(const TlsLibrary newTlsLibrary) {
            tlsLibrary = newTlsLibrary;
        }

        const std::string & getHost() const {
		return host;
	}

	void setHost(const std::string & newHost) {
		host = newHost;
	}

	uint16_t getPort() const {
		return port;
	}

	void setPort(const uint16_t newPort) {
		port = newPort;
	}

	uint32_t getListenTimeoutSeconds() const {
		return listenTimeoutSeconds;
	}

	void setListenTimeoutSeconds(const uint32_t newListenTimeoutSeconds) {
		listenTimeoutSeconds = newListenTimeoutSeconds;
	}

	uint32_t getWaitBeforeCloseSeconds() const {
		return waitBeforeCloseSeconds;
	}

	void setWaitBeforeCloseSeconds(const uint32_t newWaitBeforeCloseSeconds) {
		waitBeforeCloseSeconds = newWaitBeforeCloseSeconds;
	}

	uint32_t getTcpReceiveTimeoutSeconds() const {
		return tcpReceiveTimeoutSeconds;
	}

	void setTcpReceiveTimeoutSeconds(const uint32_t newtcpReceiveTimeoutSeconds) {
		tcpReceiveTimeoutSeconds = newtcpReceiveTimeoutSeconds;
	}

	Tooling::LogLevel getLogLevel() const {
		return logLevel;
	}

	void setLogLevel(const Tooling::LogLevel newLogLevel) {
		logLevel = newLogLevel;
	}

	const std::string & getLogFilterRegEx() const {
		return logFilterRegEx;
	}

	void setLogFilterRegEx(const std::string & newLogFilterRegEx) {
		logFilterRegEx = newLogFilterRegEx;
	}

	const std::string & getCaCertificateFile() const {
		return caCertificateFile;
	}

	void setCaCertificateFile(const std::string & newCaCertificateFile) {
		caCertificateFile = newCaCertificateFile;
	}

	const std::string & getCertificateFile() const {
		return certificateFile;
	}

	void setCertificateFile(const std::string & newCertificateFile) {
		certificateFile = newCertificateFile;
	}

	const std::string & getPrivateKeyFile() const {
		return privateKeyFile;
	}

	void setPrivateKeyFile(const std::string & newPrivateKeyFile) {
		privateKeyFile = newPrivateKeyFile;
	}

	bool hasTlsVersion() const {
		return std::make_pair(static_cast<uint8_t>(0), static_cast<uint8_t>(0)) != tlsVersion;
	}

	const std::pair<uint8_t, uint8_t> & getTlsVersion() const {
		return tlsVersion;
	}

	void setTlsVersion(const std::pair<uint8_t, uint8_t> & newTlsVersion) {
		tlsVersion = newTlsVersion;
	}

    bool getTlsUseSni() const {
        return tlsUseSni;
    }

    void setTlsUseSni(bool useSni) {
        tlsUseSni = useSni;
    }

    bool getTlsVerifyPeer() const {
        return tlsVerifyPeer;
    }

    void setTlsVerifyPeer(bool verifyPeer) {
		tlsVerifyPeer = verifyPeer;
	}

	bool getTlsEncryptThenMac() const {
		return tlsEncryptThenMac;
	}

	void setTlsEncryptThenMac(bool enableEncryptThenMac) {
		tlsEncryptThenMac = enableEncryptThenMac;
	}

	bool hasTlsCipherSuites() const {
		return !tlsCipherSuites.empty();
	}

	const std::vector<std::pair<uint8_t, uint8_t>> & getTlsCipherSuites() const {
		return tlsCipherSuites;
	}

	void clearTlsCipherSuites() {
		tlsCipherSuites.clear();
	}

	void addTlsCipherSuite(std::pair<uint8_t, uint8_t> && tlsCipherSuite) {
		tlsCipherSuites.emplace_back(std::move(tlsCipherSuite));
	}

	const std::string & getTlsServerDHParams() const {
		return tlsServerDHParams;
	}

	void setTlsServerDHParams(const std::string & newTlsServerDHParams) {
		tlsServerDHParams = newTlsServerDHParams;
	}

	const std::vector<std::unique_ptr<Manipulation>> & getManipulations() const {
		return manipulations;
	}

	void addManipulation(std::unique_ptr<Manipulation> && manipulation) {
		manipulations.emplace_back(std::move(manipulation));
	}

	const std::string & getTlsSecretFile() const {
		return tlsSecretFile;
	}

	void setTlsSecretFile(const std::string & newTlsSecretFile) {
		tlsSecretFile = newTlsSecretFile;
	}

	bool hasTlsSupportedGroups() const {
		return !tlsSupportedGroups.empty();
	}

	const std::vector<std::string> & getTlsSupportedGroups() const {
		return tlsSupportedGroups;
	}

	void clearTlsSupportedGroups() {
		tlsSupportedGroups.clear();
	}

	void addTlsSupportedGroup(std::string tlsSupportedGroup) {
		tlsSupportedGroups.emplace_back(tlsSupportedGroup);
	}

	bool hasTlsSignatureSchemes() const {
		return !tlsSignatureSchemes.empty();
	}

	const std::vector<std::pair<uint8_t, uint8_t>> & getTlsSignatureSchemes() const {
		return tlsSignatureSchemes;
	}

	void clearTlsSignatureSchemes() {
		tlsSignatureSchemes.clear();
	}

	void addTlsSignatureScheme(std::pair<uint8_t, uint8_t> && tlsSignatureScheme) {
		tlsSignatureSchemes.emplace_back(std::move(tlsSignatureScheme));
	}

    int getTimeoutTlsSessionTicket() const {
        return timeoutTlsSessionTicket;
    }

    const std::string &getSessionCache() const {
        return sessionCache;
    }

    const std::vector<uint8_t> &getEarlyData() const {
        return earlyData;
    }

    void setSessionCache(const std::string &sessionCache) {
        Configuration::sessionCache = sessionCache;
    }

    void setEarlyData(const std::vector<uint8_t> &earlyData) {
        Configuration::earlyData = earlyData;
    }

    void clearSessionCache(){
        sessionCache.clear();
    }

    void getEarlyData(){
        earlyData.clear();
    }

    HandshakeType getHandshakeType() const {
        return handshakeType;
    }

    void setHandshakeType(HandshakeType handshakeType) {
        Configuration::handshakeType = handshakeType;
    }
    
    bool hasTlsSignatureAlgorithms() const {
		return !tlsSignatureAlgorithms.empty();
	}

	const std::vector<std::pair<uint8_t, uint8_t>> & getTlsSignatureAlgorithms() const {
		return tlsSignatureAlgorithms;
	}

	void clearTlsSignatureAlgorithms() {
		tlsSignatureAlgorithms.clear();
	}

	void addTlsSignatureAlgorithm(std::pair<uint8_t, uint8_t> && tlsSignatureAlgorithm) {
		tlsSignatureAlgorithms.emplace_back(std::move(tlsSignatureAlgorithm));
	}


    const std::vector<uint8_t> &getClientHelloExtension() const {
        return clientHelloExtension;
    }

    void setClientHelloExtension(const std::vector<uint8_t> &clientHelloExtension) {
        Configuration::clientHelloExtension = clientHelloExtension;
    }

    int getSessionLifetime() const {
            return sessionLifetime;
    }

    void setSessionLifetime(int sessionLifetime) {
            Configuration::sessionLifetime = sessionLifetime;
    }

    const std::vector<uint8_t> &getPreSharedKey() const {
        return preSharedKey;
    }

    void setPreSharedKey(const std::vector<uint8_t> &preSharedKey) {
        Configuration::preSharedKey = preSharedKey;
    }

    const std::string &getPskIdentity() const {
        return pskIdentity;
    }

    void setPskIdentity(const std::string &pskIdentity) {
        Configuration::pskIdentity = pskIdentity;
    }

    const std::string &getPskIdentityHint() const {
        return pskIdentityHint;
    }

    void setPskIdentityHint(const std::string &pskIdentityHint) {
        Configuration::pskIdentityHint = pskIdentityHint;
    }
    const std::string &getOcspResponseFile() const {
        return ocspResponseFile;
    }

    void setOcspResponseFile(const std::string &ocspResponseFile) {
        Configuration::ocspResponseFile = ocspResponseFile;
    }

private:
	NetworkMode mode;
    TlsLibrary tlsLibrary;
    HandshakeType handshakeType;
	std::string host;
	uint16_t port;
	uint32_t listenTimeoutSeconds;
	uint32_t waitBeforeCloseSeconds;
	uint32_t tcpReceiveTimeoutSeconds;
	Tooling::LogLevel logLevel;
	std::string logFilterRegEx;
	std::string caCertificateFile;
	std::string certificateFile;
	std::string privateKeyFile;
	std::pair<uint8_t, uint8_t> tlsVersion;
    bool tlsUseSni;
    bool tlsVerifyPeer;
	bool tlsEncryptThenMac;
	std::vector<std::pair<uint8_t, uint8_t>> tlsCipherSuites;
	std::string tlsServerDHParams;
	std::string tlsSecretFile;
	std::vector<std::pair<uint8_t, uint8_t>> tlsSignatureSchemes;
    std::vector<std::pair<uint8_t, uint8_t>> tlsSignatureAlgorithms;
	std::vector<std::string> tlsSupportedGroups;
    std::vector<std::unique_ptr<Manipulation>> manipulations;
    //variables for session resumption
    int timeoutTlsSessionTicket;
    std::string sessionCache;
    std::vector<uint8_t> earlyData;
    std::vector<uint8_t> clientHelloExtension; //only for OpenSSL
    int sessionLifetime;
    std::vector<uint8_t> preSharedKey;
    std::string pskIdentity;
    std::string pskIdentityHint;
    std::string ocspResponseFile;

};
}

#endif /* CONFIGURATION_CONFIGURATION_H_ */
