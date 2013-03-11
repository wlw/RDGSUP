package com.bricksimple.rdg.FieldId;

public class UndoNoteStr {
    public boolean    bInUse = false;
    public int        iStartCol = 0;
    public int        iNumCol = 0;
    public int        iTrCount = 0;
    public String     strNodeData = "";
    
    public void Reset(){
    	bInUse = false;
        iStartCol = 0;
        iNumCol = 0;
        iTrCount = 0;
        strNodeData = "";
    }
    
    public void RemovePreviousStr(DateExtractSummary des) {
    	int iIndexOfStr;
    	int iCount = 0;
    	
    	for(;iCount < iNumCol; iCount++) {
    		iIndexOfStr = des.dateExtract.get(iStartCol + iCount).SupStr.indexOf(strNodeData);
    		des.dateExtract.get(iStartCol + iCount).SupStr = des.dateExtract.get(iStartCol + iCount).SupStr.substring(0,iIndexOfStr).trim();
    		
    	}
    }
}
