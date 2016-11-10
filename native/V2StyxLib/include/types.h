/*
 * types.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef TYPES_H_
#define TYPES_H_
#include <stdint.h>
#include <string>

#ifdef WIN32
	#include <winsock2.h>
	typedef SOCKET Socket;
	#define errno WSAGetLastError()
#else
	#include "errno.h"
	typedef int Socket;
	// Constants
	const int INVALID_SOCKET = -1;
#endif

typedef int64_t Date;
typedef int64_t int128_t;
typedef int32_t StyxThread;

enum MessageTypeEnum {
	Tversion = 100,
	Rversion = 101,
	Tauth = 102,
	Rauth = 103,
	Tattach = 104,
	Rattach = 105,
	Rerror = 107,
	Tflush = 108,
	Rflush = 109,
	Twalk = 110,
	Rwalk = 111,
	Topen = 112,
	Ropen = 113,
	Tcreate = 114,
	Rcreate = 115,
	Tread = 116,
	Rread = 117,
	Twrite = 118,
	Rwrite = 119,
	Tclunk = 120,
	Rclunk = 121,
	Tremove = 122,
	Rremove = 123,
	Tstat = 124,
	Rstat = 125,
	Twstat = 126,
	Rwstat = 127
};

enum ModeTypeEnum {
    OREAD = 0x00,
    OWRITE = 0x01,
    ORDWR = 0x02,
    OEXEC = 0x03,
    OTRUNC = 0x10
};

enum FileModeEnum {
	Directory = 0x80000000L, AppendOnly = 0x40000000L, ExclusiveUse = 0x20000000L,
	MountedChannel = 0x10000000L, AuthenticationFile = 0x08000000L,
	TemporaryFile = 0x04000000L, ReadOwnerPermission = 0x00000100L,
	WriteOwnerPermission = 0x00000080L, ExecuteOwnerPermission = 0x00000040L,
	ReadGroupPermission = 0x00000020L, WriteGroupPermission = 0x00000010L,
	ExecuteGroupPermission = 0x00000008L, ReadOthersPermission = 0x00000004L,
	WriteOthersPermission = 0x00000002L, ExecuteOthersPermission = 0x00000001L
};

typedef uint8_t MessageType;
typedef uint8_t QIDType;
typedef uint32_t StyxFID;
typedef uint16_t StyxTAG;
typedef std::string StyxString;
typedef ModeTypeEnum StyxMode;

class Credentials {
protected:
    StyxString mUserName;
    StyxString mPassword;
public:
    Credentials(StyxString username, StyxString password);
	~Credentials();
	StyxString getUserName();
	StyxString getPassword();
};

#endif /* TYPES_H_ */
