package com.android.coala;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.coala.database.CoalaDatabase;

public class MembersInGroupActivity extends ListActivity implements View.OnClickListener {
//	private ArrayList<Member> members;
	private ArrayList<ItemRow> items;
	
	private int groupId;
	private MemberListAdapter memberListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_in_group);
		
		groupId = getIntent().getIntExtra("groupId", 0);
		CoalaDatabase database = new CoalaDatabase(this);
		items = database.getMembersInGroup2(groupId);
		database.close();
		
		memberListAdapter = new MemberListAdapter(this);
		setListAdapter(memberListAdapter);
		
		findViewById(R.id.open_add_member_group_button).setOnClickListener(this);
		findViewById(R.id.open_delete_member_group_button).setOnClickListener(this);
		findViewById(R.id.open_edit_group_button).setOnClickListener(this);
	}
	
	private class MemberListAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;
		
		public MemberListAdapter(Context context) {
			this.context = context;
			inflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return items.size();
		}

		public ItemRow getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		// 리스트 목록항목이 보여질때 마다 실행.
		public View getView(int position, View convertView, ViewGroup parent) {
			ItemRow item = getItem(position);
			if (item instanceof MemberItemRow) {
				return getMemberView(convertView, ((MemberItemRow)item).getMember());
			} else {
				return getGroupNameView(convertView, ((GroupNameItemRow)item).getGroup());
			}
		}
		
		private View getGroupNameView(View convertView, Group group) {
			convertView = inflater.inflate(R.layout.group_name_item_row, null);
			((TextView)convertView.findViewById(R.id.group_caption)).setText(group.getName() + "(" + group.getMemberCount() + ")");
			return convertView;
		}
		
		
		private View getMemberView(View convertView, Member member) {
			convertView = inflater.inflate(R.layout.member_list_item_row, null);
			
			LinearLayout l = (LinearLayout)convertView.findViewById(R.id.member_list_item_row_linear);
			TextView memberName = (TextView)convertView.findViewById(R.id.member_name);
			TextView day = (TextView)convertView.findViewById(R.id.day_text);
			TextView memberLastContact = (TextView)convertView.findViewById(R.id.member_last_contact);
			
			if (member.getContactRemainDays() < 0) {
				l.setBackgroundResource(R.drawable.member_row_bg);
				day.setTextColor(Color.GRAY);
				memberLastContact.setTextColor(Color.GRAY);
			} else {
				l.setBackgroundResource(R.drawable.member_need_contact_row_bg);
				day.setTextColor(Color.WHITE);
				memberLastContact.setTextColor(Color.WHITE);
			}
			
			memberName.setText(member.getName());
			day.setText("" + Math.abs(member.getContactRemainDays()));
			memberLastContact.setText(member.getLastContactDate());
			
			ImageButton callButton = (ImageButton) convertView.findViewById(R.id.call_button);
			callButton.setVisibility(View.VISIBLE);
			callButton.setTag(member);
			
			callButton.setOnClickListener(new Button.OnClickListener() {
				public void onClick(View v) {
						Member member = (Member)v.getTag();
						CoalaDatabase database = new CoalaDatabase(MembersInGroupActivity.this);
						database.updateLastContactDate(member.getId());
						
						//전화번호를 바로 가져와서 update 한다.
						Contact contact = getContact(member.getContactId());
						
						if (contact == null) {
							//해당 사용자를 연락처에서 찾을 수 없는 경우 어떻게 할것인가?
						}
						
						if ( ! member.getName().equals(contact.getName())) {
							database.updateMemberName(member.getId(), contact.getName());
						}
						
						database.close();
						
						memberListAdapter.updateMembers();
						
						Intent call = new Intent(Intent.ACTION_CALL);
						call.setData(Uri.parse("tel:" + contact.getPhoneNumber()));
						startActivity(call);
					}
				});
			
			return convertView;
		}
		
		protected void updateMembers() {
			CoalaDatabase database = new CoalaDatabase(context);
			items = database.getMembersInGroup2(groupId);
			database.close();
			notifyDataSetChanged();
		}

		private class ViewHolder {
			private TextView memberName;
			private TextView day;
			private TextView memberLastContact;
			private ImageButton callButton;
		}
	}

	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.open_add_member_group_button:
			intent = new Intent(this, NewMemberListActivity.class);
			intent.putExtra("groupId", groupId);
			startActivityForResult(intent, Consts.NEW_MEMBER_REQUEST_CODE);
			break;
		case R.id.open_delete_member_group_button:
			intent = new Intent(this, MemberDeleteActivity.class);
			intent.putExtra("groupId", groupId);
			startActivityForResult(intent, Consts.REMOVE_REQUEST_CODE);
			break;
		case R.id.open_edit_group_button:
			intent = new Intent(this, EditGroupActivity.class);
			intent.putExtra("groupId", groupId);
			startActivityForResult(intent, Consts.REMOVE_REQUEST_CODE);
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			CoalaDatabase database = new CoalaDatabase(this);
			items = database.getMembersInGroup2(groupId);
			database.close();
			memberListAdapter.notifyDataSetChanged();
		}
	}
	
	private Contact getContact(int contactId) {
		//Contact 사용자가 삭제되었을 경우는?
		String [] projection = new String[] {
			ContactsContract.Contacts._ID,
			ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.Contacts.HAS_PHONE_NUMBER
		};
		
		String selection = ContactsContract.Contacts._ID + "=" + contactId;
		Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, null);
		if (cursor == null) {
			return null;
		}
		
		Contact contact = null;
		if (cursor.moveToNext()) {
			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String phoneNumber = getPhoneNumber(contactId);
			contact = new Contact(contactId, contactName, phoneNumber);
		}
		return contact;
	}
	
	private String getPhoneNumber(int contactId) {
		String phoneNumber = "";
		Cursor phoneCursor = getPhoneCursor(contactId);
		if (phoneCursor != null) {
			if (phoneCursor.moveToNext()) {
				phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			}
			phoneCursor.close();
		}
		return phoneNumber;
	}
	
	private Cursor getPhoneCursor(int contactId) {
		String [] projection = new String[] {
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
				ContactsContract.CommonDataKinds.Phone.NUMBER
		};
		return managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
	}
}