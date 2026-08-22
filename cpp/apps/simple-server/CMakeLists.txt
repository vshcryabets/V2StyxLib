CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

include(FetchContent)
FetchContent_Declare(
    spdlog
    GIT_REPOSITORY https://github.com/gabime/spdlog.git
    GIT_TAG        v1.17.0
)
FetchContent_MakeAvailable(spdlog)

add_executable(v2styxlib-simple-server
    src/server.cpp
)
target_link_libraries(v2styxlib-simple-server PUBLIC module_l4 spdlog::spdlog)
target_include_directories(v2styxlib-simple-server PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )
