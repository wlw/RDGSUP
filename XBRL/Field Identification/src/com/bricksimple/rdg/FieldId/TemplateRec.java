package com.bricksimple.rdg.FieldId;

import java.sql.*;

public class TemplateRec {
    private int    UID;
    private int    Version;
    private int    Type;
    private int    BeginLine;
    private int    EndLine;
    private double Confidence;
    
    public TemplateRec() {
    	UID = 0;
    	Version = 0;
    	Type = 0;
    	BeginLine = 0;
    	EndLine = 0;
    	Confidence = 0;
    }
    
    public TemplateRec ConstructRecFromRS(ResultSet rs) {
    	TemplateRec  tr = new TemplateRec();
    	
    	try {
    	    tr.SetUID(rs.getInt(1));
    	    tr.SetVersion(rs.getInt(2));
    	    tr.SetType(rs.getInt(3));
    	    tr.SetBeginLine(rs.getInt(4));
    	    tr.SetEndLine(rs.getInt(5));
    	    tr.SetConfidence(rs.getDouble(6));
    	}
    	catch (Exception e) {
    		System.out.println("Error parsing Template record Set: " + e.getMessage());
    		tr = null;
    	}
    	return(tr);
    }
    
    public int GetUID() {
        return(UID);
    }
    
    public void SetUID(int thisUID) {
    	UID = thisUID;
    }
    
    public int GetVersion() {
    	return(Version);
    }
    
    public void SetVersion(int thisVersion) {
    	Version = thisVersion;
    }
    
    public int GetType() {
    	return (Type);
    }
    
    public void SetType(int thisType) {
    	Type = thisType;
    }
    
    public int GetBeginLine() {
    	return(BeginLine);
    }
    
    public void SetBeginLine(int thisLine)  {
    	BeginLine = thisLine;
    }
     
     public int GetEndLine() {
    	 return(EndLine);
     }
     
     public void SetEndLine(int thisEnd) {
    	 EndLine = thisEnd;
     }
    
     public void SetConfidence(double thisConfidence) {
    	 Confidence = thisConfidence;
     }
     
     public double GetConfidence() {
    	 return (Confidence);
     }
}
