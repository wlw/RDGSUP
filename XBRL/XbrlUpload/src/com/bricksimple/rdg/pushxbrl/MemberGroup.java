package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class MemberGroup {
    private ArrayList<Integer> members = new ArrayList<Integer>();
    
    public void AddMember(Integer iValue) {
    	members.add(iValue);
    }
    
    public ArrayList<Integer> GetMembers() {
    	return(members);
    }
}
