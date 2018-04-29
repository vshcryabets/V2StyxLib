#include "types.h"

Credentials::Credentials(StyxString* username, StyxString* password) : mUserName(NULL), mPassword(NULL) {
	if (username != NULL) {
		mUserName = new std::string(username->c_str());
	}
	if (password != NULL) {
		mPassword = new std::string(password->c_str());
	}
}

Credentials::~Credentials() {
	if (mUserName != NULL) {
		delete mUserName;
	}
	if (mPassword != NULL) {
		delete mPassword;
	}
}

StyxString* Credentials::getUserName() {
	return mUserName;
}

StyxString* Credentials::getPassword() {
	return mPassword;
}

ConnectionDetails::ConnectionDetails(StyxString protocol, size_t iounit) : mProtocol(protocol), mIOUnit(iounit) {
}

ConnectionDetails::ConnectionDetails() {
	mIOUnit = 0;
	mProtocol = "";
}

StyxString ConnectionDetails::getProtocol() {
	return mProtocol;
}

size_t ConnectionDetails::getIOUnit() {
	return mIOUnit;
}
