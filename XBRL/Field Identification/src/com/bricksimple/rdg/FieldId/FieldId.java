package com.bricksimple.rdg.FieldId;


import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
//import java.util.Arrays;
import java.sql.*;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import com.bricksimple.rdg.FieldId.FieldMatchStr;
import java.io.InputStreamReader;

public class FieldId {

	/**
	 * @param 
	 * @param args
	 * 
	 */
	private String privInputFile = "";
	private String privServerName = "";
	private String privInstance = "";
	private String privPortNumber = "";
	private String privDataBase = "";
	private String privUserName = "";
	private String privPassword = "";
	private Boolean useWindows = false;
	public void setPrivUseWindows(Boolean privOutputFile) {
		this.useWindows = privOutputFile;
	}
	public Boolean getPrivUseWindows() {
		return useWindows;
	}
	public int RunThis()
	{   
		int iRtn;
		
		iRtn = run(getPrivInputFile(), getPrivServerName(), getPrivInstance(), getPrivPortNumber(),
			getPrivDataBase(), getPrivUserName(),getPrivPassword(),getPrivUseWindows());
		return(iRtn);
	}
	

	public static void main(String[] args) {
		
		String InputFile  = args[0];
		String ServerName = args[1];
		String PortNumber = args[2];
		String Instance = args[3];
		String DataBase = args[4];
		String UserName = args[5];
		String Password = "";
		Boolean useWindows = false;
		if(args.length > 6) {
			Password = args[6];
			useWindows = Boolean.valueOf(args[7]);
		}
	    FieldId        main = new FieldId();
	    
		main.run(InputFile, ServerName, Instance, PortNumber, DataBase, UserName, Password, useWindows);
	}

	private int run(String InputFile, String ServerName, String Instance, String PortNumber,  // static
			                String DataBase, String UserName, String Password, Boolean useWindows) {
		
		Version                         thisVersion = new Version();
		Connection                      con = null;
		FileInputStream                 fstream2;
		DataInputStream                 in2;
		BufferedReader                  br2;
		SubmissionInfo                  SubInfo = null;
		int                             CurrentLineNumber = 1;
		ErrorCls                        errorCls = new ErrorCls();
		boolean                         parseNotes = true;   // WLW set to true to parse the note templates
		int                             iPrevVersion;
		int                             iRtn = 0;
		CONSTANTS                       constants = new CONSTANTS();
		MySqlAccess                     mySqlAccess = new MySqlAccess();
		ArrayList<HtmlReplacementLines> hrlList = new ArrayList<HtmlReplacementLines>();
        int                             identifiedSrcHtml = 0;
        IdentifiedHtmlSource            identifiedHtmlSource = new IdentifiedHtmlSource();
        
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("Run");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);
		errorCls.setErrorText(UserName + ": invoking FieldId version: " + thisVersion.getVersion());
		
