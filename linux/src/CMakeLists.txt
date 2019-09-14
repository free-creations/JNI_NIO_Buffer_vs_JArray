#============================================================================
# File        : CMakeLists.txt
# Description : CMake-script to build the JNI Library.
#
# Copyright 2019 Harald Postner (www.free-creations.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#============================================================================

# we need the header files of the Java Development Kit.
find_package(JNI REQUIRED)

set(CMAKE_CXX_STANDARD 11)


set(CMAKE_CXX_FLAGS "-Wall -Wextra")
set(CMAKE_CXX_FLAGS_DEBUG "-g")
set(CMAKE_CXX_FLAGS_RELEASE "-O3")

#
add_library(${LIB_NAME} SHARED main.cpp) # LIB_NAME is defined in the calling CMakeLists.txt
target_include_directories(${LIB_NAME} PUBLIC ${JNI_INCLUDE_DIRS})
target_link_libraries(${LIB_NAME} ${JNI_LIBRARIES})

