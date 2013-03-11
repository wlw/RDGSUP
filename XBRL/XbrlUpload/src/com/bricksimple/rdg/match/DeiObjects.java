package com.bricksimple.rdg.match;

import java.util.ArrayList;

import com.bricksimple.rdg.match.ConfidenceLevel;

public class DeiObjects {
	private boolean            bFound = false;
	private String             DestTable = "";
	private String             DestRow = "";
	private int                DestType = 0;
	private String             XmlTag    = "";
	private ArrayList<String>  XmlData   = new ArrayList<String>();
	private boolean            bWrote = false;
	private ArrayList<String>  Contexts = new ArrayList<String>();
	private  ArrayList<String> Al;
	
	public void SetFound(boolean bValue) {
		bFound = bValue;
	}
	
	public boolean GetFound() {
		return(bFound);
	}
	
	public void SetDestTable(String iValue) {
		DestTable = iValue;
	}
	
	public String GetDestTable() {
		return(DestTable);
	}

	public void SetDestRow(String iValue) {
		DestRow = iValue;
	}
	
	public String GetDestRow() {
		return(DestRow);
	}

	public void SetDestType(int iValue) {
		DestType = iValue;
	}
	
	public int GetDestType() {
		return(DestType);
	}
	
	public void SetXmltag(String iValue) {
		XmlTag = iValue;
	}
	
	public String GetXmlTag() {
		return(XmlTag);
	}

	public void SetXmlData(String iValue) {
		XmlData.add(iValue);
	}
	
	public ArrayList<String> GetXmlData() {
		return(XmlData);
	}
	
	public void SetXmlContext(String iValue) {
		Contexts.add(iValue);
	}
	
	public ArrayList<String> GetXmlContext() {
		return(Contexts);
	}
	
  public void SetAl(String InTemplateStr) {
		ConfidenceLevel cl = new ConfidenceLevel();
		
    	Al = cl.RecurringSting(InTemplateStr);
    }
	
	public ArrayList<String> GetAl() {
		return(Al);
	}
	
	public void SetWrote(boolean bValue) {
		bWrote = bValue;
	}
	
	public boolean GetWrote() {
		return(bWrote);
	}
}
