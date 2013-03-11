package com.bricksimple.rdg.FieldId;


import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
//import java.util.ArrayList;

import com.bricksimple.rdg.FieldId.Template7Col;
import com.bricksimple.rdg.FieldId.ConfidenceLevel;
import com.bricksimple.rdg.FieldId.MatchStr;

//import com.bricksimple.rdg.FieldId.*;

public class MySqlAccess {
	
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
        	 }
	 	      DriverManager.setLoginTimeout( 5 );
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

	private void GenericErrorCls(Connection con, String OrigFunction, String errorText,  //static
        int CompanyUid, int subUid, int itemVersion, boolean bExit) {
        ErrorCls errorCls = new ErrorCls();

        errorCls.setCompanyUid(CompanyUid);
        errorCls.setFunctionStr(OrigFunction);
        errorCls.setSubUid(subUid);
        errorCls.setItemVersion(itemVersion);
        errorCls.setBExit(bExit);
        errorCls.setErrorText(errorText);
       WriteAppError(con, errorCls);

    }
	
	public ArrayList<Integer> GetListOfNoteTableUids(Connection con, SubmissionInfo si, int iNoteTableUid) {  //static
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		String             query;
		PreparedStatement  pstmt;
		ResultSet          rs;
		int                iNoteIndex;
		try {
			query = "Select NoteIndex from TemplateParse where Uid = " + iNoteTableUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				iNoteIndex = rs.getInt(1);
				rs.close();
			    //query = "Select Uid from NoteTables where NoteUid = " + iNoteTableUid + " ORDER BY Uid";
				query = "SELECT Uid from TemplateParse where NoteIndex = " + iNoteIndex + " AND SubUid = " +
			             si.getUid() + " AND Version = " + si.getVersion() + " AND TemplateId = -5 ORDER BY Uid";
			    pstmt = con.prepareStatement(query);
			    rs = pstmt.executeQuery();
			    while(rs.next()) {
				    rtnArray.add(rs.getInt(1));
			    }
			}
			rs.close();
		}
	    catch (Exception e) {
	    	GenericErrorCls(con, "GetListOfNoteTableUids", "Error resding TemplateParse: " + e.getMessage(),
	    	        0, 0, iNoteTableUid, false);
	   }		
		return(rtnArray);
	}

	public NoteTableBounds GetNoteTableBounds(Connection con, int iNoteTableUid) {  //static
		NoteTableBounds   ntb = new NoteTableBounds();
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			//query = "SELECT Uid, BeginLineNum, EndLineNum, HtmlBeginLineNum, HtmlEndLIneNum, DefinitionTbl from NoteTables where Uid = " + iNoteTableUid;
			query = "SELECT tp.Uid, tp.BeginLineNum, tp.EndLineNum, tp.HtmlBeginLineNum, tp.HtmlEndLIneNum, ntd.DefinitionTbl " +
			        "from TemplateParse tp, NoteTableDetails ntd  where tp.Uid = " + iNoteTableUid + " AND tp.Uid = ntd.NoteTableUid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				ntb.NoteTableUid = rs.getInt(1);
				ntb.BeginLineNum = rs.getInt(2);
				ntb.EndLineNum = rs.getInt(3);
				ntb.HtmlBeginLineNum = rs.getInt(4);
				ntb.HtmlEndLineNum = rs.getInt(5);
				ntb.bIsDefinitionTbl = rs.getBoolean(6);
			}
			rs.close();
		}
	    catch (Exception e) {
	    	GenericErrorCls(con, "GetNoteTableBounds", "Error reading TemplateParse or NoteTableDetails: " + e.getMessage(),
	    	        0, 0, iNoteTableUid, false);
	   }		
		return(ntb);
	}

	/*********************
	public ArrayList<NoteTableLineNum> GetNoteTableLineNums(Connection con, SubmissionInfo si,  //static
			                                                       int iNoteUid) {
	    ArrayList<NoteTableLineNum> rtnArray = new ArrayList<NoteTableLineNum>();
	    PreparedStatement           pstmt;
	    String                      query;
	    ResultSet                   rs;
	    NoteTableLineNum            CurNote;
	    try {
	    	query = "Select BeginLineNum, EndLineNum from NoteTables where NoteUid = " +
	    	        iNoteUid + " Order by JiffyId";
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	while(rs.next()) {
	    		CurNote = new NoteTableLineNum();
	    		CurNote.beginLineNum = rs.getInt(1);
	    		CurNote.endLineNum = rs.getInt(2);
	    		rtnArray.add(CurNote);
	    	}
	    	rs.close();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "GetNoteTableLineNums", "Error getting noteTables: " + e.getMessage(),
	    	        si.getCompanyId(), si.getUid(), si.getVersion(), false);
	   }
	    return (rtnArray);
	}
	***************/
	public void UpdateTemplateParse(Connection con, int myUID, int iBeginLine, int iEndLine) {  //static
		PreparedStatement pstmt = null;
		
	    try {
	    	pstmt = con.prepareStatement("Update templateparse set NormlBeginLineNum = " + iBeginLine +
	    			                     ", NormlEndLineNum = " + iEndLine + " where Uid = " + myUID);
	    	pstmt.execute();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "UpdateTemplateParse", "Error getting Templates: " + e.getMessage(),
	    	        0, 0, 0, false);
	   }
	}
	
	public void SetTemplateUnaudited(Connection con, SubmissionInfo si, int iTemplateId) {  //static
		
		PreparedStatement pstmt = null;
		
	    try {
	    	pstmt = con.prepareStatement("Update templateparse set UnAudited = 1 where SubUid = " + si.getUid() +
	    			                     " AND Version = " + si.getVersion() + " AND TemplateId = " + iTemplateId);
	    	pstmt.execute();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "SetTemplateUnaudited", "Error setting Templates UnAudited: " + e.getMessage(),
	    	        si.getCompanyId(), si.getUid(), si.getVersion(), false);
	   }
	}
	
	public void SetTemplateScale(Connection con, SubmissionInfo si, int iTemplateId, int iScale) {  //static
		
		PreparedStatement pstmt = null;
		
	    try {
	    	pstmt = con.prepareStatement("Update templateparse set Scale = " + iScale + " where SubUid = " + si.getUid() +
	    			                     " AND Version = " + si.getVersion() + " AND TemplateId = " + iTemplateId);
	    	pstmt.execute();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "SetTemplateUnaudited", "Error setting Templates UnAudited: " + e.getMessage(),
	    	        si.getCompanyId(), si.getUid(), si.getVersion(), false);
	    }
	
	}
	
	public int GetTableDateFormat(Connection con, int CompanyUid, int iTemplateType) {  //static
		int iRtn = 0;
	    ResultSet rs = null;
	    PreparedStatement pstmt = null;
	
	    try {
	    	pstmt = con.prepareStatement("Select FmtType from specialtbldatefmt where " +
	    			                     "CompanyUid = " + CompanyUid + " AND TemplateUid = " + iTemplateType);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	    		iRtn = rs.getInt(1);
	    	}
	    	else {
		    	pstmt = con.prepareStatement("Select FmtType from specialtbldatefmt where " +
	                     "CompanyUid = 0" + " AND TemplateUid = " + iTemplateType);
                rs = pstmt.executeQuery();
                if(rs.next()) {
                    iRtn = rs.getInt(1);
                }
	    	}
	    	rs.close();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "GetTableDateFormat", "ERROR initilalizing Templateparse: " + e.getMessage(),
	    	        CompanyUid, 0, 0, false);
	    }
		return(iRtn);
	}
	
	public int CopyPreambleMarkers(Connection con, int myUid, int iVersion) {  //static
	    ResultSet         rs = null;
	    PreparedStatement pstmt = null;
	    PreparedStatement uPstmt = null;
	    int               endLineNum = 0;
	    try {
	    	pstmt = con.prepareStatement("Select Uid, BeginLineNum, EndLineNum from templateparse where " +
	    			                     "SubUid = " + myUid + " AND (TemplateId = -1 OR TemplateId = -2) AND Version = " + iVersion +
	    			                     " order by BeginLineNum");
	    	rs = pstmt.executeQuery();
	    	while(rs.next()) {
	    		endLineNum = rs.getInt(3);
	    		uPstmt = con.prepareStatement("Update templateparse set NormlBeginLineNum = " +
	    				                       rs.getInt(2) + ", NormlEndLineNum = " + rs.getInt(3) +
	    				                       " where uid = " + rs.getInt(1));
	    		uPstmt.execute();
	    	}
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "CopyPreambleMarkers", "ERROR initilalizing Templateparse: " + e.getMessage(),
	    	        0, myUid, 0, false);
	    }
	    return(endLineNum);
	}
	
	public ResultSet GetTemplate(Connection con, int myUID, int iVersion, int TemplateType) {  //static
		ResultSet rtnSet = null;
	    PreparedStatement pstmt = null;

	    try {
	    	pstmt = con.prepareStatement("Select * from templateparse where SubUid = " + myUID +
	    			                      " AND TemplateId = " +  TemplateType +
	    			                      " AND Version = " + iVersion);
	    	rtnSet = pstmt.executeQuery();

	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "GetTemplate", "ERROR getting Templates: " + e.getMessage(),
	    	        0, myUID, iVersion, false);
	    }
		return(rtnSet);		
	}
	
	public void MarkAsDummy(Connection con, int iTemplateParseUid) {  //static
	    String            query;
	    PreparedStatement pstmt;
	    
	    try {
	    	query = "Update templateparse set TemplateId = -4 where Uid = " + iTemplateParseUid;
 	    	pstmt = con.prepareStatement(query);
	    	pstmt.execute();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "MarkAsDummy", "Error updating Templates: " + e.getMessage(),
	    	        0, 0, 0, false);
	   }
	}
	
	public ResultSet GetTemplates(Connection con, int myUID, int iVersion, int lastPreambleLine) {  //static
		ResultSet rtnSet = null;
	    PreparedStatement pstmt = null;

	    try {
	    	pstmt = con.prepareStatement("Select * from templateparse where SubUid = " + myUID +
	    			                      " AND Version = " + iVersion + " AND BeginLineNum > " + lastPreambleLine +
	    			                      " AND TemplateId > 0 ORDER by BeginLineNum ASC");
	    	rtnSet = pstmt.executeQuery();

	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "GetTemplates", "ERROR getting Templates: " + e.getMessage(),
	    	        0, myUID, iVersion, false);
	    }
		return(rtnSet);		
	}
	
	public MatchStr[] GetListFromTbls(Connection con, int TblNdx, int TblKey) {  //static
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
	    	GenericErrorCls(con, "GetListFromTbls", "Exception reading html_tags: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
		return newList;
	}
	
	public String GetFormString(Connection con, int TblNdx) {  //static
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
	    	GenericErrorCls(con, "GetFormString", "Exception reading formidentification: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
		return FormIdStr;
	
	}
	
	public boolean FilerStatusFound(Connection con, int iCompanyId) {  //static
		boolean           bRtn = false;
	    PreparedStatement pstmt = null;
	    ResultSet         rs = null;
		String            QueryStr = "";
		String            filerStatus;
		
		try {
	    	pstmt = con.prepareStatement("Select FilerStatus from Company where Uid = " + iCompanyId);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            filerStatus = rs.getString(1);
	            if(filerStatus != null) {
	            	if(filerStatus.length() > 0)
	            		bRtn  = true;
	            }
	    	}
	    	rs.close();
		}
		catch (SQLException e) {
		    	GenericErrorCls(con, "FilerStatusFound", "Exception reading field identifiers: " + e.getMessage(),
		    	        iCompanyId, 0, 0, false);
		    	return(false);
		    }
		return(bRtn);
	}
	
	public void InsertFilerStatus(Connection con, int iCompanyId, String filerStatus) {  //static
		String            query = "";
		PreparedStatement pstmt = null;
		
		try {
			query = "UPDATE COMPANY set FilerStatus = '" + filerStatus + 
			        "' WHERE UID = " + iCompanyId;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
	    catch (SQLException e) {
	    	GenericErrorCls(con, "InsertFilerStatus", "Exception writing filer status: " + e.getMessage(),
	    	        iCompanyId, 0, 0, false);
	    }
	}
	
	public FieldMatchStr[] GetFilerStatus(Connection con, int iSrcType) {  //static
		FieldMatchStr[] newList = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
		int iCount;
		String QueryStr = "";
		
		try {
	    	pstmt = con.prepareStatement("Select count(*) from taggedfield where TemplateId = -2" +
	    			                     " AND SourceType = " + iSrcType +
	    			                     " AND Destination = 5");
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new FieldMatchStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new FieldMatchStr();
	            }
	            rs.close();
	            pstmt.close();	            
		        QueryStr = "Select UID, Tag, Threshold, PathToData, FieldModifier, Abstract, Destination from taggedfield where TemplateId = -2" +
                            " AND SourceType = " + iSrcType +
                            " AND Destination = 5";
		        pstmt = con.prepareStatement(QueryStr);
		        rs = pstmt.executeQuery();
		        iCount = 0;
		        while(rs.next()) {
		        	newList[iCount].setWordMatch(false);
		    	    newList[iCount].setUid(rs.getInt(1));
		    	    newList[iCount].setFieldStr(rs.getString(2));
		    	    newList[iCount].setThreshold(rs.getDouble(3));
		    	    newList[iCount].setPathToData(rs.getString(4));
		    	    newList[iCount].setFieldModifier(rs.getString(5));
	        	    newList[iCount].setAl(rs.getString(2));
	        	    newList[iCount].setAbstract(rs.getBoolean(6));
	        	    newList[iCount].setDestination(rs.getInt(7));
		        	iCount++;
		        }
		        rs.close();
		        pstmt.close();
	    	}
		}
	    catch (SQLException e) {
	    	GenericErrorCls(con, "GetFieldIdentifiers", "Exception reading field identifiers: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
		return newList;
		
	}
	
	public FieldMatchStr[] GetFieldIdentifiers(Connection con, int iSrcType, int iCompanyId,  //static
			                                             int iTemplateId) {
		FieldMatchStr[] newList = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
		int iCount;
		String QueryStr = "";
		
		try {
	    	pstmt = con.prepareStatement("Select count(*) from taggedfield where TemplateId = " +
	    			                     iTemplateId + " AND SourceType = " + iSrcType +
	    			                     " AND Tag != '' AND (Companyid = 0 or Companyid = " + iCompanyId + ")");
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new FieldMatchStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new FieldMatchStr();
	            }
	            rs.close();
	            pstmt.close();	            
		        QueryStr = "Select UID, Tag, Threshold, PathToData, FieldModifier, Abstract, Destination from taggedfield where TemplateId = " +
                            iTemplateId + " AND SourceType = " + iSrcType +
                            " AND Tag != '' AND (Companyid = 0 or Companyid = " + iCompanyId + ") ORDER BY Uid ASC, CompanyId DESC";
		        pstmt = con.prepareStatement(QueryStr);
		        rs = pstmt.executeQuery();
		        iCount = 0;
		        while(rs.next()) {
		        	String pathToData = rs.getString(4);
		        	if(pathToData.equals("RF")) {
		        		pathToData = "RT";
		        		newList[iCount].setWordMatch(true);
		        	}
		        	else
		        		newList[iCount].setWordMatch(false);
		    	    newList[iCount].setUid(rs.getInt(1));
		    	    newList[iCount].setFieldStr(rs.getString(2));
		    	    newList[iCount].setThreshold(rs.getDouble(3));
		    	    newList[iCount].setPathToData(pathToData);
		    	    newList[iCount].setFieldModifier(rs.getString(5));
	        	    newList[iCount].setAl(rs.getString(2));
	        	    newList[iCount].setAbstract(rs.getBoolean(6));
	        	    newList[iCount].setDestination(rs.getInt(7));
		        	iCount++;
		        }
		        rs.close();
		        pstmt.close();
	    	}
		}
	    catch (SQLException e) {
	    	GenericErrorCls(con, "GetFieldIdentifiers", "Exception reading field identifiers: " + e.getMessage(),
	    	        iCompanyId, 0, 0, false);
	    }
		return newList;
	}
		
	public MatchStr[] GetListFromTbls(Connection con, int TblNdx) {  //static
		MatchStr[] newList = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    ConfidenceLevel confidenceLevel = new ConfidenceLevel();
	    
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
	        	    newList[iCount].al = confidenceLevel.RecurringSting(rs.getString(2));
	        	    iCount++;
	            }
	            rs.close();
	    	}
	    }
	    catch (SQLException e) {
	    	GenericErrorCls(con, "GetListMatchtable", "Exception reading match table: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
	    return(newList);
	}
		
	public void CloseConnection(Connection con)  //static
	{
		try {
		   con.close();
		}
		catch (SQLException e) {
			System.err.print("Exception closing MySql Connection: " + e.getMessage());
		}
	}
	
		

	public SubmissionInfo ReadFieldSubmissionInfo(Connection con, String myInputFileName) {  //static
		SubmissionInfo subInfo = new SubmissionInfo();
		
		String query = "Select * from Submissions where ExtractFile = '" + myInputFileName + "'";
		//query = query.replace("\\", "\\\\");
		
		try {
		   Statement stmt = con.createStatement();
		   ResultSet rs = stmt.executeQuery(query);
		   if(rs.next() == true)  {
				subInfo.setUid(rs.getInt(1));
				subInfo.setCompanyId(rs.getInt(2));
				subInfo.setHtmlFile(rs.getString(5));
				subInfo.setExtractFile(rs.getString(6));
				subInfo.setVersion(rs.getInt(7));
			}
		   rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "readSubmissionInfo", "Exception reading submission table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		return (subInfo);
	}
	
	public void WriteCik(Connection con, SubmissionInfo SubInfo, int iPrevVersion) {  //static
		
		String            query;
		PreparedStatement pstmt = null;
		ResultSet         rs;
		ResultSet         prevRs;
		int               iCount;
		
		try {
			query = "Select COUNT(*) from Cik_Ids where CompanyUid = " + SubInfo.getCompanyId();
			pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            if(iCount == 0) {
	            	query = "INSERT into CIK_IDS (CompanyUid) values (?)";
		            pstmt = con.prepareStatement(query);
		            pstmt.setInt(1, SubInfo.getCompanyId());
		            pstmt.executeUpdate();
	            }
	            query = "INSERT into COMPANYENTITYTBL (SubUid, ItemVersion, CompanyID) Values (?, ?, ?)";
	            pstmt = con.prepareStatement(query);
	            pstmt.setInt(1, SubInfo.getUid());
	            pstmt.setInt(2, SubInfo.getVersion());
	            pstmt.setInt(3, SubInfo.getCompanyId());
	            pstmt.executeUpdate();
	            if(iPrevVersion > 0) {
	            	query = "Select * from COMPANYENTITYTBL where SubUid = " + SubInfo.getUid() +
	            	        " AND ItemVersion = " + iPrevVersion;
	            	pstmt = con.prepareStatement(query);
	            	prevRs = pstmt.executeQuery();
	            	if(prevRs.next()) {
	            		for(int j = 5; j < 8; j++) {
	            			if(prevRs.getString(j) != null) {
	            				query = "Update COMPANYENTITYTBL set ";
	            				switch (j) {
	            				    //case 5:   // moved to company table
	            					//    query = query + "FilerCatagory = ";
	            					//    break;
	            				    case 5:
	            				    	query = query + "CurrentReportingStatus = ";
	            				    	break;
	            				    case 6:
	            				    	query = query + "VoluntaryFiler = ";
	            				    	break;
	            				    case 7:
	            				    	query = query + "SeasonedIssuer = ";
	            				    	break;
	            				}
	            				if(prevRs.getString(j) == null)
	            					query = query + "''";
	            				else
	            				    query = query + "'" + prevRs.getString(j) + "'";
	            			}
	            		}
	            		prevRs.close();
        				query = query + " where SubUId = " + SubInfo.getUid() + " AND ItemVersion = " + SubInfo.getVersion();
        				pstmt = con.prepareStatement(query);
        				pstmt.executeUpdate();
	            	}         	
	            }
	    	}
	        rs.close();   
		}
		catch (SQLException e) {
				//System.err.print("Exception on inserting cik table: " + e.getMessage());
		    	GenericErrorCls(con, "WriteCik", "Exception inserting into CIK: " + e.getMessage(),
		    	        SubInfo.getCompanyId(), SubInfo.getUid(), SubInfo.getVersion(), false);
		}
	}
	
	/** note this routine checks if record already exists. If yes, then returns that record number **/
	
	public String ReadSupTextInDateRef(Connection con, int iDateRefUid) {  //static
		String rtnStr = "";
		String query = "Select SupText from DateRef where Uid = " + iDateRefUid;
		PreparedStatement pstmt = null;
		ResultSet         rs = null;
		
		try {
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) 
				rtnStr = rs.getString(1);
			rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "ReadSupTextInDateRef", "Exception reading Dateref: " + e.getMessage(),
	    	        iDateRefUid, 0, 0, false);
			return("");
		}
		return(rtnStr);
	}
	
	
	public void ClearSupText(Connection con, int iDateRefUid) {  //static
		String query = "Update DateRef set supText = '' where Uid = " + iDateRefUid;
		PreparedStatement pstmt = null;
		
		try {
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "ClearSupText", "Exception writing Dateref: " + e.getMessage(),
	    	        iDateRefUid, 0, 0, false);	
		}
		
	}
	
	public int InsertTemplate7Column(Connection con, SubmissionInfo si, String NodeData) {  //static
		int               iRtn = 0;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
            query =  "Insert into StockholderEquity (SubUid, ItemVersion, StockType) values(?,?,?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, si.getUid());
            pstmt.setInt(2, si.getVersion());
            pstmt.setString(3, NodeData);
            pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing dateref table: " + e.getMessage());
	    	GenericErrorCls(con, "InsertTemplate7Column", "Exception writing record: " + e.getMessage(),
	    	        si.getCompanyId(), si.getUid(), si.getVersion(), false);
		}
	return(iRtn);
	}
	
	public int WriteDateRef(Connection con, int iSubUid, int iItemVersion, int iTemplateUid,  //static
			                       String DateData, String SupStr, int StockHolderUid, int tdColumn) {
		
        String    query;
		int       iRtn = 0;
		ResultSet rs;
		String    StartDate = "";
		String    EndDate = DateData;
		CONSTANTS constants = new CONSTANTS();
		int       iInception = -1;
 		
		PreparedStatement pstmt = null;
		String escSupStr = SupStr.replace("'", "''");  // GEE don't you like JAVA
		try {
			    iInception = constants.IsInception(SupStr);
			    if(iInception != -1) {
			    	InceptionExtract inceptionExtract  = new InceptionExtract();
			    	inceptionExtract.FindDates(DateData, SupStr, iInception);
			    	DateData = inceptionExtract.finalDate;
			    	StartDate = inceptionExtract.inceptionDate;
			    }
			    else 
				    StartDate = constants.GetPriorDate(DateData, SupStr);
			        if(StartDate.length() > 64) 
			        	StartDate = StartDate.substring(0, 63);
			        if(EndDate.length() > 64)
			        	EndDate = EndDate.substring(0,63);
			        if(DateData.length() > 120) {
			        	if(escSupStr.length() == 0) {
			        		escSupStr = DateData.replace("'", "''");
			        		DateData = "";
			        	}
			        	else {
			        		DateData = DateData.substring(0, 120);
			        	}
			        }
	                query =  "Insert into dateref (SubUid, ItemVersion, TemplateUid, StockHolderUid, " + 
			                 "DateStr, StartDate, EndDate, SupText, TdColumn, MemberElementUid, StockholdersEquivalenceType) values(?,?,?,?,?,?,?,?,?,?,?)";
	                pstmt = con.prepareStatement(query);
	                pstmt.setInt(1, iSubUid);
	                pstmt.setInt(2, iItemVersion);
	                pstmt.setInt(3, iTemplateUid);
	                pstmt.setInt(4, StockHolderUid);
	                pstmt.setString(5, DateData);
	                pstmt.setString(6, StartDate);
	                pstmt.setString(7, EndDate);
	                pstmt.setString(8,escSupStr);
	                pstmt.setInt(9, tdColumn);
	                pstmt.setInt(10,0);
	                pstmt.setInt(11,0);
	                pstmt.execute();
			        query = "SELECT @@IDENTITY";
	                pstmt = con.prepareStatement(query);
	                rs = pstmt.executeQuery();
	                rs.next();
	                iRtn = rs.getInt(1);
	                rs.close();
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing dateref table: " + e.getMessage());
	    	GenericErrorCls(con, "WriteDateRef", "Exception writing Dateref: " + e.getMessage(),
	    	        0, iSubUid, iItemVersion, false);
		}
		return(iRtn);
	}
	
	public MatchStr[] GetListOfDateSup(Connection con) {
		MatchStr[] newList = null;
		ConfidenceLevel cl = new ConfidenceLevel();
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    
	    int iCount;
	    
	    try {
	    	pstmt = con.prepareStatement("Select count(*) from DateSupStr");
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new MatchStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new MatchStr();
	            }
	            //rs = stmt.executeQuery(query1);
		    	pstmt = con.prepareStatement("Select * from DateSupStr Order by Uid");
		    	rs = pstmt.executeQuery();
	            iCount = 0;
	            while(rs.next()) {
	        	    newList[iCount].key = rs.getInt(1);	        	
	        	    newList[iCount].OrigString = rs.getString(2);
	        	    newList[iCount].iType = rs.getInt(3);
	        	    newList[iCount].dConfidence = rs.getDouble(5);
	        	    newList[iCount].bSpanColumns =rs.getBoolean(6);
        	        newList[iCount].al = cl.RecurringSting(rs.getString(2));
	        	    iCount++;
	            }
	            rs.close();
	    	}
	    }
	    catch (SQLException e) {
	    	GenericErrorCls(con, "GetListOfdateSups", "Exception reading table: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
	    return(newList);
	}

	public void writeModifiers(Connection con, int FlUid, int FieldUid, String DataStr) {  //static
	    String query;
	        
	    try {
		    query =  "Insert into FieldModifiers (FieldLocatedUid, TagId, DataStr) values(?,?,?)";
		    PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setInt(1, FlUid);
	        pstmt.setInt(2, FieldUid);
	        pstmt.setString(3, DataStr);
	        pstmt.executeUpdate();
		    }
			catch (SQLException e) {
		    	GenericErrorCls(con, "WriteModifiers", "Exception writeing FieldModifiers: " + e.getMessage(),
		    	        0, 0, 0, false);
			}
	}

	public void writeFieldLocated(Connection con, int iCompanyUid, int iSubUid, int iItemVersion,  //static
                                        int TemplateId, int TemplateUid, int iTagUid, String FieldData, int iDateRefUid,
                                        boolean bAbstract, int destination, int FieldsRowUid, int iTrNdx, int iTdNdx) {
		int jiffyId = 1;
		
		jiffyId =  JiffyServer(con);
		switch (destination) {
		    case 0:
				if(bAbstract == false)
					WriteValueFieldLocated(con, iCompanyUid, iSubUid, iItemVersion, TemplateId, TemplateUid, 
							                      iTagUid, FieldData, iDateRefUid, jiffyId, FieldsRowUid, iTrNdx, iTdNdx);
				else
					WriteAbstractFieldLocated(con, iCompanyUid, iSubUid, iItemVersion, TemplateId, TemplateUid, iTagUid,
				               FieldData, jiffyId, true, iTrNdx, iTdNdx);
                break;
                
		    case 1:
		    	//DO nothing - this is already done
		    	break;
		    	
		    case 2: 
		    	WriteEin(con, iCompanyUid, FieldData);
		    	break;
		    	
		    case 3:
		    	WriteFormEndDate(con, iSubUid, iItemVersion, FieldData);
                break;
                
		    case 4:
		    	WriteIncorporationState(con, iCompanyUid, FieldData);
		    	break;
		    	
        }
		//return(iRtn);
	}
	
	private void WriteFormEndDate(Connection con, int iSubUid, int iItemVersion, String FieldData) {  //static
		String            query;
		PreparedStatement pstmt;
		int               i;
		int               j;
		String            temp = "";
		
		try {
			FieldData = FieldData.trim();
			if(FieldData.substring(FieldData.length() -1).equals(".") || 
			   FieldData.substring(FieldData.length() -1).equals(",")	)
				FieldData = FieldData.substring(0, FieldData.length() -1);
			int iNdx = FieldData.indexOf(";");
			if(iNdx > 0) {  // all characters AFTER 
				FieldData = FieldData.substring(0, iNdx);
			}
			// only allow a two space everything else is junk
			i = FieldData.indexOf(" ");
			if(i != -1) {
				i++;
				temp = FieldData.substring(i);
				j = temp.indexOf(" ");
				if(j != -1) {  // pos junk
					i += j +1;
					temp = FieldData.substring(i);
					j = temp.indexOf(" ");
					if(j != -1) {// got junk
						
					    FieldData = FieldData.substring(0, (i+j));
					}
				}
			}
			query = "Update Submissions set DocumentPeriodEnd = '" + FieldData + "' where Uid = " +
			         iSubUid + " AND Version = " + iItemVersion;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteFormEndDate", "Exception writing table: " + e.getMessage(),
	    	        0, iSubUid, iItemVersion, false);
		}
	}
	
	public void WriteIncorporationState(Connection con, int iCompanyId, String State) {  //static
		String            query;
		PreparedStatement pstmt;
		
		try {
			query = "Update Company set StateIncorporated = '" + State + "' where Uid = " + iCompanyId;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteIncorporationState", "Exception writing table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		
	}
	
	public void WriteEin(Connection con, int iCompanyUid, String EinStr) {  //static
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "Update Company set Ein = '" + EinStr + "' where Uid = " + iCompanyUid;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
			query = "Select Uid from Cik_ids where CompanyUid = " + iCompanyUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				query = "Update Cik_ids set EIN = '" + EinStr + "' where Uid = " + rs.getInt(1);
				pstmt = con.prepareStatement(query);
				pstmt.executeUpdate();
			}
			else {
				query = "Insert into Cik_Ids ( CompanyUid, EIN, CIK) values (?,?,?)";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, iCompanyUid);
				pstmt.setString(2, EinStr);
				pstmt.setString(3, "");
			    pstmt.executeUpdate();
			}
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteEin", "Exception writing table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
	
	}
	
	public int WriteMatchedDataStr(Connection con, SubmissionInfo si, int taggedFieldUid,   //static
			                              int iTemplateId, String DataStr, int iTrNdx, int iTdNdx, int iTemplateType) {
		
		int iRtn = GenericWriteFieldsRowstr(con, si.getUid(), si.getVersion(), taggedFieldUid, iTemplateId, 
				                            DataStr, iTrNdx, iTdNdx, iTemplateType);
		return(iRtn);
	}
	
	private int GenericWriteFieldsRowstr(Connection con, int iSubUid, int iVersion, int taggedFieldUid,  //static
			                                    int iTemplateUid, String DataStr, int iTrNdx, int iTdNdx,
			                                    int iTemplateType) {
	    String    query;
	    int       iRtn = 0;
		ResultSet rs;
	    String    writeData = DataStr;
	    HASH_UTILS  utils = new HASH_UTILS();
	    
	    long      hashedUid = utils.hashValue(DataStr);
	    // not sure about this but here ya go
	    if((iTdNdx > 1) && (iTemplateType > 0))
	    	writeData = "";
		try {
	        query =  "Insert into FieldsRowStr (SubUid, ItemVersion, TaggedFieldUid, TemplateUid, RowData, TRIndex, TDIndex) values(?,?,?,?,?,?,?)";
		    PreparedStatement pstmt = con.prepareStatement(query);
		    pstmt.setInt(1, iSubUid);
		    pstmt.setInt(2, iVersion);
		    pstmt.setLong(3, hashedUid);
		    pstmt.setInt(4, iTemplateUid);
		    pstmt.setString(5, writeData);
		    pstmt.setInt(6, iTrNdx);
		    pstmt.setInt(7, iTdNdx);
		    pstmt.executeUpdate();
		    //query = "Select Last_Insert_id()";
		    query = "SELECT @@IDENTITY";
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
 		    rs.next();
	        iRtn = rs.getInt(1);
	        rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteMatchedDataStr", "Exception writing table: " + e.getMessage(),
	    	        0, iSubUid, iVersion, false);
		}
		return(iRtn);
	}

	
	public void WriteAbstractFieldLocated(Connection con, int iCompanyUid, int iSubUid, int iItemVersion,  //static
                                                 int TemplateId, int TemplateUid, int iTagUid, String FieldData, int jiffyId,
                                                 boolean bWriteFieldsRowRec, int iTrNdx, int iTdNdx) {
	    
	    String            query;
		PreparedStatement pstmt;
		
		try {
			if(bWriteFieldsRowRec) {
				GenericWriteFieldsRowstr(con, iSubUid, iItemVersion, iTagUid, TemplateUid, FieldData, iTrNdx, iTdNdx, TemplateId);
			}
	        query =  "Insert into Abstracts (TaggedUid, TemplateId, SubUid, ItemVersion, JiffyId, FieldData)" +
	                 " values(?,?,?,?,?,?)";
		    pstmt = con.prepareStatement(query);
		    pstmt.setInt(1, iTagUid);
            pstmt.setInt(2, TemplateId);
	        pstmt.setInt(3, iSubUid);
		    pstmt.setInt(4, iItemVersion);
		    pstmt.setInt(5, jiffyId);
		    pstmt.setString(6, FieldData);
		    pstmt.executeUpdate();
		    /*********
		    query = "SELECT @@IDENTITY";
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
 		    rs.next();
	        iRtn = rs.getInt(1);
	        *********/
		}
		catch (SQLException e) {
		    //System.err.print("Exception on writing fieldslocatedtable: " + e.getMessage());
	    	GenericErrorCls(con, "WriteAbstractFieldLocated", "Exception writing table: " + e.getMessage(),
	    	        0, iSubUid, iItemVersion, false);
		}
		return;
    }
	
    private void WriteValueFieldLocated(Connection con, int iCompanyUid, int iSubUid, int iItemVersion,  //static
                                               int TemplateId, int TemplateUid, int iTagUid, String FieldData, int iDateRefUid,
                                               int jiffyId, int FieldsRowUid, int iTrNdx, int iTdNdx) {
        String    query;
       
		try {
	        query =  "Insert into FieldsLocated (CompanyUid, SubUid, ItemVersion, TemplateId, TagUID, " +
	                 "DateRefUid, FieldsRowUid, DataStr, JiffyId, TRIndex, TDIndex) values(?,?,?,?,?,?,?,?,?,?,?)";
	        PreparedStatement pstmt = con.prepareStatement(query);
	        pstmt.setInt(1, iCompanyUid);
	        pstmt.setInt(2, iSubUid);
	        pstmt.setInt(3, iItemVersion);
	        pstmt.setInt(4, TemplateId);
	        pstmt.setInt(5, iTagUid);
	        pstmt.setInt(6, iDateRefUid);
	        pstmt.setInt(7, FieldsRowUid);
	        pstmt.setString(8, FieldData);
	        pstmt.setInt(9, jiffyId);
	        pstmt.setInt(10, iTrNdx);
	        pstmt.setInt(11, iTdNdx);
	        pstmt.execute();
	       // String OutStr = DebugOut(TemplateId, TemplateUid, iTagUid, FieldsRowUid, FieldData);
	    	//GenericErrorCls(con, "WriteValueFieldLocated", OutStr,
	    	//        iCompanyUid, iSubUid, iItemVersion, false);
		    //query = "Select Last_Insert_id()";
	        /************************
		    query = "SELECT @@IDENTITY";
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
		    rs.next();
		    iRtn = rs.getInt(1);
		    **************/
	    }
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteValueFieldLocated", "Exception writing table: " + e.getMessage(),
	    	        iCompanyUid, iSubUid, iItemVersion, false);
		}
		return;
	}
    private String DebugOut(int TemplateId, int TemplateUid, int TagUid, int FieldsRowStr, String FieldData) {
    	String Rtn = "";
    
        Rtn= "Tid=" + TemplateId + ":TUid=" + TemplateUid + "FRS=" + FieldsRowStr + ":" + FieldData;
        return(Rtn);
    }
    
    public UnMappedRowCls WriteRawDataRow(Connection con, String FieldData, int iSubUid, int iItemVersion, int iRowNum,  //static
                                                 int iTemplateId, int iTemplateUid, int iCompanyId, int iTrNdx, int iTdNdx) {
    	
    	UnMappedRowCls    rtnCls = new UnMappedRowCls();
		ResultSet         rs;
	    String            query;
	    PreparedStatement pstmt;
  	    //String            FieldData = "";
  	    int               taggedFieldUid = 0;
        //FieldData = CONSTANTS.UnMappedRowLabel; // + JiffyServer(con);

        taggedFieldUid = WriteTaggedFieldEntry(con, FieldData, iTemplateId, iCompanyId, iSubUid, iItemVersion);
  	    rtnCls.setTaggedFieldUid(taggedFieldUid);
	    query = "INSERT into TaggedField (TemplateId, Tag, CompanyId, SourceType, Threshold, PathToData, FieldModifier, Abstract," +
        " IsCompanyTag, Destination) values(?,?,?,?,?,?,?,?,?,?)";
        try {
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, iTemplateId);
            pstmt.setString(2, FieldData);
            pstmt.setInt(3, iCompanyId);
            pstmt.setInt(4, 1);
            pstmt.setDouble(5, CONSTANTS.THRESHOLD);
            pstmt.setString(6,"XX");
            pstmt.setString(7,"X");
            pstmt.setInt(8,0);
            pstmt.setBoolean(9, false);
            pstmt.setInt(10, 0);
            pstmt.executeUpdate();
            query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            rtnCls.setTaggedFieldUid(rs.getInt(1));
            rs.close();
            rtnCls.setFieldsRowUid(GenericWriteFieldsRowstr(con, iSubUid, iItemVersion, rtnCls.getTaggedFieldUid(), iTemplateUid, FieldData,
            		                                        iTrNdx, iTdNdx, iTemplateId));
            }
        catch (SQLException e) {
	        GenericErrorCls(con, "WriteRawDataRow", "Exception writing table: " + e.getMessage(),
	                        0, iSubUid, iItemVersion, false);
        }
    	return(rtnCls);
    }
    
    private int WriteTaggedFieldEntry(Connection con, String tag, int iTemplateId, int iCompanyId, int iSubUid, int iItemVersion) {
    	
        String            query;
  	    String            FieldData = "";
        PreparedStatement pstmt;
        ResultSet         rs;
        int               iRtn = 0;
        
	    query = "INSERT into TaggedField (TemplateId, Tag, CompanyId, SourceType, Threshold, PathToData, FieldModifier, Abstract," +
	            " IsCompanyTag, Destination) values(?,?,?,?,?,?,?,?,?,?)";
	    try {
	        FieldData = CONSTANTS.UnMappedRowLabel; // + JiffyServer(con);
	        pstmt = con.prepareStatement(query);
	        pstmt.setInt(1, iTemplateId);
	        pstmt.setString(2, tag);
	        pstmt.setInt(3, iCompanyId);
	        pstmt.setInt(4, 1);
	        pstmt.setDouble(5, CONSTANTS.THRESHOLD);
	        pstmt.setString(6,"XX");
	        pstmt.setString(7,"X");
	        pstmt.setInt(8,0);
	        pstmt.setBoolean(9, false);
	        pstmt.setInt(10, 0);
	        pstmt.executeUpdate();
	        query = "SELECT @@IDENTITY";
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        rs.next();
	        iRtn = rs.getInt(1);
	        rs.close();
	    }
	    catch (SQLException e) {
            GenericErrorCls(con, "WriteTaggedFieldEntry", "Exception writing table: " + e.getMessage(),
                            0, iSubUid, iItemVersion, false);
	    }
	    return(iRtn);
    }
    
	public UnMappedRowCls writeUnmappedNode(Connection con, int iSubUid, int iItemVersion, String FieldData, int iRowNum,  //static
			          int iTemplateId, int iTemplateUid, boolean bIsAbstract, int iCompanyId, int iTrNdx, int iTdNdx) {
	    UnMappedRowCls    rtnCls = new UnMappedRowCls();
		ResultSet         rs;
	    String            query;
	    PreparedStatement pstmt;
	    int               iIsAbstract = 0;
	    if(bIsAbstract == true)
	    	iIsAbstract = 1;
	    query = "INSERT into TaggedField (TemplateId, Tag, CompanyId, SourceType, Threshold, PathToData, FieldModifier, Abstract," +
	            " IsCompanyTag, Destination) values(?,?,?,?,?,?,?,?,?,?)";
	    try {
	        pstmt = con.prepareStatement(query);
	        pstmt.setInt(1, iTemplateId);
	        pstmt.setString(2, FieldData);
	        pstmt.setInt(3, iCompanyId);
	        pstmt.setInt(4, 1);
	        pstmt.setDouble(5, CONSTANTS.THRESHOLD);
	        pstmt.setString(6,"DT");
	        pstmt.setString(7,"N");
	        pstmt.setInt(8,iIsAbstract);
	        pstmt.setBoolean(9, false);
	        pstmt.setInt(10, 0);
	        pstmt.executeUpdate();
		    query = "SELECT @@IDENTITY";
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
		    rs.next();
		    rtnCls.setTaggedFieldUid(rs.getInt(1));
		    rs.close();
		    rtnCls.setFieldsRowUid(GenericWriteFieldsRowstr(con, iSubUid, iItemVersion, rtnCls.getTaggedFieldUid(), iTemplateUid, FieldData,
		    		               iTrNdx, iTdNdx, iTemplateId));
		    rtnCls.setSpareJiffy(JiffyServer(con));
	    }
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteUnmappedNode", "Exception writing table: " + e.getMessage(),
	    	        0, iSubUid, iItemVersion, false);
		}
	    return(rtnCls);
	}
	
	public void setTaggedFieldToAbstract(Connection con, int iTaggedFieldUid) {  //static
		
		String            query;
		PreparedStatement pstmt;
		
		try {
			query = "Update TaggedField set Abstract = 1, PathToData = '~' where Uid = " + iTaggedFieldUid;
			pstmt = con.prepareStatement(query);
			pstmt.execute();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "setTaggedFieldToAbstract", "Exception writing table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		
	}
	
	public void readFieldsLocated(Connection con, int CompanyUID, int SubUID, int ItemVersion,   //static
			                             String FileName) {
		ResultSet            rtnSet = null;
	    PreparedStatement    pstmt = null;
		FileOutputStream     ostream = null;
		DataOutputStream     out = null;
		BufferedWriter       wr = null;
		int                  TagUID;
		int                  iDateRefUid;
		String               DataStr;
        String               TagStr;
        ArrayList<StockCls>  sc;
        
		try {
	    	pstmt = con.prepareStatement("Select TagUID, ColumnNum, DateRefUid, DataStr from fieldslocated " +
	    			                     "where CompanyUid = " + CompanyUID + " AND SubUID = " + SubUID + 
	    			                     " AND ItemVersion = " + ItemVersion +
	    			                     " ORDER by UID");
	    	rtnSet = pstmt.executeQuery();
	        ostream = new FileOutputStream(FileName);
	        out = new DataOutputStream(ostream);
	        wr = new BufferedWriter(new OutputStreamWriter(out));
		    while( rtnSet.next() == true) {
		        TagUID = rtnSet.getInt(1);
		        //ColumnNum = rtnSet.getInt(2);
		        iDateRefUid = rtnSet.getInt(3);
		        DataStr = rtnSet.getString(4);
		        TagStr = getTagTextFromID(con, TagUID);
		        wr.write(TagStr);
		        wr.newLine();
		        if(iDateRefUid == 0)
		            wr.write("     " + DataStr);
		        else {
		        	TagStr = getDateStrFromRef(con, iDateRefUid);
		        	wr.write("     " + TagStr + "    " + DataStr);
		        }
		        wr.newLine();
		        wr.newLine();
		    }
		    rtnSet.close();
		    
		    // write note information
		    pstmt = con.prepareStatement("Select NoteIndex, Context from notewords where " + 
		    		"SubUid = " + SubUID + " AND ItemVersion = " + ItemVersion +
		    		" order by Uid");
            rtnSet = pstmt.executeQuery();
            int CurNoteIndex = 0;
            int ThisNoteIndex;
            while(rtnSet.next() == true) {
            	ThisNoteIndex = rtnSet.getInt(1);
            	if(ThisNoteIndex != CurNoteIndex) {
            	    TagStr = "Note " + ThisNoteIndex + " Found Items";
            	    wr.write(TagStr);
            	    wr.newLine();
            	    CurNoteIndex = ThisNoteIndex;
            	}
            	TagStr = "    DATA: " + rtnSet.getString(2);
           	    wr.write(TagStr);
        	    wr.newLine();           	
            }
		    
		    
	        wr.flush();
	        wr.close();
	        out.close();
	        ostream.close();
	        pstmt.close();	            
		}
	    catch (Exception e) {
	    	GenericErrorCls(con, "ReadFieldsLocated", "Exception reading table: " + e.getMessage(),
	    	        CompanyUID, SubUID, ItemVersion, false);
	    }
	
	}

	/***************************************************************************/
	/* CommonStockDimensionUid:                                                */
	/*          1 : no Dimensition                                             */
	/*          2: Common Class A                                              */
	/*          3: Common Class B                                              */
	/*          4: NonVoting Common Stock                                      */
	/*          5: Convertible Common Stock                                    */
	/***************************************************************************/
	
	private int WriteStockRecord(Connection con, int CompanyId, int Uid, int Version,  //static
			                            int Dimension, float Value, int Shares) {
	
        int        iUid = 0;
        ResultSet  rs;
        String     query;
        
		try {			
		    query = "Insert into Stock (SubUid, ItemVersion, TemplateId, Value, Shares, CommonStockDimensionsUid)" +
			                   " values(?,?,?,?,?,?)";
		    PreparedStatement pstmt = con.prepareStatement(query);
	        pstmt.setInt(1, Uid);
            pstmt.setInt(2,Version);
	        pstmt.setInt(3, -2);
	        pstmt.setFloat(4, Value);
		    pstmt.setInt(5, Shares);
	        pstmt.setInt(6, Dimension);
	        pstmt.executeUpdate();
		    query = "SELECT @@IDENTITY";
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        rs.next();
	        iUid = rs.getInt(1);
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteStockRecord", "Exception writing table: " + e.getMessage(),
	    	        CompanyId, Uid, Version, false);
		}
        return(iUid);
	}
	

	public int WriteCombinedStockRecord(Connection con, SubmissionInfo si, CombinedStock cs, String dateStr) {  //static
		int       iUid = 0;
		
		
		if(cs.NoDimensionShares > 0)
			WriteStockRecord(con, si.getCompanyId(), si.getUid(), si.getVersion(), CONSTANTS.NO_DIMEMSION, 
					         cs.NoDimensionValue, cs.NoDimensionShares);
		if(cs.CommonStockAShares > 0)
			WriteStockRecord(con, si.getCompanyId(), si.getUid(), si.getVersion(), CONSTANTS.COMMONCLASSA, 
					         cs.CommonStockAValue, cs.CommonStockAShares);
		if(cs.CommonStockBShares > 0)
			WriteStockRecord(con, si.getCompanyId(), si.getUid(), si.getVersion(), CONSTANTS.COMMONCLASSB, 
					         cs.CommonStockBValue, cs.CommonStockBShares);
		if(cs.NonVotingCommonStockShares > 0)
			WriteStockRecord(con, si.getCompanyId(), si.getUid(), si.getVersion(), CONSTANTS.NONVOTING, 
					         cs.NonVotingCommonStockValue, cs.NonVotingCommonStockShares);
		if(cs.ConvertibleCommonStockShares > 0)
			WriteStockRecord(con, si.getCompanyId(), si.getUid(), si.getVersion(), CONSTANTS.CONVERTIBLE, 
					         cs.ConvertibleCommonStockValue, cs.ConvertibleCommonStockShares);	
		if(cs.PreferredStockShares > 0)
			WriteStockRecord(con, si.getCompanyId(), si.getUid(), si.getVersion(), CONSTANTS.PREFERRED, 
					         cs.PreferredStockValue, cs.PreferredStockShares);	
		return(iUid);
	}
	
	
	public int WriteStockAllocation(Connection con, int stockUid, int dateRefUid,  //static
                                          AuthShare authShare, float stockValue,
                                          String stockType) {
        int 	   RecUid = 0;
        ResultSet  rs;
        
        try {
        	String query = "Insert into StockIssued (StockUid, NumSharesIssued, NumSharesOutstanding, Value, " +
        			       "DateRefUid, Type) values(?,?,?,?,?, ?)";
		    PreparedStatement pstmt = con.prepareStatement(query);
		    pstmt.setInt(1, stockUid);
		    pstmt.setInt(2, authShare.iIssued);
		    pstmt.setInt(3, authShare.iOutstanding);
		    pstmt.setFloat(4, stockValue);
		    pstmt.setInt(5, dateRefUid);
		    pstmt.setString(6, stockType);
		    pstmt.executeUpdate();
	        //query = "Select Last_Insert_id()";
		    query = "SELECT @@IDENTITY";
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        rs.next();
	        RecUid = rs.getInt(1);
        }
        catch (SQLException e) {
	    	GenericErrorCls(con, "WriteStockAllocation", "Exception writing table: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
        return(RecUid);
    }

	
	public ArrayList<StockCls> RetrieveStockInfo(Connection con, int SubUID, int ItemVersion) {  //static
		ArrayList<StockCls>  sc = new ArrayList<StockCls>();
		ResultSet            rtnSet = null;
		ResultSet            issuedSet = null;
	    PreparedStatement    pstmt = null;
	    PreparedStatement    pstmt1 = null;
	    int                  StockUid;
		int                  DateRefUid;
		ResultSet            DateRS;
		StockCls             curStockCls;
		
		try {
	    	pstmt = con.prepareStatement("Select Uid, Type, Value, NumIssued from Stock " +
	    			                     "where SubUID = " + SubUID + " AND ItemVersion = " + ItemVersion);
	    	rtnSet = pstmt.executeQuery();
		    while( rtnSet.next() == true) {
		    	curStockCls = new StockCls();
		    	curStockCls.issued = new ArrayList<StockIssuedCls>();
		    	//curStockCls.issued = null;
		    	StockUid = rtnSet.getInt(1);
		    	curStockCls.stockType = rtnSet.getInt(2);
		    	curStockCls.stockValue = rtnSet.getFloat(3);
		    	curStockCls.stockIssued = rtnSet.getInt(4);
		    	sc.add(curStockCls);
		    	pstmt = con.prepareStatement("Select NumShares, Value, DateRefUid from StockIssued " +
		    			"where StockUid = " + StockUid + " Order by Uid");
		    	issuedSet = pstmt.executeQuery();
		    	while(issuedSet.next() == true) {
		    		StockIssuedCls si = new StockIssuedCls();
		    		si.NumIssued = issuedSet.getInt(1);
		    		si.stockValue = issuedSet.getFloat(2);
		    		DateRefUid = issuedSet.getInt(3);
		    		pstmt1 = con.prepareStatement("Select DateStr from DateRef where UID = " + DateRefUid);
		    		DateRS = pstmt1.executeQuery();
		    		if(DateRS.next() == true)
		    			si.stockDateIssued = DateRS.getString(1);
		    		DateRS.close();
		    		curStockCls.issued.add(si);
		    	}
		    	issuedSet.close();
		    }
	    	rtnSet.close();
		}
		catch (Exception e) {
	    	GenericErrorCls(con, "RetrieveStockInfo", "Exception reading table: " + e.getMessage(),
	    	        0, SubUID, ItemVersion, false);
		}
		return(sc);
	}
	
	public String getTagTextFromID(Connection con, int TagUID) {  //static
		ResultSet rs = null;
		String rtnStr = "";
		PreparedStatement pstmt = null;
		
		try {
			pstmt = con.prepareStatement("Select Tag from taggedfield where UID = " + TagUID);
			rs = pstmt.executeQuery();
			if( rs.next() == true) {
				rtnStr = rs.getString(1);
			}
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetTagTextFromID", "Exception reading table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		return rtnStr;
	}

	public String getDateStrFromRef(Connection con, int RefUID) {  //static
		ResultSet rs = null;
		String rtnStr = "";
		PreparedStatement pstmt = null;
		
		try {
			pstmt = con.prepareStatement("Select DateStr, SupText from DateRef where UID = " + RefUID);
			rs = pstmt.executeQuery();
			if( rs.next() == true) {
				
				rtnStr =  rs.getString(2);
				if(rtnStr.length() > 1)
					rtnStr = rtnStr.concat(" ");
				rtnStr = rtnStr.concat(rs.getString(1));
			}
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetdateStrFromRef", "Exception reading table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		return rtnStr;
	}
	/***************
	public static void SetEin(Connection con, int SubUid, int ItemVersion, int CompanyUid) {
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		String EinStr = "";
		
		try {
			pstmt = con.prepareStatement("Select DataStr From FieldsLocated where CompanyUid = " + CompanyUid +
					                     " AND SubUid = " + SubUid + " AND ItemVersion = " + ItemVersion + 
					                     " AND TagUID = 8 ");
			rs = pstmt.executeQuery();
			if(rs.next() == true) {
				EinStr = rs.getString(1);
				String query = "Update Company set EIN = '" + EinStr + "' where UID = " + CompanyUid;
				Statement stmt = con.createStatement();
				stmt.executeUpdate(query);
				stmt.close();
				query = "Update Cik_ids set EIN = '" + EinStr + "' where UID = " + CompanyUid;
				Statement stmt1 = con.createStatement();
				stmt1.executeUpdate(query);
				stmt1.close();
			}
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "SetEIN", "Exception reading writing table: " + e.getMessage(),
	    	        CompanyUid, SubUid, ItemVersion, false);
		}
	}
****************/
	public ArrayList<Template7Col> GetTemplate7Columns(Connection con, int typeKey) {  //static
		ArrayList<Template7Col> rtnItems = new ArrayList<Template7Col>();
		String query = "Select ColumnText, DateRefTxt from Template7 where TemplateKey = " + typeKey +
		               " Order by Uid";

	    ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			// check if record already exists.
			while(rs.next() == true){
				Template7Col nextItem = new Template7Col();
				String temp  = rs.getString(1);
                nextItem.SetDbText(temp);
                nextItem.SetDateRefTxt(rs.getString(2));
                rtnItems.add(nextItem);
			}
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetTemplkate7Columns", "Exception reading table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		return(rtnItems);
	}

	public void UpdateNoteIdentifedText(Connection con, int TemplateParseUid, String UpdateText) {  //static
		
		PreparedStatement pstmt = null;
		
	    try {
	    	pstmt = con.prepareStatement("Update templateparse set IdentifiedText = '" + UpdateText +
	    			                     "' where Uid = " + TemplateParseUid);
	    	pstmt.execute();
	    }
	    catch (Exception e) {
	    	GenericErrorCls(con, "UpdateNoteIdentifiedText", "Exception update table: " + e.getMessage(),
	    	        0, 0, 0, false);
	    }
	}
	
	public void InsertNoteWord(Connection con, SubmissionInfo si, int TemplateParseUid,   //static
			                         String curWord, int contextType, int TemplateIndex) {
		
	    int 	   RecUid = 0;
	    ResultSet  rs;
	        
	    try {
	        String query = "Insert into NoteWords (CompanyUid, NoteIndex, NoteUid, SubUid, ItemVersion, " +
	        			       "Context, ContextType, Gaap_Id, Gaap_Role, Gaap_Dimension)" +
	        			       "values(?,?,?,?,?,?,?,?,?,?)";
			PreparedStatement pstmt = con.prepareStatement(query);
		    pstmt.setInt(1, si.getCompanyId());
		    pstmt.setInt(2, TemplateIndex);
		    pstmt.setInt(3, TemplateParseUid);
			pstmt.setInt(4, si.getUid());
		    pstmt.setInt(5, si.getVersion());
		    pstmt.setString(6, curWord);
		    pstmt.setInt(7, contextType);
		    pstmt.setString(8, "");
		    pstmt.setString(9, "");
		    pstmt.setString(10, "");
		    pstmt.executeUpdate();
	    }
	    catch (SQLException e) {
	        //System.out.println("Error writing notewords: " + e.getMessage());
	    	GenericErrorCls(con, "InsertNoteWord", "Exception writing table: " + e.getMessage(),
	    	        si.getCompanyId(), si.getUid(), si.getVersion(), false);
	    }
	}
	
	public boolean IsStockAllowed(Connection con, int iTemplateType) {  //static
		boolean           bRtn = false;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "Select isSharesAllowing from TemplateGaapXref where TemplateId = " + iTemplateType;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				bRtn = rs.getBoolean(1);
			}
			rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "IsStockAllowed", "Exception reading table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		return(bRtn);
	}
	
	/****************************
	public ArrayList<Integer> GetNoteTableUid(Connection con, int NoteUid) {  //static
		ArrayList<Integer>  RtnUid = new ArrayList<Integer>();
		String              query;
		PreparedStatement   pstmt;
		ResultSet           rs;
		//Integer             CurRec;
		
		try {
			query = "SELECT Uid From NoteTables where NoteUid = " + NoteUid + " ORDER by JiffyId";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				//CurRec = new Integer;
				//CurRec = rs.getInt(1);
				RtnUid.add(rs.getInt(1));
			}
			rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetNoteTableUid", "Exception reading table: " + e.getMessage(),
	    	        0, 0, 0, false);
		}
		return(RtnUid);
	}
******************/
	
	public void SetNoteTableScale(Connection con, int iNoteTableUid, int iScale) {  //static
		
		String            query;
		PreparedStatement pstmt;
		
		try {
			//query = "UPDATE NoteTables set Scale = " + iScale + " where Uid = " + iNoteTableUid;
			query = "UPDATE TemplateParse set Scale = " + iScale + " where Uid = " + iNoteTableUid;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "SetNoteTableScale", "Exception updating table: " + e.getMessage(),
	    			iNoteTableUid, 0, 0, false);
		}
		
	}
	
	private ArrayList<NoteScaleRec> GetNoteScales(Connection con) {  //static
		ArrayList<NoteScaleRec> rtnArray = new ArrayList<NoteScaleRec>();
		
		String             query;
		PreparedStatement  pstmt;
		ResultSet          rs;
		NoteScaleRec       nsc;
		
		try {
			query = "Select * from NoteScales";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				nsc = new NoteScaleRec();
				nsc.ScaleStr = rs.getString(2);
				nsc.ScaleValue = rs.getInt(3);
				rtnArray.add(nsc);
			}
			rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetNoteScales", "Exception reading table: " + e.getMessage(), 0, 0, 0, false);
		}
		return(rtnArray);
	}
	
	public void WriteColumnZero(Connection con, int iNoteTableUid, String ColumnZero) {  //static
		String                  query;
		PreparedStatement       pstmt;
		ArrayList<NoteScaleRec> noteScaleRec  = new ArrayList<NoteScaleRec>();
		NoteScales              noteScales = new NoteScales();
		String                  escapedStr = "";
		
		try {
			ColumnZero = ColumnZero.trim();
			noteScaleRec = GetNoteScales(con);
			noteScales.ExtractNoteScale(ColumnZero, noteScaleRec);
			escapedStr = noteScales.ExtractedStr.replace("'", "''");
			//query = "UPDATE NoteTables set ColumnZeroText = '" + escapedStr + "', Scale =  " + noteScales.NoteScale +
			//        " where Uid = " + iNoteTableUid;
			query = "UPDATE NoteTableDetails set ColumnZeroText = '" + escapedStr + "' where NoteTableUid = " + iNoteTableUid;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
			query = "UPDATE TemplateParse set Scale = " + noteScales.NoteScale + " where Uid = " + iNoteTableUid;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "WriteColumnZero", "Exception updating table: " + e.getMessage() + ":: length = " + ColumnZero.length(),
	    			iNoteTableUid, 0, 0, false);
		}
		
		
	}
	
	public void DeleteNoteTableRec(Connection con, int iNoteTableUid) {  //static
		
		String            query;
		PreparedStatement pstmt;
		
		try {
			//query = "DELETE from NoteTables where UId = " + iNoteTableUid ;
			query = "DELETE from TemplateParse where UId = " + iNoteTableUid ;
			pstmt = con.prepareStatement(query);
			pstmt.execute();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "DeleteNoteTableRec", "Exception Deleting From table: " + e.getMessage(),
	    			iNoteTableUid, 0, 0, false);
		}
	}
	
	public int InsertNoteTableRow(Connection con, SubmissionInfo si, int NoteTableUid, String RowData, int iTrNdx, int iTdNdx) {  //static
		int               iRtn = 0;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		//int               JiffyId = JiffyServer(con);
		int               taggedFieldUid = 0;
		HASH_UTILS        hashUtils = new HASH_UTILS();
		long              hashUid = hashUtils.hashValue(RowData);
		try {
			if(RowData.length() == 0)
				RowData = CONSTANTS.UnMappedRowLabel; // + JiffyServer(con);
			/************************
            query =  "Insert into NoteRowLabels (NoteTableUid, RowData, JiffyId) values(?,?,?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, NoteTableUid);
            pstmt.setString(2, RowData);
            pstmt.setInt(3, JiffyId);
            ******************/
			taggedFieldUid = FindTaggedFieldUid(con, si, RowData);
			query = "Insert into FieldsRowStr (SubUid, ItemVersion, TaggedFieldUid, RowData, TemplateUid, TrIndex, TdIndex) Values(?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, si.getUid());
			pstmt.setInt(2, si.getVersion());
			pstmt.setLong(3, hashUid);
			pstmt.setString(4, RowData);
			pstmt.setInt(5, NoteTableUid);
			pstmt.setInt(6, iTrNdx);
			pstmt.setInt(7, iTdNdx);
            pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing dateref table: " + e.getMessage());
			String temp  = RowData;
	    	GenericErrorCls(con, "InsertNoteTableRow", "Exception writing record: " + e.getMessage(),
	    	        0, 0, NoteTableUid, false);
		}

		return(iRtn);
	}
	
	
	private int FindTaggedFieldUid(Connection con, SubmissionInfo si, String taggedField) {
		int               taggedFieldUid = 0;
	    String            query;
	    PreparedStatement pstmt;
	    ResultSet         rs;
    	UnMappedRowCls    rtnCls = new UnMappedRowCls();
	    String            escapedTagged;
	    
    	try {
    		escapedTagged = taggedField.replace("'", "''");
	        query = "Select UId from TAGGEDFIELD where TemplateId = -5 AND CompanyId = " + si.getCompanyId() +
	    	    	" AND PathToData = 'XX' AND FieldModifier = 'X' AND Tag = '" + escapedTagged + "'";
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        if(rs.next()) 
	    	    taggedFieldUid = rs.getInt(1);
	        else {
	    	    rtnCls = WriteRawDataRow(con, taggedField, si.getUid(), si.getVersion(), 0,  //static
                        -5, 0, si.getCompanyId(), 0, 0);
	        }
    	}
    	catch (SQLException e) {
	    	GenericErrorCls(con, "FindTaggedFieldUid", "Exception read record: " + e.getMessage(),
	    	        si.getUid(), si.getVersion(), 0, false);
  		
     	}
		return(taggedFieldUid);
	}
	
	public void InsertNoteTableColumn(Connection con, SubmissionInfo si, int iNoteUid, int iRowLabelUid, int iDateRefUid,
			                          String NodeData, int iTrNdx, int iTdNdx) {  //static
		
		String            query;
		PreparedStatement pstmt;
		int               jiffyId = JiffyServer(con);
		String            tempNodeData = NodeData;
		
		try {
			/***********************
            query =  "Insert into NoteFieldsLocated (NoteUid, DateRefUid, DataStr, JiffyId) values(?,?,?,?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, iRowLabelUid);
            pstmt.setInt(2, iDateRefUid);
            pstmt.setString(3, NodeData);
            pstmt.setInt(4, JiffyId);
            *****************/
			//if(tempNodeData.length() > 2048)
			//	tempNodeData = tempNodeData.substring(0, 2047);
	        query =  "Insert into FieldsLocated (CompanyUid, SubUid, ItemVersion, TemplateId, TagUID, " +
	                 "DateRefUid, FieldsRowUid, DataStr, JiffyId, TRIndex, TDIndex) values(?,?,?,?,?,?,?,?,?,?,?)";
	        pstmt = con.prepareStatement(query);
	        pstmt.setInt(1, si.getCompanyId());
	        pstmt.setInt(2, si.getUid());
	        pstmt.setInt(3, si.getVersion());
	        pstmt.setInt(4, iNoteUid);
	        pstmt.setInt(5, 0);
	        pstmt.setInt(6, iDateRefUid);
	        pstmt.setInt(7, iRowLabelUid);
	        pstmt.setString(8, tempNodeData);
	        pstmt.setInt(9, jiffyId);
	        pstmt.setInt(10, iTrNdx);
	        pstmt.setInt(11, iTdNdx);

            pstmt.execute();
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing dateref table: " + e.getMessage());
	    	GenericErrorCls(con, "InsertNoteTableColumn", "Exception writing record: " + e.getMessage(),
	    	        0, iRowLabelUid, iDateRefUid, false);
		}
		
	}
	
	
	public ArrayList<SubmissionTemplates> GetSubmissionTemplates(Connection con, SubmissionInfo si) {  //static
		ArrayList<SubmissionTemplates> rtnArray = new ArrayList<SubmissionTemplates>();
		SubmissionTemplates            curRec;	
		String                         query;
		PreparedStatement              pstmt;
		ResultSet                      rs;
		
		try {
		    query = "SELECT Uid, TemplateId From TemplateParse where "  +
		             "SubUid = " + si.getUid() + " and Version = " + si.getVersion() +
		             " and TemplateId > 0 and TemplateId != 4 ORDER BY TemplateId ASC, Uid ASC";
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
		    while(rs.next()) {
		    	curRec = new SubmissionTemplates();
		    	curRec.TemplateUid = rs.getInt(1);
		    	curRec.TemplateId = rs.getInt(2);
		    	rtnArray.add(curRec);
		    }
		    rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetSubmissionTemplates", "Exception reading table: " + e.getMessage(),
	    			0, si.getUid(), si.getVersion(), false);
		}
		return(rtnArray);
	}
	
	public ArrayList<FieldLocatedXref> GetDistinctFieldRows(Connection con, SubmissionInfo si, int TemplateUid) {  //static
		String                         query;
		PreparedStatement              pstmt;
		ResultSet                      rs;		
		ArrayList<FieldLocatedXref>    dataStr = new ArrayList<FieldLocatedXref>();
		ResultSet                      fieldRowdataRs; 
		FieldLocatedXref               curRec;
		
		try {
		    query = "Select DISTINCT(FieldsRowUid), Uid from FieldsLocated where TemplateUid = " + TemplateUid + " ORDER BY Uid";
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
		    while(rs.next()) {
		    	query = "Select RowData from FieldsRowStr where Uid = " + rs.getInt(1);
		    	pstmt  = con.prepareStatement(query);
		    	fieldRowdataRs = pstmt.executeQuery();
		    	curRec = new FieldLocatedXref();
		    	if(fieldRowdataRs.next()) 
		    		curRec.RowData = fieldRowdataRs.getString(1);
		    	else
		    		curRec.RowData = "";
		    	curRec.FieldsRowStrUid = rs.getInt(1);
		    	dataStr.add(curRec);
		    	fieldRowdataRs.close();
		    }
		    rs.close();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "GetDistinctFieldRows", "Exception reading table: " + e.getMessage(),
	    			0, si.getUid(), si.getVersion(), false);
		}
		
		return(dataStr);
	}
	
	public void UpdateDuplicatedTemplate(Connection con, SubmissionInfo subInfo, ArrayList<FieldLocatedXref> FieldInfos,  //static
			                                    int TemplateUid1){
	
		
	}
	public int JiffyServer(Connection con) {  //static
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
            System.exit(-1);
		}		
		return (iRtn);
	}
	
	public int WriteAppError(Connection con, ErrorCls errorCls) {  //static
		int               iRtn = JiffyServer(con);
		String            ErrorText = "";
		Date              date = new Date();
		Timestamp         timestamp = new Timestamp(date.getTime());
		PreparedStatement pstmt;
		String            insertStr = "INSERT into APPERROR (CompanyUid, SubUid, ItemVersion, " +
		                              "Application, SrcFunction, ErrorLevel, ErrorText) values (?,?,?,?,?,?,?)";
		try {
			ErrorText = timestamp.toString() + " : " + errorCls.getErrorText();
			if(ErrorText.length() > 127)
				ErrorText = ErrorText.substring(1, 127);
			pstmt = con.prepareStatement(insertStr);
			pstmt.setInt(1,errorCls.getCompanyUid());
			pstmt.setInt(2,errorCls.getSubUid());
			pstmt.setInt(3,errorCls.getItemVersion());
			pstmt.setString(4,"FieldIdentification");
			pstmt.setString(5,errorCls.getFunctionStr());
			pstmt.setInt(6,iRtn);
			pstmt.setString(7, ErrorText);
			pstmt.execute();
			//if(errorCls.isBExit() == true)
			//    System.exit(iRtn);
		}
		catch (SQLException e) {
			System.exit(-1);
		}
		
		return(iRtn);
	}

	public ArrayList<TextConversionCls> GetTableEntries(Connection con, int iTableKey) {  //static
		ArrayList<TextConversionCls> rtnCls = new ArrayList<TextConversionCls>();
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		TextConversionCls thisEntry;
		
		try {
			query = "SELECT ConvText, ConvValue from ConversionTable where TYPE= 0 and SubType = " + iTableKey + " ORDER BY  UID";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				thisEntry = new TextConversionCls();
				thisEntry.setTextString(rs.getString(1));
				thisEntry.setConversionValue(rs.getInt(2));
				rtnCls.add(thisEntry);
			}
			rs.close();
		}
	    catch (SQLException e) {
    	    GenericErrorCls(con, "GetTableEntries", "Exception reading table: " + e.getMessage(),
    			            0, 0, 0, false);
	    }
	    return(rtnCls);	
	}

	public ArrayList<Integer> GetDuplicateTemplateUids(Connection con, int iSubUid, int iItemVersion,  //static
			                                                  int iTemplateId, int iTemplateUid) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		
		String            query;
		ResultSet         rs;
		PreparedStatement pstmt;
		
		try {
			query = "Select Uid from TemplateParse where SubUid = " + iSubUid + " AND Version = " + iItemVersion +
			        " AND TemplateId = " + iTemplateId + " AND Uid < " + iTemplateUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				rtnArray.add(rs.getInt(1));
			}
			rs.close();
		}
	    catch (SQLException e) {
    	    GenericErrorCls(con, "GetDuplicateTemplateUids", "Exception reading table: " + e.getMessage(),
    			            0, 0, 0, false);
    	    return(rtnArray);
	    }
		return(rtnArray);
	}
	
	public ArrayList<Integer> GetDateRefs(Connection con, int iTemplateUid) {  //static
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();		
		String             query;
		ResultSet          rs;
		PreparedStatement  pstmt;
		
		try {
			//query = "Select distinct(dateRefUid) from FieldsLOcated where fieldsrowUid IN " + 
			//        "(Select distinct(UID) from FieldsRowStr where TemplateUid = " + iTemplateUid + ")";
			query = "Select Uid from DATEREF where TemplateUid = " + iTemplateUid + " ORDER BY Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				rtnArray.add(rs.getInt(1));
			}
			rs.close();
		    //Collections.sort(rtnArray);
		}
	    catch (SQLException e) {
    	    GenericErrorCls(con, "GetDateRefs", "Exception reading table: " + e.getMessage(),
    			            0, 0, 0, false);
    	    return(rtnArray);
	    }
		return(rtnArray);
	}

	public DateRefRec GetDateRefRec(Connection con, int iDateRefUid) {  //static
		DateRefRec DRR = new DateRefRec();
		
		String             query;
		ResultSet          rs;
		PreparedStatement  pstmt;
		
		try {
			query = "Select * from DateRef where Uid = " + iDateRefUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				DRR.SetCompletedDate(rs.getString(4));
				DRR.SetSupStr(rs.getString(7));
			}
			rs.close();
		}
	    catch (SQLException e) {
    	    GenericErrorCls(con, "GetDateRefRec", "Exception reading table: " + e.getMessage(),
    			            0, 0, 0, false);
    	    return(DRR);
	    }
	
		return(DRR);
	}
	
	public ArrayList<String> GetDateForms(Connection con) {  //static
		String             query;
		ResultSet          rs;
		PreparedStatement  pstmt;
		ArrayList<String>  rtnArray = new ArrayList<String>();
		String             curStr;
		
		try {
			query = "Select Format from Date_Forms";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				curStr = new String();
				curStr = rs.getString(1);
				rtnArray.add(curStr);
			}
			rs.close();
		}
	    catch (SQLException e) {
    	    GenericErrorCls(con, "GetDateForms", "Exception reading table: " + e.getMessage(),
    			            0, 0, 0, false);
    	    curStr = new String();
    	    curStr = "MMM dd yyyy";
    	    rtnArray.add(curStr);
    	    curStr = new String();
    	    curStr = "MMM ddyyyy";
    	    rtnArray.add(curStr);
    	    curStr = new String();
    	    curStr = "mm/dd/yyyy";
    	    rtnArray.add(curStr);
	    }
        return(rtnArray);
	}
	
	public ArrayList<Integer> GetContinuationDateRefs(Connection con, SubmissionInfo si, int iTemplateType) {  //static
		ArrayList<Integer> rtnArray  = new ArrayList<Integer>();
		String             query;
		ResultSet          rs;
		PreparedStatement  pstmt;
		
		try {
			query = "SELECT min(Uid) from TEMPLATEPARSE where SubUid = " + si.getUid() + "AND Version = " + si.getVersion() +
			        " AND TemplateId = " + iTemplateType;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				query = "SELECT Uid from DATEREF where TemplateUid= " + rs.getInt(1) + " ORDER BY UID ASC";
				rs.close();
				pstmt = con.prepareStatement(query);
				rs = pstmt.executeQuery();
				while(rs.next()) {
					rtnArray.add(rs.getInt(1));
				}
				rs.close();
			}
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetContinuationDateRefs", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);
			
		}
		return(rtnArray);
	}
	
	public int GetTocEndLine(Connection con, SubmissionInfo si) {  //static
		int               iRtn = 0;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "SELECT EndLineNum from TemplateParse where SubUid = " + si.getUid() +
					" AND Version = " + si.getVersion() + " AND TemplateId = -1";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next())
				iRtn = rs.getInt(1);
			rs.close();
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetTocEndLine", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);
			
		}
		return(iRtn);
	}
	
	public ArrayList<NoteDetailKeyWord> GetNoteDetails(Connection con) {
		ArrayList<NoteDetailKeyWord> rtnArray = new ArrayList<NoteDetailKeyWord>();
		String                       query;
		PreparedStatement            pstmt;
		ResultSet                    rs;
		NoteDetailKeyWord            thisItem;
		try {
			query = "SELECT * from NoteKeyWords where Position IN (0,1) order by Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				thisItem = new NoteDetailKeyWord();
			    thisItem.SetUid(rs.getInt(1));
			    thisItem.SetKeyWord(rs.getString(2));
				thisItem.SetPlural(rs.getBoolean(3));
				thisItem.SetContain(rs.getBoolean(4));
				thisItem.SetPosition(rs.getInt(5));
				rtnArray.add(thisItem);
			}
			rs.close();
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetNoteDetails", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
		}	
		return(rtnArray);
	}
	
	
	public ArrayList<String> GetOmissionWords(Connection con) {
		ArrayList<String> rtnList = new ArrayList<String>();
		
		String                       query;
		PreparedStatement            pstmt;
		ResultSet                    rs;
		
		try {
			query = "SELECT keyword from NoteKeyWords where Position = 2 order by Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				rtnList.add(rs.getString(1));
			}
			rs.close();
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetNoteDetails", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
		}	
		return(rtnList);
	}
	
	public ArrayList<String> GetNoteConjunctions(Connection con) {
		ArrayList<String> rtnArray = new ArrayList<String>();
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "SELECT Conjunction from NoteConjunctions Order by Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next())
				rtnArray.add(rs.getString(1));
			rs.close();
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetNoteConjunctions", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
		}
		return(rtnArray);
	}


	public ArrayList<NoteComboList> GetNoteCombos(Connection con) {
		
		ArrayList<NoteComboList> rtnArray = new ArrayList<NoteComboList>();
		String                   query;
		PreparedStatement        pstmt;
		ResultSet                rs;
		NoteComboList            newCombo;
		String                   extract;
		String                   dbCombo = "";
		int                      iNdx;
		try {
			query = "SELECT Uid, WordCombos from NoteComboWords Order by Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				dbCombo = rs.getString(2);
			    newCombo = new NoteComboList();
			    newCombo.ComboUid = rs.getInt(1);
			    while(dbCombo.length() > 0) {
			    	extract = new String();
			    	iNdx = dbCombo.indexOf(" ");
			    	if(iNdx == -1) {
			    		extract = dbCombo;
			    		dbCombo = "";
			    	}
			    	else {
			    		extract = dbCombo.substring(0, iNdx).trim();
			    		dbCombo = dbCombo.substring(iNdx).trim();
			    	}
			    	newCombo.words.add(extract);
			    }
				 rtnArray.add(newCombo);
			}
			rs.close();
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetNoteCombos", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
		}
		return(rtnArray);
	}
	
	public ArrayList<DateRefRecord> GetDateRefRecords(Connection con, int templateUid) {
		ArrayList<DateRefRecord> rtnArray = new ArrayList<DateRefRecord>();
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		DateRefRecord     thisRec = null;
		
		try {
			query = "SELECT Uid, DateStr, SupText From DATEREF where TemplateUid = " + templateUid + " Order by Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				thisRec = new DateRefRecord();
			    thisRec.SetUid(rs.getInt(1));
			    thisRec.SetDateStr(rs.getString(2));
			    thisRec.SetSupText(rs.getString(3));
				rtnArray.add(thisRec);
		    }
			rs.close();
		}
		catch (SQLException e) {
    	    GenericErrorCls(con, "GetDateRefRecords", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
    	    rtnArray = null;
		}	
		return (rtnArray);
	}
	
	public void UpdateFieldsLocated(Connection con, int oldUid, int newUid) {
		String             query;
		PreparedStatement  pstmt;
		
		try {
		    query = "Update FIELDSLOCATED set DateRefUid = " + newUid + " where DateRefUid = " + oldUid;
		    pstmt = con.prepareStatement(query);
		    pstmt.execute();
		}
		catch(SQLException e) {
    	    GenericErrorCls(con, "UpdateFieldsLocated", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
		}
	}
	
	public void DeleteUnusedDateRefs(Connection con, int dateRefUid) {
		String             query;
		PreparedStatement  pstmt;
		
		try {
		    query = "Delete from  DATEREF  where Uid = " + dateRefUid;
		    pstmt = con.prepareStatement(query);
		    pstmt.execute();
		}
		catch(SQLException e) {
    	    GenericErrorCls(con, "UpdateFieldsLocated", "Exception reading table: " + e.getMessage(),
		            0, 0, 0, false);	
		}
		
	}
	
	public int WriteNoteSectionRec(Connection con, int NoteUid, String title, String Text) {
		int               iRtn = 0;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
        	query = "INSERT into Section (NoteUid, Title, SectionText) values (?,?,?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, NoteUid);
            pstmt.setString(2, title);
            pstmt.setString(3, Text);
            pstmt.executeUpdate();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteNoteSectionRec", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
		return(iRtn);
	}
	
	public int WriteSentence(Connection con, String sentence) {
		int   iRtn = 0;
		String  query;
		String  escSupStr = "";		
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			escSupStr = sentence.replace("'", "''");
        	query = "INSERT into FactSentences (Sentence) values (?)";
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, escSupStr);
            pstmt.executeUpdate();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteSentence", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
		return(iRtn);
		
	}
	
	public void UpdateNoteSectionText(Connection con, int SectionUid, String Text) {
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		String            prevText = "";
	/***************  GETS WAY TOO LARGE	
		try {
			query = "Select SectionText from SECTION where uid = " + SectionUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) 
				prevText = rs.getString(1);
			if(prevText.length() > 0)
			    prevText += " ";
			//System.out.println("LenPrev =" + prevText.length() + ":: Adding = " + Text.length());
			prevText += Text;
			prevText = prevText.replace("'", "''");
        	//query = "UPDATE SECTION Set SectionText = \"" + prevText + "\" WHERE Uid = " + SectionUid;
        	query = "UPDATE SECTION Set SectionText = ? WHERE Uid = " + SectionUid;
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, prevText);
            pstmt.executeUpdate();

		}
		catch (SQLException e) {
			GenericErrorCls(con, "UpdateNoteSectionText", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
		return;
		*******************/
	}
	
	public int WriteSectionFact(Connection con, int SectionUid, String fact, int factSentenceUid) {
		int   iRtn = 0;
		String query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
        	query = "INSERT into SectionFact (SectionUid, FactSentenceUid, fact, Active) values (?,?,?, ?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, SectionUid);
            pstmt.setInt(2, factSentenceUid);
            pstmt.setString(3, fact);
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteSectionFact", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
		return(iRtn);
	}
	
	public int getHtmlLineNumber(Connection con, SubmissionInfo si, int iCurLine) {
		int iRtn = 0;	
		String query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
        	query = "Select FileLine from HtmlFileXref where htmlLine = " + iCurLine +
        			 " AND SubUid = " + si.getUid() +  " AND ItemVersion = " + si.getVersion();
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            if(rs.next())
                iRtn = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "getHtmlLineNumber", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
		return(iRtn);
	}
	public void WriteFactAssociation(Connection con, String fact, ArrayList<Integer> factList) {
		
		String query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			for(Integer i: factList) {
        	    query = "INSERT into FactAssociation (FactUid, AssociatedText) values (?, ?)";
                pstmt = con.prepareStatement(query);
                pstmt.setInt(1, i);
                pstmt.setString(2, fact);
                pstmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteSectionFact", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
	}
	
	public void AddSentenceToTable(Connection con, int noteTableUid, String sentence) {
		String            query;
		PreparedStatement pstmt;
		
		try {
			if(sentence.length() > 0) {
        	    //query = "UPDATE NoteTables Set Sentence = ? WHERE Uid = " + noteTableUid;
        	    query = "UPDATE NoteTableDetails Set Sentence = ? WHERE NoteTableUid = " + noteTableUid;
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, sentence);
                pstmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			GenericErrorCls(con, "AddSentenceToTable", "Exception updating Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
	
	}
	
	public boolean DoZeroDateRefsExist(Connection con, SubmissionInfo subInfo) {
		boolean           bRtn = false;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		int               iCount;
		String            prevText = "";
		
		try {
			query = "Select count(*) from FieldsLocated where SubUid = " + subInfo.getUid() +
					 " AND ItemVersion = " + subInfo.getVersion() + " AND DateRefUid = 0" +
					 " AND TemplateId > 0";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				iCount = rs.getInt(1);
				if(iCount > 0)
					bRtn = true;
			}
			rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "UpdateNoteSectionText", "Exception writing Table: " + e.getMessage(),
					        0,0,0, false);
			
		}
		
		return(bRtn);
	}
	
	public ArrayList<HtmlReplacement> GetEventReplacementStr(Connection con) {
		ArrayList<HtmlReplacement> rtnArray = new ArrayList<HtmlReplacement>();
		String                     query;
		PreparedStatement          pstmt;
		ResultSet                  rs;
		HtmlReplacement            curRec = null;
		
		try {
			query = "SELECT ht.Tag, xes.SubstituteStr FROM HTML_TAGS ht, XlateEventStr xes where" +
		            " ht.Operation = 3 and ht.XlateNdx = xes.UID";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()){
				curRec = new HtmlReplacement();
				curRec.SetRec(rs.getString(1), rs.getString(2));
				rtnArray.add(curRec);
			}
			rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "GetEventReplacementStr", "Exception reading Tables: " + e.getMessage(),
					        0,0,0, false);
			
		}
		
		return(rtnArray);
	}
	
	public ArrayList<DefedFacts> GetFactDefdWords(Connection con) {
		ArrayList<DefedFacts> rtnArray = new ArrayList<DefedFacts>();
		String                     query;
		PreparedStatement          pstmt;
		ResultSet                  rs;
		DefedFacts                 cur = null;
		
		try {
			query = "SELECT DefdWord, Contain, Substitution, Append from FactsDefined order by Uid";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()){
				cur = new DefedFacts();
				cur.SetFact(rs.getString(1));
				cur.SetContain(rs.getBoolean(2));
				cur.SetSubstitution(rs.getString(3));
				cur.SetAppendable(rs.getInt(4));
				rtnArray.add(cur);
			}
			rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "GetFactDefdWords", "Exception reading Tables: " + e.getMessage(),
					        0,0,0, false);
			
		}
		
		return(rtnArray);
	}
	
	public void MarkFactStatus(Connection con, int iFactUid) {
		String query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "SELECT AssociatedText FROM FactAssociation where FactUid = " + iFactUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()){
				if(rs.getString(1).contains("month")) {
				    query = "UPDATE SectionFact set Active = 0 where UID = " + iFactUid;
				    pstmt = con.prepareStatement(query);
				    pstmt.executeUpdate();
				}
			}
			rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "MarkFactStatus", "Exception reading Tables: " + e.getMessage(),
					        0,0,0, false);
			
		}
		
	}
	
	public String GetFactSentence(Connection con, int factSentenceUid) {
		
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		String            rtnStr = "";
		
		try {
			query = "SELECT Sentence FROM FactSentences where Uid = " + factSentenceUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next()){
				rtnStr = rs.getString(1);
			}
			rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "GetFactSentence", "Exception reading Tables: " + e.getMessage(),
					        0,0,0, false);			
		}
		return(rtnStr);
	}
	
	public void DeleteSectionFact(Connection con, int iSectionFactUid) {
		String            query;
		PreparedStatement pstmt;
		
		try {
			query = "DELETE from SectionFact where UId = " + iSectionFactUid ;
			pstmt = con.prepareStatement(query);
			pstmt.execute();
		}
		catch (SQLException e) {
	    	GenericErrorCls(con, "DeleteSectionFact", "Exception Deleting From table: " + e.getMessage(),
	    			iSectionFactUid, 0, 0, false);
		}
		
	}
}
