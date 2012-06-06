CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

PROJECT(V2StyxLib)
#ADD_DEFINITIONS(-g -O2 -fsigned-char -freg-struct-return -Wall -W -Wshadow -Wpointer-arith -Wcast-qual -Winline -Werror)

ADD_DEFINITIONS(-g -O2 -Wall)
ADD_SUBDIRECTORY( src )
#ADD_SOURCE_DIRECTORY( structs )
#ADD_SOURCE_DIRECTORY( server )
#ADD_SOURCE_DIRECTORY( messages )
#ADD_SOURCE_DIRECTORY( io )

