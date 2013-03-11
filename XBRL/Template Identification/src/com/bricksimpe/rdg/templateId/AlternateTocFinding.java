package com.bricksimpe.rdg.templateId;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

public class AlternateTocFinding {
    private String FileName;
    private int FormType;
    private int BeginLineTOC;
    private int EndLineTOC;
    private int CurrentLine;
    private boolean bSuccess;
    private boolean bFindEndTOC;
    private Connection Con;
    private double MaxMatchValue;
    
    public void AlternateTocFinding() {
    	FileName = "";
        BeginLineTOC = 0;
        EndLineTOC = 0;
        CurrentLine = 0; 
        bSuccess = false;
        bFindEndTOC = false;
        MaxMatchValue = 0.0;
   	
    }
    
    public void AlternateTocFinding(String inFileName, Connection inCon, int inForm) {
    	FileName = inFileName;
    	Con = inCon;
    	FormType = inForm;
        BeginLineTOC = 0;
        EndLineTOC = 0;
        CurrentLine = 0; 
        bSuccess = false;
        bFindEndTOC = false;
   	
    }
    
    public void setFileName(String inFileName) {
    	FileName  = inFileName;
    }
    
    public void setConnection(Connection inCon){
    	Con = inCon;
    }
    
    public void setBeginLineTOC(int inBeginLineTOC) {
    	BeginLineTOC = inBeginLineTOC;
    }
    
    public void setFormType(int inForm) {
    	FormType = inForm;
    }
    
    public int getFormType() {
    	return(FormType);
    }
    
    public int getBeginLineTOC() {
    	return(BeginLineTOC);
    }
    
    public void BumpBeginLineTOC() {
    	BeginLineTOC++;
    }

    public void setEndLineTOC(int inEndLineTOC) {
    	EndLineTOC = inEndLineTOC;
    }
    
    public int getEndLineTOC() {
    	return(EndLineTOC);
    }
    
    public void BumpEndLineTOC() {
    	EndLineTOC++;
    }
  
    public void setCurrentLine(int inCurrentLine) {
    	CurrentLine = inCurrentLine;
    }
    
    public int getCurrentLine() {
    	return(CurrentLine);
    }
    
    public void BumpCurrentLine() {
    	CurrentLine++;
    }
  
    public void BumpFileLineCnts(boolean bBumpBeginLineTOC) {
	    if(bBumpBeginLineTOC) 
		    BumpBeginLineTOC();
	    BumpEndLineTOC();
	    BumpCurrentLine();
    }
 
    public void setSuccess(boolean inSuccess) {
 	    bSuccess = inSuccess;
    }
 
    public boolean getSuccess() {
 	    return(bSuccess);
    }
 
    public void setFindEndTOC(boolean inFindEndTOC) {
	 	bFindEndTOC = inFindEndTOC;
	}
	 
	public boolean getFindEndTOC() {
	 	return(bFindEndTOC);
	}

	public double getMaxMatchValue() {
		return(MaxMatchValue);
	}
	
