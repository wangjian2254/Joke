package com.wj.joke.staticvalue;

import android.os.Environment;

public class Convert {
	public static final String hosturl="http://joke.zxxsbook.com"; 
//	public static final String hosturl="http://blog.zxxsbook.com"; 
//	public static final String hosturl="http://192.168.101.151:8003"; 
	public static final String DATABASE_NAME="joke"; 
	public static final int DATABASE_VERSON=4; 
	public static final String Verson="1.2";
	
	public static String picPath   = Environment.getExternalStorageDirectory()+"/Joke/Picture/";
	public static String infPath   = Environment.getExternalStorageDirectory()+"/Joke/";
	public static String picLog="pic.txt";

	public static final String testNum="&testNum=12";
	public static  String username=null;
	public static  String userpwd=null;
	

	
}
