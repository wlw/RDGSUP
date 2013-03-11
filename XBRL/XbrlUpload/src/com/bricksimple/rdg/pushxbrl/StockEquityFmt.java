package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;
import java.sql.Connection;
import com.bricksimple.rdg.ExtractedClasses.Period;

public class StockEquityFmt {
    private ArrayList<EquityColumn> equityColumns = new ArrayList<EquityColumn>();
    private ArrayList<UnsortedRow> unsortedRows = new ArrayList<UnsortedRow>();
    
    
    public void AddColumn(String dimension, String contextRef) {
    	boolean      bFound = false;
    	EquityColumn newEc = null;
    	
    	for(EquityColumn ec: equityColumns) {
    		if(ec.GetDimension().equals(dimension)) {
    			bFound = true;
    			newEc = ec;
    		}
    	}
    	if(bFound == false) {
    		newEc = new EquityColumn();
    		newEc.AddDimension(dimension);
    		newEc.AddContextRef(contextRef);
    		equityColumns.add(newEc);
    	}
    	else
    		newEc.AddContextRef(contextRef);
    }
    
    public ArrayList<EquityColumn> GetEquityColumns() {
    	return(equityColumns);
    }
    
    public ArrayList<UnsortedRow> GetUnsortedRows() {
    	return(unsortedRows);
    }
    
    public void AddUnsortedRow(Connection con, FactDetail fd, Period period, String lineItem,
    		                   String unitRef) {
    	UnsortedRow ur = new UnsortedRow();
    	
    	ur.SetDateStr(con, period.GetStartDate(), period.GetEndDate(), period.GetInstant());
    	ur.SetGroupDetail(fd.GetGroupNdx(), fd.GetItemNdx());
    	ur.SetUnitRef(unitRef);
    	ur.SetLineItem(lineItem);
    	unsortedRows.add(ur);
    }
    
    public void InsertNewRow(Connection con, FactDetail fd, Period period, int iRow, String lineItem,
    		                 String unitRef) {
    	UnsortedRow ur = new UnsortedRow();
    	
    	ur.SetDateStr(con, period.GetStartDate(), period.GetEndDate(), period.GetInstant());
    	ur.SetGroupDetail(fd.GetGroupNdx(), fd.GetItemNdx());
    	ur.SetUnitRef(unitRef);
    	ur.SetLineItem(lineItem);
    	unsortedRows.add(iRow, ur);
    }
    
    public void AddToUnsortedRow(FactDetail fd, int iRow) {
    	
    	unsortedRows.get(iRow).SetGroupDetail(fd.GetGroupNdx(), fd.GetItemNdx());
    }
    
    public int FindDateRefViaDim(String iValue) {
    	int     iRtn = 0;
    	int     i = 0;
    	
    	while((iRtn == 0) && (i < equityColumns.size())) {
    		iRtn = equityColumns.get(i).IsDateRefViaContext(iValue);
    	    i++;
    	}
    	return(iRtn);
    }
}
