/*
 * Main.cpp
 *
 *  Created on: May 20, 2012
 *      Author: vshcryabets@gmail.com
 */
#include <string>
#include "StyxServerManager.h"
#include "vfs/IVirtualStyxFile.h"
#include "vfs/MemoryStyxDirectory.h"
#include "StdOutFile.h"
#include "stdio.h"
#include "StyxLibraryException.h"
#ifdef WIN32
#include "WinSock2.h"
#endif
using namespace std;

int main(int argc, char **argv) {
#ifdef WIN32
	WSADATA wsaData = {0};
    // Initialize Winsock
    int iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
    if (iResult != 0) {
        wprintf(L"WSAStartup failed: %d\n", iResult);
        return 1;
    }
#endif
	try {
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
		s2.addFile(new StdOutFile("out"));
		StyxServerManager manager(serveraddr, 8080, &root, protocol);
		manager.start();
	} catch (StyxLibraryException *e) {
		printf("Exception: %s \n", e->getMessage().c_str());
	} catch (const char *e) {
		printf("Exception: %s \n", e);
	}
}
