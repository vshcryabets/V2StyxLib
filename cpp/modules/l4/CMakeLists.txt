CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

add_library(module_l4 STATIC)

target_sources(module_l4
    PRIVATE
        src/impl/ChannelUnixTcp.cpp
        src/impl/ClientsRepoImpl.cpp
    PUBLIC
        include/Channel.h
        include/ChannelUnixTcp.h
        include/data.h
    )

target_include_directories(module_l4 PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )

add_executable(module_l4_tests
    test/test_ChannelUnixTcp.cpp
    )
target_include_directories(module_l4_tests PRIVATE ./include/)
target_link_libraries(module_l4_tests PUBLIC Catch2::Catch2WithMain module_l4)
catch_discover_tests(module_l4_tests 
    PROPERTIES 
    TIMEOUT 10)
