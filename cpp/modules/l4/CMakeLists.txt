CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

add_library(module_l4 STATIC)

target_sources(module_l4
    PRIVATE
        src/Channel.cpp
        src/impl/ChannelUnixTcp.cpp
        src/impl/ClientsRepoImpl.cpp
        src/impl/ChannelUnixPipeImpl.cpp
        src/impl/ChannelUnixFile.cpp
    )

target_include_directories(module_l4 PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )

add_executable(module_l4_tests
    test/test_ChannelUnixTcp.cpp
    test/test_ChannelUnixPipeImpl.cpp
    )

if (USE_STD_MEMORY)
    target_compile_definitions(module_l4 PRIVATE USE_STD_MEMORY)
    target_compile_definitions(module_l4_tests PRIVATE USE_STD_MEMORY)
endif()

if(CMAKE_CXX_STANDARD GREATER_EQUAL 23)
    target_sources(module_l4 PRIVATE src/cxx_23/ChannelTx.cpp)
elseif(CMAKE_CXX_STANDARD GREATER_EQUAL 17)
    target_sources(module_l4 PRIVATE src/cxx_17/ChannelTx.cpp)
else()
    message(FATAL_ERROR "module_l4 requires C++17 or higher. Please set CMAKE_CXX_STANDARD to 17 or above.")
endif()

target_include_directories(module_l4_tests PRIVATE ./include/)
target_link_libraries(module_l4_tests PUBLIC Catch2::Catch2WithMain module_l4)

# This helps CMake provide better error messages if the compiler doesn't fully
# support the C++23 standard.
target_compile_features(module_l4_tests PRIVATE cxx_std_23)

catch_discover_tests(module_l4_tests 
    PROPERTIES 
    TIMEOUT 10)
