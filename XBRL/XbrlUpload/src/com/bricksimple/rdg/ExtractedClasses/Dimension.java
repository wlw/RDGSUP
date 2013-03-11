package com.bricksimple.rdg.ExtractedClasses;

public class Dimension {
    private  String Dim;
    private  String Data;
    private  int Uid = 0;
    
    public void SetDim(String iValue) {
    	Dim = iValue;
    }
    
    public String GetDim() {
    	return(Dim);
    }

    public void SetData(String iValue) {
    	Data = iValue;
    }
    
    public String GetData() {
    	return(Data);
    }
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }
}
