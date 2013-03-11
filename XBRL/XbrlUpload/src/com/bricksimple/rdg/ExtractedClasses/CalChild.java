package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;


public class CalChild {
    private String             Local;
    private String             Uri;
    private float              Order = 0;
    private float              Weight = 1;
    private boolean            Mapped = false;
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
    public void SetOrder(String iValue) {
    	Order = Float.parseFloat(iValue);
    }
    
    public float GetOrder() {
    	return(Order);
    }
    
    public void SetWeight(String iValue) {
    	Weight = Float.parseFloat(iValue);
    }
    
    public float Getweight() {
    	return(Weight);
    }

    // inserts the cell uid into list
    public void SetCoreCellUid(int iValue, int colPos) {
    	int i = CoreCellUid.size();
    	
    	//check if MT slots 
    	if(i < colPos) {
    		//create MT slots
    	    while(i < colPos)  {
    		    CoreCellUid.add(0);
    		    i++;
    	    }
    	    //append value
    	    CoreCellUid.add(iValue);
    	}
    	// insert into position already allocated
    	else {
    		if(i == colPos)
    			CoreCellUid.add(iValue);
    		else
    		    CoreCellUid.set(colPos, iValue);
    	}
    }
    
    public ArrayList<Integer> GetCoreCellUid() {
    	return(CoreCellUid);
    }
    
    public int GetThisCoreCellUid(int indx) {
    	int iRtn;
    	
    	// we check in case last entry was not supplied
    	if(indx < CoreCellUid.size())
    		iRtn =  CoreCellUid.get(indx);
    	else
    		iRtn = 0;
    	return(iRtn);
    }
}
