package com.bricksimple.rdg.xbrlUpload;

//import java.util.zip.*;
import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.*;

import com.bricksimple.rdg.sqlaccess.MySqlAccess;

public class Zipping {

	final int  BUFFER = 2048;
	
	public XbrlFileStruct UnZip(Connection con, String zipFileName) {
		ErrorCls             errorCls = new ErrorCls();
		MySqlAccess          mySql = new MySqlAccess();
        //BufferedOutputStream dest = null;
        XbrlFileStruct       xbrlFileStruct = new XbrlFileStruct();
		String               path = "";
		int                  iRtn = 0;
		//String               fileName = "";
		UTILITIES            utilities = new UTILITIES();		
		ZipFile              zipFile;
		boolean              bRtn = true;
		
		utilities.MemoryUsage("UnZip");	    		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("UnZip");
		errorCls.setItemVersion(0);
		int j = zipFileName.lastIndexOf("\\");
		path = zipFileName.substring(0, j+ 1);
		xbrlFileStruct.SetFilePath(path);
	    try {
			utilities.MemoryUsage("UnZip:preZipEntry");		
			
	    	zipFile = new ZipFile(zipFileName);
	    	Enumeration entries = zipFile.getEntries();
	    	while(entries.hasMoreElements()) {
	    		ZipArchiveEntry zipEntry =  (ZipArchiveEntry)entries.nextElement();
	    		if((iRtn = xbrlFileStruct.InsertXbrlFile(zipEntry.getName())) == 0) {
	        	    bRtn = copyInputStream(con, zipEntry.getName(), zipFile.getInputStream(zipEntry),
	        				               new BufferedOutputStream(new FileOutputStream(path + zipEntry.getName())));
	        		if(bRtn == false) {
	        			break;
	        		}
	    		}
		        else {
		            switch (iRtn) {
		        		case 1:
		    	            errorCls.setErrorText(zipEntry.getName() + "File not part of XBRL Filing:");
		        			break;
		        			
		        		case 2:
		    	        	errorCls.setErrorText(zipEntry.getName() + "Duplicate File:");
		        			break;
		        	}
		    		mySql.WriteAppError(con, errorCls);
		        }
	    	}
	    	zipFile.close();
	        xbrlFileStruct.ValidateFiles();
	        if(xbrlFileStruct.GetStatus() != 0) {
	        	switch (xbrlFileStruct.GetStatus()) {
	        	    case 1:
				        errorCls.setErrorText("Error: Not all required XBRL files found");
				        break;

	        	    case 2:
				        errorCls.setErrorText("Error: Invalid XBRL root file name found");
				        break;
				        
	        	    case 3:
				        errorCls.setErrorText("Error: Invalid XBRL root file name found and not all required XBRL files found");
				        break;

	        	    case 4:
				        errorCls.setErrorText("Error: Html file not found");
				        xbrlFileStruct.SetStatus(0);   // don't care but log the error
				        xbrlFileStruct.SetHaveHtm(false);
				        break;				        
                }
				mySql.WriteAppError(con, errorCls);
	        }

			zipFile.close();
	    }
		catch (Exception e) {
			errorCls.setErrorText("Error : " + e.getMessage() + " Extracting: " + zipFileName);
			mySql.WriteAppError(con, errorCls);
			xbrlFileStruct.SetStatus(1);
		}
		return(xbrlFileStruct);
	}

	public ArrayList<String> FindZipFiles(Connection con, String zipFileName, String path) {
		ArrayList<String>    rtnArray = new ArrayList<String>();
		ErrorCls             errorCls = new ErrorCls();
	    MySqlAccess          mySql = new MySqlAccess();
		UTILITIES            utilities = new UTILITIES();
        ZipFile              zipFile = null;
        String               entryName = "";
        boolean              bContinue = true;
        boolean              bRtn;
        
		utilities.MemoryUsage("FindZipFiles");	    
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("FindZipFiles");
		errorCls.setItemVersion(0);
	    try {
	    	zipFile = new ZipFile(zipFileName);
	    	Enumeration entries = zipFile.getEntries();
	    	while((entries.hasMoreElements() && (bContinue == true))) {
	    		ZipArchiveEntry zipEntry =  (ZipArchiveEntry)entries.nextElement();
	    		entryName = zipEntry.getName();
	        	if(zipEntry.getName().toLowerCase().contains(".zip")) {
	        		rtnArray.add(path + zipEntry.getName());
	        		bRtn = copyInputStream(con, zipEntry.getName(), zipFile.getInputStream(zipEntry),
	        				               new BufferedOutputStream(new FileOutputStream(path + zipEntry.getName())));
	        		if(bRtn == false) {
	        			rtnArray = new ArrayList<String>();
	        			break;
	        		}
	        	}
	        	else {
	        		rtnArray.add(zipFileName);
	        		bContinue = false;
	        	}
	    	}
	    	zipFile.close();
	    	}
			catch (Exception e) {
				errorCls.setErrorText("Error : " + e.getMessage() + " Extracting: " + zipFileName);
				mySql.WriteAppError(con, errorCls);
				rtnArray = new ArrayList<String>();
			}
		return(rtnArray);
	}
	
	    
	private boolean copyInputStream(Connection con, String fileName, InputStream in, OutputStream out) {
		boolean      bRtn = true;
		byte[]       buffer = new byte[1024];
	    int          len;
        ErrorCls     errorCls = new ErrorCls();
        MySqlAccess  mySql = new MySqlAccess();
        
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("copyInputStream");
		errorCls.setItemVersion(0);
	    try {
		    while((len = in.read(buffer)) >= 0)
		        out.write(buffer, 0, len);

	        in.close();
	        out.close();
	    }
	    catch (Exception e) {
			errorCls.setErrorText("Error : " + e.getMessage() + " Extracting: " + fileName);
			mySql.WriteAppError(con, errorCls);
			bRtn = false;
	    }
	    return(bRtn);
    }

}
