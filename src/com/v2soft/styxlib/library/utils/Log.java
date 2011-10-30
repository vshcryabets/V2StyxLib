package com.v2soft.styxlib.library.utils;

public class Log {

	public static void writeObject(Object object)
	{
		writeObject(null, object, false);
	}
	
	public static void writeObject(String title, Object object)
	{
		writeObject(title, object, false);
	}
	
	public static void writeObject(Object object, boolean separator)
	{
		writeObject(null, object, separator);
	}
	
	public static void writeObject(String title, Object object, boolean separator)
	{
		if (title != null)
			System.out.println(title);
		System.out.println(object.toString());
		if (separator)
			writeSeparator();
	}
	
	public static void writeString(String string)
	{
		writeString(string, false);
	}
	
	public static void writeString(String string, boolean separator)
	{
		System.out.println(string);
		if (separator)
			writeSeparator();
	}
	
	public static void writeSeparator()
	{
		System.out.println("----------------------------");
	}
	
}
