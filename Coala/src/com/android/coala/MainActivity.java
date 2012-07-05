package com.android.coala;

import com.android.coala.database.CoalaDatabase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		findViewById(R.id.open_group_list_button).setOnClickListener(this);
		findViewById(R.id.open_member_list_button).setOnClickListener(this);
		
		//For Testing
//		CoalaDatabase database = new CoalaDatabase(this);
//		database.updateLastContactForTesting();
//		database.close();
//		
//		Intent intent = new Intent(this, NotiAlert.class);
//		startActivity(intent);
		
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
