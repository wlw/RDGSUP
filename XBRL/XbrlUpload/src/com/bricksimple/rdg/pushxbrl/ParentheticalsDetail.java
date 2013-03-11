package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

public class ParentheticalsDetail {
	private String ParentheticalId = "";
	private ArrayList<FactDetail> parentheticalFacts = new ArrayList<FactDetail>();
	
	public void SetParentheticalId(String iValue) {
		ParentheticalId = iValue;
	}
	
	public String GetParentheticalId() {
		return(ParentheticalId);
	}
	
	public void AddParentheticalFact(FactDetail fd) {
		parentheticalFacts.add(fd);
	}
	
	public ArrayList<FactDetail> GetParentheticalFact() {
		return(parentheticalFacts);
	}
}
