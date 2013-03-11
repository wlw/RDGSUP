package com.bricksimple.rdg.ExtractedClasses;

public class LineItem {
    private String ItemStr = "";
    private boolean IsParenthetical = false;
    
    public void SetItemStr(String iValue) {
    	ItemStr = iValue;
    }
    
    public String GetItemStr() {
    	return(ItemStr);
    }
    
    public void SetIsParenthetical(boolean iValue) {
    	IsParenthetical = iValue;
    }
    
    public boolean GetIsParenthetical() {
    	return(IsParenthetical);
    }
}

