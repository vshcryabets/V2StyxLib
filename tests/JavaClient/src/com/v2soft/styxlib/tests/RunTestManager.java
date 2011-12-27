package com.v2soft.styxlib.tests;

public class RunTestManager {
	public static final int FILELIST = 1;
	public static final int FILECOPY = 2;
	public static final int FILEUPLOAD = 3;
	
	public static RunTest factory(int id)
	{
		if (id == FILELIST)
			return new FileListTest();
		else if (id == FILECOPY)
			return new FileCopyTest();
		else if (id == FILEUPLOAD)
			return new FileUploadTest();
		
		return null;
	}
	
}
