package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

public class Template7Col {
    private boolean bIdentified;
    private String  DbText;
    private String  DateRefTxt;
    private int     iNumWordsRem;   // remaining words 
    private ArrayList<String> words;
    
    public void Template7Col() {
    	bIdentified = false;
    	DbText = "";
    	DateRefTxt = "";
    	words = new ArrayList<String>();
    }
    
    
    public void SetDateRefTxt(String dateTxt) {
    	DateRefTxt = dateTxt;
    }
    
    public String GetDateRefTxt() {
    	return(DateRefTxt);
    }
    
    public void SetIdentified(boolean bState) {
    	bIdentified = bState;
    }
    
    public boolean GetIdentified() {
    	return(bIdentified);
    }
    
    public int GetNumWordsRem() {
    	return (iNumWordsRem);
    }
    
    public String GetMyWords() {
    	return(DbText);
    }
    
    public void SetDbText(String dbText) {
    	String  aWord = "";
     	int     iNumWords = 0;
     	int     iLen;
     	
     	words = new ArrayList<String>();
     	String  tempText = dbText;
     	tempText = tempText.trim();   // remove leading and trailing spaces
       	DbText = tempText;
 	    while(tempText.length() > 0) {
 	    	iLen = tempText.indexOf(" ");
 	    	iNumWords++;   // count the word
 	    	if(iLen == -1) { // no spaces just a word left
 	    		words.add(tempText);
 	    		tempText = "";    // empty
 	    		iNumWordsRem = iNumWords;  // save number of words
 	    	}
 	    	else {
 	    		aWord = tempText.substring(0, iLen);
 	    		words.add(aWord);
 	    		tempText = tempText.substring(iLen);
 	    		tempText = tempText.trim();
 	    	}
 	    }	
    }
    
    /**********************************************************/
    /* this function will check if the word exists. If not    */
    /* returns false else it will remove the word from the    */
    /* remaining word list and decrement the remaining count  */
    /* NOTE: if you call and all words already removed/found  */
    /* function will return false                             */
    /**********************************************************/
    public boolean IsWord(String thisWord) {
    	boolean bRtn = false;
    	boolean bContinue = true;
    	int     iWordIndex = 0;
    	String  lowerCased = thisWord.toLowerCase();
    	
    	if(iNumWordsRem > 0) {
    	    while(bContinue) {
    		    if(lowerCased.equals(words.get(iWordIndex))) { // yup found it
    		    	bRtn = true;
    		    	bContinue = false;
    		    	iNumWordsRem--;
    		    	words.remove(iWordIndex);
    		    }
    		    else {
    		    	iWordIndex++;
    		    	if(iWordIndex == words.size())
    		    		bContinue = false;
    		    }
    	    }
    	}
    	return(bRtn);
    }
}
