#============================================================================
# File        : CMakeLists.txt
# Description : CMake-project description to build the JNI Library.
#
# Copyright 2019 Harald Postner (Harald at free-creations.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#============================================================================
cmake_minimum_required(VERSION 3.5)
project(jnitest)

# the version shall be given on the command line (by build.gradle), otherwise we'll use "x.x.x"
set(VERSION "x.x.x" CACHE STRING "The project version")

# The default library-name shall be the downcased project-name with version appended.
string(TOLOWER ${CMAKE_PROJECT_NAME}_${VERSION} DEFAULT_LIB_NAME )

# the library-name shall be given on the command line (by build.gradle), otherwise we'll use the default name.
set(LIB_NAME ${DEFAULT_LIB_NAME} CACHE STRING "The name (stem) of the library")

# The directory where the final artifacts should be delivered shall be given on given on the command line,
#   otherwise we'll use "build/out".
set(DELIVERIES_DIR "${PROJECT_BINARY_DIR}/out" CACHE STRING "The directory where the library should be delivered")

# The unit tests take quite long to compile. So we make building with unit-tests an option.
set(WITH_UNIT_TESTS ON CACHE BOOL "Should unit-test be build.")

# Write the generated library into the DELIVERIES_DIR selected above.
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${DELIVERIES_DIR})

# All C++ project- sources reside here.
add_subdirectory(src)

# Build the unit-tests only if explicitly requested
IF(${WITH_UNIT_TESTS})
   # add_subdirectory(tests)
ENDIF(${WITH_UNIT_TESTS})

