package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;
import java.sql.Connection;
import com.bricksimple.rdg.sqlaccess.MySqlAccess;
import com.bricksimple.rdg.ExtractedClasses.XmlExtract;

public class ElementPathXrefTbl {
	
    private ArrayList<ElementPathXref> data = new ArrayList<ElementPathXref>();
    private String                     customPath;
    private String                     customAbbrev;
    
    public void InitList(Connection con, String myCustomPath, String myCustomAbbrev) {
    	MySqlAccess mySqlAccess = new MySqlAccess();
    	
    	data = mySqlAccess.GetElementXref(con);
    	customPath = myCustomPath;
    	customAbbrev = myCustomAbbrev;
    }
    
    public String FindElementNameSpace(String ElementPath, XmlExtract xmlExtract) {
    	String     rtnStr = "";
    	int        iCount = 0;		
    	String     workStr = "";
    	int        i;
    	String     taxXref = "";
    	
    	i = ElementPath.indexOf("#");
    	workStr = ElementPath.substring(0, i);
    	while((taxXref.length() == 0) && (iCount < data.size())) {
    		if(workStr.equals(data.get(iCount).GetElementsPath()))
    			taxXref = data.get(iCount).GetElementsNameSpace();
    	    iCount++;
    	}
    	if(taxXref.length()  > 0) {
    		i = 0;
    	    while((rtnStr.length() == 0) && (i <xmlExtract.GetNameSpaceLinks().size())) {
    	    	if(taxXref.equals(xmlExtract.GetThisNameSpaceLink(i).GetNameSpace()))
    	    		rtnStr = xmlExtract.GetThisNameSpaceLink(i).GetAbbrev();
    	    	i++;
    	    }
    	}
    	else {
    		//if(ElementPath.equals(customPath))
    		rtnStr = customAbbrev;
    	}
    	return(rtnStr);
    }
}
