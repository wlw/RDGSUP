package com.bricksimple.rdg.ExtractedClasses;

public class FootnoteLink {
    private String Label;
    private int    Uid;
    
    public void SetLabel(String iValue) {
    	Label = iValue;
    }
    
    public String GetLabel() {
    	return(Label);
    }
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }

}
