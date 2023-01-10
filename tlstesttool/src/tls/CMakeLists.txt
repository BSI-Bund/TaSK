cmake_minimum_required(VERSION 3.4.0)

add_subdirectory(openssl)
add_subdirectory(mbedtls)

add_library(TlsTestToolTls STATIC
	TlsCipherSuite.h
	TlsContentType.h
	TlsDiffieHellmanGroup.cpp
	TlsDiffieHellmanGroup.h
	TlsEllipticCurveGroup.cpp
	TlsEllipticCurveGroup.h
	TlsHandshakeHeader.h
	TlsHandshakeState.h
	TlsHandshakeType.h
	TlsHashAlgorithm.h
	TlsLogConstants.h
	TlsMessage.h
	TlsNumbers.h
	TlsPlaintextHeader.h
	TlsSession.cpp
	TlsSession.h
	TlsSessionFactory.cpp
	TlsSessionFactory.h
	TlsSignatureAlgorithm.h
	TlsSignatureAndHashAlgorithm.h
	TlsSignatureScheme.h
    TlsSupportedGroup.cpp
	TlsSupportedGroup.h
	TlsVersion.h
        TlsExportSecretsConstant.h)
target_include_directories(TlsTestToolTls PRIVATE
	$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/..>
)

target_include_directories(TlsTestToolTls PRIVATE
	$<BUILD_INTERFACE:${EXTERNAL_LIBS_PATH}/include>
)

target_link_libraries(TlsTestToolTls PRIVATE
	Tooling
	TlsTestToolTlsOpenSsl
	TlsTestToolTlsMbedTls
)
