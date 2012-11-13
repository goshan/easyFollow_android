package com.easyfollow.binding;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.dongxuexidu.douban4j.model.app.RequestGrantScope;
import com.dongxuexidu.douban4j.provider.OAuthDoubanProvider;
import com.easyfollow.shake.ShakeActivity;
import com.easyfollow.util.Client;
import com.easyfollow.util.DoubanWebview;
import com.example.renrensdkdemo.R;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.Util;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.tencent.weibo.oauthv2.OAuthV2;
import com.tencent.weibo.webview.OAuthV2AuthorizeWebView;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;

public class BindActivity extends Activity {
	/*
	 * Renren
	 */
	//你的RENREN应用ID
	private static final String RENREN_APP_ID = "214568";
	//RENREN应用的API Key
	private static final String RENREN_API_KEY = "a68774405e5d43fca919fe2436f07fcf";
	//RENREN应用的Secret Key
	private static final String RENREN_SECRET_KEY = "b61906593d0b47698ef054000f05bc01";
	//Renren用户权限
	private String[] renren_permission = {"send_message","send_notification","publish_feed",
				"read_user_status","publish_comment","status_update"};

	private Renren renren;
	private Handler handler;
	
	/*
	 * 新浪微博
	 */
	private static final String SINA_CONSUMER_KEY = "1799175553";// 替换为开发者的appkey，例如"1646212860";
	private static final String SINA_REDIRECT_URL = "http://easy-follow.com";
	
	private Weibo mWeibo;
	
	/*
	 * 腾讯微博
	 */
	
	//腾讯微博回调地址
	private static final String TENCENT_OAUTH_CALLBACK = "http://easy-follow.com"; 
	//腾讯微博APP KEY
	private static final String TENCENT_OAUTH_KEY = "801255147"; 
	//腾讯微博APP SECRET
	private static final String TENCENT_OAUTH_SECRET = "875d58fb566cb9e9183830dde6515fbc";
	
	private OAuthV2 oAuth;
	
	
	/*
	 * 豆瓣
	 */
	//腾讯微博回调地址
	private static final String DOUBAN_OAUTH_CALLBACK = "http://easy-follow.com"; 
	//腾讯微博APP KEY
	private static final String DOUBAN_OAUTH_KEY = "0c342ae9640503b8249c80bc2c0f0b28"; 
	//腾讯微博APP SECRET
	private static final String DOUBAN_OAUTH_SECRET = "f3e0862f5378b28c";
	
	private OAuthDoubanProvider douban_oauth;
	
	//手机imei
	private String imei;
	
	//绑定所有
	private Button bindAll;
	//绑定人人
	private Button renrenSwitch;
	private Button renrenSwitchDown;
	//绑定新浪
	private Button sinaSwitch;
	private Button sinaSwitchDown;
	//绑定腾讯
	private Button tencentSwitch;
	private Button tencentSwitchDown;
	//绑定豆瓣
	private Button doubanSwitch;
	private Button doubanSwitchDown;
	
	//返回shake
	private Button backShake;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bind);
		// 初始化
		init();
		
		/*
		 * Renren
		 */
		
		//登录Renren的Listener
        final RenrenAuthListener listener = new RenrenAuthListener() {
            //登录成功
            public void onComplete(Bundle values) {
                renrenSwitch.setText(R.string.auth_success);
    			
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BindActivity.this,
                        		BindActivity.this.getString(R.string.auth_success),
                            Toast.LENGTH_SHORT).show();
                    }
                });
                //保存renren AccessToken
                SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
                SharedPreferences.Editor editor = preference.edit();
                editor.putString("renren_access", renren.getAccessToken());
                editor.putLong("renren_expire", renren.getExpireTime());
                editor.commit();
            }

            //登录失败
            @Override
            public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
