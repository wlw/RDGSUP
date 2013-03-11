package com.bricksimple.rdg.FieldId;

public class DefedFacts {
    private String   Fact;
    private boolean  Contain;
    private String   Substitution = "";
    private int      Appendable = 0;
    
    public void SetFact(String value) {
    	Fact = value;
    }
    
    public String GetFact() {
    	return(Fact);
    }
    
    public void SetContain(boolean value) {
    	Contain = value;
    }
    
    public boolean GetContain() {
    	return(Contain);
    }
    
    public void SetSubstitution(String iValue) {
    	if(iValue == null)
    		Substitution = "";
    	else
    		Substitution = iValue;
    }
    
    public String GetSubstitution() {
    	return(Substitution);
    }
    
    public void SetAppendable(int iValue) {
    	Appendable = iValue;
    }
    
    public int GetAppendable() {
    	return(Appendable);
    }
}
