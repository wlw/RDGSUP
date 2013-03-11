package com.bricksimpe.rdg.XbrlTemplateId;

import java.util.ArrayList;

public class NodeIdStr {
    private int               Uid;
    private String            NoteStr;
    public  ArrayList<String> Al;
    
	public void setUid(int uid) {
		Uid = uid;
	}
	public int getUid() {
		return Uid;
	}
	public void setNoteStr(String nodeStr) {
		NoteStr = nodeStr;
	}
	public String getNoteStr() {
		return NoteStr;
	}

	public ArrayList<String> getAl() {
	     return( Al);
	 }
	    
	 public void setAl(String nodeStr) {
		ConfidenceLevel cl = new ConfidenceLevel();
			
	   	Al = cl.RecurringSting(nodeStr);
	 }
}
