package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class RowDef {

	private ArrayList<Integer> Groups = new ArrayList<Integer>();
	private int                FieldsRowStrUid = 0;
	private int                MemberGroup = 0;
	
	public void AddGroup(int iValue) {
		Groups.add(iValue);
	}
	
	
	public ArrayList<Integer> GetGroups() {
		return(Groups);
	}
	
	public void SetFieldsRowStr(int iValue) {
		FieldsRowStrUid = iValue;
	}
	
	public int GetFieldsRowStr() {
		return(FieldsRowStrUid);
	}
	
	public void SetMemberGroup(int iValue) {
		MemberGroup = iValue;
	}
	
	public int GetMemberGroup() {
		return(MemberGroup);
	}
}
