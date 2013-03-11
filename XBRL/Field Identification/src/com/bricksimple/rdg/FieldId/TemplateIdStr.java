package com.bricksimple.rdg.FieldId;
import java.util.ArrayList;


public class TemplateIdStr {
    private int       Uid;
    private String    TemplateStr;
    private int       StartType;
    private int       TermType;
    private String    TermStr;
    private int       TemplateId;
    public  ArrayList<String> Al;
    
    public  TemplateIdStr(int InUid, String TempStr, int iTermType,
    		                  String InTermStr, int InTemplateId) {
    	ConfidenceLevel confidenceLevel = new ConfidenceLevel();
    	
		Uid = InUid;
    	TemplateStr = TempStr;
    	TermType = iTermType;
    	TermStr = InTermStr;
    	TemplateId = InTemplateId;
    	Al = confidenceLevel.RecurringSting(TempStr);
    }
    
    public int getUid() {
        return( Uid);
    }
    
    public void setUid(int InUid) {
    	Uid = InUid;
    }
    
    public String getTempStr() {
        return( TemplateStr);
    }
    
    public void setTempStr(String InTemplateStr) {
    	TemplateStr= InTemplateStr;
    }
    
    public int getStartType() {
        return( StartType);
    }
    
    public void setStartType(int InStartType) {
    	StartType = InStartType;
    }

    public int getTermType() {
        return( TermType);
    }
    
    public void setTermType(int InTermType) {
    	TermType = InTermType;
    }

    public String getTermStr() {
        return( TermStr);
    }
    
    public void setTermStr(String InTermStr) {
    	TermStr= InTermStr;
    }
    
    public int getTemplateId() {
        return( TemplateId);
    }
    
    public void setTemplateId(int InTemplateId) {
    	TemplateId = InTemplateId;
    }

    public ArrayList<String> getAl() {
        return( Al);
    }
    
    public void setAl(String InTemplateStr) {
    	ConfidenceLevel confidenceLevel = new ConfidenceLevel();
		
    	Al = confidenceLevel.RecurringSting(InTemplateStr);
    }

}
