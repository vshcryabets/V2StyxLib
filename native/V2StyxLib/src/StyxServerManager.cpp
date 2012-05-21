/*
 * StyxServerManager.cpp
 *
 *  Created on: May 20, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#include "StyxServerManager.h"
#include "sys/socket.h"
#include "fcntl.h"
#include "string.h"
#include "netinet/in.h"
#include <arpa/inet.h>
#include <netdb.h>
#include <stdio.h>

StyxServerManager::StyxServerManager(string address,
		int port,
		IVirtualStyxDirectory *root):mPort(port), mRoot(root) {
	// create socket
	mSocket = socket(AF_INET, SOCK_STREAM, 0);
	if ( mSocket == -1 ) {
		throw "Can't create socket socket()";
	}
	int reuse_addr = 1;
	setsockopt(mSocket, SOL_SOCKET, SO_REUSEADDR, &reuse_addr,
			sizeof(reuse_addr));
	// enable non-blocking mode
	this->setNonBlocking(mSocket);
	// resolve address and bind socket
	struct sockaddr_in server_address;
	this->setAddress(address.c_str(), port, &server_address, NULL);
	if (bind(mSocket, (struct sockaddr *) &server_address,
			sizeof(server_address)) < 0 ) {
		close(mSocket);
		throw "Can't create socket bind()";
	}
}

StyxServerManager::~StyxServerManager() {
	// TODO Auto-generated destructor stub
}

void StyxServerManager::setAddress(const char * hname,
		short port,
		struct sockaddr_in * sap,
		char * protocol) {
	struct hostent *hp;

	memset(sap, 0, sizeof(*sap));
	sap->sin_family = AF_INET;
	if(hname != NULL) {
		if(!inet_aton(hname, &sap->sin_addr)) {
			hp = gethostbyname(hname);
			if(hp == NULL)
				throw "unknown host";
			sap->sin_addr = *(struct in_addr *)hp->h_addr;
		}
	}
	else {
		sap->sin_addr.s_addr = htonl( INADDR_ANY);
	}
	sap->sin_port = htons(port);
}

void StyxServerManager::readSockets(fd_set* socks, int* connectlist) {
	/* OK, now socks will be set with whatever socket(s)
	   are ready for reading.  Lets first check our
	   "listening" socket, and then check the sockets
	   in connectlist. */

	/* If a client is trying to connect() to our listening
		socket, select() will consider that as the socket
		being 'readable'. Thus, if the listening socket is
		part of the fd_set, we need to accept a new connection. */

	if (FD_ISSET(mSocket,socks))
		handleNewConnection(connectlist);
	/* Now check connectlist for available data */

	/* Run through our sockets and check to see if anyth			perror("select");
			exit(EXIT_FAILURE);
	 * ing
		happened with them, if so 'service' them. */

	for (int listnum = 0; listnum < 5; listnum++) {
		if (FD_ISSET(connectlist[listnum],socks))
			this->deaWithData(connectlist, listnum);
	} /* for (all entries in queue) */
}

void StyxServerManager::deaWithData(int* list, int idx) {
	char buffer[80];     /* Buffer for socket reads */
	char *cur_char;      /* Used in processing buffer */

	ssize_t readed = read(list[idx], buffer, 80);
	if (readed < 0) {
		/* Connection closed, close this end
		   and free up entry in connectlist */
		close(list[idx]);
		list[idx] = 0;
	} else {
		/* We got some data, so upper case it
		   and send it back. */
		cur_char = buffer;
		while (cur_char[0] != 0) {
			cur_char[0] = toupper(cur_char[0]);
			cur_char++;
		}
		write(list[idx],(const void*)buffer,readed);
	}
}

void StyxServerManager::handleNewConnection(int* connectlist) {
	/* We have a new connection coming in!  We'll
	try to find a spot for it in connectlist. */
	int inSocket = accept(mSocket, NULL, NULL);
	if (inSocket < 0) {
		return;
	}
	this->setNonBlocking(inSocket);
	for ( int listnum = 0; (listnum < 5) && (inSocket != -1); listnum ++)
		if (connectlist[listnum] == 0) {
			printf("\nConnection accepted:   FD=%d; Slot=%d\n",
				inSocket,listnum);
			connectlist[listnum] = inSocket;
			inSocket = -1;
		}
	if (inSocket != -1) {
		/* No room left in the queue! */
		printf("\nNo room left for new client.\n");
		const char *message = "Sorry, this server is too busy. Try again later!\r\n";
		write(inSocket, message, strlen(message));
		close(inSocket);
	}
}

void StyxServerManager::start() {
	/* Set up queue for incoming connections. */
	listen(mSocket,5);

	/* Since we start with only one socket, the listening socket,
		   it is the highest socket so far. */
	int highsock = mSocket;
	int connectlist[5];
	fd_set socks;
	struct timeval timeout;

	memset((char *) &connectlist, 0, sizeof(connectlist));

	while (1) { /* Main server loop - forever */
		/* First put together fd_set for select(), which will
			   consist of the sock veriable in case a new connection
			   is coming in, plus all the sockets we have already
			   accepted. */
		FD_ZERO(&socks);
		/* FD_SET() adds the file descriptor "sock" to the fd_set,
				so that select() will return if a connection comes in
				on that socket (which means you have to do accept(), etc. */
		FD_SET(mSocket,&socks);

		/* Loops through all the possible connections and adds
				those sockets to the fd_set */
		for (int listnum = 0; listnum < 5; listnum++) {
			if (connectlist[listnum] != 0) {
				FD_SET(connectlist[listnum],&socks);
				if (connectlist[listnum] > highsock)
					highsock = connectlist[listnum];
			}
		}

		timeout.tv_sec = 1;
		timeout.tv_usec = 0;

		/* The first argument to select is the highest file
				descriptor value plus 1. In most cases, you can
				just pass FD_SETSIZE and you'll be fine. */

		/* The second argument to select() is the address of
				the fd_set that contains sockets we're waiting
				to be readable (including the listening socket). */

		/* The third parameter is an fd_set that you want to
				know if you can write on -- this example doesn't
				use it, so it passes 0, or NULL. The fourth parameter
				is sockets you're waiting for out-of-band data for,
				which usually, you're not. */

		/* The last parameter to select() is a time-out of how
				long select() should block. If you want to wait forever
				until something happens on a socket, you'll probably
				want to pass NULL. */

		int readsocks = select(highsock+1, &socks, (fd_set *) 0,
				(fd_set *) 0, &timeout);

		/* select() returns the number of sockets that had
				things going on with them -- i.e. they're readable. */

		/* Once select() returns, the original fd_set has been
				modified so it now reflects the state of why select()
				woke up. i.e. If file descriptor 4 was originally in
				the fd_set, and then it became readable, the fd_set
				contains file descriptor 4 in it. */

		if (readsocks < 0) {
			return;
		}
		if (readsocks == 0) {
			/* Nothing ready to read, just show that
				   we're alive */
		} else
			readSockets(&socks, connectlist);
	} /* while(1) */
}

void StyxServerManager::stop() {

}

void StyxServerManager::setNonBlocking(int socket) {
	int opts;
	opts = fcntl(socket,F_GETFL);
	if (opts < 0) {
		throw "Can't create socket fcntl(F_GETFL)";
	}
	opts = (opts | O_NONBLOCK);
	if (fcntl(socket,F_SETFL,opts) < 0) {
		throw "Can't create socket fcntl(F_SETFL)";
	}
}
