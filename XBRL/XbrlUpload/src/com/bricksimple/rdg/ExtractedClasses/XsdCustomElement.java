package com.bricksimple.rdg.ExtractedClasses;

public class XsdCustomElement {

	private String  Id;
	private String  Name;
    private boolean Abstract = false;
    private boolean Nillable = true;
    private String  SubstitutionGroup;
    private String  Type;
    private String  Period;
    private String  Balance = "";
    private String  Documentation = "";
	
	public void SetId(String iValue) {
		Id = iValue;
	}
	
	public String GetId() {
		return(Id);
	}

	public void SetName(String iValue) {
		Name = iValue;
	}
	
	public String GetName() {
		return(Name);
	}

	public void SetAbstract(String iValue) {
		if(iValue.equals("true"))
			Abstract = true;
	}
	
	public Boolean GetAbstract() {
		return(Abstract);
	}

	public void SetNillable(String iValue) {
		if(iValue.equals("false"))
			Nillable = false;
	}
	
	public Boolean GetNillable() {
		return(Nillable);
	}

	public void SetSubstitutionGroup(String iValue) {
		SubstitutionGroup = iValue;
	}
	
	public String GetSubstitutionGroup() {
		return(SubstitutionGroup);
	}

	public void SetType(String iValue) {
		Type = iValue;
	}
	
	public String GetType() {
		return(Type);
	}

	public void SetPeriod(String iValue) {
		Period = iValue;
	}
	
	public String GetPeriod() {
		return(Period);
	}

	public void SetBalance(String iValue) {
		Balance = iValue;
	}
	
	public String GetBalance() {
		return(Balance);
	}

	public void SetDocumentation(String iValue) {
		Documentation = iValue;
	}
	
	public String GetDocumentation() {
		return(Documentation);
	}
}
