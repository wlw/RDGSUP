package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;
import java.sql.*;


public class TemplateHdr {
    public int               currentLine = 0;
    public int               IdLine = 0;
    public String            supStr = "";
    public String            Date1 = "";
    public boolean           bDate1Complete = false;
    public String            Date2 = "";
    public boolean           bDate2Complete = false;
    public int               iDateType = -1;   // -1 MT; 0 Complete Date; 1 partial date
    public int               iUnAudited = -1;  // -1 MT; 1 Unaudited
    public int               iScale = 1;
    private String           CombinedStr = "";
    
    public ArrayList<String> HeaderLines = new ArrayList<String>(); 
    
    public void ParseHdr(Connection con) {
    	
    	CombineStrings();
    	ExtractScale();
    	CheckUnaudited();
    	CheckForIntervals(con);
    	CheckForDates();
    }
    
    private void CheckForDates() {
    	int   iNdxOfMonth = 0;
    	int   iLoopCounter = 2;
    	CONSTANTS constants = new CONSTANTS();
    	
    	// possible two dates so check max of two times
    	while(iLoopCounter > 0) {
    	    iNdxOfMonth = constants.GetFirstMonthContained(CombinedStr);
    	    if(iNdxOfMonth != -1) {  // found a month
    	    	iLoopCounter--;
    	    	ExtractMonthCls emc = new ExtractMonthCls();
    	    	emc.ExtractThisMonth(CombinedStr, iNdxOfMonth);
    	    	Date1 = emc.MonthExtracted;
    	    	CombinedStr = emc.ExtractedStr;
    	    	if(iLoopCounter == 1)
    	    	    bDate1Complete = emc.bCompleteDate;
    	    	else
    	    	    bDate2Complete = emc.bCompleteDate;
    	    }
    	    else {
    	    	iLoopCounter = 0;  // no month get out
    	    	if(Date1.length() > 0) { // no second full date - maybe just year
    	    		String YearStr = LookingForNums(CombinedStr);
    	    		if(YearStr.length() > 0) {  // got a year
    	    			Date2 = Date1.substring(0, Date1.length() -4);  // get all except year
    	    			Date2 += YearStr;
    	    			bDate2Complete = true;
    	    			CombinedStr = CombinedStr.substring(YearStr.length()).trim();
    	    		}
    	    	}
    	    }
    	}
    }
    
    private String LookingForNums(String ExtractedStr) {
    	String    NumStr = "";
    	String    testStr = "";
    	boolean   bLookingForNums = true;
       	CONSTANTS constants = new CONSTANTS();
   	
    	while(bLookingForNums) {
    		if(ExtractedStr.length() == 0)
    			bLookingForNums = false; 
    		else {
    		    testStr = ExtractedStr.substring(0,1);
    		    if(constants.isNumeric(testStr) != -1) {
    			    NumStr += testStr;
    			    ExtractedStr = ExtractedStr.substring(1);
    		    }
    		    else
    			    bLookingForNums = false;
    		}
    	}
        return(NumStr);
    }

    private void CheckForIntervals(Connection con) {
    	MySqlAccess            mySql = new MySqlAccess();
    	SupplementCls          supplementCls = new SupplementCls();
    	SupplementRtn          supplementRtn = new SupplementRtn();
    	
    	MatchStr[] DateSups =  mySql.GetListOfDateSup(con);
    	supplementRtn = supplementCls.ContainsSupplementStr(CombinedStr, DateSups);
    	if(supplementRtn.getiRtn() != -1) {
    	    supStr = supplementRtn.getSupplementStr();
    	    CombinedStr = supplementRtn.getRemainingStr();
    	}
     }
    private void CombineStrings() {
        boolean  bFirst = true;
    	
    	for(String curStr : HeaderLines) {
            if( bFirst) {
    		    CombinedStr = curStr;
    		    bFirst = false;
    		}
    		else {
    			if(curStr.length() > 1 ){
    				if(CombinedStr.length() == 0)
    					CombinedStr = curStr;
    				else {
    			        if((CombinedStr.substring(CombinedStr.length() -1).equals(" ") == true) ||(curStr.substring(1,2).equals(" ") == true))
    			    	    CombinedStr += curStr;
    			        else
    			    	    CombinedStr += " " + curStr;
    				}
    			}
    	 	}
    	}
    	CombinedStr = CombinedStr.toLowerCase();
    }
    
    private void ExtractScale() {
    	
    	if((CombinedStr.contains("in thousands")) ||
    	   (CombinedStr.contains("in 000's"))) {
    		iScale = 1000;
    		CombinedStr = CombinedStr.replace("in thousands", "");
    	}
    	else {
    		if(CombinedStr.contains("in millions")) {
    			iScale = 1000000;
    			CombinedStr = CombinedStr.replace("in millions", "");
    		}
        	else {
        		if(CombinedStr.contains("in billions")) {
        			iScale = 1000000000;
        			CombinedStr = CombinedStr.replace("in billions", "");
        		}
         	}
     	}
    	CombinedStr = CombinedStr.replace("except per share amounts", "");
    }
    
    private void CheckUnaudited() {
    	if(CombinedStr.contains("unaudited")) {
    		iUnAudited = 1;
    		CombinedStr = CombinedStr.replace("unaudited", "");
    		CombinedStr = CombinedStr.replace("()", "");
    	}
    }
}
