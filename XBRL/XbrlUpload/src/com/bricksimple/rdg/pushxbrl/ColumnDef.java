package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.GregorianCalendar;
//import java.sql.Timestamp;
//import java.util.Date;

public class ColumnDef {
    private ArrayList<String>  ContextRefs = new ArrayList<String>();
    private ArrayList<Integer> FactDetailNdxs = new ArrayList<Integer>();
    private String             startDate = "";
    private String             endDate = "";
    private boolean            Omitted = false;
  
    public void AddContextRef(String contextRef, String sDate, String eDate, int myFactDetailNdx) {

    	ContextRefs.add(contextRef);
    	startDate = sDate;
    	endDate = eDate;
    	FactDetailNdxs.add(myFactDetailNdx);
    }
    
    public boolean IsThisMyColumn(String contextRef, String sDate, String eDate, int myFactDetailNdx, 
    		                      boolean bCheckDates, boolean bDoRange) {
    	boolean bFound = false;
    	int     i = 0;
    	String  Temp = "";
    	
    	try {
    	    while((i < ContextRefs.size()) && (bFound == false)) {
    		    if(contextRef.equals(ContextRefs.get(i))) {
    			    bFound = true;
    			    FactDetailNdxs.add(myFactDetailNdx);
    		    }
    		    i++;
    	    }
    	    if(bCheckDates == true) {
    	        if(bFound == false) {
    	    	    if(bDoRange == false) {  // was true
    		            if(startDate.equals(sDate)) {
    			            if(eDate.length() > 0) {
    				            if(endDate.equals(eDate)) 
    					            bFound = true;
   				            }
    			            else 
    				            bFound = true;
    		            }
    	    	    }
    	    	    else {  // check instant date against the range
     	    			// on instants the sDate is a previous day so I add a day
    	    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	    			Calendar c = Calendar.getInstance();    	    			
    	    			c.setTime(sdf.parse(sDate));
    	    			c.add(Calendar.DATE, 1);
    	    			Temp = sdf.format(c.getTime());
    	    		    if(startDate.equals(Temp))
    	    			    bFound = true;
    	    		    else {
    	    			    if(endDate.equals(sDate)) // or check the end date
    	    				    bFound = true;
    	    		    }
    	    	    }
    		        if(bFound == true) {
    			        FactDetailNdxs.add(myFactDetailNdx);
    			        ContextRefs.add(contextRef);
    		        }
    	        }
    	    }
    	}
    	catch (Exception e) {  // if we fail (sdf.parse) assume no match
    		bFound = false;
    	}
    	return(bFound);
    }
    
    
    public ArrayList<String> GetContextRef() {
    	return(ContextRefs);
    }
    
    public void SetDates(String sDate, String eDate) {
    	startDate = sDate;
    	endDate = eDate;
    }
    
    public ArrayList<Integer> GetFacts() {
    	return(FactDetailNdxs);
    }
    
    public void SetOmitted(boolean bValue) {
    	Omitted = bValue;
    }
    
    public boolean GetOmitted() {
    	return(Omitted);
    }
    
 }
