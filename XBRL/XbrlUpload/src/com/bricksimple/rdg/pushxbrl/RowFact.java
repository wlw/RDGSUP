package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class RowFact {
    private int factDetailNdx;
    private ArrayList<Integer> ColumnDefNdxs = new ArrayList<Integer>();
    
    public void SetFactDetail(int iValue) {
    	factDetailNdx = iValue;
    }
    
    public int GetFactDetail() {
    	return(factDetailNdx);
    }
    
    public void SetColumnDefNdx(int iValue) {
    	ColumnDefNdxs.add(iValue);
    }
    
    public ArrayList<Integer> GetColumnDefNdx() {
    	return(ColumnDefNdxs);
    }
}
