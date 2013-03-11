package com.bricksimple.rdg.pushxbrl;

import com.bricksimple.rdg.ExtractedClasses.PreExtract;
import java.sql.Connection;
import com.bricksimple.rdg.sqlaccess.MySqlAccess;
import com.bricksimple.rdg.xbrlUpload.FilingInfo;

public class SubmissionInfo {
    private String CompanyName = "";
    private int    CompanyUid = 0;
    private int    SubmissionUid = 0;
    private int    Version = 1;
    private String FormId = "";
    private String SrcFile = "";
    private String ExchangeSymbol = "";
    private String FullSrcFile = "";
    
    
    public void SubmitXbrl(Connection con) {
    	MySqlAccess mySqlAccess = new MySqlAccess();
    	SubmissionUid = mySqlAccess.WriteSubmission(con, CompanyUid, CompanyName, FullSrcFile, FormId, SrcFile);
    }
    
    public void SetCompanyName(String iValue) {
    	CompanyName = iValue;
    }
    
    public String GetCompanyName() {
    	return(CompanyName);
    }
    
    public void SetCompanyUid(int iValue) {
    	CompanyUid = iValue;
    }
    
    public int GetCompanyUid() {
    	return(CompanyUid);
    }
    
    public void SetSubmissionUid(int iValue) {
    	SubmissionUid = iValue;
    }
    
    public int GetSubmissionUid() {
    	return(SubmissionUid);
    }

    public void SetVersion(int iValue) {
    	Version = iValue;
    }
    
    public int GetVersion() {
    	return(Version);
    }
    
    public boolean EstablishCompany(Connection con, PreExtract preExtract) {
    	boolean bRtn = true;
    	String CompanyName = "";
    	
    	return(bRtn);
    }
    
    public void SetFormId(String iValue) {
    	FormId = iValue;
    }
    
    public String GetFormId() {
    	return(FormId);
    }

    public void SetSrcFile(String iValue) {
    	SrcFile = iValue;
    }
    
    public String GetSrcFile() {
    	return(SrcFile);
    }

    public void SetExchangeSymbol(String iValue) {
    	ExchangeSymbol = iValue;
    }
    
    public String GetExchangeSymbol() {
    	return(ExchangeSymbol);
    }

    public void SetFullSrcFile(String iValue) {
    	FullSrcFile = iValue;
    }
    
    public String GetFullSrcFile() {
    	return(FullSrcFile);
    }

}
