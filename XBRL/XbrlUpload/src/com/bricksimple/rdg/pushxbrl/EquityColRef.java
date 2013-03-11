package com.bricksimple.rdg.pushxbrl;

public class EquityColRef {
    private String RefData = "";
    private String FromStr = "";
    private int    RefUid = 0;
    
    public void SetRefData(String iValue) {
    	RefData = iValue;
    }
    
    public String GetRefData() {
    	return(RefData);
    }
    
    public void SetFromStr(String iValue) {
    	FromStr = iValue;
    }
    
    public String GetFromStr() {
    	return(FromStr);
    }
    
    public void SetRefUid(int uid) {
    	RefUid = uid;
    }
    
    public int GetRefUid() {
    	return(RefUid);
    }
}
