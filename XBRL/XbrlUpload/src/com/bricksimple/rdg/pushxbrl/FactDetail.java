package com.bricksimple.rdg.pushxbrl;

import com.bricksimple.rdg.ExtractedClasses.Period;

public class FactDetail {
    private int     GroupNdx;
    private int     ItemNdx;
    private int     LineNum;
    private String  Concept = "";
    private String  ContextRef = "";
    private boolean Omit = false;
    private int     ColumnNdx;
    private Period  period = new Period();
    private String  UnitRef = "";
    	
    
    public void SetGroupNdx(int iValue) {
    	GroupNdx = iValue;
    }
    
    public int GetGroupNdx() {
    	return(GroupNdx);
    }

    public void SetItemNdx(int iValue) {
    	ItemNdx = iValue;
    }
    
    public int GetItemNdx() {
    	return(ItemNdx);
    }
    
    public void SetLineNum(int iValue) {
    	LineNum = iValue;
    }
    
    public int GetLineNum() {
    	return(LineNum);
    }
    
    public void SetConcept(String strValue) {
    	int i = strValue.indexOf("#");
    	
    	if(i != -1)
    		Concept = strValue.substring(i+1);
    	else
    	    Concept = strValue;
    }
    
    public String GetConcept() {
    	return(Concept);
    }
    public void SetContextRef(String iValue) {
    	ContextRef = iValue;
    }
    
    public String GetContextRef() {
    	return(ContextRef);
    }
    
    public void SetOmit(boolean bValue) {
    	Omit = bValue;
    }
    
    public boolean GetOmit() {
    	return(Omit);
    }
    
    public void SetIColumnNdx(int iValue) {
    	ColumnNdx = iValue;
    }
    
    public int GetColumnNdx() {
    	return(ColumnNdx);
    }
    
    public void SetPeriod(Period myperiod) {
    	period.SetStartDate(myperiod.GetStartDate());
    	period.SetEndDate(myperiod.GetEndDate());
    	period.SetInstant(myperiod.GetInstant());
    }
    
    public Period GetPeriod() {
    	return(period);
    }
    
    public boolean IdenticalPeriod(Period testPeriod) {
    	boolean bRtn = false;
    	
    	if((testPeriod.GetStartDate().equals(period.GetStartDate())) &&
    	   (testPeriod.GetEndDate().equals(period.GetEndDate())) &&
    	   (testPeriod.GetInstant().equals(period.GetInstant())))
    		bRtn = true;
    	return(bRtn);
    }
    
    public void SetUnitRef(String iValue) {
    	UnitRef = iValue;
    }
    
    public String GetUnitRef() {
    	return(UnitRef);
    }
}
