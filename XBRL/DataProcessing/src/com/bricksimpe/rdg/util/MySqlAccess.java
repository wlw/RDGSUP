package com.bricksimpe.rdg.util;


import java.sql.*;
import java.util.Date;
import java.sql.Timestamp;

//import java.util.ArrayList;
//import com.microsoft.sqlserver.*;
import java.util.ArrayList;

import com.bricksimpe.rdg.util.ConfidenceLevel;
import com.bricksimpe.rdg.util.MatchStr;

public class MySqlAccess {
	
	private void SqlError(Connection con) { // static
	}
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
 	 	      //System.out.println("EdgarConnectStr: " + connectionString);
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

	public void CloseConnection(Connection con) // static
	{
		try {
		   con.close();
		}
		catch (SQLException e) {
			System.err.print("Exception closing MySql Connection: " + e.getMessage());
		}
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
    	System.out.println("CALLING WriteAppError");
    	WriteAppError(con, errorCls);
	}
	
	public void GetHtmlTags(Connection con, EventState eventState) {
		HtmlTag newTag = new HtmlTag();
	    String query = "Select * from html_tags";
	    
	    try {
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while(rs.next()) {
	        	newTag.tag = rs.getString(1);
	        	newTag.endTag = rs.getString(2);
	        	newTag.termTag = rs.getString(3);
	        	newTag.operation = rs.getInt(4);
	        	newTag.endOperation = rs.getInt(5);
	        	newTag.termOperation = rs.getInt(6);
	        	newTag.xlationIndex = rs.getInt(7);
	        	eventState.insertEventStr(newTag);
	        }
	        rs.close();
	        stmt.close();
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading html_tags " + e.getMessage());
			GenericErrorCls(con, "GetHtmlTags", "Exception reading html_tags " + e.getMessage(), false);
	    }
	}
	
	public void GetSubstitutionStrs(Connection con, EventState eventState) {
	    String query = "Select SubstituteStr from xlateeventstr order by Uid Asc";
	    String recStr = "";
	    	
	    try {
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while(rs.next()) {
	        	recStr = rs.getString(1);
	        	eventState.insertSubstitutionStr(recStr);
	        }
	        
	        stmt.close();
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading xlateeventstr " + e.getMessage());
			GenericErrorCls(con, "GetSubstitutionStrs", "Exception reading xlateeventstr " + e.getMessage(), false);
	    }
		
	}
	
	public MatchStr[] GetListFromTbls(Connection con, int SourceType) {
		MatchStr[]        newList = null;
		ConfidenceLevel   cl = new ConfidenceLevel();
	    PreparedStatement pstmt = null;
	    ResultSet         rs = null;
	    String            query;
	    int               iCount;
	    
	    try {
	    	query = "Select count(*) from TAGGEDFIELD where TemplateId = -2" +
	    	        " AND IsCompanyTag = 1";
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	if(rs.next()) {
	            iCount = rs.getInt(1);
	            rs.close();
	            newList = new MatchStr[iCount];
	            for(int j = 0; j < iCount; j++) {
	            	newList[j] = new MatchStr();
	            }
		    	query = "Select tag, Threshold, PathToData from TAGGEDFIELD where TemplateId = -2" +
    	        " AND IsCompanyTag = 1";
		    	pstmt = con.prepareStatement(query);
		    	rs = pstmt.executeQuery();
	            iCount = 0;
	            while(rs.next()) {
	        	    newList[iCount].OrigString = rs.getString(1);
	        	    newList[iCount].Threshold = rs.getFloat(2);	
	        	    newList[iCount].PathToData = rs.getString(3);
	        	    newList[iCount].al = cl.RecurringSting(rs.getString(1));
	        	    iCount++;
	            }
	            rs.close();
	    	}
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading html_tags " + e.getMessage());
			GenericErrorCls(con, "GetListFromTbls", "Exception reading taggedField table:  " + e.getMessage(), true);
	    }
	    return(newList);
		
	}
	
