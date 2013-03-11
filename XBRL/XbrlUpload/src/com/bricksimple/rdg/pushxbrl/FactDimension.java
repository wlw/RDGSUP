package com.bricksimple.rdg.pushxbrl;

public class FactDimension {
    private String Dimension;
    private int    FieldsRowStr = 0;
    
    public void SetDimension(String iValue) {
    	Dimension = iValue;
    }
    
    public String GetDimension() {
    	return(Dimension);
    }
    
    public void SetFieldsRowStr(int iValue) {
    	FieldsRowStr = iValue;
    }
    
    public int GetFieldsRowStr() {
    	return(FieldsRowStr);
    }
}
