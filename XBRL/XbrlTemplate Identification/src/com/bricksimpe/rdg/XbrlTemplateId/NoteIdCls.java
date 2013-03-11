package com.bricksimpe.rdg.XbrlTemplateId;

import java.sql.Connection;
import java.util.ArrayList;

public class NoteIdCls {
    private int                iNoteNum  = 1;
    private ArrayList<String>  BaseStrs = new ArrayList<String>();
    private ArrayList<String>  NumNoteStrs = new ArrayList<String>();
    private ArrayList<String>  AscNoteStrs = new ArrayList<String>();
    private int                iActiveList = -1;
    private int                iActiveNote = -1;
    private ArrayList<Boolean> bTrimStr = new ArrayList<Boolean>();
    private String             CaseStr = "";
    private String             NoCaseStr = "";
    private String             SpecialStr = "";
    private String             ParensStr = "";
    private String             SingleParensStr = "";
    private String             PreviousMatch = "";
    private String             CharacterStr = "";
    private int                Base_character = 96;
    private boolean            bCheckForNotes = true;
    private boolean[]          bCheck = {true, true, true, true, true, true};
    private ArrayList<ArrayList<String>> noteItems = new ArrayList<ArrayList<String>>();
   
    public void SetActiveNote(int value) {
    	iActiveList = value;
    }
    
    public int GetActiveNote() {
    	return(iActiveList);
    }
    
    public void setNoteNum(int value) {
    	iNoteNum = value;
    }
    
    public int GetNoteNum() {
    	return(iNoteNum);
    }
    
    public void SetActiveList(int value) {
    	iActiveList = value;
    }
    
    public int GetActiveList() {
    	return(iActiveList);
    }
    
    public void InitCls(Connection con) {
    	MySqlAccess           mysql = new MySqlAccess();
    	String                insertStr;
    	String                NoteIdStr = "" + iNoteNum;
        String                AcsNoteIdStr = GetAscConversion(iNoteNum);
        int                   iNoteItemsNdx = 0;
    	ArrayList<NoteFmtCls> noteFmtCls = mysql.GetNoteFormats(con);
    	
    	for(NoteFmtCls curItem : noteFmtCls) {
    		bTrimStr.add(curItem.lTrim);
    		BaseStrs.add(curItem.FmtStr);
    		insertStr = curItem.FmtStr;
    		NumNoteStrs.add(insertStr.replace("~", NoteIdStr));
    		AscNoteStrs.add(insertStr.replace("~", AcsNoteIdStr));
    		noteItems.add(new ArrayList<String>());
    		InsertNoteItems(noteItems.get(iNoteItemsNdx), insertStr);
    		iNoteItemsNdx++;
    	}
    }
    
 
    private void InsertNoteItems(ArrayList<String> thisNote, String note) {
        String extract;
        int    iIndex;
        String workingStr = note;
        
        workingStr = workingStr.trim();
        iIndex = workingStr.indexOf("~");
        extract = workingStr.substring(0,iIndex).trim();
        thisNote.add(extract);
        thisNote.add("~");
        workingStr = workingStr.substring(iIndex+1).trim();
        if(workingStr.length() > 0)
            thisNote.add(workingStr);
    }
    
    private String GetAscConversion(int iNum) {
    	String rtnStr = "";
    	char[] c = null;
    	int iTempNoteNum = iNum;
    	int iPreNoteNum = 0;
    	while(iTempNoteNum > 27) {
    		iPreNoteNum++;
    		iTempNoteNum -= 27;
    	}
    	if(iPreNoteNum > 0) {
    		c = Character.toChars(Base_character + iPreNoteNum);
    		rtnStr += Character.toString(c[0]);
    	}
    	CharacterStr  = "";
    	c = Character.toChars(Base_character + iTempNoteNum);
   		rtnStr += Character.toString(c[0]);   	
    	return(rtnStr);
    }
    
