# SPDX-FileCopyrightText: 2026 Andrei Podkovyrin
# SPDX-License-Identifier: MIT

cmake_minimum_required(VERSION 3.25)

project(
  spoken_time
  VERSION 0.1.0
  DESCRIPTION "Cross-platform spoken-time formatting library"
  LANGUAGES CXX)

include(CMakePackageConfigHelpers)
include(GenerateExportHeader)
include(GNUInstallDirs)

set(SPOKEN_TIME_EXPORT_NOTICE
    "/* Copyright (c) 2026 Andrei Podkovyrin. SPDX-License-Identifier: MIT */")

option(SPOKEN_TIME_WARNINGS_AS_ERRORS "Treat compiler warnings as errors"
       ${PROJECT_IS_TOP_LEVEL})

add_library(spoken_time
  src/additional_languages.cpp
  src/african.cpp
  src/arabic_dialects.cpp
  src/celtic.cpp
  src/east_asian.cpp
  src/east_asian_extended.cpp
  src/germanic.cpp
  src/hebrew.cpp
  src/kurdish.cpp
  src/other_languages.cpp
  src/romance.cpp
  src/slavic.cpp
  src/south_asian.cpp
  src/south_asian_extended.cpp
  src/southeast_asian.cpp
  src/southeast_asian_extended.cpp
  src/spoken_time.cpp
  src/turkic.cpp)
add_library(spoken_time::spoken_time ALIAS spoken_time)

generate_export_header(
  spoken_time
  BASE_NAME spoken_time
  EXPORT_FILE_NAME "${CMAKE_CURRENT_BINARY_DIR}/generated/spoken_time/export.hpp"
  CUSTOM_CONTENT_FROM_VARIABLE SPOKEN_TIME_EXPORT_NOTICE)

set_target_properties(
  spoken_time
  PROPERTIES CXX_EXTENSIONS OFF
             CXX_VISIBILITY_PRESET hidden
             EXPORT_NAME spoken_time
             OUTPUT_NAME spoken-time
             POSITION_INDEPENDENT_CODE ON
             SOVERSION ${PROJECT_VERSION_MAJOR}
             VERSION ${PROJECT_VERSION}
             VISIBILITY_INLINES_HIDDEN ON)

target_compile_features(spoken_time PUBLIC cxx_std_20)
target_include_directories(
  spoken_time
  PUBLIC $<BUILD_INTERFACE:${CMAKE_CURRENT_BINARY_DIR}/generated>
         $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
         $<INSTALL_INTERFACE:${CMAKE_INSTALL_INCLUDEDIR}>)

if(MSVC)
  target_compile_options(spoken_time PRIVATE /permissive- /W4 /utf-8)
else()
  target_compile_options(
    spoken_time PRIVATE -Wall -Wconversion -Wextra -Wpedantic -Wshadow
                        -Wsign-conversion)
endif()

set_property(TARGET spoken_time PROPERTY COMPILE_WARNING_AS_ERROR
                                         ${SPOKEN_TIME_WARNINGS_AS_ERRORS})

install(
  TARGETS spoken_time
  EXPORT spoken_time_targets
  ARCHIVE DESTINATION ${CMAKE_INSTALL_LIBDIR}
  LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
  RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
  INCLUDES
  DESTINATION ${CMAKE_INSTALL_INCLUDEDIR})

install(
  DIRECTORY include/
  DESTINATION ${CMAKE_INSTALL_INCLUDEDIR}
  FILES_MATCHING
  PATTERN "*.hpp")

install(FILES "${CMAKE_CURRENT_BINARY_DIR}/generated/spoken_time/export.hpp"
        DESTINATION "${CMAKE_INSTALL_INCLUDEDIR}/spoken_time")

write_basic_package_version_file(
  "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config-version.cmake"
  VERSION ${PROJECT_VERSION}
  COMPATIBILITY SameMajorVersion)

configure_package_config_file(
  "${CMAKE_CURRENT_SOURCE_DIR}/cmake/spoken_time-config.cmake.in"
  "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config.cmake"
  INSTALL_DESTINATION "${CMAKE_INSTALL_LIBDIR}/cmake/spoken_time"
  PATH_VARS CMAKE_INSTALL_DATAROOTDIR)

install(
  EXPORT spoken_time_targets
  FILE spoken_time-targets.cmake
  NAMESPACE spoken_time::
  DESTINATION "${CMAKE_INSTALL_LIBDIR}/cmake/spoken_time")

install(
  FILES "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config.cmake"
        "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config-version.cmake"
  DESTINATION "${CMAKE_INSTALL_LIBDIR}/cmake/spoken_time")

set(CPACK_GENERATOR TGZ)
install(FILES LICENSE NOTICE DESTINATION "${CMAKE_INSTALL_DATAROOTDIR}/doc/spoken_time")

set(CPACK_SOURCE_GENERATOR TGZ)
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY
    "Spoken time; MIT")
set(CPACK_PACKAGE_FILE_NAME "spoken-time-${PROJECT_VERSION}-${CMAKE_SYSTEM_NAME}")
set(CPACK_SOURCE_PACKAGE_FILE_NAME "spoken-time-${PROJECT_VERSION}-source")
set(CPACK_RESOURCE_FILE_LICENSE "${CMAKE_CURRENT_SOURCE_DIR}/LICENSE")
set(CPACK_RESOURCE_FILE_README "${CMAKE_CURRENT_SOURCE_DIR}/README.md")
set(CPACK_SOURCE_IGNORE_FILES "/build/;/.git/;~$;\\.DS_Store$")
include(CPack)
