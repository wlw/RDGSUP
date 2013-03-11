package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;
public class DimensionGroup {
    private String               ContextId = "";
    private ArrayList<Dimension> dimension = new ArrayList<Dimension>();
    private ArrayList<Period>    period = new ArrayList<Period>();
    
    public void SetContextId(String iValue) {
    	ContextId = iValue;
    }
    
    public String GetContextId() {
    	return(ContextId);
    }
    
    public void AddDimension(Dimension iValue) {
    	dimension.add(iValue);
    }
    
    public ArrayList<Dimension> GetDimension() {
    	return(dimension);
    }
    
    public void AddPeriod(Period iValue) {
    	period.add(iValue);
    }
    
    public Period GetPeriod() {
    	return(period.get(0));
    }
}

