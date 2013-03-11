package com.bricksimpe.rdg.Xbrlutil;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.sql.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

//import com.bricksimpe.rdg.util.EdgarExtractor.TableUpdate;

public class XbrlEdgarExtractor {
	private String privInputFile = "";
	private String privOutputFile = "";
	private String privPrevUid = "";
	private String privServerName = "";
	private String privInstance = "";
	private String privPortNumber = "";
	private String privDataBase = "";
	private String privDisplayName = "";
	private String privUserName = "";
	private String privPassword = "";
	private Boolean useWindows = false;
    private Connection privCon = null;
    
	private String privCompanyName = "";
	/**
	 * @param <HtmlTag>
	 * @param args
	 * File name to remove html tags
	 */
	public int RunThis()
	{
		int iRtn = 0;
		
		iRtn  = run(getPrivInputFile(), getPrivOutputFile(),
				getPrivPrevUid(), getPrivServerName(), 
				getPrivPortNumber(), getPrivInstance(),
				getPrivDataBase(), getPrivDisplayName(),
				getPrivUserName(), getPrivPassword(), getPrivUseWindows(),
				getPrivCompanyName(), getPrivCon());
		return(iRtn);
	}
	
	public static void main(String[] args) { 
		//example of call event function
		//Object o = null;
		//eventState.getEventFunctions(0).call(o);
		XbrlEdgarExtractor main = new XbrlEdgarExtractor();
		// TODO Auto-generated method stub
		int iRtn = 0;

		String InputFile = args[0];
		String OutputFile = args[1];
		String PrevUid = args[2];
		String ServerName = args[3];
		String PortNumber = args[4];
		String Instance = args[5];
		String DataBase = args[6];
		String DisplayName = args[7];
		String UserName = args[8];
		String Password = args[9];
		String CompanyName = args[10];
		Boolean useWindows = Boolean.valueOf(args[11]);
		iRtn = main.run(InputFile, OutputFile, PrevUid, ServerName, PortNumber, Instance, 
				   DataBase, DisplayName, UserName, Password, useWindows, CompanyName, null);
		iRtn = iRtn;   // RETURN CLEAN
	}
	 
		private int run(String InputFile, String OutputFile, String PrevUid, // static
				String SeverName, String PortNumber, String Instance, String DataBase,
				String DisplayName, String UserName, String Password, Boolean useWindows,
				String CompanyName, Connection privCon) {
		Version          thisVersion = new Version();
		int              iRtn = 0;
		MySqlAccess      mysq = new MySqlAccess();
		Connection       con = null;
		EventState       eventState = new EventState();

		int              iPrevUid = Integer.parseInt(PrevUid);
		String           strLine;
		char[]           strArray;
		int              i;
		int              charEvent;
		FileInputStream  fstream;
		FileOutputStream ostream;
		DataInputStream  in;
		DataOutputStream out;
		BufferedReader   br;
		BufferedWriter   wr;
		boolean          bPosInsertSpace = false;
		EventRtnCls      rtnEventCls = new EventRtnCls();
		ErrorCls         errorCls = new ErrorCls();
		int              LastTableLineNum = 0;
		boolean          bAddedLine = false;
		String           IntermediateFile = "";
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("Run");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);
		errorCls.setErrorText(UserName + ": invoking Edgar version: " + thisVersion.getVersion());

		// these are used to hold the cross references of file line(CarriageReturns to HTML lines)
		int              carriageRtnCounter = 0;
		int              htmlRtnCounter = 0;
		
        ArrayList<HtmlFileXref>  htmlFileXref = new ArrayList<HtmlFileXref>();
        
