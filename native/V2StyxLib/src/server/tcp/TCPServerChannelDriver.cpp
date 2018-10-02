/*
 * TCPServerChannelDriver.cpp
 *
 */
#include "server/tcp/TCPServerChannelDriver.h"
#include "server/tcp/TCPClientDetails.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <fcntl.h>
#include <unistd.h>
#include <strings.h>
#include "utils/Log.h"

TCPServerChannelDriver::TCPServerChannelDriver(StyxString address, uint16_t port) 
    : TCPChannelDriver(address, port), mSocket(INVALID_SOCKET), mLastClientId(1) {
}

TCPServerChannelDriver::~TCPServerChannelDriver() {
}

void TCPServerChannelDriver::prepareSocket() throw(StyxException) {
    int sockfd = ::socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		  throw StyxException(DRIVER_CREATE_ERROR);
	}   
	struct hostent *server;
	server = ::gethostbyname(mAddress.c_str());
	if (server == NULL) {
		throw StyxException(DRIVER_CANT_RESOLVE_NAME);
	}

    struct sockaddr_in serv_addr;
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr,
        (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    serv_addr.sin_port = htons(mPort);
    int result = ::bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
    if (result < 0) {
#ifdef WIN32
		closesocket(res);
#else
		::close(sockfd);
#endif
        throw StyxException(DRIVER_BIND_ERROR, 
            "Can't bind socket in TCPServerChannelDriver::prepareSocket() %d", errno);
    }
#warning set socket timeout
    mSocket = sockfd;

    int reuse_addr = 1;
	if (::setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, (const char*)&reuse_addr, sizeof(reuse_addr)) != 0) {
        throw StyxException(DRIVER_CONFIGURE_ERROR);
    }

    // enable non-blocking mode
    setNonBlocking(sockfd);
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
    while (isWorking && !mThread->isInterrupted()) {
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
                    printf("TCPServer got client %d\n", clientChannel);
					setNonBlocking(clientChannel);
#warning we can add new socket to FD_SET here
                    mNewConnetions.push(clientChannel);
				}
			}
            for (std::map<Socket, ClientDetails*>::iterator it = mClientStatesMap.begin(); 
                it != mClientStatesMap.end(); it++ ) {
				if (FD_ISSET(it->first, &socks)) {
                    printf("TCPServer got readable %d\n", it->first);
                    mReadable.push(it->first);
				}
			}
            printf("TCPServer processEventsQueue enter\n");
            processEventsQueue();
        }
    }
    ::close(mSocket);
    mSocket = INVALID_SOCKET;
    isWorking = false;
}

void TCPServerChannelDriver::setNonBlocking(Socket socket) throw(StyxException) {
#ifdef WIN32
	// If iMode!=0, non-blocking mode is enabled.
	u_long mode=1;
	ioctlsocket(socket, FIONBIO, &mode);
#else
	int opts;
	opts = ::fcntl(socket, F_GETFL);
	if (opts < 0) {
		throw StyxException(DRIVER_CONFIGURE_ERROR);
	}
	opts = (opts | O_NONBLOCK);
	if (::fcntl(socket, F_SETFL,opts) < 0) {
		throw StyxException(DRIVER_CONFIGURE_ERROR);
	}
#endif
}

void TCPServerChannelDriver::closeSocket() throw(StyxException) {
    if (mSocket == INVALID_SOCKET) {
        throw StyxException(DRIVER_CLOSE_ERROR);
    }
    if (::close(mSocket) < 0) {
        throw StyxException(DRIVER_CLOSE_ERROR);
    }
    mSocket = INVALID_SOCKET;
}

void TCPServerChannelDriver::processEventsQueue() throw(StyxException) {
    // new connections
    while (!mNewConnetions.empty()) {
        Socket channel = mNewConnetions.front();
        mNewConnetions.pop();
        setNonBlocking(channel);
        TCPClientDetails* client = new TCPClientDetails(channel, this,
                mIOUnit, mLastClientId++);
        mRMessageHandler->addClient(client);
        mTMessageHandler->addClient(client);
        mClientStatesMap[channel] = client;
    }
    // new readables
    while (!mReadable.empty()) {
        Socket channel = mReadable.front();
        mReadable.pop();
        std::map<Socket, ClientDetails*>::iterator it = mClientStatesMap.find(channel);
        if (it != mClientStatesMap.end()) {
            printf("TCPServer find client\n");
            if (readSocket((TCPClientDetails*)it->second)) {
                mTMessageHandler->removeClient(it->second);
                mRMessageHandler->removeClient(it->second);
                mClientStatesMap.erase(channel);
                #warning delete TCPClientDetails
#warning we should move close logic somewhere outside
                ::close(channel);
            }
        }
    }
}