package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;

import com.bricksimple.rdg.xbrlUpload.*;

public class XmlDetail {
    private String  Tag = "";
    private String  ContextRef = "";
    private String  Decimals = "";
    private String  UnitRef = "";
    private String  Data = "";
    private String  Dimension = "";
    private boolean Nil = false;
    private int Mapped  = CONSTANTS.NOT_MAPPED;
    
    public void printXmlDetail(PrintWriter out) {
    	out.println("      Tag:" + Tag);
    	out.println("      ContextRef: " + ContextRef);
    	out.println("      Decimals: " + Decimals);
    	out.println("      UnitRef: " + UnitRef);
    	out.println("      Data: " + Data);
    	out.println("      Dimension: " + Dimension);
    	out.println("");
   }

    public void SetTag(String iValue) {
    	Tag = iValue;
    }
    
    public String GetTag() {
    	return(Tag);
    }
    
    public void SetContextRef(String iValue) {
    	ContextRef = iValue;
    }
    
    public String GetContextRef() {
    	return(ContextRef);
    }
    
    public void SetDecimals(String iValue) {
    	Decimals = iValue;
    }
    
    public String GetDecimals() {
    	return(Decimals);
    }
    
    public void SetUnitRef(String iValue) {
    	UnitRef = iValue;
    }
    
    public String GetUnitRef() {
    	return(UnitRef);
    }
    
    public void SetData(String iValue) {
    	Data = iValue;
    }
    
    public String GetData() {
    	return(Data);
    }

    public void SetMapped(int bValue) {
    	Mapped = bValue;
    }
    
    public int GetMapped() {
    	return(Mapped);
    }
    
    public void SetDimension(String iValue) {
    	Dimension = iValue;
    }
    
    public String GetDimension() {
    	return(Dimension);
    }
    
    public boolean IsThisMyDimension(String iValue) {
    	boolean bRtn = iValue.equals(Dimension);
    	return(bRtn);
    }
    
    public void SetNil(boolean bValue) {
    	Nil = bValue;
    }
    
    public boolean GetNil() {
    	return(Nil);
    }
    
    private String CONTEXTREF = "contextref";
    private String DECIMALS   = "decimals";
    private String UNITREF    = "unitref";
    private String NIL        = "nil";
    
    public String ConstructNode(XbrlNode node, int prefixLen, int iType) {
    	String workStr = "";
    	String rtnStr = "";
    	UTILITIES utils = new UTILITIES();
    	
    	workStr = node.GetTag();
    	Data = node.GetData();
    	switch (iType) {
	        case CONSTANTS.DEI_DETAIL:
		        Tag = workStr.substring(CONSTANTS.DEI_TAG.length()); 
		        break;
		
	        case CONSTANTS.GROUPING_DETAIL:
	        	int i = workStr.indexOf(":");
	        	if(i == -1) {
	        		Tag = workStr;
	        	}
	        	else {
	        	    Tag = workStr.substring(i+ 1);
	        	    rtnStr = workStr.substring(0,i);
	        	}
	        //case CONSTANTS.GAAP_DETAIL:
	        //	Tag = workStr.substring(CONSTANTS.GAAP_TAG.length()); 
		    //    break;
		        
	        //case CONSTANTS.PREFIX_DETAIL:
	        //	Tag = workStr.substring(prefixLen); 
		    //    break;
    	
    	}
    	for(NodeAttribute attr: node.GetAttributes()) {
    		workStr =  attr.GetName().toLowerCase();
    		if(workStr.equals(CONTEXTREF))
    			ContextRef = attr.GetValue();
    		else {
    			if(workStr.equals(DECIMALS))
    				Decimals = attr.GetValue();
    			else {
    				if(workStr.equals(UNITREF))
    					UnitRef = attr.GetValue();
    				else {
    					if(utils.XsMatch(workStr, NIL))
    						Nil = true;
    				}
    			}
    		}
    	}
        return(rtnStr);
    }
}
