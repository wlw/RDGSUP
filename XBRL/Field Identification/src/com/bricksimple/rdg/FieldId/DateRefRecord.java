package com.bricksimple.rdg.FieldId;

public class DateRefRecord {
    private int Uid;
    private String DateStr;
    private String SupText;
    
    public void SetUid(int value) {
    	Uid = value;
    }
    
    public int GetUid() {
    	return(Uid);
    }
    
    public void SetDateStr(String value) {
    	DateStr = value;
    }
    
    public String GetDateStr() {
    	return (DateStr);
    }
    
    public void SetSupText(String value) {
    	SupText = value;
    }
    
    public String GetSupText() {
    	return (SupText);
    }
    
    public boolean CompareDateRef (DateRefRecord checkRec) {
    	boolean bRtn = false;
    	
    	//System.out.println(checkRec.GetDateStr());
    	if(SupText.trim().equals(checkRec.GetSupText().trim())) {
    		if(DateStr.trim().equals(checkRec.GetDateStr().trim()))
    			bRtn = true;
    		else {
    			String str1 = DateStr.replace(" ", "");
    			String str2 = checkRec.GetDateStr().replace(" ", "");
    			if(str1.equals(str2))
    				bRtn = true;
    		}
    	}
    	return(bRtn);
    }
}
