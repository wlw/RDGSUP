package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class TableColumns {
    private ArrayList<ColumnDef> columns = new ArrayList<ColumnDef>();
    private ArrayList<Integer> omittedColumns = new ArrayList<Integer>(); 
 
    public void AddColumnCell(String contextRef, String sDate, String eDate, boolean bDoRange, int myFactDetailNdx) {
    	boolean   bFound = false;
    	int       iNdx = 0;
    	ColumnDef newColumn = null;
    	boolean   bDoInsert = false;
    	
    	if((bDoRange == true) && ( sDate.length() > 0) && (eDate.length() > 0))
    		bDoInsert  = true;
    	else {
    		if((bDoRange == false) && (eDate.length() == 0))
    		bDoInsert = true;
    	}
    	if(bDoInsert == true) {
    		//WLW Test code
    		boolean bFoundAColumn = false;
    	    while((iNdx < columns.size()) && (bFoundAColumn == false)) {
     	    	bFoundAColumn = columns.get(iNdx).IsThisMyColumn(contextRef, sDate, eDate, myFactDetailNdx, true, bDoRange);
    		    if(bFoundAColumn == true)
    		    	bFound = true;
    		    iNdx++;
    	    }
    	    if(bFound == false) {
    		    newColumn = new ColumnDef();
    		    newColumn.AddContextRef(contextRef, sDate, eDate, myFactDetailNdx);
    		    columns.add(newColumn);
    	    }
    	}
    }
    
    public ArrayList<ColumnDef> GetColumns() {
    	return(columns);
    }
    
    public void AddOmittedColumn(int iValue) {
    	omittedColumns.add(iValue);
    }
    
    public boolean IsColumnOmitted(int iValue) {
    	boolean bRtn = false;
    	int iCount = 0;
    	
    	while((bRtn == false) &&(iCount < omittedColumns.size())) {
    		if(iValue == omittedColumns.get(iCount))
    			bRtn = true;
    		iCount++;
    	}
    	return(bRtn);
    }
    
    public ArrayList<Integer> GetOmittedColumns() {
    	return(omittedColumns);
    }
}
