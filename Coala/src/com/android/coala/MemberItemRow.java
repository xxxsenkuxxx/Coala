package com.android.coala;

public class MemberItemRow extends ItemRow {
	private Member member;
	
	public MemberItemRow(Member member) {
		this.member = member;
	}
	
	public Member getMember() {
		return member;
	}
}