	public SubmissionInfo UpdateSubmissionsTable(Connection con, String DisplayName, String SrcFileName, String ExtractFileName,
			                           int iCompanyId) {
		PreparedStatement ps = null;
		String            query;
		ResultSet         rs;
		//String            query = "Select * from Submissions where SrcFile = '" + SrcFileName + "'";
        SubmissionInfo    subInfo = new SubmissionInfo();
        
		try {
			//Statement stmt = con.createStatement();
			//ResultSet rs = stmt.executeQuery(query);
			// check if record already exists.
			//if( rs.next() == false) {
				//rs.close();
				ps = con.prepareStatement("Insert into Submissions (Companyid, DisplayName, SrcFile, ExtractFile, Version) " +
		        "values (?, ?, ?, ?, '1')");
				ps.setInt(1, iCompanyId);
				ps.setString(2, DisplayName);
				ps.setString(3, SrcFileName);
				ps.setString(4, ExtractFileName);
			    ps.executeUpdate();
			    query = "SELECT @@IDENTITY";
	            ps = con.prepareStatement(query);
	            rs = ps.executeQuery();
	            rs.next();
	            subInfo.setSubUid(rs.getInt(1));
	            subInfo.setItemVersion(1);
		}
		catch (SQLException e) {
			//System.err.print("Exception on reading Submissions table: " + e.getMessage());
			GenericErrorCls(con, "UpdateSubmissions", "Exception reading submissions " + e.getMessage(), true);
		}
		return(subInfo);
	}
	
	public SubmissionInfo OverlaySubmission(Connection con, int iPrevUid, String InputFile, String OutputFile) {
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		SubmissionInfo    subInfo = new SubmissionInfo();
		int               iVersion;
		
		try {
			query = "Select * from Submissions where Uid = " + iPrevUid;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			if(rs.next() == false) {
				GenericErrorCls(con, "OverlaySubmission", "Could not locate UID: " + iPrevUid, false);
				subInfo = null;
			}
			else {
				iVersion = rs.getInt(7);
			    query = "INSERT into SubmissionHistory (SubUid, SrcFile, ExtractFile, Version, Date, FiscalPeriod, PublicFloat, " +
			            "PublicFloatDate, FiscalYearFocus, FiscalYearEnd, StockOutstandingDate, UsGaapTaxonomyUid, " +
			            "IsAmendment, DeiTaxonomyUid, TaggingType, InvestTaxonomyUid, CompanyId, " +
			            "FormId, State, DisplayName, CustomTaxonomyUid, FilingDate, UpdateXMLFilePath) " +
			            "VALUES  (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			    pstmt = con.prepareStatement(query);
			    pstmt.setInt(1, iPrevUid);
			    pstmt.setString(2, rs.getString(5));
			    pstmt.setString(3, rs.getString(6));
			    pstmt.setInt(4, iVersion);
			    pstmt.setDate(5, rs.getDate(8));
			    pstmt.setString(6, ResolveNullString(rs.getString(11)));
			    pstmt.setString(7, ResolveNullString(rs.getString(14)));
			    pstmt.setString(8, ResolveNullString(rs.getString(16)));
			    pstmt.setString(9, ResolveNullString(rs.getString(17)));
			    pstmt.setString(10, ResolveNullString(rs.getString(18)));
			    pstmt.setString(11, ResolveNullString(rs.getString(19)));
			    pstmt.setInt(12, rs.getInt(24));
			    pstmt.setBoolean(13, rs.getBoolean(15));
			    pstmt.setInt(14, rs.getInt(25));
			    pstmt.setInt(15, rs.getInt(22));
			    pstmt.setInt(16, rs.getInt(26));
			    pstmt.setInt(17, rs.getInt(2));
			    pstmt.setInt(18, rs.getInt(3));
			    pstmt.setInt(19, rs.getInt(10));
			    pstmt.setString(20, rs.getString(4));
			    pstmt.setInt(21, rs.getInt(21));
			    pstmt.setString(22, rs.getString(13));
			    pstmt.setString(23, rs.getString(27));
			    Date DocumentPeriodEnd = rs.getDate(12);
			    rs.close();
			    pstmt.executeUpdate();
			    query = "SELECT @@IDENTITY";
			    pstmt = con.prepareStatement(query);
	            rs = pstmt.executeQuery();
	            rs.next();
	            int iSubHistoryUid = rs.getInt(1);
			    if(DocumentPeriodEnd != null) {
			    	query = "Update SubmissionHistory set  DocumentPeriodEnd = '" + DocumentPeriodEnd + "' where Uid = " + iSubHistoryUid;
			        pstmt = con.prepareStatement(query);
			        pstmt.execute();
			    }
				iVersion++;
			    query = "UPDATE SUBMISSIONS set SrcFile = '" + InputFile + "', ExtractFile = '" + OutputFile +
			            "', Version = " + iVersion + " where UID = " + iPrevUid;
			    pstmt = con.prepareStatement(query);
			    pstmt.execute();
			    subInfo.setSubUid(iPrevUid);
			    subInfo.setItemVersion(iVersion);
			}
			
		}
		catch(SQLException e) {
			GenericErrorCls(con, "OverlaySubmission", "Exception processing overlay: " + e.getMessage(), false);
			return(null);
		}
		return(subInfo);
	}
	