		try {
			boolean exists = (new File(OutputFile)).exists();
			if(exists) {
				exists =  (new File(OutputFile)).delete();
				if(!exists) {
					System.out.println("Cannot delete destination file");
					return(-1);
				}
			}
			if(privCon == null)
			    con = mysq.OpenConnection(SeverName, Instance, PortNumber, DataBase, UserName, Password,useWindows);
			else
				con = privCon;
			if(con == null)
				return(-2);
			else {
				mysq.WriteAppError(con, errorCls);
		    	mysq.GetHtmlTags(con, eventState);
		    	mysq.GetSubstitutionStrs(con, eventState);
		    	IntermediateFile = RemoveHighByteCharacters(con, InputFile);
		    	if(IntermediateFile.length() == 0)
		    		return(-1);
			    fstream = new FileInputStream(IntermediateFile);
			    ostream = new FileOutputStream(OutputFile);
			    in = new DataInputStream(fstream);
			    out = new DataOutputStream(ostream);
			    br = new BufferedReader(new InputStreamReader(in));
			    eventState.wr = new BufferedWriter(new OutputStreamWriter(out));
			    eventState.FixTables();
			    eventState.ResetCurState();   // initialize event state tables
			    rtnEventCls.bWroteWithoutLf = false;
			    rtnEventCls.iOpenDivCount = 0;
			    rtnEventCls.bDivWroteText = false;
			    boolean bDisplayFlag = false;
			    boolean bLowerCaseFile = false;
                while((strLine = br.readLine()) != null) {
                	carriageRtnCounter = carriageRtnCounter + 1;
                	if(bLowerCaseFile == false) {
                		if(strLine.indexOf("<HTML>") != -1)
                			bLowerCaseFile = true;
                	}
            	    //System.out.println(strLine);
                	// This is place to check for special characters for debugging purposes
                    if(strLine.indexOf("ZCZC") > -1) {
                	//if(carriageRtnCounter == 274) {
                	//  if(htmlRtnCounter > 144) {
                    //if(strLine.indexOf("<table") > -1) {
                		//System.out.println(strLine);
                		bDisplayFlag = true;
                	}
                	//remove leading spaces;
                	strLine = strLine.replaceAll("^\\s+", "");
                	if(bPosInsertSpace == true)
                		strLine = " " + strLine;
              	    strArray = strLine.toCharArray();
              	    //boolean bAddedNewLine = false;
             	    for(i = 0; i < strArray.length; i++) {
             	    	if(bDisplayFlag == true)
             	    	    System.out.println("Current State: " + eventState.GetCurState());
             	    	charEvent = eventState.GetCharacterEvent(strArray[i]);
             	    	rtnEventCls.bWroteLine = false;
             	    	rtnEventCls.bIsTableTag = false;
             	    	rtnEventCls.bPreceededLf = false;
             	    	if(bDisplayFlag == true)
             	    	    System.out.println("Calling eventFunction: " + charEvent +
             	    	    		           " with character: '" + strArray[i] + "'");
             	    	if(charEvent < 100)
            	    		rtnEventCls = eventState.getEventFunctions(charEvent).call(strArray[i], 
            	    				                                   bAddedLine, rtnEventCls.iOpenDivCount);
             	    	else
            	    		rtnEventCls = eventState.getEventFunctions(0).call(strArray[i], bAddedLine,
            	    				                                           rtnEventCls.iOpenDivCount);
             	    	bAddedLine = rtnEventCls.bWroteWithoutLf;
             	    	if(rtnEventCls.bIsTableTag) 
             	    		LastTableLineNum = carriageRtnCounter;
             	    	if(rtnEventCls.bPreceededLf == true)
             	    		htmlRtnCounter += 1;
             	    	if(rtnEventCls.bWroteLine) {
             	    		//rtnEventCls.bWroteWithoutLf = false;
             	    		htmlRtnCounter = htmlRtnCounter + 1;
             	    		HtmlFileXref newHtmlLine = new HtmlFileXref();
             	    		newHtmlLine.setHtmlLine(htmlRtnCounter);
             	    		newHtmlLine.setFileLine(carriageRtnCounter);
             	    		newHtmlLine.setHtmlTblLine(LastTableLineNum);
             	    		//System.out.println("<><><><><><><>");
             	    		//System.out.println("htmlLine = " + htmlRtnCounter);
             	    		//System.out.println("FileLine = " + carriageRtnCounter);
             	    		//System.out.println("htmlTableLine = " + LastTableLineNum);           	    		
             	    		htmlFileXref.add(newHtmlLine);
             	    	}
             	    	//bAddedNewLine = bAddedNewLine | rtnEventCls.bWroteLine;
             	    	if(bDisplayFlag == true)
             	    		System.out.println("New State: " + eventState.GetCurState());
             	    }
             	   eventState.wr.flush();
         	    	// now check if saving text and last character is NOT a space
         	    	if((i > 0) && (strArray[i-1] != ' ') && (eventState.SaveToTermTag > 0))
         	    		bPosInsertSpace = true;
         	    	else
         	    		bPosInsertSpace = false;
         	    	boolean bClose = false;
         	    	boolean bJustSpot;
         	    	bJustSpot = false;
         	    	if(bClose == true) {
         	    		eventState.wr.close();
         	    		eventState.wr.close();
         	    		iRtn = -1;
         	            break;
         	    	}
         	    	
                }
                in.close();
                try {
           	        if(eventState.CompletedBuffer.length() > 0) {
        	    	    //System.out.println(eventState.CompletedBuffer);
        	    	    eventState.wr.write(eventState.CompletedBuffer);
        	            if(eventState.SaveToTermTag  == 0)
        	        	    eventState.wr.newLine();
        	            eventState.CompletedBuffer = "";
        	        }
                   eventState.wr.flush();
                }
                catch (Exception e) {
                	//System.out.println("Unable to flush output file: " + e.getMessage());
                	errorCls.setErrorText("Unable to flush output file: " + e.getMessage());
                	mysq.WriteAppError(con, errorCls);
                }
                out.close();
                //if(bLowerCaseFile == true) {
                iRtn = LowerCaseFile(con, OutputFile, bLowerCaseFile);
                if(iRtn != 0)
                	return(iRtn);
                //}
                SubmissionInfo  subInfo = new SubmissionInfo();;
	            HtmlFileXref postHtmlLine = new HtmlFileXref();
 	            postHtmlLine.setHtmlLine(-1);
 	            postHtmlLine.setFileLine(carriageRtnCounter);
 		        htmlFileXref.add(postHtmlLine);
 		        //Xbrl Addition
 		        subInfo.setSubUid(iPrevUid);
 		        subInfo.setItemVersion(1);
                mysq.WriteHmtlFileXref(con, subInfo, htmlFileXref);
		        /*************
                if(iPrevUid == 0) {
                    iRtn = FindCompanyName(OutputFile, con, CompanyName);
                    if(iRtn > 0) {
                        subInfo = mysq.UpdateSubmissionsTable(con, DisplayName, InputFile, OutputFile, iRtn);
                        mysq.WriteHmtlFileXref(con, subInfo, htmlFileXref);
                    }
                }
                else {
                	subInfo = mysq.OverlaySubmission(con, iPrevUid, InputFile, OutputFile);
                	if(subInfo != null) {
                         mysq.WriteHmtlFileXref(con, subInfo, htmlFileXref);
               	    }
               }
               *************/
                // now find the company name in the extracted output
        	   ostream.close();
       		   fstream.close();
       		   RationalizeOutput rationalOutput = new RationalizeOutput();
       		   rationalOutput.Execute(OutputFile, con);
       		   iRtn = subInfo.getSubUid();
		    }
		}
		catch (Exception e) {
		     //System.out.println("Unable to parse input file:" + e.getMessage());	
     	    errorCls.setErrorText("Unable to parse input file:"  + e.getMessage());
    	    iRtn = mysq.WriteAppError(con, errorCls);
            //mysq.CloseConnection(con);
		}
		errorCls.setErrorText(UserName + ": Exit Edgar version: " + thisVersion.getVersion());
		mysq.WriteAppError(con, errorCls);
        //mysq.CloseConnection(con);
        File file = new File(IntermediateFile);
        file.delete();
		return(iRtn);
	}
	
	
	private int LowerCaseFile(Connection con, String FileName, boolean bLowerCase) { // static
		
	    int                       bRtn = 0;
	    String                    TempFileName = "" + UUID.randomUUID().toString();
		FileInputStream           fstream;
		FileOutputStream          ostream;
		DataInputStream           in;
		DataOutputStream          out;
		BufferedReader            br;
		BufferedWriter            wr;
		ErrorCls                  errorCls = new ErrorCls();
		String                    strLine;
		File                      file;
		File                      filefrom;
		MySqlAccess               mySqlAccess = new MySqlAccess();
		ArrayList<Replacements>   replacements = null;
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("LowerCaseFile");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);	
		replacements = mySqlAccess.GetReplacements(con);
		try {
	        fstream = new FileInputStream(FileName);
	        ostream = new FileOutputStream(TempFileName);
	        in = new DataInputStream(fstream);
	        out = new DataOutputStream(ostream);
	        br = new BufferedReader(new InputStreamReader(in));
	        wr = new BufferedWriter(new OutputStreamWriter(out));
            while((strLine = br.readLine()) != null) {
                if(bLowerCase) {
                    strLine = strLine.replace("<TABLE>", "<table>");
            	    strLine = strLine.replace("</TABLE>", "</table>");
            	    strLine = strLine.replace("<TR>", "<tr>");
            	    strLine = strLine.replace("</TR>", "</tr>");
            	    strLine = strLine.replace("<TD>", "<td>");
            	    strLine = strLine.replace("</TD>", "</td>");
                }
                for(int i = 0; i < replacements.size(); i++)
                	strLine = strLine.replace(replacements.get(i).GetOrigStr(), replacements.get(i).GetReplacementStr());
                wr.write(strLine);
                wr.newLine();
            }
            wr.flush();
            wr.close();
            out.close();
            ostream.close();
            br.close();
            in.close();
            fstream.close();
            file = new File(FileName);
            file.delete();
            filefrom = new File(TempFileName);
            file = new File(FileName);
            filefrom.renameTo(file);
		}
	    catch (Exception e) {
        	    //errorCls.setErrorText("Unable to lower case = file: " + e.getMessage());
       	    mySqlAccess.WriteAppError(con, errorCls);
		    bRtn  = -1;
		}
		return(bRtn);
	}
	
	private String RemoveHighByteCharacters(Connection con, String FileName) { // static
		
	    String           intermediateFile = "" + UUID.randomUUID().toString();
		FileInputStream  fstream;
		FileOutputStream ostream;
		DataInputStream  in;
		DataOutputStream out;
		BufferedWriter   wr;
		ErrorCls         errorCls = new ErrorCls();
		File             file;
		File             filefrom;
		int              iState = 0;
		byte             aByte;
		MySqlAccess      mySqlAccess = new MySqlAccess();
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("RemoveHighByteCharacters");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);
		    try {
	            fstream = new FileInputStream(FileName);
	            ostream = new FileOutputStream(intermediateFile);
	            in = new DataInputStream(fstream);
	            out = new DataOutputStream(ostream);
	            wr = new BufferedWriter(new OutputStreamWriter(out));
                try {
	                while(true) {
                	    aByte = in.readByte();
                	    switch (iState) {
                	        case 0:
                	    	    if(aByte == CONSTANTS.UTF_DEF)
                	    		    iState++;
                	    	    else
                	    	    	 wr.write(aByte);
                	    	    break;
                	        case 1:
                	    	    if(aByte == CONSTANTS.UTF_CONT)
                	    		    iState++;
                	    	    else {
                	    	    	wr.write(CONSTANTS.UTF_DEF);
                	    	    	wr.write(aByte);
                	    	    	iState = 0;
                	    	    	errorCls.setErrorText("Unconverted UTF-8 character sequence encountered in file: " + FileName);
                	    	    	mySqlAccess.WriteAppError(con, errorCls);
                	    	    	}
                	    	    break;
                	        case 2:
                	        	aByte = ReplaceUtf(con, wr, FileName, aByte);
                	        	iState = 0;
                	        	break;
                	    }
                   }
               }
               catch (EOFException e) {
               }
               wr.flush();
               wr.close();
               out.close();
               ostream.close();
               in.close();
               fstream.close();
		    }
		    catch (Exception e) {
        	    errorCls.setErrorText("Unable to access files: " + e.getMessage());
        	    mySqlAccess.WriteAppError(con, errorCls);
        	    intermediateFile  = "";
		    }
		return(intermediateFile);
	
	}
	
	private Byte ReplaceUtf(Connection con, BufferedWriter wr, String FileName, byte i) { // static
		Byte             rtnByte = i;
		ErrorCls         errorCls = new ErrorCls();
		MySqlAccess      mySqlAccess = new MySqlAccess();
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("ReplaceUtf");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);
		if(i == CONSTANTS.UTF_FQUOTE)
			rtnByte = CONSTANTS.UTF_FQUOTE_REP;
		else {
			if(i == CONSTANTS.UTF_BQOUTE)
				rtnByte = CONSTANTS.UTF_BQOUTE_REP;
			else {
				if(i == CONSTANTS.UTF_Dash1)
					rtnByte = CONSTANTS.UTF_Dash1_REP;
				else {
					if(i == CONSTANTS.UTF_Dash2)
						rtnByte = CONSTANTS.UTF_Dash2;
					else {
						try {
						    wr.write(CONSTANTS.UTF_DEF);
						    wr.write(CONSTANTS.UTF_CONT);
							String hex = Integer.toHexString(i);
							hex = hex.substring(hex.length() -2).toUpperCase();
   	    	    	        errorCls.setErrorText("Unconverted UTF-8 character sequence encountered in file: " + FileName + " CHAR: " + hex);
    	    	    	    mySqlAccess.WriteAppError(con, errorCls);
						}
						catch (Exception e) {
							
						}
					}
				}
			}
		}
		return(rtnByte);
	}
	
	private class TableUpdate { // static
		int     iTblLineNum;
		boolean bWithinTable;
		boolean bStateChange;
	}
	
	private TableUpdate CheckForTableMarkers(String ThisLine, boolean bCurTableState, // static
			                                        int iThisLineNum) {
		String lowerCased = ThisLine.toLowerCase();
		TableUpdate tu = new TableUpdate();
		if(lowerCased.indexOf("<table>") != -1) {
			tu.bWithinTable = true;
			tu.bStateChange = true;
			tu.iTblLineNum = iThisLineNum;
			String tempStr = lowerCased.replace("<table>", "       ");
			if(tempStr.indexOf("</table>") != -1) {
				tu.bWithinTable = false;
				tu.bStateChange = false;
			}
			
		}
		else {
			if(lowerCased.indexOf("</table>") != -1) {
				tu.bWithinTable = false;
				tu.bStateChange = true;
			}
		}
		
		return(tu);
	}
	private int FindCompanyName(String InFileName, Connection con, String UserCompanyName) { // static
		int        iCompanyId = 0;
        TableUpdate tu = new TableUpdate();
		int         iLineLastTable = -1;
		boolean     bWithinTableConstruct = false;
		int         iLineNum = 1;
		String		sCurLine = "";
		ErrorCls    errorCls = new ErrorCls();
		MySqlAccess mysq = new MySqlAccess();
		MatchStr[]  CompanyTags;
		int         iMaxLinesToRead = 100;
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("FindCompanyName");
		errorCls.setItemVersion(0);
		try {
			CompanyTags = mysq.GetListFromTbls(con, 0);
			FileInputStream fstream = new FileInputStream(InFileName);
	        DataInputStream in = new DataInputStream(fstream);
	        BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        boolean bNotFound = true;
	        String[]  strFileStr = new String[2];
	        strFileStr[0] = "XXX";  // these are populated just to have a beginning
	        strFileStr[1] = "XXX";   
	        int strFileNdx = 0;
		    //String CompanyStr = new String("(Exact name of registrant as specified in its charter)");
            double dConfidence = 0;
            String fileStr = "";
            int    k =0;
            String fkaName = "";
            
    		ConfidenceLevel           cl = new ConfidenceLevel();
            
	        while(bNotFound) {
	        	fileStr = br.readLine();
	        	fileStr = fileStr.trim();
	        	iMaxLinesToRead--;
	        	if(iMaxLinesToRead == 0) {
	        		strFileStr[strFileNdx] = "";  // clear
	        		break;
	        	}
	        	String PrevStr = (strFileNdx == 0) ? strFileStr[1] : strFileStr[0];
	        	fkaName = DoesLineContainfka(fileStr, PrevStr, fkaName);
	    	    switch (strFileNdx) {
	    		    case 0:
	    		    	if(IsEmptyLine(strFileStr[1]) == true)  // note sometimes there are MT lines so we keep the last used
	    		    		strFileStr[1] = strFileStr[0];
	    			    strFileStr[strFileNdx] = fileStr;
	    			    tu = CheckForTableMarkers(strFileStr[strFileNdx], bWithinTableConstruct, iLineNum);
	    			    if(strFileStr[strFileNdx].length() > 0) {
	                	    for(k = 0; k < CompanyTags.length; k++) {
	                	        dConfidence = cl.compareToArrayList(StripLine(strFileStr[strFileNdx]), CompanyTags[k].al);
	                	        if(dConfidence > CompanyTags[k].Threshold) {
	                	    	    bNotFound = false;
	                	     	    break;
	                	        }
	                	    }
                     // dConfidence = ConfidenceLevel.compareStrings(StripLine(strFileStr[strFileNdx]), CompanyStr);
                           // if(dConfidence > 0.85) {
                    	   //     bNotFound = false;
                           // }
                        }
	    			    strFileNdx = 1;
	    			    break;
	    		    case 1:
	    		    	if(IsEmptyLine(strFileStr[0]) == true)
	    		    		strFileStr[0] = strFileStr[1];
	    			    strFileStr[strFileNdx] = fileStr;
	    			    tu = CheckForTableMarkers(strFileStr[strFileNdx], bWithinTableConstruct, iLineNum);
	    			    if(strFileStr[strFileNdx].length() > 0) {
	                	    for(k = 0; k < CompanyTags.length; k++) {
	                	        dConfidence = cl.compareToArrayList(StripLine(strFileStr[strFileNdx]), CompanyTags[k].al);
	                	        if(dConfidence > CompanyTags[k].Threshold) {
	                	    	    bNotFound = false;
	                	     	    break;
	                	        }
	                	    }
                        }
	    			    strFileNdx = 0;
	    			    break;			    			
	    	    }
	    	    if(tu.bStateChange == true) {
	    	    	bWithinTableConstruct = tu.bWithinTable;
	    	    	iLineLastTable = tu.iTblLineNum;
	    	    }
	    	    iLineNum++;
	        }
            in.close();
            fstream.close();
            String CompanyName = "";
            if(k < CompanyTags.length) {
        	    if(CompanyTags[k].PathToData.contains("EX") == true)
        		    CompanyName = CompanyTags[k].OrigString;
        	    else {
                    if((bWithinTableConstruct == true)  && (iMaxLinesToRead > 0)){
            	        fstream = new FileInputStream(InFileName);
            	        in = new DataInputStream(fstream);
    	                br = new BufferedReader(new InputStreamReader(in));
           	            for(int j = 1; j < iLineLastTable; j++)  // skip to <table>
           	    	        sCurLine = br.readLine();
           	            sCurLine = "";
           	            String TblStr = "";
           	            TblStr = br.readLine();
           	            while(sCurLine.indexOf("</table>") == -1) {
           	    	        sCurLine = br.readLine();
           	    	        TblStr += sCurLine;
           	            }
           	            CompanyName = FindCompanyNameInTableStr(TblStr, CompanyTags[k]);
           	            in.close();
                    }
                    else {
            	        if(fkaName.length() > 0)
            		        CompanyName = fkaName;
            	        else
            	            CompanyName = strFileStr[strFileNdx];
            	        boolean bByTable = false;
            	        if(CompanyName.indexOf("</table>") != -1) {
            	            CompanyName = CompanyName.replace("</table>", "");  // this is where string found but company name in preceding table
            	            bByTable = true;
            	        }
            	        if(CompanyName.length() == 0)
            		        CompanyName = SecondChanceCompanyName(InFileName, bByTable);
                    }
        	    }
            }
            if(CompanyName.length() == 0)
                CompanyName = UserCompanyName;
            String EscapedCompanyName = CompanyName.replace("'", "''");
            String query = "Select Uid from Company where CompanyName = '" + EscapedCompanyName + "'";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			// check if record already exists.
			if( rs.next() == false) {
				rs.close();
		        stmt.executeUpdate("Insert into Company (CompanyName, EIN, DisplayName) values ('" + EscapedCompanyName + 
		        		           "', '00-00000', '" + EscapedCompanyName + "')", Statement.RETURN_GENERATED_KEYS);
		        rs = stmt.getGeneratedKeys();
		        rs.next();
			}
	        iCompanyId = rs.getInt(1);
		    stmt.close();
		}
		catch (Exception e) {
			 //System.out.println("Unable to parse Extract file " + e.getMessage());	
         	errorCls.setErrorText("Unable to parse Extract file: " + e.getMessage());
        	mysq.WriteAppError(con, errorCls);
	    }
		return(iCompanyId);
	}
	
	private String DoesLineContainfka(String origStr, String PrevStr, String Curfka) { // static
		String rtnStr = Curfka;
		int    iNdx;
		
		iNdx = origStr.indexOf("f/k/a");
		if(iNdx != -1) {
			if(iNdx == 0)
				rtnStr = PrevStr;
			else {
				rtnStr = origStr.substring(0, iNdx);
				rtnStr = rtnStr.trim();
				String Junk = rtnStr.substring(rtnStr.length()-1);
				if(Junk.indexOf(",") != -1) {
					rtnStr = rtnStr.substring(0, rtnStr.length()-1);
					rtnStr = rtnStr.trim();
				}
			}
		}
		return(rtnStr);
	}
	private String FindCompanyNameInTableStr(String tableStr, MatchStr matchStr) { // static
		String           CompanyName = "";
		String           ElementStr = "";
		String           NodeData = "";
		ConfidenceLevel  cl = new ConfidenceLevel();
		double           dConfidence = 0;
		String           strWithinNode = "";
		
		try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(tableStr));

            Document doc = db.parse(is);
		    DocumentTraversal traversal = (DocumentTraversal) doc;
		
            NodeIterator iterator = traversal.createNodeIterator(
                           doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
            for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
      	        ElementStr = ((Element) n).getTagName();
       		    if(ElementStr.equals("td")) {
        	        NodeList fstNm = n.getChildNodes();
        	        int iLen = fstNm.getLength();
        	        if(iLen > 0) {
        	            NodeData = (fstNm.item(0)).getNodeValue();
        	            if(NodeData != null) {
        	    	        NodeData = NodeData.trim();
        	                if(NodeData.length() > 0) {
           	                    dConfidence = cl.compareToArrayList(StripLine(NodeData), matchStr.al);
        	                    if(dConfidence > matchStr.Threshold) 
        	     	                break;
        	     	            else {
        	     	            	if(NodeData.contains("Exact name of registrant") == true) { 
        	     	            		//matchStr.OrigString) == true) {
        	     	            		int iBegin = NodeData.indexOf("(");
        	     	            		int iEnd = NodeData.indexOf(")");
        	     	            		if((iBegin != -1) && (iEnd != -1) && (iBegin < iEnd)) {
        	     	            			strWithinNode = NodeData.substring(0, iBegin).trim();
        	     	            		}
        	     	            	}
        	     	            	else
        	     	    	            CompanyName = NodeData;
       	     	                }
        	                }
        	            }
        	        }
        	    }
       		}
        }
		catch (Exception e) {
			return("");
		}
		if(dConfidence < matchStr.Threshold) {
			if(strWithinNode.length() > 0)
				CompanyName = strWithinNode;
			else
				CompanyName = "";
		}
		return (CompanyName);
	}
	private String SecondChanceCompanyName(String FileName, boolean bByTable) { // static
		String          rtnStr = "";
		String          fileStr = "";
		int             iMaxLines = 100;
        boolean         bNotFound = true;
        FileInputStream fstream  = null;
        DataInputStream in = null;
        BufferedReader  br = null;
        CONSTANTS       constants = new CONSTANTS();
        
		try {
			fstream = new FileInputStream(FileName);
	        in = new DataInputStream(fstream);
	        br = new BufferedReader(new InputStreamReader(in));
	        while(bNotFound) {
	        	iMaxLines--;
	        	if(iMaxLines == 0)
	        		break;
	        	fileStr = br.readLine();
	        	fileStr = fileStr.trim();
	        	fileStr = fileStr.toLowerCase();
	        	if(fileStr.indexOf("commission file number") != -1) 
	        	    bNotFound = false;
	        }
		    if(bNotFound == false) {
		    	while(rtnStr.length() == 0) {
		    		rtnStr = br.readLine();
		    		rtnStr = rtnStr.trim();
		    		if(bByTable) {
		    			rtnStr = constants.RemoveRemHtmlTags(rtnStr);
		    			rtnStr = rtnStr.trim();
		    		}
		    	}
		    }
		    br.close();
		    in.close();
		    fstream.close();
		}
	    catch (Exception e) {
	        return(rtnStr);
	    }
		return(rtnStr);
	}
	
	private boolean IsEmptyLine(String origLine) { // static
		boolean bRtn = false;
		String  TestStr; 
	
		origLine = origLine.trim();
		if(origLine.length() == 0)
			bRtn = true;
		else {
			TestStr = origLine.replace("-", "");
			TestStr = TestStr.trim();
			if(TestStr.length() == 0)
				bRtn = true;
			else {
				if(TestStr.indexOf("formerly") != -1)
					bRtn = true;
			}
		}
		return(bRtn);
	}
	private String StripLine(String OrigLine) {  // static
		String rtnStr = "";
		
		rtnStr = OrigLine.replace("<table>", "");
		rtnStr = rtnStr.replace("<tr>", "");
		rtnStr = rtnStr.replace("<td>", "");
		rtnStr = rtnStr.replace("</td>", "");
		return(rtnStr);
	}
	public void setPrivInputFile(String privInputFile) {
		this.privInputFile = privInputFile;
	}
	public String getPrivInputFile() {
		return privInputFile;
	}
	public void setPrivOutputFile(String privOutputFile) {
		this.privOutputFile = privOutputFile;
	}
	public String getPrivOutputFile() {
		return privOutputFile;
	}
	public void setPrivUseWindows(Boolean privOutputFile) {
		this.useWindows = privOutputFile;
	}
	public Boolean getPrivUseWindows() {
		return useWindows;
	}
	public void setPrivPrevUid(String privPrevUid) {
		this.privPrevUid = privPrevUid;
	}
	public String getPrivPrevUid() {
		return(privPrevUid);
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
	public void setPrivDisplayName(String privDisplayName) {
		this.privDisplayName = privDisplayName;
	}
	public String getPrivDisplayName() {
		return privDisplayName;
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

	public void setPrivCompanyName(String privCompanyName) {
		this.privCompanyName = privCompanyName;	
	}

	public String getPrivCompanyName() {
		return privCompanyName;
	}
	
	public void setPrivCon(Connection con) {
		this.privCon = con;
	}	
	public Connection getPrivCon() {
		return privCon;
	}

}
