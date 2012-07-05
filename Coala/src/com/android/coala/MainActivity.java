package com.android.coala;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		findViewById(R.id.open_group_list_button).setOnClickListener(this);
		findViewById(R.id.open_member_list_button).setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.open_group_list_button:
			startActivity(new Intent(MainActivity.this, GroupListActivity.class));
			break;
		case R.id.open_member_list_button:
			startActivity(new Intent(MainActivity.this, MemberListActivity.class));
			break;
		}
	}
}
