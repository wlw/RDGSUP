package com.bricksimple.rdg.FieldId;

public class UnMappedRowRtn {
    private boolean bSkipToNextRow;
    private boolean bGotRowData;
    
    public boolean GetSkipToNextRow() {
        return(bSkipToNextRow);
    }
    
    public void SetSkipToNextRow(boolean bValue) {
    	bSkipToNextRow = bValue;
    }
    
    public boolean GetGotRowData() {
        return(bGotRowData);
    }
    
    public void SetGotRowData(boolean bValue) {
    	bGotRowData = bValue;
    }
}
