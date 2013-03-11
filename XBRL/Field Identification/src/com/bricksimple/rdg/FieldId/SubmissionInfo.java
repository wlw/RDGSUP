package com.bricksimple.rdg.FieldId;

public class SubmissionInfo {
    private int MyUid;
    private int CompanyId;
    private String ExtractFile;
    private String HtmlFile;
    private int Version;
    private int Status;
    private int State;
    
    public SubmissionInfo() {
    	MyUid = 0;
    	CompanyId = 0;
    	ExtractFile = "";
        Version = 0;
    	Status = 0;
    	State = 0;
    }
    
    public int getUid() {
        return MyUid;
}

	public void setUid(int NewUid) {
		MyUid = NewUid;
	}
	
	public int getCompanyId() {
		return CompanyId;
	}
	
	public void setCompanyId(int myCompany) {
		CompanyId = myCompany;
	}
	public String getExtractFile() {
	    return(ExtractFile);
    }
	
	public void setExtractFile(String myFile) {
		ExtractFile = myFile;
	}
	
	public String getHtmlFile() {
		return(HtmlFile);
	}
	
	public void setHtmlFile(String htmlFile) {
		HtmlFile = htmlFile;
	}
	
    public int getVersion() {
    	return Version;
    }
    
    public void setVersion(int NewExtVer) {
    	Version = NewExtVer;
    }

   public int getStatus() {
    	return Status;
    }

   public void setStatus(int NewStatus) {
       Status = NewStatus;
   }

    public int getState() {
    	return State;
    }
    public void setState(int NewState) {
    	State = NewState;
    }

}
