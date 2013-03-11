package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;
import java.util.Arrays;
import java.sql.Connection;

import com.bricksimple.rdg.sqlaccess.MySqlAccess;


public class XbrlFileStruct {
    private int                  iStatus = 0;
    private String               filePath = "";
    private ArrayList<XbrlFiles> xbrlFiles = new ArrayList<XbrlFiles>();
    private boolean              HaveHtm = true;

    public void SetStatus(int ivalue) {
    	iStatus = ivalue;
    }
    
    public int GetStatus() {
    	return(iStatus);
    }
    
    public void SetFilePath(String path) {
    	filePath = path;
    }
    
    public String GetFilePath() {
    	return(filePath);
    }
    
    public ArrayList<XbrlFiles> GetXbrlFiles() {
    	return(xbrlFiles);
    }
    
    public void SetHaveHtm(boolean bValue) {
    	HaveHtm = bValue;
    }
    
    public boolean GetHaveHtm() {
    	return(HaveHtm);
    }
    
    public int OpenDoms(Connection con, MySqlAccess mySql) {
    	
     	int        i = 0;
    	DomBuilder domBuilder = new DomBuilder();
    	
    	while((iStatus == 0) && (i < xbrlFiles.size())) {
    	    if(domBuilder.InsertFileIntoDom(con, mySql, filePath, xbrlFiles.get(i)) == false) 
    	    	iStatus = 1;
    	    else
    	    	i++;
    	}
    	return(iStatus);
    }
    
     public int InsertXbrlFile(String strFile) {
    	XbrlFiles curXbrlFiles = new XbrlFiles();
    	
    	int iRtn = curXbrlFiles.SetXbrlFile(strFile);
    	if(iRtn == 0) {
    		boolean bDup = false;
    		for(XbrlFiles cur: xbrlFiles) {
    			if(cur.GetFileType() == curXbrlFiles.GetFileType())
    				bDup = true;
    		}
    		if(bDup == true)
    			iRtn = 2;
    		else
    		   xbrlFiles.add(curXbrlFiles);
    	}
    	return(iRtn);
    }
    
    public void ValidateFiles() {
    	ArrayList<Integer> iFounds = new ArrayList<Integer>( Arrays.asList(1,1,1,1,1,1,1));
    	int                iValue = 0;
    	int                j;
    	String             root = "";
    	boolean            bFoundHtm = false;
    	int                iNumFilesFound = 0;
    	
    	for(XbrlFiles cur: xbrlFiles) {
    		j = cur.GetFileType();
    		iFounds.set(j-1, 0);
    		iNumFilesFound++;
    		if(j != CONSTANTS.XBRL_HTM) {  // this is the check for invalid file name
    			if(root.length() == 0)
    				root = cur.GetRoot();
    			else {
    				if(root.equals(cur.GetRoot()) == false)
    					iStatus = 2;
    			}
    		}
    		else
    			bFoundHtm = true;
    	}
    	for(int k = 0; k < iFounds.size(); k++) {
    		iValue += iFounds.get(k);
    	}
    	if(iValue > 0) {
    		if((iValue == 1) && (bFoundHtm == false))
    				iStatus = 4;
    		else
    		iStatus += 1;
    	}
    	//if((iStatus == 0) && (bFoundHtm == false))
    	//	iStatus = 4;
    }
}
