package com.bricksimple.rdg.ExtractedClasses;

public class PreLink {
    private String  LocalTo;
    private String  LocalFrom;
    private String  UriTo;
    private String  UriFrom;
    private String  Role = "";
    private float   Order;
    private int     RoleUid = 0;
    private boolean Negated = false;
    
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
    	Order = Float.valueOf(iValue);
    }
    
    public float GetOrder() {
    	return(Order);
    }
    
    public void SetRole(String iValue) {
    	Role = iValue;
    }
    
    public String GetRole() {
    	return(Role);
    }
    
    public void SetRoleUid(int iValue) {
    	RoleUid = iValue;
    }
    
    public int GetRoleUid() {
    	return(RoleUid);
    }
    
    public void SetNegated(boolean  bValue) {
    	Negated = bValue;
    }
    
    public boolean GetNegated() {
    	return(Negated);
    }
}
