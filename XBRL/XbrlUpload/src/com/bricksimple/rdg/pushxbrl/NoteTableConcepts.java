package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;


public class NoteTableConcepts {
	private String            OrigTag = "";
    private String            NoteTag = "";
    private int               CurrentIndex = 0;
    private int               TotalConcepts = 0;
    private ArrayList<String> TableConcepts = new ArrayList<String>();
    
    
    public void SetOrigTag(String iValue) {
    	OrigTag = iValue;
    }
    
    public String GetOrigTag() {
    	return(OrigTag);
    }
    
    public void SetNoteTag(String iValue) {
   	
    	NoteTag = iValue;
    }
    
    public String GetNoteTag() {
    	return(NoteTag);
    }
    
    public int GetCurrentIndex() {
    	return(CurrentIndex);
    }
    
    public void AddTableConcept(String iValue) {
    	TableConcepts.add(iValue);
    	TotalConcepts++;
    }
    
    public String GetTableConcept() {
    	String rtnStr = "";
    	
    	if(TotalConcepts > 0) {
    		rtnStr = TableConcepts.get(CurrentIndex);
    		CurrentIndex++;
    		TotalConcepts--;
    	}
    	return(rtnStr);
    }
}
