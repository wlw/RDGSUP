package com.bricksimpe.rdg.templateId;


public class SavedCheckForTemplate {
    private int    iIdLine;
    private int    LastMatchIndex;
    private String MatchedText;
    private double MaxMatchValue;
    private int    BeginLineNum;
    
    public  void SaveCheckForTemplate(CheckForTemplateStr inStruct) {
    	iIdLine = inStruct.GetIdLine();
    	LastMatchIndex = inStruct.GetLastMatchIndex();
    	MatchedText = inStruct.GetMatchedText();
    	MaxMatchValue = inStruct.GetMaxMatchValue();
    	BeginLineNum = inStruct.GetBeginLineNum();
    }
    
    public int GetIdLine() {
    	return(iIdLine);
    }
    
    public int GetLastMatchIndex() {
    	return(LastMatchIndex);
    }
    
    public String GetMatchedText() {
    	return(MatchedText);
    }
    
    public double GetMaxMatchValue() {
    	return(MaxMatchValue);
    }
    
    public int GetBeginLineNum() {
    	return(BeginLineNum);
    }
}
