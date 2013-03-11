package com.bricksimpe.rdg.templateId;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.sql.Connection;


public class CheckForTemplateStr {
    private int       LastMatchIndex; // = j;
    private double    MaxMatchValue; //  = dConfidence;
    private String    MatchedText = ""; //  = strLine;
    private String    strLastTemplate;
    private int       iIdLine; //  = LineNumber;
    private int       iBeginLineNum;
    private int       iLastTemplateId;
    private int       iLastTableTag = 0;
    private int       iScalePrev;
    private boolean   bFoundTemplateBegin;
    private boolean   bFoundTableBegin;
	private boolean   bMayHaveFootNotes;
	private boolean   bWithInNote;
	private int       iLinesRead;
	private boolean   bAddNoteNextLine;
	private boolean   bNoteWithinTable;
	private boolean   bAddNoteComment = false;
	private int       iPreservedMatchIndex;
	private String    strPreservedTemplate;
	private boolean   bFoundNotes = false;
	private boolean   bAddSectionText = false;
	public NoteIdCls  noteIdCls = new NoteIdCls();
	private boolean[] bFoundSections = new boolean[CONSTANTS.NUM_MIN_SECTIONS];
	private int       iLastAccompanyingNotes = 0;
	
	public void PreservePrevious() {
		if(bFoundNotes == false) {
		    iPreservedMatchIndex = LastMatchIndex;
		    strPreservedTemplate = MatchedText; //strLastTemplate;
		}
	}
	
	public void DefaultToPrevious() {
		if(bFoundNotes == false) {
			MatchedText = strPreservedTemplate;
			LastMatchIndex = iPreservedMatchIndex;
		}
	}
	
	public int GetPreservedMatchIndex() {
		return(iPreservedMatchIndex);
	}
	
	public String GetPreservedMatchedText() {
		return(strPreservedTemplate);
	}
	
	public void SetLastMatchIndex(int iValue) {
		LastMatchIndex = iValue;
	}
	
    public void Reset() {
		MaxMatchValue = 0.0;
		iLastTemplateId = 0;
		strLastTemplate = "";
		bFoundTemplateBegin = false;
		bMayHaveFootNotes = false;
		bFoundTableBegin = false;
		bWithInNote = false;
		iBeginLineNum = 0;
		iScalePrev = 1;
		iLinesRead = 0;
		bAddNoteNextLine = false;
		bNoteWithinTable = false;
		bAddNoteComment = false;
		bAddSectionText = false;
    }
    
    public void Init() {
		for(int i = 0; i < bFoundSections.length; i++) {
			bFoundSections[i] = false;
		}
    	
    }
    
    public boolean IsNotesEnabled(Connection con, ErrorCls errorCls, boolean bLog) {
    	
    	boolean        bRtn = true;
    	MySqlAccess    mySqlAccess = new MySqlAccess();
    	
    	for(int i = 0; i < bFoundSections.length; i++) {
    		if(bFoundSections[i] == false) {
    			bRtn = false;
    			if(bLog) {
		     	    errorCls.setErrorText("Minimum sections not discovered during parse: " + CONSTANTS.MIN_SECTION_STRS[i]);
		    	    mySqlAccess.WriteAppError(con, errorCls);
    			}
    		}
    	}
    	return(bRtn);
    }
    
    public void SetSectionFound(int iSection) {
    	
    	for(int i = 0; i < CONSTANTS.MIN_SECTION_IDS.length; i++) {
    		if(iSection == CONSTANTS.MIN_SECTION_IDS[i])
    			bFoundSections[CONSTANTS.SECTION_INDEX[i]] = true;
    	}
    }
    
