package com.bricksimpe.rdg.templateId;

public class TOC_STRINGS {
    private String Toc_String;
    private int    Toc_ID;
    private Double Toc_Confidence;
    private boolean Toc_DoMatch;
    
    TOC_STRINGS() {
    	Toc_String = "";
    	Toc_ID = 0;
    	Toc_Confidence = 0.0;
    	Toc_DoMatch = false;
    }
    
    public String getTocString() {
        return( Toc_String);
    }
    
    public void setTocString(String TocString) {
    	Toc_String = TocString;
    }
    
    public int getTocID() {
    	return(Toc_ID);
    }
    
    public void setTocID(int iTocID) {
    	Toc_ID = iTocID;
    }
    
    public double getTocConfidence() {
        return(Toc_Confidence);
    }
    
    public void setTocConfidence(double TocConfidence) {
    	Toc_Confidence = TocConfidence;
    }

    public boolean getTocDoMatch() {
        return(Toc_DoMatch);
    }
    
    public void setTocDoMatch(boolean TocDoMatch) {
    	Toc_DoMatch = TocDoMatch;
    }

}
