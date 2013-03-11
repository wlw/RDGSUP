package com.bricksimple.rdg.ExtractedClasses;

public class FootnoteLoc {
    private String Label = "";
    private String HRef = "";
    private int    Uid = 0;
    
    public void SetLabel(String iValue) {
    	Label = iValue;
    }
    
    public String GetLabel() {
    	return(Label);
    }

    public void SetHRef(String iValue) {
    	HRef = iValue;
    }
    
    public String GetHRef() {
    	return(HRef);
    }

    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }
}
