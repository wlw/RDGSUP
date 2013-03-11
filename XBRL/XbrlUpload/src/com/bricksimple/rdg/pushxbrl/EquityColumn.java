package com.bricksimple.rdg.pushxbrl;
import java.util.ArrayList;

public class EquityColumn {
	private String Dimension = "";
	private ArrayList<String> ContextRef = new ArrayList<String>();
	private int    DateRef = 0;
	
	public void AddDimension(String iValue) {
		Dimension = iValue;
	}
	
	public String GetDimension() {
		return(Dimension);
	}
	
	public void SetDateRef(int iValue) {
		DateRef = iValue;
	}
	
	public int GetDateRef() {
		return(DateRef);
	}
	
	public void AddContextRef(String iValue) {
		boolean bFound = false;
		
		for(String contextRef: ContextRef) {
			if(contextRef.equals(iValue))
				bFound = true;
		}
		if(bFound == false)
		    ContextRef.add(iValue);
	}
	
	public int IsThisMyContextRef(String contextRef) {
		int iRtn = 0;
		
		for(String thisContextRef: ContextRef) {
			if(contextRef.equals(thisContextRef))
				iRtn = DateRef;
		}
		return(iRtn);
	}
	
	public ArrayList<String> GetContextRef() {
		return(ContextRef);
	}
	
	public int IsDateRefViaContext(String iValue) {
		int iRtn = 0;
		
		for(String contextRef: ContextRef) {
		    if(iValue.equals(contextRef))
			    iRtn  = DateRef;
		}
		return(iRtn);
	}
}


