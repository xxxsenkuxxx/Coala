package com.android.coala;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
	
	private static final String TAG = BootReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "BootReceiver - onReceive");
		ComponentName cn = new ComponentName(context.getPackageName(), HerbService.class.getName());
		context.startService(new Intent().setComponent(cn));
		
		Toast.makeText(context, "Herb Family", Toast.LENGTH_LONG).show();
	}
}
