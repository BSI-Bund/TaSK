cmake_minimum_required(VERSION 3.4.0)

add_library(TlsTestToolTlsOpenSsl STATIC
        TlsHelper.h
        TlsHelper.cpp
        TlsLogger.h
        TlsLogger.cpp
        TlsMessageLogger.h
        TlsMessageLogger.cpp
        TlsSession.h
        TlsSession.cpp
        )
target_include_directories(TlsTestToolTlsOpenSsl PRIVATE
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/../..>
        )
target_link_libraries(TlsTestToolTlsOpenSsl PRIVATE
        Tooling
        )

set_property(GLOBAL PROPERTY FIND_LIBRARY_USE_LIB64_PATHS TRUE)
find_package(OpenSSL REQUIRED)
target_link_libraries(TlsTestToolTlsOpenSsl PRIVATE
        OpenSSL::Crypto
        OpenSSL::SSL
        )
