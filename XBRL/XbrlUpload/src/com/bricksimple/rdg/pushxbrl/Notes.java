package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class Notes {
    private int    NoteUid;
    private String NoteLabel;
    private String Truncated;
    private int    NoteNdx;
    
    
    public void SetNoteUid(int iValue) {
    	NoteUid = iValue;
    }
    
    public int GetNoteUid() {
    	return(NoteUid);
    }
    
    public void SetNoteLabel(String iValue) {
    	int i = iValue.indexOf("(");
    	NoteLabel = iValue;
    	if(i != -1)
    	    Truncated = iValue.substring(0, i).trim();
    	else
    		Truncated = iValue;
    }
    
    public String GetNoteLabel() {
    	return(NoteLabel);
    }
    
    public String GetTruncated() {
    	return(Truncated);
    }
    
    public void SetNoteNdx(String label, ArrayList<NoteTemplate> noteTemplates) {
    	int  iValue = 0;
    	
    	for(NoteTemplate nt: noteTemplates) {
    		if(label.contains(nt.GetIdentifiedText()))
    			iValue = nt.GetNoteNdx();
    	}
    	NoteNdx = iValue;
    }
    
    public int GetNoteNdx() {
    	return(NoteNdx);
    }
}
