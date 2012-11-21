package com.easyfollow.shake;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.renrensdkdemo.R;

public class NewShakeActivity extends Activity {
	private Button bind;
	private Button shake;
	private ImageView star;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_shake);
		
		init();
	}
	
	// 初始化并调整位置
	private void init(){
		//得到屏幕宽和高
		WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        Log.d("width", Integer.toString(screenWidth));
        Log.d("height", Integer.toString(screenHeight));
        
		bind = (Button) findViewById(R.id.ef_shake_binding_button);
		bind.setTextColor(Color.WHITE);
		bind.getBackground().setAlpha(64);
		
		
		star = (ImageView) findViewById(R.id.ef_shake_imageStar);
		RelativeLayout.LayoutParams lay_star = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		lay_star.height = (int)(18.0*(double)screenHeight/48.0);
		lay_star.width = lay_star.height;
		lay_star.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lay_star.addRule(RelativeLayout.ALIGN_TOP);
		lay_star.topMargin = (int)(61.0*(double)screenHeight/480.0);
		Log.d("star_margin_top", Integer.toString(lay_star.topMargin));
		star.setLayoutParams(lay_star);
		
		
		shake = (Button) findViewById(R.id.ef_shake_shake_button);
		int shake_height = (int)(42.0*(double)screenHeight/480.0);
		int shake_width = (int)(42.0*(double)screenHeight*202.0/(480.0*42.0));
		Log.d("shake_width", Integer.toString(shake_width));
        Log.d("shake_height", Integer.toString(shake_height));
		RelativeLayout.LayoutParams lay_shake = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
		lay_shake.height = shake_height;
		lay_shake.width = shake_width;
		lay_shake.addRule(RelativeLayout.CENTER_HORIZONTAL);
		lay_shake.addRule(RelativeLayout.ALIGN_BOTTOM);
		lay_shake.addRule(RelativeLayout.BELOW, R.id.ef_shake_imageStar);
//		lay_shake.bottomMargin = (int)(60.0*(double)screenHeight/480.0);
		lay_shake.topMargin = (int)(57.0*(double)screenHeight/480.0);
		Log.d("shake_margin_top", Integer.toString(lay_shake.topMargin));
		shake.setLayoutParams(lay_shake);
		
		RelativeLayout linear = (RelativeLayout) findViewById(R.id.ef_shake_linear_layout);
		int linear_height = (int)(44.0*(double)screenHeight/480.0);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, linear_height);
		linear.setLayoutParams(params);
	}

}
