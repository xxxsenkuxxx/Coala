package com.android.coala;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.coala.database.HerbDatabase;

public class MemberDeleteActivity extends ListActivity implements View.OnClickListener {
	
	private ArrayList<Member> members;
	private MemberListAdapter memberListAdapter;
	private ImageButton buttonDeleteMember = null;
	private CheckBox checkBoxSelectAllMembers = null;
	private HerbDatabase database = null;
	private int groupId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setContentView(R.layout.member_delete);
		
		groupId = getIntent().getIntExtra("groupId", 0);
		
		init();
		setupWidgets();
	}

	private void init() {
		database = new HerbDatabase(this);
		members = database.getMembersInGroup(groupId);
	}
	
	private void setupWidgets() {
		memberListAdapter = new MemberListAdapter(this);
		setListAdapter(memberListAdapter);
		
		buttonDeleteMember = (ImageButton)findViewById(R.id.buttonDeleteMember);
		buttonDeleteMember.setOnClickListener(this);
		
		((ImageButton)findViewById(R.id.buttonDeleteMemberCancel)).setOnClickListener(this);
		
		checkBoxSelectAllMembers = (CheckBox)findViewById(R.id.checkBoxMemberCheckAll);
		if (members.size() > 0) {
			checkBoxSelectAllMembers.setOnClickListener(this);
			checkBoxSelectAllMembers.setEnabled(true);
		}
	}

	private class MemberListAdapter extends BaseAdapter {
		private Context context;
		private boolean [] isSelectedMembers;
		
		public MemberListAdapter(Context context) {
			this.context = context;
			isSelectedMembers = new boolean[members.size()];
		}
		
		public int getCount() {
			return members.size();
		}

		public Member getItem(int position) {
			return members.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.member_item_row, null);
				
				holder = new ViewHolder();
				holder.memberName = (TextView) convertView.findViewById(R.id.member_name);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.member_checkbox);
				holder.checkbox.setVisibility(View.VISIBLE);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Member member = members.get(position);
			holder.memberName.setText(member.getName()); 
			holder.checkbox.setChecked(isSelectedMembers[position]);
			return convertView;
		}
		
		private class ViewHolder {
			private TextView memberName = null;
			private CheckBox checkbox = null;
		}

		private ArrayList<Member> getSelectedMembers() {
			ArrayList<Member> checkedMembers = new ArrayList<Member>();
			for (int i=0; i < isSelectedMembers.length; i++) {
				if (isSelectedMembers[i]) {
					checkedMembers.add(members.get(i));
				}
			}
			return checkedMembers; 
		}
		
		private void selectAllMember(boolean selected) {
			for (int i=0; i < isSelectedMembers.length; i++) {
				isSelectedMembers[i] = selected;
			}
			notifyDataSetChanged();
		}
		
		private void toggleSelected(int position) {
			isSelectedMembers[position] = !isSelectedMembers[position];
			notifyDataSetChanged();
		}
		
		private boolean hasSelectedMember() {
			for (int i=0; i < isSelectedMembers.length; i++) {
				if (isSelectedMembers[i]) {
					return true;
				}
			}
			return false;
		}
		
		private boolean selectedAllMember() {
			for (int i=0; i < isSelectedMembers.length; i++) {
				if ( ! isSelectedMembers[i]) {
					return false;
				}
			}
			return true;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		memberListAdapter.toggleSelected(position);
		checkBoxSelectAllMembers.setChecked(memberListAdapter.selectedAllMember());
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonDeleteMember:
			
			if ( ! memberListAdapter.hasSelectedMember()) {
				Toast.makeText(this, R.string.need_member_selection, Toast.LENGTH_SHORT).show();
				return;
			}
			//TODO 
			
			new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete)
				.setMessage(R.string.delete_warning_message)
				.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						deleteSelectedMembers();
					}
				})
				.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			break;
		case R.id.buttonDeleteMemberCancel:
			finish();
		case R.id.checkBoxMemberCheckAll:
			memberListAdapter.selectAllMember(checkBoxSelectAllMembers.isChecked());
			buttonDeleteMember.setEnabled(checkBoxSelectAllMembers.isChecked());
			break;
		}
	}
	
	private void deleteSelectedMembers() {
		MemberDeleteJob memberDeleteJob = new MemberDeleteJob(this);
		memberDeleteJob.execute(memberListAdapter.getSelectedMembers());
	}

	private class MemberDeleteJob extends AsyncTask<ArrayList<Member>, Void, Boolean> {
		
		private Context context;
		private ProgressDialog progressDialog = null;
		
		public MemberDeleteJob(Context context) {
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(context);
			progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getString(R.string.deleting));
			progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(ArrayList<Member>... members) {
			return database.deleteMembers(members[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result) {
				setResult(RESULT_OK);
				finish();
			} else {
				Toast.makeText(context, R.string.err_deleting, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}
}