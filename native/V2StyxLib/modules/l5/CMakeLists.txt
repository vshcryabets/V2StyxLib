CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

if(BUILD_MODULE_L5)
    add_library(module_l5 STATIC
        src/BufferWriterImpl.cpp
        src/BufferReaderImpl.cpp
        src/StyxMessage.cpp
    )
    target_include_directories(module_l5 PUBLIC
        $<INSTALL_INTERFACE:include>
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        )

    add_executable(module_l5_tests
        test/test_BufferWriterImpl.cpp
        test/test_BufferReaderImpl.cpp
        )
    target_include_directories(module_l5_tests PRIVATE ./include/)
    target_link_libraries(module_l5_tests PRIVATE Catch2::Catch2WithMain module_l5)
endif()