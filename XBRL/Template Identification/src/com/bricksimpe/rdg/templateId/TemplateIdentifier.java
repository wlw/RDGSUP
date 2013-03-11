package com.bricksimpe.rdg.templateId;


import java.io.*;
import java.util.ArrayList;
import java.sql.Connection;
import java.util.Date;

// import com.bricksimpe.rdg.util.EdgarExtractor;

public class TemplateIdentifier {

	/**
	 * @param 
	 * @param args
	 * 
	 */
    int iNoteCount;   // static
    private String privInputFile = "";
    private String privCreateOutputFile = "";
	private String privServerName = "";
	private String privPortNumber = "";
	private String privInstance = "";
	private String privDataBase = "";
    private String privUserName = "";
    private String privPassword = "";
    private int    privNewVersion = 0;
 	private Boolean useWindows = false;
	public void setPrivUseWindows(Boolean privOutputFile) {
		this.useWindows = privOutputFile;
	}
	public Boolean getPrivUseWindows() {
		return useWindows;
	}

	public int RunThis()
	{
		int   iRtn;
		
		iRtn = run(getPrivInputFile(), getPrivCreateOutputFile(), 
				getPrivServerName(), getPrivInstance(),
				getPrivPortNumber(),getPrivDataBase(),
				getPrivUserName(), getPrivPassword(), getPrivUseWindows(),
				getPrivNewVersion());
	    return(iRtn);
	}
	  
	public static void main(String[] args) {
		
		String                    Password = "";
		String                    InputFile = args[0];
		String                    CreateOutputFile = args[1];
		String                    ServerName = args[2];
		String                    PortNumber = args[3];
		String                    Instance = args[4];
		String                    DataBase = args[5];
		String                    UserName = args[6];
		boolean useWindows = false;
		if(args.length > 7)
			Password = args[7];
			useWindows = Boolean.valueOf(args[8]);
			int NewVersion = Integer.parseInt(args[9]);
		TemplateIdentifier main = new TemplateIdentifier();
		
		main.run(InputFile, CreateOutputFile, ServerName, Instance, PortNumber,
		    DataBase, UserName, Password, useWindows, NewVersion);
	}

