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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.coala.database.CoalaDatabase;


public class EditGroupActivity extends Activity implements View.OnClickListener {
	
	private static final String TAG = EditGroupActivity.class.getSimpleName();
	
	private static final String [] ALARM_CYCLE_CAPTIONS = {"일주일", "한달", "3달"};
	private static final int [] ALARM_CYCLE_VALUES = {7, 30, 90};
	private int alarmCycle = ALARM_CYCLE_VALUES[0];
	private Group group;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		int groupId = getIntent().getIntExtra("groupId", 0);
		CoalaDatabase database = new CoalaDatabase(this);
		group = database.getGroup(groupId);
		database.close();
		
		setContentView(R.layout.new_group_dialog);
		
		LinearLayout l = (LinearLayout)findViewById(R.id.group_dialog_linearlayout);
		l.setBackgroundResource(R.drawable.group_edit_dialog_bg);
		
		setupWidgets();
	}

	private void setupWidgets() {
		((TextView)findViewById(R.id.editTextGroupName)).setText(group.getName());
		
		findViewById(R.id.alarm_button).setOnClickListener(this);
		findViewById(R.id.save_group_button).setOnClickListener(this);
		ImageButton button = (ImageButton)findViewById(R.id.save_group_button);
		button.setImageResource(R.drawable.dialog_save_button_style);
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
			CoalaDatabase database = new CoalaDatabase(this);
			String name = ((EditText)findViewById(R.id.editTextGroupName)).getText().toString();
			if (name.trim().equals("")) {
				Toast.makeText(this, "그룹 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
			} else if (database.existedGroupName(name) && ! group.getName().equals(name)) {
				Toast.makeText(this, "사용중인 이름입니다.", Toast.LENGTH_SHORT).show();
			} else {
				if (database.saveGroup(group.getId(), name, alarmCycle)) {
					setResult(RESULT_OK);
					finish();
				}
			}
			database.close();
			break;
		case R.id.cancel_save_group_button:
			finish();
			break;
		}
	}
}
