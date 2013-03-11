package com.bricksimple.rdg.pushxbrl;

public class Stock {
    private String Dimension = "";
    private String Stock = "";
    private String Concept = "";
    private int    DimUid = 1;
    
    
    public void SetDimension(String iValue) {
    	Dimension = iValue;
    }
    
    public String GetDimension() {
    	return(Dimension);
    }
    
    
    public void SetStock(String iValue) {
    	Stock = iValue;
    }
    
    public String GetStock() {
    	return(Stock);
    }
    
    public void SetConcept(String iValue) {
    	Concept = iValue;
    }
    
    public String GetConcept() {
    	return(Concept);
    }
 
    public void SetDimUid(int iValue) {
    	DimUid = iValue;
    }
    
    public int GetDimUid() {
    	return(DimUid);
    }
}
