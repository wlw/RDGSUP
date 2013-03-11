package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;
import java.sql.Connection;
import com.bricksimple.rdg.xbrlUpload.UTILITIES;

public class EquityRow {

    private ArrayList<String> contextRefs = new ArrayList<String>();
    private String            InstantDate = "";
    private String            AddADay = "";
    private String            BeginDate = "";
    private String            EndDate = "";
    
    public void SetContextRef(String iValue) {
    	contextRefs.add(iValue);
    }
    	
    public boolean IsThisMyContextRef(String iValue) {
    	boolean bRtn = false;
    	int     i = 0;
    	
    	while((i < contextRefs.size()) && ( bRtn == false)) {
    		if(contextRefs.get(i).equals(iValue))
    			bRtn = true;
    		i++;
    	}
    	return(bRtn);
    }
    
    public void AddInstant(Connection con, String iValue, String sValue) {
    	UTILITIES utilities = new UTILITIES();
    	
    	contextRefs.add(sValue);
    	InstantDate = iValue;
    	AddADay = utilities.AddADay(con, iValue);
    }
    
    public boolean IsThisMySlot(String instant, String begin) {
    	boolean bRtn = false;
    	
    	if(instant.length() > 0) {
    		bRtn = InstantDate.equals(instant);
    	}
    	else {
    		bRtn = AddADay.equals(begin);
    	} 
    	return(bRtn);
    }

}
