package com.bricksimpe.rdg.templateId;

import java.io.*;

public class UserDefineRefs {
    private int iCurLine = 0;
    private int iBeginLine;
    private int iEndLine;
    
    public void setCurLine(int iValue) {
    	iCurLine = iValue;
    }
    
    public int getCurLine() {
    	return(iCurLine);
    }
    
    public void setBeginLine (int iValue) {
    	iBeginLine = iValue;
    }
    
    public int getBeginLine() {
    	return(iBeginLine);
    }

    public void setEndLine (int iValue) {
    	iEndLine = iValue;
    }
    
    public int getEndLine() {
    	return(iEndLine);
    }
    
    public void FindUserDefinedSection(BufferedReader br, String MatchStr) {
    	boolean bFound = false;
    	String  curLine = "";
    	boolean bLookingFor = true;
    	
    	if(iCurLine == -1)
    		bFound = true;
    	try {
    	    while(bFound == false) {
    	        curLine = br.readLine();
    	        iCurLine++;
    	        if(curLine == null) {
    	    	    iBeginLine = -1;
    	    	    iEndLine  = -1;
    	    	    iCurLine = -1;
    	    	    bFound = true;
    	        }
    	        else {
    	        	if(curLine.length() > 0) {
	        	        //curLine = curLine.toLowerCase();
    	        	    if(bLookingFor == true) {
    	        	        if(curLine.contains(MatchStr)) {
    	        	    	    iBeginLine = iCurLine + 1;
    	        	    	    bLookingFor = false;
    	        	        }
    	        	    }
    	        	    else {
    	        		    if(curLine.equals("</rdgtemplate>")) {
    	        		    	iEndLine = iCurLine -1;
    	        		    	bFound = true;
    	        		    }
    	        	    }
    	        	}
    	        }
    	    }
    	}
    	catch (Exception e) {
    		iEndLine = -1;
    		iBeginLine = -1;
    	}
    }
    
	public boolean FindFinancialSchedule(BufferedReader br, int lineNumber) {
		boolean bRtn = false;
		String  strLine = "";
		int     state = 0;
		
		try {
			while(strLine != null) {
		        strLine = br.readLine();
                lineNumber++;
                switch (state) {
                    case 0:
                    	if(strLine.contains(CONSTANTS.FinancialScheduleBegin)) {
                    		state++;
                    		iBeginLine = lineNumber;
                    	}
                	    break;
                	    
                    case 1:
                    	if(strLine.contains(CONSTANTS.FinancialScheduleEnd)) {
                    		state++;
                    		iEndLine = lineNumber;
                    		bRtn = true;
                    	}
                	    break;
                	    
                	default:  // do nothing
                		break;
                }
			}
		}
		catch (Exception e) {
			if(bRtn == false)
			    bRtn = false;
		}
		return(bRtn);
	}

}
