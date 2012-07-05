package com.android.coala;

import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.coala.database.CoalaDatabase;

public class CoalaService extends Service implements Runnable {
	
	private static final String TAG = CoalaService.class.getSimpleName();
	private static final long NEXT_DAY = 1000 * 60 * 60 * 24;
	
	private Handler handler = null;
	
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
		
		Log.e(TAG, "Service OnCreate - Coala");
	}
	
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date tomorrow = calendar.getTime();
		
		Date now = new Date();
		long diff = tomorrow.getTime() - now.getTime();
		handler.postDelayed(this, diff);
		
		Log.e(TAG, "Service onStart(" + diff + ") - Coala");
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	 
	public void run() {
		CoalaDatabase database = new CoalaDatabase(this);
		if (database.existedNeedContactMember()) {
			alarmNotification();
		}
		handler.postDelayed(this, NEXT_DAY);
		database.close();
	}
	
	public void alarmNotification() {
		Intent intent = new Intent(this, NotiAlert.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}