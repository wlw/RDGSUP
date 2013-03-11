package com.bricksimple.rdg.FieldId;

public class NumericCls {
    private int iValue = -1;
    private String strValue = "";
    
    public void ExtractNumeric(String testStr) {
    	
		int iIndex;
		String Temp;
		
		try {
			iIndex = testStr.indexOf(" ");
			if(iIndex == -1)
				Temp = testStr;
			else
				Temp = testStr.substring(0,iIndex);
			iValue = Integer.parseInt(Temp);
			strValue = Temp;
		}
		catch(Exception e) {
			iValue = -1;
		    return;
		}
		return;
    }
    
    public int GetNumericValue() {
    	return(iValue);
    }
    
    public String GetNumericStr() {
    	return(strValue);
    }
}
