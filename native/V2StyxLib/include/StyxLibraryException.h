/*
 * StyxLibraryException - class that conatins detailed information about error
 *
 *  Created on: May 22, 2012
 *      Author: V.Shcriyabets (vshcryabets@gmail.com)
 */
#include <string>

class StyxLibraryException {
private:
	std::string mMessage;
	std::string mClassName;
public:
	StyxLibraryException(const char* classname, const char* message);
	StyxLibraryException(std::string classname, std::string message);
	virtual ~StyxLibraryException(void);
	std::string getMessage();
};

