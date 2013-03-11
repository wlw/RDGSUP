package com.bricksimple.rdg.pushxbrl;

public class UnitUid {
    private int  Uid;
    private String XbrlRefStr;
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }
    
    public void SetXbrlRefStr(String iValue) {
    	XbrlRefStr = iValue;
    }
    
    public String GetXbrlRefStr() {
    	return(XbrlRefStr);
    }
}
