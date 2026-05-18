CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

add_library(module_l4_c STATIC)

set(MODULE_L4C_SRC
    src/arch.c
    src/Channel_c.c
)

target_sources(module_l4_c
    PRIVATE
        ${MODULE_L4C_SRC}
    )

target_include_directories(module_l4_c PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )


if (ENABLE_CATCH2)
    add_executable(module_l4_c_tests
        test/test_Channel_c.cpp
        ${MODULE_L4C_SRC}
        )
    target_compile_definitions(module_l4_c_tests PRIVATE TARGET_CATCH2)
    target_link_libraries(module_l4_c_tests PUBLIC Catch2::Catch2WithMain)
    target_include_directories(module_l4_c_tests PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)

    catch_discover_tests(module_l4_c_tests 
        PROPERTIES 
        TIMEOUT 10)
endif()