    public void FindTOC(Connection con, boolean bDoingKform, String companyName) {
		MySqlAccess     mysq = new MySqlAccess();
		TemplateIdStr[] TemplateInfo = null;
		FileInputStream fstream;
		DataInputStream in;
		BufferedReader  br;
		boolean         bContinue = true;
        String          CurLine;
        int             CurLineNum = 0;
		int             iIndex;
		String          TblStr = "<table>";
		int             TblLineNum = 0, LastMatchIndex;
		String          EndTblStr = "</table>";
		double          dConfidence;
		ConfidenceLevel cl = new ConfidenceLevel();
	    boolean         bFoundTemplateBegin = false;
		NoteIdCls       noteIdCls = new NoteIdCls();
		CONSTANTS       constants = new CONSTANTS();
		int             iLastOpenTableLineNum = 0;
		
	    noteIdCls.InitCls(con);   
        TemplateInfo = mysq.GetTemplateIdentifiers(Con, FormType);
        try {
	        fstream = new FileInputStream(FileName);
	        in = new DataInputStream(fstream);
	        br = new BufferedReader(new InputStreamReader(in));
	        int jj = 0;
	        boolean bTestLine = true;
	        
	        while(bContinue) {
	        	CurLine = br.readLine();
	        	if(CurLine == null)
	        		break;
	        	if((CurLine.length() > 0) && (CurLine.length() < 75)) {
	        	    iLastOpenTableLineNum = CheckForTableTag(CurLine, CurLineNum + 1, iLastOpenTableLineNum);
	        	    if(CurLineNum > 2022)
	        	    	jj = CurLineNum;
	        	    // we check here if we are within a table and beyond line count to not check
	        	    // also must be 10-K type as there is extra text 
	        	    bTestLine = true;  // just for clarity
	        	    if(bDoingKform == true) { // only skip if doing the K type
	        	    	if(iLastOpenTableLineNum != 0) {  // if 0 we are outside of <table>
	        	    		if(iLastOpenTableLineNum + CONSTANTS.MAX_LINES_OFF_START < CurLineNum)
	        	    			bTestLine = false;
	        	    	}
	        	    }        	    
	        	    if(bTestLine == true) {	
	        	    	MaxMatchValue = 0;
	        	    	CurLine = constants.RemoveHtmlCode(CurLine);
    	                for(int j = 0; j < TemplateInfo.length; j++) {
    	                    switch (TemplateInfo[j].getStartType()) {
    	                        case 0:
        	                        dConfidence = cl.compareToArrayList(CurLine, TemplateInfo[j].Al);
        	                        if(dConfidence > MaxMatchValue) {
        	                	        LastMatchIndex = j;
        	                	        MaxMatchValue = dConfidence;
         	                        }
                                    break;
                        
    	                        case 1:  // found 'note' now is it our 'Note n.' ??
    	                        	if(FormType != 2) {
    	                    	        if(noteIdCls.TestNote(CurLine, false, companyName) != 0) {
    	            		                bFoundTemplateBegin = true;
    	                    	        }
    	                        	}
    	            	            break;
    	            	
    	                        case 2:  // looking for this string
    	                    	    if(FormType != 2) {
    	            	                if(CurLine.indexOf(TemplateInfo[j].getTempStr()) >= 0) {
    	            		                bFoundTemplateBegin = true;
    	            		                dConfidence = 1.0;
    	            		                bContinue = false;
    	            	                }
    	            	            }
    	            	            break;
   	                        }  // end of switch
    	                    if(bFoundTemplateBegin)  { // found NOTE so exit 
    	            	        LastMatchIndex = j;
       	        	            break;   // break out of the FOR loop
   	                        }                	            	
 	                    }
	        	    }  // end of bTestLine
 	                if(bFoundTemplateBegin == false) { //else check if we have a match on template
                        if(MaxMatchValue > 0.8) {
             	            bFoundTemplateBegin = true;
             	            bContinue = false;
            	            //iBeginLineNum = CurLineNum;
                        }
	                }
	        	}
 	            CurLineNum++;
	        } // end while
	        if(bFoundTemplateBegin) {
	        	
	        	if(iLastOpenTableLineNum == 0) {  // no TOC
	                BeginLineTOC = CurLineNum -1;
	                EndLineTOC =  CurLineNum -1;
	                CurrentLine =  CurLineNum; 
	                bSuccess = true;
	                bFindEndTOC = false;
	        	}
	        	else   {  // this is a TOC line
	                BeginLineTOC = iLastOpenTableLineNum -1;
	                EndLineTOC =  iLastOpenTableLineNum -1;
	                CurrentLine =  iLastOpenTableLineNum; 
	                bSuccess = true;
	                bFindEndTOC = false;
	        	}
	        }
	        br.close();
	        in.close();
	        fstream.close();
        }
        catch (Exception e) {
        	System.out.println("Error finding alternate TOC: "  + e.getMessage());
    		ErrorCls    errorCls = new ErrorCls();
    		
    		errorCls.setCompanyUid(0);
    		errorCls.setFunctionStr("FindTOC");
    		errorCls.setItemVersion(0);
    		mysq.WriteAppError(con, errorCls);
       }
    	
    }
    int iNoteCount = 0;   // static
    
	
	private int CheckForTableTag(String curLine, int curLineNum, int prevLineNum) {  // static
		int rtnInt = prevLineNum;
		String lowerCased = curLine.toLowerCase();
		
		if(lowerCased.indexOf("<table>") != -1) 
				rtnInt = curLineNum;
		else {
			if(lowerCased.indexOf("</table>") != -1) 
				rtnInt = 0;	
		}
		return(rtnInt);		
	}
    private String BuildNoteString() {  // static
    	String RtnStr = "";
    		
     	iNoteCount++;
     	RtnStr = "Note " + iNoteCount;
     	return (RtnStr);
    }
}
