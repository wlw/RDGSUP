package com.bricksimple.rdg.FieldId;

public class HtmlReplacement {
    private String htmlStr;
    private String replacementStr;
    
    public void SetRec(String hValue, String Value) {
    	htmlStr = hValue;
    	replacementStr = Value;
    }
    
    public void SetHtmlStr(String value) {
    	htmlStr = value;
    }
    
    public String GetHtmlStr() {
    	return(htmlStr);
    }
    
    public void SetReplacementStr(String value) {
    	replacementStr = value;
    }
    
    public String GetReplacementStr() {
    	return(replacementStr);
    }
    
    public String ReplaceStr(String origStr) {
    	String rtnStr;
    	
    	rtnStr = origStr.replace(htmlStr, replacementStr);
    	return(rtnStr);
    }
}
