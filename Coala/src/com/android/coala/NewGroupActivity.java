package com.android.coala;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.android.coala.database.GroupNameEmptyException;
import com.android.coala.database.GroupNameUsedException;
import com.android.coala.database.CoalaDatabase;

public class NewGroupActivity extends Activity implements View.OnClickListener {
	private static final String [] ALARM_CYCLE_CAPTIONS = {"7일", "한달", "3달"};
	private static final int [] ALARM_CYCLE_VALUES = {7, 30, 90};
	private int alarmCycle = ALARM_CYCLE_VALUES[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setContentView(R.layout.new_group_dialog);
		setupWidgets();
	}

	private void setupWidgets() {
		findViewById(R.id.alarm_button).setOnClickListener(this);
		findViewById(R.id.save_group_button).setOnClickListener(this);
		findViewById(R.id.cancel_save_group_button).setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.alarm_button:
			new AlertDialog.Builder(this)
				.setTitle(R.string.alarm_cycle)
				.setItems(ALARM_CYCLE_CAPTIONS, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						alarmCycle = ALARM_CYCLE_VALUES[which];
						dialog.dismiss();
					}
				}).show();
			break;
		case R.id.save_group_button:
			String name = ((EditText)findViewById(R.id.editTextGroupName)).getText().toString();
			CoalaDatabase database = new CoalaDatabase(this);
			try {
				database.addGroup(Group.getNewGroup(name, alarmCycle));
				finish();
			} catch (GroupNameEmptyException e) {
				Toast.makeText(this, R.string.input_group_name, Toast.LENGTH_SHORT).show();
			} catch (GroupNameUsedException e) {
				Toast.makeText(this, R.string.used_group_name, Toast.LENGTH_SHORT).show();
			} finally {
				database.close();
			}
			break;
		case R.id.cancel_save_group_button:
			finish();
			break;
		}
	}
}