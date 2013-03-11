package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;
import com.bricksimple.rdg.ExtractedClasses.Period;
import java.util.Date;
import java.util.Date;

public class TableColumn {
    private ArrayList<ContextNdx> ContextRef = new ArrayList<ContextNdx>();
    private Period                period = new Period();
    private int                   RefUid = 0;
    private Date                  Instant;
    private Date                  StartDate;
    private Date                  EndDate;
    
    public void SetContextRef(ContextNdx iValue) {
    	ContextRef.add(iValue);
    }
    
    public ArrayList<ContextNdx> GetContextRef() {
    	return(ContextRef);
    }
    
    public ContextNdx GetThisContextRef(int iIndx) {
    	return(ContextRef.get(iIndx));
    }
    
    public void SetPeriod(Period iValue) {
    	period.SetStartDate(iValue.GetStartDate());
    	period.SetEndDate(iValue.GetEndDate());
    	period.SetInstant(iValue.GetInstant());
    }
    
    public void SetInstant(Period iValue) {
    	period.SetInstant(iValue.GetInstant());
    }
    public Period GetPeriod() {
    	return(period);
    }
    
    public void SetRefUid(int iValue) {
    	RefUid = iValue;
    }
    
    public int GetRefUid() {
    	return(RefUid);
    }
    
    public void SetInstant(Date iValue) {
    	Instant = iValue;
    }
    
    public Date GetInstant() {
    	return(Instant);
    }

    public void SetStartDate(Date iValue) {
    	StartDate = iValue;
    }
    
    public Date GetStartDate() {
    	return(StartDate);
    }

    public void SetEndDate(Date iValue) {
    	EndDate = iValue;
    }
    
    public Date GetEndDate() {
    	return(EndDate);
    }

}
