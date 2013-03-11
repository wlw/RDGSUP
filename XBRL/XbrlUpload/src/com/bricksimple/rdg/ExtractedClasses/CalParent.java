package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;

public class CalParent {
	private String Local = "";
    private String Uri = "";
    private boolean Mapped = false;
    private ArrayList<CalChild> children = new ArrayList<CalChild>();
    private ArrayList<Integer> CoreCellUid = new ArrayList<Integer>();
    
    
    public void SetLocal(String iValue) {
    	Local = iValue;
    }
    
    public String GetLocal() {
    	return(Local);
    }
    
    public void SetUri(String iValue) {
    	Uri = iValue;
    }
    
    public String GetUri() {
    	return(Uri);
    }
    
    public void SetMapped(boolean iValue) {
    	Mapped = iValue;
    }
    
    public boolean GetMapped() {
    	return(Mapped);
    }
    public void SetCoreCellUid(int iValue) {
    	CoreCellUid.add(iValue);
    }
    
    public ArrayList<Integer> GetCoreCellUid() {
    	return(CoreCellUid);
    }
    
    public int GetThisCellUid(int indx) {
    	return(CoreCellUid.get(indx));
    }
    
    public CalChild GetThisChild(int indx) {
    	return(children.get(indx));
    }
    
    public void InsertChild(CalChild thisChild) {
    	if(children.size() == 0) 
    		children.add(thisChild);
    	else {
    		int i = children.size() -1;
    		boolean bInsert = false;
    		while((children.get(i).GetOrder() > thisChild.GetOrder()) && (i > 0)) {
    				i--;
    				bInsert = true;
    		}
    		if(bInsert == false) // It's an append
    			children.add(thisChild);
    		else {
    			children.add(i, thisChild);
    		}
    	}
    }
    
    public ArrayList<CalChild> GetChildren() {
    	return(children);
    }
}
