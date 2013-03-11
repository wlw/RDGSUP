package com.bricksimple.rdg.FieldId;

public class UnMappedRowCls {
    private int     iTaggedFieldUid;
    private int     iFieldsRowUid;
    private int     iSpareJiffy;
    private boolean bAddedParen = false;
    
    public void setTaggedFieldUid (int newValue) {
    	iTaggedFieldUid = newValue;
    }
    
    public int getTaggedFieldUid() {
    	return(iTaggedFieldUid);
    }

    public void setFieldsRowUid (int newValue) {
    	iFieldsRowUid = newValue;
    }
    
    public int getFieldsRowUid() {
    	return(iFieldsRowUid);
    }
    
    public void setSpareJiffy(int newValue) {
    	iSpareJiffy = newValue;
    }
    
    public int getSpareJiffy() {
    	return(iSpareJiffy);
    }
    
    public void SetAddedParen(boolean bValue) {
    	bAddedParen= bValue;
    }
    
    public boolean GetAddedParen() {
    	return(bAddedParen);
    }
}
