#include "types.h"

Credentials::Credentials(StyxString username, StyxString password) : mUserName(username), mPassword(password) {

}

Credentials::~Credentials() {

}

StyxString Credentials::getUserName() {
	return mUserName;
}

StyxString Credentials::getPassword() {
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
