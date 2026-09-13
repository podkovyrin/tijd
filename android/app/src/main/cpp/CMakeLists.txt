# SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
# SPDX-License-Identifier: MIT

cmake_minimum_required(VERSION 3.25)
project(spoken_time_android LANGUAGES CXX)

set(BUILD_SHARED_LIBS OFF)
add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/../../../../../cpp-spoken-time"
                 "${CMAKE_CURRENT_BINARY_DIR}/spoken-time")
add_library(spoken-time-jni SHARED spoken_time_jni.cpp)
target_link_libraries(spoken-time-jni PRIVATE spoken_time::spoken_time)
