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

typedef int Socket;
typedef int64_t Date;
typedef int64_t int128_t;

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

enum QIDTypeEnum {
	QTDIR = 0x80,
	QTAPPEND = 0x40,
	QTEXCL = 0x20,
	QTMOUNT = 0x10,
	QTAUTH = 0x08,
	QTFILE = 0x00
};

typedef uint8_t MessageType;
typedef uint8_t QIDType;
typedef uint32_t StyxFID;
typedef uint16_t StyxTAG;
typedef std::string StyxString;

#endif /* TYPES_H_ */
