package BatchSubmission;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MySqlAccess {

	private static boolean useJTDS = true;
	private static boolean bDebug = true;
	
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
 	 	      if(bDebug) {
 	 	    	  
 	 	      }
 	 	      //System.out.println("ConnectionString: " + connectionString);
 	 	      //System.out.println("User: " + UserName + ":: Password: " + Password);
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
	
	public int GetLastAppErrorId(Connection con) {
	    String query = "Select Max(Uid) from AppError";
	    int    iRtn = 0;
	    	
	    try {
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        if(rs.next()) {
	        	iRtn = rs.getInt(1);
	        }
	        rs.close();
	        stmt.close();
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading xlateeventstr " + e.getMessage());
			iRtn = 0;
	    }
	    return(iRtn);
	}
	
	public ArrayList<String> GetAppErrorOut(Connection con, int iLastUid) {
		ArrayList<String> rtnArray = new ArrayList<String>();
		String            outStr = "";
		String            query = "SELECT Uid, Application, ErrorText, SrcFunction From AppError where uid > " + iLastUid +
				                  " Order by Uid";
		String            srcFunction;
		
	    try {
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while(rs.next()) {
	        	outStr = rs.getString(2) + ": ";
	        	srcFunction = rs.getString(4);
	        	if(srcFunction.equals("Run") == false)
	        		outStr += srcFunction + ": ";
	        	outStr += rs.getString(3);
	        	rtnArray.add(outStr);
	        }
	        rs.close();
	        stmt.close();
	    }
	    catch (SQLException e) {
			//System.err.print("Exception reading xlateeventstr " + e.getMessage());
			
	    }
		return(rtnArray);
	}
	
	public SubmissionInfo GetUidVersion(Connection con, String htmlFile) {
		SubmissionInfo si = new SubmissionInfo();	
		String         query = "SELECT Uid, Version From Submissions where SrcFile = '" + htmlFile + "'";

        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
            	si.iUid = rs.getInt(1);
            	si.iVersion = rs.getInt(2);
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
        }
		return(si);
	}
	
	public ArrayList<Note> GetNotes(Connection con, SubmissionInfo si) {
		ArrayList<Note> rtnArray = new ArrayList<Note>();
		String          query = "SELECT Uid, IdentifiedText From TemplateParse where SubUid = " + si.iUid + 
				                " And Version = " + si.iVersion + " AND TemplateId = 4 ORDER by Uid";
        Note            cur = null;
        
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
            	cur = new Note();
            	cur.NoteUid = rs.getInt(1);
            	cur.IdentifiedText = rs.getString(2);
            	rtnArray.add(cur);
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
        }
	return(rtnArray);
	}
	
	public ArrayList<Integer> GetSections(Connection con, int iNoteUid) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		
		String          query = "SELECT Uid From Section where NoteUid = " + iNoteUid;
        
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
            	rtnArray.add(rs.getInt(1));
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
        }
		return(rtnArray);
	}
	
	public ArrayList<FactInfo> GetFacts(Connection con, int SectionUid) {
		ArrayList<FactInfo> rtnArray = new ArrayList<FactInfo>();
		
		String          query = "SELECT Fact, FactSentenceUid From SectionFact where SectionUid = " + SectionUid + " ORDER BY Uid";
        FactInfo        cur = null;
        
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
            	cur = new FactInfo();
            	cur.fact = rs.getString(1);
            	cur.factSentenceUid = rs.getInt(2);
            	rtnArray.add(cur);
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
        }
	
		return(rtnArray);
	}
	
	public String GetSentence(Connection con, int iSentenceUid) {
		String   rtnStr = "";
		String          query = "SELECT Sentence From FactSentences where UId = " + iSentenceUid;
        FactInfo        cur = null;
        
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()) {
            	rtnStr = rs.getString(1);
            }
            rs.close();
            stmt.close();
        }
        catch (SQLException e) {
        }
        return(rtnStr);		
	}
}