//                loginText.setText(R.string.auth_failed);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BindActivity.this,
                        		BindActivity.this.getString(R.string.auth_failed),
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelLogin() {
            }

            @Override
            public void onCancelAuth(Bundle values) {
            }
        };

        //人人开关按钮的事件
        renrenSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renren.authorize(BindActivity.this, renren_permission, listener);
            }
        });
        
        //人人开关按钮退出的事件
        renrenSwitchDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	renren.logout(getApplicationContext());
            	Toast.makeText(BindActivity.this, "退出", Toast.LENGTH_SHORT).show();
            	//重置token
            	SharedPreferences settings = getSharedPreferences("BindPre", MODE_PRIVATE);
            	SharedPreferences.Editor editor = settings.edit();
            	editor.putString("token", "default");
            	editor.commit();				
            }
        });
        
        /*
         * 新浪微博
         */
        //sina开关按钮的事件
        sinaSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mWeibo.authorize(BindActivity.this, new AuthDialogListener());
			}
		});
        
        //sina开关退出的事件
        sinaSwitchDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                AccessTokenKeeper.clear(BindActivity.this);
            	Util.clearCookies(getApplicationContext());
        		//保存sina AccessToken
                SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
                SharedPreferences.Editor editor = preference.edit();
                editor.putString("sina_access", "default");
                editor.putString("sina_expire", "-1");
                editor.commit();
            }
        });
        
        
        /*
         * 腾讯微博
         */
        tencentSwitch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BindActivity.this, OAuthV2AuthorizeWebView.class);//创建Intent，使用WebView让用户授权
                intent.putExtra("oauth", oAuth);
                startActivityForResult(intent,2);
			}
        });
        
        tencentSwitchDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Util.clearCookies(getApplicationContext());
        		//重置tencent AccessToken
                SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
                SharedPreferences.Editor editor = preference.edit();
                editor.putString("tencent_access", "default");
                editor.putString("tencent_expire", "-1");
                editor.putString("tencent_id", "-1");
                editor.commit();
            }
        });
        
        /*
         * 豆瓣
         * 
         */
        doubanSwitch.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(BindActivity.this, DoubanWebview.class);//创建Intent，使用WebView让用户授权
                intent.putExtra("oauth", douban_oauth);
                startActivityForResult(intent , 3);
			}
        });
        
        doubanSwitchDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Util.clearCookies(getApplicationContext());
        		//重置douban AccessToken
                SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
                SharedPreferences.Editor editor = preference.edit();
                editor.putString("douban_access", "default");
                editor.putString("douban_expire", "-1");
                editor.putString("douban_id", "-1");
                editor.commit();
            }
        });
        
    	
        //绑定所有按钮的事件
        bindAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences("BindPre", MODE_PRIVATE);
				// 是否有token，有是更新事件，没有是注册时间
				String defaultToken = settings.getString("token", "default");
				Log.d("default token", defaultToken);
				
				//参数
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("default_info", "0");
				//using_sns
				String sns = "";
				if (!settings.getString("renren_access", "default").equals("default")){
					sns += "1";
					param.put("renren_token", settings.getString("renren_access", "default"));
					param.put("renren_expire", Long.toString(settings.getLong("renren_expire", -1)));
				}
				else
					sns += "0";
				if (!settings.getString("sina_access", "default").equals("default")){
					sns += ",1";
					param.put("sina_id", settings.getString("sina_id", "default"));
					param.put("sina_token", settings.getString("sina_access", "default"));
					param.put("sina_expire", settings.getString("sina_expire", "-1"));
				}
				else
					sns += ",0";
				if (!settings.getString("tencent_access", "default").equals("default")){
					sns += ",1";
					param.put("tencent_openid", settings.getString("tencent_id", "default"));
					param.put("tencent_token", settings.getString("tencent_access", "default"));
					param.put("tencent_expire", settings.getString("tencent_expire", "-1"));
				}
				else
					sns += ",0";
				if (!settings.getString("douban_access", "default").equals("default")){
					sns += ",1";
					param.put("douban_id", settings.getString("douban_id", "default"));
					param.put("douban_token", settings.getString("douban_access", "default"));
					param.put("douban_expire", settings.getString("douban_expire", "-1"));
				}
				else
					sns += ",0";
				
				param.put("using_sns", sns);
				
				String json = "";
				//注册
				if (defaultToken.equals("default")){
					param.put("signup_from", "2");
					param.put("android_imei", imei);
					
					json = Client.getResponse(param, "regist");
				}
				//更新
				else {
					param.put("token", settings.getString("token", "default"));
					json = Client.getResponse(param, "update");
				}
					
				//得到json，存储本应用token
				Log.d("result", json);
				
				String token = "default";
				try {
					JSONObject jsonObj = new JSONObject(json);
					token = jsonObj.getString("token");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				Log.d("token", token);
				//存储token
				SharedPreferences.Editor editor = settings.edit();
                editor.putString("token", token);
                editor.commit();					
			}
		});
        
        //返回按钮的事件
        backShake.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//跳转Activity
				Intent i = new Intent();
				i.setClass(BindActivity.this, ShakeActivity.class);
				startActivity(i);
			}
		});
	}
	
	
	//初始化按钮和实例
    private void init(){
    	//Renren
        renren = new Renren(RENREN_API_KEY, RENREN_SECRET_KEY, RENREN_APP_ID, BindActivity.this);
        handler = new Handler();
        renrenSwitch = (Button) findViewById(R.id.renrenSwitch_Bind);
		renrenSwitchDown = (Button) findViewById(R.id.renrenSwitchDown_Bind);
        //Sina
        mWeibo = Weibo.getInstance(SINA_CONSUMER_KEY, SINA_REDIRECT_URL);
        sinaSwitch = (Button) findViewById(R.id.sinaSwitch_Bind);
        sinaSwitchDown = (Button) findViewById(R.id.sinaSwitchDown_Bind);
        //Tencent
        oAuth=new OAuthV2(TENCENT_OAUTH_CALLBACK);
        oAuth.setClientId(TENCENT_OAUTH_KEY);
        oAuth.setClientSecret(TENCENT_OAUTH_SECRET);
        tencentSwitch = (Button) findViewById(R.id.tecentSwitch_Bind);
        tencentSwitchDown = (Button) findViewById(R.id.tecentSwitchDown_Bind);
        //豆瓣
        douban_oauth = new OAuthDoubanProvider();
        douban_oauth.setApiKey(DOUBAN_OAUTH_KEY).setSecretKey(DOUBAN_OAUTH_SECRET);
        douban_oauth.setRedirectUrl(DOUBAN_OAUTH_CALLBACK);
        douban_oauth.addScope(RequestGrantScope.SHUO_READ_SCOPE).addScope(RequestGrantScope.SHUO_WRITE_SCOPE).addScope(RequestGrantScope.BASIC_COMMON_SCOPE)
        			.addScope(RequestGrantScope.MAIL_READ_SCOPE).addScope(RequestGrantScope.MAIL_WRITE_SCOPE);
        doubanSwitch = (Button) findViewById(R.id.doubanSwitch_Bind);
        doubanSwitchDown = (Button) findViewById(R.id.doubanSwitchDown_Bind);
        
        
        bindAll = (Button)findViewById(R.id.bindingButton_Bind);
		backShake = (Button) findViewById(R.id.backButton_Bind);
		
		imei = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
				.getDeviceId();
    }

    
    
    
    
    
    
    //新浪监听
    class AuthDialogListener implements WeiboAuthListener {
    	@Override
    	public void onComplete(Bundle values) {
    		Log.d("Buddle value", values.toString());
    		String uid = values.getString("uid");
    		String token = values.getString("access_token");
    		String expires_in = values.getString("expires_in");
    		Log.d("sina id", uid);
    		Log.d("sina token", token);
    		Log.d("sina expires", expires_in);
    		
    		//保存sina AccessToken
            SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("sina_id", uid);
            editor.putString("sina_access", token);
            editor.putString("sina_expire", expires_in);
            editor.commit();
    	}

    	@Override
    	public void onError(WeiboDialogError e) {
//    		Toast.makeText(getApplicationContext(), "Auth error : " + e.getMessage(),
//    				Toast.LENGTH_LONG).show();
    	}

    	@Override
    	public void onCancel() {
    		Toast.makeText(getApplicationContext(), "Auth cancel", Toast.LENGTH_LONG).show();
    	}

    	@Override
    	public void onWeiboException(WeiboException e) {
//    		Toast.makeText(getApplicationContext(), "Auth exception : " + e.getMessage(),
//    				Toast.LENGTH_LONG).show();
    	}

    }
    
    
    /*
     * 腾讯通过读取OAuthV2AuthorizeWebView返回的Intent，获取用户授权信息
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data)   {
        if (requestCode==2) {
            if (resultCode == OAuthV2AuthorizeWebView.RESULT_CODE)    {
                oAuth=(OAuthV2) data.getExtras().getSerializable("oauth");
                Log.d("Tecent_success", oAuth.toString());
                if(oAuth.getStatus()==0){
                    Toast.makeText(getApplicationContext(), "登陆成功", Toast.LENGTH_SHORT).show();
                    //保存tecent AccessToken
                    SharedPreferences preference = getSharedPreferences("BindPre", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preference.edit();
                    editor.putString("tencent_access", oAuth.getAccessToken());
                    editor.putString("tencent_expire", oAuth.getExpiresIn());
                    editor.putString("tencent_id", oAuth.getOpenid());
                    editor.commit();
                }
            }
        }
        else if (requestCode == 3){
        	if (resultCode == DoubanWebview.RESULT_CODE) {
        		Toast.makeText(getApplicationContext(), "登陆成功", Toast.LENGTH_SHORT).show();
        	}
        	else if (resultCode == DoubanWebview.ERROR_CODE) {
        		Toast.makeText(getApplicationContext(), "登陆失败", Toast.LENGTH_SHORT).show();
        	}
        }
    }

}
