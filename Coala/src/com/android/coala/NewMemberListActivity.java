package com.android.coala;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.coala.database.HerbDatabase;

public class NewMemberListActivity extends ListActivity implements View.OnClickListener {
	
	private ArrayList<Contact> contacts = null;
	private ContactsAdapter contactAdapter = null;
	private HerbDatabase database = null;
	private int groupId = 0;
	private ArrayList<Member> members;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		
		setContentView(R.layout.new_member_list);
		groupId = getIntent().getIntExtra("groupId", 0);
		
		database = new HerbDatabase(this);
		
		members = database.getMembers();
		
		contacts = getContacts();
		contactAdapter = new ContactsAdapter(this); 
		setListAdapter(contactAdapter);
		
		findViewById(R.id.add_member_group_button).setOnClickListener(this);
		findViewById(R.id.cancel_add_member_group_button).setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		database.close();
	}
	
	private ArrayList<Contact> getContacts() {
		
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		
		Cursor cursor = getContactsCursor();
		if (cursor == null) {
			return contacts;
		}
		
		while (cursor.moveToNext()) {
			int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
			if (idUsedContactId(contactId)) {
				continue;
			}
			String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			String phoneNumber = getPhoneNumber(contactId);
			contacts.add(new Contact(contactId, contactName, phoneNumber));
		}
		cursor.close();
		return contacts;
	}
	
	private boolean idUsedContactId(int contactId) {
		if (members.size() == 0) {
			return false;
		}
		
		for (int i=0; i < members.size(); i++) {
			int id = members.get(i).getContactId();
			if (contactId == id) {
				// 속도 개선을 위해 한번 일치한 member 는 삭제함.
				members.remove(i);
				return true;
			}
		}
		return false;
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

	private Cursor getContactsCursor() {
		String [] projection = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.HAS_PHONE_NUMBER
		};
		
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		
		return managedQuery(ContactsContract.Contacts.CONTENT_URI, projection, selection, null, sortOrder);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		contactAdapter.toggleSelection(position);
		findViewById(R.id.add_member_group_button).setEnabled(contactAdapter.hasSelectedContact());
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == Consts.NEW_MEMBER_REQUEST_CODE) {
				setResult(RESULT_OK);
				finish();
			}
		}
	}

	private class ContactsAdapter extends BaseAdapter {
		private Context context = null;
		private boolean [] isSelectedContacts;
		
		public ContactsAdapter(Context context) {
			this.context = context;
			isSelectedContacts = new boolean[contacts.size()];
		}

		public int getCount() {
			return contacts.size();
		}

		public Contact getItem(int position) {
			return contacts.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.contacts_row, null);
				
				holder = new ViewHolder();
				holder.contactName = (TextView)convertView.findViewById(R.id.contactName);
				holder.checkBox = (CheckBox)convertView.findViewById(R.id.contactCheckbBox);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			
			Contact contact = (Contact)contacts.get(position);
			holder.contactName.setText(contact.getName() + "(" + contact.getPhoneNumber() + ")");
			holder.checkBox.setChecked(isSelectedContacts[position]);
			return convertView;
		}
		
		private class ViewHolder {
			private TextView contactName = null;
			private CheckBox checkBox = null;
		}
		
		private void toggleSelection(int position) {
			isSelectedContacts[position] = ! isSelectedContacts[position];
			notifyDataSetChanged();
		}

		public ArrayList<Contact> getSelectedContacts() {
			ArrayList<Contact> selectedContacts = new ArrayList<Contact>();
			for (int i=0; i < isSelectedContacts.length; i++) {
				if (isSelectedContacts[i]) {
					selectedContacts.add(contacts.get(i));
				}
			}
			return selectedContacts;
		}
		
		public boolean hasSelectedContact() {
			for (int i=0; i < isSelectedContacts.length; i++) {
				if (isSelectedContacts[i]) {
					return true;
				}
			}
			return false;
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_member_group_button:
			
			if ( ! contactAdapter.hasSelectedContact()) {
				Toast.makeText(this, R.string.need_member_selection, Toast.LENGTH_SHORT).show();
				return;
			}
			
			MemberAddJob memberAddJob = new MemberAddJob(this);
			memberAddJob.execute(contactAdapter.getSelectedContacts());
			break;
		case R.id.cancel_add_member_group_button:
			finish();
			break;
		}
	}
	
	private class MemberAddJob extends AsyncTask<ArrayList<Contact>, Void, Boolean> {
		
		private Context context;
		private ProgressDialog progressDialog = null;
		
		public MemberAddJob(Context context) {
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(context);
			progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getString(R.string.adding));
			progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(ArrayList<Contact>... contacts) {
			return database.addMembers(contacts[0], groupId);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result) {
				setResult(RESULT_OK);
				finish();
			} else {
				Toast.makeText(context, R.string.err_adding, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
