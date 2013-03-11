package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;

public class Entity {
    private String  Scheme = "";
    private String  Data = "";
    private Segment segment = new Segment();
    
    public void PrintEntity(PrintWriter out) {
    	out.println("      ENTITY:");
    	out.println("        Scheme: " + Scheme);
    	out.println("        Data: " + Data);
    	segment.PrintSegment(out);
    }
    
    public void SetScheme(String iValue) {
    	Scheme = iValue;
    }
    
    public String GetScheme(){
    	return(Scheme);
    }
    
    public void SetData(String iValue) {
    	Data = iValue;
    }
    
    public String GetData(){
    	return(Data);
    }
    
   // public void SetDimension(String iValue) {
   // 	Dimension = iValue;
   // }
    
    //public String GetDimension(){
    //	return(Dimension);
    //}
    
    public Segment GetSegment() {
    	return(segment);
    }
}
