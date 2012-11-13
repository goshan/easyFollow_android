package com.easyfollow.shake;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.easyfollow.binding.BindActivity;
import com.easyfollow.util.Client;
import com.example.renrensdkdemo.R;

//需要实现SensorEventListener接口
public class ShakeActivity extends Activity implements SensorEventListener{
	// 绑定按钮
	Button bind;
	// 摇一摇按钮
	Button shake;

	//定义sensor管理器
	private SensorManager mSensorManager;
	//震动
    private Vibrator vibrator;
    //地理位置管理器
    private LocationManager locationManager;
    //地理位置类
    private Criteria criteria;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shake);
            
		//获取传感器管理服务
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		//震动
		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		
		//获取到LocationManager对象
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		//创建一个Criteria对象
		criteria = new Criteria();
		//设置粗略精确度
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//设置是否需要返回海拔信息
		criteria.setAltitudeRequired(false);
		//设置是否需要返回方位信息
		criteria.setBearingRequired(false);
		//设置是否允许付费服务
		criteria.setCostAllowed(true);
		//设置电量消耗等级
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		//设置是否需要返回速度信息
		criteria.setSpeedRequired(false);
		
		
		bind = (Button) findViewById(R.id.bindingButton_Shake);
		shake = (Button) findViewById(R.id.shake_check);
		
		bind.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//跳转Activity
				Intent i = new Intent();
				i.setClass(ShakeActivity.this, BindActivity.class);
				startActivity(i);
			}
		});
		
		shake.setOnClickListener(new Button.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//摇动手机后，再伴随震动提示
				vibrator.vibrate(300);
				getLocationInfo();
			}
		});
    }

	@Override
	protected void onResume(){
		super.onResume();
		//加速度传感器
		mSensorManager.registerListener(this, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				//还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，
				//根据不同应用，需要的反应速率不同，具体根据实际情况设定
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onStop(){
		mSensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	protected void onPause(){
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//当传感器精度改变时回调该方法，Do nothing.
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		//values[0]:X轴，values[1]：Y轴，values[2]：Z轴
		float[] values = event.values;
		
		// 加速度阈值
		double yu = 15;
	  
		if(sensorType == Sensor.TYPE_ACCELEROMETER){
  
		/*因为一般正常情况下，任意轴数值最大就在9.8~10之间，只有在你突然摇动手机
		*的时候，瞬时加速度才会突然增大或减少。
		*所以，经过实际测试，只需监听任一轴的加速度大于14的时候，改变你需要的设置就OK了
		*/
			if((Math.abs(values[0])>yu || Math.abs(values[1])>yu || Math.abs(values[2])>yu)){
				//摇动手机后，再伴随震动提示
				vibrator.vibrate(300);
				
				getLocationInfo();
			}
		}
	}
	
	private void getLocationInfo(){
		//根据设置的Criteria对象，获取最符合此标准的provider对象
        String currentProvider = locationManager.getBestProvider(criteria, true);
        Log.d("Location", "currentProvider: " + currentProvider);
        //根据当前provider对象获取最后一次位置信息
        Location currentLocation = locationManager.getLastKnownLocation(currentProvider);
        //如果位置信息为null，则请求更新位置信息
        if(currentLocation == null){
            locationManager.requestLocationUpdates(currentProvider, 0, 0, locationListener);
        }
        //直到获得最后一次位置信息为止，如果未获得最后一次位置信息，则显示默认经纬度
        //每隔10秒获取一次位置信息
        while(true){
            currentLocation = locationManager.getLastKnownLocation(currentProvider);
            if(currentLocation != null){
                Log.d("Location", "Latitude: " + currentLocation.getLatitude());
                Log.d("Location", "location: " + currentLocation.getLongitude());
                break;
            }else{
                Log.d("Location", "Latitude: " + 0);
                Log.d("Location", "location: " + 0);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                 Log.e("Location", e.getMessage());
            }
        }
        
        //解析地址并发送请求
        try {
            double latitude = (double) currentLocation.getLatitude();
            double longitude = (double) currentLocation.getLongitude();
//            Toast.makeText(ShakeActivity.this, "精度："+longitude+"\n纬度："+latitude, Toast.LENGTH_LONG).show();
            SharedPreferences settings = getSharedPreferences("BindPre", MODE_PRIVATE);
            HashMap<String, String> param = new HashMap<String, String>();
			
			param.put("token", settings.getString("token", "default"));
			param.put("latitude", Double.toString(latitude));
			param.put("longitude", Double.toString(longitude));
			
			String json = Client.getResponse(param, "shake");
			Log.d("shake json", json);
			
			String result = "";
			try {
				JSONObject jsonObj = new JSONObject(json);
				result = jsonObj.getString("result");
				if (result.equals("sucess")){
					JSONObject nearby = jsonObj.getJSONObject("nearby");
					String name = nearby.getString("name");
//					int id = nearby.getInt("id");
					Toast.makeText(ShakeActivity.this, name, Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
        } catch (Exception e) {
            Toast.makeText(ShakeActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
        }
	}
	
	
	//创建位置监听器
    private LocationListener locationListener = new LocationListener(){
        //位置发生改变时调用
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Location", "onLocationChanged");
            Log.d("Location", "onLocationChanged Latitude" + location.getLatitude());
            Log.d("Location", "onLocationChanged location" + location.getLongitude());
        }

        //provider失效时调用
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Location", "onProviderDisabled");
        }

        //provider启用时调用
        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Location", "onProviderEnabled");
        }

        //状态改变时调用
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Location", "onStatusChanged");
        }
    };

}