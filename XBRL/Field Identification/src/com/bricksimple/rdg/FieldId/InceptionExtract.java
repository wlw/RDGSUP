package com.bricksimple.rdg.FieldId;

public class InceptionExtract {
    public String inceptionDate;
    public String finalDate;
    public String remainingStr;
    
    public void FindDates(String completedDate, String SupStr, int iInceptionTagLen) {
    	inceptionDate  = "";
    	finalDate = "";
    	remainingStr = SupStr.toLowerCase();
    	
    	inceptionDate = Extractdate(iInceptionTagLen);
        if(completedDate.length() > 0)
        	finalDate = completedDate;
        else
        	finalDate = Extractdate(iInceptionTagLen);
    }
    
    /* DATE FORMAT month day,Year */
    
    private String Extractdate(int iInceptionTagLen) {
    	String    extractedStr = "";
    	int       iBeginningNdx;
    	String    workingStr;
    	CONSTANTS constants = new CONSTANTS();
    	
    	if(constants.DoesStrContainWithinMonth(remainingStr) == true) {
    		int iMonth = constants.GetFirstMonthContained(remainingStr);
    		if(iMonth != -1) {
    			iBeginningNdx = remainingStr.indexOf(CONSTANTS.MonthName.get(iMonth));
    			workingStr = remainingStr.substring(iBeginningNdx);
    			if(iBeginningNdx > 0)
    			    remainingStr = remainingStr.substring(0, iBeginningNdx -1) + "X" + remainingStr.substring(iBeginningNdx + 1);
    			iBeginningNdx = workingStr.indexOf(",");
    			if(workingStr.length() < (iBeginningNdx + 6)) // it's not all there
    				extractedStr = workingStr;
    			else {
    			    workingStr = workingStr.substring(0, iBeginningNdx + 6);  // this should be it
    			    extractedStr = workingStr.substring(0,1).toUpperCase() + workingStr.substring(1);
    			}
    		}
    	}
    	return(extractedStr);
    }
}
