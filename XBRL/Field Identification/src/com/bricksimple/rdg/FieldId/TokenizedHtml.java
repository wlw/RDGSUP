package com.bricksimple.rdg.FieldId;

public class TokenizedHtml {
    private int    iTokenType;
    private String strToken;
    
    public void SetTokenType(int iValue) {
    	iTokenType = iValue;
    }
    
    public int GetTokenType() {
    	return (iTokenType);
    }
    
    public void SetToken(String iValue) {
    	strToken = iValue;
    }
    
    public String GetToken() {
    	return(strToken);
    }
    
    public void AppendToken(String iValue) {
    	if(strToken.length() > 0)
    		strToken += " ";
    	strToken += iValue;
    }
}
