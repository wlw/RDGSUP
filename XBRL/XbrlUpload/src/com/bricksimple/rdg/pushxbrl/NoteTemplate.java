package com.bricksimple.rdg.pushxbrl;

public class NoteTemplate {
    private String identifiedText;
    private int    templateUid;
    private int    noteNdx;
    
    
    public void SetIdentifiedText(String iValue) {
    	identifiedText = iValue;
    }
    
    public String GetIdentifiedText() {
    	return(identifiedText);
    }
    
    public void SetTemplateUid(int iValue) {
    	templateUid = iValue;
    }
    
    public int GetTemplateUid() {
    	return(templateUid);
    }
    
    public void SetNoteNdx(int iValue) {
    	noteNdx = iValue;
    }
    
    public int GetNoteNdx() {
    	return(noteNdx);
    }
    
}
