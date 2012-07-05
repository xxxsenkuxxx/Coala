package com.android.coala;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

public class NotiAlert extends Activity implements View.OnClickListener {

	NotificationManager noti;

	private ImageButton callBTN, waitBTN;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		setContentView(R.layout.noti_alert);

		noti = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		callBTN = (ImageButton) findViewById(R.id.alert_call);
		callBTN.setOnClickListener(this);
		waitBTN = (ImageButton) findViewById(R.id.alert_wait);
		waitBTN.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.alert_call:
			startActivity(new Intent(this, MemberListActivity.class));
			finish();
			break;
			
		case R.id.alert_wait:
			
			PendingIntent pendingIntent = PendingIntent.getActivity(NotiAlert.this, 0,
					new Intent(NotiAlert.this, MemberListActivity.class), 0);

			Notification notifi = new Notification(R.drawable.noti_icon,
					"전화알림", System.currentTimeMillis());
			notifi.setLatestEventInfo(NotiAlert.this, "Coala", "연락을 기다리고 있어요",
					pendingIntent);
			notifi.flags = Notification.FLAG_AUTO_CANCEL;
			noti.notify(0, notifi);
			finish();

		}
	}
}
