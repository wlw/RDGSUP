package com.bricksimpe.rdg.util;
import java.util.ArrayList;

public class MatchStr {
    public String OrigString;  // original string as first read
    public String PathToData;
    public float    Threshold;         // unique identifier of OrigString
    public ArrayList<String> al;       // array list used to find in matching  

    public void MatchStr() {
    	OrigString = "";
    	PathToData = "";
    	Threshold = 0;
    	al = null;
    }
}
