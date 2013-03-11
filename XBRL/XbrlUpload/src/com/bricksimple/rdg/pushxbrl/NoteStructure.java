package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;


public class NoteStructure {
    private String            NoteId = "";
    private ArrayList<String> NoteTables = new ArrayList<String>();
    private String            NoteDetails = "";
    private boolean           ContainsTables = false;
    private String            workStr;
    private int               NoteIndex;
    
    public void SetNoteId(String iValue) {
    	int i;
    	String DISCLOSURE = "Disclosure -";
   	
    	NoteId = iValue;
    	i = iValue.indexOf(DISCLOSURE);
    	workStr = iValue.substring(i+1 + DISCLOSURE.length()).trim();
    }
    
    public String GetNoteId() {
    	return(NoteId);
    }
    
    public String GetWorkStr() {
    	return(workStr);
    }
    
    public void SetNoteTables(String iValue) {
    	NoteTables.add(iValue);
    }
    
    public ArrayList<String> GetNoteTables() {
    	return(NoteTables);
    }

    public void SetNoteDetails(String iValue) {
    	NoteDetails = iValue;
    }
    
    public String GetNoteDetails() {
    	return(NoteDetails);
    }
    
    public void SetContainsTables(boolean bValue) {
    	ContainsTables = bValue;
    }
    
    public boolean GetContainsTables() {
    	return(ContainsTables);
    }
    
    public void InsertTableOrDetail(String iValue) {
    	int i = iValue.indexOf("(Detail)");
    	
    	if(iValue.length() > (i + "(Detail)".length()))
    			SetNoteTables(iValue);
    	else
    		SetNoteDetails(iValue);
    	/*
    	if(ContainsTables == true)
    		SetNoteTables(iValue);
    	else
    		SetNoteDetails(iValue);
    		*/
    }
    
    public void SetNoteIndex(int iValue) {
    	NoteIndex = iValue;
    }
    
    public int GetNoteIndex() {
    	return(NoteIndex);
    }
}
