CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

PROJECT(V2StyxLib)
INCLUDE_DIRECTORIES(include lib) 
ADD_DEFINITIONS(-g -O2 -fsigned-char -freg-struct-return -Wall)
# -W -Wshadow -Wpointer-arith -Wcast-qual -Winline -Werror)

ADD_DEFINITIONS(-g -O2 -Wall)
ADD_SUBDIRECTORY( lib bin )
ADD_SUBDIRECTORY( testserver bin )
