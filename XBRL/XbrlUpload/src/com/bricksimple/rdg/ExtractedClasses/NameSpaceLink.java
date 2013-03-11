package com.bricksimple.rdg.ExtractedClasses;

public class NameSpaceLink {
    private String Abbrev = "";
    private String NameSpace = "";
    
    public void SetAbbrev(String iValue) {
    	Abbrev = iValue;
    }
    
    public String GetAbbrev() {
    	return(Abbrev);
    }
    
    public void SetNameSpace(String iValue) {
    	NameSpace = iValue;
    }
    
    public String GetNameSpace() {
    	return(NameSpace);
    }
}
