package com.bricksimpe.rdg.templateId;

import java.io.BufferedReader;

public class UserDefinedTemplate {

	public boolean IsUserDefinedTemplate(String origStr) {
		boolean bRtn = false;
		bRtn = origStr.contains(CONSTANTS.UserDefinedStartTemplate);
		return(bRtn);
	}
	
	public int GetUserDefinedTemplateId(String origStr) {
		int    iRtn = 0;
		int    iIndx = origStr.indexOf(CONSTANTS.UserDefinedIdTag);
		String TempStr = origStr.substring(iIndx+CONSTANTS.UserDefinedIdTag.length());
		
		iIndx = TempStr.indexOf('"');
		TempStr = TempStr.substring(0, iIndx);
		iRtn = Integer.parseInt(TempStr);
		return(iRtn);
	}
	
	public String GetUserDefinedDisplayName(String origStr) {
		String rtnStr = origStr;
	
		int i = origStr.indexOf(CONSTANTS.UserDefinedDisplayTag);
		if(i != -1) {
			rtnStr = rtnStr.substring(i + CONSTANTS.UserDefinedDisplayTag.length());
			i = rtnStr.indexOf('"');
			rtnStr = rtnStr.substring(0, i);
		}
		return(rtnStr);
	}
	
	public int GetNoteIndex(String origStr) {
		
		String tempStr = origStr;
		int    iRtn = 0;
		
		int i = origStr.indexOf(CONSTANTS.UserDefinedNoteTag);
		if(i != -1) {
			tempStr = tempStr.substring(i + CONSTANTS.UserDefinedNoteTag.length());
			i = tempStr.indexOf('"');
			tempStr = tempStr.substring(0, i);
			iRtn = Integer.valueOf(tempStr);
		}
		return(iRtn);
	}
	
	public int FindLastLineOfTemplate(BufferedReader br, int CurLine ) {
		int    iRtn = CurLine;
		String thisLine = "";
		
		try {
		    while(thisLine.contains(CONSTANTS.UserDefinedEndTemplate) == false) {
	            iRtn++;
	            thisLine = br.readLine();
		    }
		}
		catch (Exception e) {
		    ErrorCls errorCls = new ErrorCls();	

		    errorCls.setCompanyUid(0);
		    errorCls.setFunctionStr("UserDefinedTemplate");
		    errorCls.setItemVersion(0);
		    errorCls.setBExit(false);
		    errorCls.setErrorText("Reading the output file");
		}
		return(iRtn);
	}
}
