package com.bricksimple.rdg.FieldId;

public class DateRefRec {
    private String CompletedDate;
    private String SupStr;
    
    public void SetCompletedDate(String inStr) {
    	CompletedDate = inStr;
    }
    
    public String GetCompletedDate() {
    	return(CompletedDate);
    }

    public void SetSupStr(String inStr) {
    	SupStr = inStr;
    }
    
    public String GetSupStr() {
    	return(SupStr);
    }
}
