#include "StyxLibraryException.h"

StyxLibraryException::StyxLibraryException(const char* classname, const char* message) {
	mMessage = message;
	mClassName = classname;
}

StyxLibraryException::StyxLibraryException(std::string classname, std::string message) {
	mMessage = message;
	mClassName = classname;
}


StyxLibraryException::~StyxLibraryException(void) {
}

std::string StyxLibraryException::getMessage(void) {
	return mMessage;
}