	public boolean DoCheck(String strLine, String extracted, int LineNumber, TemplateIdStr[] TemplateInfo,
			               ArrayList<HtmlTraceItem>  TemplateTrace, BufferedReader br, String companyName) {
		boolean          bRtn = false;
		int              j;
		double           dConfidence = 0;
		ConfidenceLevel  cl = new ConfidenceLevel();
		boolean          bLocalFoundTemplateBegin = false;
		boolean          bDebug = false;
		CONSTANTS        constants = new CONSTANTS();
		boolean          bOnlyCheckForNotes = false;
		/****
		 *  ||
    	    	    	   ((checkForTemplateStr.CheckAccompanying(LineNumber) == true) && 
    	    	    	    (checkForTemplateStr.GetFoundTemplateBegin() == true) && 
    	    	    	    (checkForTemplateStr.GetFoundTableBegin() == false)  && 
    	    	    	    (checkForTemplateStr.GetWithInNote() == false))
		 * 
		 * 
		 */
		if(iLastAccompanyingNotes == LineNumber )
			return(bRtn);
		if((iLastAccompanyingNotes + 1) == LineNumber )
			bOnlyCheckForNotes = true;
		String Test = extracted.trim();
		if((Test.indexOf("and") == 0) && (LineNumber -1 == iIdLine)) {  // this is a continuation line
			extracted = constants.RemoveHtmlCode(MatchedText) + " " + extracted;
			strLine = MatchedText + " " + strLine;
			LineNumber--;
		}
    	for(j = 0; j < TemplateInfo.length; j++) {
            switch (TemplateInfo[j].getStartType()) {
                case 0:
                	if((bFoundNotes == false) && (bOnlyCheckForNotes == false)) {
                        dConfidence = cl.compareToArrayList(extracted, TemplateInfo[j].Al);
                        if(dConfidence > MaxMatchValue) {
            	            LastMatchIndex = j;
            	            MaxMatchValue = dConfidence;
            	            MatchedText = strLine;
                            iIdLine = LineNumber;
                        }
                     }
                    break;
        
                case 1:  // found 'note' now is it our 'Note n.' ??
                	if(IsNotesEnabled(null, null, false)) {
                	    boolean bCheckForNote = true;
            	        if(bMayHaveFootNotes == true) {
            		        boolean bFoundFootnote = noteIdCls.MatchFootNote(extracted);
            		        if(bFoundFootnote == true) {
            			        bMayHaveFootNotes = false;   
            			        bCheckForNote = false;  // do not check for note on this line!!
            		        }
            	        }
            	        NoteIdCls prevNote = new NoteIdCls();
            	        prevNote.SetActiveList(noteIdCls.GetActiveList());
            	        prevNote.SetActiveNote(noteIdCls.GetActiveNote());
            	        prevNote.setNoteNum(noteIdCls.GetNoteNum());
            	        int iMatchReturn = 0;
            	        if(bCheckForNote == true) {
            	            iMatchReturn = noteIdCls.TestNote(extracted, bFoundTableBegin, companyName);
            	            if( iMatchReturn != 0) {
            	            	bAddSectionText = noteIdCls.DoesTextHaveLength(iMatchReturn, extracted.length(), bFoundTableBegin);
            	        	    bFoundNotes = true;
            		            bWithInNote = true;
            		            if(bFoundTableBegin == true)
            		        	    iBeginLineNum = iLastTableTag;
            		            else
		                            iBeginLineNum = LineNumber;
		                        bLocalFoundTemplateBegin = true;
    		                    MatchedText = strLine;
    		                    if((bFoundTableBegin == true) && (iMatchReturn == 2)) {  // table note pick up next line just to make sure
    		            	        boolean bContinue = true;
    		            	        while(bContinue) {
    		            	            try {
    		            	                strLine = br.readLine();
    	    		            	        LineNumber++;
    	    		            	        iLinesRead++;
    	    		            	        strLine = constants.RemoveHtmlCodeSaveCase(strLine);
    	    		            	        if(strLine.length() > 0) {
    	    		            	            MatchedText += " " + strLine;
    	    		            	            bContinue = false;
    	    		            	        }
  		            	                }
    		            	            catch (Exception e) {
    		            	            //WLW to fix
    		            	            }
    		            	        }
    		                    }
    		                    bAddNoteNextLine = noteIdCls.IsTableNote(extracted, strLine);
    		                    bNoteWithinTable = bFoundTableBegin;
    		                    MaxMatchValue = 1.0;
    		                    bRtn = true;
            	            }
            	        }
            	        if(iMatchReturn == 0) {  // put back 
            	    	    noteIdCls.SetActiveList(prevNote.GetActiveList());
            	    	    noteIdCls.SetActiveNote(prevNote.GetActiveNote());
            	    	    noteIdCls.setNoteNum(prevNote.GetNoteNum());
            	        }
                	}
    	            break;
    	
                case 2:  // looking for this string
                	if(bDebug) {
                		int i = -2;
                		System.out.println(strLine);
                		String StrLine = "";
                		String StrLine1 = "";
                		StringBuffer ostr = new StringBuffer();
                		char c;
                		String hex;
                	    for(i = 0; i < strLine.length(); i++) {
                	    	c = strLine.charAt(i);
                	    	hex = Integer.toHexString(c);
                	    	hex = hex.substring(hex.length() -2);
                	    	StrLine += hex + " ";
                	    }
                	    for(i = 0; i < TemplateInfo[j].getTempStr().length(); i++) {
                	    	c = TemplateInfo[j].getTempStr().charAt(i);
                	    	hex = Integer.toHexString(c);
                	    	hex = hex.substring(hex.length() -2);
                	    	StrLine1 += hex + " ";
                	    }
                		System.out.println(TemplateInfo[j].getTempStr());
                		System.out.println(StrLine);
                		System.out.println(StrLine1);
                		i = strLine.indexOf(TemplateInfo[j].getTempStr());
                		boolean b = strLine.contains(TemplateInfo[j].getTempStr());
                		boolean cc = b;
                	}
                	if(bOnlyCheckForNotes == false) {
    	                if((strLine.indexOf(TemplateInfo[j].getTempStr()) >= 0) && (strLine.length() < (TemplateInfo[j].getTempStr().length() * 10))) {  // guess here on length
    	            	    if(TemplateInfo[j].getLcTempStr().equals("operations") == false) {  // false reading
     		                    iBeginLineNum = LineNumber;
     		                    bLocalFoundTemplateBegin = true;
    		                    dConfidence = 1.0;
    		                    MatchedText = strLine;
    		                    MaxMatchValue = 1.0;
    		                    bRtn = true;
    	            	    }
    	            	}
    	            }
    	            break;
            	
               }  // end of switch
            if(bLocalFoundTemplateBegin)  { // found NOTE so exit 
            	bFoundTemplateBegin = true;
    	        LastMatchIndex = j;
   	            break;   // break out of the FOR loop
               }
            else {  // we check here for orphan table
        	    if((bFoundTableBegin == true) && (iLastTemplateId > 0)) {
        		    if(constants.CheckForIdentifier(extracted, 1) != -1) {  // found orphan table assign to previous template??
                	    bRtn = false;
                        HtmlTraceItem TraceItem = new  HtmlTraceItem();
                        TraceItem.setBeginLine(iLastTableTag);
                        TraceItem.setConfidenceLevel(1);
                        TraceItem.setEndLine(LineNumber);
                        TraceItem.setTemplateId(iLastTemplateId);
                        TraceItem.setTermStr(CONSTANTS.EndOfTblOfCont);
                        TraceItem.setIdentifiedText(strLastTemplate);
                        TraceItem.setScale(iScalePrev);
                        TraceItem.setIdLine(-1);
                        TemplateTrace.add(TraceItem);
                        iLastTemplateId = 0;
                        strLastTemplate = "";
        		    }
        	    }
            }
        }  // end of FOR
    	if((bFoundTemplateBegin == true)&& (MaxMatchValue > 0.80) && (iLinesRead == 0)) { // check if parsing text side and got a match
    		iBeginLineNum = LineNumber;                             // then set line number and caller will not restore info
    		//if(bWithInNote == true)
    		//	bFoundTemplateBegin = false;
    	}
		return(bRtn);
	}
	
	
	public void AddNoteHeader(String htmlStr) {
		String extracted = "";
		CONSTANTS constants = new CONSTANTS();
		
		if(bAddSectionText) {
			extracted = constants.RemoveHtmlCodeNoCase(htmlStr);
			if((extracted.length() > 5) && (extracted.length() < 50)) {
				MatchedText = MatchedText.trim() + " " + extracted;
				bAddSectionText = false;
			}
			
		}
	}
	public void UnMarkedTable( ArrayList<HtmlTraceItem> TemplateTrace, int curLineNumber) {
		
		HtmlTraceItem TraceItem = new  HtmlTraceItem();
		HtmlTraceItem PrevItem = new HtmlTraceItem();
		
		if(iPreservedMatchIndex != 0) {
		    PrevItem = TemplateTrace.get(TemplateTrace.size() -1);
            TraceItem.setBeginLine(iLastTableTag);
            TraceItem.setConfidenceLevel(PrevItem.getConfidenceLevel());
            TraceItem.setEndLine(curLineNumber);
            TraceItem.setTemplateId(PrevItem.getTemplateId());
            TraceItem.setTermStr(PrevItem.getTermStr());
            TraceItem.setIdentifiedText("CONTINUATION");
            TraceItem.setUnAudited(PrevItem.getBoolUnAudited());
            TraceItem.setScale(PrevItem.getScale());
            TraceItem.setIdLine(iIdLine);
            noteIdCls.SetCheckForNotesFlag(true);
            TemplateTrace.add(TraceItem);
            SetFoundTemplateBegin(false);
            SetFoundTableBegin(false);
            SetMaxMatchValue(0);
		}
	}
	
