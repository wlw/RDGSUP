package com.bricksimpe.rdg.templateId;

public class SubmissionInfo {
    private int MyUid;
    private int MyCompanyId;
    private String CompanyName;
    private String CompanyNameLc;
    private int MyForm;
    private int Version;
    private int Status;
    private int State;
    
    public SubmissionInfo() {
    	MyUid = 0;
        Version = 0;
    	Status = 0;
    	State = 0;
    	CompanyName = "";
    	CompanyNameLc = "";
    }
    
    public int getUid() {
        return MyUid;
}

	public void setUid(int NewUid) {
		MyUid = NewUid;
	}
	
	public int getCompanyId() {
		return(MyCompanyId);
	}
	
	public void setCompanyId(int iMyCompanyId) {
		MyCompanyId = iMyCompanyId;
	}
	
	public String getCompanyName() {
		return(CompanyName);
	}
	
	public void setCompanyName(String inStr) {
		CompanyName =inStr;
	}
	
	public String getCompanyNameLc() {
		return(CompanyNameLc);
	}
	
	public void setCompanyNameLc(String inStr) {
		CompanyNameLc =inStr.toLowerCase();
	}
	
	public int getMyForm() {
		return(MyForm);
	}
	
	public void setMyForm(int iMyForm) {
		MyForm = iMyForm;
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
