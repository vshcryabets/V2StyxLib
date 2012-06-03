/*
 * Main.cpp
 *
 *  Created on: May 20, 2012
 *      Author: mrco
 */
#include <string>
#include "StyxServerManager.h"
#include "vfs/IVirtualStyxFile.h"
#include "vfs/MemoryStyxDirectory.h"
#include "stdio.h"
using namespace std;

int main(int argc, char **argv) {
	string serveraddr = "127.0.0.1";
	MemoryStyxDirectory root ("root");
	string protocol = "9P2000";
	MemoryStyxDirectory s1("s1");
	root.addFile(&s1);
	MemoryStyxDirectory s2("s2");
	root.addFile(&s2);
	s1.addFile(new MemoryStyxFile("testfile1"));
	s1.addFile(new MemoryStyxFile("testfile2"));
	s1.addFile(new MemoryStyxFile("testfile3"));
	s1.addFile(new MemoryStyxFile("testfile11"));
	s1.addFile(new MemoryStyxFile("testfile21"));
	s1.addFile(new MemoryStyxFile("testfile31"));
	s1.addFile(new MemoryStyxFile("testfile12"));
	s1.addFile(new MemoryStyxFile("testfile22"));
	s1.addFile(new MemoryStyxFile("testfile32"));
	StyxServerManager manager(serveraddr, 8080, &root, &protocol);
	try {
	manager.start();
	} catch (const char *e) {
		printf("Exception: %s \n", e);
	}
}