    private void BumpNoteNum() {
    	String NewNoteStr = "";
    	String NextNoteStr = "";
    	
    	iNoteNum++;
    	
    	NewNoteStr = BaseStrs.get(iActiveNote);
    	switch(iActiveList) {
    	    case 1:
    	    	NextNoteStr = "" + iNoteNum;
    	    	NewNoteStr = NewNoteStr.replace("~", NextNoteStr);
    	    	NumNoteStrs.set(iActiveNote, NewNoteStr);
    	    	break;
    	    	
    	    case 2:
    	    	NextNoteStr = GetAscConversion(iNoteNum);
    	    	NewNoteStr = NewNoteStr.replace("~", NextNoteStr);
    	    	AscNoteStrs.set(iActiveNote, NewNoteStr);
          	    	
    	}
    }
    
    public void SetCheckForNotesFlag(boolean bValue) {
    	bCheckForNotes = bValue;
    }
    
    public boolean CheckForFootnote(String strLine) {
    	boolean bRtn = false;
    	
    	if(strLine.contains("(1)") == true)
    		bRtn = true;
    	return(bRtn);
    }
    
    public boolean MatchFootNote(String strLine) {
    	boolean bRtn = false;
    	int     iIndex;
    	
       iIndex = strLine.indexOf("(1)");
       if(iIndex == 0)
    	   bRtn = true;
       return(bRtn);
    }
    
    private String GetStringToMatch(int iActive, int iNote) {
    	String rtnStr = "";
    	switch (iActive) {
    	    case 1:
    	    	rtnStr = NumNoteStrs.get(iNote);
    	    	break;
    	    	
    	    case 2:
    	    	rtnStr = AscNoteStrs.get(iNote);
    	    	break;
    	}
    	return(rtnStr);
    }
    
    private String GetNoteId(int iActive) {
    	String rtnStr = "";
    	
    	switch (iActive) {
    	    case 1:
    		    rtnStr += iNoteNum;
    		    break;
    		    
    	    case 2:
    	    	char digit = (char)(iNoteNum + 97 -1);   // 97 is a lower case A and we start at 1
    	    	rtnStr += digit; 
    	}
    	return(rtnStr);
    }
    
    public boolean DoesTextHaveLength(int iMatchIndx, int extractedLen, boolean bFoundTableBegin) {
    	boolean  bRtn = false;
    	
    	if(bFoundTableBegin == true) { //only check if within a table
    		if((BaseStrs.get(iMatchIndx).length() + 3) > extractedLen)
    			bRtn = true;
    	}
    	return(bRtn);
    }
    
    public int TestNote(String strLine, boolean bWithinTable, String companyName) {
    	int        iRtn = 0;
    	String     TestStr = strLine.trim();  // possible format error as leading spaces - BIG ASSUMPtION
    	String     NoteStr = "";
    	boolean    bSetiRtn = true;
    	CONSTANTS  constants = new CONSTANTS();
    	
    	//THIS IS A CHECK for labeling issues
    	
    	//if((iNoteNum == 1) && (strLine.contains("financial statements")))
    	if(bCheckForNotes == false)
    			iRtn = 0;
    	else {
    		if(iActiveList == -1) {  // check all for a match
    			iRtn = CheckThisTable(1, strLine, bWithinTable, companyName);
    			if(iRtn == 0)
    				iRtn = CheckThisTable(2, strLine, bWithinTable, companyName);
     		}
    		else {  // just test this one
    			if(bTrimStr.get(iActiveNote) == true)
    				TestStr = ltrim(strLine);
    			NoteStr = GetStringToMatch(iActiveList, iActiveNote);
    			if(bWithinTable)
    				NoteStr = NoteStr.trim();
    			if(TestStr.indexOf(NoteStr) == 0) {  // expanded test to ensure that match is NOT 8.43
    				if(TestStr.length() > NoteStr.length()) {
    					String isNum = TestStr.substring(NoteStr.trim().length(), NoteStr.trim().length() + 1);
    					if(constants.isNumeric(isNum) != -1)
    						bSetiRtn = false;
    				}
    				//else  // they must be equal, so set match which is non zero  // I think this is not necessary
    				//	iRtn = 1;
    				if(bSetiRtn == true)
    				    iRtn = SetReturnCode(TestStr, NoteStr);
    			}
    			if((NoteStr.contains("-") || (NoteStr.toLowerCase().contains("note")))) {  // this one is messed up because of long dash
     				iRtn = NonSpaceCompare(iActiveList, iActiveNote, TestStr);
    			}
    		}
    	}
    	if(iRtn != 0) {
     		BumpNoteNum();
    	}
    	return(iRtn);
    }
    
