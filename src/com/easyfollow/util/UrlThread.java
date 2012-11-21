package com.easyfollow.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

public class UrlThread extends Thread {
	private static String urlHead = "http://192.168.1.104:3000";
	
	private HashMap<String, String> param;
	private String type;
	private Handler hand;
	
	private String result = null;
	
	public UrlThread(HashMap<String, String> param, String type, Handler hand){
		this.param = param;
		this.type = type;
		this.hand = hand;
	}
	
	public void run(){
		StringBuffer sb = new StringBuffer();
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
				sb.append(tmp+"\n");
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String json = sb.toString();
		Log.d("UrlThread return json", json);
		// 没得到结果
		if (json.equals("")){
			hand.sendEmptyMessage(0);
			return;
		}
		
		if (type.equals("regist") || type.equals("update")){
			String token = "default";
			String success = "";
			try {
				JSONObject jsonObj = new JSONObject(json);
				token = jsonObj.getString("token");
				success = jsonObj.getString("result");
			} catch (Exception e) {
				e.printStackTrace();
			}
			// success才存储token
			if (success.equals("success")){
				result = token;
			}
			hand.sendEmptyMessage(0);
		}
		else if (type.equals("shake")){
			String name = ""; 
			String success = "";
			try {
				JSONObject jsonObj = new JSONObject(json);
				result = jsonObj.getString("result");
				JSONObject nearby = jsonObj.getJSONObject("nearby");
				name = nearby.getString("name");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (success.equals("success")){
				result = name;
			}
		}
	}
	
	
	public String getResult(){
		return result;
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
