package com.easyfollow.util;

import java.util.HashMap;

public class UrlClient {
	//最多等待时间
	public static int waitTime = 10*1000;
	
	public static String getResponse(HashMap<String, String> param, String type){
		UrlThread t = new UrlThread(param, type);
		t.start();
		try {
			t.join(waitTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return t.getResult();
	}
}
