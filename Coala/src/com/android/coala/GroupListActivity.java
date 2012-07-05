package com.android.coala;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.coala.database.HerbDatabase;

public class GroupListActivity extends ListActivity implements View.OnClickListener {
	
	private GroupListAdapter groupListAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_list);
		
		setupGroupListAdapter();
		setupButtons();
	}
	
	private void setupGroupListAdapter() {
		groupListAdapter = new GroupListAdapter(this);
		setListAdapter(groupListAdapter);
	}
	
	private void setupButtons() {
		findViewById(R.id.new_group_dialog_button).setOnClickListener(this);
		findViewById(R.id.delete_group_dialog_button).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		groupListAdapter.update();
		findViewById(R.id.delete_group_dialog_button).setEnabled(groupListAdapter.hasGroups());
	}

	private class GroupListAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<Group> groups = null;
		private LayoutInflater inflater;
		
		public GroupListAdapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
			update();
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
				convertView = inflater.inflate(R.layout.group_list_item_row, null);
				
				holder = new ViewHolder();
				holder.groupName = (TextView)convertView.findViewById(R.id.group_name);
				holder.groupDay = (ImageView)convertView.findViewById(R.id.group_day_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			Group group = getItem(position);
			holder.groupName.setText(group.getName() + " (" + group.getMemberCount() + "명)");
			
			int id = getResources().getIdentifier("day" + group.getAlarmCycle(), "drawable", getPackageName());
			holder.groupDay.setImageResource(id);
			return convertView;
		}
		
		private class ViewHolder {
			private TextView groupName;
			private ImageView groupDay;
		}
		
		private void update() {
			HerbDatabase database = new HerbDatabase(context);
			groups = database.getGroups();
			database.close();
			notifyDataSetChanged();
		}
		
		private boolean hasGroups() {
			return getCount() > 0;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		openGroupActivity(groupListAdapter.getItem(position));
	}
	
	private void openGroupActivity(Group group) {
		Intent intent = new Intent(this, MembersInGroupActivity.class);
		intent.putExtra("groupId", group.getId());
		startActivity(intent);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.new_group_dialog_button:
			startActivity(new Intent(this, NewGroupActivity.class));
			break;
		case R.id.delete_group_dialog_button:
			startActivity(new Intent(this, GroupDeleteActivity.class));
			break;
		}
	}
}