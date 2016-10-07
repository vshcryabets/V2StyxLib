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
