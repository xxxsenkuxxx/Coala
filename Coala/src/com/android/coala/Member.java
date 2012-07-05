package com.android.coala;

public class Member {
	
	private int id = 0;
	private String name = "";
	private int groupId = 0;
	private int contactId;
	private int contactRemainDays = 0;
	private String lastContactDate = "";
	
	public Member(int id, String name) {
		this.id = id; 
		this.name = name;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public void setContactId(int contactId) {
		this.contactId = contactId;
	}

	public int getContactId() {
		return contactId;
	}

	public int getContactRemainDays() {
		return contactRemainDays;
	}
	
	public void setContactRemainDays(int days) {
		contactRemainDays = days;
	}
	
	public void setLastContactDate(String date) {
		lastContactDate = date;
	}
	
	public String getLastContactDate() {
		return lastContactDate;
	}
}
