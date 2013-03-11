package com.bricksimple.rdg.FieldId;

public class HtmlTraceItem {

	private int     BeginLineNum;
	private int     EndLineNum;
	private int     TemplateId;
	private double  ConfidenceLevel;
	private String  TermStr;
	
	
public  HtmlTraceItem (int Bline, int Eline, int Tid, double Cf, String Term) {
	    	BeginLineNum = Bline;
	    	EndLineNum = Eline;
	    	TemplateId = Tid;
	    	ConfidenceLevel = Cf;
	    	TermStr = Term;
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
	    
}
