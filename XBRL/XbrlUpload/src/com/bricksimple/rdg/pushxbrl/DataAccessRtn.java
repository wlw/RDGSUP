package com.bricksimple.rdg.pushxbrl;


public class DataAccessRtn {
    private int     Uid;
    private String  StrValue;
    private int     Success;
    private int     TaxonomyUid = 0;
    
    public static final int SUCCESS = 0;
    public static final int ERROR   = -1;
    public static final int NOT_FOUND = 1;
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }

    public void SetStrValue(String iValue) {
    	StrValue = iValue;
    }
    
    public String GetStrValue() {
    	return(StrValue);
    }

    public void SetSuccess(int iValue) {
    	Success = iValue;
    }
    
    public int GetSuccess() {
    	return(Success);
    }
    
    public void SetTaxonomyUid(int iValue) {
    	TaxonomyUid = iValue;
    }
    
    public int GetTaxonomyUid() {
    	return(TaxonomyUid);
    }
}
