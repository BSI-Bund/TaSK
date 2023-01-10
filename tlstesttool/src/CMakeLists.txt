cmake_minimum_required(VERSION 3.4.0)

add_subdirectory(configuration)
add_subdirectory(manipulation)
add_subdirectory(network)
add_subdirectory(tls)

add_library(TlsTestToolLib INTERFACE)
target_include_directories(TlsTestToolLib INTERFACE
	$<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}>
)

target_link_libraries(TlsTestToolLib INTERFACE TlsTestToolConfiguration)
target_link_libraries(TlsTestToolLib INTERFACE TlsTestToolManipulation)
target_link_libraries(TlsTestToolLib INTERFACE TlsTestToolNetwork)
target_link_libraries(TlsTestToolLib INTERFACE TlsTestToolTls)
target_link_libraries(TlsTestToolLib INTERFACE Tooling)


add_executable(TlsTestTool
	TlsTestTool.cpp
)


target_compile_definitions(TlsTestTool PRIVATE TLS_TEST_TOOL_VERSION="${PROJECT_VERSION}")
target_link_libraries(TlsTestTool TlsTestToolLib)

find_package(Asio)
target_link_libraries(TlsTestTool
		Asio::Asio
		)
target_include_directories(TlsTestTool PRIVATE
	$<BUILD_INTERFACE:${EXTERNAL_LIBS_PATH}/include>
)

install(TARGETS TlsTestTool
    RUNTIME DESTINATION .
)
