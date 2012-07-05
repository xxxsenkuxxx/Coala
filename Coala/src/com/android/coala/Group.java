package com.android.coala;

public class Group {
	private int id;
	private String name;
	private int memberCount;
	private int alarmCycle;
	
	public Group(int id, String name) {
		this(id, name, 0);
	}
	
	public static Group getNewGroup(String name, int alarmCycle) {
		return new Group(name, alarmCycle);
	}
	
	private Group(String name, int alarmCycle) {
		this.name = name;
		this.alarmCycle = alarmCycle;
	}
	
	public Group(int id, String name, int member_count) {
		this.id = id;
		this.name = name;
		this.memberCount = member_count;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public int getMemberCount() {
		return memberCount;
	}
	
	public void setMemberCount(int count) {
		memberCount = count;
	}
	
	public void setAlarmCycle(int cycle) {
		alarmCycle = cycle;
	}
	
	public int getAlarmCycle() {
		return alarmCycle;
	}
}
