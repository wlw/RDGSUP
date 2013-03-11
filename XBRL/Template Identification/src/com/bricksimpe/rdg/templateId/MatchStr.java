package com.bricksimpe.rdg.templateId;
import java.util.ArrayList;

public class MatchStr {
    public int               iType;      // type of string contained
    public String            OrigString;  // original string as first read
    public int               key;         // unique identifier of OrigString
    public boolean           bBooleanFlag;             //  For formidentification notesEnabled 
    public ArrayList<String> al;       // array list used to find in matching  

    public void MatchStr() {
    	iType = 0;
    	OrigString = "";
    	key = 0;
    	bBooleanFlag = true;
    	al = null;
    }
}
