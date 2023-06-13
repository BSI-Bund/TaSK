cmake_minimum_required(VERSION 3.4.0)

add_library(TlsTestToolConfiguration STATIC
        Configuration.h
        ConfigurationLoader.cpp
        ConfigurationLoader.h
        ConfigurationParser.cpp
        ConfigurationParser.h
        )
target_include_directories(TlsTestToolConfiguration PRIVATE
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/..>
        )
target_link_libraries(TlsTestToolConfiguration PRIVATE
        Tooling
        )
