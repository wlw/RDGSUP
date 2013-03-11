package com.bricksimple.rdg.pushxbrl;

public class NoteUidXref {
    private int    Uid;
    private String IdentifiedText; 
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }
    
    public void SetIdentifiedText(String iValue) {
    	IdentifiedText = iValue;
    }
    
    public int IsThisMyNote(String iValue) {
    	String temp;
    	int    iRtn = 0;
    	
    	if(IdentifiedText.contains(iValue))
    		iRtn = Uid;
    	return(iRtn);
    }
}
