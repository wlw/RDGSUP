package com.bricksimpe.rdg.XbrlTemplateId;

public class TemplateStrs {
    private int  StrId;
    private String TemplateStr;
    
    void TempateStrs (int NewId, String NewStr) {
    	StrId = NewId;
    	TemplateStr = NewStr;
    }
    
    public int getId() {
        return( StrId);
    }
    
    public void setId(int NewId) {
    	StrId = NewId;
    }
    
    public String getTemplateStr() {
    	return TemplateStr;
    }
    
    public void setTemplateStr(String NewStr) {
    	TemplateStr = NewStr;
    }
    
}