    private int NonSpaceCompare(int iActiveList, int iArrayNdx, String TestStr) {
    	int     iRtn = 0;
    	String  strNoteNum = GetNoteId(iActiveList); // "" + iNoteNum;
    	int     iRowIndex = 0;
    	boolean bContinue = true;
    	String  extract = TestStr.trim();
    	
    	while(bContinue) {
    		if(iRowIndex < noteItems.get(iArrayNdx).size()) {  // check for match
    			if(noteItems.get(iArrayNdx).get(iRowIndex).contains("~")) {
    				if(extract.indexOf(strNoteNum) == 0) {
    					extract = extract.substring(strNoteNum.length()).trim();
    				}
    				else {
    					bContinue = false;  // no match in number
    				}				
    			}
    			else {  // match text
    				if(extract.indexOf(noteItems.get(iArrayNdx).get(iRowIndex))== 0) { // matched word
    					extract = extract.substring(noteItems.get(iArrayNdx).get(iRowIndex).length()).trim();
    				}
    				else
    					bContinue = false;
    			}
    		}
    		else {  // matched all
    			iRtn = 1;
    			bContinue = false;
    		}
    		iRowIndex++;
    	}
    	return(iRtn);
    }
     
    private int CheckThisTable(int iTable, String origStr, boolean bWithinTable, String companyName) {
    	int     iRtn = 0;
    	int     iNoteNdx = 0;
    	String  testStr = "";
    	String  toMatch = "";
    	
    	while((iRtn == 0) && (iActiveList == -1) && (iNoteNdx < bTrimStr.size())) {
    	    if(bTrimStr.get(iNoteNdx) == true)
    		    testStr = ltrim(origStr);
    	    else
    		    testStr = origStr;
            switch (iTable) {
                case 1:
            	    toMatch = NumNoteStrs.get(iNoteNdx);
            	    break;
                case 2:
            	    toMatch = AscNoteStrs.get(iNoteNdx);
            	    break;
            }
            if(bWithinTable)
            	toMatch = toMatch.trim();
           // int j = testStr.indexOf(toMatch);
            if(testStr.indexOf(toMatch) == 0) {
            	if(testStr.indexOf(companyName.toLowerCase()) == 0) 
            		iNoteNdx++;
            	else {	
            	    iActiveList = iTable;
            	    iActiveNote = iNoteNdx;
            	    iRtn = SetReturnCode(testStr, toMatch);
            	}
            }
            else
            	iNoteNdx++;
        }
    	
    	return(iRtn);
    }
    
    private int SetReturnCode(String fileStr, String tableStr) {
    	int iRtn = 1;
    	String extractStr = fileStr.substring(tableStr.length()).trim();
    	
    	if(extractStr.length() == 0)
    		iRtn = 2;
    	return(iRtn);
    }
    
    public String ltrim(String source) {
    	return(source.replace("^\\s+",""));
    }
    
    public boolean IsTableNote(String matchText, String origText) {
    	boolean bRtn = false;
    	
    	if((origText.indexOf("<table>") != -1) || 
    	   (origText.indexOf("<TABLE>") != -1)) {
    		if(matchText.length() == PreviousMatch.length())
    			bRtn = true;
    	}
    	return(bRtn);
    }
}
