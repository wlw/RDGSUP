package com.bricksimple.rdg.pushxbrl;

public class GroupDetailNdx {
    private int GroupNdx = 0;
    private int DetailNdx = 0;
    
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
    
    public void InitGroupDetailNdx(int iGroup, int iDetail) {
    	GroupNdx = iGroup;
    	DetailNdx = iDetail;
    }
}
