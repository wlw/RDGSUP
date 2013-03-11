package com.bricksimple.rdg.sqlaccess;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.text.*;
import java.util.GregorianCalendar;


import com.bricksimple.rdg.xbrlUpload.ErrorCls;
import com.bricksimple.rdg.pushxbrl.*;
import com.bricksimple.rdg.match.*;
import com.bricksimple.rdg.xbrlUpload.*;
import com.bricksimple.rdg.ExtractedClasses.PreGroup;
//import com.bricksimple.rdg.ExtractedClasses.XmlContext;
import com.bricksimple.rdg.ExtractedClasses.XsdCustomElement;
import com.bricksimple.rdg.ExtractedClasses.Period;
import com.bricksimple.rdg.ExtractedClasses.XsdExtract;
import com.bricksimple.rdg.ExtractedClasses.DimensionGroup;
//import com.bricksimple.rdg.FieldId.CONSTANTS;

public class MySqlAccess {

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
        } 
        catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            //System.exit(-1);
        }
        return(con);
    }
	
	
	public String GetCompanyName(Connection con, int companyUid) {
		String strRtn = "";
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "Select CompanyName from Company where Uid = " + companyUid;
			pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();

		    if(rs.next())
		    	strRtn = rs.getString(1);
            rs.close();
		}
		catch(Exception e) {
			GenericErrorCls(con, "GetCompanYName", "Exception reading table " + e.getMessage());			
		}
		return(strRtn);		
				
	}
	
	public boolean DoesCompanyExist(Connection con, int companyUid) {
		
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		int               iFound = 0;
		boolean           bRtn = false;
		
		try {
			query = "Select count(*) from Company where Uid = " + companyUid;
			pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();

		    if(rs.next())
		    	iFound = rs.getInt(1);
		    if(iFound != 0) 
               bRtn = true;
            rs.close();
		}
		catch(Exception e) {
			GenericErrorCls(con, "GetXbrlTemplateXrefs", "Exception reading table " + e.getMessage());			
		}
		return(bRtn);
	}
	
	public int WriteTaxomonyUid(Connection con, int companyUid) {
		int               iRtn = 0;
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "INSERT into Taxonomy (CompanyUid, Type) Values (?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, companyUid);
			pstmt.setInt(2, 4);
			pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
            rs.close();
		}
		catch(Exception e) {
			GenericErrorCls(con, "GetXbrlTemplateXrefs", "Exception reading table " + e.getMessage());			
		}
		return(iRtn);
	}
	
	public int ValidateTaxonomyExists(Connection con, int companyUid) {
	    int               iRtn = 0;
	    String            query;
	    PreparedStatement pstmt;
	    ResultSet         rs;
	    
	    try {
	    	query = "SELECT Uid from Taxonomy where CompanyUid = " + companyUid + " and Type = 4";
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	if(rs.next())
	    		iRtn = rs.getInt(1);
	    	rs.close();
	    	if(iRtn == 0)
	    		iRtn = WriteTaxomonyUid(con, companyUid);
	    }
		catch(Exception e) {
			GenericErrorCls(con, "ValidateTaxonomyExists", "Exception reading table " + e.getMessage());			
		}
		return(iRtn);
	}
	
	public DataAccessRtn FindCompanyUid(Connection con, String CompanyName) {
		DataAccessRtn RtnCls = new DataAccessRtn();
		
		try {
            String EscapedCompanyName = CompanyName.replace("'", "''");
            String query = "Select Uid from Company where CompanyName = '" + EscapedCompanyName + "'";
		    Statement stmt = con.createStatement();
		    ResultSet rs = stmt.executeQuery(query);
		    // check if record already exists.
		    if( rs.next() == false) {
			    RtnCls.SetSuccess(RtnCls.NOT_FOUND);
		    }
		    else {
		    	RtnCls.SetSuccess(RtnCls.SUCCESS);
		    	RtnCls.SetUid(rs.getInt(1));
		    }
		}
		catch(Exception e) {
			GenericErrorCls(con, "FindCompanyUid", "Exception reading table " + e.getMessage());			
		}
	return(RtnCls);
	}
	
	
	public ArrayList<XbrlTemplateXref> GetXbrlTemplateXrefs(Connection con) {
		String query = "Select * from XbrlTemplateXref";
		PreparedStatement pstmt;
		ResultSet         rs;
		XbrlTemplateXref  curRec;
		ArrayList<XbrlTemplateXref> rtnCls = new ArrayList<XbrlTemplateXref>();
		
		try {
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				curRec =  new XbrlTemplateXref();
				curRec.SetXbrlTag(rs.getString(1));
				curRec.SetTemplateId(rs.getInt(2));
				rtnCls.add(curRec);
			}
			rs.close();
		}
		catch(Exception e) {
			GenericErrorCls(con, "GetXbrlTemplateXrefs", "Exception reading table " + e.getMessage());			
		}
		return(rtnCls);
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
		    	pstmt.setString(4,"XbrlUpload");
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
	
	private void GenericErrorCls(Connection con, String OrigFunction, String errorText) {
        ErrorCls errorCls = new ErrorCls();

        errorCls.setCompanyUid(0);
        errorCls.setFunctionStr(OrigFunction);
        errorCls.setItemVersion(0);
        errorCls.setErrorText(errorText);
        WriteAppError(con, errorCls);

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
 
	
	public void UpdateElementUid(Connection con, SubmissionInfo subInfo, int elementUid, int iTemplateParseUid) {
		String            query;
		PreparedStatement pstmt;
		
		try {
			query = "UPDATE TemplateParse set ElementUid = " + elementUid + " where Uid = " + iTemplateParseUid;
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		} 
		catch (SQLException e) {
			GenericErrorCls(con, "UpdateElementUid", "Update : " + e.getMessage());
		}
	}
	
	
	public void MapDeiData(Connection con, SubmissionInfo si, ArrayList<DeiObjects> deiObjects,
			               ArrayList<DimensionGroup> contexts, String displayName,
			               ArrayList<Stock> stocks, int userCompanyUid) {
	    String  query  = "";
	    boolean bRtn = false;
	    String  key = "";
	    String  valueStr = "";
	    
		for(DeiObjects thisDei: deiObjects) {
			if((thisDei.GetFound() == true)  && (thisDei.GetDestRow().length() > 0)){
				switch (thisDei.GetDestType()) {
				
				    case 0:  // bit
				    	valueStr = ConvertToBitValue(thisDei.GetXmlData().get(0));
				    	query = "Update " + thisDei.GetDestTable() + " Set "  +
				               thisDei.GetDestRow() + " = " + valueStr + " ";
					    break;
					
				    case 1:  // string
				    	if(thisDei.GetDestRow().equals("FormId")) {
				    		int formId = GetFormId(con, thisDei.GetXmlData().get(0));
							query = "Update " + thisDei.GetDestTable() + " Set " +
						                thisDei.GetDestRow() + " = " + formId;
				    	}
				    	else {
						    query = "Update " + thisDei.GetDestTable() + " Set " +
				                    thisDei.GetDestRow() + " = '" + thisDei.GetXmlData().get(0).replace("'", "''") + "' ";
				    	}
					    break;
					    
				    case 2: // int
			    		//if(thisDei.GetDestRow().equals("Shares")) {
			    		//	WriteStockRecs(con, thisDei, si, contexts);
			    		//	bRtn = true;
			    		//	query = "";
			    		//}
			    		//else {
					        query = "Update " + thisDei.GetDestTable() + " Set " +
			                        thisDei.GetDestRow() + " = " + thisDei.GetXmlData().get(0) + " ";
			    		//}
					    break;
					    
				    case 3:  // Date
				    	query = "Update " +thisDei.GetDestTable() + " Set " +
				                 thisDei.GetDestRow() + " = '" + thisDei.GetXmlData().get(0) + "' ";
				    	break;
				    	
				    case 4:
				    	query = "";
				    	bRtn = true;
				    	break;
				}
				if(query.length() > 0) {
				    key = BuildKey(thisDei.GetDestTable(), si);
				    query += key;
				    if(((query.toLowerCase().contains("update company ")) && (userCompanyUid == 0)) || 
				    	(query.toLowerCase().contains("update company ") == false))
				        bRtn = WriteData(con, query);
				}
				thisDei.SetWrote(bRtn);
			}
		}
		if(userCompanyUid == 0) {
		    key = BuildKey("Company", si);
		    query = "Update Company set displayName = '" + si.GetCompanyName().replace("'", "''") + "' " + key;
		    bRtn = WriteData(con, query);
		}
		WriteStockRecs(con, stocks, si);
	}
	
	//private boolean WriteStockRecs(Connection con, DeiObjects thisDei, SubmissionInfo si, ArrayList<DimensionGroup> contexts) {
    private boolean WriteStockRecs(Connection con, ArrayList<Stock> stocks, SubmissionInfo si) {
		boolean           bRtn = true;
		String            query;
		PreparedStatement pstmt;
		
		try {
			for(Stock stock: stocks) {
		    	query = "Insert into Stock (SubUid, ItemVersion, TemplateId, Shares, CommonStockDimensionsUid) Values (?,?,?,?,?)";
		    	pstmt = con.prepareStatement(query);
		    	pstmt.setInt(1, si.GetSubmissionUid());
		    	pstmt.setInt(2, si.GetVersion());
		    	pstmt.setInt(3, -2);
		    	pstmt.setInt(4, Integer.parseInt(stock.GetStock()));
		    	pstmt.setInt(5, stock.GetDimUid());
		    	pstmt.execute();
		    }
		}
		catch (Exception e) {
			GenericErrorCls(con, "WriteStockRecs", "Writing: " + e.getMessage());
		}
		return(bRtn);
	}
	
	private int GetFormId(Connection con, String id) {
		int               iRtn = 0;
	    PreparedStatement pstmt = null;
	    ResultSet         rs = null;
		String            QueryStr = "";
		
		try {
		    QueryStr = "Select Id From FormIdentification where FormStr = 'Form " + id + "'";
		    pstmt = con.prepareStatement(QueryStr);
		    rs = pstmt.executeQuery();
		    if(rs.next()) {
		     iRtn = rs.getInt(1);
		    }
		    rs.close();
		}
		catch (Exception e) {
			GenericErrorCls(con, "GetFormId", "Error reading: " + e.getMessage());
		}
		return(iRtn);
	}
	
	private String ConvertToBitValue(String data) {
		String rtnStr = "0";
		
		if(data.toLowerCase().equals("yes"))
			rtnStr = "1";
		if(data.toLowerCase().equals("true"))
			rtnStr = "1";
		return(rtnStr);
	}
	

	private String BuildKey(String table, SubmissionInfo si) {
	    String rtnStr = "";
	    
	    if(table.equals("Submissions"))
	    	rtnStr = "where Uid = " + si.GetSubmissionUid();
	    if(table.equals("Company"))
	    	rtnStr = "Where Uid = " + si.GetCompanyUid();
	    if (table.equals("CIK_Ids"))
	    	rtnStr = "Where CompanyUid = " + si.GetCompanyUid();
	    if(table.equals("CompanyEntityTbl"))
	    	rtnStr = "Where CompanyId = " + si.GetCompanyUid();
	    if(table.equals("Stock"))
	    	rtnStr = "Where SubUid = " + si.GetSubmissionUid() + " AND ItemVersion = " + si.GetVersion();
	    return(rtnStr);
	}
	
	private boolean WriteData(Connection con, String query) {
		boolean           bRtn = true;
		PreparedStatement pstmt;
		
		try {
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		}
		catch (Exception e) {
			GenericErrorCls(con, "WriteData", "Error executing '" + query + "'" + e.getMessage());
			bRtn = false;
		}
		return(bRtn);
	}
	
    public ArrayList<DeiObjects> GetDeiObjects(Connection con) {
    	ArrayList<DeiObjects> rtnCls = new ArrayList<DeiObjects>();
    	
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
		String QueryStr = "";
		DeiObjects cur = null;
		
		try {
		        QueryStr = "Select * from XbrlObjects where SectionId = " + CONSTANTS.DEI_TYPE;
		        pstmt = con.prepareStatement(QueryStr);
		        rs = pstmt.executeQuery();
		        while(rs.next()) {
		        	cur = new DeiObjects();
		        	cur.SetDestTable(rs.getString(2));
		        	cur.SetDestRow(rs.getString(3));
		        	cur.SetDestType(rs.getInt(4));
		        	cur.SetXmltag(rs.getString(5));
		        	cur.SetFound(false);
		        	cur.SetAl(rs.getString(5));
		        	rtnCls.add(cur);
		        }
		        rs.close();
		        pstmt.close();
	    	}
	    catch (SQLException e) {
			GenericErrorCls(con, "GetDeiObjects", "Exception reading Dei objects " + e.getMessage());
	    }
  	return(rtnCls);
    }
    
    public String FindXmlXrefRole(Connection con, String XbrlRole) {
    	String            RtnStr = "";
    	String            query;
    	PreparedStatement pstmt;
    	ResultSet         rs;
    	
    	try {
    		if(XbrlRole.equals("documentation") == false) {
    		    query ="SELECT xmlRole from LabelRoleXref where XbrlRole = '" + XbrlRole.toLowerCase() + "'";
		        pstmt = con.prepareStatement(query);
		        rs = pstmt.executeQuery();
		        if(rs.next()) 
		    	    RtnStr = rs.getString(1);
		        else {
	    		    GenericErrorCls(con, "FindXmlXrefRole", "Unable to find Xbrl role: " + XbrlRole);	
	    		    RtnStr = XbrlRole;
		        }
    		}
    	}
    	catch (Exception e) {
    		GenericErrorCls(con, "FindXmlXrefRole", "Error reading table: " + e.getMessage());	
    	}
    	return(RtnStr);		
    }
    
    public int InsertCompanyRow(Connection con, ArrayList<DeiObjects> dei, String stockSymbol, String webAddress) {
    	// Rows: Uid, CompanyName, StateIncorporated, EID, ExchangeSymbol, WebAddress DisplayName
    	//       FilerStatus, FiscalYearEndMonth, FiscalYearEndRule, FiscalYearEndCustomDate, Deleted
    	int                iRtn = 0;
    	String             query = "";
    	PreparedStatement  pstmt;
    	int                iNumParams = 0;
    	ArrayList<String>  DeiData = new ArrayList<String>();
    	ArrayList<Integer> DeiType = new ArrayList<Integer>();
    	ResultSet          rs;
    	UTILITIES          utilities = new UTILITIES();
    	
    	try {
    	    query = "INSERT into Company (";
    	    for(DeiObjects doa: dei) {
    		    if(doa.GetDestTable().equals("Company")) {
    			    if(iNumParams > 0)
    				   query+= ", ";
    			    query += doa.GetDestRow();
    			    DeiData.add(doa.GetXmlData().get(0));
    			    DeiType.add(doa.GetDestType());
    			    iNumParams++;
    		    }
    	    }
    	    if(iNumParams > 0) {
    		    query += ", ExchangeSymbol, webAddress) Values (";
    		    for (int i = 0; i < iNumParams; i++) {
    			    if(i > 0)
    				    query += ", ";
    			    query += "?";
    		    }
    		    query += ",?,?)";
    		    pstmt = con.prepareStatement(query);
    		    int i;
    		    for(i = 0; i < DeiData.size(); i++) {
    		    	switch(DeiType.get(i)) {
    		    	    case CONSTANTS.DEI_STRING:
    		    	    	pstmt.setString(i + 1, DeiData.get(i));
    		    	        break;
    		    	        
    		    	    case CONSTANTS.DEI_BIT:
    		    	    	boolean  iConvert = ConvertBitStringToValue(DeiData.get(i));
    		    	    	pstmt.setBoolean(i+ 1, iConvert);
    		    		    break;
    		    		    
    		    	    case CONSTANTS.DEI_INTEGER:
    		    	    	pstmt.setInt(i+1, Integer.valueOf(DeiData.get(i)));
    		    		    break;
    		    		    
    		    		case CONSTANTS.DEI_DATE:
     		    			Date date = utilities.DateConverter(con, DeiData.get(i));
    		    			pstmt.setDate(i+ 1, new java.sql.Date(date.getTime()));
    		    			break;
    		    	}
    		    }
    		    pstmt.setString(i + 1, stockSymbol);
    		    pstmt.setString(i + 2, webAddress);
			    pstmt.executeUpdate();
			    query = "SELECT @@IDENTITY";
	            pstmt = con.prepareStatement(query);
	            rs = pstmt.executeQuery();
	            rs.next();
	            iRtn = rs.getInt(1);
	            rs.close();
   		
    	    }
    	}
    	catch (Exception e) {
    		GenericErrorCls(con, "InsertCompanyRow", "Error inserting into Company table: " + e.getMessage());
    	}
    	return(iRtn);
    }
    
    public void AddCoreParentheticalRef(Connection con, int concretRow, int parentheticalRow) {
  
    	PreparedStatement pstmt;
    	String            query;
    	
    	try {
	        query = "INSERT into CoreParentheticalReference (ParentRowUid, ParentheticalRowUid) values (?,?)";
	        pstmt = con.prepareStatement(query);
	        pstmt.setInt(1, concretRow);
	        pstmt.setInt(2, parentheticalRow);
	        pstmt.execute();
		}
	    catch (SQLException e) {
	    	GenericErrorCls(con, "AddCoreParentheticalRef", "Error inserting: " + e.getMessage());
	    }
    }
    
	private  String reverseIt(String a) {
		
		int length = a.length();
		StringBuilder reverse = new StringBuilder();
		for(int i = length; i > 0; --i) {
			char result = a.charAt(i-1);
			reverse.append(result);
		}
		return reverse.toString();
	}

    public int WriteSubmission(Connection con, int CompanyUid, String CompanyName, String srcFullFile,
    		                   String FormId, String srcFile) {
    	int iRtn = 0;
    	
		PreparedStatement pstmt;
		ResultSet         rs;
		int               formId = 0;
		String            query;
		String            insertStr = "INSERT into Submissions (CompanyId, FormId, DisplayName, " +
		                              "SrcFile, ExtractFile, version) values (?,?,?,?,?,?)";
 		try {
 			srcFile = reverseIt(srcFile);
 			int i = srcFile.indexOf(".");
 			srcFile = srcFile.substring(i+1);
 			srcFile = reverseIt(srcFile);
 		    query = "Select Id from FormIdentification where FormStr = 'Form " + FormId  + "'";
 			pstmt = con.prepareStatement(query);
 		    rs = pstmt.executeQuery();
 			if(rs.next()) 
 			    formId = rs.getInt(1);
 			rs.close();
 			pstmt = con.prepareStatement(insertStr);
 			pstmt.setInt(1,CompanyUid);
 			pstmt.setInt(2,formId);
 		    pstmt.setString(3,srcFile);
 		    pstmt.setString(4,srcFullFile);
 		    String Temp = srcFullFile.replace(".htm", ".txt");
 		    pstmt.setString(5,Temp);
 			pstmt.setInt(6, 1);   // VERSION
 			pstmt.execute();
 			query = "SELECT @@IDENTITY";
 			pstmt = con.prepareStatement(query);
 		    rs = pstmt.executeQuery();
 			rs.next();
 			iRtn = rs.getInt(1);
            rs.close();
 			    //if(errorCls.isBExit() == true)
			    //    System.exit(iRtn);
		}
	    catch (SQLException e) {
	    	GenericErrorCls(con, "WriteSubmission", "Error inserting into Submisssions: " + e.getMessage());
	    }
     	return(iRtn);
    }
    
    
    public int WriteEquityDateRef(Connection con, SubmissionInfo subInfo, String equityStr) {
        int               iRefIndx = 0;
        String            query;
        PreparedStatement pstmt;
        ResultSet         rs;
        
        try {
            query = "INSERT into DateRef (SubUid, ItemVersion, DateStr, StartDate, EndDate, SupText) values (?,?,?,?,?,?)";
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, subInfo.GetSubmissionUid());
            pstmt.setInt(2, subInfo.GetVersion());
            pstmt.setString(3, "");
            pstmt.setString(4, "");
            pstmt.setString(5, "");
            pstmt.setString(6, equityStr);   // this clears suptext - UI has issues
            pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
	        iRefIndx = rs.getInt(1);
        }
        catch(Exception e) {
	        GenericErrorCls(con, "WriteDateRef", "Error inserting into DateRef: " + e.getMessage());
        }
        return(iRefIndx);
    }
    
    public int WriteDateRef(Connection con, SubmissionInfo subInfo, Period period) { //, XmlContext context) {
    	int               iRefIndx = 0;
    	String            query;
    	PreparedStatement pstmt;
    	ResultSet         rs;
    	String            supText = "";
    	
    	try {
    	        if(period.GetInstant().length() > 0) {
    		        query = "INSERT into DateRef (SubUid, ItemVersion, DateStr, StartDate, SupText) values (?,?,?,?,?)";
    		        pstmt = con.prepareStatement(query);
    		        pstmt.setInt(1, subInfo.GetSubmissionUid());
    		        pstmt.setInt(2, subInfo.GetVersion());
    		        pstmt.setString(3, period.GetInstant());
    		        pstmt.setString(4, period.GetInstant());
    		        pstmt.setString(5, "");    // clears sup text as UI has issues!
    		        pstmt.execute();
    	        }
    	        else {
    	    	    supText = GetSupTextStr(con, period);
    		        query = "INSERT into DateRef (SubUid, ItemVersion, DateStr, StartDate, EndDate, SupText) values (?,?,?,?,?,?)";
    		        pstmt = con.prepareStatement(query);
    		        pstmt.setInt(1, subInfo.GetSubmissionUid());
    		        pstmt.setInt(2, subInfo.GetVersion());
    		        pstmt.setString(3, period.GetEndDate());
    		        pstmt.setString(4, period.GetStartDate());
    		        pstmt.setString(5, period.GetEndDate());
    		        pstmt.setString(6, supText);   // this clears suptext - UI has issues
    		        pstmt.execute();
    	        }
    	    /*}
    		else {
		        query = "INSERT into DateRef (SubUid, ItemVersion, DateStr, StartDate, EndDate, SupText) values (?,?,?,?,?,?)";
		        pstmt = con.prepareStatement(query);
		        pstmt.setInt(1, subInfo.GetSubmissionUid());
		        pstmt.setInt(2, subInfo.GetVersion());
		        pstmt.setString(3, "");
		        pstmt.setString(4, "");
		        pstmt.setString(5, "");
		        pstmt.setString(6, equityStr);   // this clears suptext - UI has issues
		        pstmt.execute();
    		}
    		*/
			query = "SELECT @@IDENTITY";
		        pstmt = con.prepareStatement(query);
	            rs = pstmt.executeQuery();
		        rs.next();
			    iRefIndx = rs.getInt(1);
    	}
    	catch(Exception e) {
	    	GenericErrorCls(con, "WriteDateRef", "Error inserting into DateRef: " + e.getMessage());
    	}
    	return(iRefIndx);
    }
    
    private String GetSupTextStr(Connection con, Period period) { //String startDate, String endDate) {
    	
    	
    	String rtnStr = "";
    	GregorianCalendar sDate = new GregorianCalendar();
    	GregorianCalendar eDate = new GregorianCalendar();
    	DateFormat        df = new SimpleDateFormat("yyyy-MM-dd");
    	Date              sdDate = null;
    	Date              edDate = null;
    	int               mdiff; 
    	int               ddiff;
    	 
    	try {
            sdDate = df.parse(period.GetStartDate());
        	edDate = df.parse(period.GetEndDate());
   	        sDate.setTime(sdDate);
    	    eDate.setTime(edDate);
    	    mdiff = eDate.get(GregorianCalendar.MONTH) - sDate.get(GregorianCalendar.MONTH);
    	    ddiff = eDate.get(GregorianCalendar.DAY_OF_MONTH) - sDate.get(GregorianCalendar.DAY_OF_MONTH);
    	    if(ddiff > 1)
    	    	mdiff++;
    	    switch (mdiff) {
    	        case 3:
    	    	    rtnStr = CONSTANTS.THREE_MONTHS_END;
    	    	    period.SetSupNdx(CONSTANTS.THREE_MONTHS_NDX);
    	    	    break;
    	        case 6:
    	    	    rtnStr = CONSTANTS.SIX_MONTHS_END;
       	    	    period.SetSupNdx(CONSTANTS.SIX_MONTHS_NDX);
       	    	    break;
    	        case 9:
    	    	    rtnStr = CONSTANTS.SIX_MONTHS_END;
       	    	    period.SetSupNdx(CONSTANTS.NINE_MONTHS_NDX);
       	    	    break;
    	        case 12:
    	    	    rtnStr = CONSTANTS.TWELVE_MONTHS_END;
       	    	    period.SetSupNdx(CONSTANTS.TWELVE_MONTHS_NDX);
       	    	    break;
    	    }
    	    	
    	}
    	catch(Exception e) {
	    	GenericErrorCls(con, "GetSupTextStr", "Error converting date: " +  period.GetStartDate() + 
	    			        " or " + period.GetEndDate());
    	}
    	return(rtnStr);
    }
    
    public void InsertAbstract(Connection con, String tag, int ConceptUid, SubmissionInfo subInfo, int iTemplateParseUid,
    		                   boolean IsParenthetical, int RoleUid, boolean bNegated, long hashedUid) {
    	String            query;
    	PreparedStatement pstmt;
    	int               jiffyId;
    	int               FieldStrUid;
    	ResultSet         rs;
    	int               fieldsRowStrUid;
    	
    	try {
    	    jiffyId = JiffyServer(con);
    	    query = "INSERT into Abstracts (TemplateId, SubUid, ItemVersion, FieldData, JIffyId) values(?,?,?,?,?)";
    	    pstmt = con.prepareStatement(query);
    	    pstmt.setInt(1, iTemplateParseUid);
    	    pstmt.setInt(2, subInfo.GetSubmissionUid());
    	    pstmt.setInt(3, subInfo.GetVersion());
    	    pstmt.setString(4, tag);
    	    pstmt.setInt(5, jiffyId);
    	    pstmt.execute();
    	    FieldStrUid = InsertFieldsRowStr(con, subInfo, tag, iTemplateParseUid, IsParenthetical, hashedUid);

    	    query = "INSERT into CoreAbstractRow (Uid, ElementUid) values(?,?)";
    	    pstmt = con.prepareStatement(query);
    	    pstmt.setInt(1, FieldStrUid);
    	    pstmt.setInt(2, ConceptUid);
    	    pstmt.execute();
		    InsertIntoCoreRow(con, FieldStrUid);
    	    }
    	catch (Exception e) {
	    	GenericErrorCls(con, "InsertAbstract", "Error inserting Abstracts: " + e.getMessage());
    	}
    }
  
    /*******************************************
    public int GetGaapRefConcept(Connection con, String GaapRefKey) {
    	String query;
    	int               ConceptUid  = 0;
        PreparedStatement pstmt;
        ResultSet         rs;
        
        try {
	        query = "Select TaxonomyUid from GaapRef where Concept = '" + GaapRefKey + "'";
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        if(rs.next())
	    	    ConceptUid = rs.getInt(1);
	        rs.close();
        }
        catch (Exception e) {
	    	GenericErrorCls(con, "GetGaapRefConcept", "Error inserting Abstracts: " + e.getMessage());
        }
	    return(ConceptUid);
    }
    ******************************/
    
    public int GetTaxonomyUid(Connection con, String key, int companyUid, boolean bType) {
    	int               iRtn = 0;
    	String            query;
    	PreparedStatement pstmt;
    	ResultSet         rs;
    	
    	try {
    		if(bType)
    			query = "Select UID from Taxonomy where ElementsNameSpace = '";
    		else
    			query = "Select UID from Taxonomy where ElementsPath = '";
    		query += key + "'";
    		pstmt = con.prepareStatement(query);
    		rs = pstmt.executeQuery();
    		if(rs.next())
    			iRtn = rs.getInt(1);
    		else {
    			rs.close();
   			    query = "Select Uid from Taxonomy where CompanyUid = " + companyUid;
    			pstmt = con.prepareStatement(query);
    			rs = pstmt.executeQuery();
    			if(rs.next())
    				iRtn = rs.getInt(1);
    		}
    		rs.close();
   	    }
        catch (Exception e) {
	    	GenericErrorCls(con, "GetTaxonomuUid", "Error reading taxonomy: " + e.getMessage());
        }
    	return(iRtn);
    }
    
    public int GetGaapRefNdx(Connection con, String concept, int taxonomyUid, XsdExtract xsdExtract) {
       	int               iRtn = 0;
    	String            query;
    	PreparedStatement pstmt;
    	ResultSet         rs;
    	
    	try {
     	    query = "Select UID from GaapRef where Concept = '" + concept + "' AND TaxonomyUid =" + taxonomyUid;
    		pstmt = con.prepareStatement(query);
    		rs = pstmt.executeQuery();
    		if(rs.next())
    			iRtn = rs.getInt(1);
    		rs.close();
    		if(iRtn == 0) 
    			iRtn = AddCustomConcept(con, concept, taxonomyUid, xsdExtract);
   	    }
        catch (Exception e) {
	    	GenericErrorCls(con, "GetGaapRefNdx", "Error reading GaapRef: " + e.getMessage());
        }
    	return(iRtn);
    	
    }
    
    public int AddCustomConcept(Connection con, String concept, int taxonomyUid,  XsdExtract xsdExtract) {
    	int               iRtn = 0;
    	String            insertStr;
    	PreparedStatement pstmt;
    	ResultSet         rs = null;
    	String            stdName;
    	XsdCustomElement  xce;
    	String            query;
    	int               gaapRefUid = 0;
    	//String            escaped = "";
    	
    	xce = FindMyXsdRec(concept, xsdExtract);
    	if(xce == null) {
    	    GenericErrorCls(con, "AddCustomConcept", "Finding CONCEPT: " + concept);
    	}
    	else {
    	    try {
       		    insertStr = "INSERT INTO GAAPREF (TaxonomyUid, Concept, DisplayName, DataType, PeriodType, BalanceType, SubstitutionType, " +
	                        "Abstract, Nillable, Parent, Deprecated, StandardLabel) values (?,?,?,?,?,?,?,?,?,?,?,?)";
   		        stdName = Wordize(xce.GetName());
		    	pstmt = con.prepareStatement(insertStr);
		    	pstmt.setInt(1, taxonomyUid);
		    	pstmt.setString(2, concept);
		        pstmt.setString(3, xce.GetName());
		        pstmt.setString(4, xce.GetType());
		    	pstmt.setString(5, xce.GetPeriod());
		    	pstmt.setString(6, xce.GetBalance());
		    	pstmt.setString(7, xce.GetSubstitutionGroup());
		    	pstmt.setBoolean(8, xce.GetAbstract());
		    	pstmt.setBoolean(9, xce.GetNillable());
		    	pstmt.setString(10, "");
		    	pstmt.setBoolean(11, false);
		    	pstmt.setString(12, stdName);
		    	pstmt.execute();
				query = "SELECT @@IDENTITY";
			    pstmt = con.prepareStatement(query);
		        rs = pstmt.executeQuery();
			    rs.next();
		    	gaapRefUid = rs.getInt(1);
		    	rs.close();
		    	//escaped = xce.GetDocumentation().replace("'", "''");
		    	insertStr = "INSERT into GaapDocumentationRef (GaapRefUid, Documentation) values (?,?)";
		    	pstmt = con.prepareStatement(insertStr);
		    	pstmt.setInt(1, gaapRefUid);
		    	pstmt.setString(2, xce.GetDocumentation());
		    	pstmt.execute();
		    	iRtn = gaapRefUid;
    	    }
            catch (Exception e) {
	    	    GenericErrorCls(con, "AddCustomConcept", "Error creating concept: " + e.getMessage());
            }
    	}
   	return(iRtn);
    	
    }
    
    private XsdCustomElement FindMyXsdRec(String concept, XsdExtract xsdExtract) {
    	
    	int              i = 0;
    	boolean          bFound = false;
    	XsdCustomElement xsdCustomElement = null;
    	String           testStr = concept.replace(":", "_");
    	
    	while((bFound == false) && (i < xsdExtract.GetXsdCustomElements().size())) {
    		if(xsdExtract.GetXsdCustomElements().get(i).GetId().equals(testStr)) {
    			bFound = true;
    			xsdCustomElement = xsdExtract.GetXsdCustomElements().get(i);
    	    }
    		i++;
    	}
    	return(xsdCustomElement);
    }
    
    public FieldRowInsertCls InsertColumnData(Connection con, String RowLabel, int fieldsRowStrUid, String columnData, SubmissionInfo subInfo, 
    		                    int iTemplateParseUid, int refUid, boolean IsParenthetical, int gaapRefId, long hashedUid, boolean bNil) {
    	
    	String            query;
    	PreparedStatement pstmt;
    	int               jiffyId;
    	ResultSet         rs;
     	FieldRowInsertCls fieldRowInsertCls = new FieldRowInsertCls();
    	String            dataStr =  columnData;
    	UTILITIES         utils  = new UTILITIES();
    	
    	if((columnData.equals("-")) && (bNil == true))
    		dataStr = CONSTANTS.NIL;
    	else 
    		dataStr = utils.DoSubstitutions(columnData);
    	fieldRowInsertCls.fieldsRowStrUid = fieldsRowStrUid;
    	if(columnData.contains("<font"))
    		return(fieldRowInsertCls);
    	if(fieldRowInsertCls.fieldsRowStrUid == 0) {
    	    fieldRowInsertCls.fieldsRowStrUid  = InsertFieldsRowStr(con, subInfo, RowLabel, iTemplateParseUid, IsParenthetical, hashedUid);
		    InsertIntoCoreRow(con, fieldRowInsertCls.fieldsRowStrUid);
   	    }
    	jiffyId = JiffyServer(con);
    	try {
    	    query = "INSERT into FieldsLocated (CompanyUid, SubUid, ItemVersion, TemplateId, DateRefUid, DataStr, JiffyId, FieldsRowUid) values(?,?,?,?,?,?,?,?)";
    	    pstmt = con.prepareStatement(query);
    	    pstmt.setInt(1, subInfo.GetCompanyUid());
    	    pstmt.setInt(2, subInfo.GetSubmissionUid());
    	    pstmt.setInt(3, subInfo.GetVersion());
    	    pstmt.setInt(4, iTemplateParseUid);
    	    pstmt.setInt(5, refUid);
    	    pstmt.setString(6, dataStr);
    	    pstmt.setInt(7, jiffyId);
    	    pstmt.setInt(8, fieldRowInsertCls.fieldsRowStrUid);
    	    pstmt.execute();
			query = "SELECT @@IDENTITY";
		    pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
		    rs.next();
		    fieldRowInsertCls.fieldsLocatedUid = rs.getInt(1);
		    rs.close();
	    	//InsertFieldIntoCoreCell(con, Uid, gaapRefId);
  	    }
    	catch (Exception e) {
	    	GenericErrorCls(con, "InsertColumnData", "Error inserting FieldsLocated: " + e.getMessage());
    	}
  	    return(fieldRowInsertCls);
    }
    
    private void InsertFieldIntoCoreCell(Connection con, int uid, int elementUid) {
    	
      	String            query;
    	PreparedStatement pstmt;
    	
    	try {
    	    query = "INSERT into CoreCell (Uid, ElementUid, TdColumn, DefinitionPath) values(?,?,?,?)";
    	    pstmt = con.prepareStatement(query);
    	    pstmt.setInt(1, uid);
    	    pstmt.setInt(2, elementUid);
    	    pstmt.setInt(3,0);
    	    pstmt.setInt(4,0);
            pstmt.execute();
  	    }
    	catch (Exception e) {
	    	GenericErrorCls(con, "InsertFieldIntoCoreCell", e.getMessage());
    	}
 }
    
    public int FindStockDimension(Connection con, String strDim) {
    	int               iRtn = 1;
    	String            query;
    	PreparedStatement pstmt;
    	ResultSet         rs;
    	
		try {
			query = "Select Uid from CommonStockDimension where GaapConcept = '" + strDim + "'";
	        pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
	        if(rs.next()) {
	        	iRtn = rs.getInt(1);
	        }
	        rs.close();
	        pstmt.close();
    	}
        catch (SQLException e) {
		    GenericErrorCls(con, "FindStockDimension", "Exception reading: " + e.getMessage());
        }
		return(iRtn);
    }
    
    public void WriteCoreSubmissionChild(Connection con, int parentCellUid, int childCellUid, Debug debug) {
    	
      	String            query;
    	PreparedStatement pstmt;
    	
    	try {
    		if(childCellUid != 0) {
    	        query = "INSERT into CoreSummationChildren (ParentCellUid, ChildCellUid) values(?,?)";
    	        pstmt = con.prepareStatement(query);
    	        pstmt.setInt(1, parentCellUid);
    	        pstmt.setInt(2, childCellUid);
    	        pstmt.execute();
    	        if(debug.IsEnabled() == true)
    	    	    GenericErrorCls(con, "WriteCoreSubmissionChild", "Parent: " + parentCellUid + " <> Child: " + childCellUid);
    		}
  	    }
    	catch (Exception e) {
	    	GenericErrorCls(con, "WriteCoreSubmissionChild", "Error inserting Abstracts: " + e.getMessage());
    	}
    }
    
    public int InsertFieldsRowStr(Connection con, SubmissionInfo subInfo, String data, int TemplateId, boolean bIsParenthetical,
    		                      long hashedUid) {
       	String            query;
    	PreparedStatement pstmt = null;
    	int               Uid = 0;
    	ResultSet         rs;
    	
    	try {
  		    if(bIsParenthetical == false) {
  	   	        query = "INSERT into FieldsRowStr (SubUid, ItemVersion, RowData, TemplateUid, TaggedFieldUid) values(?,?,?,?,?)";
  		        pstmt = con.prepareStatement(query);
    	        pstmt.setInt(1, subInfo.GetSubmissionUid());
    	        pstmt.setInt(2, subInfo.GetVersion());
    	        pstmt.setString(3, data);
    	        pstmt.setInt(4, TemplateId);
    	        pstmt.setLong(5, hashedUid);
    	    }
    		else {
       	        query = "INSERT into FieldsRowStr (RowData, TemplateUid) values(?,?)";
    	        pstmt = con.prepareStatement(query);
    	        pstmt.setString(1, data);
    	        pstmt.setInt(2, 0);
     	        /*
       	        query = "INSERT into FieldsRowStr (SubUid, ItemVersion, RowData, TemplateUid, TaggedFieldUid) values(?,?,?,?,?)";
    	        pstmt = con.prepareStatement(query);
     	        pstmt.setInt(1, subInfo.GetSubmissionUid());
    	        pstmt.setInt(2, subInfo.GetVersion());
    	        pstmt.setString(3, data);
    	        pstmt.setInt(4, 0);
    	        pstmt.setInt(5, taggedFieldUid);
    	        */
  		}
    	    pstmt.execute();
			query = "SELECT @@IDENTITY";
		    pstmt = con.prepareStatement(query);
	        rs = pstmt.executeQuery();
		    rs.next();
		    Uid = rs.getInt(1);
	        rs.close();
		    //InsertIntoCoreRow(con, Uid);
 	    }
    	catch (Exception e) {
	    	GenericErrorCls(con, "InsertFieldsRowStr", e.getMessage());
    	}
   	    return(Uid);
    }
        
    private void InsertIntoCoreRow(Connection con, int uid) {
    	String            query;
    	PreparedStatement pstmt;
    	
    	try {
    	    query = "INSERT into CoreRow (Uid, ReferencedRowUid) values(?,?)";
    	    pstmt = con.prepareStatement(query);
    	    pstmt.setInt(1, uid);
    	    pstmt.setInt(2, 0);
    	    pstmt.execute();
   	}
    	catch (SQLException e) {
	    	GenericErrorCls(con, "InsertIntoCoreRow",  "ERROR: Uid = " + uid); //"Error inserting CoreRow: " + e.getMessage());
    	}
    }
    
    
    public void InsertCikRecord(Connection con, ArrayList<DeiObjects> dei, int CompanyUid) {
    	// rows: Uid, CompanyUid, EIN, CIK   UId is Identity
       	int                iRtn = 0;
    	String             query = "";
    	PreparedStatement  pstmt;
    	int                iNumParams = 0;
    	ArrayList<String>  DeiData = new ArrayList<String>();
    	
    	try {
    	    query = "INSERT into Cik_ids (";
    	    for(DeiObjects doa: dei) {
    		    if(doa.GetDestTable().equals("CIK_Ids")) {
    			    if(iNumParams > 0)
    				   query+= ", ";
    			    query += doa.GetDestRow();
    			    DeiData.add(doa.GetXmlData().get(0));
    			    iNumParams++;
    		    }
    	    }
    	    if(iNumParams > 0) {
    		    query += ", CompanyUid) Values (";
    		    for (int i = 0; i < iNumParams; i++) {
    			    if(i > 0)
    				    query += ", ";
    			    query += "?";
    		    }
    		    query += ", ?)";  // we manually add company uid
    		    pstmt = con.prepareStatement(query);
    		    int i;
    		    for(i = 0; i < DeiData.size(); i++) {
     		        pstmt.setString(i + 1, DeiData.get(i));
     		    }
    		    pstmt.setInt(i + 1, CompanyUid);
			    pstmt.executeUpdate();
    	    }
    	}
    	catch (Exception e) {
    		GenericErrorCls(con, "InsertCikRecord", "Error inserting into Company table: " + e.getMessage());
    	}
  }

    private boolean ConvertBitStringToValue(String sBool) {
    	boolean bRtn = false;
    	String sLowered = sBool.toLowerCase();
    	
    	if(sLowered.equals("yes"))
    		bRtn = true;
    	else {
    		if(sLowered.equals("true"))
    			bRtn = true;
    	}
    	return(bRtn);	
    }
 
    /*****************
    public void WriteCustomElements(Connection con, int TaxonomyUid, ArrayList<XsdCustomElement> wrkArray, XsdExtract xsdExtract) {
    	String            query;
    	PreparedStatement pstmt;
    	String            insertStr;
    	int               iFound;
    	ResultSet         rs;
    	String            stdName = "";
    	
    	try {
    		insertStr = "INSERT INTO GAAPREF (TaxonomyUid, Concept, DisplayName, DataType, PeriodType, BalanceType, SubstitutionType, " +
    	                "Abstract, Nillable, Parent, Deprecated, StandardLabel) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    	    for(XsdCustomElement xce: wrkArray) {
    		    query = "Select count(*) from GAAPREF where Concept = '" + xce.GetId() + "'";
    		    pstmt = con.prepareStatement(query);
    		    iFound = 0;
    		    rs = pstmt.executeQuery();
    		    if(rs.next())
    		    	iFound = rs.getInt(1);
    		    if(iFound == 0) {
        		    rs.close();
        		    stdName = Wordize(xce.GetName());
  		    	    pstmt = con.prepareStatement(insertStr);
  		    	    pstmt.setInt(1, TaxonomyUid);
  		    	    pstmt.setString(2, xce.GetId());
  		    	    pstmt.setString(3, xce.GetName());
  		    	    pstmt.setString(4, xce.GetType());
  		    	    pstmt.setString(5, xce.GetPeriod());
  		    	    pstmt.setString(6, xce.GetBalance());
  		    	    pstmt.setString(7, xce.GetSubstitutionGroup());
  		    	    pstmt.setBoolean(8, xce.GetAbstract());
  		    	    pstmt.setBoolean(9, xce.GetNillable());
  		    	    pstmt.setString(10, "");
  		    	    pstmt.setBoolean(11, false);
  		    	    pstmt.setString(12, stdName);
  		    	    pstmt.execute();
   		    }
    		    rs.close();
   	    }
    	}
    	catch (Exception e) {
			GenericErrorCls(con, "WriteCustomElements", "Exception reading/writing gaapref objects " + e.getMessage());
    	}
    	
    }
    ****************/
    
    private String Wordize(String origStr) {
    	String rtnStr = "";
    	int    i = 1;
    	char   c;
    	
    	rtnStr = origStr.substring(0,1);
    	rtnStr = rtnStr.toUpperCase();
    	while(i < origStr.length()) {
    		c = origStr.charAt(i);
    		if(Character.isUpperCase(c))
    			rtnStr += " ";
    		rtnStr += c;
    		i++;
    	}
    	return(rtnStr);
    }
    
     public ArrayList<ElementPathXref> GetElementXref(Connection con) {
    	ArrayList<ElementPathXref> rtnArray = new ArrayList<ElementPathXref>();
    	
	    PreparedStatement pstmt = null;
	    ResultSet         rs = null;
		String            QueryStr = "";
		ElementPathXref   cur = null;
		
		try {
		        QueryStr = "Select ElementsPath, ElementsNameSpace from taxonomy where ElementsPath IS NOT NULL";
		        pstmt = con.prepareStatement(QueryStr);
		        rs = pstmt.executeQuery();
		        while(rs.next()) {
		        	cur = new ElementPathXref();
		        	cur.SetElementPath(rs.getString(1));
		        	cur.SetElementsNameSpace(rs.getString(2));
		        	rtnArray.add(cur);
		        }
		        rs.close();
		        pstmt.close();
	    	}
	    catch (SQLException e) {
			GenericErrorCls(con, "GetElementXref", "Exception reading taxonomy objects " + e.getMessage());
	    }
   	return(rtnArray);
    	
    }
    
	public FieldMatchStr[] GetFieldIdentifiers(Connection con, int iSrcType, int iCompanyId) 
	{
        FieldMatchStr[] newList = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int iCount;
        String QueryStr = "";
        UTILITIES utils = new UTILITIES();
        
        try {
            pstmt = con.prepareStatement("Select count(*) from taggedfield where  SourceType = " + iSrcType +
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
                QueryStr = "Select UID, Tag, Threshold, PathToData, FieldModifier, Abstract, Destination from taggedfield where" +
                           " SourceType = " + iSrcType +
                           " AND Tag != '' AND (Companyid = 0 or Companyid = " + iCompanyId +
                           ")  AND TemplateId > 0 ORDER BY Uid ASC, CompanyId DESC";
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
                    String hashable = utils.hashableString(rs.getString(2));
                    newList[iCount].setUid(rs.getInt(1));
                    newList[iCount].setFieldStr(hashable);
                    newList[iCount].setThreshold(rs.getDouble(3));
                    newList[iCount].setPathToData(pathToData);
                    newList[iCount].setFieldModifier(rs.getString(5));
                    newList[iCount].setAl(hashable);
                    newList[iCount].setAbstract(rs.getBoolean(6));
                    newList[iCount].setDestination(rs.getInt(7));
                    iCount++;
                }
                rs.close();
                pstmt.close();
            }
        }
        catch (SQLException e) {
            GenericErrorCls(con, "GetFieldIdentifiers", "Exception reading field identifiers: " + e.getMessage());
        }
        return newList;
    }
	
 	
	public int ExtractCoreUnitId(Connection con, String queryStr) {
	    int               iRtn = 0;
	    PreparedStatement pstmt;
	    String            query;
	    ResultSet         rs;
	    
	    try {
	    	query = "SELECT UID from CoreUnits where XbrlReference = '" + queryStr + "'";
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	if(rs.next())
	    		iRtn = rs.getInt(1);
	    	rs.close();
	    }
		catch (SQLException e) {
			GenericErrorCls(con, "ExtractCoreUnitId", "Exception reading table: " + e.getMessage());
		}
		return(iRtn);
	}
	
	public int  WriteTemplateParse(Connection con, SubmissionInfo subInfo, RootNode curNode) {
	
        int               iRtn = 0;
		PreparedStatement pstmt;
		String            insertStr = "INSERT into TemplateParse (SubUid, Version, TemplateId, IdentifiedText) values (?,?,?,?)";
        String            query;
        ResultSet         rs;
        int               iNoteCounter = 1;
        boolean           bNotFound = true;
        
        
		switch (curNode.GetTemplateId()) {
		
		    case CONSTANTS.GP_NOTE_TABLE:		
			    iRtn = WriteNoteTableRec(con, subInfo, curNode);
			    break;
			
		    case  CONSTANTS.GP_NOTE_DETAIL:
			    iRtn = WriteNoteDetailRec(con, subInfo, curNode);
			    break;
			
			default:
			    iRtn = WriteNormalTableRec(con, subInfo, curNode);
			    break;
		}
		return(iRtn);
	}
	
	public int WriteNoteTemplateParse(Connection con, SubmissionInfo subInfo, RootNode curNode,  int iNoteNdx) {
		
		String            insertStr = "INSERT into TemplateParse (SubUid, Version, TemplateId, IdentifiedText, NoteIndex) values (?,?,?,?,?)";
		PreparedStatement pstmt;
		ResultSet         rs;
		int               iRtn = 0;
		String            query;
		String            extractedLabel = "";
		int               i;
		int               iLen;
		
		try {
			/*
			iLen = "(Detail) -".length();
			i = curNode.GetLabel().indexOf("(Detail) -");
			if(i == -1) {
				iLen = "(Details) -".length();
				i = curNode.GetLabel().indexOf("(Details) -");
			}
			if(i != -1)
				extractedLabel = curNode.GetLabel().substring(i + iLen).trim();
			else
			*/
			extractedLabel = curNode.GetLabel();
			i = curNode.GetLabel().indexOf(CONSTANTS.NOTE_ID);
			if(i != -1) {
				extractedLabel = curNode.GetLabel().substring(i + CONSTANTS.NOTE_ID.length()).trim();
			}

            pstmt = con.prepareStatement(insertStr);
            pstmt.setInt(1,subInfo.GetSubmissionUid());
            pstmt.setInt(2, subInfo.GetVersion());
            pstmt.setInt(3, CONSTANTS.GP_NOTE_TABLE);
            pstmt.setString(4, extractedLabel);
            pstmt.setInt(5, iNoteNdx);
            pstmt.execute();
            query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteNoteTemplateParse", "Exception writing templateparse table: " + e.getMessage());
		}		
        return(iRtn);
    }
	
	private int WriteNoteTableRec(Connection con, SubmissionInfo subInfo, RootNode curNode) {
		String            insertStr;
		PreparedStatement pstmt;
		ResultSet         rs;
		int               iNoteCounter = 1;
		boolean           bNotFound = true;
		String            query;
		int               iRtn = 0;
		String            identifiedText = "";
		int               i;
		
		i = curNode.GetLabel().indexOf(CONSTANTS.NOTE_ID);
		if(i != -1) {
			identifiedText = curNode.GetLabel().substring(i + CONSTANTS.NOTE_ID.length()).trim();
		}
		
		try {
	        insertStr = "Select Uid, IdentifiedText from TemplateParse where TemplateId = 4 and SubUid = " +
                        subInfo.GetSubmissionUid() + " AND Version = " + subInfo.GetVersion() + " Order by Uid";
            pstmt = con.prepareStatement(insertStr);
            rs = pstmt.executeQuery();
            while((bNotFound == true) && rs.next()) {
	            if(curNode.GetLabel().contains(rs.getString(2))) {
		            bNotFound = false;
	            }
	            else
	            	iNoteCounter++;
            }
            rs.close();
            insertStr = "INSERT into TemplateParse (SubUid, Version, TemplateId, IdentifiedText, NoteIndex) values (?,?,?,?,?)";
            pstmt = con.prepareStatement(insertStr);
            pstmt.setInt(1,subInfo.GetSubmissionUid());
            pstmt.setInt(2, subInfo.GetVersion());
            pstmt.setInt(3, curNode.GetTemplateId());
            pstmt.setString(4, curNode.GetLabel());
            pstmt.setInt(5, iNoteCounter);
            pstmt.execute();
            query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteNoteTableRec", "Exception writing templateparse table: " + e.getMessage());
		}		
        return iRtn;
	}
	
	private int WriteNormalTableRec(Connection con, SubmissionInfo subInfo, RootNode curNode) {
		
		String            insertStr = "INSERT into TemplateParse (SubUid, Version, TemplateId, IdentifiedText) values (?,?,?,?)";
		PreparedStatement pstmt;
		ResultSet         rs;
		int               iRtn = 0;
		String            query;
		String            identifiedText = "";
		int               i;
		
		switch (curNode.GetTemplateId()) {
		    case CONSTANTS.GP_NOTE :
				i = curNode.GetLabel().indexOf(CONSTANTS.NOTE_ID);
				if(i != -1) {
					identifiedText = curNode.GetLabel().substring(i + CONSTANTS.NOTE_ID.length()).trim();
				}
			    break;
		
			default:
				i = curNode.GetLabel().indexOf(CONSTANTS.SECTION_ID);
				if(i != -1) {
					identifiedText = curNode.GetLabel().substring(i + CONSTANTS.SECTION_ID.length()).trim();
				}
				break;
		}
		try {
            pstmt = con.prepareStatement(insertStr);
            pstmt.setInt(1,subInfo.GetSubmissionUid());
            pstmt.setInt(2, subInfo.GetVersion());
            pstmt.setInt(3, curNode.GetTemplateId());
            pstmt.setString(4, identifiedText);
            pstmt.execute();
            query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            iRtn = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteNormalTableRec", "Exception writing templateparse table: " + e.getMessage());
		}		
        return(iRtn);
	}

	private int WriteNoteDetailRec(Connection con, SubmissionInfo subInfo, RootNode curNode) {
		String            insertStr;
		PreparedStatement pstmt;
	    int               iRtn = 0;
	    ResultSet         rs;
	    
	    try {
            insertStr = "Select Uid, IdentifiedText from TemplateParse where TemplateId = 4 and SubUid = " +
                        subInfo.GetSubmissionUid() + " AND Version = " + subInfo.GetVersion();
            pstmt = con.prepareStatement(insertStr);
            rs = pstmt.executeQuery();
            while((iRtn == 0) && rs.next()) {
	            if(curNode.GetLabel().contains(rs.getString(2))) {
		            iRtn = rs.getInt(1);
	            }
            }
        rs.close();
	    }
	    catch (SQLException e) {
		    GenericErrorCls(con, "WriteNoteDetailRec", "Exception writing templateparse table: " + e.getMessage());
	    }		
        return(iRtn);
    }

	public int GetScaleNdx(Connection con, int iMultiplier) {
		int               iRtn = 1;  // Default if not found
		String            query;
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
            query = "Select Uid from RowScale where Multiplier = " + iMultiplier;
		    pstmt = con.prepareStatement(query);
		    rs = pstmt.executeQuery();
		    // check if record already exists.
		    if( rs.next()) {
			    iRtn = rs.getInt(1);
		    }
		    rs.close();
		}
		catch(Exception e) {
			GenericErrorCls(con, "GetScaleNdx", "Exception reading table " + e.getMessage());			
		}
		return(iRtn);
	}
	
	public void InsertCoreCell(Connection con, CoreCell coreCell) {
		String            query = "";
		PreparedStatement pstmt;
		ResultSet         rs;
		try {
			query = "INSERT into CoreCell (Uid, ElementUid, RoleUid, UnitUid, ScaleUid, StartDate, EndDate, Negated, tdColumn) values (?,?,?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, coreCell.GetUid());
			pstmt.setInt(2, coreCell.GetElementUid());
			pstmt.setInt(3, coreCell.GetRole());
			pstmt.setInt(4, coreCell.GetUnitUid());
			pstmt.setInt(5, coreCell.GetScale());
			pstmt.setDate(6, new java.sql.Date(coreCell.GetStartDate().getTime()));
			pstmt.setDate(7, new java.sql.Date(coreCell.GetEndDate().getTime()));
			pstmt.setBoolean(8, coreCell.GetNegated());
			pstmt.setInt(9, -1);
		    pstmt.execute();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "InsertCoreCell", e.getMessage());
		}		
		return;
	}
	
	public int FindTaxonomyUid(Connection con, String nameSpace) {
		int               iRtn = 0;	
	    PreparedStatement pstmt;
	    String            query;
	    ResultSet         rs;
	    
	    try {
	    	query = "SELECT UID from Taxonomy where ElementsNameSpace = '" + nameSpace + "'";
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	if(rs.next())
	    		iRtn = rs.getInt(1);
	    	rs.close();
	    }
		catch (SQLException e) {
			GenericErrorCls(con, "FindTaxonomyUid", "Exception reading table: " + e.getMessage());
		}
		return(iRtn);
	}
	
	public ArrayList<RoleGaapXref> GetRoleGaapRefs(Connection con) {
		ArrayList<RoleGaapXref> rtnArray = new ArrayList<RoleGaapXref>();
		String                  query;
		PreparedStatement       pstmt;
		ResultSet               rs;
		RoleGaapXref            thisEntry;
		
	    try {
	    	query = "SELECT * from RoleGaapXref";
	    	pstmt = con.prepareStatement(query);
	    	rs = pstmt.executeQuery();
	    	while(rs.next()) {
	    		thisEntry = new RoleGaapXref();
	    		thisEntry.SetUid(rs.getInt(1));
	    		thisEntry.SetText(rs.getString(3));
	    		thisEntry.SetNegated(false);
	    		rtnArray.add(thisEntry);
	    		if(rs.getString(5) != null) {
	    		    thisEntry = new RoleGaapXref();
	    		    thisEntry.SetUid(rs.getInt(1));
	    		    thisEntry.SetText(rs.getString(5));
	    		    thisEntry.SetNegated(true);
	    		    rtnArray.add(thisEntry);
	    		}
	    	}
	    	rs.close();
	    }
		catch (SQLException e) {
			GenericErrorCls(con, "GetRoleGaapRefs", "Exception reading table: " + e.getMessage());
		}
		return(rtnArray);
	}
	
	public void WriteCoreDimensions(Connection con,  int coreCellUid, int AxisElementUid, int MemberElementUid, int iType) {
		
		String            query = "";
		PreparedStatement pstmt;
		
		try {
			switch (iType) {
			    case 0:
					query = "INSERT into CoreDimension (CoreCellUid, AxisElementUid, MemberElementUid) values (?,?,?)";
					break;
			
			    case 1:
					query = "INSERT into SectionFactDimension (SectionFactUid, AxisElementUid, MemberElementUid) values (?,?,?)";
					break;
			    	
		}
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, coreCellUid);
			pstmt.setInt(2, AxisElementUid);
			pstmt.setInt(3, MemberElementUid);
			//pstmt.setInt(2, dimensionTaxUid.GetDimTaxonomyUid());
			//pstmt.setInt(3, dimensionTaxUid.GetDataTaxonomyUid());
		    pstmt.execute();
 		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteCoreDimensions", e.getMessage());
			GenericErrorCls(con, "writeCoreDimensions1", "CoreCell = " + coreCellUid + " DimTax = " + AxisElementUid +
					        " DataTax = " + MemberElementUid);
		}
	}
	
	public int WriteFootNoteRec(Connection con, String footNote) {
		int               iRtn = 0;
		String            query = "";
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "INSERT into FootNote (text) values (?)";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, footNote);
		    pstmt.execute();
		    query = "SELECT @@IDENTITY";
	        pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
	        rs.next();
	        iRtn = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteFootNoteRec", "Exception writing footnote table: " + e.getMessage());
		}		
		return(iRtn);	
	}
	
	public void WriteFootnoteRef(Connection con, int coreCellUid, int footNoteUid) {
		String            query = "";
		PreparedStatement pstmt;
		ResultSet         rs;
		
		try {
			query = "INSERT into FootnoteReference (factId, FootnoteId) values (?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, coreCellUid);
			pstmt.setInt(2, footNoteUid);
		    pstmt.execute();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteFootnoteRef", "Exception writing footnote Reference table: " + e.getMessage());
		}		
		
	}
	
	public void RemoveUnMappedNotes(Connection con, SubmissionInfo subInfo) {
		String            query;
		PreparedStatement pstmt;
		
		try {
			query = "Delete from TemplateParse where TemplateId = 4 AND BeginLineNum is null AND " +
		             "SubUid = " + subInfo.GetSubmissionUid() + " AND Version = " + subInfo.GetVersion();
			pstmt = con.prepareStatement(query);
			pstmt.execute();		
		}
		catch (SQLException e) {
			GenericErrorCls(con, "RemoveUnMappedNotes", "Exception deleting: " + e.getMessage());
		}		
	}
	
	public int WriteSectionRec(Connection con, String label, int NoteUid, String text) {
		
		String            query;
		PreparedStatement pstmt;
		int               sectionUid = 0;
		ResultSet         rs;
		
		try {
			query = "INSERT into Section (Title, NoteUid, SectionText) values (?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, label);
			pstmt.setInt(2, NoteUid);
			pstmt.setString(3, text);
		    pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            sectionUid = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteSectionRec", "Exception writing: " + e.getMessage());
		}
		return(sectionUid);
	}
	
	public int WriteFactSentenceRec(Connection con, String text) {
		
		String            query;
		PreparedStatement pstmt;
		int               factSentenceUid = 0;
		ResultSet         rs;
		
		try {
			query = "INSERT into FactSentences (Sentence) values (?)";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, text);
		    pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            factSentenceUid = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteFactSentenceRec", "Exception writing: " + e.getMessage());
		}
		return(factSentenceUid);
	}

	public int WriteSectionFact(Connection con, int SectionUid, int FactSentenceUid, String data, int gaapId) {
		
		String            query;
		PreparedStatement pstmt;
		int               sectionFactUid = 0;
		ResultSet         rs;
		UTILITIES         utilities = new UTILITIES();
		
		String            convertedFact = utilities.convertFact(data);
		try {
			
			query = "INSERT into SectionFact (SectionUid, FactSentenceUid, Fact, Gaap_Id, Active) values (?,?,?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, SectionUid);
			pstmt.setInt(2, FactSentenceUid);
			pstmt.setString(3, convertedFact);
			pstmt.setInt(4, gaapId);
			pstmt.setBoolean(5, true);
		    pstmt.execute();
		    query = "SELECT @@IDENTITY";
            pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();
            rs.next();
            sectionFactUid = rs.getInt(1);
            rs.close();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteFactSentenceRec", "Exception writing: " + e.getMessage());
		}
		return(sectionFactUid);
	}
	
	public ArrayList<UnitUid> GetUnitUids(Connection con) {
		ArrayList<UnitUid> rtnArray = new ArrayList<UnitUid>();
		String             query;
		ResultSet          rs;
		PreparedStatement  pstmt;
		UnitUid            nextRec;
		
		try {
			query = "Select Uid, XBRLReference FROM CoreUnits";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
			    nextRec = new UnitUid();
			    nextRec.SetUid(rs.getInt(1));
			    nextRec.SetXbrlRefStr(rs.getString(2).toLowerCase());
			    rtnArray.add(nextRec);
			}
		}
		catch (SQLException e) {
			GenericErrorCls(con, "GetUnitUids", "Exception reading: " + e.getMessage());
		}
		return(rtnArray);
	}

	public ArrayList<ScaleUid> GetScaleUids(Connection con) {
		ArrayList<ScaleUid> rtnArray = new ArrayList<ScaleUid>();
		String             query;
		ResultSet          rs;
		PreparedStatement  pstmt;
		ScaleUid           nextRec;
		Float              fTemp;
		int                sNumScale;
		
		try {
			query = "Select Uid, Multiplier FROM RowScale";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while(rs.next()) {
			    nextRec = new ScaleUid();
			    nextRec.SetUid(rs.getInt(1));
			    fTemp = rs.getFloat(2);
			    sNumScale = 0;
			    while(fTemp > 1) {
			    	fTemp = fTemp/10;
			    	sNumScale++;
			    }
			    nextRec.SetXbrlRefStr(sNumScale);
			    rtnArray.add(nextRec);
			}
		}
		catch (SQLException e) {
			GenericErrorCls(con, "GetUnitUids", "Exception reading: " + e.getMessage());
		}
		return(rtnArray);
	}

	public void WriteSectionFactRefRec(Connection con, int sectionFactUid, int gaapId, int unitUid, 
			                           int scaleUid, String startDate, String endDate) {
		
		String            query;
		PreparedStatement pstmt;
		
		try {
			query = "INSERT into SectionFactReference (SectionFactUid, State, ElementUid, UnitUid," +
		             "ScaleUid, StartDate, EndDate, DefinitionPath) values (?,?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, sectionFactUid);
			pstmt.setInt(2, 0);
			pstmt.setInt(3, gaapId);
			pstmt.setInt(4, unitUid);
			pstmt.setInt(5, scaleUid);
			pstmt.setString(6, startDate);
			pstmt.setString(7, endDate);
			pstmt.setInt(8, 0);
		    pstmt.execute();
		}
		catch (SQLException e) {
			GenericErrorCls(con, "WriteSectionFactRefRec", "Exception writing: " + e.getMessage());
		}
	}
	
	public int WriteTaggedFieldEntry(Connection con, String tag, int iTemplateId, int iCompanyId, int iSubUid, 
		                            int iItemVersion, boolean isParenthetical) {
	    	
	    String            query;
	  	String            FieldData = "";
	    PreparedStatement pstmt;
        ResultSet         rs;
	    int               iRtn = 0;
	        
	    if(isParenthetical == false) {
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
	            GenericErrorCls(con, "WriteRawDataRow", "Exception writing table: " + e.getMessage());
		    }
		}
		return(iRtn);
	}
}
