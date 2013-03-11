
package com.bricksimpe.rdg.templateId;


import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class MySqlAccess {
	//if set true this enables detailed debug statements to the error log
	boolean bDebug = false;  //static
	
	static String[] TblCountQueries = new String[] {"Select count(*) from formidentification",
		                                            "Select count(*) from TemplateId where Formid = "};
	static String[] TblEntryQueries = new String[] {"Select * from formidentification",
		                                            "Select StrId from templateidentifier where FormId = "};
	private static boolean useJTDS = true;
	public Connection OpenConnection(String serverName, String dbInstance, String portNumber,  //static
            String dataBase, String UserName, String Password, Boolean useWindows)
    {
		
        Connection con = null;
        try {
        	 String connectionString = "jdbc:jtds:sqlserver://" + serverName + ":" + portNumber + "/" + dataBase +";"
     	 	         + "instance = " + dbInstance + "; databaseName=" + dataBase +";" ;
        	 connectionString += "bufferMaxMemory=204800; cacheMetaData=true; maxStatements=2147483647;" +
     	 	                     " sendStringParametersAsUnicode=false; socketKeepAlive=true;";
        	 if(useWindows) {
 	 	    	  connectionString+= "domain=RDGFILE; namedPipe=true;";
 	 	     }
        	 if(!useJTDS) {
            	 connectionString = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";"
         	 	         + "instance = " + dbInstance + "; databaseName=" + dataBase +";" ;
            	 Driver d = (Driver) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        	 } else {
        		 Driver d = ( Driver ) Class.forName( "net.sourceforge.jtds.jdbc.Driver").newInstance( );
        	 }	 	      DriverManager.setLoginTimeout( 5 );
	 	      con = DriverManager.getConnection( connectionString, UserName, Password );
        if(con.isClosed()){
            System.out.println("Unable to open SQL Connection");
            con.close();
            con = null;
        }
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            //System.exit(-1);
        }
        return(con);
    }

	public ArrayList<NoteFmtCls> GetNoteFormats(Connection con) {  //static
		ArrayList<NoteFmtCls> rtnCls = new ArrayList<NoteFmtCls>();
		String                query;
		ResultSet             rs;
		PreparedStatement     pstmt;
		NoteFmtCls            curRec;
		
		try {
			query = "Select * from Notefmt order by UID";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
			    curRec = new NoteFmtCls();
			    curRec.FmtStr = rs.getString(2);
			    curRec.lTrim = rs.getBoolean(3);
			    rtnCls.add(curRec);	
			}
			rs.close();
		}
	    catch (SQLException e) {
			System.err.print("Exception reading noteFmt " + e.getMessage());
	    }
		return(rtnCls);		
	}
	
	public MatchStr[] GetListFromTbls(Connection con, int TblNdx, int TblKey) {
		MatchStr[] newList = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    int iCount;
	    
		try {
			pstmt = con.prepareStatement(TblCountQueries[TblNdx] + TblKey);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				iCount = rs.getInt(1);
				rs.close();
				newList = new MatchStr[iCount];
			}
		}
	    catch (SQLException e) {
			System.err.print("Exception reading html_tags " + e.getMessage());
	    }
		return newList;
	}
	
	//note this routine will not return
	private void GenericErrorCls(Connection con, String OrigFunction, String errorText,
			                     boolean bExit) {
		ErrorCls errorCls = new ErrorCls();
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr(OrigFunction);
		errorCls.setItemVersion(0);
		errorCls.setBExit(bExit);
    	errorCls.setErrorText(errorText);
    	WriteAppError(con, errorCls);

	}

	public String GetFormString(Connection con, int TblNdx) {
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
		String FormIdStr = "";
		
		try {
	    	pstmt = con.prepareStatement("Select FormStr from formidentification where Id = " + (TblNdx+1));
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            FormIdStr = rs.getString(1);
	            rs.close();
	            pstmt.close();	            
	    	}
		}
	    catch (SQLException e) {
			System.err.print("Exception reading template Identifiers " + e.getMessage());
			GenericErrorCls(con, "GetFormString", "Exception reading template Identifiers " + e.getMessage(), true);
	    }
		return FormIdStr;
	
	}
	
	public String GetSrcFile(Connection con, int iSubUid, int iVersion) {
		String rtnStr = "";
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
		
		try {
	    	pstmt = con.prepareStatement("Select SrcFile from Submissions where Uid = " +
	    			                     iSubUid + " and Version = " + iVersion);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            rtnStr = rs.getString(1);
	            rs.close();
	            pstmt.close();	            
	    	}
		}
	    catch (SQLException e) {
			System.err.print("Exception reading template Identifiers " + e.getMessage());
			GenericErrorCls(con, "GetSrcFile", "Exception reading template Identifiers " + e.getMessage(), true);
	    }
		return rtnStr;
			
	}
	
	public ArrayList<NodeIdStr> GetNodeIdentifiers(Connection con, int iCompanyId) {
		ArrayList<NodeIdStr> newList =  new ArrayList<NodeIdStr>();
	    String            query = "SELECT Uid, NoteText From NoteGaapXref where CompanyId = " +
	                              iCompanyId + " OR CompanyId = 0";
	    PreparedStatement pstmt;
	    ResultSet         rs;
	    NodeIdStr         thisNode;
	    
	    try {
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	while (rs.next()) {
	    		thisNode = new NodeIdStr();
	    		thisNode.setUid(rs.getInt(1));
	    		thisNode.setNoteStr(rs.getString(2));
	    		thisNode.setAl(rs.getString(2));
	    		newList.add(thisNode);
	    	}
		}
	    catch (SQLException e) {
			System.err.print("Exception reading template Identifiers " + e.getMessage());
			GenericErrorCls(con, "GetTemplateIdentifiers", "Exception reading html_tags " + e.getMessage(), true);
	    }
		return(newList);
	}
	
	public TemplateIdStr[] GetTemplateIdentifiers(Connection con, int TblNdx) {
		TemplateIdStr[] newList = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
		int iCount;
		String QueryStr = "";
		
		try {
			TblNdx = 1;  // forced 
	    	pstmt = con.prepareStatement("Select count(*) from TemplateIdentifier where FormId = " + TblNdx);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new TemplateIdStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new TemplateIdStr();
	            }
	            rs.close();
	            pstmt.close();	            
		        QueryStr = "Select TI.Uid, TI.StrId, TI.StartType, TI.TermType, TI.TermStr, TI.TemplateId, ST.Id, ST.StrText " +
		        		"from TemplateIdentifier TI, StrTable ST where TI.FormId = " + TblNdx + 
		        		" and TI.StrId = ST.Id";
		        pstmt = con.prepareStatement(QueryStr);
		        rs = pstmt.executeQuery();
		        iCount = 0;
		        while(rs.next()) {
		        	int j = rs.getInt(1);
		    	    newList[iCount].setUid(rs.getInt(1));
		    	    newList[iCount].setStartType(rs.getInt(3));
		    	    newList[iCount].setTermType(rs.getInt(4));
		    	    newList[iCount].setTermStr(rs.getString(5));
		    	    newList[iCount].setTemplateId(rs.getInt(6));
		    	    newList[iCount].setStrTblId(rs.getInt(7));
		    	    newList[iCount].setTempStr(rs.getString(8));
	        	    newList[iCount].setAl(rs.getString(8));
		        	iCount++;
		        }
		        rs.close();
		        pstmt.close();
	    	}
		}
	    catch (SQLException e) {
			System.err.print("Exception reading template Identifiers " + e.getMessage());
			GenericErrorCls(con, "GetTemplateIdentifiers", "Exception reading html_tags " + e.getMessage(), true);
	    }
		return newList;
	}
		
	public MatchStr[] GetListFromTbls(Connection con, int TblNdx) {
		MatchStr[] newList = null;
		ConfidenceLevel cl = new ConfidenceLevel();
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    
	    int iCount;
	    
	    try {
	    	pstmt = con.prepareStatement(TblCountQueries[TblNdx]);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new MatchStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new MatchStr();
	            }
	            //rs = stmt.executeQuery(query1);
		    	pstmt = con.prepareStatement(TblEntryQueries[TblNdx]);
		    	rs = pstmt.executeQuery();
	            iCount = 0;
	            while(rs.next()) {
	        	    newList[iCount].iType = 0;
	        	    newList[iCount].key = rs.getInt(1);	        	
	        	    newList[iCount].OrigString = rs.getString(2);
	        	    newList[iCount].al = cl.RecurringSting(rs.getString(2));
	        	    newList[iCount].bBooleanFlag = rs.getBoolean(3);
	        	    iCount++;
	            }
	            rs.close();
	    	}
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading html_tags " + e.getMessage());
			GenericErrorCls(con, "GetListFrom Tbls", "Exception reading html_tags " + e.getMessage(), true);
	    }
	    return(newList);
	}

	public MatchStr[] GetFormTags(Connection con, int TblNdx) {
		MatchStr[]         newList = null;
		ConfidenceLevel    cl = new ConfidenceLevel();
	    PreparedStatement  pstmt = null;
	    ResultSet          rs = null;
	    String             query;
	    
	    int iCount;
	    
	    try {
	    	query = "Select Count(*) from Form_Tags where Formid = " + TblNdx;
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new MatchStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new MatchStr();
	            }
	            //rs = stmt.executeQuery(query1);
		    	query = "Select Uid, tag from Form_Tags where Formid = " + TblNdx;
		    	pstmt = con.prepareStatement(query);
		    	rs = pstmt.executeQuery();
	            iCount = 0;
	            String upperCased = "";
	            while(rs.next()) {
	            	upperCased = rs.getString(2).toUpperCase();
	        	    newList[iCount].iType = 0;
	        	    newList[iCount].key = rs.getInt(1);	        	
	        	    newList[iCount].OrigString = rs.getString(2);
	        	    newList[iCount].al = cl.RecurringSting(upperCased);
	        	    iCount++;
	            }
	            rs.close();
	    	}
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading html_tags " + e.getMessage());
			GenericErrorCls(con, "GetFormTags Tbls", "Exception reading html_tags " + e.getMessage(), true);
	    }
	    return(newList);
	}

	public void CloseConnection(Connection con)
	{
		try {
		   con.close();
		}
		catch (SQLException e) {
			System.err.print("Exception closing MySql Connection: " + e.getMessage());
		}
	}
	
	public ArrayList<TOC_STRINGS> GetTableOfContentStrings(Connection con) {
		ArrayList<TOC_STRINGS> alRtn = new ArrayList<TOC_STRINGS>();
		String query = "Select * from toc_strings order by Uid";
		ResultSet rs;
		TOC_STRINGS  rsStr;
		
		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()) {
				rsStr = new TOC_STRINGS();
				rsStr.setTocID(rs.getInt(1));
			    rsStr.setTocString(rs.getString(2));
			    rsStr.setTocConfidence(rs.getDouble(3));
			    rsStr.setTocDoMatch(rs.getBoolean(4));
			    alRtn.add(rsStr);
			}
		}
		catch (Exception e) {
			System.out.println("Error reading toc_strings table: " + e.getMessage());
			GenericErrorCls(con, "GetTableOfContents", "Exception reading toc_strings " + e.getMessage(), true);
		}
		return(alRtn);
	}
	
	public SubmissionInfo UpdateSubmissionsTable(Connection con, String ExtractFileName,
			                                     int myFormId, int NewVersion) {
		SubmissionInfo subInfo = new SubmissionInfo();
		//String query = "Select * from Submissions where ExtractFile = '" + ExtractFileName + "'";
		String query = "Select * from Submissions where ExtractFile = '" + ExtractFileName + "'";
		//query = query.replace("\\", "\\\\");
		PreparedStatement  pstmt;
		
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			// check if record already exists.
			if(rs.next() == true) {
		        subInfo.setUid(rs.getInt(1));
		        subInfo.setCompanyId(rs.getInt(2));
				subInfo.setVersion(rs.getInt(7));
				rs.close();
				query = "Update Submissions set FormId = " + myFormId;
				if(NewVersion != 0) {
					subInfo.setVersion( subInfo.getVersion() +1);
					query = query + ", Version = " + subInfo.getVersion();
				}
				query = query + " where UID = " + subInfo.getUid();
				stmt.executeUpdate(query);
				stmt.close();
				query = "Select CompanyName from Company where UId = " + subInfo.getCompanyId();
				pstmt = con.prepareStatement(query);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					if(rs.getString(1).length() > 0) {
					    subInfo.setCompanyName(rs.getString(1));
					    subInfo.setCompanyNameLc(rs.getString(1));
					}
					else {
						System.err.print("Empty companyName field");
						subInfo = null;
					}
				}
				else {
					subInfo.setCompanyName("No Company Record");
					subInfo.setCompanyNameLc("No Company Record");
				}
				rs.close();
			}
		}
		catch (SQLException e) {
			System.err.print("Exception on reading Submissions table: " + e.getMessage());
		}
		return (subInfo);
	}

	public void insertNewNotesRecord(Connection con, SubmissionInfo subInfo, TemplateNotes NodeItem, int NoteGaapUid) {
		String     query;
		
		PreparedStatement pstmt;
		String            insertStr = "INSERT into Notes (SubUid, ItemVersion, " +
		                              "TemplateParseUid, NoteToGaapId, UserText) values (?,?,?,?,?)";
		try {
			pstmt = con.prepareStatement(insertStr);
			pstmt.setInt(1,subInfo.getUid());
			pstmt.setInt(2,subInfo.getVersion());
			pstmt.setInt(3,NodeItem.getTemplateParseUid());
			pstmt.setInt(4,NoteGaapUid);
			pstmt.setString(5,NodeItem.getIdentifiedText());
			pstmt.execute();
		}
		catch (SQLException e) {
			System.err.print("Exception on writing NOTES table: " + e.getMessage());
			GenericErrorCls(con, "WriteTemplateInfo", "xception on writing NOTES table: " + e.getMessage(), true);
		}
	}
	
	private String RemoveTags(String OrigStr) {
		String RtnStr = OrigStr;

		RtnStr = RtnStr.replace("<table>", "");
		RtnStr = RtnStr.replace("<tr>", "");
		RtnStr = RtnStr.replace("</tr>", "");
		RtnStr = RtnStr.replace("<td>", "");
		RtnStr = RtnStr.replace("</td>", "");
        RtnStr = RtnStr.replace("'", "''");  // escape the single quote
		return (RtnStr);
	}
	
	private int GetLastEdgar(Connection con, SubmissionInfo si) {
		int               iRtn = si.getVersion();
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
        boolean           bContinue = true;
        int               iCount = 0;
		
		try {
        	while(bContinue) {
        		query = "Select COUNT(*) from HtmlFileXref where SubUid = " + si.getUid() + "AND ItemVersion = " + iRtn;
        		pstmt = con.prepareStatement(query);
        		rs = pstmt.executeQuery();
        		if(rs.next()) {
        		    iCount = rs.getInt(1);
         		}
        		rs.close();
        		if(iCount == 0) {
        			iRtn--;
        			if(iRtn <= 0)
        				return(iRtn);
        		}
        		else
        			bContinue = false;
        	} //when we exit loop we have index
			
		}
        catch (SQLException e) {
			System.err.print("Exception on reading HtmlFileXref table: " + e.getMessage());
			GenericErrorCls(con, "GetLastEdgar", "xception on reading HtmlFileXref table: " + e.getMessage(), false);     	
        }
		return(iRtn);
	}
	
	private HtmlFileXref GetCrossReference(Connection con, SubmissionInfo si, int iVersion, int beginLine, int endLine) {
		String                    query;
		PreparedStatement         pstmt;
		ResultSet                 rs;
        HtmlFileXref              rtnCls = new HtmlFileXref();
        
        
        try {
        	// first we have to find the last version that went through EdgarExtractor as he is the one
        	//  that populates the HtmlFileXref table
            query = "SELECT FileLine, HtmlTblLine  From HtmlFileXref where SubUid = " + si.getUid() + " AND ItemVersion = " +
                     iVersion + " AND HtmlLine = " + beginLine;
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            if(rs.next()) {
        	    rtnCls.setBeginLine(rs.getInt(1));
        	    rtnCls.setTableBeginLine(rs.getInt(2));
                query = "SELECT FileLine  From HtmlFileXref where SubUid = " + si.getUid() + " AND ItemVersion = " +
                        iVersion + " AND HtmlLine = " + endLine;
                pstmt = con.prepareStatement(query);
                rs = pstmt.executeQuery();
                if(rs.next()) {
    	            rtnCls.setEndLine(rs.getInt(1));
                }
    	        else {
     	            query = "SELECT FileLine  From HtmlFileXref where SubUid = " + si.getUid() + " AND ItemVersion = " +
     	                    iVersion + " AND HtmlLine = -1";
    	            pstmt = con.prepareStatement(query);
    	            rs = pstmt.executeQuery();
    	            if(rs.next()) {
    	    	        rtnCls.setEndLine(rs.getInt(1));  
    	            }
                }
           }
            rs.close();
        }
        catch (SQLException e) {
			System.err.print("Exception on reading HtmlFileXref table: " + e.getMessage());
			GenericErrorCls(con, "WriteTemplateInfo", "xception on reading HtmlFileXref table: " + e.getMessage(), false);     	
        }
        if(bDebug) {
		    GenericErrorCls(con, "GetCrossReference", "BL = " + beginLine + ":: EL = " + endLine, false);
		    GenericErrorCls(con, "GetCrossReferenceRTN", "BL = " + rtnCls.getBeginLine() + ":: EL = " + rtnCls.getEndLine(), false);
        }
      return (rtnCls);
	}
	
	public int GetNoteIndex(Connection con, int iNoteUid) {
		int               iRtn = 0;
		String            query = "SELECT NoteIndex from TemplateParse where Uid = " + iNoteUid;
		PreparedStatement pstmt;
		ResultSet         rs;
		
	    try {
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        if(rs.next()) {
	    	    iRtn = rs.getInt(1);
	        }
	        rs.close();
	    }
	    catch (SQLException e) {
		    System.err.print("Exception on reading noteIndex table: " + e.getMessage());
			GenericErrorCls(con, "GetNoteIndex", "xception on reading HtmlFileXref table: " + e.getMessage(), false);     	
	    }
	    return(iRtn);
	}
	
	public void AddNoteTable(Connection con, SubmissionInfo si, int iNoteUid, int TableCounter, int iNoteIndex, 
			                 int iTblBeginLine, int iTblEndLine, boolean bDefinitionTbl) {
		HtmlFileXref              hfx;		int                       iVersion = 0;
		String                    query;
		PreparedStatement         pstmt;
		String                    identityStr = "SELECT @@IDENTITY";
		PreparedStatement         Uidstmt;
		ResultSet                 rs;
		int                       TemplateParseUid = 0;
		
        hfx = GetCrossReference(con, si, si.getVersion(), iTblBeginLine, iTblEndLine);
	    try {
	    	//query = "INSERT INTO NoteTables (NoteUid, JIffyId, BeginLineNum, EndLineNum, HtmlBeginLineNum, HtmlEndLineNum, " +
	    	//        "ColumnZeroText, DefinitionTbl) Values(?,?,?,?,?,?,?,?)";
	    	query = "INSERT INTO TemplateParse (SubUid, version, TemplateId, NoteIndex, BeginLineNum, EndLineNum, HtmlBeginLineNum, HtmlEndLineNum) " +
	    	        "Values(?,?,?,?,?,?,?,?)";
	    	pstmt = con.prepareStatement(query);
	    	pstmt.setInt(1, si.getUid());
	    	pstmt.setInt(2, si.getVersion());
	    	pstmt.setInt(3, -5);
	    	//pstmt.setInt(4,iNoteUid);
	    	pstmt.setInt(4,iNoteIndex);
	    	pstmt.setInt(5, iTblBeginLine);
	    	pstmt.setInt(6, iTblEndLine);
	    	pstmt.setInt(7, hfx.getTableBeginLine());  //hfx.getBeginLine());
	    	pstmt.setInt(8, hfx.getEndLine());
	    	//pstmt.setString(9, "");
	    	//pstmt.setBoolean(8, bDefinitionTbl);
	    	pstmt.execute();
        	Uidstmt = con.prepareStatement(identityStr);
        	con.setAutoCommit(false);
        	Uidstmt.execute();
            rs = Uidstmt.executeQuery();
            rs.next();
            TemplateParseUid = rs.getInt(1);
            rs.close();
            con.commit();
            con.setAutoCommit(true);
            query = "INSERT into NoteTableDetails (NoteTableUid, ColumnZeroText, Sentence, DefinitionTbl) " +
                    "Values (?,?,?,?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, TemplateParseUid);
            pstmt.setString(2, "");
            pstmt.setString(3, "");
            pstmt.setBoolean(4, bDefinitionTbl);
            pstmt.execute();
	    }
		catch (SQLException e) {
			GenericErrorCls(con, "AddNoteTable", "xception on writing AddNoteTable table: " + e.getMessage(), false);
		}
	}
	
	public int GetNoteId(Connection con) {
		int                iRtn = 0;
		PreparedStatement  pstmt;
		ResultSet          rs;
		String             query;
		
		try {
			query = "Select Id from StrTable where StrText = 'Note'";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			rs.next();
			iRtn = rs.getInt(1);
			rs.close();
		}
		catch (SQLException e) {
				//System.err.print("Exception on writing TemplateParse table: " + e.getMessage());
				GenericErrorCls(con, "Reading StrText", "unable to find Note Record " + e.getMessage(), false);
				iRtn = -1;
		}
		return(iRtn);
	}
	
	public ArrayList<TemplateNotes> WriteTemplateInfo(Connection con, SubmissionInfo si, 
			                                          ArrayList<HtmlTraceItem>  TemplateTrace, boolean bUserDefed){
		String                    query = "";
		String                    TagRemoved = "";
		String                    identityStr = "SELECT @@IDENTITY";
        ResultSet                 rs;
		PreparedStatement         pstmt = null;
		PreparedStatement         Uidstmt = null;
		int                       TemplateParseUid;
		ArrayList<TemplateNotes>  rtnArray = new ArrayList<TemplateNotes>();
		TemplateNotes             thisNote = null;
		HtmlFileXref              hfx = new HtmlFileXref();
		int                       iTemplateId;
		int                       beginLineNum = 0;
		int                       iVersion = 0;
		int                       iScale = 0;
		int                       iNoteCounter = 1;
		int                       iNoteCounterToWrite = 0;
		
		try {
			iVersion = GetLastEdgar(con, si);
		   //Statement stmt = con.createStatement();
			//System.out.println("WriteTemplateInfo count: " + TemplateTrace.size());
           for(int k = 0; k < TemplateTrace.size(); k++) {
			//int k = 0;
               iTemplateId = TemplateTrace.get(k).getTemplateId();
               TagRemoved = RemoveTags(TemplateTrace.get(k).getIdentifiedText()); 
               if(TagRemoved.length() > 64)
            	   TagRemoved = TagRemoved.substring(0,63);
               
               if(iTemplateId < 0)
                   hfx = GetCrossReference(con, si, iVersion, TemplateTrace.get(k).getBeginLine(), TemplateTrace.get(k).getEndLine());
               else {
                   if(bUserDefed == false)
                       hfx = GetCrossReference(con, si, iVersion, TemplateTrace.get(k).getBeginLine(), TemplateTrace.get(k).getEndLine());
                   else {
            	       hfx.setBeginLine(TemplateTrace.get(k).getUserStart());
            	       hfx.setEndLine(TemplateTrace.get(k).getUserEnd());
                   }
               }
                if((iTemplateId < 0 )) {
            	   iScale = 1;
                   beginLineNum = hfx.getBeginLine();
               }
               else {
            	   if(iTemplateId == 4) {
            		   if(TemplateTrace.get(k).getNoteIndex() == 0) {
            			   iNoteCounterToWrite = iNoteCounter;
            			   iNoteCounter++;
            		   }
            		   else
            			   iNoteCounterToWrite = TemplateTrace.get(k).getNoteIndex();
                	   iScale = 1;
           		       if(TemplateTrace.get(k).getNoteWithinTable() == true)
           		    	   beginLineNum = hfx.getTableBeginLine();
           		       else
           		    	   beginLineNum = hfx.getBeginLine();
            	   }
            	   else {
            		   iNoteCounterToWrite = 0;
            	       iScale = TemplateTrace.get(k).getScale();
            	       if(bUserDefed == true)
            	    	   beginLineNum = hfx.getBeginLine();
            	       else
            	           beginLineNum = hfx.getTableBeginLine();
            	   }
               }
               if(bDebug)
                   GenericErrorCls(con, "WriteTemplateInfo", "Tid = " +  TemplateTrace.get(k).getTemplateId() + " BL = " + hfx.getBeginLine() + 
            		           ":: EL = " + hfx.getEndLine(), false);
            	         
                query =  "Insert into TemplateParse (SubUid, Version, TemplateId, BeginLineNum, EndLineNum, Confidence," +
            	         " HtmlBeginLineNum, HtmlEndLineNum, IdentifiedText, UnAudited, Scale, IdLine, NoteIndex) " +
            	         "values ('" + si.getUid() + "', '" + si.getVersion() + "', '" + 
            	         TemplateTrace.get(k).getTemplateId() + "', '" + TemplateTrace.get(k).getBeginLine() + "', '" + 
            	         TemplateTrace.get(k).getEndLine() + "', '" + TemplateTrace.get(k).getConfidenceLevel() + "'," +
            	         beginLineNum + ", " + hfx.getEndLine() + ", '" + TagRemoved + "'," + 
            	         TemplateTrace.get(k).getUnAudited() + ", " + iScale + ", " + TemplateTrace.get(k).getIdLine() + 
            	         ", " + iNoteCounterToWrite + ")";
                if(bDebug)
                	System.out.println("QUERY: " + query);
                pstmt = con.prepareStatement(query);
                if(TemplateTrace.get(k).getTemplateId() == 4) {
                	Uidstmt = con.prepareStatement(identityStr);
                	con.setAutoCommit(false);
                	pstmt.execute();
    	            rs = Uidstmt.executeQuery();
    	            rs.next();
    	            TemplateParseUid = rs.getInt(1);
    	            rs.close();
    	            con.commit();
    	            con.setAutoCommit(true);
    	            thisNote = new TemplateNotes();
    	            thisNote.setIdentifiedText(TagRemoved);
    	            thisNote.setTemplateParseUid(TemplateParseUid);
    	            rtnArray.add(thisNote);
                }
                else {
                	pstmt.execute();
                	if(bDebug) {
               		    System.out.println("QUERY: " + query);
                	}
                }
               //stmt.executeUpdate(query);
           }
	       //stmt.close();
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing TemplateParse table: " + e.getMessage());
			GenericErrorCls(con, "WriteTemplateInfo", "xception on writing TemplateParse table: " + e.getMessage(), false);
		}
		return (rtnArray);
	}
	
	public int WriteAppError(Connection con, ErrorCls errorCls) {  //static
		int               iRtn = JiffyServer(con);
		String            ErrorText = "";
		Date              date = new Date();
		Timestamp         timestamp = new Timestamp(date.getTime());
		PreparedStatement pstmt;
		String            insertStr = "INSERT into APPERROR (CompanyUid, SubUid, ItemVersion, " +
		                              "Application, SrcFunction, ErrorLevel, ErrorText) values (?,?,?,?,?,?,?)";
        if(iRtn != 0) {
		    try {
				ErrorText = timestamp.toString() + " : " + errorCls.getErrorText();
				if(ErrorText.length() > 127)
					ErrorText = ErrorText.substring(1, 127);
			    pstmt = con.prepareStatement(insertStr);
	    		pstmt.setInt(1,errorCls.getCompanyUid());
		    	pstmt.setInt(2,errorCls.getSubUid());
			    pstmt.setInt(3,errorCls.getItemVersion());
		    	pstmt.setString(4,"TemplateIdentification");
			    pstmt.setString(5,errorCls.getFunctionStr());
			    pstmt.setInt(6,iRtn);
			    pstmt.setString(7,ErrorText);
			    pstmt.execute();
			    //if(errorCls.isBExit() == true)
			    //    System.exit(iRtn);
		    }
		    catch (SQLException e) {
			    //System.exit(-1);
		    }
        }
		return(iRtn);
	}
	
	public void GetItemsIdentified(Connection con, SubmissionInfo si, TemplateLogger me) {
		String            query = "";
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "Select count(*) from TemplateParse where SubUid = " + si.getUid() + " and Version = " +
		            si.getVersion() + " And TemplateId > 0";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				me.iNumTemplates = rs.getInt(1);
				rs.close();
				query = "Select count(*) from TemplateParse where SubUid = " + si.getUid() + " and Version = " +
		            si.getVersion() + " And TemplateId = 4";
				pstmt = con.prepareStatement(query);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					me.iNumNotes = rs.getInt(1);
					me.iNumTemplates -= me.iNumNotes;
				}
				query = "Select count(*) from TemplateParse where SubUid = " + si.getUid() + " and Version = " +
				        si.getVersion() + " and TemplateId = -5";
				pstmt = con.prepareStatement(query);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					me.iNumTablesInNotes = rs.getInt(1);
				}
			}
			rs.close();
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing TemplateParse table: " + e.getMessage());
			GenericErrorCls(con, "Reading TemplateParse ", "unable to select Records " + e.getMessage(), false);
	    }
    }

	private int JiffyServer(Connection con) {  //static
		int               iRtn = 0;
		String            query;
		CallableStatement cstmt;
		
		try {
			query = "{call dbo.GetJiffyNum(?)}";
			cstmt = con.prepareCall(query);
			cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
			cstmt.execute();
			iRtn = cstmt.getInt(1);
		}
		catch (SQLException e) {
			System.out.println("Error processing JiffyServer: " + e.getMessage());
			//System.exit(-1);
			return(0);
		}
		
		
		return (iRtn);
	}

}
