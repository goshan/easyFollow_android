package com.easyfollow.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.dongxuexidu.douban4j.model.app.AccessToken;
import com.dongxuexidu.douban4j.model.app.DoubanException;
import com.dongxuexidu.douban4j.provider.OAuthDoubanProvider;

public class DoubanWebview extends Activity {
	public final static int RESULT_CODE = 2;
	public final static int ERROR_CODE = 3;
    private static final String TAG = "DoubanWebView";
    private OAuthDoubanProvider oAuth;
    
    //loading
    private ProgressDialog pd = null;
    
    //handler，监听loading
    private Handler handler = new Handler(){  
    	@Override  
        public void handleMessage(Message msg) {
    		if(pd != null) {
    			Log.i(TAG, "handle dismiss");
    			pd.dismiss();
    			pd = null;
    		}
    	}
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        WebView webView = new WebView(this);
        linearLayout.addView(webView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        setContentView(linearLayout);
        Intent intent = this.getIntent();
        oAuth = (OAuthDoubanProvider) intent.getExtras().getSerializable("oauth");
        String urlStr = oAuth.getGetCodeRedirectUrl();
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webView.requestFocus();
        webView.loadUrl(urlStr);
//        Log.d(tag, msg)
        System.out.println(urlStr.toString());
        Log.i(TAG, "WebView Starting....");
        WebViewClient client = new WebViewClient() {
        	
        	@Override
        	public void onPageFinished(WebView view, String url) {
        		Log.i(TAG, "Page finished");
        		handler.sendEmptyMessage(0);
        	}
        	
        	
            /**
             * 回调方法，当页面开始加载时执行
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            	if(pd != null) {
        	    	Log.i(TAG, "start dismiss");
        			pd.dismiss();
        			pd = null;
        		}
                Log.i(TAG, "WebView onPageStarted...");
                Log.i(TAG, "URL = " + url);
                if (url.indexOf("?code=") != -1) {
                    int start=url.indexOf("code=") + "code=".length();
                    String code = url.substring(start);
                    Log.d(TAG, "code="+code);
                    Intent intent = new Intent();
                    
                    AccessToken token = null;
                    try {
						token = oAuth.tradeAccessTokenWithCode(code);
						Log.d(TAG, "token="+token.toString());
					} catch (DoubanException e) {
						e.printStackTrace();
					}
                    if (token != null){
//                    	intent.putExtra("oauth", oAuth);
                    	//保存douban AccessToken
                        SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preference.edit();
                        editor.putString("douban_access", token.getAccessToken());
                        editor.putString("douban_expire", token.getExpiresIn().toString());
                        editor.putString("douban_id", token.getDoubanUserId());
                        editor.commit();
                        
                    	setResult(RESULT_CODE, intent);
                    }
                    else {
                    	setResult(ERROR_CODE, intent);
                    }
                    view.destroyDrawingCache();
                    view.destroy();
                    finish();
                }
                super.onPageStarted(view, url, favicon);
                Log.i(TAG, "pd show; url="+url);
                pd = ProgressDialog.show(DoubanWebview.this, "", "Loading...", true);
            }
            

        };
        webView.setWebViewClient(client);
    }
    
    
    @Override 
    public void onPause(){
	    if(pd != null) {
	    	Log.i(TAG, "pause dismiss");
			pd.dismiss();
			pd = null;
		}
	    super.onPause();
    } 
    
    @Override
	protected void onStop() {
		if (pd != null) {
			Log.i(TAG, "stop dismiss");
			pd.dismiss();
			pd = null;
		}
		super.onStop();
	}
    
    @Override
    protected void onDestroy(){
    	if (pd != null) {
			Log.i(TAG, "destroy dismiss");
			pd.dismiss();
			pd = null;
		}
    	super.onDestroy();
    }
    
}
