package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

import com.bricksimple.rdg.pushxbrl.TableCell;

public class TableRowCls {
    private ArrayList<TableCell>   tableCells  = new ArrayList<TableCell>();
	private String                 AbbrevNameSpace = "";
	private int                    TblRowNdx;

    public void SetTableCell(TableCell tc) {
    	tableCells.add(tc);
    }
    
    public ArrayList<TableCell> GetTableCells() {
    	return(tableCells);
    }
    
	public void SetAbbrevNameSpace(String iValue) {
	    AbbrevNameSpace = iValue;
	}
	
	public String GetAbbrevNameSpace() {
		return(AbbrevNameSpace);
	}

	public void SetTblRowNdx(int iValue) {
		TblRowNdx = iValue;
	}
	
	public int GetTblRowNdx() {
		return(TblRowNdx);
	}
}
