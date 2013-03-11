package com.bricksimple.rdg.ExtractedClasses;

public class LabelLink {

    private String LocalTo = "";
    private String LocalFrom = "";
    private String UriTo = "";
    private String UriFrom = "";
    private String Data = "";
    private String XbrlRole = "";
    private String XmlRole = "";
    
    
    public void Clone(LabelLink src) {
    	LocalTo = src.GetLocalTo();
    	LocalFrom = src.GetLocalFrom();
    	UriTo = src.GetUriTo();
    	UriFrom = src.GetUriFrom();
    	Data = src.GetData();
    	XbrlRole = src.GetXbrlRole();
    	XmlRole = src.GetXmlRole();
    }
    
    public void SetLocalTo(String iValue) {
   	     LocalTo = iValue;
    }
    
    public String GetLocalTo() {
   	    return(LocalTo);
    }
    
    public void SetLocalFrom(String iValue) {
   	    LocalFrom = iValue;
    }
    
    public String GetLocalFrom() {
   	    return(LocalFrom);
    }

    public void SetUriTo(String iValue) {
   	    UriTo = iValue;
    }
    
    public String GetUriTo() {
   	    return(UriTo);
    }

    public void SetUriFrom(String iValue) {
   	    UriFrom = iValue;
    }
    
    public String GetUriFrom() {
   	    return(UriFrom);
    }

    public void SetData(String iValue) {
        Data = iValue;
    }
       
    public String GetData() {
        return(Data);
    }

    public void SetXbrlRole(String iValue) {
        XbrlRole = iValue;
    }
       
    public String GetXbrlRole() {
        return(XbrlRole);
    }

    public void SetXmlRole(String iValue) {
        XmlRole = iValue;
    }
       
    public String GetXmlRole() {
        return(XmlRole);
    }
}
