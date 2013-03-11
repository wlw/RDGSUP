package com.bricksimple.rdg.ExtractedClasses;

public class CalLink {
    private String LocalTo;
    private String LocalFrom;
    private String UriTo;
    private String UriFrom;
    private int    Order = 1;
    private int    Weight = 1;
    
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

    public void SetOrder(String iValue) {
    	Order = Integer.parseInt(iValue);
    }

    public int GetOrder() {
    	return(Order);
    }
    
    public void SetWeight(String iValue) {
    	Weight = Integer.parseInt(iValue);
    }
    
    public int GetWeight() {
    	return(Weight);
    }
}
