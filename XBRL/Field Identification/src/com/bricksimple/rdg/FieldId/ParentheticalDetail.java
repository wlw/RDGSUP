package com.bricksimple.rdg.FieldId;

public class ParentheticalDetail {
    private String thisParenthetical;
	private int    thisLength;
			
	public void setParentheticalStr(String inValue) {
	    thisParenthetical = inValue;
    }
	
	public String getParentheticalStr() {
		return(thisParenthetical);
	}
	
	public void setParentheticalLen(int inValue) {
		thisLength = inValue;
	}
	
	public int getParentheticalLen() {
		return(thisLength);
	}

}
