package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class ItemDetailInfo {
    private String Group = "";
    private String Detail = "";
    private String LineItem = "";
    private ArrayList<FactDetail> factDetails = new ArrayList<FactDetail>();
    
    public void SetGroup(String iValue) {
    	Group = iValue;
    }
    
    public String GetGroup() {
    	return(Group);
    }
    
    public void SetDetail(String iValue) {
    	Detail = iValue;
    }
    
    public String GetDetail() {
    	return(Detail);
    }
    
    public void SetLineItem(String iValue) {
    	LineItem = iValue;
    }
    
    public String GetLineItem() {
    	return(LineItem);
    }
    
    public void SetFactDetail(FactDetail fd) {
    	factDetails.add(fd);
    }
    
    public ArrayList<FactDetail> GetFactDetails() {
    	return(factDetails);
    }
}
