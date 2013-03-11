package BatchSubmission;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;

import com.bricksimpe.rdg.templateId.TemplateIdentifier;
import com.bricksimpe.rdg.util.EdgarExtractor;
import com.bricksimple.rdg.FieldId.FieldId;
import java.io.File;
import java.text.*;
import java.util.Date;

public class BatchSubmission {

	public static void main(String[] args) { 

	    //BatchSubmission main = new BatchSubmission();
	    
	    String FilingsSourcePath = args[0];	
	    String databaseServer = args[1];
	    String databasePort = args[2];
	    String databaseDomain = args[3];
	    String databaseName = args[4];
	    String displayName = args[5];
	    String username = args[6];
	    String password = args[7];
	    String companyName = args[8];
	    String OrigSourcePath = args[9];
	    // String LogFileName = args[9];
	    String LogFileName = OrigSourcePath + "\\AppErrorLog.txt";
	    String NoteDetailFilePath = OrigSourcePath + "\\"; 
	    		
	    ArrayList<String> Filings = new ArrayList<String>();
        Commons           commons = new Commons();
	    Filings = commons.GetListOfFiles(OrigSourcePath);
        //Filings = main.GetListOfFiles(FilingsSourcePath);	

        MySqlAccess       mySqlAccess = new MySqlAccess();
        Connection        myCon;
        
        String            htmlFilePath = "";
        String            edgarFilePath  = "";
        String            submissionUidString = "0";
        Boolean           useWindows = false;
        int               iNdx;
        int               iLastUid = 0;
        ArrayList<String> outArray = new ArrayList<String>();
        
		//FileOutputStream ostream;
		//DataOutputStream out;
		//BufferedWriter   wr;
        try {
            FileWriter outFile = new FileWriter(LogFileName);
            PrintWriter out = new PrintWriter(outFile);
	        myCon = mySqlAccess.OpenConnection(databaseServer, databaseDomain, databasePort, databaseName, username, password, useWindows);  
	        if(Filings == null)
	        	out.println("Error reading source htm list file:");
	        else {
                for (String fileName: Filings) {
                	if(commons.CopyFile(fileName, OrigSourcePath, FilingsSourcePath) == false) 
                		System.out.println("Unable to copy file to server: " + fileName);
                	else {
                		System.out.println("Parsing: " + fileName);
            	        String tempStr = new StringBuffer(fileName).reverse().toString();
                	    iNdx = tempStr.indexOf(".");
                	    tempStr = "txt." + tempStr.substring(iNdx+1);
        	            edgarFilePath = FilingsSourcePath + "\\" + new StringBuffer(tempStr).reverse().toString();
            	        htmlFilePath = FilingsSourcePath + "\\" + fileName;
	                    EdgarExtractor edgarExtractor = new EdgarExtractor();

                        edgarExtractor.setPrivDisplayName( displayName );
                        edgarExtractor.setPrivInputFile( htmlFilePath );
                        edgarExtractor.setPrivOutputFile( edgarFilePath );
                        edgarExtractor.setPrivCompanyName( companyName );
                        edgarExtractor.setPrivUserName( username );
                        edgarExtractor.setPrivPassword( password );
                        edgarExtractor.setPrivUseWindows( useWindows );
                        edgarExtractor.setPrivDataBase( databaseName );
                        edgarExtractor.setPrivServerName( databaseServer );
                        edgarExtractor.setPrivPortNumber( databasePort );
                        edgarExtractor.setPrivInstance( databaseDomain );
                        edgarExtractor.setPrivPrevUid( submissionUidString );
                        edgarExtractor.setPrivUserCompanyUid("0");
                        
                        iLastUid = mySqlAccess.GetLastAppErrorId(myCon);
                        int submissionUid = edgarExtractor.RunThis( );
    
                        TemplateIdentifier templateIdentifier = new TemplateIdentifier( );
                        templateIdentifier.setPrivInputFile( edgarFilePath );
                        templateIdentifier.setPrivCreateOutputFile( "N" );
                        templateIdentifier.setPrivUserName( username );
                        templateIdentifier.setPrivPassword( password );
                        templateIdentifier.setPrivUseWindows( useWindows );
                        templateIdentifier.setPrivDataBase( databaseName );
                        templateIdentifier.setPrivServerName( databaseServer );
                        templateIdentifier.setPrivPortNumber( databasePort );
                        templateIdentifier.setPrivInstance( databaseDomain );
                        templateIdentifier.setPrivNewVersion( 0 );

                        templateIdentifier.RunThis( );

                        FieldId fieldIdentifier = new FieldId( );
                        fieldIdentifier.setPrivInputFile( edgarFilePath );
                        fieldIdentifier.setPrivUserName( username );
                        fieldIdentifier.setPrivPassword( password );
                        fieldIdentifier.setPrivUseWindows( useWindows );
                        fieldIdentifier.setPrivDataBase( databaseName );
                        fieldIdentifier.setPrivServerName( databaseServer );
                        fieldIdentifier.setPrivPortNumber( databasePort );
                        fieldIdentifier.setPrivInstance( databaseDomain );
    
                        fieldIdentifier.RunThis( );
                        outArray = mySqlAccess.GetAppErrorOut(myCon,  iLastUid);
                        out.println("FILING: " + fileName);
                        for(int j = 0; j < outArray.size(); j++) {
                	        out.println("    " + outArray.get(j));
                        }
                        File file =  new File(htmlFilePath);
                        long length = file.length();
                        String duration = commons.GetDuration(outArray.get(0), outArray.get(outArray.size() -1));
                        if(duration.length() == 0) {
                        	duration = ":Time stamp records not found";
                        }
                        else {  // successful parse - extract note details
                        	LogNoteDetails lnd = new LogNoteDetails();
                        	String NoteDetailFileName = commons.ConstructNoteDetailFileName(fileName);
                        	int iRtn = lnd.DoLogging(myCon, htmlFilePath, NoteDetailFilePath + NoteDetailFileName);
                        	if(iRtn == 1) {
                        		out.println("Unable to write noteDetailFile: " + NoteDetailFilePath + NoteDetailFileName);
                        	}
                        	if(iRtn == 2) {
                        		out.println("Unable to find submission ");
                        	}
                        }
                        String lengthStr = commons.ConvertLengthToString(length);
                        out.println("File Length = " + lengthStr + " KB: " + duration);
                        out.println("");
                	}
	            }
	        }
            out.close();
            myCon.close();
        }
        catch (Exception e) {
        	System.out.println("ERROR CLOSING CONNECTION: " + e.getMessage());
        }
	}
	
}
