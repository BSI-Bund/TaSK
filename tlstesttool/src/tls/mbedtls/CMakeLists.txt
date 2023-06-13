cmake_minimum_required(VERSION 3.4.0)

add_library(TlsTestToolTlsMbedTls STATIC
        TlsLogFilter.h
        TlsLogFilter.cpp
        TlsSession.h
        TlsSession.cpp
        )
target_include_directories(TlsTestToolTlsMbedTls PRIVATE
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/../..>
        )
target_link_libraries(TlsTestToolTlsMbedTls PRIVATE
        Tooling
        )

find_package(zlib)
find_package(mbedTLS)
target_link_libraries(TlsTestToolTlsMbedTls PRIVATE
        mbedTLS::mbedTLS
        mbedTLS::X509
        mbedTLS::Crypto
        )
