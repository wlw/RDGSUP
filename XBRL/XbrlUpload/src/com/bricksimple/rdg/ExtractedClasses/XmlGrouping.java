package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;

public class XmlGrouping {
    private String Prefix = "";
    private ArrayList<XmlDetail> xmldetails = new ArrayList<XmlDetail>();
    
    public void SetPrefix(String iValue) {
    	Prefix = iValue;
    }
    
    public String GetPrefix() {
    	return(Prefix);
    }
    
    public void AddDetail(XmlDetail iValue) {
    	xmldetails.add(iValue);
    }
    
    public ArrayList<XmlDetail> GetDetails() {
    	return(xmldetails);
    }
    
    public XmlDetail GetThisDetail(int iValue) {
    	return(xmldetails.get(iValue));
    }
}
