package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

public class FilingInfo {
    private String CompanyId = "";
    private String DateFiling = "";  // format MMDDYY
    private String SrcFile = "";
    private String FullSrcFile = "";
    private String CompanyAbbrev = "";
    
    public String GetCompanyId() {
    	return(CompanyId);
    }
    
    public String GetDateFiling() {
    	return(DateFiling);
    }
    
    public String GetSrcFile() {
    	return(SrcFile);
    }
    public String GetFullSrcFile() {
    	return(FullSrcFile);
    }
    
    public String GetCompanyAbbrev() {
    	return(CompanyAbbrev);
    }
    
    public void ExtractInfo(ArrayList<XbrlFiles> origFiles, String path) {
    	boolean bFound = false;
    	String  origStr = "";
    	int     i = 0;
    	
    	while((bFound == false) && (i < origFiles.size())) {
       		if(origFiles.get(i).GetFileName().endsWith(".htm")) {
    			SrcFile = origFiles.get(i).GetFileName();
     			FullSrcFile = path + SrcFile;
    			bFound = true;
    		}
    		i++;
    	}
    	while((bFound == false)  && (i < origFiles.size())) {
    		if(origFiles.get(i).GetFileName().endsWith(".xsd")) {
    			SrcFile = origFiles.get(i).GetFileName();
    			SrcFile = SrcFile.replace(".xsd", ".htm");
    			FullSrcFile = path + SrcFile;
    			bFound = true;
    		}
    		i++;
    	}
    	i = 0;
    	bFound = false;
    	while(bFound == false) {
    		if(origFiles.get(i).GetFileName().endsWith(".xml"))
    			bFound = true;
    		else
    			i++;
    	}
    	origStr = origFiles.get(i).GetFileName();
    	int j = origStr.lastIndexOf(".");
    	String Extract = origStr.substring(0, j);
    	i = Extract.indexOf("-");
    	//if(Integer.parseInt(userCompanyUid) == 0)
    	    CompanyAbbrev = Extract.substring(0, i);
    	//else
    	//	CompanyId = userCompanyUid;
    	Extract = Extract.substring(i+1);   // remaining formay YYYYMMDD or YYYYMMDD_xxx
    	i = Extract.indexOf("_");
    	if(i != -1)
    		Extract = Extract.substring(0, i -1);
    	DateFiling = Extract.substring(4,6) + Extract.substring(6) + Extract.substring(2,4); // format MMDDYY
    }
}
