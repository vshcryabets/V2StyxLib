/*
 * StyxLibraryException - class that conatins detailed information about error
 *
 *  Created on: June 12, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */
#include <string>

class StyxLibraryException {
private:
	std::string mMessage;
	std::string mClassName;
	int mAdditionalCode;
public:
	StyxLibraryException(const char* classname, const char* message, int additionalCode);
	StyxLibraryException(std::string classname, std::string message, int additionalCode);
	virtual ~StyxLibraryException(void);
	std::string getMessage();
	int getAdditionalCode();
};

