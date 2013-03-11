package com.bricksimple.rdg.pushxbrl;
import java.util.Date;

public class CoreCell {
    private int     Uid;
    private int     ElementUid;
    private int     RoleUid;
    private int     UnitUid;
    private int     Scale = 1;
    private Date    StartDate;
    private Date    EndDate;
    private boolean Negated = false;
    private int     TdColumn = -1;
    private int     MyUid = 0;
    
    public void SetUid(int iValue) {
    	Uid = iValue;
    }
    
    public int GetUid() {
    	return(Uid);
    }

    public void SetElementUid(int iValue) {
    	ElementUid = iValue;
    }
    
    public int GetElementUid() {
    	return(ElementUid);
    }

    public void SetRole(int iValue) {
    	RoleUid = iValue;
    }
    
    public int GetRole() {
    	return(RoleUid);
    }


    public void SetUnitUid(int iValue) {
    	UnitUid = iValue;
    }
    
    public int GetUnitUid() {
    	return(UnitUid);
    }

    public void SetScale(int iValue) {
    	Scale = iValue;
    }
    
    public int GetScale() {
    	return(Scale);
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

    public void SetNegated(boolean iValue) {
    	Negated = iValue;
    }
    
    public boolean GetNegated() {
    	return(Negated);
    }

    public void SetTdColumn(int iValue) {
    	TdColumn = iValue;
    }
    
    public int GetTdColumn() {
    	return(TdColumn);
    }

    public void SetMyUid(int iValue) {
    	MyUid = iValue;
    }
    
    public int GetMyUid() {
    	return(MyUid);
    }

 }

