package com.bricksimple.rdg.FieldId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SupplementCls {

	public SupplementRtn ContainsSupplementStr(String DataStr, MatchStr[] matches) {
		SupplementRtn     rtnCls = new SupplementRtn();
        boolean           bMatchFound = false;
        String            toLower = "";
        int               i = 0;
        ArrayList<String> matchedStrs = new ArrayList<String>();
        
        rtnCls.setiRtn(-1);
        while(i < matches.length) {
        	toLower = matches[i].OrigString.toLowerCase();
        	if(DataStr.contains(toLower)) {
        		bMatchFound = true;
        		rtnCls.setSupplementStr(matches[i].OrigString);
        		DataStr = DataStr.replace(toLower, "");
        	}
        	else
        	    i++;
        }
        if(bMatchFound) {
        	rtnCls.setRemainingStr(DataStr);
        }
		return(rtnCls);
	}
	public SupplementRtn IsSupplementStr(String DataStr, MatchStr[] matches, ArrayList<String> dateForms) {
		SupplementRtn rtnCls = new SupplementRtn();
		
		int             iRtn = -1;
		int             i = 0;
		double          dMatch;
		ConfidenceLevel cl = new ConfidenceLevel();
		double          dMaxMatch = 0;
		int             dMaxIndex = -1;
		String          testStr = DataStr.toLowerCase();
		double          maxMatch = 0;
		int             maxMatchIndex = -1;
		CONSTANTS       constants = new CONSTANTS();
		boolean         bFoundForPeriod  = false;
		
		rtnCls.setiRtn(-1);
		while(i < matches.length) {
			dMatch = cl.compareToArrayList(testStr, matches[i].al, matches[i].OrigString);
			if(dMatch > maxMatch) {
				maxMatch = dMatch;
				maxMatchIndex = i;
			}
			if((testStr.indexOf("for the period") == 0) && (rtnCls.getSupplementStr().length() == 0)) {
				rtnCls.setSupplementStr(DataStr);
				bFoundForPeriod = true;
			}
			else {
			    if((dMatch >= matches[i].dConfidence) && ( dMatch > dMaxMatch)) {
				    rtnCls.setSupplementStr(matches[i].OrigString);
				    dMaxMatch = dMatch;
				    if(matches[i].OrigString.length() < DataStr.length()) {
					    String TempStr = DataStr.substring(matches[i].OrigString.length()).trim();
					    if(CompleteDate(TempStr, dateForms))
					    	rtnCls.setCompleteDateStr(TempStr);
					    else {
					        if(IsPartialDate(TempStr))
						        rtnCls.setPartialDateStr(TempStr);
					        else
						        rtnCls.setSupplementStr(rtnCls.getSupplementStr() + TempStr);
					    }
				    }
				    rtnCls.setiRtn(i);
			    }
			}
			i++;
		}
		if(rtnCls.getiRtn() == -1) {  //second chance
			if(maxMatch > 0.5) {
				if(constants.MonthInString(testStr) == true) { // if month here - ya gotta believe
					rtnCls.setSupplementStr(matches[maxMatchIndex].OrigString);
					//String TempStr = DataStr.substring(matches[maxMatchIndex].OrigString.length()).trim();
					String TempStr  = "";
					if(testStr.contains("unaudited")) {
						TempStr = DataStr.replace("Unaudited", "");
						TempStr = TempStr.replace("unaudited", "");
						TempStr = TempStr.replace("()", "");
					}
					else {
					    if(testStr.contains("audited")) {
						    TempStr = DataStr.replace("Audited", "");
						    TempStr = TempStr.replace("audited", "");
						    TempStr = TempStr.replace("()", "");
					    }
					    else
					    	TempStr = testStr;
					}
					TempStr = TempStr.toLowerCase();
					TempStr = TempStr.replace(matches[maxMatchIndex].OrigString.toLowerCase(), "").trim();
					int iMonth = constants.GetFirstMonthContained(TempStr);
					int iMonthIndex = constants.GetBeginIndexOfMonth(TempStr, iMonth); //TempStr.indexOf(CONSTANTS.MonthName.get(iMonth));
					TempStr = TempStr.substring(iMonthIndex);
					// we look here for another month - if yes, just return the supplement string 
			        // as this must be the header of the table
					int iLength = constants.GetLengthOfMonth(TempStr, iMonth);
					TempStr = TempStr.substring(iLength);                      //CONSTANTS.MonthName.get(iMonth).length());
					if(constants.MonthInString(TempStr) == false) {
						String LookForPeriod = TempStr.substring(0,1);
						if(LookForPeriod.equals("."))  //remove the period
							TempStr = TempStr.substring(1);
						else {
							if(LookForPeriod.equals(" ") == false)
								TempStr = " " + TempStr;
						}
					    TempStr = CONSTANTS.MonthName.get(iMonth).substring(0,1).toUpperCase() +
						          CONSTANTS.MonthName.get(iMonth).substring(1) + TempStr;                
					    if(CompleteDate(TempStr, dateForms))
						    rtnCls.setCompleteDateStr(TempStr.trim());
					    else {
					        if(IsPartialDate(TempStr))
						        rtnCls.setPartialDateStr(TempStr);
					        else
						        rtnCls.setSupplementStr(rtnCls.getSupplementStr() + TempStr);
					    }
					}
					rtnCls.setiRtn(maxMatchIndex);  // moved this to inside so greater than .5 MUST be date only was matching on 'common'
				}
				else {  // got close so maybe thousands and millions and billions
					if((testStr.contains("thousands")) || (testStr.contains("millions")) || (testStr.contains("billions")))
						rtnCls.setiRtn(0);
				}
			}
		}
		if((bFoundForPeriod == true) && (rtnCls.getCompleteDateStr().length() == 0) && 
		   (rtnCls.getPartialDateStr().length() == 0))
			rtnCls.setSupplementStr(DataStr);
		return(rtnCls);
	}

	private boolean IsPartialDate(String origStr) {
		boolean   bRtn = false;
		String    TestStr;
		CONSTANTS constants = new CONSTANTS();
		
		if(constants.DoesStrContainMonth(origStr) == true) { // still in the running
			String MonthStr = origStr.substring(0, origStr.indexOf(" ")) + " ";
			String RemStr = origStr.substring(origStr.indexOf(" ")).trim();
			if(RemStr.length() == 0)  // if there is more here - more than partial date
				bRtn = true;  // just month
			else {       // now check for month with day
				String TempStr = "";
				boolean bFoundComma = false;
				for(int i = 0; i < RemStr.length(); i++) {
					TempStr = RemStr.substring(i, i + 1);
					if(TempStr.equals(","))
						bFoundComma = true;
					else {
						if(constants.isNumeric(TempStr) != -1)
						    bRtn = true;
					}
				}
			}
		}		
		return (bRtn);
	}

	public boolean CompleteDate(String dateStr, ArrayList<String> DateForms) {
		boolean bRtn  = CompleteDate1(dateStr);
		if(bRtn == false) {
			int i = 0;
			while((bRtn == false) && (i < DateForms.size())) {
			    bRtn = CheckDateForm(dateStr, DateForms.get(i));
			    i++;
			}
		}
		return(bRtn);
	}
	
	private boolean CheckDateForm(String dateStr, String dateForm ) {
		boolean bRtn = true;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateForm);
		try {
			Date convertedDate = dateFormat.parse(dateStr);
		}
		catch (Exception e) {
			bRtn = false;
		}
		return(bRtn);
		
	}
	private boolean CompleteDate1(String dateStr) {
		boolean bRtn = true;
		//SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
		DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
		
		try {
			Date convertedDate = format.parse(dateStr);
		}
		catch (Exception e) {
			bRtn = false;
		}
		return(bRtn);
	}
	
}
