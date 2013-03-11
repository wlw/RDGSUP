package com.bricksimple.rdg.FieldId;


public class NoteDetailKeyWord {
	static final int POSTWORD = 0;
	static final int PREWORD = 1;
	
	private int     Uid; 
    private String  KeyWord = "";
    private Boolean Plural = false;
    private Boolean Contain = false;
    private int Position = POSTWORD;
   
    
    public void SetUid(int inVal) {
    	Uid = inVal;
    }
    
    public int GetUid() {
    	return(Uid);
    }
    
    public void SetKeyWord(String inVal) {
    	KeyWord = inVal;
    }
    
    public String GetKeyWord() {
        return(KeyWord);
    }
    
    public void SetPlural(Boolean inVal) {
    	Plural = inVal;
    }
    
    public Boolean GetPlural() {
    	return (Plural);
    }
    public void SetContain(Boolean inVal) {
    	Contain = inVal;
    }
    
    public Boolean GetContain() {
    	return (Contain);
    }
    
    public void SetPosition(int inVal) {
    	Position = inVal;
    }
    
    public int GetPosition() {
    	return(Position);
    }
}
