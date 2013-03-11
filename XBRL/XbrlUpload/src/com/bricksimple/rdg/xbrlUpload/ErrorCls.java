package com.bricksimple.rdg.xbrlUpload;

public class ErrorCls {
    private int     CompanyUid;
    private int     SubUid;
    private int     ItemVersion;
    private String  FunctionStr;
    private String  ErrorText;
    
	public void setCompanyUid(int companyUid) {
		CompanyUid = companyUid;
	}
	public int getCompanyUid() {
		return CompanyUid;
	}
	public void setSubUid(int subUid) {
		SubUid = subUid;
	}
	public int getSubUid() {
		return SubUid;
	}
	public void setItemVersion(int itemVersion) {
		ItemVersion = itemVersion;
	}
	public int getItemVersion() {
		return ItemVersion;
	}
	public void setFunctionStr(String functionStr) {
		FunctionStr = functionStr;
	}
	public String getFunctionStr() {
		return FunctionStr;
	}
	public void setErrorText(String errorText) {
		ErrorText = errorText;
	}
	public String getErrorText() {
		return ErrorText;
	}

}
