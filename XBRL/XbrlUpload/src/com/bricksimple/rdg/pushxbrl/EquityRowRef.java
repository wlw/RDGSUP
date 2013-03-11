package com.bricksimple.rdg.pushxbrl;

public class EquityRowRef {
    private String RowId = "";
    private String RowData = "";
    
    public void SetRowId(String iValue) {
    	RowId = iValue;
    }
    
    public String GetRowId() {
    	return(RowId);
    }
    
    public void SetRowData(String iValue) {
    	RowData = iValue;
    }
    
    public String GetRowData() {
    	return(RowData);
    }
}
