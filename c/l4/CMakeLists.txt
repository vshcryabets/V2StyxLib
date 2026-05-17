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

if (ENABLE_CATCH2)
    add_executable(module_l4_c_tests
        test/test_Channel_c.cpp)
    target_link_libraries(module_l4_c_tests PUBLIC Catch2::Catch2WithMain module_l4_c)
    target_include_directories(module_l4_c_tests PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)

    catch_discover_tests(module_l4_c_tests 
        PROPERTIES 
        TIMEOUT 10)
endif()
