
cmake_minimum_required(VERSION 3.4.0)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

project(Tooling VERSION 1.3.99 LANGUAGES CXX)

if (CMAKE_CXX_COMPILER_ID STREQUAL "GNU" OR CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
    set(FLAGS_WARN "-pedantic-errors -Werror -Wall -Wextra -Wold-style-cast")

    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${FLAGS_WARN}")
endif ()
if (MINGW AND CMAKE_CXX_COMPILER_ID STREQUAL "GNU")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -static-libgcc -static-libstdc++")
endif ()

add_subdirectory(src)

