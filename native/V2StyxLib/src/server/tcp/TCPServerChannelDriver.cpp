/*
 * TCPServerChannelDriver.cpp
 *
 */
#include "server/tcp/TCPServerChannelDriver.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>
#include <unistd.h>

TCPServerChannelDriver::TCPServerChannelDriver(StyxString address, uint16_t port) 
    : TCPChannelDriver(address, port), mSocket(INVALID_SOCKET) {
}

TCPServerChannelDriver::~TCPServerChannelDriver() {
}

void TCPServerChannelDriver::prepareSocket() throw(StyxException) {
    int sockfd = ::socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		  throw StyxException("Can't create socket");
	}
    
    int reuse_addr = 1;
	::setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, (const char*)&reuse_addr, sizeof(reuse_addr));

    // enable non-blocking mode
    setNonBlocking(sockfd);

    struct sockaddr_in serv_addr;
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
#warning INADDR_ANY this is incorrect, we should listen specified socketAddress interface
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(mPort);
    int result = ::bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    if (result < 0) {
#ifdef WIN32
		closesocket(res);
#else
		::close(sockfd);
#endif
        throw StyxException("Can't bind socket");
    }
#warning set socket timeout
    mSocket = sockfd;
}

size_t TCPServerChannelDriver::getMaxPendingQueue() {
    return 5;
}

std::vector<ClientDetails*> TCPServerChannelDriver::getClients() {
    std::vector<ClientDetails*> result;
    for (std::map<Socket, ClientDetails*>::iterator it = mClientStatesMap.begin();
        it != mClientStatesMap.end(); it++) {
            result.push_back(it->second);
        }
    return result;
}

bool TCPServerChannelDriver::isConnected() {
    return true;
}

bool TCPServerChannelDriver::isStarted() {
    return isWorking;
}

StyxString TCPServerChannelDriver::toString() {
    return StyxString("TCPServerChannelDriver");
}

void* TCPServerChannelDriver::run() {
    isWorking = true;
    ::listen(mSocket, getMaxPendingQueue());
    // Since we start with only one socket, the listening socket, it is the highest socket so far.
	Socket highsock = mSocket;
    fd_set socks;
    struct timeval timeout;
    while (isWorking && !mAcceptorThread->isInterrupted()) {
        FD_ZERO(&socks);
        FD_SET(mSocket, &socks);

        for (std::map<Socket, ClientDetails*>::iterator it = mClientStatesMap.begin(); 
            it != mClientStatesMap.end(); it++ ) {
            if ( it->first > highsock ) {
                highsock = it->first;
            }
            FD_SET(it->first, &socks);
        }

        timeout.tv_sec = 1;
        timeout.tv_usec = 0;
        int readsocks = ::select(highsock + 1, &socks, (fd_set*) 0, (fd_set*) 0, &timeout);

        if (readsocks < 0) {
			// something wrong with our socket
			break;
		} else if (readsocks > 0) {
            if (FD_ISSET(mSocket, &socks)) {
				Socket clientChannel = ::accept(mSocket, NULL, NULL);
				if ( clientChannel > 0 ) {
					setNonBlocking(clientChannel);
#warning we can add new socket to FD_SET here
                    mNewConnetions.push(clientChannel);
				}
			}
            for (std::map<Socket, ClientDetails*>::iterator it = mClientStatesMap.begin(); 
                it != mClientStatesMap.end(); it++ ) {
				if (FD_ISSET(it->first, &socks)) {
                    mReadable.push(it->first);
				}
			}
#warning processEventsQueue();
        }
    }
    ::close(mSocket);
    mSocket = INVALID_SOCKET;
    isWorking = false;
}

void TCPServerChannelDriver::setNonBlocking(Socket socket) {
#ifdef WIN32
	// If iMode!=0, non-blocking mode is enabled.
	u_long mode=1;
	ioctlsocket(socket, FIONBIO, &mode);
#else
	int opts;
	opts = ::fcntl(socket, F_GETFL);
	if (opts < 0) {
		throw StyxException("Can't create socket fcntl(F_GETFL)");
	}
	opts = (opts | O_NONBLOCK);
	if (::fcntl(socket, F_SETFL,opts) < 0) {
		throw StyxException("Can't create socket fcntl(F_SETFL)");
	}
#endif
}