	private String ResolveNullString(String thisObject) {
		String rtnObj = "";
		
		if(thisObject != null)
			rtnObj = thisObject;
	    return(rtnObj);
	}
	
	
	public void WriteHmtlFileXref(Connection con, SubmissionInfo subInfo, ArrayList<HtmlFileXref> items) {
		
		String            insertStr;
		PreparedStatement pstmt;
		
		try {
		    for(HtmlFileXref hfx : items) {
			    insertStr = "INSERT into HtmlFileXref (SubUid, ItemVersion, HtmlLine, FileLine, HtmlTblLine) " +
			                "values (?,?,?,?,?)";
			    pstmt = con.prepareStatement(insertStr);
			    pstmt.setInt(1, subInfo.getSubUid());
			    pstmt.setInt(2, subInfo.getItemVersion());
			    pstmt.setInt(3, hfx.getHtmlLine());
			    pstmt.setInt(4,hfx.getFileLine());
			    pstmt.setInt(5, hfx.getHtmlTblLine());
			    pstmt.execute();
		    }
		}
		catch (SQLException e) {
			//System.err.print("Exception on writing HtmlFileXref table: " + e.getMessage());
			GenericErrorCls(con, "WriteHmtlFileXref", "Exception writing HtmlFileXref " + e.getMessage(), true);
		 }
	}

	public ArrayList<Replacements> GetReplacements(Connection con) {
		ArrayList<Replacements> rtnArray = new ArrayList<Replacements>();
		String                  query = "Select * from Replacements order by UID";
		PreparedStatement       pstmt;
		ResultSet               rs;
        Replacements             newRec;
        
		try {
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				newRec = new Replacements();
				newRec.SetOrigStr(rs.getString(2));
				newRec.SetReplacementStr(rs.getString(3));
				rtnArray.add(newRec);
			}
			rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "GetReplacements", "Exception reading Replacement table:"  + e.getMessage(), true);
		}

		return(rtnArray);
	}
	

	public int WriteAppError(Connection con, ErrorCls errorCls) {  // static
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
			pstmt.setString(4,"EdgarExtraction");
			pstmt.setString(5,errorCls.getFunctionStr());
			pstmt.setInt(6,iRtn);
			pstmt.setString(7,ErrorText);
			pstmt.execute();
			if(errorCls.isBExit() == true)
			    iRtn = -1;
		}
		catch (SQLException e) {
			System.out.println("SQL ERROR: " + e.getMessage());
			e.printStackTrace();
			iRtn = -1;
		}
		
		return(iRtn);
	}

	private int JiffyServer(Connection con) { // static
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
			iRtn = -1;
		}
		
		
		return (iRtn);
	}

}
