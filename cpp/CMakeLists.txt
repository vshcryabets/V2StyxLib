CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

project(V2StyxLib LANGUAGES CXX)

enable_testing()
include(CTest)

# option(BUILD_MODULE_L5 "Build the L5 module" ON)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_STANDARD_REQUIRED True)
set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED True)

option(USE_STD_MEMORY "Use std lib <memory>" ON)

# find_package(Catch2 3 REQUIRED)

include(FetchContent)
FetchContent_Declare(
    Catch2
    GIT_REPOSITORY https://github.com/catchorg/Catch2.git
    GIT_TAG        v3.5.3
)
FetchContent_MakeAvailable(Catch2)
include(Catch)

# For GCC and Clang compilers
if(CMAKE_CXX_COMPILER_ID MATCHES "GNU|Clang")
    add_compile_options(-Werror=return-type)

# For the Microsoft MSVC compiler
elseif(CMAKE_CXX_COMPILER_ID STREQUAL "MSVC")
    add_compile_options(/we4716)
endif()

add_subdirectory(modules/l4)
add_subdirectory(modules/l5)


#include_directories(include) 

#ADD_SUBDIRECTORY( lib out/lib )

#ADD_SUBDIRECTORY( testserver out/testserver )
