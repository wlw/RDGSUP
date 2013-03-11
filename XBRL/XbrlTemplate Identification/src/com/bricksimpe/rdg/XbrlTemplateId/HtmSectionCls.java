package com.bricksimpe.rdg.XbrlTemplateId;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;

public class HtmSectionCls {
 private String EndOfPreamble = "RDGPreambleEnd";
 private String BeginOfSection = "RDGXBRLParseBegin";
 private String EndOfSection = "RDGXBRLParseEnd";
 
 private int  iEndOfPreamble = 0;
 private int  iBeginOfSection = 0;
 private int  iEndOfSection = 0;
 
 public int GetEndOfPreamble() {
	 return(iEndOfPreamble);
 }
 
 public int GetBeginOfSection() {
	 return(iBeginOfSection);
 }
 
 public int GetEndOfSection() {
	 return(iEndOfSection);
 }
 
 public boolean FoundRDGtags() {
	 boolean bRtn = false;
	 
	 if((iEndOfPreamble != 0) &&
	    (iBeginOfSection != 0) &&
	    (iEndOfSection != 0))
		 bRtn = true;
	 return(bRtn);
 }
 
 public int FindUserInsertedTags(Connection con, String fileName) {
	 int             iRtn = 0;
	 FileInputStream fstream;
	 DataInputStream in;
	 BufferedReader  br;
	 String          strLine;
	 ErrorCls        errorCls = new ErrorCls();
	 int             iLineNum = 0;
	 int             iState = 0;
	 MySqlAccess     mySqlAccess = new MySqlAccess();
	 
     errorCls.setCompanyUid(0);
	 errorCls.setFunctionStr("Run");
	 errorCls.setItemVersion(0);
	 errorCls.setBExit(false);
	 try {
	     fstream = new FileInputStream(fileName);
	     in = new DataInputStream(fstream);
	     br = new BufferedReader(new InputStreamReader(in));
         while(((strLine = br.readLine()) != null)  && (iEndOfSection == 0)) {
        	 iLineNum++;
        	 switch (iState) {
        	 case 0:
        		 if(strLine.contains(EndOfPreamble)) {
        			 iEndOfPreamble = iLineNum;
        		     iState++;
        		 }
        		 break;
        		 
        	 case 1:
        		 if(strLine.contains(BeginOfSection)) {
        			 iBeginOfSection = iLineNum;
        		     iState++;
        		 }
       		 break;
       		 
        	 case 2:
           		 if(strLine.contains(EndOfSection)) {
        			 iEndOfSection = iLineNum;
        		     iState++;
        		 }
        		 break;
        	 }
         }
    	 br.close();
    	 in.close();
    	 fstream.close();
	 }
		catch (Exception e) {
	     	errorCls.setErrorText("Unable to parse input file:"  + e.getMessage());
	    	mySqlAccess.WriteAppError(con, errorCls);
	    	iRtn = -1;
		}
	 return(iRtn);
 }
}
