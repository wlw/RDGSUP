package com.bricksimple.rdg.ExtractedClasses;

import com.bricksimple.rdg.xbrlUpload.CONSTANTS;

public class UnitLink {
    private int UnitType = CONSTANTS.UNIT_MEASURE;
    private String Id = "";
    private String LocalMeasure = "";
    private String LocalNumerator = "";
    private String LocalDenominator = "";
    private String UriMeasure = "";
    private String UriNumerator = "";
    private String UriDenominator = "";
    private int    CoreUnitsUid = 0;
    
    public void SetUnitType(int iValue) {
    	UnitType = iValue;
    }
    public int GetUnitType(){
    	return(UnitType);
    }
    
    public void SetId(String iValue) {
    	Id = iValue;
    }
    
    public String GetId() {
    	return(Id);
    }
    
    public void SetLocalMeasure(String iValue) {
    	LocalMeasure = iValue;
    }
    
    public String GetLocalMeasure() {
    	return(LocalMeasure);
    }

    public void SetUriMeasure(String iValue) {
    	UriMeasure = iValue;
    }
    
    public String GetUriMeasure() {
    	return(UriMeasure);
    }
    
    public void SetLocalNumerator(String iValue) {
    	LocalNumerator = iValue;
    }
    
    public String GetLocalNumerator() {
    	return(LocalNumerator);
    }

    public void SetUriNumerator(String iValue) {
    	UriNumerator = iValue;
    }
    
    public String GetUriNumerator() {
    	return(UriNumerator);
    }
    
    public void SetLocalDenominator(String iValue) {
    	LocalDenominator = iValue;
    }
    
    public String GetLocalDenominator() {
    	return(LocalDenominator);
    }

    public void SetUriDenominator(String iValue) {
    	UriDenominator = iValue;
    }
    
    public String GetUriDenominator() {
    	return(UriDenominator);
    }

    public void SetCoreUnitsUid(int iValue) {
    	CoreUnitsUid = iValue;
    }
    
    public int GetCoreUnitsUid() {
    	return(CoreUnitsUid);
    }
}
