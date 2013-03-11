package com.bricksimple.rdg.xbrlUpload;

public class RoleGaapXref {
	private String  Text;
    private int     Uid;
    private boolean Negated;
    
    public void SetText(String iValue) {
    	Text = iValue;
    }
    
    public String GetText() {
    	return(Text);
    }
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }
    
    public void SetNegated(Boolean bValue) {
    	Negated = bValue;
    }
    
    public boolean GetNegated() {
    	return(Negated);
    }
}
