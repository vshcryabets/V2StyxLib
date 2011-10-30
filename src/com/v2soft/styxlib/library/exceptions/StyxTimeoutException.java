package com.v2soft.styxlib.library.exceptions;

public class StyxTimeoutException extends StyxException {

	private static final long serialVersionUID = 2224159600274666037L;
	
	public StyxTimeoutException()
	{
		super("Time out.");
	}

}
