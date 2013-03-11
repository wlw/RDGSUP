package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class SectionRowDef {
    private ArrayList<Integer> RowFacts = new ArrayList<Integer>();
    private boolean            Omitted = false;
    
    public void SetRowFact(int iValue) {
    	RowFacts.add(iValue);
    }
    
    public ArrayList<Integer> GetRowFact() {
    	return(RowFacts);
    }
    
    public void SetOmitted(boolean bValue) {
    	Omitted = bValue;
    }
    
    public boolean GetOmitted() {
    	return(Omitted);
    }
    
    /*
    public void SetRowsOmitted(ArrayList<ColumnDef> columnDefs) {
    	boolean bOmitted = true;
    	int     i = 0;
    	int     j;
    	
    	while(i < RowFacts.size()) {
    		j = 0;
    		while(j < RowFacts.get(i).GetColumnDefNdx().size()) {
      			if(columnDefs.get(RowFacts.get(i).GetColumnDefNdx().get(j)).GetOmitted() ==  false)
    				bOmitted = false;
    			j++;
    		}
    		Omitted = bOmitted;
    		i++;
    	}
    }
    */
}
