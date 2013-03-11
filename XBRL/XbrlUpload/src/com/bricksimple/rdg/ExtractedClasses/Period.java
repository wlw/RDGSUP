package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.Date;

public class Period {
    private String Instant = "";
    private String EndDate = "";
    private String StartDate = "";
    private long   duration = 0;
    private int    Uid = 0;
    private int    SupNdx = 0;
    
    public void PrintPeriod(PrintWriter out) {
    	out.println("      PERIOD:");
    	if(Instant.length() > 0)
    		out.println("         INSTANT :" + Instant);
    	else {
    		out.println("         STARTDATE :" + StartDate);
    		out.println("         ENDDATE :" + EndDate);
    	}
    }

    public void SetInstant(String iValue) {
    	Instant = iValue;
    }
    
    public String GetInstant() {
    	return(Instant);
    }
    
    public void SetEndDate(String iValue) {
    	EndDate = iValue;
    }
    
    public String GetEndDate() {
    	return(EndDate);
    }
    
    public void SetStartDate(String iValue) {
    	StartDate = iValue;
    }
    
    public String GetStartDate() {
    	return(StartDate);
    }
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);  
    }
    
    public void SetDuration(long iValue) {
    	duration = iValue;
    }
    
    public long GetDuration() {
    	return(duration);
    }
    
    public void SetSupNdx(int iValue) {
    	SupNdx = iValue;
    }
    
    public int GetSupNdx() {
    	return(SupNdx);
    }
}
