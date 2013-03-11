package com.bricksimpe.rdg.XbrlTemplateId;

public class HtmlTraceItem {

	private int     BeginLineNum;
	private int     EndLineNum;
	private int     TemplateId;
	private double  ConfidenceLevel;
	private String  TermStr;
	private String  IdentifiedText;
	private boolean UnAudited = false;
	private int     Scale = 1;
	private boolean bNoteWithinTable = false;
	private int     iIdLine = 0;
	private boolean bUserDefined = false;
	private int     iUserStartDefine;
	private int     iUserEndDefine;
	private int     iNoteIndex = 0;
	
void HtmlTraceItem (int Bline, int Eline, int Tid, double Cf, String Term,
		            String IdText) {
	    	BeginLineNum = Bline;
	    	EndLineNum = Eline;
	    	TemplateId = Tid;
	    	ConfidenceLevel = Cf;
	    	TermStr = Term;
	    	IdentifiedText = IdText;
	    	UnAudited = false;
	    	Scale = 1;
	    	bNoteWithinTable = false;
	    }
	    
	    public int getBeginLine() {
	        return( BeginLineNum);
	    }
	    
	    public void setBeginLine(int Bline) {
	    	BeginLineNum = Bline;
	    }
	    
	    public int getEndLine() {
	        return( EndLineNum);
	    }
	    
	    public void setEndLine(int Eline) {
	    	EndLineNum = Eline;
	    }
	    
	    public int getTemplateId() {
	        return( TemplateId);
	    }
	    
	    public void setTemplateId(int Tid) {
	    	TemplateId = Tid;
	    }
	    
	    public double getConfidenceLevel() {
	        return( ConfidenceLevel);
	    }
	    
	    public void setConfidenceLevel(double cl) {
	    	ConfidenceLevel = cl;
	    }
	    
	    public String getTermStr() {
	        return( TermStr);
	    }
	    
	    public void setTermStr(String strTerm) {
	    	TermStr = strTerm;
	    }
	    
	    public void setIdentifiedText(String TextStr) {
	    	IdentifiedText = TextStr;
	    }
	    
	    public String getIdentifiedText() {
	    	return(IdentifiedText);
	    }
	    
	    public void setUnAudited(boolean bUnAudited) {
	    	UnAudited = bUnAudited;
	    }
	    
	    public int getUnAudited() {
	    	int iRtn = 0;
	    	
	    	if(UnAudited == true) 
	    		iRtn = 1;
	    	return(iRtn);
	    }
	    
	    public boolean getBoolUnAudited() {
	    	return(UnAudited);
	    }
	    
	    public void setScale(int iScale) {
	    	Scale = iScale;
	    }
	    
	    public int getScale() {
	    	return(Scale);
	    }
	    
	    public void setNoteWithinTable(boolean bValue) {
	    	bNoteWithinTable = bValue;
	    }
	    
	    public boolean getNoteWithinTable() {
	    	return(bNoteWithinTable);
	    }
	    
	    public void setIdLine(int iValue) {
	    	iIdLine = iValue;
	    }
	    
	    public int getIdLine() {
	    	return(iIdLine);
	    }
	    
	    public void setUserStart(int iValue) {
	    	iUserStartDefine = iValue;
	    }
	    
	    public int getUserStart() {
	    	return(iUserStartDefine);
	    }
	    
	    public void setUserEnd(int iValue) {
	    	iUserEndDefine = iValue;
	    }
	    
	    public int getUserEnd() {
	    	return(iUserEndDefine);
	    }
	    
	    public void setUserDefined(boolean bValue) {
	    	bUserDefined = bValue;
	    }
	    
	    public boolean getUserDefined() {
	    	return(bUserDefined);
	    }

	    public void setNoteIndex(int iValue) {
	    	iNoteIndex = iValue;
	    }
	    
	    public int getNoteIndex() {
	    	return(iNoteIndex);
	    }
	    
}
