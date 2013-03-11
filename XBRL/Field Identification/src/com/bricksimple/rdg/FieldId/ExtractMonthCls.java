package com.bricksimple.rdg.FieldId;

public class ExtractMonthCls {
    public String  OrigStr = "";
    public String  ExtractedStr = "";
    public String  MonthExtracted = "";
    public int     MonthExtractedNdx = 0;
    public boolean bCompleteDate = false;
    
    public void ExtractThisMonth(String origStr, int iMonth) {
    	
    	int       iStartOfMonth = origStr.indexOf(CONSTANTS.MonthName.get(iMonth));
    	String    dayStr = "";
    	boolean   bLookingForNums = true;
    	String    testStr = "";
    	CONSTANTS constants = new CONSTANTS();
    	
    	OrigStr = origStr;
    	MonthExtracted = constants.GetDisplayMonth(iMonth);
    	ExtractedStr = origStr.substring(iStartOfMonth);
    	ExtractedStr = ExtractedStr.replace(CONSTANTS.MonthName.get(iMonth), "");
    	ExtractedStr = ExtractedStr.trim();
    	dayStr = LookingForNums();
    	if(dayStr.length() > 0) {
    		MonthExtracted += " " + dayStr;
    		//need comma for year
    		testStr = ExtractedStr.substring(0,1);
    		if(testStr.equals(",")) {
    			// yup comma - year to follow
    			ExtractedStr = ExtractedStr.substring(1).trim();
    			dayStr = LookingForNums();
    			if(dayStr.length() > 0) {
    				MonthExtracted += "," + dayStr;
    				ExtractedStr = ExtractedStr.trim();
    				bCompleteDate = true;
    				int iNdx = ExtractedStr.indexOf("and");
    				if(iNdx == 0) {
    					ExtractedStr = ExtractedStr.substring(3).trim();
    				}
    			}
    		}
    	}
    }
    
    private String LookingForNums() {
    	String    NumStr = "";
    	String    testStr = "";
    	boolean   bLookingForNums = true;
    	CONSTANTS constants = new CONSTANTS();
    	
    	while(bLookingForNums) {
    		if(ExtractedStr.length() > 0) {
    		    testStr = ExtractedStr.substring(0,1);
    		    if(constants.isNumeric(testStr) != -1) {
    			    NumStr += testStr;
    			    ExtractedStr = ExtractedStr.substring(1);
    		    }
    		    else
    			    bLookingForNums = false;
    		}
    		else
    			bLookingForNums = false;
    	}
        return(NumStr);
    }
}
