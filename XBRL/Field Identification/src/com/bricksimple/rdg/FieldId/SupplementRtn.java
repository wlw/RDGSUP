package com.bricksimple.rdg.FieldId;

public class SupplementRtn {
    private int    iRtn;
    private String SupplementStr = "";
    private String PartialDateStr = "";
    private String CompleteDateStr = "";
    private String RemainingStr = "";
    
	public void setiRtn(int iRtn) {
		this.iRtn = iRtn;
	}
	public int getiRtn() {
		return iRtn;
	}
	public void setSupplementStr(String supplementStr) {
		SupplementStr = supplementStr;
	}
	public String getSupplementStr() {
		return SupplementStr;
	}
	public void setPartialDateStr(String partialDateStr) {
		PartialDateStr = partialDateStr;
	}
	public String getPartialDateStr() {
		return PartialDateStr;
	}
	public void setCompleteDateStr(String completeDateStr) {
		CompleteDateStr = completeDateStr;
	}
	public String getCompleteDateStr() {
		return CompleteDateStr;
	}
    
	public String getRemainingStr() {
		return(RemainingStr);
	}
	
	public void setRemainingStr(String rmStr) {
		RemainingStr = rmStr;
	}
}
