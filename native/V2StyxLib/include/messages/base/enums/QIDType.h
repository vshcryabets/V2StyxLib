/*
 * QIDType.h
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */

#ifndef QIDTYPE_H_
#define QIDTYPE_H_
#include <stdint.h>
#include <string>

enum QIDTypeEnum {
	QTDIR = 0x80,
	QTAPPEND = 0x40,
	QTEXCL = 0x20,
	QTMOUNT = 0x10,
	QTAUTH = 0x08,
	QTTMP = 0x04,
	QTSYMLINK = 0x02,
	QTLINK = 0x01,
	QTFILE = 0x00
};

#endif /* QIDTYPE_H_ */
