package com.bricksimpe.rdg.Xbrlutil;

public class Replacements {
    private String OrigStr;
    private String ReplacementStr;
    
    public void SetOrigStr(String value) {
    	OrigStr = value;
    }
    
    public String GetOrigStr() {
    	return(OrigStr);
    }

    public void SetReplacementStr(String value) {
    	ReplacementStr = value;
    }
    
    public String GetReplacementStr() {
    	return(ReplacementStr);
    }
}
