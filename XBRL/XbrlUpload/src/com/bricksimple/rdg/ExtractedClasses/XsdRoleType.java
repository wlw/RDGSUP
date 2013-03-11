
package com.bricksimple.rdg.ExtractedClasses;

public class XsdRoleType {
    private String Id;
    private String RoleUri;
    private String Data;
    private boolean IsParenthetical = false;

	public void SetId(String iValue) {
		Id = iValue;
	}
	
	public String GetId() {
		return(Id);
	}

	public void SetRoleUri(String iValue) {
		RoleUri = iValue;
	}
	
	public String GetRoleUri() {
		return(RoleUri);
	}

	public void SetData(String iValue) {
		Data = iValue;
	}
	
	public String GetData() {
		return(Data);
	}
	
	public void SetIsParenthetical(boolean iValue) {
		IsParenthetical = iValue;
	}
	
	public boolean GetIsParenthetical() {
		return(IsParenthetical);
	}
}
