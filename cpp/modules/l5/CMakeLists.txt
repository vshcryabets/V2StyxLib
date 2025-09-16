CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

add_library(module_l5 STATIC
    src/BufferWriterImpl.cpp
    src/BufferReaderImpl.cpp
    src/messages/base/StyxMessage.cpp
    src/StyxSerializerImpl.cpp
    src/StyxQID.cpp
    src/StyxStat.cpp
    src/messages/v9p2000/BaseMessage.cpp
    src/messages/v9p2000/MessageFactoryImpl.cpp
)
target_link_libraries(module_l5 PUBLIC module_l4)
target_include_directories(module_l5 PUBLIC
    $<INSTALL_INTERFACE:include>
    $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
    )

add_executable(module_l5_tests
    test/test_BufferWriterImpl.cpp
    test/test_BufferReaderImpl.cpp
    test/test_StyxSerializerImpl.cpp
    test/test_MessageFactoryImpl.cpp
    )
target_include_directories(module_l5_tests PRIVATE ./include/)
target_link_libraries(module_l5_tests PRIVATE Catch2::Catch2WithMain module_l5)
catch_discover_tests(module_l5_tests 
    PROPERTIES 
    TIMEOUT 10)