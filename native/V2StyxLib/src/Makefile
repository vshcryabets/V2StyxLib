CC = g++
SRCS= ClientState.cpp StyxQID.cpp StyxServerManager.cpp StyxStat.cpp Main.cpp \
    MemoryStyxDirectory.cpp ClientBalancer.cpp ClientsHandler.cpp \
    ClientState.cpp ConnectionAcceptor.cpp
CFLAGS=-g -Wall -Xlinker -zmuldefs -D _LINUX
## --save-temps
OBJS=$(SRCS:.cpp=.o)
.SUFFIXES: .cpp .o

.cpp.o:
	$(CC) $(CFLAGS) -c $<

all: $(OBJS)	
	$(CC) $(CFLAGS) $(OBJS) -o test
	
clean:
	rm *.o
	rm test
