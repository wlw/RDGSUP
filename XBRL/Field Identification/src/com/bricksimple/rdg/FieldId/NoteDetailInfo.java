package com.bricksimple.rdg.FieldId;

public class NoteDetailInfo {

	private int iSectionUid = 0;
	private String SectionText = "";
	
	public void SetSectionUid (int value) {
		iSectionUid = value;
	}
	
	public int GetSectionUid() {
		return(iSectionUid);
	}
	
	public void SetSectionText(String value) {
		SectionText += value;
	}
	
	public String GetSectionText() {
		return(SectionText);
	}
}

