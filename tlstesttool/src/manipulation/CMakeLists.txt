cmake_minimum_required(VERSION 3.4.0)

add_library(TlsTestToolManipulation STATIC
	ForceCertificateUsage.cpp
	ForceCertificateUsage.h
	ManipulateClientHelloCompressionMethods.cpp
	ManipulateClientHelloCompressionMethods.h
	ManipulateClientHelloExtensions.cpp
	ManipulateClientHelloExtensions.h
    ManipulateServerHelloCompressionMethods.cpp
    ManipulateServerHelloCompressionMethods.h
    ManipulateServerHelloExtensions.cpp
    ManipulateServerHelloExtensions.h
    ManipulateEllipticCurveGroup.cpp
    ManipulateEllipticCurveGroup.h
    ManipulateHelloVersion.cpp
	ManipulateHelloVersion.h
	Manipulation.cpp
	Manipulation.h
	ManipulationsParser.cpp
	ManipulationsParser.h
	Renegotiate.cpp
	Renegotiate.h
	SendApplicationData.cpp
	SendApplicationData.h
	SendHeartbeatRequest.cpp
	SendHeartbeatRequest.h
)
target_include_directories(TlsTestToolManipulation PRIVATE
	$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/..>
)
target_include_directories(TlsTestToolManipulation PRIVATE
	$<BUILD_INTERFACE:${EXTERNAL_LIBS_PATH}/include>
)
target_link_libraries(TlsTestToolManipulation PRIVATE
	Tooling
)
