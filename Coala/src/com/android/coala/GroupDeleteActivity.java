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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.coala.database.CoalaDatabase;

public class GroupDeleteActivity extends ListActivity implements View.OnClickListener {

	private ArrayList<Group> groups = null;
	private GroupListAdapter groupListAdapter;
	private CoalaDatabase database;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setContentView(R.layout.group_delete);
		
		init();
		
		groupListAdapter = new GroupListAdapter(this);
		setListAdapter(groupListAdapter);
		
		findViewById(R.id.delete_group_button).setOnClickListener(this);
		findViewById(R.id.cancel_delete_group_button).setOnClickListener(this);
	}
	
	private void init() {
		database = new CoalaDatabase(this);
		groups = database.getGroups();
	}

	private class GroupListAdapter extends BaseAdapter {
		private Context context;
		private boolean [] isSelectedGroups;
		public GroupListAdapter(Context context) {
			this.context = context;
			isSelectedGroups = new boolean[groups.size()];
		}

		public int getCount() {
			return groups.size();
		}

		public Group getItem(int position) {
			return groups.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.group_item_row, null);
				
				holder = new ViewHolder();
				holder.groupName = (TextView)convertView.findViewById(R.id.group_name);
				holder.checkBox = (CheckBox)convertView.findViewById(R.id.group_select_checkbox);
				holder.checkBox.setVisibility(View.VISIBLE);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			Group group = groups.get(position);
			holder.groupName.setText(group.getName() + " (" + group.getMemberCount() + "명)");
			holder.checkBox.setChecked(isSelectedGroups[position]);
			return convertView;
		}
		
		private class ViewHolder {
			private TextView groupName = null;
			private CheckBox checkBox = null;
		}

		private void toggleSelection(int position) {
			isSelectedGroups[position] = ! isSelectedGroups[position];
			notifyDataSetChanged();
		}
		
		private ArrayList<Group> getSelectedGroups() {
			ArrayList<Group> selectedGroups = new ArrayList<Group>();
			for (int i=0; i < isSelectedGroups.length; i++) {
				if (isSelectedGroups[i]) {
					selectedGroups.add(groups.get(i));
				}
			}
			return selectedGroups;
		}
		
		private boolean hasSelectedGroup() {
			for (int i=0; i < isSelectedGroups.length; i++) {
				if (isSelectedGroups[i]) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		groupListAdapter.toggleSelection(position);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delete_group_button:
			
			if ( ! groupListAdapter.hasSelectedGroup()) {
				Toast.makeText(this, R.string.need_group_selection, Toast.LENGTH_SHORT).show();
				return;
			}
			
			new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.delete)
				.setMessage(R.string.delete_warning_message)
				.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						deleteSelectedGroups();
					}
				})
				.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			break;
		case R.id.cancel_delete_group_button:
			finish();
			break;
		}
	}
	
	private void deleteSelectedGroups() {
		GroupDeleteJob groupDeleteJob = new GroupDeleteJob(this);
		groupDeleteJob.execute(groupListAdapter.getSelectedGroups());
	}
	
	private class GroupDeleteJob extends AsyncTask<ArrayList<Group>, Void, Boolean> {
		
		private Context context;
		private ProgressDialog progressDialog = null;
		
		public GroupDeleteJob(Context context) {
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
		protected Boolean doInBackground(ArrayList<Group>... groups) {
			return database.deleteGroups(groups[0]);
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
