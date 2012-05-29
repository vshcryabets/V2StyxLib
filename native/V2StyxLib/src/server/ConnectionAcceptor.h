/*
 * ConnectionAcceptor.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CONNECTIONACCEPTOR_H_
#define CONNECTIONACCEPTOR_H_
#include "../types.h"
#include "../classes.h"

class ConnectionAcceptor {
private:
	Socket mSocket;
	ClientBalancer *mBalancer;

	void setNonBlocking(Socket socket);
public:
	ConnectionAcceptor(Socket socket, ClientBalancer *balancer);
	~ConnectionAcceptor();
	// stop handling connections
	void stop();
	// start handling connections
	void start();
};

#endif /* CONNECTIONACCEPTOR_H_ */
