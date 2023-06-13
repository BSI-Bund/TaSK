cmake_minimum_required(VERSION 3.4.0)

add_library(TlsTestToolNetwork STATIC
        AbstractSocketObserver.h
        TcpClient.cpp
        TcpClient.h
        TcpServer.cpp
        TcpServer.h
        TimestampObserver.cpp
        TimestampObserver.h
        WaitFor.cpp
        WaitFor.h
        TcpConnection.cpp
        TcpConnection.h
        StartTlsHandshake.cpp
        StartTlsHandshake.h)
target_include_directories(TlsTestToolNetwork PRIVATE
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/..>
        )

find_package(Asio)
target_link_libraries(TlsTestToolNetwork PRIVATE
        Asio::Asio
        )
target_link_libraries(TlsTestToolNetwork PRIVATE
        Tooling
        )

include(CheckIncludeFileCXX)
CHECK_INCLUDE_FILE_CXX("linux/net_tstamp.h" HAVE_LINUX_NET_TSTAMP_H)
CHECK_INCLUDE_FILE_CXX("sys/socket.h" HAVE_SYS_SOCKET_H)
CHECK_INCLUDE_FILE_CXX("sys/types.h" HAVE_SYS_TYPES_H)

if (HAVE_LINUX_NET_TSTAMP_H AND
        HAVE_SYS_SOCKET_H AND
        HAVE_SYS_TYPES_H)
    target_compile_definitions(TlsTestToolNetwork PRIVATE USE_SO_TIMESTAMPING)
endif ()
