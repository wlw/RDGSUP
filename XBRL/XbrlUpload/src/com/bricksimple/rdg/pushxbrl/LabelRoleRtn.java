package com.bricksimple.rdg.pushxbrl;

public class LabelRoleRtn {
    private String  Label = "";
    private int     RoleId = 0;
    
    public void SetLabel(String iValue) {
    	Label = iValue;
    }
    
    public String GetLabel() {
    	return(Label);
    }
    
    public void SetRoleId(int iValue) {
    	RoleId = iValue;
    }
    
    public int GetRoleId() {
    	return(RoleId);
    }
}
