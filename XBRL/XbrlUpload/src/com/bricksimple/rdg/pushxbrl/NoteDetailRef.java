package com.bricksimple.rdg.pushxbrl;

public class NoteDetailRef {
    private String Group;
    private String Context;
    
    public void SetGroup(String iValue) {
    	Group = iValue;
    }
    
    public String GetGroup() {
    	return(Group);
    }
    
    public void SetContext(String iValue) {
    	Context = iValue;
    }
    
    public String GetContext() {
    	return(Context);
    }
}
