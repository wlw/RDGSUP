package com.bricksimple.rdg.FieldId;


public class DateDeConstruct {
    private String origStr;
    private int    month;
    private int    day;
    private int    year;
    
    public void DeConstruct() {
    	String    sMonth = "";
    	String    sDay = "";
    	String    Temp = origStr;
    	int       iIndex;
    	String    sYear = "";
    	int       iYear;
    	CONSTANTS constants = new CONSTANTS();
    	
    	month = constants.ConvertMonthStringToNum(origStr);
      	Temp = constants.RemoveMonth(origStr);
    	iIndex = Temp.indexOf(",");
    	sDay = Temp.substring(0, iIndex).trim();
    	day = Integer.parseInt(sDay);
    	sYear = Temp.substring(iIndex+ 1);
    	sYear = sYear.trim();
    	year = Integer.parseInt(sYear);
    	if(year < 2000)
    		year += 2000;
    }
    
    public String PriorMonths(int iNumMonths) {
    	String    rtnStr = "";
    	int       iTempMonth; 
    	int       iTempYear;
    	CONSTANTS constants = new CONSTANTS();
    	
		iTempMonth = month - (iNumMonths -1);
		iTempYear = year;
		if(iTempMonth < 0) {
			iTempMonth += 12;
			iTempYear -= 1;
		}
		String sYear = "" + iTempYear;
		String sMonth = constants.getMonthStr(iTempMonth);
        rtnStr = sMonth + " 01," + sYear;    	
    	return (rtnStr);
    }
    
	public void setOrigStr(String origStr) {
		this.origStr = origStr;
	}
	public String getOrigStr() {
		return origStr;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getMonth() {
		return month;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getDay() {
		return day;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getYear() {
		return year;
	}
    
}
