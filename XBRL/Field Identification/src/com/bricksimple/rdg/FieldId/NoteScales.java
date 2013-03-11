package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

public class NoteScales {
    public String ExtractedStr = "";
    public int    NoteScale = 1;
    
    public void ExtractNoteScale(String OrigStr, ArrayList<NoteScaleRec> nscArray) {
    	
    	int iIndex = 0;

    	while((iIndex < nscArray.size()) && (NoteScale == 1)) {
    		if(OrigStr.contains(nscArray.get(iIndex).ScaleStr)) {
    			NoteScale = nscArray.get(iIndex).ScaleValue;
    			OrigStr = OrigStr.replace(nscArray.get(iIndex).ScaleStr, "");
    			OrigStr = OrigStr.replace("()", "");
    		}
    		iIndex++;
    	}
    	ExtractedStr = OrigStr.trim(); 
    }
}
