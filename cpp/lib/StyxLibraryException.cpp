#include "StyxLibraryException.h"

StyxLibraryException::StyxLibraryException(const char* classname, const char* message, int additionalCode ) {
	mMessage = message;
	mClassName = classname;
	mAdditionalCode = additionalCode;
}

StyxLibraryException::StyxLibraryException(std::string classname, std::string message, int additionalCode ) {
	mMessage = message;
	mClassName = classname;
	mAdditionalCode = additionalCode;
}


StyxLibraryException::~StyxLibraryException(void) {
}

std::string StyxLibraryException::getMessage(void) {
	return mMessage;
}
int StyxLibraryException::getAdditionalCode(void) {
	return mAdditionalCode;
}
