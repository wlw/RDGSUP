package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;
import com.bricksimple.rdg.ExtractedClasses.LineItem;
import com.bricksimple.rdg.pushxbrl.FactDetail;
import com.bricksimple.rdg.pushxbrl.TableCls;
import com.bricksimple.rdg.pushxbrl.TableColumns;
import com.bricksimple.rdg.pushxbrl.ColumnDef;
import com.bricksimple.rdg.pushxbrl.TableCell;

public class RootNode {
     private String                 RootId = "";
     private String                 Label = "";
     private int                    GroupType = CONSTANTS.GP_NONE;
     private ArrayList<PreNode>     children = new ArrayList<PreNode>();
     private ArrayList<LineItem>    LineItems = new ArrayList<LineItem>();
     private int                    TemplateId = CONSTANTS.INVALID_TEMPLATE_ID;
     private String                 NameSpaceAbbrev = "";
     private TableCls               tableCls = new TableCls();
     private ArrayList<TableColumn> tableColumns = new ArrayList<TableColumn>();
     private ArrayList<String>      Dimensions = new ArrayList<String> ();
     private ArrayList<Integer>     omittedColumns = new ArrayList<Integer>();
     private ArrayList<String>     ItemDimensions = new ArrayList<String>(); // contains dimensions of tables on side
     
     public void AddDimension(String iValue) {
    	 int i = iValue.indexOf("#");
    	 
    	 Dimensions.add(iValue.substring(i+1));
     }
     
     public void ReplaceTableRows(TableColumns newColumns,  ArrayList<FactDetail> factDetails) {
    	 
     	 TableRowCls trc = null;
    	 TableCell   tc = null;
    	 int         iRowNdx = 0;
    	 
    	 tableCls.ClearRows();
    	 
    	 for(ColumnDef cd: newColumns.GetColumns()) {
    		 trc = new TableRowCls();
    		 trc.SetTblRowNdx(iRowNdx);
    		 iRowNdx++;
    		 for(Integer fdNdx: cd.GetFacts()) {
     			tc = new TableCell();
     			tc.SetGroupNdx(factDetails.get(fdNdx).GetGroupNdx());
     			tc.SetDetailNdx(factDetails.get(fdNdx).GetItemNdx());
     			trc.SetTableCell(tc);
    		 }
    		 tableCls.AddRow(trc);
    	 }
     }
     
     public ArrayList<String> GetDimensions() {
    	 return(Dimensions);
     }
     
     public boolean IsThisDimension(String iValue) {
    	 boolean bRtn = false;
    	 int     i = 0;
    	 
    	 if(iValue.length () == 0) // if no dimension on fact - its ours
    		 bRtn = true;
    	 while((bRtn == false) && ( i < Dimensions.size())) {
    		 if(iValue.equals(Dimensions.get(i)))
    			 bRtn = true;
    		 i++;
    	 }
    	 return(bRtn);
     }
     
     public void SetTableColumns(ArrayList<TableColumn> iValue) {
    	 tableColumns = iValue;
     }
     
     public ArrayList<TableColumn> GetTableColumns() {
    	 return(tableColumns);
     }
     
     
     public void SetTableCls(TableRowCls iValue) {
    	 tableCls.AddRow(iValue);
     }
     
     public TableCls GetTableCls() {
    	 return(tableCls);
     }
     
      public void SetNameSpaceAbbrev(String iValue) {
    	 NameSpaceAbbrev = iValue;
     }
     
     public String GetNameSpaceAbbrev () {
    	 return(NameSpaceAbbrev);
     }
     
     public void SetRootId(String iValue) {
    	 RootId = iValue;
     }
     
     public String GetRootId() {
    	 return(RootId);
     }
     
     public void SetLabel(String iValue) {
    	 Label = iValue;
     }
     
     public String GetLabel() {
    	 return(Label);
     }
     
     public void SetGroupType(int iValue) {
    	 GroupType = iValue;
     }
     
     public int GetGroupType() {
    	 return(GroupType);
     }
     
     public void AddChild(PreNode iValue) {
    	 children.add(iValue);
     }
     
     public ArrayList<PreNode> GetChildren() {
    	 return(children);
     }
     
     public void AddLineItem(String iValue, boolean bValue) {
    	 LineItem newItem = new LineItem();
    	 
    	 newItem.SetItemStr(iValue);
    	 newItem.SetIsParenthetical(bValue);
    	 LineItems.add(newItem);
     }
     
     public ArrayList<LineItem> GetLineItems() {
    	 return(LineItems);
     }
     
     public void SetTemplateId(int iValue) {
    	 TemplateId = iValue;
     }
     
     public int  GetTemplateId() {
    	 return(TemplateId);
     }
     
     public void RemoveTableColumn(int iValue) {
    	 tableColumns.remove(iValue);
     }
     
     public void AddOmittedColumn(int iValue) {
    	 omittedColumns.add(iValue);
     }
     
     public boolean IsColumnOmitted(int iValue) {
    	 boolean bRtn = false;
    	 int     iCount = 0;
    	 
    	 while((bRtn == false) && (iCount < omittedColumns.size())) {
    		 if(iValue == omittedColumns.get(iCount))
    			 bRtn = true;
    	     iCount++;
    	 }
    	 return(bRtn);
     }
     
     public void RemoveOmittedColumns(TableColumns tableColumns) {
    	 
    	 int     j = 0;
    	 int     k = 0;
    	 int     l = 0;
    	 
    	 String  omittedStr = "";
         boolean bFound = false;
    	 String  temp = "";
    	 
    	 for(ColumnDef cd: tableColumns.GetColumns()) {
    		 if(cd.GetOmitted()) {
    			 k = 0;
    			 bFound = false;
                 while((bFound == false) && (k < cd.GetContextRef().size())) {   			 
    			     omittedStr = cd.GetContextRef().get(0);
    			     j = 0;
    			     while((j < this.tableColumns.size()) && (bFound == false)) {
    			    	 l = 0;
    			    	 while((l < this.tableColumns.get(j).GetContextRef().size()) && (bFound == false)) {
    			    		 temp = this.tableColumns.get(j).GetContextRef().get(l).GetContextRef();
    				         if(temp.equals(omittedStr))
    				    	    bFound = true;
    				         else
    				        	 l++;
    			    	 }
    			    	 if(bFound == false)
    				         j++;
    			     }
    			     k++;
    			 }
                 if(bFound == true)
    			     this.tableColumns.remove(j);
    		 }
    	 }
     }
     public void AddItemDimensions(String iValue, boolean bValue) {
    	 //LineItem newItem = new LineItem();
    	 
    	 //newItem.SetItemStr(iValue);
    	 //newItem.SetIsParenthetical(bValue);
    	 ItemDimensions.add(iValue);
     }
     
     public ArrayList<String> GetItemDimensions() {
    	 return(ItemDimensions);
     }
     
}