	public void RestoreSaved(SavedCheckForTemplate savedCheckForTemplate) {
    	iIdLine = savedCheckForTemplate.GetIdLine();
    	LastMatchIndex = savedCheckForTemplate.GetLastMatchIndex();
    	MatchedText = savedCheckForTemplate.GetMatchedText();
    	MaxMatchValue = savedCheckForTemplate.GetMaxMatchValue();
		
	}
	
	public int GetLastMatchIndex() {
		return(LastMatchIndex);
	}
	
	public void SetMaxMatchValue(double iValue) {
		MaxMatchValue = iValue;
	}
	
	public double GetMaxMatchValue() {
		return(MaxMatchValue);
	}
	
	public String GetMatchedText() {
		return(MatchedText);
	}
	
	public void SetMatchedText(String iValue) {
		MatchedText = iValue;
	}
	
	public int GetIdLine() {
		return(iIdLine);
	}
	
	public int GetBeginLineNum() {
		return(iBeginLineNum);
	}
	
	public void SetBeginLineNum(int iValue) {
		iBeginLineNum = iValue;
	}
	
	public int GetLastTemplateId() {
		return(iLastTemplateId);
	}
	
	public void SetLastTemplateId(int iValue) {
		iLastTemplateId = iValue;
	}
	
	public int GetScalePrev() {
		return(iScalePrev);
	}
	
