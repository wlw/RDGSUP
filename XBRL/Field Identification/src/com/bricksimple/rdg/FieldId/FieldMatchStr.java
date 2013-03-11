package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

public class FieldMatchStr {
	private int        Uid;
    private String    FieldStr;
    private double    Threshold;
	private String    PathToData;
	private boolean   bFieldModifier;
	private boolean   bWordMatch;
	private boolean   bAbstract;
	private int       destination = 0;
	
	public  ArrayList<String> Al;

    public void FieldIdStr(int InUid, String TempStr, String TempPath, double dThreshold,
    		               String FieldModifierStr) {

    	ConfidenceLevel      confidenceLevel = new ConfidenceLevel();
    	
         Uid = InUid;
         FieldStr = TempStr;
         PathToData = TempPath;
         Threshold = dThreshold;
         bWordMatch = false;
         if(FieldModifierStr.equals("Y"))
        	 bFieldModifier = true;
         else
        	 bFieldModifier = false;
         Al = confidenceLevel.RecurringSting(TempStr);
    }

    public void setWordMatch(boolean wordMatch) {
    	bWordMatch = wordMatch;
    }
    
    public boolean getWordMatch() {
    	return(bWordMatch);
    }
    
    public int getUid() {
        return( Uid);
    }

    public void setUid(int InUid) {
        Uid = InUid;
    }
    
    public String getFieldStr() {
    	return(FieldStr);
    }
    
    public void setFieldStr(String tempStr) {
    	FieldStr = tempStr;
    }
    
    public String getPathToData() {
    	return(PathToData);
    }
    
    public void setPathToData(String tempPath) {
    	PathToData = tempPath;
    }

    public double getThreshold() {
    	return(Threshold);
    }
    
    public void setThreshold(double dThreshold) {
    	Threshold = dThreshold;
    }

    public boolean getFieldModifier() {
    	return(bFieldModifier);
    }
    
    public void setFieldModifier(String FieldModifierStr) {
    	if(FieldModifierStr.equals("Y"))
    		bFieldModifier = true;
    	else
    		bFieldModifier = false;
    }

  public ArrayList<String> getAl() {
        return( Al);
    }
    
    public void setAl(String InTemplateStr) {
    	ConfidenceLevel confidenceLevel = new ConfidenceLevel();
    	
    	Al = confidenceLevel.RecurringSting(InTemplateStr);
    }
    
    public void setAbstract(boolean bIn) {
    	bAbstract = bIn;
    }
    
    public boolean getAbstract() {
    	return(bAbstract);
    }

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getDestination() {
		return destination;
	}
}
