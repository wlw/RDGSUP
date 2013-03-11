package com.bricksimple.rdg.ExtractedClasses;

import com.bricksimple.rdg.xbrlUpload.*;
import java.io.PrintWriter;
/******
public class XmlContext {
    private String Id = "";
    private Entity entity = new Entity();
    private Period period = new Period();
    private int    Uid    = 0;
    
    
    public String GetId() {
    	return(Id);
    }
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }
    
    public Entity GetEntity() {
    	return(entity);
    }
    
    public Period GetPeriod() {
    	return(period);
    }
    
    public void PrintXmlContext(PrintWriter out) {
    
        out.println("   ID: " + Id);
        entity.PrintEntity(out);
        period.PrintPeriod(out);
    }
    
    public void PopulateContext(XbrlNode node) {
       	String workStr = "";
    	
    	for(NodeAttribute attr: node.GetAttributes()) {
    		workStr =  attr.GetName().toLowerCase();
    		if(workStr.equals("id"))
    			Id = attr.GetValue();
    	}
	    for(XbrlNode child: node.GetChildren()) {
	    	workStr = child.GetTag().toLowerCase();
            int iRtn = DetermineType(workStr);
            switch (iRtn) {
            case 1:  // entity
            	PopulateEntity(child);
            	break;
            	
            case 2:  //period
            	PopulatePeriod(child);
            	break;
            }
	    }
    }
    
    private void PopulateEntity(XbrlNode node) {
    	String workStr = "";
    	
    	for(XbrlNode child: node.GetChildren()) {
    	    workStr = child.GetTag().toLowerCase();
    	    int iRtn = DetermineEntityChild(workStr);
   	        switch (iRtn) {
    	        case CONSTANTS.ENTITY_ID:
    		    	for(NodeAttribute attr: child.GetAttributes()) {
    		    		workStr =  attr.GetName().toLowerCase();
   	    		        if(workStr.equals("scheme"))
    	    			    entity.SetData(child.GetData());
    		    	}
    	    	    break;
    	    	
    	        case CONSTANTS.ENTITY_SEGMENT:
    	            for (XbrlNode grandChild: child.GetChildren()) {
    	        	    //workStr = grandChild.GetName().toLowerCase();
    	            	entity.GetSegment().SetData(grandChild.GetData());
    	            	for(NodeAttribute attr: grandChild.GetAttributes()) {
    	            	    workStr = attr.GetName().toLowerCase();	
    	        		    if(workStr.equals("dimension")) {
    	        		        entity.GetSegment().SetDimension(attr.GetValue());   
    	        		    }
    	        		}
    	        	}
  	    		    if(workStr.equals("dimension"))
   	    	   break;
   	    	}
    	}
    }
    
    private void PopulatePeriod(XbrlNode node) {
        String workStr = "";
        
    	for(XbrlNode child: node.GetChildren()) {
    	    workStr = child.GetTag().toLowerCase();
    	    int iRtn = DeterminePeriodChild(workStr);
   	        switch (iRtn) {
   	            case CONSTANTS.PERIOD_STARTDATE:
   	            	period.SetStartDate(child.GetData());
   	        	    break;
   	        	
   	            case CONSTANTS.PERIOD_ENDDATE:
   	            	period.SetEndDate(child.GetData());
   	        	    break;
   	        	
   	            case CONSTANTS.PERIOD_INSTANT:
   	            	period.SetInstant(child.GetData());
   	        	    break;
   	        }
    	}
    }
    	
    private int DetermineEntityChild(String workStr) {
        int iRtn = 0;
        UTILITIES utilitites = new UTILITIES();
        
        if(utilitites.XbrlMatch(workStr, "identifier"))
        	iRtn = CONSTANTS.ENTITY_ID;
        else {
            if(utilitites.XbrlMatch(workStr, "segment"))
            	iRtn = CONSTANTS.ENTITY_SEGMENT;
        }
        return(iRtn);
    }
    
    private int DeterminePeriodChild(String workStr) {
        int iRtn = 0;
        UTILITIES utilitites = new UTILITIES();
        
        if(utilitites.XbrlMatch(workStr, "startdate"))
        	iRtn = CONSTANTS.PERIOD_STARTDATE;
        else {
            if(utilitites.XbrlMatch(workStr, "enddate"))
            	iRtn = CONSTANTS.PERIOD_ENDDATE;
            else {
                if(utilitites.XbrlMatch(workStr, "instant"))
                	iRtn = CONSTANTS.PERIOD_INSTANT;
           }
        }
        return(iRtn);
    }
    
    private int DetermineType(String workStr) {
    	int   iRtn = 0;
    	
    	if(workStr.equals("entity") ||
    			(workStr.equals("xbrli:entity")))
    		iRtn = 1;
    	else {
        	if(workStr.equals("period") ||
        			(workStr.equals("xbrli:period")))
        		iRtn = 2;
    	}
    	return(iRtn);
    }
}
************/