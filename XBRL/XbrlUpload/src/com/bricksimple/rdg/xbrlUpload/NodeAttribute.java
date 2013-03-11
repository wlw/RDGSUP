package com.bricksimple.rdg.xbrlUpload;

public class NodeAttribute {
    private String Name;
    private String Value;
    
    public void SetName(String iValue) {
    	String normalized = iValue.replace("xlink:", "");
    	Name = normalized;
    }
    
    public String GetName() {
    	return(Name);
    }

    public void SetValue(String iValue) {
    	Value = iValue;
    }
    
    public String GetValue() {
    	return(Value);
    }
}
