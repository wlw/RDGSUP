package com.bricksimple.rdg.FieldId;
import java.sql.Connection;
import java.util.ArrayList;

public class NoteDetailParseCls {
    public String          preText = "";
    public int             preTextType = 0;
    private int            TemplateParseUid;
    private SubmissionInfo si = new SubmissionInfo();
    private int            NoteIndex;
    
    public void InitNoteDetailPasseCls(int iTemplateParseUid, SubmissionInfo inSi, int iNoteIndex) {
    	TemplateParseUid = iTemplateParseUid;
    	si.setCompanyId(inSi.getCompanyId());
    	si.setUid(inSi.getUid());
    	si.setVersion(inSi.getVersion());
    	NoteIndex = iNoteIndex;
    }
    
    public void WritePreText(Connection con) {
    	MySqlAccess mySqlAccess = new MySqlAccess();
    	
		if(preText.length() > 0) {
			mySqlAccess.InsertNoteWord(con, si, TemplateParseUid, preText, preTextType,
	                   NoteIndex);	
		    preText = "";
		    preTextType = 0;
        }
    }
    
    public void WritePreTextIfNotNumber(Connection con) {
    	MySqlAccess mySqlAccess = new MySqlAccess();
    	
	    if((preText.length() > 0) && (preTextType != CONSTANTS.NOTE_NUMBER)) {
		    mySqlAccess.InsertNoteWord(con, si, TemplateParseUid, preText, preTextType,
                                       NoteIndex);	
	        preText = "";
	        preTextType = 0;
	    }
	}

    public void WritePreTextWithCurrentInfo(Connection con, String strWord, int iWordType) {
    	MySqlAccess mySqlAccess = new MySqlAccess();

    	if(preText.length() > 0) {
			preTextType = 0;
			mySqlAccess.InsertNoteWord(con, si, TemplateParseUid, preText + " " + strWord, iWordType, NoteIndex);
			preText = "";
		}
 	
    }
    
    public int WriteMonies(Connection con, ArrayList<String> words, int iCurNdx, int iNoteDetailType) {
    	int         iRtn = 1;
        String      wordsToWrite = words.get(iCurNdx);
        String      nextWord = "";
    	MySqlAccess mySqlAccess = new MySqlAccess();
        
        if((iCurNdx + iRtn) < words.size()) {
        	nextWord = words.get((iCurNdx + iRtn));
        	if(wordsToWrite.length() == 1) {  // this must be number
        		wordsToWrite += " " + nextWord;
         		iRtn++;
        	}
        	//now we check for thousands/millions/billions
        	if((iCurNdx + iRtn) < words.size()) {  // only check if available
        		nextWord = words.get((iCurNdx + iRtn)).toLowerCase();
        		if((nextWord.contains("thousand")) || (nextWord.contains("million")) || (nextWord.contains("billion"))) {
        			wordsToWrite += " " + words.get((iCurNdx + iRtn));
        			iRtn++;
        		}
        	}
        }
		mySqlAccess.InsertNoteWord(con, si, TemplateParseUid, wordsToWrite, iNoteDetailType, NoteIndex);
    	return(iRtn);
    }
    

    public void SetPreText(String strPreText, int iTextType) {
    	preText = strPreText;
    	preTextType = iTextType;
    }
}
