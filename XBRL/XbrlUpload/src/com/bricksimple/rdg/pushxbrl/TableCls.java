package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

import com.bricksimple.rdg.xbrlUpload.TableRowCls;

public class TableCls {

	private ArrayList<TableRowCls> TableRows = new ArrayList<TableRowCls>();
	
	public void AddRow(TableRowCls trc) {
		TableRows.add(trc);
	}
	
	public ArrayList<TableRowCls> GetRows() {
		return(TableRows);
	}
	
	public void ClearRows() {
		TableRows = null;
		TableRows = new ArrayList<TableRowCls>();
	}
}
