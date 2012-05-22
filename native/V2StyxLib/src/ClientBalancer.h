/*
 * ClientBalancer.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef CLIENTBALANCER_H_
#define CLIENTBALANCER_H_
#include <vector>
#include "ClientsHandler.h"
#include "IVirtualStyxDirectory.h"
#include "types.h"
using namespace std;

class ClientBalancer {
private:
	ClientsHandler *mHandler;
	vector<Socket> *mNewConnections;
	vector<Socket> *mReadable;
public:
	ClientBalancer(int iounit, IVirtualStyxDirectory *root);
	~ClientBalancer();
	void pushNewConnection(Socket socket);
	void pushReadable(Socket socket);
	void process();
};

#endif /* CLIENTBALANCER_H_ */
