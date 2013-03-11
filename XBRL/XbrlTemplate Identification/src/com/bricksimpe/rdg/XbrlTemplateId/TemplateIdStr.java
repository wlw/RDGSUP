package com.bricksimpe.rdg.XbrlTemplateId;
import java.util.ArrayList;


public class TemplateIdStr {
    private int               Uid;
    private String            TemplateStr;
    private String            LcTemplateStr;
    private int               StartType;
    private int               TermType;
    private String            TermStr;
    private int               TemplateId;
    private int               StrTblId;
    public  ArrayList<String> Al;
    
    public void TemplateIdStrDflt(int InUid, String TempStr, int iTermType,
    		                  String InTermStr, int InTemplateId) {
    	
		ConfidenceLevel cl = new ConfidenceLevel();

		Uid = InUid;
    	TemplateStr = TempStr;
    	LcTemplateStr = TempStr.toLowerCase();
    	TermType = iTermType;
    	TermStr = InTermStr;
    	TemplateId = InTemplateId;
    	Al = cl.RecurringSting(TempStr);
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
    
    public String getLcTempStr() {
    	return(LcTemplateStr);
    }
    
    public void setTempStr(String InTemplateStr) {
    	TemplateStr= InTemplateStr;
    	LcTemplateStr = InTemplateStr.toLowerCase();
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

    public int getStrTblId() {
        return( StrTblId);
    }
    
    public void setStrTblId(int InStrTblId) {
    	StrTblId = InStrTblId;
    }
    
    public ArrayList<String> getAl() {
        return( Al);
    }
    
    public void setAl(String InTemplateStr) {
		ConfidenceLevel cl = new ConfidenceLevel();
		
    	Al = cl.RecurringSting(InTemplateStr);
    }

    public boolean isStringWithin(String testStr) {
    	boolean bRtn = false;
 
    	if(testStr.contains(LcTemplateStr))
    		bRtn = true;
    	return(bRtn);
    }
}
