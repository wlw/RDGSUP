package com.bricksimple.rdg.FieldId;

public class ReformatedDateRefs {
	private boolean bUsed = false;
    private String  CompletedDate;
    private String  SupStr;
    private String  ExtSupStr = "";  // if found an exact match to supp str
    private String  ExtDateStr = ""; // remaining after above removed
    
    
    public void FixUpDates(TemplateHdr templateHdr, int iTemplateId, MatchStr[] DateSups) {
    	ExtSupStr = "";
    	ExtDateStr = "";
    	boolean bSetSup = false;
    	
    	if(CompletedDate.length() == 0) {  //we have work to do
    		if(templateHdr.bDate1Complete == false)  { // format is mm dd  and year is from table
    			if(templateHdr.Date1.length() > 0) {
    			    CompletedDate = templateHdr.Date1 + "," + SupStr;
    			    SupStr = templateHdr.supStr;
    			}
    			else {
    				CompletedDate = SupStr;
    				SupStr = "";
    			}
    		}
    		else {   // we have completed date in table header and indicator on table columns
    			RemoveDefinedSupStr(DateSups);
    			if(templateHdr.Date1.contains(SupStr)) 
    				CompletedDate = templateHdr.Date1;
    			else {  // check if only one date
    				if((ExtDateStr.length() > 0) && (templateHdr.Date1.contains(ExtDateStr))) {
    				    CompletedDate = templateHdr.Date1;
    				    SupStr = ExtSupStr;
    				    bSetSup = true;
    				}
    				else {
        				if((ExtDateStr.length() > 0) && (templateHdr.Date2.contains(ExtDateStr))) {
        				    CompletedDate = templateHdr.Date2;
        				    SupStr = ExtSupStr;
        				    bSetSup = true;
        				}
        				else {
    				        if(templateHdr.Date2.length() > 0)
    				            CompletedDate = templateHdr.Date2;
    				        else {
		    		            NumericCls numericCls = new NumericCls();
		    		            numericCls.ExtractNumeric(SupStr);
		    		            if(numericCls.GetNumericValue() > 2000) {  // we just got a year, so strip year off other
		    		    	        CompletedDate = templateHdr.Date1.substring(0, templateHdr.Date1.length() -4) + SupStr;
		    		            }
		    		            else
		    		    	        CompletedDate = templateHdr.Date2;  // this is the default
    				        }
        				}
    				}
    			}
    			if((iTemplateId != 7) && (bSetSup == false))
    			    SupStr = templateHdr.supStr;
    		}
    	}
    }
    
    // If this becomes an issue, will expand check
    private void RemoveDefinedSupStr(MatchStr[] DateSups) {
    	String  checkStr = "Three Months Ended";
    	
    	if((SupStr.contains(checkStr)) && (SupStr.length() > checkStr.length())) {
        	ExtSupStr = checkStr;
        	ExtDateStr = SupStr.replace(checkStr, "").trim();  		
    	}
    }
    
    public void SetCompletedDate(String inStr) {
    	CompletedDate = inStr;
    }
    
    public String GetCompletedDate() {
    	return(CompletedDate);
    }

    public void SetSupStr(String inStr) {
    	SupStr = inStr;
    }
    
    public String GetSupStr() {
    	return(SupStr);
    }
    
    public void SetUsed(boolean bValue) {
    	bUsed = bValue;
    }
    
    public boolean GetUsed() {
    	return(bUsed);
    }
}
