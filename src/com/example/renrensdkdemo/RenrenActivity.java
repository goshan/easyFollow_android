package com.example.renrensdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.RenrenAuthListener;

public class RenrenActivity extends Activity {
	//你的应用ID
	private static final String APP_ID = "214568";
	//应用的API Key
	private static final String API_KEY = "a68774405e5d43fca919fe2436f07fcf";
	//应用的Secret Key
	private static final String SECRET_KEY = "b61906593d0b47698ef054000f05bc01";

	private Renren renren;
	private Handler handler;
	
	private Button loginBtn;
	private Button logoutBtn;
	private TextView loginText;
	private TextView showText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renren);
        init();

        //登录的Listener
        final RenrenAuthListener listener = new RenrenAuthListener() {
            //登录成功
            public void onComplete(Bundle values) {
                showLoginBtn(false);
                loginText.setText(R.string.auth_success);
    			
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RenrenActivity.this,
                        		RenrenActivity.this.getString(R.string.auth_success),
                            Toast.LENGTH_SHORT).show();
                    }
                });
                showText.setText(renren.getAccessToken());
//                System.out.println(renren.getAccessToken());
            }

            //登录失败
            @Override
            public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
                loginText.setText(R.string.auth_failed);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RenrenActivity.this,
                        		RenrenActivity.this.getString(R.string.auth_failed),
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

        //登录按钮的事件
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renren.authorize(RenrenActivity.this, listener);
            }
        });
    	
        //退出按钮的事件
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renren.logout(getApplicationContext());
                showLoginBtn(true);
                loginText.setText(R.string.auth_string);
            }
        });
    }

    //初始化按钮和Renren实例
    private void init(){
        renren = new Renren(API_KEY, SECRET_KEY, APP_ID, RenrenActivity.this);
        handler = new Handler();
    	
        loginBtn = (Button) findViewById(R.id.loginBtn);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);
        loginText = (TextView) findViewById(R.id.loginText);
        showText = (TextView) findViewById(R.id.showText);
    	
        showLoginBtn(true);
    }

    //显示登录/退出按钮
    private void showLoginBtn(boolean flag){
        if(flag){
            loginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
        } else {
            loginBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_renren, menu);
        return true;
    }
}
