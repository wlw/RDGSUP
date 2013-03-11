package com.bricksimple.rdg.pushxbrl;

public class XbrlTemplateXref {
    private String XbrlTag = "";
    private int    TemplateId = 0;
    
    public void SetXbrlTag(String iValue)  {
    	XbrlTag = iValue;
    }
    
    public String GetXbrlTag() {
    	return(XbrlTag);
    }
    
    public void SetTemplateId(int iValue) {
    	TemplateId = iValue;
    }
    
    public int GetTemplateId() {
    	return(TemplateId);
    }
}
