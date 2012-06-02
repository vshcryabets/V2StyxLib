/*
 * IVirtualStyxDirectory.h
 *
 *  Created on: May 20, 2012
 *      Author: vschryabets@gmail.com
 */

#ifndef IVIRTUALSTYXDIRECTORY_H_
#define IVIRTUALSTYXDIRECTORY_H_
#include <string>
#include "IVirtualStyxFile.h"
using namespace std;

class IVirtualStyxDirectory : virtual public IVirtualStyxFile
{
public:
	virtual IVirtualStyxFile* getFile(StyxString *path) = 0;
	virtual IVirtualStyxDirectory* getDirectory(StyxString *path) = 0;
};


#endif /* IVIRTUALSTYXDIRECTORY_H_ */
