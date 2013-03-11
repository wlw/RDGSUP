package com.bricksimple.rdg.FieldId;

public class NormalizeStrRtn {
    private String  rtnStr;
    private boolean bAddedParen = false;
    
    public void SetRtnStr(String strValue) {
    	rtnStr = strValue;
    }
    
    public String GetRtnStr() {
    	return(rtnStr);
    }
    
    public void SetAddedParen(boolean bValue) {
    	bAddedParen = bValue;
    }
    
    public boolean GetAddedParen() {
    	return(bAddedParen);
    }
}
