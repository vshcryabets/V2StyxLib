/*
 * Main.cpp
 *
 *  Created on: May 20, 2012
 *      Author: mrco
 */
#include <string>
#include "StyxServerManager.h"
#include "vfs/MemoryStyxDirectory.h"
using namespace std;

int main(int argc, char **argv) {
	string serveraddr = "127.0.0.1";
	IVirtualStyxDirectory *root = new MemoryStyxDirectory("root");
	string protocol = "9p2000";
	StyxServerManager manager(serveraddr, 8080, root, &protocol);
	manager.start();
}
