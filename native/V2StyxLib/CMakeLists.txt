CMAKE_MINIMUM_REQUIRED(VERSION 3.22)

PROJECT(V2StyxLib)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED True)
set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED True)

include(FetchContent)
FetchContent_Declare(
    Catch2
    GIT_REPOSITORY https://github.com/catchorg/Catch2.git
    GIT_TAG        v3.5.3
)
FetchContent_MakeAvailable(Catch2)

include_directories(include) 

ADD_SUBDIRECTORY( lib out/lib )
#ADD_SUBDIRECTORY( testserver out/testserver )
