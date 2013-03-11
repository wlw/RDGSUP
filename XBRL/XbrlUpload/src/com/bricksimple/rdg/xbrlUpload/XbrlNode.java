package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

public class XbrlNode {
    private String                   Tag;
    private String                   Data = "";
    private ArrayList<NodeAttribute> Attributes = new ArrayList<NodeAttribute>();
    private ArrayList<XbrlNode>      Children = new ArrayList<XbrlNode>();
    private XbrlNode                 Sibling = null;
    
    public void SetTag(String iValue) {
    	Tag = iValue;
    }
    
    public String GetTag() {
    	String normalized = "";
    	normalized = Tag.replace("xlink:", "");
    	return(normalized);
    }

    public void SetData(String iValue) {
    	Data = iValue;
    }
    
    public String GetData() {
    	return(Data);
    }
    
    public void AddAttribute(NodeAttribute iValue) {
    	Attributes.add(iValue);
    }
    
    public ArrayList<NodeAttribute> GetAttributes() {
    	return(Attributes);
    }
    
    public void SetChild(XbrlNode child) {
    	Children.add(child);
    	return;
    }
    
    public ArrayList<XbrlNode> GetChildren() {
    	return(Children);
    }
    
    public void SetSibling(XbrlNode sibling) {
    	Sibling = sibling;
    	return ;
    }
    
    public XbrlNode GetSibling() {
    	return(Sibling);
    }
}
