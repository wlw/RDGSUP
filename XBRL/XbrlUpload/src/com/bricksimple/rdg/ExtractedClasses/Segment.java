package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;

public class Segment {
    private String Dimension = "";
    private String Data = "";
    
    public void PrintSegment(PrintWriter out) {
    	out.println("         SEGMENT:");
    	out.println("             Dimension: " + Dimension);
    	out.println("             Data: " + Data);
    }
    
    public void SetDimension(String iValue) {
    	Dimension = iValue;
    }
    
    public String GetDimension() {
    	return(Dimension);
    }

    public void SetData(String iValue) {
    	Data = iValue;
    }
    
    public String GetData() {
    	return(Data);
    }
}
