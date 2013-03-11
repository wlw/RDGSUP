package com.bricksimple.rdg.FieldId;

public class HtmlReplacementLines {
    private int     htmlLineNumber;
    private String  factWord;
    private int     factUid;
    private int     previousWordCount;  
    private int     wordCount;
    private int     iMatchType;
    private int     iFactSentenceId;
    private boolean bIgnore = false;
    
    public void Populate(int lineNum, String fact, int fUid, int prevCount, int count,
    		             int matchType, int sentenceUid) {
    	htmlLineNumber = lineNum;
    	factWord = fact;
    	factUid = fUid;
    	previousWordCount = prevCount;
    	wordCount = count;
    	iMatchType = matchType;
    	iFactSentenceId = sentenceUid;
    }
    
    public int GetHtmlLineNumber() {
    	return(htmlLineNumber);
    }
    
    public String GetFact() {
    	return(factWord);
    }
    
    public int GetFactUid() {
    	return(factUid);
    }
    
    public int GetSectionIndex() {
    	return(previousWordCount + wordCount);
    }
    
    public int GetWordIndex() {
    	return(wordCount);
    }
    
    public int GetMatchType() {
    	return(iMatchType);
    }
    
    public int GetFactSentenceUid() {
    	return (iFactSentenceId);
    }
    
    public void SetIgnore() {
    	bIgnore = true;
    }
    
    public boolean GetIgnore() {
    	return(bIgnore);
    }
}
