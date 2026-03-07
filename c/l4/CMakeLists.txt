CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

add_library(module_l4_c STATIC)

target_sources(module_l4_c
    PRIVATE
        src/Channel_c.c
    )

target_include_directories(module_l4_c PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )
