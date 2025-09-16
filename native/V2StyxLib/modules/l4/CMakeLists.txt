CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

add_library(module_l4 STATIC)

target_sources(module_l4
    PRIVATE
        src/ChannelDriverTcp.cpp
    PUBLIC
        include/ChannelDriver.h
        include/ChannelDriverTcp.h
        include/data.h
    )

target_include_directories(module_l4 PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )
