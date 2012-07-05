package com.android.coala.database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.coala.Contact;
import com.android.coala.Group;
import com.android.coala.GroupNameItemRow;
import com.android.coala.ItemRow;
import com.android.coala.Member;
import com.android.coala.MemberItemRow;

public class HerbDatabase {
	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;
	
	private static final String MEMBER_TABLE_NAME = "members";
	private static final String MEMBER_ID_COL = "_id";
	private static final String MEMBER_CONTACT_ID_COL = "contact_id";
	private static final String MEMBER_GROUP_ID_COL = "group_id";
	private static final String MEMBER_NAME_COL = "name";
	private static final String MEMBER_ALARM_CYCLE_COL = "alarm_cycle";
	private static final String MEMBER_LAST_CONTACT_DATE_COL = "last_contact_date";
	private static final String[] MEMBER_COLUMNS = {
		MEMBER_ID_COL, MEMBER_CONTACT_ID_COL, MEMBER_GROUP_ID_COL, MEMBER_NAME_COL, MEMBER_ALARM_CYCLE_COL, MEMBER_LAST_CONTACT_DATE_COL};
	
	private static final String GROUP_TABLE_NAME = "groups";
	private static final String GROUP_ID_COL = "_id";
	private static final String GROUP_NAME_COL = "name";
	private static final String GROUP_ALARM_CYCLE_COL = "alarm_cycle";
	private static final String[] GROUP_COLUMNS = {
		GROUP_ID_COL, GROUP_NAME_COL, GROUP_ALARM_CYCLE_COL};
	
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase db;
	
	public HerbDatabase(Context context) {
		databaseHelper = new DatabaseHelper(context);
		db = databaseHelper.getWritableDatabase();
	}
	
	public void close() {
		if (db != null && db.isOpen()) {
			db.close();
			db = null;
		}
		
		databaseHelper.close();
		databaseHelper = null;
	}
	
	public ArrayList<Member> getMembers() {
		ArrayList<Member> members = new ArrayList<Member>();
		
		Cursor cursor = db.query(MEMBER_TABLE_NAME, MEMBER_COLUMNS, null, null, null, null, null);
		if (cursor == null) {
			return members;
		}
		
		while (cursor.moveToNext()) {
			Member member = new Member(
					cursor.getInt(cursor.getColumnIndex(MEMBER_ID_COL)), 
					cursor.getString(cursor.getColumnIndex(MEMBER_NAME_COL)));
			member.setContactId(cursor.getInt(cursor.getColumnIndex(MEMBER_CONTACT_ID_COL)));
			members.add(member);
		}
		return members;
	}
	
	// 그룹안의 멤버수 추가해야 한다.
	public ArrayList<ItemRow> getMembers2() {
		ArrayList<ItemRow> items = new ArrayList<ItemRow>();
		
		String sql = "SELECT m.*, ((julianday(date('now')) - julianday(m.last_contact_date)) - m.alarm_cycle) AS contact_remain_days, gm._id AS group_id, gm.name AS group_name, gm.member_count AS member_count FROM members m JOIN (SELECT g._id, g.name, mc.member_count FROM groups g JOIN " + 
				"(SELECT group_id, COUNT(*) AS member_count FROM members GROUP BY group_id) AS mc " +
				"ON g._id=mc.group_id) AS gm ON m.group_id=gm._id " + 
				"ORDER BY gm.name ASC, m.name ASC";
		
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
			return items;
		}
		
		String groupName = "";
		
		while (cursor.moveToNext()) {
			int idColIndex = cursor.getColumnIndex(MEMBER_ID_COL);
			int nameColIndex = cursor.getColumnIndex(MEMBER_NAME_COL);
			int lastContactDateColIndex = cursor.getColumnIndex(MEMBER_LAST_CONTACT_DATE_COL);
			int contactRemainDaysColIndex = cursor.getColumnIndex("contact_remain_days");
			int contactIdColIndex = cursor.getColumnIndex(MEMBER_CONTACT_ID_COL);
			int groupNameColIndex = cursor.getColumnIndex("group_name");
			int groupIdColIndex = cursor.getColumnIndex("group_id");
			int groupMemberCountColIndex = cursor.getColumnIndex("member_count");
			
			if ( ! groupName.equals(cursor.getString(groupNameColIndex))) {
				groupName = cursor.getString(groupNameColIndex);
				Group group = new Group(cursor.getInt(groupIdColIndex), cursor.getString(groupNameColIndex));
				group.setMemberCount(cursor.getInt(groupMemberCountColIndex));
				items.add(new GroupNameItemRow(group));
			}
			
			Member member = new Member(cursor.getInt(idColIndex), cursor.getString(nameColIndex));
			member.setContactId(cursor.getInt(contactIdColIndex));
			member.setContactRemainDays(cursor.getInt(contactRemainDaysColIndex));
			member.setLastContactDate(cursor.getString(lastContactDateColIndex));
			items.add(new MemberItemRow(member));
		}
		
		return items;
	}
	
	public ArrayList<ItemRow> getMembersInGroup2(int groupId) {
		ArrayList<ItemRow> items = new ArrayList<ItemRow>();
		
		String sql = "SELECT m.*, ((julianday(date('now')) - julianday(m.last_contact_date)) - m.alarm_cycle) AS contact_remain_days, gm._id AS group_id, gm.name AS group_name, gm.member_count AS member_count FROM members m JOIN (SELECT g._id, g.name, mc.member_count FROM groups g JOIN " + 
				"(SELECT group_id, COUNT(*) AS member_count FROM members GROUP BY group_id) AS mc " +
				"ON g._id=mc.group_id) AS gm ON m.group_id=gm._id " + 
				"WHERE m.group_id=" + groupId + " ORDER BY gm.name ASC, m.name ASC";
		
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
			return items;
		}
		
		String groupName = "";
		
		while (cursor.moveToNext()) {
			int idColIndex = cursor.getColumnIndex(MEMBER_ID_COL);
			int nameColIndex = cursor.getColumnIndex(MEMBER_NAME_COL);
			int lastContactDateColIndex = cursor.getColumnIndex(MEMBER_LAST_CONTACT_DATE_COL);
			int contactRemainDaysColIndex = cursor.getColumnIndex("contact_remain_days");
			int contactIdColIndex = cursor.getColumnIndex(MEMBER_CONTACT_ID_COL);
			int groupNameColIndex = cursor.getColumnIndex("group_name");
			int groupIdColIndex = cursor.getColumnIndex("group_id");
			int groupMemberCountColIndex = cursor.getColumnIndex("member_count");
			
			if ( ! groupName.equals(cursor.getString(groupNameColIndex))) {
				groupName = cursor.getString(groupNameColIndex);
				Group group = new Group(cursor.getInt(groupIdColIndex), cursor.getString(groupNameColIndex));
				group.setMemberCount(cursor.getInt(groupMemberCountColIndex));
				items.add(new GroupNameItemRow(group));
			}
			
			Member member = new Member(cursor.getInt(idColIndex), cursor.getString(nameColIndex));
			member.setContactId(cursor.getInt(contactIdColIndex));
			member.setContactRemainDays(cursor.getInt(contactRemainDaysColIndex));
			member.setLastContactDate(cursor.getString(lastContactDateColIndex));
			items.add(new MemberItemRow(member));
		}
		
		return items;
	}
	
	public ArrayList<Member> getMembersInGroup(int groupId) {
		ArrayList<Member> members = new ArrayList<Member>();
		ArrayList<ItemRow> items = new ArrayList<ItemRow>();
		
		String sql = "SELECT *, ((julianday(date('now')) - julianday(last_contact_date)) - alarm_cycle) AS contact_remain_days FROM members WHERE group_id=" + groupId + " ORDER BY name ASC";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
			return members;
		}
		
		while (cursor.moveToNext()) {
			int idColIndex = cursor.getColumnIndex(MEMBER_ID_COL);
			int nameColIndex = cursor.getColumnIndex(MEMBER_NAME_COL);
			int lastContactDateColIndex = cursor.getColumnIndex(MEMBER_LAST_CONTACT_DATE_COL);
			int contactRemainDaysColIndex = cursor.getColumnIndex("contact_remain_days");
			int contactIdCol = cursor.getColumnIndex(MEMBER_CONTACT_ID_COL);
			
			Member member = new Member(cursor.getInt(idColIndex), cursor.getString(nameColIndex));
			member.setContactRemainDays(cursor.getInt(contactRemainDaysColIndex));
			member.setLastContactDate(cursor.getString(lastContactDateColIndex));
			member.setContactId(cursor.getInt(contactIdCol));
			members.add(member);
		}
		return members;
	}
	
	public ArrayList<ItemRow> getMembersOrderByContactPriority() {
		ArrayList<ItemRow> items = new ArrayList<ItemRow>();
		
		String sql = "SELECT m.*, mp.contact_remain_days AS contact_remain_days FROM members m LEFT JOIN (SELECT _id, ((julianday(date('now')) - julianday(last_contact_date)) - alarm_cycle) AS contact_remain_days FROM members) AS mp ON m._id=mp._id WHERE mp.contact_remain_days >= 0 ORDER BY mp.contact_remain_days DESC, m.name ASC";
		Cursor cursor = db.rawQuery(sql, null);
		
		if (cursor == null || cursor.getCount() == 0) {
			return items;
		}
		
		Group group = new Group(0, "연락대기");
		group.setMemberCount(cursor.getCount());
		items.add(new GroupNameItemRow(group));
		
		while (cursor.moveToNext()) {
			int contactRemainDaysColIndex = cursor.getColumnIndex("contact_remain_days");
			int lastContactDateColIndex = cursor.getColumnIndex(MEMBER_LAST_CONTACT_DATE_COL);
			
			Member member = new Member(
					cursor.getInt(cursor.getColumnIndex(MEMBER_ID_COL)), 
					cursor.getString(cursor.getColumnIndex(MEMBER_NAME_COL))); 
			member.setContactId(cursor.getInt(cursor.getColumnIndex(MEMBER_CONTACT_ID_COL)));
			member.setContactRemainDays(cursor.getInt(contactRemainDaysColIndex));
			member.setLastContactDate(cursor.getString(lastContactDateColIndex));
			items.add(new MemberItemRow(member));
		}
		
		return items;
	}
	
	public boolean existedNeedContactMember() {
		String sql = "SELECT * FROM " + 
				" (SELECT ((julianday(date('now')) - julianday(last_contact_date)) - alarm_cycle) AS priority FROM members) AS m " +
				" WHERE m.priority > 0";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
			return false;
		}
		
		return cursor.getCount() > 0;
	}
	
	public ArrayList<Group> getGroups() {
		ArrayList<Group> groups = new ArrayList<Group>();
		
		String sql = "SELECT g._id, g.name, CASE WHEN m.count IS NULL THEN 0 ELSE m.count END AS member_count, g.alarm_cycle FROM groups g LEFT JOIN (" + 
				"SELECT group_id, COUNT(*) AS COUNT FROM members GROUP BY group_id) m ON g._id=m.group_id ORDER BY g.name ASC";
		
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor == null) {
			return groups;
		}
		
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndex("_id"));
			String name = cursor.getString(cursor.getColumnIndex("name"));
			int memberCount = cursor.getInt(cursor.getColumnIndex("member_count"));
			int alarmCycle = cursor.getInt(cursor.getColumnIndex(GROUP_ALARM_CYCLE_COL));
			
			Group group = new Group(id, name, memberCount);
			group.setAlarmCycle(alarmCycle);
			groups.add(group);
		}
		return groups;
	}
	
	public void addGroup(Group group) throws GroupNameEmptyException, GroupNameUsedException {
		if (group.getName().trim().equals("")) {
			throw new GroupNameEmptyException();
		} else if (existedGroupName(group.getName())) {
			throw new GroupNameUsedException();
		}
		
		ContentValues values = new ContentValues();
		values.put(GROUP_NAME_COL, group.getName().trim());
		values.put(GROUP_ALARM_CYCLE_COL, group.getAlarmCycle());
		db.insert(GROUP_TABLE_NAME, null, values);
	}
	
	public boolean existedGroupName(String groupName) {
		Cursor cursor = db.rawQuery("SELECT * FROM " + GROUP_TABLE_NAME + " WHERE name=?", new String[] {groupName.trim()});
		if ( cursor.getCount() > 0 ) {
			return true;
		}
		return false;
	}
	
	public boolean saveGroup(int id, String name, int alarmCycle) {
		ContentValues values = new ContentValues();
		values.put(GROUP_NAME_COL, name.trim());
		values.put(GROUP_ALARM_CYCLE_COL, alarmCycle);
		return db.update(GROUP_TABLE_NAME, values, GROUP_ID_COL + "=" + id, null) == 1;
	}
	
	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createMemberTable(db);
			createGroupTable(db);
		}
		
		private void createMemberTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + MEMBER_TABLE_NAME + " (" +
					MEMBER_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					MEMBER_CONTACT_ID_COL + " INTEGER NOT NULL, " +
					MEMBER_GROUP_ID_COL + " INTEGER NOT NULL DEFAULT 0," +
					MEMBER_NAME_COL + " TEXT NOT NULL," +
					MEMBER_ALARM_CYCLE_COL + " INTEGER NOT NULL DEFAULT 7," +
					MEMBER_LAST_CONTACT_DATE_COL + " TEXT NOT NULL DEFAULT '')");
		}

		private void createGroupTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + GROUP_TABLE_NAME + " (" +
					GROUP_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					GROUP_NAME_COL + " TEXT NOT NULL DEFAULT ''," + 
					GROUP_ALARM_CYCLE_COL + " INTEGER NOT NULL DEFAULT 7)");
			db.execSQL("INSERT INTO " + GROUP_TABLE_NAME + " VALUES (1, '부모님', 7)");
			db.execSQL("INSERT INTO " + GROUP_TABLE_NAME + " VALUES (2, '형제', 7)");
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion != newVersion) {
	            dropTables(db);
	            
	            createMemberTable(db);
	            createGroupTable(db);
			}
		}

		private void dropTables(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + MEMBER_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + GROUP_TABLE_NAME);
		}
	}
	
	public boolean deleteMembers(ArrayList<Member> members) {
		for (Member member : members) {
			if ( 1 != db.delete(MEMBER_TABLE_NAME, "_id = " + member.getId(), null)) {
				return false;
			}
		}
		return true;
	}

	public Boolean deleteGroups(ArrayList<Group> groups) {
		db.beginTransaction();
		try {
			for (Group group : groups) {
				db.delete(MEMBER_TABLE_NAME, MEMBER_GROUP_ID_COL + "=" + group.getId(), null);
				db.delete(GROUP_TABLE_NAME, GROUP_ID_COL + "=" + group.getId(), null);
			}
			db.setTransactionSuccessful();
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			db.endTransaction();
		}
	}

	public Boolean addMembers(ArrayList<Contact> contacts, int groupId) {
		if (contacts.size() == 0 || groupId == 0) {
			return false;
		}
		
		Group group = getGroup(groupId);
		if (group == null) {
			return false;
		}
		
		for (Contact contact : contacts) {
			ContentValues values = new ContentValues();
			values.put(MEMBER_CONTACT_ID_COL, contact.getId());
			values.put(MEMBER_GROUP_ID_COL, groupId);
			values.put(MEMBER_NAME_COL, contact.getName());
			values.put(MEMBER_ALARM_CYCLE_COL, group.getAlarmCycle());
			values.put(MEMBER_LAST_CONTACT_DATE_COL, getCurrentDate());
			if (db.insert(MEMBER_TABLE_NAME, null, values) == -1) {
				return false;
			}
		}
		return true;
	}
	
	private String getCurrentDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date());
	}

	public Group getGroup(int groupId) {
		Cursor cursor = db.query(GROUP_TABLE_NAME, GROUP_COLUMNS, GROUP_ID_COL + "=" + groupId, null, null, null, null);
		if (cursor.moveToFirst()) {
			int id = cursor.getInt(cursor.getColumnIndex(GROUP_ID_COL));
			String name = cursor.getString(cursor.getColumnIndex(GROUP_NAME_COL));
			Group group = new Group(id, name);
			group.setAlarmCycle(cursor.getInt(cursor.getColumnIndex(GROUP_ALARM_CYCLE_COL)));
			return group;
		}
		return null;
	}
	
	public void updateLastContactDate(int id) {
		ContentValues values = new ContentValues();
		values.put(MEMBER_LAST_CONTACT_DATE_COL, getCurrentDate());
		db.update(MEMBER_TABLE_NAME, values, MEMBER_ID_COL + "=" + id, null);
	}

	public void updateMemberName(int id, String name) {
		ContentValues values = new ContentValues();
		values.put(MEMBER_NAME_COL, name);
		db.update(MEMBER_TABLE_NAME, values, MEMBER_ID_COL + "=" + id, null);
	}
}
