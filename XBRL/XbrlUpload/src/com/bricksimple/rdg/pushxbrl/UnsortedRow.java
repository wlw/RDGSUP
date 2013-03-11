package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;
import java.sql.Connection;
import com.bricksimple.rdg.xbrlUpload.UTILITIES;

import com.bricksimple.rdg.xbrlUpload.UTILITIES;

public class UnsortedRow {
    private boolean                   InstantDateType;
    private String                    InstantDateStr = "";
    private int                       InstantDateInt = 0;
    private String                    StartDateStr = "";
    private int                       StartDateInt = 0;
    private String                    EndDateStr = "";
    private int                       EndDateInt = 0;
    private String                    NextDay = ""; 
    private int                       NextDayInt = 0;
    private String                    LineItem = "";
    private String                    UnitRef = "";
    private ArrayList<GroupDetailNdx> RowDetails = new ArrayList<GroupDetailNdx>();
    
    
    public String GetInstantDateStr() {
    	return(InstantDateStr);
    }
    
    public String GetEndDateStr() {
    	return(EndDateStr);
    }
    
    public String GetStartDateStr() {
    	return(StartDateStr);
    }
    
    public boolean GetInstantDateType() {
    	return(InstantDateType);
    }
    
    public int GetInstantDateInt() {
    	return(InstantDateInt);
    }
    
    public int GetStartDateInt() {
    	return(StartDateInt);
    }
    
    public int GetNextDayInt() {
    	return(NextDayInt);
    }
    
    public void SetLineItem(String iValue) {
    	LineItem = iValue;
    }
    
    public String GetLineItem() {
    	return(LineItem);
    }
    
    public void SetDateStr(Connection con, String startDate, String endDate, String instantDate) {
    	UTILITIES utilities = new UTILITIES();

    	if(instantDate.length() > 0) {
    		InstantDateType = true;
    		InstantDateStr = instantDate;
    		InstantDateInt = Integer.parseInt(instantDate.replace("-", ""));
    		NextDay = utilities.AddADay(con, instantDate);
    		NextDayInt = Integer.parseInt(NextDay.replace("-", ""));
   	}
    	else {
       		InstantDateType = false;
    	    StartDateStr = startDate;
    	    EndDateStr = endDate;
    	    StartDateInt = Integer.parseInt(startDate.replace("-", ""));
    	    EndDateInt = Integer.parseInt(endDate.replace("-", ""));
    	}
    }
    
    public void SetGroupDetail(int iGroupNdx, int iDetailNdx) {
    	GroupDetailNdx  gdn = new GroupDetailNdx();
    	gdn.SetGroupNdx(iGroupNdx);
    	gdn.SetDetailNdx(iDetailNdx);
    	RowDetails.add(gdn);
    }
    
    public ArrayList<GroupDetailNdx> GetRowDetails() {
    	return(RowDetails);
    }
    
    public void SetUnitRef(String iValue) {
    	UnitRef = iValue;
    }
    
    public String GetUnitRef() {
    	return(UnitRef);
    }
    /************************************************************/
    /* returns:                                                 */
    /*          -1 if new object date is less than this ones    */
    /*           0 if dates the same                            */
    /*           1 if this object date is greater than this one */
    /************************************************************/
    public int IsThisMyRow(String startDate, String endDate, String instantDate) {
    	int   iRtn = 1;
    	
    	if(instantDate.length() > 0) {
    		if(InstantDateStr.length() > 0) {
    		    if(instantDate.equals(InstantDateStr)) {
    		    	iRtn = 0;
    		    }
    		    else {
    		    	if(Integer.parseInt(instantDate.replace("-", "")) < InstantDateInt)
    		    		iRtn = -1;
    		    }
    	    }
    	}
    	else {
    		if(startDate.equals(StartDateStr)) {
    			iRtn = 0;
    		}
    		else {
    			if(Integer.parseInt(startDate.replace("-", "")) < StartDateInt)
    				iRtn = -1;
    		}
    	}
    	return(iRtn);
    }
}
