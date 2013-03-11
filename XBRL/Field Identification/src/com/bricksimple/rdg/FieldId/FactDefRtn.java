package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

public class FactDefRtn {
    private int     Status = 0;
    private String  DetailStr = "";
    private int     MatchNdx; 
    private boolean PatternMatch = false;
    
    public void SetStatus(int iValue) {
    	Status = iValue;
    }
    
    public int GetStatus() {
    	return(Status);
    }
    
    public void SetDetailStr(int iStatus, String iValue) {
    	DetailStr = iValue;
    	Status = iStatus;
    }
    
    public String GetDetailStr() {
    	return(DetailStr);
    }
    
    public void SetMatchNdx(int iValue) {
    	MatchNdx = iValue;
    }
    
    public int GetMatchNdx() {
    	return(MatchNdx);
    }
    
    public void SetPatternMatch(boolean bValue) {
    	PatternMatch = bValue;
    }
    
    public boolean GetPatternMatch() {
    	return(PatternMatch);
    }
    
    public void CheckForAppend(String word, ArrayList<DefedFacts> DefdWords) {
    	int     i;
    	String  match;
    	String  extract;
    	boolean bFound = false;
    	int     appendNdx;
    	int     result;
    	
    	if(MatchNdx >= 0) {
    	    if(DefdWords.get(MatchNdx).GetAppendable() == 2) {
    		    match = DefdWords.get(MatchNdx).GetFact() + "-";
    		    i = word.indexOf(match);
    		    if(i == 0) {
    			    extract = word.substring(match.length());
    			    while((bFound == false) && (i < DefdWords.size())) {
    				    if(DefdWords.get(i).GetAppendable() == 1) {
    				        appendNdx = extract.indexOf(DefdWords.get(i).GetFact())	;
    				        if(appendNdx == 0) {
    				    	    bFound = true;
    				        }
     				    }
    				    if(bFound == false)
    					    i++;
    			    }
    			    if(bFound) {
    			        result = Integer.parseInt(DefdWords.get(MatchNdx).GetSubstitution());
    			        result += Integer.parseInt(DefdWords.get(i).GetSubstitution());
    			        DetailStr = Integer.toString(result);
    			    }
    			}
    		}
    	}
    }
}
