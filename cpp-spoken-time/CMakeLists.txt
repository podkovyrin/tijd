cmake_minimum_required(VERSION 3.25)

project(
  spoken_time
  VERSION 0.1.0
  DESCRIPTION "Cross-platform spoken-time formatting library"
  LANGUAGES CXX)

include(CMakePackageConfigHelpers)
include(CTest)
include(GenerateExportHeader)
include(GNUInstallDirs)

option(SPOKEN_TIME_WARNINGS_AS_ERRORS "Treat compiler warnings as errors"
       ${PROJECT_IS_TOP_LEVEL})

add_library(spoken_time src/spoken_time.cpp)
add_library(spoken_time::spoken_time ALIAS spoken_time)

generate_export_header(
  spoken_time
  BASE_NAME spoken_time
  EXPORT_FILE_NAME "${CMAKE_CURRENT_BINARY_DIR}/generated/spoken_time/export.hpp")

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
  target_compile_options(spoken_time PRIVATE /permissive- /W4)
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

install(FILES "${CMAKE_CURRENT_SOURCE_DIR}/../LICENSE"
        DESTINATION "${CMAKE_INSTALL_DATAROOTDIR}/licenses/spoken_time")

write_basic_package_version_file(
  "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config-version.cmake"
  VERSION ${PROJECT_VERSION}
  COMPATIBILITY SameMajorVersion)

configure_package_config_file(
  "${CMAKE_CURRENT_SOURCE_DIR}/cmake/spoken_time-config.cmake.in"
  "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config.cmake"
  INSTALL_DESTINATION "${CMAKE_INSTALL_LIBDIR}/cmake/spoken_time")

install(
  EXPORT spoken_time_targets
  FILE spoken_time-targets.cmake
  NAMESPACE spoken_time::
  DESTINATION "${CMAKE_INSTALL_LIBDIR}/cmake/spoken_time")

install(
  FILES "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config.cmake"
        "${CMAKE_CURRENT_BINARY_DIR}/spoken_time-config-version.cmake"
  DESTINATION "${CMAKE_INSTALL_LIBDIR}/cmake/spoken_time")
