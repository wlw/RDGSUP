package com.bricksimple.rdg.FieldId;

public class FieldModifier {
	
	private int FieldModifierUid;
	private String DataStr;
	
	public FieldModifier(int fmuid, String Datastr) {
		FieldModifierUid = fmuid;
		DataStr = Datastr;
	}

	public void SetFieldUid(int fmuid) {
		FieldModifierUid = fmuid;
	}
	
	public int GetFieldUid() {
		return(FieldModifierUid);
	}
	
	public void SetDataStr(String inData) {
		DataStr = inData;
	}
	
	public String GetDataStr() {
		return(DataStr);
	}
}
