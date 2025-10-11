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

# if(CMAKE_CXX_COMPILER_ID STREQUAL "Clang" AND CMAKE_SYSTEM_NAME STREQUAL "Linux")
#     # Try to find the libbacktrace library
#     find_library(LIBBACKTRACE_LIBRARY NAMES backtrace)
#     if(LIBBACKTRACE_LIBRARY)
#         message(STATUS "Found libbacktrace, linking against it for better stack traces with Clang.")
#         target_link_libraries(module_l4_tests PRIVATE ${LIBBACKTRACE_LIBRARY})
#     else()
#         message(WARNING "Could not find libbacktrace. Stack traces may be incomplete.")
#     endif()
# endif()

#  find_library(LIBBACKTRACE_LIBRARY NAMES backtrace
#                  HINTS /usr/lib /usr/local/lib /usr/lib/x86_64-linux-gnu/) # Add paths if needed               
# find_library(LIBBACKTRACE_LIBRARY NAMES backtrace)
# if(LIBBACKTRACE_LIBRARY)
#     message(STATUS "Found libbacktrace, linking against it for better stack traces with Clang.")
#     target_link_libraries(module_l4_tests PRIVATE ${LIBBACKTRACE_LIBRARY})
# else()
#     message(WARNING "Could not find libbacktrace. Stack traces may be incomplete.")
# endif()


# This helps CMake provide better error messages if the compiler doesn't fully
# support the C++23 standard.
target_compile_features(module_l4_tests PRIVATE cxx_std_23)


catch_discover_tests(module_l4_tests 
    PROPERTIES 
    TIMEOUT 10)
