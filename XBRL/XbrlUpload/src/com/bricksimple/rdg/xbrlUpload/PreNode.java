package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

public class PreNode {
    private String Id = "";
    private float    Order = 0;
    private ArrayList<PreNode> children = new ArrayList<PreNode>();
    private boolean IsParenthetical = false;
    
    public void SetId(String iValue) {
    	Id = iValue;
    }
    
    public String GetId() {
    	return(Id);
    }
    
    public void SetOrder(float iValue) {
    	Order = iValue;
    }
    
    public float GetOrder() {
    	return(Order);
    }
    
    public void addNode(PreNode child) {
    	children.add(child);
    }
    
    public ArrayList<PreNode> GetChildren() {
    	return(children);
    }
    
    public void SetIsParenthetical(boolean iValue) {
    	IsParenthetical = true;
    }
    
    public boolean GetIsParenthetical() {
    	return(IsParenthetical);
    }
    
    public void AddThisNode(int i, PreNode child) {
    	children.add(i, child);
    }
}