	private int run(String InputFile, String CreateOutputFile, String ServerName,  // static
			                String Instance, String PortNumber, String DataBase,
			                String UserName, String Password, boolean useWindows, 
			                int Version) {
		Version                   thisVersion = new Version();
		ArrayList<TOC_STRINGS>    TOC_Strings = new ArrayList<TOC_STRINGS>();
		
		
		MySqlAccess               mysq = new MySqlAccess();
		Connection                con = null;
		String                    strLine;
		FileInputStream           fstream;
		DataInputStream           in;
		BufferedReader            br;
		ConfidenceLevel           cl = new ConfidenceLevel();
		int                       LineNumber = 0;
		int                       j = 0;
		double                    MaxConfidence = 0.0;
		int                       MaxConfidenceNdx = 0;
		double                    dConfidence = 0;
		MatchStr[]                AvailForms;
		MatchStr[]                FormKtags = null;;
		boolean                   bContinue;
		int                       iStartTOC = 0;
		String                    TocIdentifier = "";
		int                       iEndTOC = 0;
		TemplateIdStr[]           TemplateInfo = null;
		HtmlTraceItem             TraceItem = null;
		String                    NoteStr = "";
		SubmissionInfo            subInfo = null;
		String                    SeeNodeConst = "(See Note";
		int                       LastMatchIndex = 0;
		boolean                   bHaveStartOfTable = false;
		ArrayList<HtmlTraceItem>  TemplateTrace  =  new ArrayList<HtmlTraceItem>();
		ErrorCls                  errorCls = new ErrorCls();
		int                       iScale = 1;
		boolean                   bUnAudited = false;
		TemplateModifiers         templateModifiers;
		int                       iTrailingText = -3;   //in case no MD and A found
		boolean                   bFindTablesInNotes = true;  // WLW set to false until DB set up 
		boolean                   bSkipToEndTable = false;
		CheckForTemplateStr       checkForTemplateStr = new CheckForTemplateStr();
        boolean                   bSecondQuessMatch = false;
        boolean                   bFoundUserDefinedTag = false;
        ParsingOptions            parsingOptions = new ParsingOptions();
        boolean                   bDoingKform = false;
        boolean                   bUserDefed = false;
        FileInputStream           ustream = null;  
        DataInputStream           uIn = null;     
        BufferedReader            ubr = null;      
        UserDefineRefs            udr = new UserDefineRefs();
        int                       iPrevTableLength = -1;
        boolean                   bDetailedLogging  = false;
        Date                      beginTime = new Date();
        Date                      curTime = new Date();
        int                       iNoteId = 0;
        HtmSectionCls             htmSectionCls = new HtmSectionCls();
   	    int                       iLastLoggedLocation = 0;
   	    int                       iMaxLinesToDiscard = CONSTANTS.I_MAX_LINES_TO_DISCARD_TABLE;
   	    CONSTANTS                 constants = new CONSTANTS();
   	    
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("Run");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);
		errorCls.setErrorText(UserName + ": invoking Template version: " + thisVersion.getVersion());
		iNoteCount = 0;	
		checkForTemplateStr.Init();
		//tl = new ArrayList(cl.RecurringSting(TblOfCont)); // table of contents
		try {
		    con = mysq.OpenConnection(ServerName, Instance, PortNumber, DataBase, UserName, Password, useWindows);
		    if(con == null) 
		    	return(-1);
		    else {
		    	checkForTemplateStr.noteIdCls.InitCls(con);
				mysq.WriteAppError(con, errorCls);
			    if(htmSectionCls.FindUserInsertedTags(con, InputFile) != 0)
			    	return(-1);
			    fstream = new FileInputStream(InputFile);
			    in = new DataInputStream(fstream);
			    br = new BufferedReader(new InputStreamReader(in));
			    AvailForms = mysq.GetListFromTbls(con, 0);
			    TOC_Strings = mysq.GetTableOfContentStrings(con);
			    iNoteId = mysq.GetNoteId(con);
			    if(iNoteId == -1)
			    	return(-1);
                while(((strLine = br.readLine()) != null)  && (MaxConfidence < 1.0)) {
                	LineNumber++;
                	if(strLine.length() > 0) {
                		strLine = constants.RemoveHtmlCodeSaveCase(strLine);
                	    for(j = 0; j < AvailForms.length; j++) {
                	        dConfidence = cl.compareToArrayList(strLine, AvailForms[j].al);
                	        if(dConfidence > MaxConfidence) {
                	    	    MaxConfidence = dConfidence;
                	     	    MaxConfidenceNdx = j;
                	     	    checkForTemplateStr.noteIdCls.SetCheckForNotesFlag(AvailForms[j].bBooleanFlag);
                	        }
                	    }
                	}
                }
        		if(bDetailedLogging == true) {
        			curTime = new Date();
        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
        		    errorCls.setErrorText("Found Form Type: secs-" + dif);
        		    mysq.WriteAppError(con, errorCls);
        		}
                TemplateInfo = mysq.GetTemplateIdentifiers(con, AvailForms[MaxConfidenceNdx].key);
                // WLW just to kick the can down the road for the time being!! 11/30/11
                if(AvailForms[MaxConfidenceNdx].key == 2) {
                	bDoingKform = true;
                	FormKtags = mysq.GetFormTags(con, AvailForms[MaxConfidenceNdx].key);
                }
		    	subInfo = mysq.UpdateSubmissionsTable(con, InputFile, MaxConfidenceNdx, Version);
		    	if(subInfo == null) {
		    		return(-1);
		    	}
		    	errorCls.setSubUid(subInfo.getUid());
		    	errorCls.setItemVersion(subInfo.getVersion());
                subInfo.setMyForm(AvailForms[MaxConfidenceNdx].key);
                //now first template is the header portion - up to and including the table of contents
                br.close();
                in.close();
                fstream.close();
                LineNumber = 0;
			    fstream = new FileInputStream(InputFile);
			    in = new DataInputStream(fstream);
			    br = new BufferedReader(new InputStreamReader(in));
			    MaxConfidence = 0;
			    bContinue = true;
			    boolean bFoundTOC = false;
			    int iAttemptCounter = 0;
			    boolean bEndOfFile = false;
			    //int debuglineNum = 3040;
			    boolean bTrailingText = false;
    	    	UserDefinedTemplate TOCudt = new UserDefinedTemplate();
			    while ((bFoundTOC == false) && (bEndOfFile == false) && htmSectionCls.FoundRDGtags() == false) {
			        while((bContinue) && (bFoundTOC == false)) {
			        	LineNumber++;
			    	    strLine = br.readLine();
                        if(strLine == null) {  //EOF
                    	    bContinue = false;
			        		bEndOfFile = true;
                        }
                        else {  // ya got a line to test for the TOC  do not check MT lines
                        	if(strLine.length() > 0) {  // not MT line so check it
        			    	    iAttemptCounter = 0;
        			    	    if(bDoingKform == true) {
        	                	    for(j = 0; j < FormKtags.length; j++) {
        	                	        dConfidence = cl.compareToArrayList(strLine, FormKtags[j].al);
        	                	        if(dConfidence > 0.80) {
        	                	        	bFoundTOC = true;
        	                	        	iStartTOC = LineNumber;
        	                	        	j = FormKtags.length;  // this gets us out
        	                	        }
        	                	    }
        			    	    }
        			    	    else
        			    	    { //CHECKING FOR TABLE OF CONTENTS
        			    	        if(bFoundUserDefinedTag = TOCudt.IsUserDefinedTemplate(strLine)) {
        			    	    	    iStartTOC = LineNumber;   // this is 
        			    	    	    bFoundTOC = true;  // either user defined TOC or first template
        			    	    	    bSecondQuessMatch = true;  // no second quessing either
        			    	    	    int iTemplateId = TOCudt.GetUserDefinedTemplateId(strLine);
        			    	    	    if(iTemplateId != CONSTANTS.TableOfContentsId) {  // set up to begin templates
        			    	    		    iEndTOC = -1; // this is used
        			    	    	    }
        			    	    	    else  { // Table of Contents
        			    	    		    LineNumber = TOCudt.FindLastLineOfTemplate(br, LineNumber);
        			    	    		    iEndTOC = LineNumber -1;
        			    	    		    strLine = CONSTANTS.UserDefinedEndTemplate;
        			    	    	    }
        			    	        }
        			    	        else {
        			    	        	if(LineNumber > CONSTANTS.MINIMUMPREAMBLELINES) {
                       		                while((iAttemptCounter != TOC_Strings.size()) && (bFoundTOC == false)) {
                           		                dConfidence = cl.compareStrings(strLine, TOC_Strings.get(iAttemptCounter).getTocString());
                                                if(dConfidence > TOC_Strings.get(iAttemptCounter).getTocConfidence()) {
                      	                            bContinue = false;
                      	                            iStartTOC = LineNumber;
                      	                            bFoundTOC = true;
                      	                            TocIdentifier  = strLine;
                                                }
                                                else  {
                                    	            if(TOC_Strings.get(iAttemptCounter).getTocDoMatch() == true) {
                                    	                if(strLine.indexOf(TOC_Strings.get(iAttemptCounter).getTocString()) != -1) {
                                    	    	            bContinue = false;
                                    	    	            iStartTOC = LineNumber;
                                    	    	            TocIdentifier = " DEFAULTED TOC";
                                    	    	            bFoundTOC = true;
                                    	                }
                                    	            }
                                                }
                                                if(iStartTOC == 0)
                                	                iAttemptCounter++;
                                            }  // end while checking against TOC_STRINGS
        			    	        	}
        			    	        }
                                }
                        	} // end of ELSE not Form K
                        }
                    }
			    }  // end while looking for TOC
			    bContinue = true;  // set here so we can skip looking of end of TOC
				if(bDetailedLogging == true) {
					String temp = "Completed search for TOC: ";
					if(bFoundTOC == true)
						temp += " FOUND";
					else
						temp += " NOT FOUND";
        			curTime = new Date();
        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
				    errorCls.setErrorText(temp + " secs- " + dif);
				    mysq.WriteAppError(con, errorCls);
				}
		        if((bFoundTOC == false) && (htmSectionCls.FoundRDGtags() == false)) { // || (bDoingKform == true)) { // DID NOT FIND TOC  - check for template
	                br.close();  // close the file - will open if get TOC straightened out
	                in.close();
	                fstream.close();
			    	AlternateTocFinding ACF = new AlternateTocFinding();
			    	ACF.setFormType(subInfo.getMyForm());
			    	ACF.setFileName(InputFile);
			    	ACF.setConnection(con);
			    	ACF.FindTOC(con, bDoingKform, subInfo.getCompanyName());
			    	if(ACF.getSuccess() == false ) {
				    	//System.out.println("DID NOT FIND start of table of contents");
				    	//System.out.println("Should check for templates here");
			    	    return(-2);
			    	}
			    	else { 
			    		bContinue = ACF.getFindEndTOC();
			    		if(bDoingKform == false)   // we already have the start we just looking for end!!
			    		    iStartTOC = ACF.getBeginLineTOC();
			    		dConfidence = ACF.getMaxMatchValue();
			    		iEndTOC = ACF.getEndLineTOC();
			    		// now we position to the file line
					    fstream = new FileInputStream(InputFile);
					    in = new DataInputStream(fstream);
					    br = new BufferedReader(new InputStreamReader(in));
                        LineNumber = 0;
                        while(LineNumber < ACF.getCurrentLine() -1) {
                        	strLine = br.readLine();
                        	LineNumber++;
                        }
			    	}
			    }
	    	    if((bDoingKform == false) && (htmSectionCls.FoundRDGtags() == false)) {
		            if(bFoundUserDefinedTag == false) {  // all done with TOC 
		                int IdentifiedStrTblId = -1;
		                int SecondQuessMatchIndex = 0;
		                double SecondQuessMatchValue = 0.0;
		        
			            while(bContinue) {
			    	        LineNumber++;
							if(bDetailedLogging == true) {
								int modu = LineNumber/25;
								if(LineNumber == modu * 25) {
				        			curTime = new Date();
				        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
							        errorCls.setErrorText("Looking for End TOC: " + LineNumber  + " Secs -" + dif);
							        mysq.WriteAppError(con, errorCls);
								}
							}
			    	    //if(347 == LineNumber)
			    	    //	System.out.println("TEST");
			    	        strLine = br.readLine();
			    	        if(strLine == null) {
			    		        bContinue = false;
			    	        }
			    	        else {
			    		        if(constants.CheckForIdentifier(strLine, 0) != -1)  // check for <table> tag
			    			        checkForTemplateStr.SetLastTableTag(LineNumber);
			    		        dConfidence = cl.compareStrings(strLine, CONSTANTS.EndOfTblOfCont);
			    		        int iEndTableIndex = strLine.indexOf(CONSTANTS.EndOfTblOfCont);
			    		    //System.out.println("TESTING LINE: " + strLine);
			    		    //System.out.println("dConfidence: " + dConfidence);
			    		        if((dConfidence > 0.70) || (iEndTableIndex == 0)) {
			    			        if(constants.CheckForIdentifier(strLine, 1) >= 0) {
			    			            bContinue = false;
			    		                iEndTOC = LineNumber;
			    		                checkForTemplateStr.SetLastTableTag(0);
			    			        }
			    		        }
			    		        if(bContinue == true) { // haven't found end TOC yet - second guessing
                	                for(j = 0; j < TemplateInfo.length; j++) {
                	                    switch (TemplateInfo[j].getStartType()) {
                	                        case 0:
                    	                        dConfidence = cl.compareToArrayList(strLine, TemplateInfo[j].Al);
                    	                        if(dConfidence > SecondQuessMatchValue) {
                    	                	        SecondQuessMatchIndex = TemplateInfo[j].getStrTblId();
                    	                	        SecondQuessMatchValue = dConfidence;
                    	                        }
                                                break;
                	                    }
                	                }
                	                if(SecondQuessMatchValue > 0.8) { // got ourselves a match
                	        	        if(IdentifiedStrTblId == -1)  { // first one save it
                	        		        IdentifiedStrTblId = SecondQuessMatchIndex;
                	        		        SecondQuessMatchValue = 0.0;
                	        	        }
                	        	        else  {  // otherwise check if duplicate
                	        		        if(IdentifiedStrTblId == SecondQuessMatchIndex) { // yup
                	        			        if(checkForTemplateStr.GetLastTableTag() == 0) // check if we within <table>
                	        			            iEndTOC = LineNumber -1; // nope - table coming
                	        			        else
                	        				        iEndTOC = checkForTemplateStr.GetLastTableTag() -1;  
                	        			        bSecondQuessMatch = true;
                	        			        bContinue = false;
                	        		        }
                	        		    }
                	        	    }
                	            }
			    		    }
			    	    } // END OF WHILE
			        }  // END of bFoundUserDefinedTag = false
	    	    } // END of bDoingKform == false
			    if(htmSectionCls.FoundRDGtags() == true) {
			    	iStartTOC = htmSectionCls.GetEndOfPreamble();
			    }
                TraceItem = new  HtmlTraceItem();
                TraceItem.setBeginLine(0);
                TraceItem.setConfidenceLevel(dConfidence);
                TraceItem.setEndLine(iStartTOC -1);
                TraceItem.setTemplateId(CONSTANTS.PreambleId);
                TraceItem.setTermStr("");
                TraceItem.setIdentifiedText(" NONE - IDENTIFIED BY START OF TABLE OF CONTENTS");
                TemplateTrace.add(TraceItem);
				if(bDetailedLogging == true) {
        			curTime = new Date();
        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
				    errorCls.setErrorText("Completed TOC begin and end secs -" + dif);
				    mysq.WriteAppError(con, errorCls);
				}
                if(htmSectionCls.FoundRDGtags() == true) {
                	iStartTOC = htmSectionCls.GetEndOfPreamble() + 1;
                	iEndTOC = htmSectionCls.GetBeginOfSection() -1;
                	if(iEndTOC < iStartTOC)   // this is the case where there is no preamble or table of contents
                		iEndTOC = iStartTOC;
                }
                if((iStartTOC != iEndTOC) && (iEndTOC > 0)) {
                    TraceItem = new  HtmlTraceItem();
                    if(bFoundUserDefinedTag == false)
                        TraceItem.setBeginLine(iStartTOC);
                    else
                        TraceItem.setBeginLine(iStartTOC +1);
                    TraceItem.setConfidenceLevel(dConfidence);
                    TraceItem.setEndLine(iEndTOC);
                    if(bDoingKform == false) {
                        TraceItem.setTemplateId(CONSTANTS.TableOfContentsId);
                        TraceItem.setTermStr(CONSTANTS.EndOfTblOfCont);
                    }
                    else {
                        TraceItem.setTemplateId(CONSTANTS.DummySection);
                        TraceItem.setTermStr(CONSTANTS.FoundTemplate);                   	
                    }
                    TraceItem.setIdentifiedText(TocIdentifier);
                    TemplateTrace.add(TraceItem);
                }
		        checkForTemplateStr.Reset();
		        if(htmSectionCls.FoundRDGtags() == false) {
		            if((bFoundUserDefinedTag == false) && (bDoingKform == false)) { // we found User DEFINED template
		                checkForTemplateStr.SetBeginLineNum(iEndTOC + 1);
     	    	        if(bSecondQuessMatch == false) {// check if we are already positioned at start of template
    	    		        LineNumber++;
    	    		        strLine = br.readLine();
    	    		        //check if we have passed beginning of table
    	    		        if(checkForTemplateStr.GetLastTableTag() > 0) {  // Yup, set up to look for the </table>
    	    			        checkForTemplateStr.SetFoundTableBegin(true);
    	    			        checkForTemplateStr.SetBeginLineNum(checkForTemplateStr.GetLastTableTag());  // last know table tag
    	    			        checkForTemplateStr.SetFoundTemplateBegin(true);     // set already found start tag
    	    			        checkForTemplateStr.SetLastTableTag(0);
    	    		        }
    	    	        }
		            }
		            else {
		        	    if(iEndTOC > 0)
		        		    checkForTemplateStr.SetBeginLineNum(LineNumber);
		        	    else
		        		    checkForTemplateStr.SetBeginLineNum(LineNumber);
		            }
    	    	    bContinue  = true;
 		            //int iLastTemplateId = 0;
		            //String strLastTemplate = "";
    	    	    if(bDoingKform) { // skip to Item 8
    	    		    while (bContinue) {
    	    			    if(strLine == null)
    	    				    bContinue = false;
    	    			    if((strLine.indexOf("Item 8") == 0) ||
    	    			       (strLine.indexOf("RDGXBRLParseBegin") == 0))
    	    				    bContinue = false;
    	    			    else {
    	 	    		        strLine = br.readLine();
    	                        LineNumber++;
    	    			    }
    	    		    }
    	    		    if(strLine != null) {
    	                    TraceItem = new  HtmlTraceItem();
    	                    TraceItem.setBeginLine(iStartTOC);
    	                    TraceItem.setConfidenceLevel(dConfidence);
    	                    TraceItem.setEndLine(LineNumber);
    	                    TraceItem.setTemplateId(CONSTANTS.TableOfContentsId);
    	                    TraceItem.setTermStr("");
    	                    TraceItem.setIdentifiedText(" NONE - IDENTIFIED BY Item 8");
    	                    TemplateTrace.add(TraceItem);
    	    		    }
    	    	    }
		        }
		        else { // here we set up to read from the user inserted tag
		        	while(LineNumber < (htmSectionCls.GetBeginOfSection() + 1)) {
		        		strLine = br.readLine();
		        		LineNumber++;
		        	}
		        }
    	    	bContinue = true;
		        String extracted = "";
		        int    iIdLine = 0;   // this is the line in which we first determine templateName
		        int    iHeaderLines = 0;
   	    	    while(bContinue) {
    	    	    if((strLine == null) || ((htmSectionCls.GetEndOfSection() -1) == LineNumber))
    	    	    	bContinue = false;
    	    	    else {  // this check is for some trailing text that inadvertently starts a template
    	    	    	if(((checkForTemplateStr.GetFoundTemplateBegin() == true) && 
    	    	    	    (checkForTemplateStr.GetFoundTableBegin() == false)  && 
    	    	    	    (checkForTemplateStr.GetWithInNote() == false))) {
    	    	    		iHeaderLines += checkForTemplateStr.IsHeaderLine(strLine);
    	    	    		if(LineNumber > (checkForTemplateStr.GetIdLine() + 8 + iHeaderLines)) {
    	    	    			checkForTemplateStr.SetFoundTemplateBegin(false);
    	    	    		}
    	    	        }
    	    	    	else
    	    	    		iHeaderLines = 0;
    	    	    	//if(((LineNumber > (checkForTemplateStr.GetIdLine() + 8)) && 
    	    	    	//    (checkForTemplateStr.GetFoundTemplateBegin() == true) && 
    	    	    	//    (checkForTemplateStr.GetFoundTableBegin() == false)  && 
    	    	    	//    (checkForTemplateStr.GetWithInNote() == false))) {
    	    	    	//	checkForTemplateStr.SetFoundTemplateBegin(false);
    	    	    	//}
    	    	    	strLine = strLine.trim();
    	    	    	checkForTemplateStr.SetLastAccompanyingNotes(strLine, LineNumber);
			    	    if((strLine.length() > 0)  && (bTrailingText == false) && (bUserDefed == false)) {
							if(bDetailedLogging == true) {
								int modu = LineNumber/25;
								if(LineNumber == modu * 25) {
				        			curTime = new Date();
				        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
							        errorCls.setErrorText("Completed parsing Line: " + LineNumber  + " Secs -" + dif);
							        mysq.WriteAppError(con, errorCls);
								}
							}
			    	    	if(bSkipToEndTable == true) {
			    	    		if(constants.CheckForIdentifier(strLine, 1) != -1) {  // check here if previous table mark as that 
			    	    			checkForTemplateStr.UnMarkedTable(TemplateTrace, LineNumber);
			    	    			bSkipToEndTable = false;
			    	                iScale = 1;
			    	                bUnAudited = false;
			    	    		}
			    	    	}
			    	    	else {
			    	    		strLine = constants.RemoveLineJunk(strLine);
			    	    		checkForTemplateStr.AddNoteHeader(strLine);
			    	    	    if(constants.CheckForIdentifier(strLine, 0) != -1) {      
			    	    	    	iMaxLinesToDiscard = CONSTANTS.I_MAX_LINES_TO_DISCARD_TABLE;
			    	    	    	if(checkForTemplateStr.GetLastTableTag() + 25 < LineNumber) {  // this checks if we going through a  table to clear previous info
			    	    		    	//checkForTemplateStr.PreservePrevious();  // only saving in case we have a split section
			    	    		    	checkForTemplateStr.SetLastTemplateId(0);
			    	    		    	checkForTemplateStr.SetLastTemplate("");
			    	    		    }
			    	    		    //if(checkForTemplateStr.GetLastTableTag() > 0)
			    	    		    //    iPrevTableLength = LineNumber - checkForTemplateStr.GetLastTableTag();
			    	    		    checkForTemplateStr.SetLastTableTag(LineNumber);
			    	        		if((checkForTemplateStr.GetFoundTableBegin() == false) && 
			    	        		   (checkForTemplateStr.GetFoundTemplateBegin() == true) && 
			    	        		   (checkForTemplateStr.GetWithInNote() == false)) {
			    	        			checkForTemplateStr.SetBeginLineNum(LineNumber);
			    	    	    	}
			    	        		checkForTemplateStr.SetFoundTableBegin(true);
			    	    	    }
			    	    	    if(checkForTemplateStr.GetWithInNote() == true) {
			    	    	    	if(constants.CheckForIdentifier(strLine, 1) != -1)
			    	    	    		checkForTemplateStr.SetFoundTableBegin(false);
			    	    	    }
			    	    	    if((checkForTemplateStr.GetFoundTableBegin() == false) && (checkForTemplateStr.GetFoundTemplateBegin() == true)) { // check if table modifier
                            	    templateModifiers = new TemplateModifiers();
                            	    templateModifiers = CheckForTemplateModifiers(extracted, iScale, bUnAudited);
                            	    iScale = templateModifiers.iScale;
                            	    bUnAudited = templateModifiers.bUnAudited;
			    	    	    }
			    	    	    extracted = constants.RemoveHtmlCode(strLine);
			    	            if(checkForTemplateStr.GetFoundTemplateBegin() == false){ 
    	                    	    checkForTemplateStr.SetMaxMatchValue(0);
    	                    	    //if((extracted.indexOf("notes to") == -1) && (extracted.indexOf("see note") == -1)) {  // this was moved from below 5 lines
    	                    	    	checkForTemplateStr.SetFoundTemplateBegin(checkForTemplateStr.DoCheck(strLine, extracted, 
    	                    	    			                                  LineNumber, TemplateInfo, TemplateTrace, br,subInfo.getCompanyName()));
    	                    	    	if((checkForTemplateStr.GetFoundTemplateBegin() == true) && (checkForTemplateStr.GetFoundNotes() == true) && (checkForTemplateStr.GetFoundTableBegin() == true)) {
    	                    	    		checkForTemplateStr.SetBeginLineNum(checkForTemplateStr.GetLastTableTag());
    	                    	    	}
    	                    	    //}  // end of if see note
    	                            if(extracted.indexOf("notes to") != -1) {
    	                            	checkForTemplateStr.SetMayHaveFootNotes(false);
    	                            }
             	                    if(checkForTemplateStr.GetFoundTemplateBegin() == false) { //else check if we have a match on template
             	                    	if((checkForTemplateStr.GetLastTemplateId() > 0) && (checkForTemplateStr.GetFoundTableBegin() == true) 
             	                    		&& (iPrevTableLength < 15) && (iPrevTableLength != -1)) {
             	                    		checkForTemplateStr.SetMaxMatchValue(1.0);
             	                    	    //LastMatchIndex = checkForTemplateStr.GetLastMatchIndex();
             	                    		LastMatchIndex = checkForTemplateStr.GetPreservedMatchIndex();
             	                    		checkForTemplateStr.SetLastMatchIndex(LastMatchIndex);
             	                    		checkForTemplateStr.SetMatchedText(checkForTemplateStr.GetPreservedMatchedText());
             	                    	    checkForTemplateStr.SetFoundTemplateBegin(true);
             	                    	    checkForTemplateStr.SetBeginLineNum(LineNumber);
             	                    	}
                                        if(checkForTemplateStr.GetMaxMatchValue() > 0.80){
                                        	checkForTemplateStr.noteIdCls.SetCheckForNotesFlag(true);
                                   	        templateModifiers = new TemplateModifiers();
                                    	    templateModifiers = CheckForTemplateModifiers(extracted, iScale, bUnAudited);
                                    	    iScale = templateModifiers.iScale;
                                    	    bUnAudited = templateModifiers.bUnAudited;
                                    	    checkForTemplateStr.SetFoundTemplateBegin(true);
                                    	    checkForTemplateStr.SetMayHaveFootNotes(true);
                          	                checkForTemplateStr.SetWithInNote(false);
                         	                if(checkForTemplateStr.GetLastTableTag() == 0)
                         	                	checkForTemplateStr.SetBeginLineNum(LineNumber);
                         	                else
                         	                	checkForTemplateStr.SetBeginLineNum(checkForTemplateStr.GetLastTableTag());
                         	                LastMatchIndex = checkForTemplateStr.GetLastMatchIndex(); // WLW Added after make new CLASS
                                        }
                                        else  { // check if we are skipping a table
                                    	    if(checkForTemplateStr.GetFoundTableBegin() == true) {
                                    		    if(constants.CheckForIdentifier(strLine, 1) != -1) {
                                    		    	checkForTemplateStr.SetFoundTableBegin(false);                                  			
                                    		    }
                                    		    else {   // WLW checking for <table> without template
                                    		    	//iMaxLinesToDiscard = CONSTANTS.I_MAX_LINES_TO_DISCARD_TABLE;
                                    		    	// if we find the company name extend  
                                    		    	if(iMaxLinesToDiscard == CONSTANTS.I_MAX_LINES_TO_DISCARD_TABLE) { 
                                    		    		if(strLine.toLowerCase().contains(subInfo.getCompanyNameLc()))
                                    		    			iMaxLinesToDiscard += CONSTANTS.I_MAX_LINES_TO_DISCARD_TABLE/2;
                                    		    	}
                                    			    if((checkForTemplateStr.GetLastTableTag() + iMaxLinesToDiscard) <= LineNumber) { // we far enough away
                                    				    bSkipToEndTable = true;
                                    				    checkForTemplateStr.SetFoundTableBegin(false);
                                    			    }
                                    		    }
                                    	    }
                                        }
            	                    }
  			    	            }
                	            else {
                	            	if((checkForTemplateStr.GetFoundTableBegin() == false) && (checkForTemplateStr.GetWithInNote() == false)){  // haven't found table yet - check for replacement
        	                    	    //if((extracted.indexOf("notes to") == -1) && (extracted.indexOf("see note") == -1)) {  // this was moved from below 5 lines
        	                    	    	SavedCheckForTemplate savedCheckForTemplate = new SavedCheckForTemplate();
        	                    	    	savedCheckForTemplate.SaveCheckForTemplate(checkForTemplateStr);
               	                    	    checkForTemplateStr.SetMaxMatchValue(0);
       	                    	    	    checkForTemplateStr.DoCheck(strLine, extracted, LineNumber, TemplateInfo, TemplateTrace, br, subInfo.getCompanyName());
                                            if(checkForTemplateStr.GetBeginLineNum() == savedCheckForTemplate.GetBeginLineNum())
                                            	checkForTemplateStr.RestoreSaved(savedCheckForTemplate);
        	                    	    //}  // end of if see note
               	            	    }
                		            switch (TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTermType()) {
                		                case 0:
                		            	    String lowerStrLine = strLine.toLowerCase();
                		            	    if(checkForTemplateStr.GetMayHaveFootNotes() == false)
                		            	    	checkForTemplateStr.SetMayHaveFootNotes(checkForTemplateStr.noteIdCls.CheckForFootnote(strLine));
                                            if(lowerStrLine.indexOf(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTermStr()) >= 0) {
                                            	checkForTemplateStr.SetFoundTableBegin(false);
                                            	iPrevTableLength = LineNumber - checkForTemplateStr.GetBeginLineNum();
                                                TraceItem = new  HtmlTraceItem();
                                                if((checkForTemplateStr.GetLastTableTag() != 0) && 
                                                		(checkForTemplateStr.GetLastTableTag() < checkForTemplateStr.GetBeginLineNum()))
                                            	    TraceItem.setBeginLine(checkForTemplateStr.GetLastTableTag());
                                                else
                                                    TraceItem.setBeginLine(checkForTemplateStr.GetBeginLineNum());
                                                TraceItem.setConfidenceLevel(checkForTemplateStr.GetMaxMatchValue());
                                                TraceItem.setEndLine(LineNumber);
                                                TraceItem.setTemplateId(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTemplateId());
                                                TraceItem.setTermStr(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTermStr());
                                                TraceItem.setIdentifiedText(constants.RemoveTableTags(checkForTemplateStr.GetMatchedText()));
                                                TraceItem.setUnAudited(bUnAudited);
                                                TraceItem.setScale(iScale);
                                                TraceItem.setIdLine(iIdLine);
                                                checkForTemplateStr.SetScalePrev(iScale);
                                                iScale = 1;
                                                bUnAudited = false;
                                                checkForTemplateStr.noteIdCls.SetCheckForNotesFlag(true);
                                                TemplateTrace.add(TraceItem);
                                                checkForTemplateStr.SetSectionFound(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTemplateId());
                                                checkForTemplateStr.SetLastTemplateId(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTemplateId());
                                                checkForTemplateStr.PreservePrevious();
                                                checkForTemplateStr.SetLastTemplate(constants.RemoveTableTags(checkForTemplateStr.GetMatchedText()));
                                                checkForTemplateStr.SetFoundTemplateBegin(false);
                                                checkForTemplateStr.SetFoundTableBegin(false);
                                                checkForTemplateStr.SetMaxMatchValue(0);
                                            }
                                            break;
                                        
                		                case 1:
                		            	    if(checkForTemplateStr.GetAddNoteNextLine() == true) { // we are in note table with only note tag
                		            	    	checkForTemplateStr.SetAddNoteNextLine(false);
                		            		    checkForTemplateStr.SetMatchedText(checkForTemplateStr.GetMatchedText() + " " + strLine);
                		            	    }
                		            	    int iFoundNextSection =  checkForTemplateStr.noteIdCls.TestNote(extracted, checkForTemplateStr.GetFoundTableBegin(), subInfo.getCompanyName());
                		            	    boolean bFoundMDandA = CheckForItem2(extracted, bDoingKform);
                		            	    if( iFoundNextSection != 0) {
                		            	    	checkForTemplateStr.SetAddSectionText(extracted);
                		            	    	checkForTemplateStr.SetAddNoteNextLine(checkForTemplateStr.noteIdCls.IsTableNote(extracted, strLine));
                		            		    if(strLine.indexOf(SeeNodeConst) == -1) {                                       		
                                                    TraceItem = new  HtmlTraceItem();
                                                    TraceItem.setBeginLine(checkForTemplateStr.GetBeginLineNum());
                                                    TraceItem.setConfidenceLevel(1);
                                                    if(checkForTemplateStr.GetFoundTableBegin() == false) {
                                                        TraceItem.setEndLine(LineNumber -1);  // as this is the start of the next 
                                                        checkForTemplateStr.SetBeginLineNum(LineNumber);  // this is the start of this note
                                                    }
                                                    else {
                                                        TraceItem.setEndLine(checkForTemplateStr.GetLastTableTag() -1);  // as this is the start of the next 
                                                        checkForTemplateStr.SetBeginLineNum(checkForTemplateStr.GetLastTableTag());  // this is the start of this note
                                                    }
                                                    TraceItem.setTemplateId(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTemplateId());
                                                    TraceItem.setTermStr(NoteStr);
                                                    String Extracted = checkForTemplateStr.GetMatchedText().replace("&#160;", "");
                                                    TraceItem.setIdentifiedText(constants.RemoveTableTags(Extracted));
                                                    TraceItem.setNoteWithinTable(checkForTemplateStr.GetNoteWithinTable());
                                                    TraceItem.setIdLine(iIdLine);
                                                    checkForTemplateStr.SetNoteWithinTable(checkForTemplateStr.GetFoundTableBegin());
                                                    //bFoundTableBegin = false;
                                                    TemplateTrace.add(TraceItem);
                                                    checkForTemplateStr.SetMatchedText(strLine);  // this is for the next end!!
                   	            		            if((checkForTemplateStr.GetFoundTableBegin() == true) && (iFoundNextSection == 2)) {  // table note pick up next line just to make sure
                   	            		            	boolean bContinue1 = true;
                   	            		            	while(bContinue1) {
                	            		            	    strLine = br.readLine();
                	            		            	    strLine = constants.RemoveHtmlCodeSaveCase(strLine);
                	            		            	    checkForTemplateStr.BumpLinesRead();
                	            		            	    if(strLine.length() > 0) {
                	            		            	        checkForTemplateStr.SetMatchedText(checkForTemplateStr.GetMatchedText() + " " + strLine); 
                	            		            	        bContinue1 = false;
                	            		            	    }
                   	            		            	}
                	            		            }
                                                //NoteStr = BuildNoteString();
            	            	    	        //NoCaseStr2 = NoteStr.toLowerCase();
                   	            		            checkForTemplateStr.SetLastTableTag(0);
            	            	    	            checkForTemplateStr.SetFoundTemplateBegin(true);
            	            	    	            checkForTemplateStr.SetLastTemplateId(0);
            	            	    	            checkForTemplateStr.SetLastTemplate("");
                                        	    }
                                            }
                		            	    else {
                		            		    if(bFoundMDandA == true) {
                		            		    	if(parsingOptions.GetMandD_Last() == true)
                		            			        bTrailingText = true;        
                                                    TraceItem = new  HtmlTraceItem();
                                                    TraceItem.setBeginLine(checkForTemplateStr.GetBeginLineNum());
                                                    TraceItem.setConfidenceLevel(1);
                                                    if(bHaveStartOfTable == false) {
                                                        TraceItem.setEndLine(LineNumber -1);  // as this is the start of the next 
                                                        checkForTemplateStr.SetBeginLineNum(LineNumber);  // this is the start of this note
                                                    }
                                                    else {
                                                        TraceItem.setEndLine(checkForTemplateStr.GetLastTableTag() -1);  // as this is the start of the next 
                                                        checkForTemplateStr.SetBeginLineNum(checkForTemplateStr.GetLastTableTag());  // this is the start of this note
                                                    }
                                                    TraceItem.setTemplateId(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTemplateId());
                                                    TraceItem.setTermStr(NoteStr);
                                                    String Extracted = checkForTemplateStr.GetMatchedText().replace("&#160;", "");
                                                    TraceItem.setIdentifiedText(constants.RemoveTableTags(Extracted));
                                                    TraceItem.setNoteWithinTable(checkForTemplateStr.GetNoteWithinTable());
                                                    TraceItem.setIdLine(0);
                                                    TemplateTrace.add(TraceItem);
                                                    checkForTemplateStr.SetMatchedText(strLine);  // this is for the next end!!
                                                    checkForTemplateStr.SetLastTableTag(0);
            	            	    	            checkForTemplateStr.SetFoundTemplateBegin(true);
            	            	    	            checkForTemplateStr.SetLastTemplateId(0);
            	            	    	            checkForTemplateStr.SetLastTemplate("");
            	            	    	            checkForTemplateStr.SetWithInNote(false);  // WLW testing added with the above bTrailingText commented out!!
               		            		      }
                		            	}
               		    	            break;
               		    	            
               		                 }
                                     //checkForTemplateStr.SetMaxMatchValue(0);
                                     //WLW removed 7/13/11 with above
                                    //iLastTableTag = 0;
                                    iLastLoggedLocation = LineNumber;
               	                }
			    	    	}
			    	    }
			    	    if(strLine.contains(CONSTANTS.UserDefinedStartTemplate)) {
			    	    	if(bUserDefed == false) {
			    	    	    bUserDefed = true;
			    	            //int                       iHtmlLineCount = 0;
			    	    	    String FileName = mysq.GetSrcFile(con, subInfo.getUid(),
			    	    	                                             subInfo.getVersion());
			    	            ustream = new FileInputStream(FileName);
			    	            uIn = new DataInputStream(ustream);
			    	            ubr = new BufferedReader(new InputStreamReader(uIn));
			    	    	}
			    	    	UserDefinedTemplate udt = new UserDefinedTemplate();
                            checkForTemplateStr.SetLastTemplateId(udt.GetUserDefinedTemplateId(strLine));
                            //checkForTemplateStr.SetLastTemplate(constants.RemoveTableTags(checkForTemplateStr.GetMatchedText()));
                            checkForTemplateStr.SetLastTemplate(udt.GetUserDefinedDisplayName(strLine));
                            checkForTemplateStr.SetFoundTemplateBegin(false);
                            checkForTemplateStr.SetFoundTableBegin(false);
                            checkForTemplateStr.SetMaxMatchValue(0);
			    	    	//}
                            TraceItem = new  HtmlTraceItem();
                            //if(udt.GetUserDefinedTemplateId(strLine) == 4)
                            	//TraceItem.setBeginLine(LineNumber);
                            //else
                            TraceItem.setBeginLine(LineNumber +1);
                            TraceItem.setConfidenceLevel(1);
                            TraceItem.setUserDefined(bUserDefed);
                            TraceItem.setTemplateId(udt.GetUserDefinedTemplateId(strLine));
                            checkForTemplateStr.SetSectionFound(udt.GetUserDefinedTemplateId(strLine));
                            TraceItem.setIdentifiedText(udt.GetUserDefinedDisplayName(strLine));
                            if(TraceItem.getTemplateId() == 4)
                            	TraceItem.setNoteIndex(udt.GetNoteIndex(strLine));
                            LineNumber = udt.FindLastLineOfTemplate(br, LineNumber);
                            TraceItem.setEndLine(LineNumber - 1);
                            udr.FindUserDefinedSection(ubr, strLine);
                            TraceItem.setUserStart(udr.getBeginLine());
                            TraceItem.setUserEnd(udr.getEndLine());
                            TemplateTrace.add(TraceItem);
			    	    }
			    	    else  {  // check here for Item 2 IF???
			    	    	if(checkForTemplateStr.GetFoundNotes()) {
			    	    	//if((bUserDefed == true) && (bUserDefed == true) && 
			    	    	//   (checkForTemplateStr.GetFoundTemplateBegin() == false)) {
			    	    		if(CheckItem(strLine) == true)
			    	    		    bTrailingText = true;  
			    	    	}
			    	    }
    	    	    }
			    	if(checkForTemplateStr.GetLinesRead() > 0) {  // if already processed
 	                    LineNumber += checkForTemplateStr.GetLinesRead();       // and bump to line counter
			    		checkForTemplateStr.SetLinesRead(0);      // clear read lines
			    	}
  	    		    strLine = br.readLine();
                    LineNumber++;
			    }
				if(bDetailedLogging == true) {
        			curTime = new Date();
        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
				    errorCls.setErrorText("Completed entire file secs - " + dif);
				    mysq.WriteAppError(con, errorCls);
				}
				/********/
   	    	    if((bTrailingText == false) && (bUserDefed  == false)) {
			        if(((iLastLoggedLocation + 1) < (LineNumber -1))
			    	    || (checkForTemplateStr.GetFoundTemplateBegin() == true)) {  // trailer and did not terminate
                        TraceItem = new  HtmlTraceItem();
                        if((checkForTemplateStr.GetFoundTemplateBegin() == true) || (iLastLoggedLocation == 0))
                            TraceItem.setBeginLine(checkForTemplateStr.GetBeginLineNum());
                        else
                            TraceItem.setBeginLine(iLastLoggedLocation);
                        TraceItem.setConfidenceLevel(1);
                        TraceItem.setEndLine(LineNumber -1);
                        if(checkForTemplateStr.GetFoundTemplateBegin() == false)
                    	    TraceItem.setTemplateId(iTrailingText);
                        else {
                        	if(checkForTemplateStr.GetWithInNote())
                        		TraceItem.setTemplateId(4);
                        	else
                                TraceItem.setTemplateId(TemplateInfo[j].getTemplateId());
                        }
                        TraceItem.setTermStr("EOF");
                        TraceItem.setIdentifiedText(constants.RemoveTableTags(checkForTemplateStr.GetMatchedText()));
                        TraceItem.setUnAudited(bUnAudited);
                        TraceItem.setScale(iScale);
                        TraceItem.setIdLine(iIdLine);
                        TemplateTrace.add(TraceItem);
			        }
			    }
			
				/*********/
				if(htmSectionCls.GetBeginOfSection() != 0) {
					if(udr.FindFinancialSchedule(br, LineNumber) == true) {
                        TraceItem = new  HtmlTraceItem();
                        //if(udt.GetUserDefinedTemplateId(strLine) == 4)
                        	//TraceItem.setBeginLine(LineNumber);
                        //else
                        TraceItem.setBeginLine(udr.getBeginLine() + 1);
                        TraceItem.setConfidenceLevel(1);
                        TraceItem.setUserDefined(bUserDefed);
                        TraceItem.setTemplateId(4);
                        TraceItem.setIdentifiedText(CONSTANTS.FinancialScheduleBegin);
                        TraceItem.setNoteIndex(0);
                        //LineNumber = udt.FindLastLineOfTemplate(br, LineNumber);
                        TraceItem.setEndLine(udr.getEndLine() - 1);
                        //udr.FindUserDefinedSection(ubr, strLine);
                        TraceItem.setUserStart(0);
                        TraceItem.setUserEnd(0);
                        TemplateTrace.add(TraceItem);
						
					}
				}
   	    	    //errorCls.setErrorText("Writing parsed: UId: " + subInfo.getUid() + ":: Version:  " + 
   	    	    //		               subInfo.getVersion() + " COUNT: " + TemplateTrace.size());
   		    	//MySqlAccess.WriteAppError(con, errorCls);
				if(bDetailedLogging == true) {
        			curTime = new Date();
        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
				    errorCls.setErrorText("Writing template recs secs -" + dif);
				    mysq.WriteAppError(con, errorCls);
				}
 	    	    
                ArrayList<TemplateNotes> Notes = mysq.WriteTemplateInfo(con, subInfo, TemplateTrace, bUserDefed);
                InsertIntoNotesTbl(con, subInfo, mysq, Notes);
                br.close();
                in.close();
                fstream.close();
                if(bFindTablesInNotes == true)
                FindTablesInNotes(InputFile, subInfo, con, TemplateTrace, Notes);
                if(bUserDefed == true) {
                	ubr.close();
                	uIn.close();
                	ustream.close();
                }
                /*************** no longer required *****************/
                //if(CreateOutputFile.compareTo("Y") == 0)
                //	CreateOutputFile(con, subInfo, MaxConfidenceNdx, InputFile, TemplateTrace);
				if(bDetailedLogging == true) {
        			curTime = new Date();
        			long dif = (curTime.getTime() - beginTime.getTime())/1000;
				    errorCls.setErrorText("ALL DONE - EXIT TOTAL ELAPSED SECS: " + dif);
				    mysq.WriteAppError(con, errorCls);
				}
				checkForTemplateStr.IsNotesEnabled(con, errorCls, true);
				TemplateLogger tl = new TemplateLogger();
				tl.LogIdentifiedSections(con, errorCls, subInfo);
				errorCls.setErrorText(UserName + ": Exit Template version: " + thisVersion.getVersion());
                mysq.WriteAppError(con, errorCls);
				mysq.CloseConnection(con);
		    }
		}
		catch (Exception e) {
		    //System.out.println("Unable to parse input file:" + e.getMessage());	
	     	errorCls.setErrorText("Unable to parse input file:"  + e.getMessage());
	    	mysq.WriteAppError(con, errorCls);
		}
		return(0);
	}
	
	
	private boolean CheckItem(String extracted) {  // static
		String  lowcase = extracted.toLowerCase();
		boolean bRtn = false;
		
		if(lowcase.indexOf("item 2") == 0) {
			bRtn = true;
		}
		return(bRtn);
	}
	
	private boolean CheckForItem2(String extracted, boolean bDoingKform) {  // static
	    boolean bRtn = false;
	    
	    if(extracted.indexOf("item ") == 0) {  // added space as 'items' was found
	    	if(bDoingKform == true)
	    		bRtn = true;
	    	else {
	    	    extracted = extracted.substring(4).trim();
	    	    if(extracted.indexOf("2") == 0)
	    		   bRtn = true;
	    	}
	    }
	    return(bRtn);
	}
	
	
	private TemplateModifiers CheckForTemplateModifiers(String strLine, int iScale, boolean bUnAudited) {  // static
		TemplateModifiers rtnCls = new TemplateModifiers();
		
		rtnCls.iScale = iScale;
		rtnCls.bUnAudited = bUnAudited;
		String lowerStrLine = strLine.toLowerCase();
		if(lowerStrLine.indexOf("in thousands") != -1)
			rtnCls.iScale = 1000;
		if(lowerStrLine.indexOf("unaudited") != -1)
			rtnCls.bUnAudited = true;
	    return (rtnCls);
	}

	private void FindTablesInNotes(String InputFile, SubmissionInfo si, Connection con,    // static
			                              ArrayList<HtmlTraceItem> TemplateTrace, ArrayList<TemplateNotes> Notes) {
		
		FileInputStream           fstream;
		DataInputStream           in;
		BufferedReader            br;
		ErrorCls                  errorCls = new ErrorCls();
        String                    curLine = "";
        int                       curLineNum = 1;
        int                       TableCounter;
        int                       iNotesIndex = 0;
        int                       iNoteUid = 0;
        int                       iLookFor;
        int                       iTblBeginLine = 0;
        int                       iTblEndLine;
        boolean                   bDefinitionTbl = false;
        MySqlAccess               mySql = new MySqlAccess();
        CONSTANTS                 constants = new CONSTANTS();
        int                       iCharacterCount = 0;
        boolean                   bIsLeadingTable = false;
        
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("FindTablesInNotes");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);
         try {
	        fstream = new FileInputStream(InputFile);
	        in = new DataInputStream(fstream);
	        br = new BufferedReader(new InputStreamReader(in));
	        for(int ii =0; ii < TemplateTrace.size(); ii++) {
	        	if(TemplateTrace.get(ii).getTemplateId() == 4) { // we got a note
	        		TableCounter = 1;
	        		iNoteUid = Notes.get(iNotesIndex).getTemplateParseUid();  // UID of note in templateparse
	        		iNotesIndex++;
	        		for(; curLineNum < TemplateTrace.get(ii).getBeginLine(); curLineNum++)
	        			br.readLine();
	        		//br.readLine();   // skip the first as it may be a <table>  NOPE FOR NOW WE WILL SEE
	        		//curLineNum++;
	        		iLookFor = 0;
	        		int iNoteIndex = mySql.GetNoteIndex(con, iNoteUid);
	        		for(; curLineNum <= TemplateTrace.get(ii).getEndLine(); curLineNum++) {
	        			curLine = br.readLine();
	        			if(curLine != null) {  // check for end of file
	        			    switch (iLookFor) {
	        			        case 0:
	        			    	    if(constants.CheckForIdentifier(curLine, 0) != -1) {
	        			    	    	iCharacterCount = 0;
	        			    	    	if(curLineNum == TemplateTrace.get(ii).getBeginLine())
	        			    	    		bIsLeadingTable = true;
	        			    	    	else
	        			    	    		bIsLeadingTable = false;
	        			    		    if(constants.CheckForIdentifier(curLine, 1) == -1) { // if</table> then <table></table> IGNORE
	        			    		        iLookFor = 1;
	        			     		        iTblBeginLine = curLineNum;
	        			    		        if(curLineNum ==  TemplateTrace.get(ii).getBeginLine())
	        			    			        bDefinitionTbl = true;
	        			    		        else
	        			    			       bDefinitionTbl = false;
	        			    		    }
	        			    	    }
	        			    	    break;
	        			        case 1:
	        			        	iCharacterCount += curLine.length();
	        			    	    if(constants.CheckForIdentifier(curLine, 1) != -1) {
	        			    		    iLookFor = 0;
	        			    		    iTblEndLine = curLineNum;
	        			    		    if((curLineNum == TemplateTrace.get(ii).getEndLine()) && (bDefinitionTbl == true))  // if entire note is this table  parse it later
	        			    		    		bDefinitionTbl = false;
	        			    		    else {
	        			    		    	if((bDefinitionTbl == true) && (iCharacterCount > 200) && (bIsLeadingTable == true))
	        			    		    		bDefinitionTbl = false;
	        			    		    }
	        			    		    mySql.AddNoteTable(con, si, iNoteUid, TableCounter, iNoteIndex, iTblBeginLine, iTblEndLine, bDefinitionTbl);
	        			    		    TableCounter++;
	        			    	    }
	        			    	    break;
	        			    }
	        			}
	        		}
	        		
	        	}
	        }
	        br.close();
	        in.close();
	        fstream.close();
		}
		catch (Exception e) {
	     	errorCls.setErrorText("Unable to parse input file:"  + e.getMessage());
	    	mySql.WriteAppError(con, errorCls);
		}

	}
	private void InsertIntoNotesTbl(Connection con, SubmissionInfo subInfo, MySqlAccess mysq,   // static
			                               ArrayList<TemplateNotes> notes) {
		double               dConfidence;
		double               MaxMatchValue;
		int                  lastMatchIndex;
		ArrayList<NodeIdStr> NodeList = mysq.GetNodeIdentifiers(con, subInfo.getCompanyId());
		ConfidenceLevel      cl = new ConfidenceLevel();
		int                  NoteGaapUid;
	
		for(int j = 0; j < notes.size(); j ++) {
			lastMatchIndex = -1;
			MaxMatchValue = 0;
			NoteGaapUid = 0;
			for(int k = 0; k < NodeList.size(); k++) {
				dConfidence = cl.compareToArrayList(notes.get(j).getIdentifiedText(), NodeList.get(k).Al);
				if(dConfidence > MaxMatchValue) {
					lastMatchIndex = k;
					MaxMatchValue = dConfidence;
				}
			}
			if(MaxMatchValue > 0.85) {
				NoteGaapUid = NodeList.get(lastMatchIndex).getUid();
			}
			mysq.insertNewNotesRecord(con, subInfo, notes.get(j), NoteGaapUid);
		}
	}
	public void setPrivInputFile(String inputFile) {
		privInputFile = inputFile;
	}

	public String getPrivInputFile() {
		return privInputFile;
	}

	public void setPrivCreateOutputFile(String privCreateOutputFile) {
		this.privCreateOutputFile = privCreateOutputFile;
	}

	public String getPrivCreateOutputFile() {
		return privCreateOutputFile;
	}
	public void setPrivServerName(String privServerName) {
		this.privServerName = privServerName;
	}
	public String getPrivServerName() {
		return privServerName;
	}
	public void setPrivPortNumber(String privPortNumber) {
		this.privPortNumber = privPortNumber;
	}
	public String getPrivPortNumber() {
		return privPortNumber;
	}
	public void setPrivDataBase(String privDataBase) {
		this.privDataBase = privDataBase;
	}
	public String getPrivDataBase() {
		return privDataBase;
	}

	public void setPrivUserName(String privUserName) {
		this.privUserName = privUserName;
	}

	public String getPrivUserName() {
		return privUserName;
	}

	public void setPrivPassword(String privPassword) {
		this.privPassword = privPassword;
	}

	public String getPrivPassword() {
		return privPassword;
	}

	public void setPrivInstance(String privInstance) {
		this.privInstance = privInstance;
	}

	public String getPrivInstance() {
		return privInstance;
	}
	
	public void setPrivNewVersion(int privVersion) {
		this.privNewVersion = privVersion;
	}

	public int getPrivNewVersion() {
		return privNewVersion;
	}
}
