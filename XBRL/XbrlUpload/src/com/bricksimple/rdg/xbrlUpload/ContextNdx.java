package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

public class ContextNdx {
	   private String  ContextRef;
	   private int     TableRowNdx;
	   private int     TableCellNdx;
	   
	   public void SetContextRef(String iValue) {
		   ContextRef = iValue;
	   }
	   
	   public String GetContextRef() {
		   return(ContextRef);
	   }
	   
	   public void SetTableRowNdx(int iValue) {
		   TableRowNdx = iValue;
	   }
	   
	   public int GetTableRowNdx() {
		   return(TableRowNdx);
	   }
	   
	   public void SetTableCellNdx(int iValue) {
		   TableCellNdx = iValue;
	   }
	   
	   public int GetTableCellNdx() {
		   return(TableCellNdx);
	   }
	   
}