	public void SetScalePrev(int iValue) {
		iScalePrev = iValue;
	}
	
	public int GetLastTableTag() {
		return(iLastTableTag);
	}
	
	public void SetLastTableTag(int iValue) {
		iLastTableTag = iValue;
	}
	
	public boolean GetFoundTemplateBegin() {
	    return(bFoundTemplateBegin);
	}
	
	public void SetFoundTemplateBegin(boolean bValue) {
		bFoundTemplateBegin = bValue;
	}

	public boolean GetFoundTableBegin() {
	    return(bFoundTableBegin);
	}
	
	public void SetFoundTableBegin(boolean bValue) {
		bFoundTableBegin = bValue;
	}

	public boolean GetWithInNote() {
	    return(bWithInNote);
	}
	
	public boolean GetFoundNotes() {
		return(bFoundNotes);
	}
	
	public void SetWithInNote(boolean bValue) {
		bWithInNote = bValue;
	}

	public boolean GetMayHaveFootNotes() {
	    return(bMayHaveFootNotes);
	}
	
	public void SetMayHaveFootNotes(boolean bValue) {
		bMayHaveFootNotes = bValue;
	}

	public int GetLinesRead() {
	    return(iLinesRead);
	}
	
	public void SetLinesRead(int iValue) {
		iLinesRead = iValue;
	}

	public void BumpLinesRead() {
		iLinesRead++;
	}
	
	public boolean GetAddNoteNextLine() {
	    return(bAddNoteNextLine);
	}
	
	public void SetAddNoteNextLine(boolean bValue) {
		bAddNoteNextLine = bValue;
	}

	public boolean GetNoteWithinTable() {
	    return(bNoteWithinTable);
	}
	
	public void SetNoteWithinTable(boolean bValue) {
		bNoteWithinTable = bValue;
	}
	
	public String GetLastTemplate() {
		return(strLastTemplate);
	}
	
	public void SetLastTemplate(String strValue) {
		strLastTemplate = strValue;
	}
	
	public void SetAddSectionText(String extracted) {
		
		bAddSectionText = noteIdCls.DoesTextHaveLength(noteIdCls.GetActiveNote(), extracted.length(), bFoundTableBegin);
	}
	
	public void SetLastAccompanyingNotes(String LineIn, int iValue) {
		CONSTANTS constants = new CONSTANTS();
		String    lowerCased = LineIn.toLowerCase();
		
		if(lowerCased.contains(constants.FalseIdentifier))
		    iLastAccompanyingNotes = iValue;
		else {
			if(lowerCased.contains(constants.FalseIdentifier2))
				iLastAccompanyingNotes = iValue;
		}
	}
	
	public int GetLastAccompanyingNotes() {
		return(iLastAccompanyingNotes);
	}
	
	public boolean CheckAccompanying(int curLineNumber) {
		boolean bRtn = false;
		
		if((iIdLine > 0) && (iLastAccompanyingNotes + 1) >= iIdLine )
			bRtn = true;
		return(bRtn);
	}
	
	public int IsHeaderLine(String origStr) {
		int iRtn = 0;
		String TestStr = origStr.toLowerCase();
		CONSTANTS constants = new CONSTANTS();
		
		if(constants.DoesStrContainMonth(TestStr))
			iRtn = 1;
		else {
			if(constants.DoesStrContainModifier(TestStr))
			    iRtn = 1;
		}	
		return(iRtn);
	}
}