		try {
		    con = mySqlAccess.OpenConnection(ServerName, Instance, PortNumber, DataBase, UserName, Password, useWindows);
		    if(con == null)
		    	return(-1);
		    mySqlAccess.WriteAppError(con, errorCls);
		    SubInfo = mySqlAccess.ReadFieldSubmissionInfo(con, InputFile);
		    identifiedSrcHtml = identifiedHtmlSource.FindSourceOfHtml(SubInfo.getHtmlFile());
		    constants.PopulateTextConversionTable(con);
		    errorCls.setSubUid(SubInfo.getUid());
		    errorCls.setCompanyUid(SubInfo.getCompanyId());
		    errorCls.setItemVersion(SubInfo.getCompanyId());
		    iPrevVersion = SubInfo.getVersion() -1;    // WLW only temporary until versioning
		    mySqlAccess.WriteCik(con, SubInfo, iPrevVersion);
		    fstream2 = new FileInputStream(SubInfo.getExtractFile());
		    in2 = new DataInputStream(fstream2);
		    br2 = new BufferedReader(new InputStreamReader(in2));
	        CurrentLineNumber = FindFieldsInPreamble(con, SubInfo, CurrentLineNumber, br2, identifiedSrcHtml);
	        CurrentLineNumber = SkipPastTOC(con, SubInfo, CurrentLineNumber, br2);
	        CurrentLineNumber = FindFieldsInTables(con, SubInfo, CurrentLineNumber, br2, parseNotes, hrlList, identifiedSrcHtml);
		    in2.close();
		    //ResolveDupTemplates  resolveDupTemplates = new ResolveDupTemplates();
		    //resolveDupTemplates.Resolve(con, SubInfo);
		    //if (bDoParentheticals == true)
		    //    parenthicalCls.InsertParenticals(con, SubInfo);
		    if(mySqlAccess.DoZeroDateRefsExist(con, SubInfo)) {
		    	iRtn = -3;
		    	errorCls.setErrorText("Zero DateRefs Detected");
		    	mySqlAccess.WriteAppError(con,  errorCls);
		    }
		    else {
		    	iRtn = 0;
		    	//for(int j = 0; j < hrlList.size(); j++) {
		    	//	errorCls.setErrorText("HrlList[" + j + "] = " + hrlList.get(j).GetFact());
			    //	mySqlAccess.WriteAppError(con, errorCls);
		    	//}
		    	if(hrlList.size() > 0) {
		    		MarkNoteFactsActive(con, hrlList);
			    	//errorCls.setErrorText("HrlList size = " + hrlList.size());
			    	//errorCls.setBExit(false);
			    	//mySqlAccess.WriteAppError(con, errorCls);
		    		iRtn = ModifyHtmlFile(con, SubInfo, hrlList);
		    	}
		    }
			errorCls.setErrorText(UserName + ": Exit FieldId version: " + thisVersion.getVersion());
			mySqlAccess.WriteAppError(con,  errorCls);
		    con.close();
		}
		catch (Exception e) {
			 //System.out.println("Unable to parse input file:" + e.getMessage());	
		     errorCls.setErrorText("Unable to parse input file:"  + e.getMessage());
		     errorCls.setBExit(false);
		     mySqlAccess.WriteAppError(con, errorCls);
		     iRtn = -2;
	    }
		return(iRtn);
	}

	private void MarkNoteFactsActive(Connection con,  ArrayList<HtmlReplacementLines> hrlList) {
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		for(HtmlReplacementLines hrl: hrlList) {
			if((hrl.GetMatchType() == 2) || (hrl.GetMatchType() == 3))
			    mySqlAccess.MarkFactStatus(con, hrl.GetFactUid());
		}
	}
	
	private int ModifyHtmlFile(Connection con, SubmissionInfo SubInfo, ArrayList<HtmlReplacementLines> hrlList) {
	    //String                     OrigHtmlFile = "";
	    MySqlAccess                mySqlAccess = new MySqlAccess();
	    String                     ModHtmlFile = "";
	    int                        iLineNum = 0;
	    int                        iNdx = 0;
	    String                     fileLine = "";
	    ErrorCls                   errorCls = new ErrorCls();
	    int                        iRtn = 0;
		FileInputStream            fstream;
		FileOutputStream           ostream;
		DataInputStream            in;
		DataOutputStream           out;
		BufferedWriter             wr;
	    File                       file;
	    File                       filefrom;
	    ArrayList<HtmlReplacement> hr = new ArrayList<HtmlReplacement>();
	    NoteFactHtmlDetail         noteFactHtmlDetail = new NoteFactHtmlDetail();
	    boolean                    bNotComplete = true;
	    boolean                    bError = false;
	    
		errorCls.setCompanyUid(SubInfo.getCompanyId());
		errorCls.setFunctionStr("ModifyHtmlFile");
		errorCls.setItemVersion(SubInfo.getVersion());
		errorCls.setBExit(false);
	    
	    hr = mySqlAccess.GetEventReplacementStr(con);
	    try {
	    	while(bNotComplete) {
	    		bError = false;
	    	    iLineNum = 0;
	    	    iNdx = 0;
	    		ModHtmlFile = "" + UUID.randomUUID().toString();
                fstream = new FileInputStream(SubInfo.getHtmlFile());
                ostream = new FileOutputStream(ModHtmlFile);
                in = new DataInputStream(fstream);
                out = new DataOutputStream(ostream);
                wr = new BufferedWriter(new OutputStreamWriter(out));
                RemoveFactsIgnored(hrlList);
	            while((iNdx < hrlList.size()) && (bError == false)) {
	        	    fileLine = in.readLine();    //   in.readLine();
	        	    iLineNum++;
	        	    if(fileLine != null) {
	        	        if ((iLineNum == hrlList.get(iNdx).GetHtmlLineNumber()) && ((hrlList.get(iNdx).GetMatchType() == 2) || 
	        	            (hrlList.get(iNdx).GetMatchType() == 1) || (hrlList.get(iNdx).GetMatchType() == 4))) {
	        		        fileLine = DoEventStrReplacement(fileLine, hr);
	        		        iNdx = ModifyFileLine(con, SubInfo, wr, fileLine, hrlList, iNdx);
	        		        if(iNdx < 0)  // if ERROR!!!
	        			        break;
	        	        }
 			    	    else {  // check if note fact is in table
	    	        	    if ((iLineNum >= hrlList.get(iNdx).GetHtmlLineNumber()) && (hrlList.get(iNdx).GetMatchType() == 3)) { 
	    	        		    if(fileLine.length() > 0) {
	    	        		        noteFactHtmlDetail.SetFactSentenceUid(hrlList.get(iNdx).GetFactSentenceUid());
	    	        		        noteFactHtmlDetail.ParseHtmlLine(fileLine);
	    	        		        noteFactHtmlDetail.SetPrevProcessedIdx(0);
	    	       			        iNdx = noteFactHtmlDetail.ModifyHtmlLine(con, SubInfo, wr, hrlList, iNdx);
	    	       		        }
	    	       	        }
	    	       	        else {
                                wr.write(fileLine);
			    	        }
	    	        	}
	        	        // we check here for a fact not found
	        	        if((iNdx < hrlList.size()) && (hrlList.get(iNdx).GetHtmlLineNumber() == iLineNum)) {
	        	        	// error 
	        	        	bError = true;
	        	        	hrlList.get(iNdx).SetIgnore();
	        	        	mySqlAccess.DeleteSectionFact(con, hrlList.get(iNdx).GetFactUid());
	        	        	if(hrlList.get(iNdx).GetMatchType() == 4)  {// there are two 
		        	        	mySqlAccess.DeleteSectionFact(con, hrlList.get(iNdx+1).GetFactUid());
		        	        	hrlList.get(iNdx+1).SetIgnore();
	        	        	}
	        	    	    errorCls.setErrorText("Unable to find fact: '" + hrlList.get(iNdx).GetFact() +
	        	    	    		               "' on line: " + iLineNum);
	        	    	    mySqlAccess.WriteAppError(con, errorCls);
	        	        }
	        	        else
                            wr.newLine();
	        	    }
	        	    else
	        		    break;
	            }
	            if(bError == false) { // no error complete writing
	                while(fileLine != null) {
	        	        fileLine = in.readLine();
	        	        if(fileLine != null) {
	        	            iLineNum++;
	                        wr.write(fileLine);
	                        wr.newLine();
	        	        }
	        	    }
	            }
	            wr.flush();
	            wr.close();
	            in.close();
                fstream.close();
                ostream.close();
                if(bError == false) {
                    file = new File(SubInfo.getHtmlFile());
                    file.delete();
                    filefrom = new File(ModHtmlFile);
                    file = new File(SubInfo.getHtmlFile());
                    filefrom.renameTo(file);
                    bNotComplete = false;
                }
                else { // clean up partial file
                	file = new File(ModHtmlFile);
                	file.delete();
                }
	    	}
	    }
	    catch (Exception e) {
    	    errorCls.setErrorText("Unable to access files: " + e.getMessage() + " LineNum = " + iLineNum);
    	    mySqlAccess.WriteAppError(con, errorCls);
    	    iRtn = -4;
	    }
	    
	    return(iRtn);
	}
	
	private void RemoveFactsIgnored(ArrayList<HtmlReplacementLines> hrlList) {
		int i = hrlList.size() -1;
	 
		while(i >= 0) {
			if(hrlList.get(i).GetIgnore() == true)
				hrlList.remove(i);
			i--;
		}
	}
	
	private String DoEventStrReplacement(String fileLine, ArrayList<HtmlReplacement> hr) {
		String rtnStr = fileLine;
		for(HtmlReplacement curHr: hr) {
			rtnStr = curHr.ReplaceStr(rtnStr);
		}
		return(rtnStr);
	}
	
	private int ModifyFileLine(Connection con, SubmissionInfo SubInfo,  BufferedWriter wr, String fileLine, 
			                   ArrayList<HtmlReplacementLines> hrlList, int iNdx) {
		int               iLineNum = hrlList.get(iNdx).GetHtmlLineNumber();
		String            ModLine = "";
	    ErrorCls          errorCls = new ErrorCls();
		MySqlAccess       mySqlAccess = new MySqlAccess();
		int               iCharLoc = 0;
		String            remStr = fileLine;
		String            testChar = "";
		CONSTANTS         constants = new CONSTANTS();
		String            wordStr = "";
		ArrayList<String> words;
		boolean           bContinue = true;
		
		errorCls.setCompanyUid(SubInfo.getCompanyId());
		errorCls.setFunctionStr("ModifyFileLine");
		errorCls.setItemVersion(SubInfo.getVersion());
		errorCls.setBExit(false);
	    try {
	    	// we check to first character of the remaining string
	    	// at which time we decide upon actions:::
	    	//  characters: < then we look for >
	    	int iWordCount = 0;
	    	while(remStr.length() > 0) {
	    	    testChar = remStr.substring(0,1);
	    	    if(testChar.equals("<")) {
	    	    	iCharLoc = remStr.indexOf(">");
	    	    	if(iCharLoc == -1) { //this should not happen, but just in case
	    	    		ModLine += remStr;
	    	    		remStr = "";
	    	    	}
	    	    	else { // found the end atag
	    	    		ModLine += remStr.substring(0, iCharLoc + 1);
	    	    		if(iCharLoc == remStr.length())
	    	    			remStr = "";
	    	    		else
	    	    		    remStr = remStr.substring(iCharLoc+1);	    	    		
	    	    	}	    	    	
	    	    }
	    	    else {  // found the text 
	    	    	iCharLoc = remStr.indexOf("<");
	    	    	if(iCharLoc == -1) {
	    	    	    wordStr = remStr;
	    	    	    remStr = "";
	    	    	}
	    	    	else {
	    	    		int iGtChar = remStr.indexOf(">");  // this is the check for line in which no '<' as first char
	    	    		if((iGtChar == -1) || (iGtChar > iCharLoc)) { // case where no '<' or '<' not in beginning of line
	    	    		    wordStr = remStr.substring(0, iCharLoc);
	    	    		    remStr=  remStr.substring(iCharLoc);
	    	    		}
	    	    		else {  // we remove
	    	    			wordStr = remStr.substring(iGtChar+1, iCharLoc);
	    	    			remStr = remStr.substring(iCharLoc);
	    	    		}
	    	    	}
	    	    	words = constants.ListOfWords(wordStr);
	    	    	if((words.get(0).length() == 0) && (ModLine.length() > 0)) // skip if MT
	    	    		iWordCount += 1;
	    	    	int iWordsProcessed = 0;
	    	    	while(iWordsProcessed < words.size()) {
	    	    		if(iNdx >= hrlList.size()) {
				            if(ModLine.length() > 0) 
					            ModLine += " ";
				            ModLine += words.get(iWordsProcessed);
	    	    		}
	    	    		else {
	    	    		    if((hrlList.get(iNdx).GetMatchType() == 2) || (hrlList.get(iNdx).GetMatchType() == 1)) {
	    	    		        int iBumpCount = IsThisTheFactToSurround(iLineNum, iWordCount, iWordsProcessed, words, hrlList, iNdx);
	    	    		        if(iBumpCount > 0) {
	    	    			        if(iBumpCount > 1) {
		    	    			        if(ModLine.length() > 0) 
		    	    				        ModLine += " ";
		    	    			        ModLine += words.get(iWordsProcessed);
    	    				            iWordCount++;
    	    				            iWordsProcessed++;
	    	    			        }
	    	    			        ModLine += constants.SetNoteFactTag(hrlList.get(iNdx).GetFactUid(), true) + words.get(iWordsProcessed)  + constants.GetNoteFactEndTag();
	    	    			        iNdx++;  // done this fact, point to next
	    	    		        }
	    	    		        else {  // yup got there
	    	    			        if(ModLine.length() > 0) 
	    	    				        ModLine += " ";
	    	    			        ModLine += words.get(iWordsProcessed);
	    	    		        }
	    	    		    }
	    	    		    else {  // it's a hyphenated match
		                	    if(IsThisAPattern(iWordsProcessed, words)) {
			                	    int i = words.get(iWordsProcessed).indexOf("-");
			                	    String temp = words.get(iWordsProcessed).substring(0, i);
					                ModLine += constants.SetNoteFactTag(hrlList.get(iNdx).GetFactUid(), true) + temp  + constants.GetNoteFactEndTag();
					                ModLine += "-";
					                iNdx++;  // done this fact, point to next
					                temp = words.get(iWordsProcessed).substring(i + 1);
					                ModLine += constants.SetNoteFactTag(hrlList.get(iNdx).GetFactUid(), false) + temp  + constants.GetNoteFactEndTag();
					                iNdx++;
				                }
				                else {  // yup got there
					                if(ModLine.length() > 0) 
						                ModLine += " ";
					                ModLine += words.get(iWordsProcessed);
				                }
	    	    		    }
	    	    		}
	    	    		iWordCount++;  // skip to next word
	    	    		iWordsProcessed++;
	    	    	}
	    	    }
	    	}
            wr.write(ModLine);
	    }
	    catch (Exception e) {
    	    errorCls.setErrorText("Unable to access files: " + e.getMessage());
    	    mySqlAccess.WriteAppError(con, errorCls);
    	    iNdx = -5;
	    }
        return(iNdx);
    }
	/**************
	private int ModifyFileLine2(Connection con, SubmissionInfo SubInfo,  BufferedWriter wr, String fileLine, 
                                ArrayList<HtmlReplacementLines> hrlList, int iNdx) {
        int               iLineNum = hrlList.get(iNdx).GetHtmlLineNumber();
        String            ModLine = "";
        ErrorCls          errorCls = new ErrorCls();
        MySqlAccess       mySqlAccess = new MySqlAccess();
        int               iCharLoc = 0;
        String            remStr = fileLine;
        String            testChar = "";
        CONSTANTS         constants = new CONSTANTS();
        String            wordStr = "";
        ArrayList<String> words;
        boolean           bContinue = true;

        errorCls.setCompanyUid(SubInfo.getCompanyId());
        errorCls.setFunctionStr("ModifyFileLine");
        errorCls.setItemVersion(SubInfo.getVersion());
        errorCls.setBExit(false);
        try {
            // we check to first character of the remaining string
            // at which time we decide upon actions:::
            //  characters: < then we look for >
            int iWordCount = 0;
            while(remStr.length() > 0) {
                testChar = remStr.substring(0,1);
                if(testChar.equals("<")) {
	                iCharLoc = remStr.indexOf(">");
	                if(iCharLoc == -1) { //this should not happen, but just in case
		                ModLine += remStr;
		                remStr = "";
	                }
	                else { // found the end atag
		                ModLine += remStr.substring(0, iCharLoc + 1);
		                if(iCharLoc == remStr.length())
			                remStr = "";
		                else
		                    remStr = remStr.substring(iCharLoc+1);	    	    		
	                }	    	    	
                }
                else {  // found the text 
	                iCharLoc = remStr.indexOf("<");
	                if(iCharLoc == -1) {
	                    wordStr = remStr;
	                    remStr = "";
	                }
	                else {
		                wordStr = remStr.substring(0, iCharLoc);
		                remStr=  remStr.substring(iCharLoc);
	                }
	                words = constants.ListOfWords(wordStr);
	                if((words.get(0).length() == 0) && (ModLine.length() > 0)) // skip if MT
		                iWordCount += 1;
	                int iWordsProcessed = 0;
	                while(iWordsProcessed < words.size()) {
	                	if(IsThisAPattern(iWordsProcessed, words)) {
		                //int iBumpCount = IsThisTheFactToSurround(iLineNum, iWordCount, iWordsProcessed, words, hrlList, iNdx, con, SubInfo);
		                //if(iBumpCount > 0) {
	                		int i = words.get(iWordsProcessed).indexOf("-");
	                		String temp = words.get(iWordsProcessed).substring(0, i);
			                ModLine += constants.SetNoteFactTag(hrlList.get(iNdx).GetFactUid(), true) + temp  + constants.GetNoteFactEndTag();
			                ModLine += "-";
			                iNdx++;  // done this fact, point to next
			                temp = words.get(iWordsProcessed).substring(i + 1);
			                ModLine += constants.SetNoteFactTag(hrlList.get(iNdx).GetFactUid(), false) + temp  + constants.GetNoteFactEndTag();
			                iNdx++;
		                }
		                else {  // yup got there
			                if(ModLine.length() > 0) 
				                ModLine += " ";
			                ModLine += words.get(iWordsProcessed);
		                }
		                iWordCount++;  // skip to next word
		                iWordsProcessed++;
 	                }
                }
            }
            wr.write(ModLine);
        }
        catch (Exception e) {
            errorCls.setErrorText("Unable to access files: " + e.getMessage());
            mySqlAccess.WriteAppError(con, errorCls);
            iNdx = -5;
        }
        return(iNdx);
    }
*****************/
	private boolean IsThisAPattern(int iWordProcessing, ArrayList<String> words) {
		boolean bRtn = false;
		
		if(words.get(iWordProcessing).matches(CONSTANTS.HYP_PATTERN))
			bRtn = true;
		return(bRtn);
	}
	
	private int IsThisTheFactToSurround(int iHtmlLineNum, int iWordNdx, int iWordProcessing, ArrayList<String> words, ArrayList<HtmlReplacementLines> hrlList, 
			                            int iNdx) {
		int iRtn = 0;

		if(iNdx < hrlList.size()) {  // check if any left?
			if(words.get(iWordProcessing).equals(hrlList.get(iNdx).GetFact()) == true) {
				iRtn++;
			}
		}	
		return(iRtn);
	}

	private void TraverseNode(Node n) {
		NodeList children = n.getChildNodes();
		
		if(children != null) {
			for(int i = 0; i < children.getLength(); i++) {
				Node childNode = children.item(i);
				System.out.println("node Name: " + childNode.getNodeName());
				System.out.println("Node Value: " + childNode.getNodeValue());
				TraverseNode(childNode);
			}
		}
	}
	private int FindFieldsInPreamble(Connection con, SubmissionInfo si, int CurrentLineNumber,
			                         BufferedReader br, int htmlSrc) {
	
		MyFileAccess      myFile = new MyFileAccess();
		ArrayList<String> mySection = null;
		int               parsedLine = 0;
		int               endLine = 0;
		FieldMatchStr[]   TextMatchStr = null;
		FieldMatchStr[]   TableMatchStr = null;
		FieldMatchStr[]   TheOthers = null;
		double            dConfidence = 0;
		double            MaxConfidence; 
		int               lineNdx = 1;
		String            curLine = "";
		ConfidenceLevel   conLevel = new ConfidenceLevel();
		int               j;
		int               MaxConfidenceNdx = 0;
		int               iTblLen; 
		ErrorCls          errorCls = new ErrorCls();
		int               iTemplateParseUid = 0;
		CONSTANTS         constants = new CONSTANTS();
		MySqlAccess       mySqlAccess = new MySqlAccess();
		
		errorCls.setFunctionStr("FindFieldsInPreamble");
	    errorCls.setSubUid(si.getUid());
	    errorCls.setCompanyUid(si.getCompanyId());
	    errorCls.setItemVersion(si.getCompanyId());
		try {
			TextMatchStr = mySqlAccess.GetFieldIdentifiers(con, 0, si.getCompanyId(), -2);
			TheOthers = mySqlAccess.GetFieldIdentifiers(con, 0, si.getCompanyId(), -99);
			TableMatchStr = mySqlAccess.GetFieldIdentifiers(con, 1, si.getCompanyId(), -2);
		    ResultSet rs = mySqlAccess.GetTemplate( con, si.getUid(), si.getVersion(), -2);
		    rs.next();
		    iTemplateParseUid = rs.getInt(1);
		    parsedLine = rs.getInt(5);  // was 7
		    endLine = rs.getInt(6);     // was 8
		    //String temp = br.readLine();
		    if(endLine > 0) {   // this is check if NO preamble found
		        mySection = myFile.ReadSectionIntoMem(br, CurrentLineNumber, parsedLine, endLine, false);
		        while((parsedLine < (endLine-1))  && (lineNdx < mySection.size())) {
	                curLine = mySection.get(lineNdx);
	                curLine = curLine.trim();
	                if(curLine.length() > 0) {
	                    if(constants.CheckForBeginTable(curLine) == -1) {  // not a table check
	            	        MaxConfidence = 0;
               	            for(j = 0; j < TextMatchStr.length; j++) {
            	                dConfidence = conLevel.compareToArrayList(curLine, TextMatchStr[j].Al, TextMatchStr[j].getFieldStr());
            	                if(dConfidence > MaxConfidence) {
            	    	            MaxConfidence = dConfidence;
            	     	            MaxConfidenceNdx = j;
            	                }
            	            }
               	            if(MaxConfidence > 0.75) {
               	    	        //System.out.println("We got a match: " + curLine);
               	        	    if(TextMatchStr[MaxConfidenceNdx].getDestination() == 5)
               	        		    ProcessTextFilerStatus(con, si, curLine);
               	        	    else
               	    	            ProcessTextMatch(con, si, mySection, lineNdx, TextMatchStr[MaxConfidenceNdx], -2, iTemplateParseUid);
               	            }
               	            else {   // check for shares  AND OTHERS!!
               	        	    if(IsThisStockLine(curLine) == true) {
               	        		    iTblLen = SharesOutstandingCk(con, mySection, curLine, si, lineNdx);
               	        		    lineNdx += iTblLen;
               	        		    parsedLine += iTblLen;
               	        	    }
               	        	    String lowerCurLine = curLine.toLowerCase();
               	        	    if(lowerCurLine.indexOf("accelerated filer") != -1)
               	        		    // beware of imitations
               	        		    ProcessTextFilerStatus(con, si, curLine);
               	        	    else {  // The others
               	        		    int iFound = -1;
               	        		    for(j = 0; j < TheOthers.length; j++) {
               	        			    if(curLine.indexOf(TheOthers[j].getFieldStr()) != -1) {
               	        				    iFound = j;
               	        			    }
               	        		    }
               	        		    if(iFound != -1)
               	        		  	    ProcessTextMatch(con, si, mySection, lineNdx, TheOthers[iFound], -2, iTemplateParseUid);
               	        	    }
               	            }
	                    }
	                    else {
	            	        iTblLen = ParseTableFields(mySection, lineNdx, TableMatchStr, con, si, -2, iTemplateParseUid, htmlSrc);
	            	        iTblLen--;   // decrement by one to postion correctly
	            	        lineNdx += iTblLen;
	            	        parsedLine += iTblLen;
	                    }
	                }
	                lineNdx++;
		    	    parsedLine++;
		        }
		    }  // this is end of no reading
		}
		catch (Exception e) {
			System.out.println("Error reading template: " + e.getMessage());
		     errorCls.setErrorText("Error reading template:"  + e.getMessage());
		     mySqlAccess.WriteAppError(con, errorCls);
		}
		endLine++;   // we are pointed to this line
		//MySqlAccess.SetEin(con,si.getUid(),si.getVersion(), si.getCompanyId());
		return(endLine);
	}
	
	private boolean IsThisStockLine(String curLine) {  // static
		boolean bRtn = false;
		
		curLine = curLine.toLowerCase();
		if(curLine.indexOf("shares outstanding") != -1)
			bRtn = true;
		else {
			if((curLine.indexOf("stock") != -1) && (curLine.indexOf("outstanding") != -1) &&
			(curLine.indexOf("share") != -1))
				bRtn = true;
		}
		return(bRtn);
	}
	
	private int SharesOutstandingCk(Connection con, ArrayList<String> mySection, String curLine,   // static
			                               SubmissionInfo si, int iLineNdx) {
		int         iRtn = 0;  // NOTE: the line counts are automatically incremented by one on return
		
		String             dateStr = "";
		int                sharesInt = -1;
		double             parValueFloat = 0;
		int                i;
		ArrayList<String>  le = new ArrayList<String>();
		boolean            bStillChecking = false;
		int                iTemp;
		String             testDateStr = "";
		CombinedStock      cs = new CombinedStock();
		String             extract;
		boolean            bCheckNextValue = true;
		String             lowerCased = curLine.toLowerCase();
		boolean            bSharesNoDimension = true;
		boolean            bSharesClassA = false;
		boolean            bSharesClassB = false;
		boolean            bCommonStock = false;
		boolean            bPreferredStock = false;
		boolean            bFoundShares = false;  // if no shares found must be on successive lines
		CONSTANTS          constants = new CONSTANTS();
		MySqlAccess        mySqlAccess = new MySqlAccess();
		
		// stay here until walked through all line
		extract = curLine.substring(curLine.length() -1);
		if(extract.equals(".")) 
			curLine = curLine.substring(0,curLine.length()-1);
		if(lowerCased.indexOf("class a") != -1)
			bSharesClassA = true;
		if(lowerCased.indexOf("class b") != -1)
			bSharesClassB = true;
		if(((lowerCased.indexOf("common stock") != -1)  ||(lowerCased.indexOf("common shares") != -1)) && (bSharesClassA == false))
			bCommonStock = true;
		if((lowerCased.indexOf("preferred stock") != -1) && (bSharesClassA == false))
			bPreferredStock = true;
		le = ExtractWord(curLine);
		for(i = 0; i < le.size(); i++) {
		    bCheckNextValue = true;
		    if((bSharesClassA == true)  || (bSharesClassB == true) 
		    	|| (bSharesNoDimension == true) || (bCommonStock == true) ||
		    	(bPreferredStock == true)){ 
			    sharesInt = IsThisShares(le.get(i));
			    if(sharesInt != -1) {
			    	bCheckNextValue = false;
			    	bFoundShares = true;
			    	if((bSharesClassA == false) && (bSharesClassB == false) &&
			    		(bCommonStock == false) && (bPreferredStock == false)) {
			    		cs.NoDimensionShares = sharesInt;
			    		bSharesNoDimension = false;
			    	}
			    	else {
			    	    if(bSharesClassA == true) {
			    		    cs.CommonStockAShares = sharesInt;
			    		    bSharesClassA = false;
			    	    }
			    	    else {
			    	    	if(bSharesClassB == true) {
			    		        cs.CommonStockBShares = sharesInt;
			    		        bSharesClassB = false;
			    	    	}
			    	    	else {
			    	    		if(bCommonStock == true) {
			    	    			cs.NoDimensionShares = sharesInt;
			    	    			bCommonStock = false;
			    	    		}
			    	    		else {
			    	    			cs.PreferredStockShares = sharesInt;
			    	    			bPreferredStock = false;
			    	    		}
			    	    	}
			    	    }
			    	}
			    }
		   }
		   if(bCheckNextValue == true) {
		       if(dateStr.length() == 0) {
			       if(constants.DoesStrContainMonth(le.get(i)) == true) {
			    	   bCheckNextValue = false;
				       bStillChecking = true;
				       iTemp = 0;
				       testDateStr = le.get(i);
				       while(bStillChecking) {
				           if(constants.IsThisADate(le.get(i)) == true) {
					           dateStr = le.get(i);
					           bStillChecking = false;
				           }
				           else {
					          if((i+iTemp+ 1) < le.size()) {  // check if we have another slot
					    	      iTemp++;
					    	      testDateStr  = testDateStr + " " + le.get(i+iTemp);
					    	      if(constants.IsThisADate(testDateStr)) {
					    		      dateStr = testDateStr.replace(":", "");
					    		      i = i + iTemp;
					    		      bStillChecking = false;
					    	      }
					          }
					          else
					    	      bStillChecking = false;
					       }
				       }  // end while
			       }
		       }
		   }
		   if((parValueFloat == 0) && (bCheckNextValue == true)) {
			   testDateStr = le.get(i);
			   if(testDateStr.indexOf("$") != -1)
				   testDateStr = testDateStr.replace("$", "");
			   parValueFloat = constants.IsThisDecimalMoney(testDateStr);
			   if(parValueFloat > 0) {
			       if((bSharesClassA == false) && (bSharesClassB == false))
				       cs.NoDimensionValue = (float)parValueFloat;
			       else {
				       if(bSharesClassA == true) {
					       cs.CommonStockAValue = (float)parValueFloat;
					       bSharesClassA = false;
				       }
				       if(bSharesClassB == true) {
					       cs.CommonStockBValue = (float)parValueFloat;
					       bSharesClassB = false;
				       }
			       }
			       parValueFloat = 0;
			   }
		   }
		}
		if(bFoundShares == false) {  // no share yet - keep going until MT line
			//WLW KSWISS
			boolean bContinue = false;
			String strLine;
			
			while (bContinue)  {
				iLineNdx++;
				strLine = mySection.get(iLineNdx);
			}
		}
		//just write - that routine only writes shares positive
		mySqlAccess.WriteCombinedStockRecord(con, si, cs, dateStr);
		return(iRtn);
	}
	
	private int IsThisShares(String origStr) {  // static
		int       rtnStr = -1;
		CONSTANTS constants = new CONSTANTS();
		
		String Temp = origStr.replace(",", "");
		rtnStr = constants.isNumeric(Temp);		
		return(rtnStr);
	}
	
	
	
	private ArrayList<String> ExtractWord(String origStr) {  // static
		ArrayList<String> rtnLe = new ArrayList<String>();
		int i;
		String Temp = null;
		
		origStr = origStr.trim();
		while(origStr.length() > 0) {
			i = origStr.indexOf(" ");
			Temp = new String();
			if(i > 0) {
				Temp = origStr.substring(0,i).trim();
				origStr = origStr.substring(i).trim();
			}
			else {
				Temp = origStr;
				origStr = "";
			}
			rtnLe.add(Temp);
		}
		
		return(rtnLe);
	}
	
	private int SkipPastTOC(Connection con, SubmissionInfo si, int iCurLine, BufferedReader br) {  // static
	
		int            RtnLineNum = iCurLine;
		int             iTocEndLine = 0;
		MySqlAccess     mySqlAccess = new MySqlAccess();
		
		iTocEndLine = mySqlAccess.GetTocEndLine(con, si);
		try {
		    while(iTocEndLine >= RtnLineNum ) {
			    br.readLine();
			    RtnLineNum++;
		    }
		}
		catch (Exception e) {
			
		}
		return(RtnLineNum);
	}
	
	private int FindFieldsInTables(Connection con, SubmissionInfo si,  // static
            int CurrentLineNumber, BufferedReader br, boolean ParseNotes,
            ArrayList<HtmlReplacementLines> hrlList, int iHtmlSrc) {

        MyFileAccess                    myFile = new MyFileAccess();
        ArrayList<String>               mySection = null;
        int                             TemplateType;
        boolean                         bReadtoTable;
        String                          IdentifiedText = "";
        int                             TemplateParseUid = 0;
        int                             iNoteCounter = 1;
		ErrorCls                        errorCls = new ErrorCls();
		int                             beginLineNum = 0;
		int                             endLineNum = 0;
		int                             htmlBeginLineNum = 0;
		int                             htmlEndLineNum = 0;
		TdColSpan[]                     aTdColSpan = new TdColSpan[2]; 
		int                             activeTdColSpan = 0;
		int                             iIdLine = 0;
		ArrayList<String>               dateForms = new ArrayList<String>();
        DateRefCls                      dateRefUid = new DateRefCls();
		boolean                         bIsContinuation = false;
		MySqlAccess                     mySqlAccess = new MySqlAccess();
		NoteParseCls                    noteParseCls = new NoteParseCls();
		NoteDetailWord                  noteDetailWord = new NoteDetailWord();
		String                          prevIdentifiedText = "";
		String                          CONTINUATION_TEXT = "CONTINUATION";
		int                             prevTemplateParseUid = 0;
		int                             prevTemplateType = 0;
		int                             iNoteIndex = 0;
		ArrayList<DefedFacts>           DefdWords = new ArrayList<DefedFacts>();
		
		errorCls.setFunctionStr("FindFieldsInTable");
	    errorCls.setSubUid(si.getUid());
	    errorCls.setCompanyUid(si.getCompanyId());
	    errorCls.setItemVersion(si.getCompanyId()); 
	    dateForms = mySqlAccess.GetDateForms(con);
	    DefdWords = mySqlAccess.GetFactDefdWords(con);
	    aTdColSpan[0] = null;
	    aTdColSpan[1] = null;
	    if(ParseNotes == true)
	    	noteDetailWord.GetNoteDetailItems(con);
        try {
            ResultSet rs = mySqlAccess.GetTemplates( con, si.getUid(), si.getVersion(), CurrentLineNumber);
            while(rs.next()) {
            	activeTdColSpan++;
            	if(activeTdColSpan > 1)
            		activeTdColSpan = 0;
            	TemplateParseUid = rs.getInt(1);
                TemplateType = rs.getInt(4);
                beginLineNum = rs.getInt(5);
                endLineNum = rs.getInt(6);
                htmlBeginLineNum = rs.getInt(9);
                htmlEndLineNum = rs.getInt(10);
                IdentifiedText = rs.getString(12);
                iIdLine = rs.getInt(17);
                iNoteIndex = rs.getInt(18);
                
                // double check continuation - 
                bIsContinuation = false;
           		if(IdentifiedText.contains(prevIdentifiedText) && (IdentifiedText.toLowerCase().contains("continued")))
        			IdentifiedText = CONTINUATION_TEXT;
                aTdColSpan[activeTdColSpan] = new TdColSpan();
                aTdColSpan[activeTdColSpan].getTableColumns(con,si.getHtmlFile(),htmlBeginLineNum,htmlEndLineNum);
                if(IdentifiedText.contains(CONTINUATION_TEXT)) {
                	bIsContinuation = EnsureTemplate(aTdColSpan, activeTdColSpan);
                }
                //tdColSpan = new TdColSpan();
                //tdColSpan.getTableColumns(con,si.getHtmlFile(),htmlBeginLineNum,htmlEndLineNum);
                boolean bDummy = false;
                if(TemplateType != 4) {
                   //if(((tdColSpan.TblRow.size() == 0) || ((tdColSpan.TblRow.size() == 1) && (tdColSpan.TblRow.get(0).get(0) == 1))))  // was this
                	if(((IdentifiedText.contains("CONTINUATION") == false) && (aTdColSpan[activeTdColSpan].TblRow.size() < 5)) ||
                	   ((IdentifiedText.contains("CONTINUATION") == true)  && (aTdColSpan[activeTdColSpan].TblRow.size() < 3)))
                	//if(tdColSpan.TblRow.size() < 5)  // table must have at least 3 rows
                	   bDummy = true;
                }
                if(bDummy == true) {
                	mySqlAccess.MarkAsDummy(con, TemplateParseUid );
                }
                else {
                    if(TemplateType == 4) // It's a note read it all
                	    bReadtoTable = false;
                    else
                	    bReadtoTable = true;
                    TemplateHdr  templateHdr = new TemplateHdr();
                    if(bReadtoTable) {
                    	templateHdr = myFile.ReadHeaderIntoMem(br, CurrentLineNumber, iIdLine, beginLineNum);
                    	CurrentLineNumber = templateHdr.currentLine;
                    	templateHdr.ParseHdr(con);
                    }
                    mySection = myFile.ReadSectionIntoMem(br, CurrentLineNumber, beginLineNum, endLineNum, bReadtoTable);
                    switch (TemplateType) {
                    //do nothing here - unkown section
                        case -3:
                	        break;
                        case 4:  // It's a note
                    	    if(ParseNotes == true) {
                    	        endLineNum = noteParseCls.ParseNote(mySection, TemplateType, con, si, TemplateParseUid,
                    	    	    	               IdentifiedText, iNoteCounter, htmlBeginLineNum, 
                    	    		                   htmlEndLineNum, beginLineNum, dateForms, noteDetailWord,
                    	    		                   hrlList, DefdWords, iNoteIndex, iHtmlSrc);
                    	    }
                        	iNoteCounter++;
                    	    break;
                        // case 7:
                        //	Template7.ParseTemplateStockholders(mySection, TemplateType, con, si, htmlBeginLineNum, htmlEndLineNum,
     			        //                                       beginLineNum, endLineNum, tdColSpan);
                        //	break;
                        default:
                        	// before we call parsing - fake it out if thought was continuation 
                        	if((IdentifiedText.contains("CONTINUATION")) && (bIsContinuation == false))
                        	    IdentifiedText = "continuation";
                     	    dateRefUid = ParseTemplateType(mySection, TemplateParseUid, TemplateType, con, si, htmlBeginLineNum, htmlEndLineNum,
     			                                           beginLineNum, endLineNum, aTdColSpan[activeTdColSpan], templateHdr, dateForms, IdentifiedText,
     			                                           dateRefUid);
                    	    break;
                     }
                    if((prevTemplateType == TemplateType) && (TemplateType != 4) && (TemplateType != 3))
                    	CheckForVerticleMerge(con, TemplateParseUid, prevTemplateParseUid, si, TemplateType, IdentifiedText);
                    prevTemplateParseUid = TemplateParseUid;
                    prevTemplateType = TemplateType;
                     CurrentLineNumber  = endLineNum + 1;  // this is where we are now!!
                }
                if(IdentifiedText.equals(CONTINUATION_TEXT) == false)
                    prevIdentifiedText = IdentifiedText;
            }   // END WHILE
            rs.close();
        }
        catch (Exception e) {
            //System.out.println("Error reading template: " + e.getMessage());
		     errorCls.setErrorText("Error reading template:"  + e.getMessage());
		     errorCls.setBExit(false);
		     mySqlAccess.WriteAppError(con, errorCls);
        }
        mySection = null;
        return(CurrentLineNumber);  // was endLine
    }

	private void CheckForVerticleMerge(Connection con, int TemplateParseUid, int prevTemplateParseUid, SubmissionInfo si, 
			                           int TemplateType, String identifiedText) {
	
		ArrayList<DateRefRecord> prevTemplate = null;
		ArrayList<DateRefRecord> thisTemplate = null;
		MySqlAccess              mySqlAccess = new MySqlAccess();
		boolean                  bDuplicate = true;
		// if contains continuation - already done
		if(identifiedText.toLowerCase().contains("continuation") == false) {
			prevTemplate = mySqlAccess.GetDateRefRecords(con, prevTemplateParseUid);
			if(prevTemplate != null) {
		        thisTemplate = mySqlAccess.GetDateRefRecords(con, TemplateParseUid);
		        if(thisTemplate != null) {
		            if(prevTemplate.size() == thisTemplate.size()) {
		            	int i = 0;
		            	while((bDuplicate == true) && (i < thisTemplate.size())) {
		            		bDuplicate = thisTemplate.get(i).CompareDateRef(prevTemplate.get(i));
		            		i++;
		            	}
		            	if(bDuplicate) {
		            		for(i = 0; i < thisTemplate.size(); i++) {
		            			mySqlAccess.UpdateFieldsLocated(con, thisTemplate.get(i).GetUid(), prevTemplate.get(i).GetUid());
		            			mySqlAccess.DeleteUnusedDateRefs(con, thisTemplate.get(i).GetUid());
		            		}
		            	}
		            }
		        }
			}
		}
	}
	
	private boolean EnsureTemplate(TdColSpan[] atdColSpan, int activeNdx) {  // static
		boolean bRtn = true;
		int i;
		int iNumColumns = 0;
		
		if(atdColSpan[activeNdx].TblRow.size() < 5) {
			bRtn = false;
		}
		else {  // if min rows - check that enough columns
			for(i = 0; i < atdColSpan[activeNdx].TblRow.size(); i++) {
				if(atdColSpan[activeNdx].TblRow.get(i).size() > 5)
					iNumColumns++;
			}
			if(iNumColumns < 5)  // must have at least 5 rows with 5 columns
				bRtn = false;
		}
		if(bRtn == true) {
			int i0Cols = 0;
			int i1Cols = 0;
			int iCounter = 0;
			while(iCounter < atdColSpan[0].TblRow.get(0).size()) {
				i0Cols += atdColSpan[0].TblRow.get(0).get(iCounter);
				iCounter++;
			}
			iCounter = 0;
			while(iCounter < atdColSpan[1].TblRow.get(0).size()) {
				i1Cols += atdColSpan[1].TblRow.get(0).get(iCounter);
				iCounter++;
			}
			if(i0Cols != i1Cols)
				bRtn = false;
		}
			//if(atdColSpan[0].TblCol.size() != atdColSpan[1].TblCol.size())
				//bRtn = false;
		return(bRtn);
	}
	
	private void ProcessTextMatch(Connection con, SubmissionInfo subInfo,   // static
			                             ArrayList<String> mySection, int lineNdx, 
			                             FieldMatchStr matchDtl, int TemplateType,
			                             int TemplateUid) {
	
		String            dataLocStr = matchDtl.getPathToData();
		String            DataStr = "";
		int               iLenMatch = 0;
		int               iLenSrc = mySection.get(lineNdx).trim().length();
		MySqlAccess       mySqlAccess = new MySqlAccess();
		CONSTANTS         constants = new CONSTANTS();
		
		if(dataLocStr.equals("RT")) {
			iLenMatch = constants.MonthBegin(mySection.get(lineNdx).trim());
			if(iLenMatch != -1) 
				DataStr = mySection.get(lineNdx).trim().substring(iLenMatch);
			else {
				iLenMatch = matchDtl.getFieldStr().length();
			    DataStr = mySection.get(lineNdx).trim().substring(iLenMatch, iLenSrc).trim();	
			}
			DataStr = DataStr.replace(":", "");
			DataStr = DataStr.trim();
			// we may want in the future to test for date 
			//and skip line on caller
			if(DataStr.length() == 0)  // not on this line - get next
				DataStr = mySection.get(lineNdx+1).trim();
		}
		if(dataLocStr.equals("PL")) {
			String TempStr = "";
			int    lineOffset = 1;
			while(TempStr.length() == 0) {
				DataStr = mySection.get(lineNdx- lineOffset).trim();
				TempStr= DataStr.replace("-", "").trim();
				lineOffset++;
			}
		}
		if(dataLocStr.equals("MT")) {
			DataStr = mySection.get(lineNdx);
			int iIndex = DataStr.indexOf(matchDtl.getFieldStr());
			DataStr = DataStr.substring(iIndex + matchDtl.getFieldStr().length());
			DataStr = DataStr.trim();
		}
		mySqlAccess.writeFieldLocated(con, subInfo.getCompanyId(), subInfo.getUid(), 
				                      subInfo.getVersion(), TemplateType, TemplateUid, matchDtl.getUid(),
				                      DataStr, 0, matchDtl.getAbstract(), matchDtl.getDestination(), 0, 0, 0);
	}
	
	private int ParseTableFields(ArrayList<String> mySection, int iSectionNdx, FieldMatchStr[] tblMatchArray, Connection con,
			                     SubmissionInfo subInfo, int TemplateType, int iTemplateParseUid, int htmlSrc) {
		int             iTblLen = 0;
		String          TableStr = "";
		String          curLine = "";
		boolean         bFoundTblEnd = false;
		String	        NodeData;
		double          dConfidence;
		ConfidenceLevel conLevel = new ConfidenceLevel();
		int             iTrNdx = 0;
		int             iTdNdx = 0;
		String          ElementStr;
		ErrorCls        errorCls = new ErrorCls();
		int             iTableNdx = 0;
		CONSTANTS       constants = new CONSTANTS();
		MySqlAccess     mySqlAccess = new MySqlAccess();
		WriteFieldData  writeFieldData = new WriteFieldData();
		
		errorCls.setFunctionStr("ParseTableFields");
	    errorCls.setSubUid(subInfo.getUid());
	    errorCls.setCompanyUid(subInfo.getCompanyId());
	    errorCls.setItemVersion(subInfo.getCompanyId());      
		
		try {
		    while(bFoundTblEnd != true) {
		    	//if(iSectionNdx == 142) 
		    	//    System.out.println("PROCESSING: " + iSectionNdx);
		    	if(iSectionNdx == mySection.size())
		    		break;
			    curLine = mySection.get(iSectionNdx);
			    iTblLen++;
	            iSectionNdx++;
	            iTableNdx = constants.CheckForEndTable(curLine);
                if(iTableNdx == -1)   //  table end yet!!
           	        TableStr += curLine;
           	    else {   
           	    	TableStr += curLine.substring(0, (iTableNdx + 8));
                    bFoundTblEnd = true;	
                    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(TableStr));

                    Document doc = db.parse(is);
            		DocumentTraversal traversal = (DocumentTraversal) doc;
            		
                    NodeIterator iterator = traversal.createNodeIterator(
                                       doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
                    for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
                  	    ElementStr = ((Element) n).getTagName();
                   	    //System.out.println("ELEMENT: " + ElementStr);
                   	    if(ElementStr.equals("tr")) {
                    	    iTrNdx++;
                   		    iTdNdx = 0;
                   	    }
                    	else {
                    		if(ElementStr.equals("td"))
                    			iTdNdx++;
                    	}
                    	NodeList fstNm = n.getChildNodes();
                    	//System.out.println("DATA: " + (fstNm.item(0)).getNodeValue());
                    	int iLen = fstNm.getLength();
                    	if(iLen > 0) {
                    	    NodeData = (fstNm.item(0)).getNodeValue();
                    	    if(NodeData != null) {
                    	    	if(htmlSrc == 1) {  // RDG html generator
                   	                for(int j = 0; j < tblMatchArray.length; j++) {
                 	                    if((NodeData.contains(tblMatchArray[j].getFieldStr()) && 
                 	                       (tblMatchArray[j].getFieldStr().length() > 3))) {
                	                	    switch (tblMatchArray[j].getDestination()) {
                	                	        case 2:
                	                	        	if(NodeData.matches(".*\\d.*"))
                	                	        	    writeFieldData.WriteConcatedEin(con, subInfo, NodeData, 
                	                	        			                            NodeData.indexOf(tblMatchArray[j].getFieldStr()));
                	                	        	else
                    	                	    	    writeFieldData.WriteEin(con, subInfo, tblMatchArray[j],
    	                	    		    	                                n, iTrNdx, iTdNdx, NodeData);

                	                	        	break;
                	                	        case 3:
                	                	        	int i = NodeData.indexOf(tblMatchArray[j].getFieldStr());
                	                	        	if((i + tblMatchArray[j].getFieldStr().length() + 5) > NodeData.length()) {
                       	    	                        writeFieldData.DbWriteFieldData(con, subInfo, TemplateType, iTemplateParseUid,
	                                                                tblMatchArray[j], n, iTrNdx, iTdNdx, null, 
	                                                                iterator, null, null, NodeData, true);
                	                	        	}
                	                	        	else {
                 	                	        	    NodeData = NodeData.substring(i + tblMatchArray[j].getFieldStr().length()).trim();
            	                	    		        mySqlAccess.writeFieldLocated(con, subInfo.getCompanyId(),  subInfo.getUid(),
          	    			            	                                          subInfo.getVersion(), -2,  iTemplateParseUid, 0, 
          	    			            	                                          NodeData, 0, true, 3, 0, iTrNdx, iTdNdx);
                	                	        	}
                	                	        	break;
                	                	        case 4:
                	                	        	writeFieldData.WriteStateIncorporated(con, subInfo, NodeData,
                	                	        			                        NodeData.indexOf(tblMatchArray[j].getFieldStr()));
                	                	        	break;
                	                	        case 5:
                	                	        	if(writeFieldData.WriteFilerStatus(con, subInfo, NodeData) == false)
                    	                		        ProcessTableFilerStatus(con, subInfo, NodeData, iterator, false);
	
                	                	        	break;
                	                	        case 6:
                	                	    	    ProcessStocks(con, subInfo, NodeData, iterator);
                	                		        break;
               	                	    }
                	                	}
                   	                }
                    	    	}
                    	    	else {   // Edgarizer
                   	                for(int j = 0; j < tblMatchArray.length; j++) {
                	                    dConfidence = conLevel.compareToArrayList(NodeData, tblMatchArray[j].Al, tblMatchArray[j].getFieldStr());
                	                    if(dConfidence >= tblMatchArray[j].getThreshold()) {
                	                	    switch (tblMatchArray[j].getDestination()) {
                	                	        case 0:
                	    	                        writeFieldData.DbWriteFieldData(con, subInfo, TemplateType, iTemplateParseUid,
	        	  	                                        tblMatchArray[j], n, iTrNdx, iTdNdx, null, 
	        	  	                                        iterator, null, null, NodeData, false);
                	    	                        break;
                	                	        case 3:  // For quarterly period ended may be in this node or NEXT
                	                	    	    int iMonthNdx = constants.ReturnMonthIndex(NodeData);
                	                	    	    if( iMonthNdx != -1) {
                	                	    		    NodeData = NodeData.substring(iMonthNdx);
                	                	    		    mySqlAccess.writeFieldLocated(con, subInfo.getCompanyId(),  subInfo.getUid(),
                	                	    			            	              subInfo.getVersion(), -2,  iTemplateParseUid, 0, 
                	                	    			            	              NodeData, 0, true, 3, 0, iTrNdx, iTdNdx);
                	                	    	    }
                	                	    	    else  // should be in next column
                    	    	                        writeFieldData.DbWriteFieldData(con, subInfo, TemplateType, iTemplateParseUid,
    	        	  	                                                                tblMatchArray[j], n, iTrNdx, iTdNdx, null, 
    	        	  	                                                                iterator, null, null, NodeData, true);
                	                	    	    break;
                	                	        case 5:
                	                		        ProcessTableFilerStatus(con, subInfo, NodeData, iterator, false);
                	                		        break;
                	                	        case 6:
                	                	    	    ProcessStocks(con, subInfo, NodeData, iterator);
                	                		        break;
                	                	        case 2:
                	                	    	    writeFieldData.WriteEin(con, subInfo, tblMatchArray[j],
                	                	    		    	                n, iTrNdx, iTdNdx, NodeData);
                	                	    	    break;
                	                	        default:
                	    	                         writeFieldData.DbWriteFieldData(con, subInfo, TemplateType, iTemplateParseUid,
	        	  	                                                                 tblMatchArray[j], n, iTrNdx, iTdNdx, null, 
	        	  	                                                                 iterator, null, null, NodeData, false);
               	                		             break;
                	                	    }
                	                    }
                	                    else {  // additional check for filer status for all being on a single line
                	                	    if((dConfidence > .33) && (tblMatchArray[j].getDestination() == 5)) 
                	                		ProcessTableFilerStatus(con, subInfo, NodeData, iterator, true);
                	                    }
                	                }
                	            }
                    	    }
                	    }
                    }
                }

		    }
		}
		catch (Exception e) {
			//System.out.println("Document build error: " + e.getMessage());
		    errorCls.setErrorText("Document build error:"  + e.getMessage());
		    errorCls.setBExit(false);
		    mySqlAccess.WriteAppError(con, errorCls);
		}
		return(iTblLen);
	}
	
	private void ProcessTextFilerStatus(Connection con, SubmissionInfo subInfo, String NodeData) {  // static
		MySqlAccess         mySqlAccess = new MySqlAccess();
		
		FieldMatchStr[]     fieldMatch = mySqlAccess.GetFilerStatus(con, 1);
		ArrayList<Integer>  matchIndex = new ArrayList<Integer>();
		int                 i = 0;
		int                 j = 0;
		String              CompareString = "";
		int                 currentIndexInPlay = -1;
		int                 offsetIndex = -1;
		
		if(mySqlAccess.FilerStatusFound(con, subInfo.getCompanyId()) == false) {
			NodeData = NodeData.toLowerCase();
		    for(i = 0; i < fieldMatch.length; i++) {
		    	CompareString = fieldMatch[i].getFieldStr().toLowerCase();
		    	j = NodeData.indexOf(CompareString);
		    	if((j == -1) && (i == 0))  { // this could be abbreviated large accelerated filer
		    		CompareString = CompareString.replace("filer", "").trim();
		    		j = NodeData.indexOf(CompareString);
		    	}
		    	matchIndex.add(j);
		    }
		    j = NodeData.indexOf("x");
		    if(j != -1) {
		    	for(i = 0; i < fieldMatch.length; i++) {
		    		int iTemp = matchIndex.get(i);
		    	   if(iTemp != -1) {  // found this string
		    		   if((iTemp < j) && (iTemp > offsetIndex)) {  // found a new possible
		    			   currentIndexInPlay = i;
		    			   offsetIndex = iTemp;
		    		   }
		    	   }
		    	}
		    	// This is the check for imposters
		    	if (j <= (offsetIndex + fieldMatch[currentIndexInPlay].getFieldStr().length() + 5))
		    		mySqlAccess.InsertFilerStatus(con, subInfo.getCompanyId(), fieldMatch[currentIndexInPlay].getFieldStr());
		    	else { // check if duplicate of this appears later - smaller reporting company is known for this
		    		String MatchRemoved = NodeData.substring(offsetIndex + fieldMatch[currentIndexInPlay].getFieldStr().length());
		    		offsetIndex += MatchRemoved.indexOf(fieldMatch[currentIndexInPlay].getFieldStr().toLowerCase());
			    	if (j <= (offsetIndex + (fieldMatch[currentIndexInPlay].getFieldStr().length() * 2) + 5))  // second chance on duplicate
			    		mySqlAccess.InsertFilerStatus(con, subInfo.getCompanyId(), fieldMatch[currentIndexInPlay].getFieldStr());
		    	}
		    }
		}	
	}
	

	private void ProcessTableFilerStatus(Connection con, SubmissionInfo subInfo, String NodeData,   // static
			                               NodeIterator iterator, boolean bSingleLine ) {
		if(bSingleLine == false)
			ProcessFilerStatusViaTable(con, subInfo, NodeData, iterator);
		else 
			ProcessFilerStatusViaLine(con, subInfo, NodeData);
	}
	
	private void ProcessFilerStatusViaLine(Connection con, SubmissionInfo subInfo, String NodeData) {  // static
		
		String              TestData = NodeData.toLowerCase();
		MySqlAccess         mySqlAccess = new MySqlAccess();
		
		int                 iXndx = TestData.indexOf("x");
		int                 iPossibleNdx = -1;
		int                 iPossibleId = 0;
		int                 iThisNdx = 0;
		String              TempStr = "";
		String              iPossibleStr = "";
		
		if(iXndx != -1 ) {
			for(int i = 0; i < 4; i++) {
				switch(i) {
				case 0:
				    iThisNdx = TestData.indexOf("large accelerated filer");
				    TestData = TestData.replace("large accelerated filer", "xxxxxxxxxxxxxxxxxxxxxxx");
				    TempStr = "Large Accelerated Filer";
					break;
				case 1:
				    iThisNdx = TestData.indexOf("accelerated filer");
				    TestData = TestData.replace("accelerated filer", "xxxxxxxxxxxxxxxxx");
				    TempStr = "Accelerated Filer";
					break;
				case 2:
				    iThisNdx = TestData.indexOf("non-accelerated filer");
				    TempStr = "Non-Accelerated Filer";
					break;
				case 3:
				    iThisNdx = TestData.indexOf("smaller reporting company");
				    TempStr = "Smaller Reporting Company";
					break;
				}
			    if((iThisNdx > iPossibleNdx) && (iThisNdx < iXndx)) {
			    	iPossibleNdx = iThisNdx;
			    	iPossibleId = i;
			    	iPossibleStr = TempStr;
			    }
			}
			if(iPossibleId != -1) { // someone matched
				mySqlAccess.InsertFilerStatus(con, subInfo.getCompanyId(), iPossibleStr);
			}
		}
	}
	
	private void ProcessFilerStatusViaTable(Connection con, SubmissionInfo subInfo, String NodeData,   // static
                                                   NodeIterator iterator) {
		MySqlAccess         mySqlAccess = new MySqlAccess();
		FieldMatchStr[]     fieldMatch = mySqlAccess.GetFilerStatus(con, 1);
		boolean             bFoundFilerStatus = false;
		boolean             bEncounteredEndOfTable = false;
		String              ElementStr = "td";  // we are positioned at the first one already
		Node                n;
		String              lastMatchedStr = "";
		double              dConfidence = 0;
		ConfidenceLevel     conLevel = new ConfidenceLevel();
		double              dMaxConfidence = 0;
		CONSTANTS           constants = new CONSTANTS();
		
	    bEncounteredEndOfTable = mySqlAccess.FilerStatusFound(con, subInfo.getCompanyId());
		while((bEncounteredEndOfTable == false) && (bFoundFilerStatus == false)) {
        	if(ElementStr.equals("td")) {
   	            for(int j = 0; j < fieldMatch.length; j++) {
	                dConfidence = conLevel.compareToArrayList(NodeData, fieldMatch[j].Al, fieldMatch[j].getFieldStr());
	                if(dConfidence >= fieldMatch[j].getThreshold()) {
	                	if(dConfidence > dMaxConfidence) {
	                	    lastMatchedStr = fieldMatch[j].getFieldStr();
	                	    dMaxConfidence = dConfidence;
	                	}
	                }
   	            }
   	            if(NodeData.toLowerCase().indexOf("x") != -1)
   	            	bFoundFilerStatus = true;
   	            else   // if we did not find 'X' then reset Maximum confidence
   	            	dMaxConfidence = 0;
        	}
			if(bFoundFilerStatus == false) {  // check if we skip to next
				n=iterator.nextNode();
				if(n == null)
					bEncounteredEndOfTable = true;
				else {	
				    ElementStr = ((Element) n).getTagName();
           	        if(constants.EqualsEndTable(ElementStr)) {
            	        bEncounteredEndOfTable = true;
           	        }
           	        else {
           	    	    if(ElementStr.equals("td")) {
                    	    NodeList fstNm = n.getChildNodes();
                    	    int iLen = fstNm.getLength();
                    	    if(iLen > 0) {
                    	        NodeData = (fstNm.item(0)).getNodeValue();
                    	        if((NodeData == null) || (NodeData.trim().length() == 0)){
                    	    	    ElementStr = "tr";  // just skip it
                    	        }
                    	    }
                    	}
           	    	}
           	    }
			}
		}
		if(bFoundFilerStatus == true) {
			mySqlAccess.InsertFilerStatus(con, subInfo.getCompanyId(), lastMatchedStr);
		}
	}
	
	private void ProcessStocks(Connection con, SubmissionInfo subInfo, String NodeData,   // static
			                          NodeIterator iterator) {
		ArrayList<String> tableNodes = new ArrayList<String>();
		CombinedStock     combinedStock = new CombinedStock();
        String            ElementStr = "";
        String            StrText;
        String            dateStr = "";
		MySqlAccess       mySqlAccess = new MySqlAccess();
        
		tableNodes.add(NodeData.toLowerCase());
        for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
             ElementStr = ((Element) n).getTagName();
             if(ElementStr.equals("td")) {
                 NodeList fstNm = n.getChildNodes();
                 if(fstNm.item(0) != null) {
                     StrText = (fstNm.item(0)).getNodeValue();
                     if((StrText != null) && (StrText.trim().length() > 0)){  // we got some text
                    	 tableNodes.add(StrText);
                     }
                 }
             }
        }
        dateStr = combinedStock.ProcessTableStockNumbers(tableNodes);
		mySqlAccess.WriteCombinedStockRecord(con, subInfo, combinedStock, dateStr);
	}
	
	private DateRefCls ParseTemplateType(ArrayList<String> mySection, int iTemplateParseUid, int iTemplateType,  // static
                                         Connection con, SubmissionInfo si, int htmlBeginLineNum,
                                         int htmlEndLineNum, int beginLineNum, int endLineNum,
                                         TdColSpan tdColSpan, TemplateHdr templateHdr, ArrayList<String> dateForms,
                                         String IdentifiedText, DateRefCls dateRefUid) {
		
		//ArrayList<FieldModifier> fm = new ArrayList<FieldModifier>();  
		//int                      iTblLen = 0;
        String                   TableStr = "";
        String                   curLine = "";
        boolean                  bFoundTblEnd = false;
        String	                 NodeData = "";
        //double                   dConfidence;
       // ConfidenceLevel          conLevel = new ConfidenceLevel();
        int                      iTrNdx = 0;
        int                      iTdNdx = 0;
        String                   ElementStr;
        int                      iCurLine = 0;
        FieldMatchStr[]          tblMatchArray = null;
        boolean                  bSkipToNextRow = false;
        boolean                  bFieldMatched;
        int                      iMyDateFmt = 0;
        int                      iNumDateLines = 0;
        //boolean                  bContinueMatching = true;
        //ArrayList<Integer>       DateRefUid = new ArrayList<Integer>();
		ErrorCls                 errorCls = new ErrorCls();
		ColumnPosInfo            columnPosInfo = new ColumnPosInfo();
		int                      iTableNdx = 0;
		UnMappedRowCls           RawDataCls = new UnMappedRowCls();
		CONSTANTS                constants = new CONSTANTS();
		DateFmts                 dateFmts = new DateFmts();
		MySqlAccess              mySqlAccess = new MySqlAccess();
		WriteFieldData           writeFieldData = new WriteFieldData();
			
		errorCls.setFunctionStr("ParseTemplateType");
	    errorCls.setSubUid(si.getUid());
	    errorCls.setCompanyUid(si.getCompanyId());
	    errorCls.setItemVersion(si.getCompanyId());    
	    String strTable = mySection.get(iCurLine).trim();
	    int iTable = constants.CheckForBeginTable(strTable);
	    RawDataCls.setTaggedFieldUid(0);
	    if(iTable == -1) {
	    	errorCls.setErrorText("Invalid start of table: " + strTable);
	    	mySqlAccess.WriteAppError(con, errorCls);
	    	return(dateRefUid);
	    }
        tblMatchArray = mySqlAccess.GetFieldIdentifiers(con, 1, si.getCompanyId(), iTemplateType);
        //bIsStockLineAllowed = MySqlAccess.IsStockAllowed(con, iTemplateType);
        try {
        	iMyDateFmt = mySqlAccess.GetTableDateFormat(con, si.getCompanyId(), iTemplateType);
        	columnPosInfo.iCurColumn = 0;
        	columnPosInfo.iThisColumnWidth = 0;
        	//tdColSpan.getTableColumns(con, si.getHtmlFile(), htmlBeginLineNum, htmlEndLineNum);
        	boolean bFoundTable = false;
            while(bFoundTblEnd != true) {
                curLine = mySection.get(iCurLine);
                if (iTable != 0) {
                    curLine = curLine.substring(iTable);
                    iTable = 0;
                }
                //iTblLen++;
                iCurLine++;
           	    if(bFoundTable == false) {
                	iTableNdx = constants.ContainsDOMbegin(curLine);
           	        if(iTableNdx != -1)
                	    bFoundTable = true;
                }
                if(bFoundTable == true) {
                    iTableNdx = constants.CheckForEndTable(curLine);
                    if(iTableNdx == -1)
                        TableStr += curLine;
                    else {  //  table end yet??
                	    TableStr += curLine.substring(0, (iTableNdx + 8));
                        bFoundTblEnd = true;	             // YES start the processing
                        IsRealTemplate isRealTemplate = new IsRealTemplate();
                        if(isRealTemplate.Test(si, TableStr, iTemplateParseUid, tblMatchArray, con)) {
                        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        	DocumentBuilder db = dbf.newDocumentBuilder();
                        	InputSource is = new InputSource();
                        	is.setCharacterStream(new StringReader(TableStr));

                        	Document doc = db.parse(is);
                        //spewNodes(doc);
                        	DocumentTraversal traversal = (DocumentTraversal) doc;
                        // we do preNorml because we need all the tags
                        // the other has empty tags removed
                        //right now others use it we should make a database entry
                        //also in template7
                        	Document docx = dateFmts.GetPreNorml(con, si, beginLineNum, endLineNum);
                        	if(IdentifiedText.indexOf("CONTINUATION") == 0) {
                        		dateRefUid = dateFmts.GetPreviousRefs(doc, con, si, iTemplateType, dateRefUid);
                        		iNumDateLines = dateRefUid.DateRefUid.get((dateRefUid.DateRefUid.size() -1));
                    		    WriteAnyAbstractsFoundInDate(con, si, iTemplateType, iTemplateParseUid, tblMatchArray, dateRefUid, iTrNdx, iTdNdx);
                       	    }
                        	else {
                        	    dateRefUid = dateFmts.GetTableDates(con, doc, docx, iMyDateFmt, si, iTemplateType, 
                        		          	                        iTemplateParseUid, tdColSpan, templateHdr, dateForms,
                        		          	                        tblMatchArray);
                        	    if(dateRefUid.DateRefUid.size() == 0)
                        		    iNumDateLines = 0;
                        	    else {
                        		    iNumDateLines = dateRefUid.DateRefUid.get((dateRefUid.DateRefUid.size() -1));
                        		    WriteAnyAbstractsFoundInDate(con, si, iTemplateType, iTemplateParseUid, tblMatchArray, dateRefUid, iTrNdx, iTdNdx);
                        	    }
                        	}
                        	NodeIterator iterator = traversal.createNodeIterator(
                                                doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
                        	Node n = iterator.nextNode();
                        	boolean bIterate = true;
                        	while(n != null) {
//                          	for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
                        		if(bIterate) {
                        			ElementStr = ((Element) n).getTagName();
                                //System.out.println("ELEMENT: " + ElementStr);
                        			if(ElementStr.equals("tr") || ElementStr.equals("TR")) {
                        				iTrNdx++;
                        				iTdNdx = 0;
                        				bSkipToNextRow = false;
                        				columnPosInfo.iCurColumn = 0;
                        				columnPosInfo.iThisColumnWidth = 0;
                        				RawDataCls.setTaggedFieldUid(0);
                        			}
                        			else {
                        				if(ElementStr.equals("td") || ElementStr.equals("TD")) {
                        					columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;
                        					if(tdColSpan.TblRow.get(iTrNdx-1).size() > iTdNdx)
                        						columnPosInfo.iThisColumnWidth = tdColSpan.TblRow.get(iTrNdx-1).get(iTdNdx);
                        					iTdNdx++;
                        				}
                        			}
                        			NodeList fstNm = n.getChildNodes();
                        			NodeData = null;
                        			if(fstNm.getLength() > 0) {
                        				if(fstNm.item(0) != null) {
                        					NodeData = (fstNm.item(0)).getNodeValue();
                        				}
                        			}
                        		}
                        		bIterate = true;
                        		if(iTrNdx > iNumDateLines) {
                        			if(bSkipToNextRow == false) {
                        				if(NodeData != null) {
                        					NodeData = NodeData.trim();
                        					if(NodeData.length() > 0) {
                        						if((NodeData.equals(")") && (RawDataCls.GetAddedParen() == true)))
                        								RawDataCls.SetAddedParen(false);
                        						else {
                    								RawDataCls.SetAddedParen(false);
                      						    //System.out.println("NodeData: " + NodeData);
                        						    bFieldMatched = false;
                        						    //bContinueMatching = true;
                        						    CheckConceptRtn checkConceptRtn = new CheckConceptRtn();
                        						    checkConceptRtn = constants.CheckForConcepts(NodeData, tblMatchArray);
                        						    if(checkConceptRtn.iMatchIndex != -1) {
                        							    if(tblMatchArray[checkConceptRtn.iMatchIndex ].getAbstract() == true ) {
                        								    if(writeFieldData.CheckForNoData(con, n, dateForms) == true) {
                        									    mySqlAccess.writeFieldLocated(con, si.getCompanyId(), si.getUid(), 
                                                                                              si.getVersion(), iTemplateType, iTemplateParseUid, 
                                                                                              tblMatchArray[checkConceptRtn.iMatchIndex ].getUid(), 
                                                                                              NodeData, 0, true, tblMatchArray[checkConceptRtn.iMatchIndex ].getDestination(),
                                                                                              0, iTrNdx, iTdNdx);
                        									    bSkipToNextRow = true; //WLW 
                        								    }
                        								    else {   // it's a dup of another so make our own!!
                        									    columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;  // prime with current
                        									    writeFieldData.DbWriteFieldNotFound(con, si, 
                                   		                                                            iTemplateType, iTemplateParseUid, 0, n, 
                                		                                                            iTrNdx, iTdNdx, dateRefUid.DateRefUid, htmlBeginLineNum,
                            	    	                                                            columnPosInfo,tdColSpan.TblRow.get(iTrNdx-1),
                            		                                                                NodeData, si.getCompanyId());
                        									    bSkipToNextRow = true;
                        								    }
                        							    }
                        							    else {
                        								    columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;  // prime with current
                        								    bSkipToNextRow = writeFieldData.DbWriteFieldData(con, si, 
                                                        	                        	                     iTemplateType, iTemplateParseUid,
                                                        		                                             tblMatchArray[checkConceptRtn.iMatchIndex ], n, 
                                                        		                                             iTrNdx, iTdNdx, dateRefUid.DateRefUid, null,
                                                    	    	                                             columnPosInfo,tdColSpan.TblRow.get(iTrNdx-1),
                                                    		                                                 NodeData, false);  // -1 because tables normalized
                        							    }
                        						    }  // else
                        						    else {  // we didn't find a match so write a not found record
                        							    if(bFieldMatched == false) {
                        								     if((iTdNdx == 1) || (dateRefUid.DateRefUid.get(columnPosInfo.iCurColumn) == 0)) {  // check if we processed this last line
                        									      columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;  // prime with current
                        									      writeFieldData.DbWriteFieldNotFound(con, si, 
                               		                                                                  iTemplateType, iTemplateParseUid,0, n, 
                            		                                                                  iTrNdx, iTdNdx, dateRefUid.DateRefUid, htmlBeginLineNum,
                        	    	                                                                  columnPosInfo,tdColSpan.TblRow.get(iTrNdx-1),
                        		                                                                      NodeData, 0);
                        									     bSkipToNextRow = true;
                        								    }
                        								    else {   // this is a field with nothing on column 1
                                       	    	            //XXXX
                                          	                //columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;  // prime with current
                        									    RawDataCls = writeFieldData.DbWriteFieldRawData(con, si, 
                               		                                                                            iTemplateType, iTemplateParseUid, iTrNdx,
                            		                                                                            iTdNdx, dateRefUid.DateRefUid, htmlBeginLineNum,
                        	    	                                                                            columnPosInfo,
                        		                                                                                NodeData, 0, RawDataCls);
                        								    }
                        							    }
                        							}
                        						}
                        					}
                        				}
                        			}
                        		}
                        		if(bIterate)
                        			n = iterator.nextNode();
                        	}
                        }  // end if it's a real template
                    }
                }
            }  // end of code that processes the table
        }
        catch (Exception e) {
            //System.out.println("Document build error: " + e.getMessage());
		    errorCls.setErrorText("Document build error:"  + e.getMessage());
		    errorCls.setBExit(false);
		    mySqlAccess.WriteAppError(con, errorCls);
        }
        return(dateRefUid);
    }
	
	private void WriteAnyAbstractsFoundInDate(Connection con, SubmissionInfo si, int iTemplateType, int TemplateUid,  // static
			                                         FieldMatchStr[] tblMatchArray, DateRefCls dateRefUid, int iTrNdx, int iTdNdx) {
		String             AbstractStr = "";
		int                iTblMatchNdx = 0;
		int                iDateRefNdx = 0;
        double             dConfidence;
        ConfidenceLevel    conLevel = new ConfidenceLevel();
		boolean            bContinue = true;
		boolean            bWriteAbstract = false;
		boolean            bUseAbstract = false;
	    MySqlAccess        mySqlAccess = new MySqlAccess();
	    
		// check all records 
		if(dateRefUid.AbstractStr.length() > 0) {
			bUseAbstract = true;
			iDateRefNdx--;
		}
		while(iDateRefNdx < (dateRefUid.DateRefUid.size() -1)) {
			if(bUseAbstract) 
				AbstractStr = dateRefUid.AbstractStr;
			else {
				if(iTemplateType == 7) {  // we do not process them
					AbstractStr = "";
				}
				else {
			        AbstractStr = mySqlAccess.ReadSupTextInDateRef(con, dateRefUid.DateRefUid.get(iDateRefNdx));
				}
			}
			if(AbstractStr.length() > 0) {  // check if abstract string
				iTblMatchNdx = 0;
			    bContinue = true;
			    bWriteAbstract = false;
			    while((bContinue) && (iTblMatchNdx < tblMatchArray.length)) {
			    	if(tblMatchArray[iTblMatchNdx].getAbstract() == true) { // only check for abstract
		                dConfidence = conLevel.compareToArrayList(AbstractStr, tblMatchArray[iTblMatchNdx].Al, tblMatchArray[iTblMatchNdx].getFieldStr());
		                if(dConfidence >= tblMatchArray[iTblMatchNdx].getThreshold()) {
		                	bContinue = false;
		                	bWriteAbstract = true;
		                }
		                else
		                	iTblMatchNdx++;
			    	}
			    	else
			    		iTblMatchNdx++;
			    }
				if(bWriteAbstract) {
					mySqlAccess.writeFieldLocated(con, si.getCompanyId(), si.getUid(), 
			                si.getVersion(), iTemplateType, TemplateUid, tblMatchArray[iTblMatchNdx].getUid(), 
			                AbstractStr, 0, tblMatchArray[iTblMatchNdx].getAbstract(), 
			                tblMatchArray[iTblMatchNdx].getDestination(), 0, iTrNdx, iTdNdx);
					if((bUseAbstract == false) && (iTemplateType != 7))
					    mySqlAccess.ClearSupText(con, dateRefUid.DateRefUid.get(iDateRefNdx));
				}
			}
			bUseAbstract = false;
			iDateRefNdx++;
		}
	}

	
	public void setPrivInputFile(String privInputFile) {
		this.privInputFile = privInputFile;
	}

	public String getPrivInputFile() {
		return privInputFile;
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
	
}