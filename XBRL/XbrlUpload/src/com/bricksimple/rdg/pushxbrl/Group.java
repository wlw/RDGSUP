package com.bricksimple.rdg.pushxbrl;

public class Group {
    private int AxisUid;
    private int MemberUid;
    private String Axis;
    private String Member;
    
    public void SetAxisUid(int iValue) {
    	AxisUid = iValue;
    }
    
    public int GetAxisUid() {
    	return(AxisUid);
    }
    
    public void SetMemberUid(int iValue) {
    	MemberUid = iValue;
    }
    
    public int GetMemberUid() {
    	return(MemberUid);
    }
    
    public void SetAxis(String iValue) {
    	Axis = iValue;
    }
    
    public String GetAxis() {
    	return(Axis);
    }
    
    public void SetMember(String iValue) {
    	Member = iValue;
    }
    
    public String GetMember() {
    	return(Member);
    }
    
    public boolean IsThisMyGroup(String AxisStr, String MemberStr) {
    	boolean bRtn = false;
    	
    	if((AxisStr.equals(Axis)) && (MemberStr.equals(Member)))
    		bRtn = true;
    	return(bRtn);
    }
}
