package com.bricksimple.rdg.FieldId;
import java.util.ArrayList;

public class MatchStr {
    public int               iType;                    // type of string contained
    public String            OrigString;               // original string as first read
    public boolean           bSpanColumns;             // If true, may span columns
    public boolean           bPartialDateSpanColumns;  // if true, the partial date was part of a supplement str that spann
    public int               key;                      // unique identifier of OrigString
    public double            dConfidence;              // minimum confidence level     
    public boolean           bBooleanFlag;             //  For formidentification notesEnabled 
    public ArrayList<String> al;                       // array list used to find in matching  

    public MatchStr() {
    	iType = 0;
    	OrigString = "";
    	bSpanColumns = false;
    	bPartialDateSpanColumns = false;
    	key = 0;
    	dConfidence = 0.8;
    	al = null;
    }
}
