package com.bricksimple.rdg.ExtractedClasses;

public class XmlLink {
    private String LocalTo;
    private String LocalFrom;
    private String UriTo;
    private String UriFrom;
    
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

}
