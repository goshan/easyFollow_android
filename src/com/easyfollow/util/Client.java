package com.easyfollow.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import android.util.Log;

public class Client {
	private static String urlHead = "http://192.168.1.104:3000";
	
	public static String getResponse(HashMap<String, String> param, String type){
		StringBuffer result = new StringBuffer();
		BufferedReader in = null;
		try {
			String url = urlHead;
			if (type.equals("regist")){
				url += "/create_user.json?" +buildUrl(param);
				
			}
			else if (type.equals("update")){
				url += "/update_user.json?" +buildUrl(param);
			}
			else if (type.equals("shake")){
				url += "/lookfor.json?" +buildUrl(param);
			}


			Log.d("Url info", url);
			URL realUrl = new URL(url);
			//打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			conn.connect(); 
			
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String tmp = "";
			while ((tmp = in.readLine()) != null){
				result.append(tmp+"\n");
			}
			Log.d("return json", result.toString());
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result.toString();
	}
	
	private static String buildUrl(HashMap<String, String> map){
		StringBuffer sb = new StringBuffer();
		for (HashMap.Entry<String,String> entry : map.entrySet()) {
			if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                urlEncodeUTF8(entry.getKey().toString()),
                urlEncodeUTF8(entry.getValue().toString())
            ));
		}
		return sb.toString();
	}
	
	private static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
