package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class TableCell {

	private int     GroupNdx;
	private int     DetailNdx;
	private int     Column = 0;
	
	
    public void SetGroupNdx(int iValue) {
    	GroupNdx = iValue;
    }
    
    public int GetGroupNdx() {
    	return(GroupNdx);
    }

    public void SetDetailNdx(int iValue) {
    	DetailNdx = iValue;
    }
    
    public int GetDetailNdx() {
    	return(DetailNdx);
    }

    public void SetColumn(int iValue) {
    	Column = iValue;
    }
    
    public int GetColumn() {
    	return(Column);
    }
    
    public boolean IsOmitted(ArrayList<FactDetail> factDetails) {
    	boolean bRtn = false;
    	boolean bFound = false;
    	int     iFactNdx = 0;
    	
    	if(factDetails != null) {
    	    while((bFound == false) && (iFactNdx < factDetails.size())) {
    		    if((GroupNdx == factDetails.get(iFactNdx).GetGroupNdx()) &&
    			    	(DetailNdx == factDetails.get(iFactNdx).GetItemNdx())) {
    			    bFound= true;
    			    bRtn= factDetails.get(iFactNdx).GetOmit();
    		    }
    		    iFactNdx++;
    	    }
    	}
    	
    	return(bRtn);
    }
    
}
