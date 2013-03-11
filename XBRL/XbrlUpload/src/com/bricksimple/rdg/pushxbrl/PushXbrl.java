package com.bricksimple.rdg.pushxbrl;

//import java.io.FileWriter;
//import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Date;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;

import com.bricksimple.rdg.ExtractedClasses.CalExtract;
import com.bricksimple.rdg.ExtractedClasses.DefExtract;
import com.bricksimple.rdg.ExtractedClasses.LabExtract;
import com.bricksimple.rdg.ExtractedClasses.PreExtract;
//import com.bricksimple.rdg.ExtractedClasses.XmlContext;
import com.bricksimple.rdg.ExtractedClasses.NameSpaceLink;
import com.bricksimple.rdg.ExtractedClasses.XmlExtract;
import com.bricksimple.rdg.ExtractedClasses.XsdExtract;
import com.bricksimple.rdg.ExtractedClasses.UnitLink;
import com.bricksimple.rdg.ExtractedClasses.DimensionGroup;
//import com.bricksimple.rdg.ExtractedClasses.Dimension;
import com.bricksimple.rdg.ExtractedClasses.Period;
import com.bricksimple.rdg.match.ConfidenceLevel;
import com.bricksimple.rdg.match.DeiObjects;
import com.bricksimple.rdg.sqlaccess.*;
import com.bricksimple.rdg.xbrlUpload.CONSTANTS;
import com.bricksimple.rdg.xbrlUpload.ContextNdx;
import com.bricksimple.rdg.xbrlUpload.Debug;
import com.bricksimple.rdg.xbrlUpload.FilingInfo;
import com.bricksimple.rdg.xbrlUpload.RoleGaapXref;
import com.bricksimple.rdg.xbrlUpload.RootNode;
import com.bricksimple.rdg.xbrlUpload.TableRowCls;
import com.bricksimple.rdg.ExtractedClasses.PreGroup;
import com.bricksimple.rdg.xbrlUpload.ErrorCls;
import com.bricksimple.rdg.ExtractedClasses.PreLink;
import com.bricksimple.rdg.ExtractedClasses.XmlDetail;
import com.bricksimple.rdg.ExtractedClasses.XmlGrouping;
import com.bricksimple.rdg.ExtractedClasses.FootnoteLoc;
import com.bricksimple.rdg.ExtractedClasses.CalParent;
import com.bricksimple.rdg.ExtractedClasses.CalChild;
import com.bricksimple.rdg.ExtractedClasses.LineItem;
import com.bricksimple.rdg.xbrlUpload.UTILITIES;
import com.bricksimple.rdg.xbrlUpload.TableColumn;
import com.bricksimple.rdg.ExtractedClasses.XsdRoleType;
import com.bricksimple.rdg.xbrlUpload.PreNode;

//import com.bricksimple.rdg.FieldId.FieldMatchStr;


public class PushXbrl {

	public SubmissionInfo WriteBasicSubInfo(Connection con, XmlExtract xmlExtract, ArrayList<RootNode> NodeChain,
			                                FilingInfo filingInfo, XsdExtract xsdExtract, String stockSymbol,
			                                String webAddress, int userSuppliedCompanyUid) {
		SubmissionInfo         subInfo = new SubmissionInfo();
		MySqlAccess            mysql  = new MySqlAccess();
		ArrayList<DeiObjects>  deiObjects = new ArrayList<DeiObjects>();
		DataAccess             da      = new DataAccess();
		DataAccessRtn          dar     = new DataAccessRtn();
		String                 displayName = "";
		ArrayList<Stock>       stockArray = new ArrayList<Stock>();
		
		deiObjects = mysql.GetDeiObjects(con); 
		if(ValidateTemplates(con, mysql, NodeChain, xsdExtract)) {
		    stockArray = xmlExtract.MapOutStock();
		    MapStockToDb(con, xmlExtract, stockArray);
	        xmlExtract.MapData(con, deiObjects);
			if(userSuppliedCompanyUid == 0) {
		        subInfo.SetCompanyName(FindData(deiObjects, CONSTANTS.DEI_COMPANYNAME));
		        dar = da.FindCompany(con, subInfo.GetCompanyName(), stockSymbol, webAddress, deiObjects);
		        subInfo.SetCompanyUid(dar.GetUid());
			}
			else {
				subInfo.SetCompanyName(mysql.GetCompanyName(con, userSuppliedCompanyUid));
				subInfo.SetCompanyUid(userSuppliedCompanyUid);
			}
	        subInfo.SetFormId(FindData(deiObjects, CONSTANTS.DEI_FORMID));  // 10-Q
	        subInfo.SetSrcFile(filingInfo.GetSrcFile());
	        subInfo.SetFullSrcFile(filingInfo.GetFullSrcFile());
	        subInfo.SubmitXbrl(con);
	        subInfo.SetExchangeSymbol(stockSymbol);
	        String Temp = xsdExtract.GetXsdFileName().toLowerCase();
	        int    j = Temp.indexOf(".xsd");
	        displayName = xsdExtract.GetXsdFileName().substring(0,j);
	        mysql.MapDeiData(con, subInfo, deiObjects, xmlExtract.GetDimensionGroup(), displayName, stockArray, userSuppliedCompanyUid);
		}
		return(subInfo);
	}

	private void MapStockToDb(Connection con, XmlExtract xmlExtract, ArrayList<Stock> stockArray) {
			
		for(Stock stock: stockArray) {
			stock.SetDimUid(FindStockDimUid(con, xmlExtract, stock.GetConcept()));
		}
	}
	
	
	private int FindStockDimUid(Connection con, XmlExtract xmlExtract, String concept) {
		int         iRtn = 1;
		boolean     bFound = false;
		int         i = 0;
		String      dim = "";
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		while((bFound == false) && (i < xmlExtract.GetDimensionGroup().size())) {
			if(xmlExtract.GetDimensionGroup().get(i).GetContextId().equals(concept)) 
				bFound = true;
			else
				i++;
		}
		if(bFound) {
			if(xmlExtract.GetDimensionGroup().get(i).GetDimension().size() == 0)
			    dim = CONSTANTS.NODIMENSION_KEY;
			else {
				dim = xmlExtract.GetDimensionGroup().get(i).GetDimension().get(0).GetDim();
				dim = dim.replace(":", "_");
			}
			iRtn = mySqlAccess.FindStockDimension(con, dim);
		}
		return(iRtn);
	}
	
	private void GetLineItems(RootNode rootNode, XmlExtract xmlExtract, ElementPathXrefTbl elementPathXrefTbl) {
		
	String tag;
	int    tblRowIndex = 0;
	
    for(LineItem curNode: rootNode.GetLineItems()) {
        rootNode.SetNameSpaceAbbrev(elementPathXrefTbl.FindElementNameSpace(curNode.GetItemStr(), xmlExtract));
        tag = ExtractTag(curNode.GetItemStr());			
        FindTableRows(rootNode.GetNameSpaceAbbrev(), xmlExtract, xmlExtract.GetXmlGrouping(), tag, rootNode, tblRowIndex);
        tblRowIndex++;
    }
}

	public void DoPush(Connection con, XsdExtract xsdExtract, 
	                   DefExtract defExtract, LabExtract labExtract, 
	                   PreExtract preExtract, CalExtract calExtract, 
	                   XmlExtract xmlExtract, FilingInfo filingInfo, 
	                   ArrayList<RootNode> NodeChain, ArrayList<RootNode> parentheticalChain,
	                   SubmissionInfo subInfo, int companyTaxonomyUid, String exchangeSymbol,
	                   ArrayList<RoleGaapXref> roleGaapXref, Debug debug) {

		ElementPathXrefTbl      elementPathXrefTbl = new ElementPathXrefTbl();		
		
        //tblMatchArray = mySqlAccess.GetFieldIdentifiers(con, 1, subInfo.GetCompanyUid());		
		elementPathXrefTbl.InitList(con, xsdExtract.GetXsdFileName(), exchangeSymbol);
		FindAllTableRowsAndColumns(con, NodeChain, xmlExtract, elementPathXrefTbl);
		//RemoveOutLyers(con, NodeChain, xmlExtract.GetXmlGrouping());
		FormatDataForDb(con, xsdExtract, defExtract, labExtract, preExtract, calExtract, xmlExtract, filingInfo,
				        NodeChain, parentheticalChain, subInfo, companyTaxonomyUid, exchangeSymbol, roleGaapXref, elementPathXrefTbl, debug);
	}
	
	private void FindAllTableRowsAndColumns(Connection con, ArrayList<RootNode> NodeChain, 
			                                XmlExtract xmlExtract, ElementPathXrefTbl elementPathXrefTbl) {
	    for(RootNode curNode: NodeChain) {
    	    if(curNode.GetTemplateId() > 0) {
    		    //if(WriteableTemplate(curNode)) {
    			    if((curNode.GetGroupType() == CONSTANTS.GP_FINANCIAL)  ||
    			       (curNode.GetGroupType() == CONSTANTS.GP_NOTE_TBL_DETAIL))
    			    {
    				     GetLineItems(curNode, xmlExtract, elementPathXrefTbl);       				
    			         curNode.SetTableColumns(FindTableColumns(con, xmlExtract.GetXmlGrouping(), 
    			        		                 xmlExtract.GetDimensionGroup(), curNode.GetTableCls()));    
   		    	}
   		       // }
    	    }
	    }
	}
	private void FormatDataForDb(Connection con, XsdExtract xsdExtract, DefExtract defExtract, LabExtract labExtract, 
	                            PreExtract preExtract, CalExtract calExtract, XmlExtract xmlExtract, FilingInfo filingInfo, 
	                            ArrayList<RootNode> NodeChain, ArrayList<RootNode> parentheticalChain, SubmissionInfo subInfo, 
	                            int companyTaxonomyUid, String exchangeSymbol, ArrayList<RoleGaapXref> roleGaapXref, 
	                            ElementPathXrefTbl elementPathXrefTbl, Debug debug) {
	
		ArrayList<NoteStructure>        notes;
		TableColumns                    tableColumns  = null;
        ArrayList<FactDetail>           factDetails = null;
        int                             iNodeCount = 0;
        boolean                         bIsStockEquity = false;
        StockEquityFmt                  stockEquityFmt = null;
        StockEquityFmt                  passedStockEquityFmt = null;
        ArrayList<ParentheticalsDetail> parentheticals = new ArrayList<ParentheticalsDetail>();
        
        // we format the stock equity first since it is built oddly
        for(RootNode curNode: NodeChain) {
        	if(curNode.GetTemplateId() == 5) {
        		stockEquityFmt = FormatStockEquity(con, curNode, xmlExtract);
        	}
        }
        for(RootNode curNode: parentheticalChain) {
		    GetLineItems(curNode, xmlExtract, elementPathXrefTbl);       				
	        curNode.SetTableColumns(FindTableColumns(con, xmlExtract.GetXmlGrouping(), 
	        		                xmlExtract.GetDimensionGroup(), curNode.GetTableCls()));    
            parentheticals.add(FindParentheticalDetails(curNode, preExtract, xmlExtract, preExtract.GetEquityComponents()));       	    
        }
        
		for(RootNode curNode: NodeChain ) {
			if(curNode.GetGroupType() == CONSTANTS.GP_FINANCIAL) {
				// WLW removed code segment
				factDetails = new ArrayList<FactDetail>();
				//equityLabels = new ArrayList<EquityRowRef>();
				//sectionRowDef = new ArrayList<SectionRowDef>();
				bIsStockEquity = (curNode.GetTemplateId() == 5 ? true :false);
				//bIsStockEquity = false;  // until fixed
				if(bIsStockEquity == false) {
					passedStockEquityFmt = null;
					FindEquityRowsIncluded(curNode, xmlExtract, preExtract.GetEquityComponents(), factDetails);
					tableColumns = OrganizeDetailsIntoColumns(factDetails, xmlExtract, bIsStockEquity);
					MarkOmittedColumns(tableColumns, factDetails, iNodeCount, NodeChain, stockEquityFmt, preExtract, parentheticals);
				    CopyOmittedColumns(tableColumns, curNode);
					//curNode.RemoveOmittedColumns(tableColumns);
					MarkDuplicateCells(factDetails, xmlExtract, false);
				}
				else {
					passedStockEquityFmt = stockEquityFmt;
	        		//stockEquityFmt = FormatStockEquity(con, curNode, xmlExtract);	
				}
				int iTemplateParseUid = 0;  // dummy here as function used by Note tables
				SubmitDataForDb(con, xsdExtract, defExtract, labExtract, preExtract, calExtract, xmlExtract, filingInfo, 
		                curNode, subInfo, companyTaxonomyUid, exchangeSymbol, roleGaapXref, elementPathXrefTbl, factDetails, 
		                passedStockEquityFmt, iTemplateParseUid, debug);
			}
			iNodeCount++;
		}
		notes = GetNoteUri(xsdExtract);
		SubmitNotesDataForDb(con, xsdExtract, defExtract, labExtract, preExtract, calExtract, xmlExtract, filingInfo, 
                NodeChain, subInfo, companyTaxonomyUid, exchangeSymbol, roleGaapXref, elementPathXrefTbl, notes, debug);
		WriteCoreSummations(con, calExtract.GetCalParents(), debug);
	}
	
	private ParentheticalsDetail FindParentheticalDetails(RootNode curNode, PreExtract preExtract, XmlExtract xmlExtract, ArrayList<String> equityDimensions) {
		ParentheticalsDetail  rtnDetail = new ParentheticalsDetail();
	    ArrayList<FactDetail> factDetails = new ArrayList<FactDetail>();
	
		FindEquityRowsIncluded(curNode, xmlExtract, preExtract.GetEquityComponents(), factDetails);
		rtnDetail.SetParentheticalId(curNode.GetRootId());
		for(FactDetail fd: factDetails) {
			rtnDetail.AddParentheticalFact(fd);
		}
		return(rtnDetail);
	}
	
	/*
	private ArrayList<PreLink> FindPreLinks(PreExtract preExtract, String nodeId) {
		
		ArrayList<PreLink> rtnArray = null;
		int                i = 0;
		
		while(rtnArray == null) {
		    if(nodeId.equals(preExtract.GetPreGroups().get(i).GetRole()))
		    	rtnArray = preExtract.GetPreGroups().get(i).GetLinks();
		    i++;
		}
		return(rtnArray);
	}
	*/
	private StockEquityFmt FormatStockEquity(Connection con, RootNode curNode, XmlExtract xmlExtract) {
		StockEquityFmt  stockEquityFmt = new StockEquityFmt();
		boolean         bExtract = false;
		ArrayList<ItemDetailInfo> equityColList = new ArrayList<ItemDetailInfo>();
		StockEquityFmt  tempEquityFmt;
		
		for(PreNode preNode: curNode.GetChildren()) {
			ExtractEquityLoop(preNode, equityColList, bExtract);
		}
		RemoveDuplicateColumns(equityColList);
		//int iPreLinkNdx = 0;
		
		for(ItemDetailInfo colItem: equityColList) {
			FindEquityDetails(colItem, xmlExtract);
			//while(preLinks.get(iPreLinkNdx).GetUriTo().equals(colItem.GetLineItem()) == false)
			//	iPreLinkNdx++;
			FindEquityColumns(stockEquityFmt, xmlExtract, colItem);
			tempEquityFmt = new StockEquityFmt();
			InsertEquityRows(con, tempEquityFmt, xmlExtract, colItem);
			MergeEquityFmt(stockEquityFmt, tempEquityFmt);
		}
		return(stockEquityFmt);
	}
	
	private void MergeEquityFmt(StockEquityFmt sef, StockEquityFmt tempEquityFmt) {
		
		int i;
		boolean bInsertHere;
		
		for(UnsortedRow usr:  tempEquityFmt.GetUnsortedRows()) {
		    i = 0;
		    bInsertHere = false;
		    while((bInsertHere == false) && (i < sef.GetUnsortedRows().size())) {
		    	if(usr.GetInstantDateType() == true) {
		    		if(sef.GetUnsortedRows().get(i).GetInstantDateType() == true) {
		    			if(sef.GetUnsortedRows().get(i).GetInstantDateInt() > usr.GetInstantDateInt())
		    				bInsertHere = true;
		    			else
		    				i++;
		    		}
		    		else {
		    			if(sef.GetUnsortedRows().get(i).GetStartDateInt() > usr.GetNextDayInt())
		    				bInsertHere = true;
		    			else 
		    				i++;
		    		}
		    	}
		    	else {
		    		if(sef.GetUnsortedRows().get(i).GetInstantDateType() == true) {
		    			if(sef.GetUnsortedRows().get(i).GetNextDayInt() > usr.GetStartDateInt())
		    				bInsertHere = true;
		    			else 
		    				i++;
		    		}
		    		else {
		    			if(sef.GetUnsortedRows().get(i).GetStartDateInt() > usr.GetStartDateInt())
		    				bInsertHere = true;
		    			else
		    				i++;
		    		}
		    	}
		    }
		    // now insert row here or at append
		    if(bInsertHere)
		    	sef.GetUnsortedRows().add(i, usr);  // insert row 
		    else
		    	sef.GetUnsortedRows().add(usr);  // add to end
		}
	}
	
	private void FindEquityColumns(StockEquityFmt sef, XmlExtract xmlExtract, ItemDetailInfo colItem) {
		
		for(FactDetail fd: colItem.GetFactDetails()) {
		    sef.AddColumn(xmlExtract.GetXmlGrouping().get(fd.GetGroupNdx()).GetDetails().get(fd.GetItemNdx()).GetDimension(),
		    		      xmlExtract.GetXmlGrouping().get(fd.GetGroupNdx()).GetDetails().get(fd.GetItemNdx()).GetContextRef());
		}
	}
	
	private void InsertEquityRows(Connection con, StockEquityFmt sef, XmlExtract xmlExtract, ItemDetailInfo colItem) {
		int    iState  = 1;
		int    iRowCount = 0;
		Period myPeriod = null;
		
		for(FactDetail fd: colItem.GetFactDetails()) {
			iState = 1;
			iRowCount = 0;
			myPeriod = FindMyPeriod(xmlExtract, fd);			
			while((iRowCount < sef.GetUnsortedRows().size()) && (iState > 0)) {
				iState = sef.GetUnsortedRows().get(iRowCount).IsThisMyRow(myPeriod.GetStartDate(),
						myPeriod.GetEndDate(), myPeriod.GetInstant());
				if(iState > 0)
					iRowCount++;
						
			}
			switch(iState) {
			    case -1:
			    	sef.InsertNewRow(con, fd, myPeriod, iRowCount, colItem.GetLineItem(), fd.GetUnitRef());
			        break;
			    case 0:
			    	sef.AddToUnsortedRow(fd, iRowCount);
				    break;
			    case 1:
			    	sef.AddUnsortedRow(con, fd, myPeriod, colItem.GetLineItem(), fd.GetUnitRef());
				    break;
			}
		}
	}
	
	private Period FindMyPeriod(XmlExtract xmlExtract, FactDetail fd) {
		String  context;
		Period  period = null;
		int     i = 0;
		boolean bFound = false;
		
		context = xmlExtract.GetXmlGrouping().get(fd.GetGroupNdx()).GetDetails().get(fd.GetItemNdx()).GetContextRef();
		while((bFound == false) && (i < xmlExtract.GetDimensionGroup().size())) {
			if(xmlExtract.GetDimensionGroup().get(i).GetContextId().equals(context)) {
				period = xmlExtract.GetDimensionGroup().get(i).GetPeriod();
				bFound = true;
			}
			i++;
		}
		return(period);
	}
	
	private void RemoveDuplicateColumns(ArrayList<ItemDetailInfo> equityColList) {
		int     i = equityColList.size() -1;
		int     j = 0;
		boolean bFound = false;
		
		while(i > 0) {
			j = i-1;
			bFound = false;
			while(j >= 0) {
				if((equityColList.get(i).GetGroup().equals(equityColList.get(j).GetGroup())) &&
					(equityColList.get(i).GetDetail().equals(equityColList.get(j).GetDetail()))	)
					bFound = true;
				j--;
			}
			if(bFound == true) {
				equityColList.remove(i);
			}
			i--;
		}
	}
	
	private void FindEquityDetails(ItemDetailInfo colItem, XmlExtract xmlExtract) {
	
		boolean    bFound = false;
		FactDetail fd = null;
		int        iGroup = 0;
		int        iFact = 0;
		
		while(bFound == false) {
			if(xmlExtract.GetXmlGrouping().get(iGroup).GetPrefix().equals(colItem.GetGroup()))
				bFound = true;
			else
				iGroup++;
		}
		while(iFact < xmlExtract.GetXmlGrouping().get(iGroup).GetDetails().size()) {
		    String temp = xmlExtract.GetXmlGrouping().get(iGroup).GetDetails().get(iFact).GetTag();
		    if(temp.equals(colItem.GetDetail())) {
		    	fd = new FactDetail();
		    	fd.SetGroupNdx(iGroup);
		    	fd.SetItemNdx(iFact);
		    	fd.SetUnitRef(xmlExtract.GetXmlGrouping().get(iGroup).GetDetails().get(iFact).GetUnitRef());
		    	colItem.SetFactDetail(fd);
		    }
		    iFact++;
		}
	}
	
	private boolean ExtractEquityLoop(PreNode preNode, ArrayList<ItemDetailInfo> equityColList,boolean bExtract) {
		boolean bMyExtract = bExtract;
        String  extract = "";
        int     i;
        
		if(bExtract == true) {
			ItemDetailInfo idi = new ItemDetailInfo();
		    extract = preNode.GetId();
		    idi.SetLineItem(extract);
		    i = extract.indexOf("#");
		    extract = extract.substring(i+1);
		    i = extract.indexOf("_");
		    idi.SetGroup(extract.substring(0, i));
		    idi.SetDetail(extract.substring(i+1));
		    equityColList.add(idi);
		}
		else {
			extract = preNode.GetId();
			if(extract.toLowerCase().contains("statementlineitems"))
				bMyExtract = true;
		}
		for(PreNode child: preNode.GetChildren()) {
			bMyExtract = ExtractEquityLoop(child, equityColList, bMyExtract);
		}
		return(bMyExtract);
	}
	
	private void CopyOmittedColumns(TableColumns tableColumns, RootNode curNode) {
		int i = 0;
		
		while(i < tableColumns.GetOmittedColumns().size()) {
			curNode.AddOmittedColumn(tableColumns.GetOmittedColumns().get(i));
			i++;
		}
	}

	/*
	private ArrayList<EquityRowRef> FindEquityColumnLabels(RootNode curNode, LabExtract labExtract) {
		ArrayList<EquityRowRef> rtnLabels = new ArrayList<EquityRowRef>();
		//boolean             bFound = false;
		ArrayList<PreNode>  preNodes = curNode.GetChildren();
		ArrayList<String>   startIndicators = new ArrayList<String>(Arrays.asList("us-gaap_StatementTable", "us-gaap_StatementEquityComponentsAxis"));
		int                 iNdx = 0;
		//String              columnLabel = "";
		int                 iLblNdx = 0;
		EquityRowRef        newRow = null;
		
		if(preNodes.size() > 0) {
		    while((iNdx  < startIndicators.size())  && (preNodes.size() != 0)) {
			    if(preNodes.get(0).GetId().contains(startIndicators.get(iNdx))) 
				    iNdx++;
			    preNodes = preNodes.get(0).GetChildren();
		    }
		    iNdx = 0;
		    if(preNodes.size() != 0) {
		        while(iNdx < preNodes.size()) {
			        newRow = new EquityRowRef();
			        newRow.SetRowId(preNodes.get(iNdx).GetId());
		            rtnLabels.add(newRow);
		            iNdx++;
		        }
		        iNdx = 0;
		        while(iNdx < rtnLabels.size()) {
			        iLblNdx = 0;
			        while(labExtract.GetLabelArcs().get(iLblNdx).GetUriFrom().equals(rtnLabels.get(iNdx).GetRowId()) == false)
				        iLblNdx++;
			        rtnLabels.get(iNdx).SetRowData(labExtract.GetLabelArcs().get(iLblNdx).GetData());
			        iNdx++;
		        }
		    }
		}
		return(rtnLabels);
	}
	*/
	private void MarkDuplicateCells(ArrayList<FactDetail> factDetails, XmlExtract xmlExtract, boolean bIsStockEquity) {
	
		int       iChkNdx;
		int       iMatchNdx;
		boolean   bFoundMatch;
		String    chkData;
		String    matchData;
		
		if(factDetails.size() > 1) {
			iChkNdx = 1;
			while(iChkNdx < factDetails.size())  {
				iMatchNdx = 0;
				bFoundMatch = false;
				while((iMatchNdx < iChkNdx) && (bFoundMatch == false)) {
					if(factDetails.get(iChkNdx).GetConcept().equals(factDetails.get(iMatchNdx).GetConcept())) {
						if(bIsStockEquity == false) {
				            if(factDetails.get(iChkNdx).IdenticalPeriod(factDetails.get(iMatchNdx).GetPeriod()) == true) {
					            chkData = xmlExtract.GetXmlGrouping().get(factDetails.get(iChkNdx).GetGroupNdx()).GetDetails().get(factDetails.get(iChkNdx).GetItemNdx()).GetData();
					            matchData = xmlExtract.GetXmlGrouping().get(factDetails.get(iMatchNdx).GetGroupNdx()).GetDetails().get(factDetails.get(iMatchNdx).GetItemNdx()).GetData();
                                if(chkData.equals(matchData)) {
                    	            bFoundMatch = true;
                    	            factDetails.get(iChkNdx).SetOmit(true);
                                }
				            }
				        }
						else  { // stock equity
							if((factDetails.get(iChkNdx).GetGroupNdx() == factDetails.get(iMatchNdx).GetGroupNdx()) &&
									(factDetails.get(iChkNdx).GetItemNdx() == factDetails.get(iMatchNdx).GetItemNdx())) {
								bFoundMatch = true;
								factDetails.get(iChkNdx).SetOmit(true);
							}
						}
					}
				    iMatchNdx++;
			    }
			    iChkNdx++;
			}
		}
	}
	/***********
	private void FindRowsIncluded(RootNode curNode, XmlExtract xmlExtract, 
			                      ArrayList<FactDetail> factDetails) {
		//ArrayList<SectionRowDef> rtnArray = new ArrayList<SectionRowDef>();
		int                      iColumnNdx;
	    int                      iTableClsNdx = 0;
		ArrayList<Integer>       xmlDetails = new ArrayList<Integer>();
		ArrayList<Integer>       facts = new ArrayList<Integer>();
		ArrayList<FactDimension> factDimensions = new ArrayList<FactDimension>();
        ArrayList<RowFact>       rowFacts = new ArrayList<RowFact>();
        FactDetail               factDetail;
        int                      lineNum = 0;
        String                   contextRef = "";
        RowFact                  rowFact = null;
        boolean                  bHaveRowFact = false;
        int                      iMyColumnNdx = 0;
        Period                   myPeriod = null;
        String                   rowConcept = "";
        
        while(iTableClsNdx < curNode.GetLineItems().size()) {
            TableRowCls tableRowCls = FindMyTableRows(curNode, iTableClsNdx);
            bHaveRowFact  = false;
            rowConcept = curNode.GetLineItems().get(iTableClsNdx).GetItemStr();
    		for (iColumnNdx = 0; iColumnNdx < tableRowCls.GetTableCells().size(); iColumnNdx++) {
     			factDetail = new FactDetail();
    			factDetail.SetGroupNdx(tableRowCls.GetTableCells().get(iColumnNdx).GetGroupNdx());
    			factDetail.SetItemNdx(tableRowCls.GetTableCells().get(iColumnNdx).GetDetailNdx());
    			factDetail.SetLineNum(lineNum);
    			factDetail.SetConcept(rowConcept);
    			factDetails.add(factDetail);
       			if(bHaveRowFact == false) {
    				rowFact = new RowFact();
    	   			rowFact.SetFactDetail(factDetails.size() -1);
    	   			bHaveRowFact = true;
   			    }
    			contextRef = xmlExtract.GetXmlGrouping().get(factDetail.GetGroupNdx()).GetDetails().get(factDetail.GetItemNdx()).GetContextRef();
    			factDetail.SetContextRef(contextRef);
    			myPeriod = FindPeriod(contextRef, xmlExtract.GetDimensionGroup());
    			factDetail.SetPeriod(myPeriod);
    			iMyColumnNdx = FindMyColumnIndex(contextRef, curNode.GetTableColumns());
    			//iMyColumnNdx = AddToColumnDefs(columnDefs, factDetails.size() -1, contextRef, xmlExtract);
    			rowFact.SetColumnDefNdx(iMyColumnNdx);
    			int groupNdx = tableRowCls.GetTableCells().get(iColumnNdx).GetGroupNdx();
    			int detailNdx = tableRowCls.GetTableCells().get(iColumnNdx).GetDetailNdx();
    			FindDetails(xmlExtract.GetXmlGrouping().get(groupNdx).GetDetails().get(detailNdx),
    					    xmlExtract.GetDimensionGroup(), xmlDetails, facts, factDimensions);
    		}
    		if(bHaveRowFact == true)
    			rowFacts.add(rowFact);
       	    iTableClsNdx++;
       	    lineNum++;
        }
		return;
	}
	
	**************/
	private void FindEquityRowsIncluded(RootNode curNode, XmlExtract xmlExtract, ArrayList<String> equityDimensions,
                                        ArrayList<FactDetail> factDetails) {
        //ArrayList<SectionRowDef> rtnArray = new ArrayList<SectionRowDef>();
        int                      iColumnNdx;
        int                      iTableClsNdx = 0;
        ArrayList<Integer>       xmlDetails = new ArrayList<Integer>();
        ArrayList<Integer>       facts = new ArrayList<Integer>();
        ArrayList<FactDimension> factDimensions = new ArrayList<FactDimension>();
        ArrayList<RowFact>       rowFacts = new ArrayList<RowFact>();
        FactDetail               factDetail;
        int                      lineNum = 0;
        String                   contextRef = "";
        RowFact                  rowFact = null;
        boolean                  bHaveRowFact = false;
        int                      iMyColumnNdx = 0;
        Period                   myPeriod = null;
        String                   rowConcept = "";

        while(iTableClsNdx < curNode.GetLineItems().size()) {
            TableRowCls tableRowCls = FindMyTableRows(curNode, iTableClsNdx);
            bHaveRowFact  = false;
            rowConcept = curNode.GetLineItems().get(iTableClsNdx).GetItemStr();
            for (iColumnNdx = 0; iColumnNdx < tableRowCls.GetTableCells().size(); iColumnNdx++) {
                factDetail = new FactDetail();
                factDetail.SetGroupNdx(tableRowCls.GetTableCells().get(iColumnNdx).GetGroupNdx());
                factDetail.SetItemNdx(tableRowCls.GetTableCells().get(iColumnNdx).GetDetailNdx());
                factDetail.SetLineNum(lineNum);
                factDetail.SetConcept(rowConcept);
                factDetails.add(factDetail);                   
                if(bHaveRowFact == false) {
                    rowFact = new RowFact();
	                rowFact.SetFactDetail(factDetails.size() -1);
	                bHaveRowFact = true;
                }
                contextRef = xmlExtract.GetXmlGrouping().get(factDetail.GetGroupNdx()).GetDetails().get(factDetail.GetItemNdx()).GetContextRef();
                factDetail.SetContextRef(contextRef);
                myPeriod = FindPeriod(contextRef, xmlExtract.GetDimensionGroup());
                factDetail.SetPeriod(myPeriod);
                iMyColumnNdx = FindMyColumnIndex(contextRef, curNode.GetTableColumns());
                rowFact.SetColumnDefNdx(iMyColumnNdx);
                int groupNdx = tableRowCls.GetTableCells().get(iColumnNdx).GetGroupNdx();
                int detailNdx = tableRowCls.GetTableCells().get(iColumnNdx).GetDetailNdx();
                FindDetails(xmlExtract.GetXmlGrouping().get(groupNdx).GetDetails().get(detailNdx),
	            xmlExtract.GetDimensionGroup(), xmlDetails, facts, factDimensions);
            }
            if(bHaveRowFact == true)
                rowFacts.add(rowFact);
            iTableClsNdx++;
            lineNum++;
        }
        // now remove details that are not ours
        //RemoveDetailsThatNotOurs(xmlExtract, factDetails, equityDimensions);
        //RemoveDuplicates(factDetails);
        return;
    }

	/*
	private void RemoveDuplicates(ArrayList<FactDetail> factDetails) {
		
		int     iNdx = factDetails.size() -1;
		int     iTest;
		boolean bFound = false;
		
		while(iNdx > 0) {
		    iTest = iNdx -1;
		    bFound = false;
		    while((bFound == false) && (iTest > -1)) {
		    	if((factDetails.get(iNdx).GetGroupNdx() == factDetails.get(iTest).GetGroupNdx()) &&
		    	   (factDetails.get(iNdx).GetItemNdx() == factDetails.get(iTest).GetItemNdx()))
		    		bFound = true;
		    	iTest--;
		    }
		    if(bFound) {
		    	factDetails.remove(iNdx);		    
		    }
		    iNdx--;
		}
	}
	
    private void RemoveDetailsThatNotOurs(XmlExtract xmlExtract, ArrayList<FactDetail> factDetails, ArrayList<String> equityDimensions) {
    	
        int     j = factDetails.size() -1;
        int     equityNdx;
        boolean bFound = false;
        String  myDimension = "";
        
        while(j >= 0) {
    		myDimension = xmlExtract.GetXmlGrouping().get(factDetails.get(j).GetGroupNdx()).GetDetails().get(factDetails.get(j).GetItemNdx()).GetDimension();
        	bFound = false;
    		equityNdx = 0;
        	while((bFound == false) && (equityNdx < equityDimensions.size())) {
        	    if(equityDimensions.get(equityNdx).equals(myDimension))
        		    bFound = true;
        		equityNdx++;
        	}
        	if(bFound == false) {
        		factDetails.remove(j);
        	}
        	j--;
        }
   }
    */
	private TableColumns OrganizeDetailsIntoColumns(ArrayList<FactDetail> factDetails, XmlExtract xmlExtract, boolean bIsStockEquity ) {
		TableColumns       tableColumns = new TableColumns();		
        int                detailNdx = 0;
        ArrayList<Integer> contextNdxs = new ArrayList<Integer>();
        //ArrayList<String>  dimensions = new ArrayList<String>();
        //String             myDim = "";
        
        while(detailNdx < factDetails.size()) {
        	contextNdxs.add(FindMyIndex(factDetails.get(detailNdx).GetContextRef(), xmlExtract.GetDimensionGroup()));   
        	detailNdx++;
        }
        //if(bIsStockEquity == false) {
            for(detailNdx = 0; detailNdx < factDetails.size(); detailNdx++) {
            	if(factDetails.get(detailNdx).GetOmit() == false) {
            		//iGroupNdx = factDetails.get(detailNdx).GetGroupNdx();
            		//iDetailNdx = factDetails.get(detailNdx).GetItemNdx();
        	        tableColumns.AddColumnCell(factDetails.get(detailNdx).GetContextRef(), 
        		        	                   xmlExtract.GetDimensionGroup().get(contextNdxs.get(detailNdx)).GetPeriod().GetStartDate(),
        			                           xmlExtract.GetDimensionGroup().get(contextNdxs.get(detailNdx)).GetPeriod().GetEndDate(),
        			                           true, detailNdx);
            	}
            }
            for(detailNdx = 0; detailNdx < factDetails.size(); detailNdx++) {
            	if(factDetails.get(detailNdx).GetOmit() == false) {
            		//iGroupNdx = factDetails.get(detailNdx).GetGroupNdx();
            		//iDetailNdx = factDetails.get(detailNdx).GetItemNdx();
        	        tableColumns.AddColumnCell(factDetails.get(detailNdx).GetContextRef(), 
        		        	                   xmlExtract.GetDimensionGroup().get(contextNdxs.get(detailNdx)).GetPeriod().GetInstant(),
        			                           xmlExtract.GetDimensionGroup().get(contextNdxs.get(detailNdx)).GetPeriod().GetEndDate(),
        			                           false, detailNdx);
            	}
            }
        //}
//        else {
//            for(detailNdx = 0; detailNdx < factDetails.size(); detailNdx++) {
//            	myDim = xmlExtract.GetXmlGrouping().get(factDetails.get(detailNdx).GetGroupNdx()).GetDetails().get(factDetails.get(detailNdx).GetItemNdx()).GetDimension();
//            	tableColumns.AddDimensionCell(myDim, detailNdx);
//            }
//        }
		return(tableColumns);
	}
		
	private int FindMyIndex(String contextRef, ArrayList<DimensionGroup> dimensions) {
		int     iRtn = 0;
		boolean bFound = false;
		
		while(bFound == false) {
			if(contextRef.equals(dimensions.get(iRtn).GetContextId()))
				bFound = true;
			else
				iRtn++;
		}
		return(iRtn);
	}
	
	private int FindMyColumnIndex(String contextRef, ArrayList<TableColumn> tableColumns) {
		int iRtn = -1;
		int iNdx = 0;
		int iColNdx = 0;
		
		while((iNdx < tableColumns.size()) && (iRtn == -1)) {
			iColNdx = 0;
			while((iColNdx < tableColumns.get(iNdx).GetContextRef().size()) && (iRtn == -1)) {
				if(contextRef.equals(tableColumns.get(iNdx).GetContextRef().get(iColNdx).GetContextRef()))
					iRtn = iNdx;
				iColNdx++;
			}
			iNdx++;
		}
		
		return(iRtn);
	}
	private void MarkOmittedColumns(TableColumns tableColumns, ArrayList<FactDetail> factDetails, int iThisNodeNdx,
			                        ArrayList<RootNode> nodeChain, StockEquityFmt stockEquityFmt, PreExtract preExtract,
			                        ArrayList<ParentheticalsDetail> parentheticals) {
		
		int                  ifactDetailNdx;
		boolean              bNotFound;
		int                  iGroupNdx;
		int                  iDetailNdx;
		int                  iNodeNdx;
		//boolean              bIsAnyNotFound = false;
		boolean              bFoundDetail;
		int                  iRemoveNdx;
		int                  iCurColumn = 0;
		ArrayList<Integer>   columnsKept = new ArrayList<Integer>();
		ParentheticalsDetail myParenthetical = null;
		
		for(ColumnDef columnDef: tableColumns.GetColumns()) {
			ifactDetailNdx = 0;
			bNotFound = false;
			//check a column
			while((bNotFound == false) && (ifactDetailNdx < columnDef.GetFacts().size())) {
				iGroupNdx = factDetails.get(columnDef.GetFacts().get(ifactDetailNdx)).GetGroupNdx();
				iDetailNdx = factDetails.get(columnDef.GetFacts().get(ifactDetailNdx)).GetItemNdx();
				iNodeNdx = 0;
				bFoundDetail = false;
				// check a detail
				while((bFoundDetail == false) && (iNodeNdx < nodeChain.size())) {
					if(iNodeNdx != iThisNodeNdx)  {  // do not check self
						if(nodeChain.get(iNodeNdx).GetGroupType() == CONSTANTS.GP_FINANCIAL) {
							if(nodeChain.get(iNodeNdx).GetTemplateId() == 5) {
							    bFoundDetail = CheckEquitySection(iGroupNdx, iDetailNdx, stockEquityFmt);
							}
							else {
							    bFoundDetail = CheckThisSection(iGroupNdx, iDetailNdx, nodeChain.get(iNodeNdx));  // true found it used elsewhere
							    if(bFoundDetail == false) {  //check parentheticals here
								    myParenthetical = GetThisParentheticalGroup(parentheticals, nodeChain.get(iNodeNdx).GetRootId());
								    if(myParenthetical != null) {
								    	bFoundDetail = CheckParentheticalsForDetail(iGroupNdx, iDetailNdx, myParenthetical);
								    }
							    }
							}
						}
					}
					iNodeNdx++;
				}
				if(bFoundDetail == false)  // detail NOT found  keep column
					bNotFound = true;
				ifactDetailNdx++;
			}
			if( bNotFound == false) {  // all details found in another section)(s)
				      // remove column here
			    for(iRemoveNdx = 0; iRemoveNdx < columnDef.GetFacts().size(); iRemoveNdx++) {
			    	//WLW test
			    	if(OmitFact(columnDef.GetFacts().get(iRemoveNdx), columnsKept, iCurColumn, tableColumns) == true)
			    	    factDetails.get(columnDef.GetFacts().get(iRemoveNdx)).SetOmit(true);
			    }
			    columnDef.SetOmitted(true);
			    tableColumns.AddOmittedColumn(iCurColumn);
			}
			else
				columnsKept.add(iCurColumn);
			iCurColumn++;
		}
	}
	
	private ParentheticalsDetail GetThisParentheticalGroup(ArrayList<ParentheticalsDetail> parentheticals, String nodeStr) {
		ParentheticalsDetail rtnDetail = null;
		int                  i = 0;
		
		while((i < parentheticals.size()) && (rtnDetail == null)) {
			if(parentheticals.get(i).GetParentheticalId().contains(nodeStr))
				rtnDetail = parentheticals.get(i);
			i++;
		}
		return(rtnDetail);
	}
	
	private boolean CheckParentheticalsForDetail(int iGroupNdx, int iDetailNdx, ParentheticalsDetail pd) {
		boolean bRtn = false;
		int     iNdx = 0;
		
		while((bRtn == false) && (iNdx < pd.GetParentheticalFact().size())) {
			if((iGroupNdx == pd.GetParentheticalFact().get(iNdx).GetGroupNdx()) &&
				(iDetailNdx == pd.GetParentheticalFact().get(iNdx).GetItemNdx()))
				bRtn = true;
			iNdx++;
		}
		return(bRtn);
	}
	
	private boolean  CheckEquitySection(int iGroupNdx, int iDetailNdx, StockEquityFmt stockEquityFmt) {
		boolean bRtn = false;
		int     iRowCnt = 0;
		int     iItemCnt = 0;
		
		while((bRtn == false) && (iRowCnt < stockEquityFmt.GetUnsortedRows().size())) {
			iItemCnt = 0;
			while((bRtn == false) && (iItemCnt < stockEquityFmt.GetUnsortedRows().get(iRowCnt).GetRowDetails().size())) {
				if ((iGroupNdx == stockEquityFmt.GetUnsortedRows().get(iRowCnt).GetRowDetails().get(iItemCnt).GetGroupNdx()) &&
					(iDetailNdx == stockEquityFmt.GetUnsortedRows().get(iRowCnt).GetRowDetails().get(iItemCnt).GetDetailNdx()))
					bRtn = true;
				iItemCnt++;
			}
			iRowCnt++;
		}
		return(bRtn);
	}
	
	private boolean OmitFact(int iRemoveNdx, ArrayList<Integer> columnsKept,
			                 int iCurColumn, TableColumns tableColumns) {
	    boolean bRtn = true;
	    int     iCounter;
	    int     iTempNdx;
	    
	    // if detail in the kept columns - we omit on this column
	    iCounter = 0;
	    while((bRtn== true) && (iCounter < columnsKept.size())) {
	    	iTempNdx = 0;
	    	while((bRtn == true) && (iTempNdx < tableColumns.GetColumns().get(columnsKept.get(iCounter)).GetFacts().size())) {
	    		if(iRemoveNdx == tableColumns.GetColumns().get(columnsKept.get(iCounter)).GetFacts().get(iTempNdx))
	    				bRtn = false;
	    		iTempNdx++;
	    	}
	    	iCounter++;
	    }
	    if(bRtn == true) { // if not found above check if in a latter column
	    	iCounter = iCurColumn + 1;  // skip to the next row
	    	while((bRtn == true) && (iCounter < tableColumns.GetColumns().size())) {
		    	iTempNdx = 0;
		    	while((bRtn == true) && (iTempNdx < tableColumns.GetColumns().get(iCounter).GetFacts().size())) {
		    		if(iRemoveNdx == tableColumns.GetColumns().get(iCounter).GetFacts().get(iTempNdx))
		    				bRtn = false;
		    		iTempNdx++;
		    	}
		    	iCounter++;
	    	}
	    }
	    return(bRtn);
	}
	
	private boolean CheckThisSection(int iGroupNdx, int iDetailNdx, RootNode curNode) {
		boolean bDetailFound = false;
	    int     iTableClsNdx = 0;
	    int     iColumnNdx;
	    
        while((iTableClsNdx < curNode.GetLineItems().size()) && (bDetailFound == false)) {
            TableRowCls tableRowCls = FindMyTableRows(curNode, iTableClsNdx);
            iColumnNdx = 0;
     		while ((iColumnNdx < tableRowCls.GetTableCells().size()) && (bDetailFound == false)) {
     			if((iGroupNdx == tableRowCls.GetTableCells().get(iColumnNdx).GetGroupNdx()) &&
     			   (iDetailNdx == tableRowCls.GetTableCells().get(iColumnNdx).GetDetailNdx()))
     		        bDetailFound = true;
     			iColumnNdx++;
    		}
     		iTableClsNdx++;
        }
        return(bDetailFound);
	}
    	
	private static final String IGNORE_STRING = "Document And Entity Information";
	private static final String IGNORE_STRING2 = "(Policies)";
	private static final String DISCLOSURE = "Disclosure -";
	private static final String TABLE = "(Table";
	private static final String DETAIL = "(Detail";
	
	private ArrayList<NoteStructure> GetNoteUri(XsdExtract xsdExtract) {
		ArrayList<NoteStructure> notes = new ArrayList<NoteStructure>();
		NoteStructure            curNote;
	    String                   workStr;
	    
		for(XsdRoleType roleType: xsdExtract.GetXsdRoleTypes()) {
			workStr = roleType.GetData();
			if((workStr.contains(IGNORE_STRING) == false) && (workStr.contains(IGNORE_STRING2) == false)) {
	            if(workStr.contains(DISCLOSURE)) {
	            	if(workStr.contains(TABLE))
	            		SetTableFlag(notes, workStr);
	            	else {
	            		if(workStr.contains(DETAIL)) {
	            			SetTableToNote(notes, workStr);
	            		}
	            		else {
	            			curNote = new NoteStructure();
	            			curNote.SetNoteId(workStr);
	            			notes.add(curNote);
	            		}
	            	}
	            }
			}
		}
		return(notes);
	}
	    
	private void SetTableToNote(ArrayList<NoteStructure> notes, String workStr) {
		
		for(NoteStructure note: notes) {
			if(workStr.contains(note.GetWorkStr()))
				note.InsertTableOrDetail(workStr);
		}
	}
	
	private void SetTableFlag(ArrayList<NoteStructure> notes, String workStr) {
		
		for(NoteStructure note: notes) {
			if(workStr.contains(note.GetWorkStr()))
				note.SetContainsTables(true);
		}
	}
	
    private void SubmitDataForDb(Connection con, XsdExtract xsdExtract, DefExtract defExtract, LabExtract labExtract, 
			                     PreExtract preExtract, CalExtract calExtract, XmlExtract xmlExtract, FilingInfo filingInfo, 
			                     RootNode curNode, SubmissionInfo subInfo, int companyTaxonomyUid, String exchangeSymbol,
			                     ArrayList<RoleGaapXref> roleGaapXref, ElementPathXrefTbl elementPathXrefTbl,
			                     ArrayList<FactDetail> factDetails, StockEquityFmt stockEquityFmt, int iTemplateParseUid,  Debug debug) {
		
	    MySqlAccess             mysql   = new MySqlAccess();
		//int                     iTemplateParseUid = 0;
        PreGroup                myGroup = null;
		PreGroup                parentheticals = null;
		ArrayList<PreLink>      preLinks = null;
		String                  keyString = "StatementTable";
		//String                  noteConcept = "";       
		
		if(iTemplateParseUid == 0)
            iTemplateParseUid = mysql.WriteTemplateParse(con, subInfo, curNode);
        myGroup = GetGroup(curNode.GetRootId(), preExtract);
	    FindNoteConcept(con, subInfo, keyString, myGroup.GetLinks());
        UpdateElementUid(con, subInfo, curNode.GetLineItems().get(0).GetItemStr(), xmlExtract, xsdExtract,
           		         iTemplateParseUid, myGroup.GetConcept());
        debug.WriteDebug("Template: " + curNode.GetLabel());
	    parentheticals = GetParentheticalGroup(preExtract, curNode.GetRootId());
        ResolveRoleGaapXref(myGroup.GetLinks(), roleGaapXref);
        if(parentheticals != null) {
            ResolveRoleGaapXref(parentheticals.GetLinks(), roleGaapXref);
            preLinks = parentheticals.GetLinks();
        }
        if(iTemplateParseUid > 0) {
	        if(stockEquityFmt == null)
                WriteTableDetails(con, subInfo, iTemplateParseUid, curNode, elementPathXrefTbl, myGroup.GetLinks(),
               	    	          preLinks, xmlExtract, labExtract, companyTaxonomyUid, 
               		             calExtract.GetCalParents(), xsdExtract, factDetails, debug);
	       	else
                WriteEquityTableDetails(con, subInfo, iTemplateParseUid, curNode, elementPathXrefTbl, myGroup.GetLinks(),
      	    	                        preLinks, xmlExtract, labExtract, companyTaxonomyUid, 
      		                            calExtract.GetCalParents(), xsdExtract, factDetails, stockEquityFmt,
      		                            preExtract.GetEquityComponents(), debug);

	    }
	}
    
        private void SubmitNotesDataForDb(Connection con, XsdExtract xsdExtract, DefExtract defExtract, LabExtract labExtract, 
                PreExtract preExtract, CalExtract calExtract, XmlExtract xmlExtract, FilingInfo filingInfo, 
                ArrayList<RootNode> NodeChain, SubmissionInfo subInfo, int companyTaxonomyUid, String exchangeSymbol,
                ArrayList<RoleGaapXref> roleGaapXref, ElementPathXrefTbl elementPathXrefTbl, 
                ArrayList<NoteStructure> notes, Debug debug) {
    	
	    	int                      noteNdx = 0;
		    int                      notesIndex = -1;
			int                      iTemplateParseUid = 0;
		    MySqlAccess              mysql = new MySqlAccess();
		    boolean                  bWriteDetails = false;
		    PreGroup                 myGroup = null;
		    PreGroup                 parentheticals = null;
	        ArrayList<PreLink>       preLink = null;
	        ArrayList<UnitUid>       unitUids = new ArrayList<UnitUid>();
	        ArrayList<ScaleUid>      scaleUids = new ArrayList<ScaleUid>();
	        ArrayList<NoteUidXref>   noteUids = new ArrayList<NoteUidXref>();
	        NoteUidXref              noteUidXref = null;
	        String                   noteConcept = "";
	        String                   keyString = "StatementLineItems";
	        MasterNoteTableConcepts  mNTc = new MasterNoteTableConcepts();
	        ArrayList<FactDetail>    factDetails = new ArrayList<FactDetail>();
	        
	        unitUids = mysql.GetUnitUids(con);
	        scaleUids = mysql.GetScaleUids(con);
	        for(RootNode curNode: NodeChain) {
    		    notesIndex = curNode.GetGroupType();
     		    if(notesIndex == CONSTANTS.GP_NOTE_TABLE) {
     		    	mNTc.InsertNoteTableConcepts(curNode);
     		    }
	        }
    	    for(RootNode curNode: NodeChain) {
    		    notesIndex = CheckForNoteInfo(curNode.GetLabel(), notes);
    		    bWriteDetails = true;
    		    if(notesIndex != -1) {
				    myGroup = GetGroup(curNode.GetRootId(), preExtract);
    		    	switch (curNode.GetGroupType()) {
    		    	
    			        case CONSTANTS.GP_NOTE:
    			    	    iTemplateParseUid = mysql.WriteTemplateParse(con, subInfo, curNode);
    			    	    noteUidXref = new NoteUidXref();
    			    	    noteUidXref.SetUid(iTemplateParseUid);
    			    	    noteUidXref.SetIdentifiedText(curNode.GetLabel());
    			    	    noteUids.add(noteUidXref);
    			    	    noteConcept = FindNoteConcept(con, subInfo, keyString, myGroup.GetLinks());
 		    	            UpdateElementUid(con, subInfo, curNode.GetLineItems().get(0).GetItemStr(), xmlExtract, xsdExtract,
 		    	            		         iTemplateParseUid, noteConcept);
	    	                //thisNote = new NoteTemplate();
	    	                //thisNote.SetTemplateUid(iTemplateParseUid);
	    	                //thisNote.SetIdentifiedText(curNode.GetLabel());
	    	                //thisNote.SetNoteNdx(++noteNdx);
	    	                //noteTemplates.add(thisNote);
	    	                notes.get(notesIndex).SetNoteIndex(++noteNdx);
	    	                bWriteDetails = false;
	    	                break;
	    	                
        			    case CONSTANTS.GP_NOTE_TBL_DETAIL:
        			    	if(notes.get(notesIndex).GetContainsTables() == true) {
	    		                iTemplateParseUid = mysql.WriteNoteTemplateParse(con, subInfo, curNode, notes.get(notesIndex).GetNoteIndex());
	    		                //noteConcept = FindNoteConcept(con, subInfo, keyString, myGroup.GetLinks());
	    		                noteConcept = mNTc.FindTableConcept(curNode.GetLabel());
    		    	            UpdateElementUid(con, subInfo, curNode.GetLineItems().get(0).GetItemStr(),
    		    	            		         xmlExtract, xsdExtract, iTemplateParseUid, noteConcept);
        			    	}
	    	    	        break;
	    	    	        
        			    case CONSTANTS.GP_NOTE_DETAIL:
        			    	int NoteUid = FindOriginNote(noteUids, curNode.GetLabel());
        			    	WriteNoteDetails(con, subInfo, curNode, xmlExtract, labExtract, NoteUid, xmlExtract.GetDimensionGroup(),
        			    			          xsdExtract, companyTaxonomyUid, xmlExtract.GetNameSpaceLinks(),
        			    			          unitUids, scaleUids);
        			    	/*
        			    	 * 	private void WriteNoteDetails(XX Connection con, 
        			    	 *                                XX SubmissionInfo subInfo,
        			    	 *                                 XX RootNode curNode, 
        			    	 *                                 XX XmlExtract xmlExtract,
        			    	 *                                 XX  LabExtract labExtract, 
			                                                   XX int NoteUid, 
			                                                   XX ArrayList<DimensionGroup> contexts,
			                                                   XX XsdExtract xsdExtract,
			                                                   XX  int companyTaxonomyUid,
			                                                    ArrayList<NameSpaceLink> nameSpaceLink){
                            */
        			    	 
        			    	bWriteDetails = false;
        			    	break;

	    	    	        default:
	    	    	        	bWriteDetails = false;
	    	    	        	break;
    		    	
    		    	}
        		    if(bWriteDetails){
 	    		        debug.WriteDebug("Template: " + curNode.GetLabel());
	    		        parentheticals = GetParentheticalGroup(preExtract, curNode.GetRootId());	    		        
	                    ResolveRoleGaapXref(myGroup.GetLinks(), roleGaapXref);
	                    if(parentheticals != null) {
	                        ResolveRoleGaapXref(parentheticals.GetLinks(), roleGaapXref);
	                        preLink = parentheticals.GetLinks();
	                    }
		    	        if(iTemplateParseUid > 0) {
		    	        	if(IsNormalTableOrientation(curNode.GetChildren()) == false) {
		    	        		ResetLineItems(curNode);
		    	        		StockEquityFmt stockEquityFmt;
		    	        		stockEquityFmt = FormatStockEquity(con, curNode, xmlExtract);
		    	        		FindEquityRowsIncluded(curNode, xmlExtract, curNode.GetItemDimensions(), factDetails);		    	        	
		    					SubmitDataForDb(con, xsdExtract, defExtract, labExtract, preExtract, calExtract, xmlExtract, filingInfo, 
		    			                        curNode, subInfo, companyTaxonomyUid, exchangeSymbol, roleGaapXref, elementPathXrefTbl, factDetails, 
		    			                        stockEquityFmt, iTemplateParseUid, debug);
		    	        	}
		    	        	else {
	    				        GetLineItems(curNode, xmlExtract, elementPathXrefTbl); 
	    			            curNode.SetTableColumns(FindTableColumns(con, xmlExtract.GetXmlGrouping(), 
	    			        	    	                xmlExtract.GetDimensionGroup(), curNode.GetTableCls()));    
	                            WriteTableDetails(con, subInfo, iTemplateParseUid, curNode, elementPathXrefTbl, myGroup.GetLinks(),
          	   	                                  preLink, xmlExtract, labExtract, companyTaxonomyUid, 
          	   	                                  calExtract.GetCalParents(), xsdExtract, null, debug);
		    	        	}
		    	        }
        		    }

    		    }
    	    }   	
        }
        
    private boolean IsNormalTableOrientation(ArrayList<PreNode> preLinks) {
    	boolean bRtn = false;
    	int     i = 0;
    	
    	while((bRtn == false) && (i < preLinks.size())) {
    		if(preLinks.get(i).GetId().contains(CONSTANTS.StatementScenarioAxis))
    			bRtn = true;
    		else
    			bRtn = IsNormalTableOrientation(preLinks.get(i).GetChildren());
    		i++;
    	}
    	return(bRtn);
    }
    
    private void ResetLineItems(RootNode curNode) {
    	
    	boolean bFound = false;
     	
    	FindLineItems(curNode, curNode.GetChildren(), bFound);
    }
    
	private boolean FindLineItems(RootNode root, ArrayList<PreNode> preNodes, boolean bFoundAxis) {
		
		boolean bMyFoundAxis = bFoundAxis;
		
		for(PreNode preNode: preNodes) {
			//if found 'Axis' we just save the children
			if(bMyFoundAxis == false) {
				if(preNode.GetId().endsWith("Axis")) {
					bMyFoundAxis = true;
					SaveTheChildren(root, preNode.GetChildren());
				}
				else
				    bMyFoundAxis = FindLineItems(root, preNode.GetChildren(), bMyFoundAxis);
			}
		}
		return(bMyFoundAxis);
	}

	private void SaveTheChildren(RootNode root, ArrayList<PreNode> preNodes) {
	
		for(PreNode preNode: preNodes) {
			root.AddItemDimensions(preNode.GetId(), preNode.GetIsParenthetical());		
		}
	}
	
    private String FindNoteConcept(Connection con, SubmissionInfo si, String keyString, ArrayList<PreLink> preLinks) {
    	String      rtnConcept = "";
    	boolean     bFound = false;
    	int         i = 0;
    	ErrorCls    errorCls = new ErrorCls();
    	MySqlAccess mySqlAccess = new MySqlAccess();
    	
    	while((i < preLinks.size()) && (bFound == false)) {
    		if(preLinks.get(i).GetLocalTo().contains(keyString))
    			bFound = true;
    		else
    		    i++;
    	}
    	if(bFound == true) {
			bFound = false;
			while((i < preLinks.size()) && (bFound == false)) {	
				i++;
  			    bFound = preLinks.get(i).GetLocalTo().contains("_");
			}
    	}	
    	if(bFound == false) {
    		errorCls.setFunctionStr("FindNoteConcept");
    		errorCls.setCompanyUid(si.GetCompanyUid());
    		errorCls.setItemVersion(si.GetVersion());
    		errorCls.setSubUid(si.GetSubmissionUid());
		    errorCls.setErrorText("No note concept encountered ");
		    mySqlAccess.WriteAppError(con, errorCls);   	
    	}
    	else
    	{
    		rtnConcept = preLinks.get(i).GetLocalTo();
    	}
    	return(rtnConcept);
    }
        
    private int FindOriginNote(ArrayList<NoteUidXref> noteUids, String Label) {
        int     iRtn = 0;
        boolean bFound = false;
        int     i = 0;
        String  extract;
        int     j;
        String  keyStr = "Disclosure -";
        
        j = Label.indexOf(keyStr);
        extract = Label.substring(keyStr.length() + 1 + j).trim();
        extract = extract.replace("Detail", "").trim();
        extract = extract.replace("()", "").trim();
        while((i < noteUids.size()) && (bFound == false)) {
        	iRtn = noteUids.get(i).IsThisMyNote(extract);
        	if(iRtn != 0)
        		bFound = true;
        	i++;
        }
        return(iRtn);  
    }
    
    private int CheckForNoteInfo(String label, ArrayList<NoteStructure> notes) {
    	int iRtn = -1;
    	int iCheck = 0;
    	
    	while((iCheck < notes.size()) && (iRtn == -1)) {
    		if(label.contains(notes.get(iCheck).GetWorkStr()))
    			iRtn = iCheck;
    		else
    			iCheck++;
    	}
    	return(iRtn);
    }
    
    private PreGroup GetGroup(String GroupStr, PreExtract preExtract) {
    	PreGroup myGroup = null;
    	
        int kk = 0;
        while((myGroup == null) && (kk < preExtract.GetPreGroups().size())) {
            if(GroupStr.equals(preExtract.GetPreGroups().get(kk).GetRole()))
		        myGroup = preExtract.GetPreGroups().get(kk);
	        kk++;
        }
        return(myGroup);
    }

    private PreGroup GetParentheticalGroup(PreExtract preExtract, String roleStr) {
    	PreGroup parentheticals = null;
    	
    	int kk = 0;
    	
        while((parentheticals == null) && (kk < preExtract.GetPreGroups().size())) {
            if(preExtract.GetPreGroups().get(kk).GetRole().contains("_Parentheticals")) {
            	if(preExtract.GetPreGroups().get(kk).GetRole().contains(roleStr))
            	    parentheticals = preExtract.GetPreGroups().get(kk);
            }
	        kk++;
        }
        return(parentheticals);
   	
    }
    /*
	private int FindNoteTemplateParseUid(RootNode curNode, ArrayList<NoteTemplate> noteTemplates) {
		int iRtn = 0;
		
		for(NoteTemplate nt: noteTemplates) {
			if(curNode.GetLabel().contains(nt.GetIdentifiedText()))
				iRtn = nt.GetTemplateUid();
		}
		return(iRtn);
	}
	*/
	private void WriteNoteDetails(Connection con, SubmissionInfo subInfo, RootNode curNode, XmlExtract xmlExtract, LabExtract labExtract, 
			                      int NoteUid, ArrayList<DimensionGroup> contexts, XsdExtract xsdExtract, int companyTaxonomyUid,
			                      ArrayList<NameSpaceLink> nameSpaceLink, ArrayList<UnitUid> unitUids,
			                      ArrayList<ScaleUid> scaleUids){
	 
		NoteDetailRef            ndr = null;
		int                      iGroupNdx;
		MySqlAccess              mySqlAccess = new MySqlAccess();
		//int                      AxisElement = 0;
		//int                      MemberElement = 0;
		ArrayList<Integer>       xmlDetails = new ArrayList<Integer>();
		ArrayList<Integer>       facts = new ArrayList<Integer>();
		ArrayList<FactDimension> factDimensions = new ArrayList<FactDimension>();
		ArrayList<Group>         groups = new ArrayList<Group>();
		ArrayList<MemberGroup>   memberGroup = new ArrayList<MemberGroup>();
		ArrayList<Integer>       groupDetailMember = new ArrayList<Integer>();
	    RowDef                   rowDef = new RowDef();
	    int                      gaapId;
	    LabelRoleRtn             labelRoleRtn = null;
	    //ArrayList<RoleGaapXref>  roleGaapXref = new ArrayList<RoleGaapXref>();
	    
	    //roleGaapXref = mySqlAccess.GetRoleGaapRefs(con);
        for(LineItem cn: curNode.GetLineItems()) {
            ndr = ExtractNoteReferences(cn.GetItemStr());
    		iGroupNdx = FindGroupIndex( ndr.GetGroup(), xmlExtract.GetXmlGrouping());
    		if(iGroupNdx != -1) {
                //String taxonomyPathKey = ExtractTaxKey(cn.GetItemStr());
                //String taxonomySpaceKey = ExtractSpaceKey(cn.GetItemStr(), xmlExtract.GetNameSpaceLinks());
                labelRoleRtn = FindRowLabel(cn.GetItemStr(), labExtract, null, null);
                String label = labelRoleRtn.GetLabel();
                //String GaapRefKey = ExtractGaapRefKey(cn.GetItemStr());
		        for(XmlDetail testDetail: xmlExtract.GetXmlGrouping().get(iGroupNdx).GetDetails()) {			
		            if(testDetail.GetTag().equals(ndr.GetContext())) {
		        	    int SectionUid = mySqlAccess.WriteSectionRec(con, label, NoteUid, "Text TBD");
		        	    int FactSentenceUid = mySqlAccess.WriteFactSentenceRec(con, "Text TBD");
		        	    gaapId = GetNoteDetailGaapId(con, cn.GetItemStr(), subInfo.GetCompanyUid(), xmlExtract, xsdExtract);
		        	    int SectionFactUid = mySqlAccess.WriteSectionFact(con, SectionUid, FactSentenceUid, testDetail.GetData(), gaapId);
		        	    FindDetails(testDetail, contexts, xmlDetails, facts, factDimensions);
		    		    FindGroupForThisDetail(con, xmlDetails, contexts, groups, subInfo.GetExchangeSymbol(), companyTaxonomyUid, 
		    			    	               nameSpaceLink, xsdExtract, memberGroup, groupDetailMember);	
		    		    CheckForDimensionsToWrite(con, SectionFactUid, rowDef, memberGroup, groups, 1);
                        WriteSectionFactRefTbl(con, SectionFactUid, gaapId, unitUids, scaleUids, contexts, 
                        		               testDetail);
		            }
		        }
		    }
        }
	}
	
	private void WriteSectionFactRefTbl(Connection con, int sectionFactUid, int gaapId, ArrayList<UnitUid> unitUids,
			                            ArrayList<ScaleUid> scaleUids, ArrayList<DimensionGroup> contexts, XmlDetail xmlDetail) {
	
		int         scaleUid = FindScaleUid(scaleUids, xmlDetail.GetDecimals());
		int         unitUid = FindUnitUid(unitUids, xmlDetail.GetUnitRef());
		MySqlAccess mySqlAccess = new MySqlAccess();
		int         contextNdx;
		String      startDate = "";
		String      endDate = "";
		
		contextNdx = FindContextIndex(contexts, xmlDetail.GetContextRef());
		if(contextNdx != -1) {
			if(contexts.get(contextNdx).GetPeriod().GetStartDate().length() == 0) {
			    startDate = contexts.get(contextNdx).GetPeriod().GetInstant();
			    endDate = contexts.get(contextNdx).GetPeriod().GetInstant();
			}
			else {
			    startDate = contexts.get(contextNdx).GetPeriod().GetStartDate();
			    endDate = contexts.get(contextNdx).GetPeriod().GetEndDate();
			}
		}
		mySqlAccess.WriteSectionFactRefRec(con, sectionFactUid, gaapId, unitUid, scaleUid,
				                           startDate, endDate);		
	}
	
	private int FindContextIndex(ArrayList<DimensionGroup> contexts, String context) {
	    int     iRtn = -1;
	    boolean bFound = false;
	    int     iCheck = 0;
	    
	    while((bFound == false) && (iCheck < contexts.size())) {
	    	if(context.equals(contexts.get(iCheck).GetContextId())) {
	    		bFound = true;
	    		iRtn = iCheck;
	    	}
	    	iCheck++;
	    }
	    return(iRtn);
	}
	
	private int FindScaleUid(ArrayList<ScaleUid> scaleUids, String decimals) {
		int     iRtn = 0;
		boolean bFound = false;
		int     i = 0;
		
		while((bFound == false) && (i < scaleUids.size())) {
			if(scaleUids.get(i).GetXbrlRefStr().equals(decimals.toLowerCase())) {
				bFound = true;
				iRtn = scaleUids.get(i).GetUid();
			}
			i++;
		}
		return(iRtn);
	}
	
	private int FindUnitUid(ArrayList<UnitUid> unitUids, String decimals) {
		int     iRtn = 0;
		boolean bFound = false;
		int     i = 0;
		
		while((bFound == false) && (i < unitUids.size())) {
			if(unitUids.get(i).GetXbrlRefStr().contains(decimals)) {
				bFound = true;
				iRtn = unitUids.get(i).GetUid();
			}
			i++;
		}
		return(iRtn);
	}
	
	private int GetNoteDetailGaapId(Connection con, String itemStr, int companyUid, XmlExtract xmlExtract,
			                           XsdExtract xsdExtract) {
		int         gaapId;
		int         taxonomyUid;
		String      taxKey = "";
		MySqlAccess mySqlAccess = new MySqlAccess();
		String      gaapRefKey = "";
		
		taxKey = ExtractSpaceKey(itemStr, xmlExtract.GetNameSpaceLinks());
		taxonomyUid = mySqlAccess.GetTaxonomyUid(con, taxKey, companyUid, true);
		gaapRefKey = ExtractGaapRefKey(itemStr);
		gaapId = mySqlAccess.GetGaapRefNdx(con, gaapRefKey, taxonomyUid, xsdExtract);
		return(gaapId);
	}
	
	private NoteDetailRef ExtractNoteReferences(String origStr) {
		NoteDetailRef  ndr = new NoteDetailRef();
		String         temp;
		int            i;
		
		i = origStr.indexOf("#");
		temp = origStr.substring(i+1);
		i = temp.indexOf("_");
		ndr.SetGroup(temp.substring(0, i));
		ndr.SetContext(temp.substring(i+1));
		return(ndr);
	}
	
	private void UpdateElementUid(Connection con, SubmissionInfo subInfo, String itemStr, XmlExtract xmlExtract, 
			                      XsdExtract xsdExtract,  int iTemplateParseUid, String concept) {
		MySqlAccess mysql = new MySqlAccess();
	    int         elementUid = 0;
	    
        String taxonomySpaceKey = ExtractNameSpace(concept, xmlExtract.GetNameSpaceLinks());
        int taxonomyUid = mysql.GetTaxonomyUid(con, taxonomySpaceKey, subInfo.GetCompanyUid(), true);
        //String GaapRefKey = ExtractGaapRefKey(itemStr);
        //elementUid = mysql.GetGaapRefNdx(con, GaapRefKey, taxonomyUid, xsdExtract);
        elementUid = mysql.GetGaapRefNdx(con, concept, taxonomyUid, xsdExtract);
        mysql.UpdateElementUid(con, subInfo, elementUid, iTemplateParseUid);
	}

	
	private void ResolveRoleGaapXref(ArrayList<PreLink> preLinks, ArrayList<RoleGaapXref> roleGaapXref) {
		int     i = 0;
		boolean bFound = false;
		int     j = 0;
		
		while(i < preLinks.size()) {
		    bFound = false;
		    j = 0;
		    preLinks.get(i).SetRoleUid(1);
		    preLinks.get(i).SetNegated(false);
		    if(preLinks.get(i).GetRole().length() > 0) {
		        while((bFound == false) && (j < roleGaapXref.size())) {
		    	    if(preLinks.get(i).GetRole().equals(roleGaapXref.get(j).GetText())) {
		    		    bFound  = true;
		    		    preLinks.get(i).SetRoleUid(roleGaapXref.get(j).GetUid());
			    	    preLinks.get(i).SetNegated(roleGaapXref.get(j).GetNegated());
		    	    }
		    	    j++;
		        }
		    }
		    i++;
		}	
	}

	private void WriteTableDetails(Connection con, SubmissionInfo subInfo, int iTemplateParseUid, RootNode rootNode,
                                   ElementPathXrefTbl elementPathXrefTbl, ArrayList<PreLink> preLinks, ArrayList<PreLink> parentheticalPreLink, 
                                   XmlExtract xmlExtract, LabExtract labExtract, int companyTaxonomyUid, ArrayList<CalParent> calParents, 
                                   XsdExtract xsdExtract, ArrayList<FactDetail> factDetails, Debug debug) {
        MySqlAccess            mySqlAccess = new MySqlAccess();
        DimensionTaxUid        dimensionTaxUid  = new DimensionTaxUid();
        CalCheckList           calCheckList = new CalCheckList();
        String                 NameSpaceAbbrev;
        String                 tag;
        ArrayList<Integer>     previousRefs = new ArrayList<Integer>();;
        UTILITIES              utils = new UTILITIES();
	    LabelRoleRtn           labelRoleRtn = null;

        //ArrayList<EquityColRef>      equityCols = new ArrayList<EquityColRef>();
        
       // here write DateRefs for all the columns
        WriteDateRefs(con, subInfo, rootNode.GetTableColumns());
        ClearMapped(xmlExtract.GetXmlGrouping());  // I don't remember why I did this
        int iTableClsNdx = 0;
        for(LineItem curNode: rootNode.GetLineItems()) {
            NameSpaceAbbrev = elementPathXrefTbl.FindElementNameSpace(curNode.GetItemStr(), xmlExtract);
            tag = ExtractTag(curNode.GetItemStr());
            String taxonomyPathKey = ExtractTaxKey(curNode.GetItemStr());
            String taxonomySpaceKey = ExtractSpaceKey(curNode.GetItemStr(), xmlExtract.GetNameSpaceLinks());
            labelRoleRtn = FindRowLabel(curNode.GetItemStr(), labExtract, null, null);
            String label = labelRoleRtn.GetLabel();
            long hashedUid = utils.hashValue(label);
            String GaapRefKey = ExtractGaapRefKey(curNode.GetItemStr());
            if(NameSpaceAbbrev.indexOf(subInfo.GetExchangeSymbol()) == 0)
                dimensionTaxUid.SetDataTaxonomyUid( companyTaxonomyUid);
            int iPreLink = -1;
            PreLink preLink = null;
            if(curNode.GetIsParenthetical() == true) {
                iPreLink = FindPrelinkIndex(GaapRefKey, parentheticalPreLink);
                if(iPreLink != -1)
                    preLink = parentheticalPreLink.get(iPreLink);
           }
            else {
                iPreLink = FindPrelinkIndex(GaapRefKey, preLinks);
                if(iPreLink != -1)
                    preLink = preLinks.get(iPreLink);
            }
            TableRowCls myRows = FindMyTableRows(rootNode, iTableClsNdx);
            previousRefs = WriteDataToDb(con, mySqlAccess, subInfo, NameSpaceAbbrev, tag, label, xmlExtract.GetXmlGrouping(), 
                                         xmlExtract.GetDimensionGroup(), iTemplateParseUid, GaapRefKey, taxonomyPathKey, 
                                         xmlExtract.GetUnitLinks(), companyTaxonomyUid, xmlExtract.GetNameSpaceLinks(),
                                         xmlExtract.GetFootNoteLoc(), taxonomySpaceKey, calParents, calCheckList, 
                                         curNode.GetIsParenthetical(), preLink, rootNode, xsdExtract,
                                         previousRefs, myRows, rootNode.GetTableColumns(), debug, hashedUid, factDetails);
            iTableClsNdx++;
        }
    }

	private void WriteEquityTableDetails(Connection con, SubmissionInfo subInfo, int iTemplateParseUid, RootNode rootNode,
            ElementPathXrefTbl elementPathXrefTbl, ArrayList<PreLink> preLinks, ArrayList<PreLink> parentheticalPreLink, 
            XmlExtract xmlExtract, LabExtract labExtract, int companyTaxonomyUid, ArrayList<CalParent> calParents, 
            XsdExtract xsdExtract, ArrayList<FactDetail> factDetails, StockEquityFmt stockEquityFmt, 
            ArrayList<String> equityDimensions, Debug debug) {
        MySqlAccess              mySqlAccess = new MySqlAccess();
        DimensionTaxUid          dimensionTaxUid  = new DimensionTaxUid();
        CalCheckList             calCheckList = new CalCheckList();
        String                   NameSpaceAbbrev;
        String                   tag;
        ArrayList<Integer>       previousRefs = new ArrayList<Integer>();;
        UTILITIES                utils = new UTILITIES();
	    LabelRoleRtn             labelRoleRtn = null;
 
        WriteEquityDateRefs(con, subInfo, stockEquityFmt.GetEquityColumns(), labExtract);
        ClearMapped(xmlExtract.GetXmlGrouping());  // I don't remember why I did this
        
        int iPreLinkNdx = -1;  // gets bumped later
        for(UnsortedRow curNode: stockEquityFmt.GetUnsortedRows()) {
            NameSpaceAbbrev = elementPathXrefTbl.FindElementNameSpace(curNode.GetLineItem(), xmlExtract);
            tag = ExtractTag(curNode.GetLineItem());
            String taxonomyPathKey = ExtractTaxKey(curNode.GetLineItem());
            String taxonomySpaceKey = ExtractSpaceKey(curNode.GetLineItem(), xmlExtract.GetNameSpaceLinks());
            int iMyPreLink = FindMyPreLink(preLinks, iPreLinkNdx, curNode.GetLineItem());
            if(iMyPreLink > iPreLinkNdx)
            	iPreLinkNdx = iMyPreLink;
			//while(preLinks.get(iPreLinkNdx).GetUriTo().equals(curNode.GetLineItem()) == false)
			//	iPreLinkNdx++;
            labelRoleRtn = FindRowLabel(curNode.GetLineItem(), labExtract, curNode.GetUnitRef(), 
            		                    preLinks.get(iPreLinkNdx).GetRole());
            String label = labelRoleRtn.GetLabel();
            if(labelRoleRtn.GetRoleId() != 0)
            	preLinks.get(iPreLinkNdx).SetRoleUid(labelRoleRtn.GetRoleId());
            label = ModifyLabel(label, preLinks.get(iPreLinkNdx).GetRole(), curNode);
            long hashedUid = utils.hashValue(label);
            String GaapRefKey = ExtractGaapRefKey(curNode.GetLineItem());
            if(NameSpaceAbbrev.indexOf(subInfo.GetExchangeSymbol()) == 0)
                dimensionTaxUid.SetDataTaxonomyUid( companyTaxonomyUid);
            previousRefs = WriteEquityDataToDb(con, mySqlAccess, subInfo, NameSpaceAbbrev, tag, label, xmlExtract.GetXmlGrouping(), 
                                         xmlExtract.GetDimensionGroup(), iTemplateParseUid, GaapRefKey, taxonomyPathKey, 
                                         xmlExtract.GetUnitLinks(), companyTaxonomyUid, xmlExtract.GetNameSpaceLinks(),
                                         xmlExtract.GetFootNoteLoc(), taxonomySpaceKey, calParents, calCheckList, 
                                         false, preLinks.get(iPreLinkNdx), rootNode, xsdExtract,
                                         previousRefs, rootNode.GetTableColumns(), debug, hashedUid, factDetails,
                                         curNode, stockEquityFmt.GetEquityColumns());
            //iTableClsNdx++;
        }
    }
	
	private int FindMyPreLink(ArrayList<PreLink> preLinks, int prevLink, String lineItem) {
		int     iRtn = prevLink + 1;
		boolean bFound = false;
		
		//check if we are at the end already
		if(iRtn >= preLinks.size())
			iRtn = 0;
		while(bFound == false) {
		    if(preLinks.get(iRtn).GetUriTo().equals(lineItem) == true) {
		    	bFound = true;
		    }
		    else {
		    	iRtn++;
		    	if(iRtn == preLinks.size())
		    		iRtn = 0;
		    }
		}
		return(iRtn);
    }			

	private String ModifyLabel(String orig, String role, UnsortedRow curNode) {
		String rtnStr = orig;		
		if(orig.contains("Balance")) {
			if(role.contains("Start")) {
				if(curNode.GetStartDateStr().length() == 0)
					rtnStr = orig + " at " + curNode.GetInstantDateStr();
				else
					rtnStr = orig + " at " + curNode.GetStartDateStr();					
			}
			else {
				if(curNode.GetEndDateStr().length() == 0)
					rtnStr = orig + " at " + curNode.GetInstantDateStr();
				else
					rtnStr = orig + " at " + curNode.GetEndDateStr();
			}
		}
		return(rtnStr);
	}
	/*
    private ArrayList<EquityColRef> FindEquityColumns(RootNode rootNode, LabExtract labExtract) {
    	ArrayList<EquityColRef> rtnArray = new ArrayList<EquityColRef>();
        String            itemStr = "";
        String            fromStr = "";
        int               iNdx;
        boolean           bFound = false;
        EquityColRef      equityColRef = null;
        
        for(LineItem curNode: rootNode.GetLineItems()) {
             itemStr = curNode.GetItemStr();
             iNdx = itemStr.indexOf("#");
             itemStr = itemStr.substring(iNdx + 1);
             iNdx = 0;
             bFound = false;
             while((iNdx < labExtract.GetLabelArcs().size()) && (bFound == false)) {
            	 if(labExtract.GetLabelArcs().get(iNdx).GetUriFrom().equals(curNode.GetItemStr())) {
            		 bFound = true;
            		 itemStr = labExtract.GetLabelArcs().get(iNdx).GetData();
            		 fromStr = labExtract.GetLabelArcs().get(iNdx).GetLocalFrom();
            	 }
              	 iNdx++;
             }
             bFound = false;
            // iNdx = 0;
            // while((iNdx < rtnArray.size()) && (bFound == false)) {
           // 	 if(rtnArray.get(iNdx).GetRefData().equals(itemStr)) {
           // 		 bFound = true;
           // 	 }
           // 	 iNdx++;
            // }
             if(bFound == false) {
            	 equityColRef = new EquityColRef();
            	 equityColRef.SetRefData(itemStr);
            	 equityColRef.SetFromStr(fromStr);
            	 rtnArray.add(equityColRef);
             }
        }
    	return(rtnArray);
    }
      */         
	private TableRowCls FindMyTableRows(RootNode rootNode, int myNdx) {
		TableRowCls rtn = null;
		int         i = 0;
		
		while((rtn == null) && (i < rootNode.GetTableCls().GetRows().size())) {
			if(myNdx == rootNode.GetTableCls().GetRows().get(i).GetTblRowNdx()) {
				rtn = rootNode.GetTableCls().GetRows().get(i);
				// now we set to unfindable number in case of a following duplicate
				//rootNode.GetTableCls().GetRows().get(i).SetTblRowNdx(-1);
			}
		    i++;	
		}
	return(rtn);
	}
	
	/*
	private boolean DeleteItTest(int iMax, int iMyCount) {
		boolean bRtn = false;
		
		if(iMax >= 20)  {
            if (iMyCount < 5)
				bRtn = true;
		}
		else {
			if(iMax >= 10) {
				if(iMyCount < 3)
					bRtn = true;
			}
			else {
				if(iMyCount < 2)
					bRtn = true;
			}
		}
		return(bRtn);
	}
	
	
	private ArrayList<Integer> ArraingeColumns(ArrayList<TableColumn> tableColumns) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		int i = 0;
		
		// for now leave alone the list
		for(TableColumn tc : tableColumns) {
			rtnArray.add(i);
			i++;
		}
		return(rtnArray);
	 }

	
	private  ArrayList<Integer> FindUnmarkedSups(ArrayList<TableColumn> tableColumns) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		int                i;
		
		for(i = 0; i < tableColumns.size();  i++) {
			if(tableColumns.get(i).GetPeriod().GetSupNdx() == 0)
				rtnArray.add(i);
		}
	    return(rtnArray);
	}
	
	private ArrayList<Integer> FindTheseSups(ArrayList<TableColumn> tableColumns, int iSupNdx) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
	    int                i = 0;
		int                testSupStr = 0;
	    
		switch (iSupNdx) {
		    case 0:
			    testSupStr = CONSTANTS.THREE_MONTHS_NDX;
			    break;
		    case 1:
			    testSupStr = CONSTANTS.SIX_MONTHS_NDX;
			    break;
		    case 2:
			    testSupStr = CONSTANTS.NINE_MONTHS_NDX;
			    break;
		    case 3:
			    testSupStr = CONSTANTS.TWELVE_MONTHS_NDX;
			    break;
	    }
		for(i = 0; i < tableColumns.size(); i++) {
			if(tableColumns.get(i).GetPeriod().GetSupNdx() == testSupStr)
				rtnArray.add(i);
		}
		return(rtnArray);
	}
	
	
	// this oreders two dates returning the lastest first and the oldest last
	private ArrayList<Integer> DoTwoColumnOrder(ArrayList<TableColumn> tableColumns, int int1, int int2) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		

	    if(IsFirstBeforeLast(GetPeriodDate(tableColumns.get(int1).GetPeriod()), GetPeriodDate(tableColumns.get(int2).GetPeriod()))) {
		    rtnArray.add(int2);
		    rtnArray.add(int1);						
	    }
	    else {
		    rtnArray.add(int1);						
		    rtnArray.add(int2);
	    }
	    return(rtnArray);
	}

	private String GetPeriodDate(Period period) {
		String rtnStr = "";
		
		if(period.GetInstant() != null) 
			rtnStr = period.GetInstant();
		else
			rtnStr = period.GetStartDate();
		return(rtnStr);
	}
	
	private boolean IsFirstBeforeLast(String firstDate, String lastDate) {
		boolean        bRtn = true;
    	DateFormat     df = new SimpleDateFormat("yyyy-MM-dd");
		Date           fDate;
		Date           lDate;
		
		try {
            fDate = df.parse(firstDate);
    	    lDate = df.parse(lastDate);
    	    if(fDate.after(lDate))
    	    	bRtn = false;
		}
		catch (Exception e) {
			// do nothing just move forward
		}
		return(bRtn);
	}
	*/
	
	private int FindPrelinkIndex(String GaapRef, ArrayList<PreLink> preLinks) {
		int     iRtn = -1;
		boolean bFound = false;
		int     iLooking = 0;
		
		// first look for exact match
		while((bFound == false) && (iLooking < preLinks.size())) {
			if(GaapRef.equals(preLinks.get(iLooking).GetLocalTo())) {
				bFound = true;
				iRtn = iLooking;
			}
			iLooking++;
		}
		if(iRtn == -1) {
			String shortened  = "";
			int    i;
			i = GaapRef.indexOf("_");
			if(i > 0) {
				shortened = GaapRef.substring(i+1);
				iLooking = 0;
				while((bFound == false) && (iLooking < preLinks.size())) {
					if(shortened.equals(preLinks.get(iLooking).GetLocalTo())) {
						bFound = true;
						iRtn = iLooking;
					}
					iLooking++;
				}
			}
		}
		// if not found try closeee
		if(iRtn == -1) {
			iLooking = 0;
		    while((bFound == false) && (iLooking < preLinks.size())) {
			    if(preLinks.get(iLooking).GetLocalTo().contains(GaapRef)) {
				    bFound = true;
				    iRtn = iLooking;
			    }
			    iLooking++;
		    }
		}
		return(iRtn);
	}
	
	private void WriteEquityDateRefs(Connection con, SubmissionInfo subInfo, ArrayList<EquityColumn> equityColRefs,
			                         LabExtract labExtract) {
	    int         i = 0;
	    MySqlAccess mySqlAccess = new MySqlAccess();
	    String      dateRefSupStr = "";
	    int         j;
	    String      modifiedDimension;
	    
	    for(i = 0; i < equityColRefs.size(); i++) {
	    	dateRefSupStr = "";
	    	if(equityColRefs.get(i).GetDimension().length() > 0) {
	    		modifiedDimension = equityColRefs.get(i).GetDimension().replaceFirst(":", "_");
	    		j = 0;
	    		while((j < labExtract.GetLabelArcs().size()) && (dateRefSupStr.length() == 0)) {
	    			if(labExtract.GetLabelArcs().get(j).GetLocalFrom().contains(modifiedDimension)) {
	    				dateRefSupStr = labExtract.GetLabelArcs().get(j).GetData();
	    			}
	    			else
	    				j++;
	    		}
	    	}
	    	equityColRefs.get(i).SetDateRef(mySqlAccess.WriteEquityDateRef(con, subInfo, dateRefSupStr));
	    }
	}
	
	private void WriteDateRefs(Connection con, SubmissionInfo subInfo, ArrayList<TableColumn> tableColumns) {
	    int         i = 0;
	    MySqlAccess mySqlAccess = new MySqlAccess();
	    
	    for(i = 0; i < tableColumns.size(); i++) {
			    tableColumns.get(i).SetRefUid(mySqlAccess.WriteDateRef(con, subInfo, tableColumns.get(i).GetPeriod()));
	    }
	}
	
	private void ClearMapped(ArrayList<XmlGrouping> xmlGrouping) {
		
		
		for(XmlGrouping xmlgroup:  xmlGrouping) {
			for(XmlDetail xmlDetail: xmlgroup.GetDetails()) {
				xmlDetail.SetMapped(CONSTANTS.NOT_MAPPED);
			}			
		}
	}
	
	private void FindTableRows(String NameSpaceAbbrev, XmlExtract xmlExtract, ArrayList<XmlGrouping> groupings, 
			                   String tag, RootNode curNode, int tblRowIndex) {
		int                  groupNdx = 0;
		int                  xmlDetailNdx = 0;
		TableCell            newRow = null;
		TableRowCls          row = new TableRowCls();
		//String               dimension = null;
		

		groupNdx = FindGroupIndex(NameSpaceAbbrev, xmlExtract.GetXmlGrouping());
		row.SetAbbrevNameSpace(NameSpaceAbbrev);
		row.SetTblRowNdx(tblRowIndex);
		if(groupNdx != -1) {  // if not found no rows
		    while(xmlDetailNdx < groupings.get(groupNdx).GetDetails().size()) {
			    if( groupings.get(groupNdx).GetDetails().get(xmlDetailNdx).GetTag().equals(tag)) {
			        newRow = BuildTableCell(groupNdx, xmlDetailNdx);
			        row.SetTableCell(newRow);
			    }
			    xmlDetailNdx++;
		    }
		}
	    curNode.SetTableCls(row);
	}
	
	private TableCell BuildTableCell(int groupNdx, int xmlDetailNdx) {
		TableCell newRow = new TableCell();
		
		newRow = new TableCell();
		newRow.SetGroupNdx(groupNdx);
		newRow.SetDetailNdx(xmlDetailNdx);
        return(newRow);		
	}
	private ArrayList<TableColumn> FindTableColumns(Connection con, ArrayList<XmlGrouping> groupings, 
			                                        ArrayList<DimensionGroup> dimensions, TableCls table) {
		ArrayList<TableColumn> tblCol = new ArrayList<TableColumn>();
		
		tblCol = ArraingePeriods(con, groupings, dimensions, table);
		return(tblCol);
	}
	
	private ArrayList<TableColumn> ArraingePeriods(Connection con, ArrayList<XmlGrouping> groupings, 
			                                       ArrayList<DimensionGroup> dimensions,TableCls table) {
		ArrayList<TableColumn> tblCol = new ArrayList<TableColumn>();
        int                    iGroup;
        int                    iDetail;
        String                 contextStr;
        Period                 period = null;
        int                    iDetailCnt = 0;
        int                    iTableRow = 0;
        //boolean                bHavePeriods = false;
        
		for(TableRowCls trc: table.GetRows()) {  // for each ROW of table
			for(TableCell tc: trc.GetTableCells()) {
		        iGroup = tc.GetGroupNdx();
		        iDetail = tc.GetDetailNdx();
		        //XXXXXX
		        contextStr = groupings.get(iGroup).GetDetails().get(iDetail).GetContextRef();
		        period = FindPeriod(contextStr, dimensions);
		        if(period.GetStartDate().length() > 0) {
		        	UpdateColumnList(con, tblCol, period, contextStr, iTableRow, iDetailCnt);
		        	//bHavePeriods = true;
		        }
		        iDetailCnt++;
			}
			iTableRow++;
		}
		iDetailCnt = 0;
		iTableRow = 0;
		for(TableRowCls trc: table.GetRows()) {  // for each ROW of table
			iDetailCnt = 0;  // Added
		    for(TableCell tc: trc.GetTableCells()) {
	            iGroup = tc.GetGroupNdx();
		        iDetail = tc.GetDetailNdx();
		        contextStr = groupings.get(iGroup).GetDetails().get(iDetail).GetContextRef();
		        period = FindPeriod(contextStr, dimensions);
                if(period.GetInstant().length() > 0) {
                	//if(bHavePeriods == false)
		        	    UpdateInstantColumnList(con, tblCol, period, contextStr, iTableRow, iDetailCnt);
                	//else
                		//UpdateInstantInPeriodsColumnList(con, tblCol, period, contextStr, iTableRow, iDetailCnt);
		        }
		        //if(groupings.get(iGroup).GetDetails().get(iDetail).)
		        iDetailCnt++;
		    }
		    iTableRow++;
		}
		return(tblCol);
	}
	
	private final int INS_BEFORE = 0;
	private final int INS_AFTER  = 1;
	private final int INS_SAME   = 2;
	
	/**
	private void UpdateInstantInPeriodsColumnList(Connection con, ArrayList<TableColumn> tblCol,
            Period period, String contextStr, int iTableRow, int iDetailCnt) {
		UTILITIES   utilities = new UTILITIES();
        Date        myDatePlus = null;
        Date        myDateMinus = null;
        Date        myDate = null;
        boolean     bContinue = true;
        int         iColNdx = 0;
        int         iRtn;
        
        myDate = utilities.DateConverter(con, period.GetInstant());
        myDatePlus = utilities.AddDays(myDate, 1);
        myDateMinus = utilities.AddDays(myDate, -1);
        while(bContinue) {   // we only check of extact matches on first pass and only check periods -
            if(tblCol.get(iColNdx).GetPeriod().GetStartDate().length() == 0 ) {
            	iRtn = CheckInstantRelationship(myDate, tblCol.get(iColNdx).GetInstant());
            }
            else {
            	
            }
            
        }
	}
	
	**/
	private void UpdateInstantColumnList(Connection con, ArrayList<TableColumn> tblCol,
			                             Period period, String contextStr, int iTableRow, int iDetailCnt) {
		UTILITIES   utilities = new UTILITIES();
		TableColumn newTblCol = null;
		int         iRtn;
		int         iColNdx = 0;
		boolean     bContinue = true;
		
		Date myStart = utilities.DateConverter(con, period.GetInstant());
		if(tblCol.size() == 0) {
			newTblCol = BuildInstantTableColumn(contextStr, myStart, period, iTableRow, iDetailCnt);
			tblCol.add(newTblCol);
		}
		else {
			while(bContinue) {
			    if(tblCol.get(iColNdx).GetPeriod().GetStartDate().length() == 0) {
			    	iRtn = CheckInstantRelationship(myStart, tblCol.get(iColNdx).GetInstant());
			    }
			    else {
			    	iRtn = CheckInstantToPeriodRelationship(myStart, tblCol.get(iColNdx).GetStartDate(),
			    			                                tblCol.get(iColNdx).GetEndDate());
			    }
			
			    switch (iRtn) {
			        case  INS_BEFORE:
						newTblCol = BuildInstantTableColumn(contextStr, myStart, period, iTableRow, iDetailCnt);
						tblCol.add(iColNdx, newTblCol);
			        	bContinue = false;
			            break;
			            
			        case INS_AFTER:
			        	iColNdx++;
			        	if(iColNdx == tblCol.size()) {
							newTblCol = BuildInstantTableColumn(contextStr, myStart, period, iTableRow, iDetailCnt);
							tblCol.add(newTblCol);
				        	bContinue = false;
			        	}
				        break;
				        
			        case INS_SAME:
			        	ContextNdx contextNdx = new ContextNdx();
			        	contextNdx.SetContextRef(contextStr);
			        	contextNdx.SetTableRowNdx(iTableRow);
			        	contextNdx.SetTableCellNdx(iDetailCnt);
			        	tblCol.get(iColNdx).SetContextRef(contextNdx);
			        	bContinue = false;
			        	break;
			    }
			}
		}
	}
	
	private int CheckInstantRelationship(Date myStart, Date prevDate) {
		int iRtn = 0;
		
		if(myStart.after(prevDate))
			iRtn = INS_BEFORE;
		else {
			if(myStart.equals(prevDate))
				iRtn = INS_SAME;
			else
				iRtn = INS_AFTER;
		}
		return(iRtn);
	}
	
	private int CheckInstantToPeriodRelationship(Date myStart, Date prevStart, Date prevEnd) {
        Date        myDatePlus = null;
        Date        myDateMinus = null;
        //boolean     bContinue = true;
        //int         iColNdx = 0;
        UTILITIES utilities = new UTILITIES();
        
        myDatePlus = utilities.AddDays(myStart, 1);
        myDateMinus = utilities.AddDays(myStart, -1);
 	    int iRtn = INS_AFTER;
	    
	    if((myDatePlus.equals(prevStart)) || (myDatePlus.equals(prevEnd)))
	    	iRtn = INS_SAME;
	    else {
	    	if((myDateMinus.equals(prevStart)) || (myDateMinus.equals(prevEnd)))
                iRtn = INS_SAME;
	    	else {
		    	if((myStart.equals(prevStart)) || (myStart.equals(prevEnd)))
	                iRtn = INS_SAME;
	    	}
	    }
	    return(iRtn);
	}
	
	private void UpdateColumnList(Connection con, ArrayList<TableColumn> tblCol,
			                      Period period, String contextStr, int iTableRow, int iDetailCnt) {
		UTILITIES   utilities = new UTILITIES();
		TableColumn newTblCol = null;
		int         iRtn;
		int         iColNdx = 0;
		boolean     bContinue = true;
		
		Date myStart = utilities.DateConverter(con, period.GetStartDate());
		Date myEnd = utilities.DateConverter(con, period.GetEndDate());
		if(tblCol.size() == 0) {
			newTblCol = BuildTableColumn(contextStr, myStart, myEnd, period, iTableRow, iDetailCnt);
			tblCol.add(newTblCol);
		}
		else {
			while(bContinue) {
			    iRtn = CheckRelationship(myStart, myEnd, period.GetSupNdx(), tblCol.get(iColNdx).GetStartDate(),
			    		                 tblCol.get(iColNdx).GetEndDate(), tblCol.get(iColNdx).GetPeriod().GetSupNdx());
			    switch (iRtn) {
			        case  INS_BEFORE:
						newTblCol = BuildTableColumn(contextStr, myStart, myEnd, period, iTableRow, iDetailCnt);
						tblCol.add(iColNdx, newTblCol);
			        	bContinue = false;
			            break;
			            
			        case INS_AFTER:
			        	iColNdx++;
			        	if(iColNdx == tblCol.size()) {
							newTblCol = BuildTableColumn(contextStr, myStart, myEnd, period, iTableRow, iDetailCnt);
							tblCol.add(newTblCol);
				        	bContinue = false;
			        	}
				        break;
				        
			        case INS_SAME:
			        	ContextNdx contextNdx  = new ContextNdx();
			        	contextNdx.SetContextRef(contextStr);
			        	contextNdx.SetTableRowNdx(iTableRow);
			        	contextNdx.SetTableCellNdx(iDetailCnt);
			        	tblCol.get(iColNdx).SetContextRef(contextNdx);
			        	bContinue = false;
			        	break;
			    }
			}
		}
	}
	
	private int CheckRelationship(Date myStart, Date myEnd, int mySup, Date prevStart, Date prevEnd, int prevSup) {
	    int iRtn = 0;
	    
	    // first order of business - check durations
	    if(mySup < prevSup) {
	    	iRtn = INS_BEFORE;
	    }
	    else {
	    	if(mySup > prevSup)
	    		iRtn = INS_AFTER;
	    	else {
	    		if(myStart.after(prevStart))
	    			iRtn = INS_BEFORE;
	    		else {
	    			if(myStart.equals(prevStart))
	    				iRtn = INS_SAME;
	    			else
	    			    iRtn = INS_AFTER;
	    		}
	    	}
	    }
	    return(iRtn);
	}
	
	private TableColumn BuildInstantTableColumn(String contextStr, Date instant, Period period, int iTableRow, int iColNdx) {
		
		TableColumn newTblCol = new TableColumn();
		ContextNdx  newContext = new ContextNdx();
		
		newContext.SetContextRef(contextStr);
		newContext.SetTableRowNdx(iTableRow);
		newContext.SetTableCellNdx(iColNdx);
		newTblCol.SetContextRef(newContext);
		newTblCol.SetInstant(instant);
		newTblCol.SetPeriod(period);
        return(newTblCol);
	}
	
	private TableColumn BuildTableColumn(String contextStr, Date start, Date end,
			                             Period period, int iTableRow, int iColNdx) {
		TableColumn newTblCol = new TableColumn();
		ContextNdx  newContext = new ContextNdx();
		
		newContext.SetContextRef(contextStr);
		newContext.SetTableRowNdx(iTableRow);
		newContext.SetTableCellNdx(iColNdx);
		newTblCol.SetContextRef(newContext);
		newTblCol.SetStartDate(start);
		newTblCol.SetEndDate(end);
		newTblCol.SetPeriod(period);
        return(newTblCol);
	}
	private Period FindPeriod(String contextStr, ArrayList<DimensionGroup> dimensions) {
		Period rtnPeriod = null;
		int    i = 0;
		
		while((i < dimensions.size()) && (rtnPeriod == null)) {
			if(dimensions.get(i).GetContextId().equals(contextStr)) {
				rtnPeriod = dimensions.get(i).GetPeriod();
			}
			i++;
		}
		return(rtnPeriod);
	}
	

	
	private String ExtractGaapRefKey(String origStr) {
		String rtnStr = "";
		int    i = origStr.indexOf("#");
		
		rtnStr = origStr.substring(i+ 1);
		
		return(rtnStr);
	}
	
	private String ExtractSpaceKey(String origStr, ArrayList<NameSpaceLink>  NameSpaceLinks) {
		String workStr = "";
		String rtnStr = "";
		
		workStr = ExtractGaapRefKey(origStr);
		rtnStr = ExtractNameSpace(workStr, NameSpaceLinks);
		return(rtnStr);
		
	}
	private String ExtractNameSpace(String origStr,  ArrayList<NameSpaceLink>  NameSpaceLinks) {
		String rtnStr = "";
		String workStr = "";
	    int    i;
		
		i = origStr.indexOf("_");
		workStr = origStr.substring(0, i);
		for(NameSpaceLink nsl: NameSpaceLinks) {
			if(nsl.GetAbbrev().equals(workStr))
				rtnStr = nsl.GetNameSpace();
		}
		return(rtnStr);
	}
	
	private LabelRoleRtn FindRowLabel(String fromTag, LabExtract labExtract, String periodType, String role) {
		LabelRoleRtn rtnCls = new LabelRoleRtn();;
		int    i = 0;
		String posRtnStr = "";
		int    iPosCnt = 0;
		int    RoleId = -1;
		
		while((rtnCls.GetLabel().length() == 0) && (i < labExtract.GetLabelArcs().size())) {
			if(labExtract.GetLabelArcs().get(i).GetXbrlRole().toLowerCase().contains("label")) { // was equal
			    if(fromTag.equals(labExtract.GetLabelArcs().get(i).GetUriFrom())) {
		    		if(role == null)
			            rtnCls.SetLabel(labExtract.GetLabelArcs().get(i).GetData());
		    		else {
		    			if((labExtract.GetLabelArcs().get(i).GetXbrlRole().contains("label"))  ||
		    			   (labExtract.GetLabelArcs().get(i).GetXbrlRole().contains("Label"))) {
		    			    if(labExtract.GetLabelArcs().get(i).GetXbrlRole().contains("terse")) {
			    			    posRtnStr = labExtract.GetLabelArcs().get(i).GetData();
		    			    	RoleId = 3;
		    			    }
		    			    else {
		    			    	if(RoleId == -1) {
		    			    	    RoleId = 1;
				    			    posRtnStr = labExtract.GetLabelArcs().get(i).GetData();
		    			    	}
		    			    }
		    			    iPosCnt++;
		    			}
		    			if(labExtract.GetLabelArcs().get(i).GetUriTo().equals(role)) {
		    				rtnCls.SetLabel(labExtract.GetLabelArcs().get(i).GetData());
		    			}
		    		}
			    }
			}
			i++;
		}
		// check if found a match
		if(rtnCls.GetLabel().length() == 0) {
			if(iPosCnt >= 1) {  // no role match, but use it anyway
				rtnCls.SetLabel(posRtnStr);
				rtnCls.SetRoleId(RoleId);
			}
		}
		return(rtnCls);
	}
	
	private String ExtractTag(String origUri) {
		String rtnStr = "";
		int    i = 0;
		
		i = origUri.indexOf("_");
		rtnStr = origUri.substring(i+ 1);
		return(rtnStr);
	}
	
	private String ExtractTaxKey(String origUri) {
		String rtnStr = "";
		int    i = origUri.indexOf("#");
		rtnStr = origUri.substring(0, i);
		return(rtnStr);
	}
	
	private ArrayList<Integer> WriteDataToDb(Connection con, MySqlAccess mySqlAccess, SubmissionInfo subInfo, String abbrevStr, String tag, 
			                                  String RowLabel, ArrayList<XmlGrouping> groupings, ArrayList<DimensionGroup> contexts, 
			                                  int iTemplateParseUid, String GaapRefKey, String TaxonomyPathKey, ArrayList<UnitLink> unitLinks,
			                                  int companyTaxonomyUid, ArrayList<NameSpaceLink> nameSpaceLink, ArrayList<FootnoteLoc> footNotes,
			                                  String TaxonomySpaceKey, ArrayList<CalParent> calParents, CalCheckList calCheckList,
			                                  boolean IsParenthetical, PreLink preLink, RootNode rootNode, XsdExtract xsdExtract, 
			                                  ArrayList<Integer> previousFieldRefs,  TableRowCls tableRowCls, ArrayList<TableColumn> tableColumns, 
			                                  Debug debug, long hashedUid, ArrayList<FactDetail> factDetails) {
		boolean                   bFoundDetail = false;
		FieldRowInsertCls         fieldRowInsertCls = new FieldRowInsertCls();
		CoreCell                  coreCell = new CoreCell();
		int                       taxonomyUid = 0;
		int                       gaapRefUid = 0;
		String                    workStr;
    	ColumnFormatData          columnFormatData = null;
		int                       iXmlDetailCnt = 0;
		XmlDetail                 xmlDetail;
		int                       iColumnNdx = 0;
		//boolean                   bNewRow = false;
		ArrayList<Group>          groups = new ArrayList<Group>();
		ArrayList<MemberGroup>    memberGroup = new ArrayList<MemberGroup>();
		ArrayList<RowDef>         rowDefinitions = new ArrayList<RowDef>();
		ArrayList<Integer>        xmlDetails = new ArrayList<Integer>();
		ArrayList<Integer>        groupDetailMember = new ArrayList<Integer>();
		ArrayList<Integer>        facts = new ArrayList<Integer>();
		ArrayList<FactDimension>  factDimensions = new ArrayList<FactDimension>();
		
		
		//groupNdx = FindGroupIndex( abbrevStr, groupings);
		bFoundDetail = false;
		if(IsParenthetical == false) {
			previousFieldRefs = new ArrayList<Integer>();
		}
		//if(tableRowCls.GetTableCells().size() > )
		for (iColumnNdx = 0; iColumnNdx < tableRowCls.GetTableCells().size(); iColumnNdx++) {
			FindDetails(groupings.get(tableRowCls.GetTableCells().get(iColumnNdx).GetGroupNdx()).GetDetails().get(tableRowCls.GetTableCells().get(iColumnNdx).GetDetailNdx()),
					                               contexts, xmlDetails, facts, factDimensions);
		}
		FindGroupForThisDetail(con, xmlDetails, contexts, groups, subInfo.GetExchangeSymbol(), companyTaxonomyUid, nameSpaceLink, xsdExtract,
				               memberGroup, groupDetailMember);			
		iColumnNdx = 0;
		for (iXmlDetailCnt = 0; iXmlDetailCnt < tableRowCls.GetTableCells().size(); iXmlDetailCnt++) {
	        if(tableRowCls.GetTableCells().get(iXmlDetailCnt).IsOmitted(factDetails) == false) {
	        	//WLW testColumnOmitted
	        	//if(rootNode.IsColumnOmitted(iColumnNdx))
	        	//	iColumnNdx++;
	        	//else {
			        int iGroupNdx = tableRowCls.GetTableCells().get(iXmlDetailCnt).GetGroupNdx();
			        int iDetailNdx = tableRowCls.GetTableCells().get(iXmlDetailCnt).GetDetailNdx();
			        xmlDetail = groupings.get(iGroupNdx).GetDetails().get(iDetailNdx);
                    xmlDetail.SetMapped(CONSTANTS.MAPPED);
			        workStr = abbrevStr + "_" + tag;
		            CheckForCalItem(workStr, calCheckList, calParents);
	                bFoundDetail = true;
			        debug.WriteDebug("Data: " + xmlDetail.GetData());
			        debug.WriteDebug("  LookingUp: " + xmlDetail.GetContextRef());
	                int refUid = FindTableColumnRefId(xmlDetail.GetContextRef(), tableColumns);
			        iColumnNdx++;
	                debug.WriteDebug("  Ref: " + refUid );
			        if(refUid != -1) {
			            xmlDetail.SetMapped(1);
		   	            columnFormatData = FormatColumnData(con, xmlDetail);
	                    taxonomyUid = mySqlAccess.GetTaxonomyUid(con, TaxonomySpaceKey, subInfo.GetCompanyUid(), true);
                        gaapRefUid = mySqlAccess.GetGaapRefNdx(con, GaapRefKey, taxonomyUid, xsdExtract);
                        int myRowDef = FindMyRowDefinition(rowDefinitions, groupDetailMember.get(iXmlDetailCnt));
                    
			            fieldRowInsertCls = mySqlAccess.InsertColumnData(con, RowLabel, factDimensions.get(facts.get(iXmlDetailCnt)).GetFieldsRowStr(), 
                                                                         columnFormatData.ColumnData, subInfo, iTemplateParseUid, refUid,
                                                                         IsParenthetical, gaapRefUid, hashedUid, xmlDetail.GetNil());
		                if(fieldRowInsertCls.fieldsRowStrUid > 0) {
		            	    factDimensions.get(facts.get(iXmlDetailCnt)).SetFieldsRowStr(fieldRowInsertCls.fieldsRowStrUid);
		            	    rowDefinitions.get(myRowDef).SetFieldsRowStr(fieldRowInsertCls.fieldsRowStrUid);
		               	    if(IsParenthetical == false) {
		               	        previousFieldRefs.add(fieldRowInsertCls.fieldsRowStrUid);
		           	        }
		           	        else {
		               		    // Not sure what to do here I find more entries than previousFieldRefs
		               		    int prevNdx = 0;
		               	        if(iXmlDetailCnt < previousFieldRefs.size()) 
		               		        prevNdx = previousFieldRefs.get(iXmlDetailCnt);
	                	        mySqlAccess.AddCoreParentheticalRef(con, prevNdx, fieldRowInsertCls.fieldsRowStrUid);
		                    }
		                    coreCell.SetUid(fieldRowInsertCls.fieldsLocatedUid);
	    	                coreCell.SetElementUid(gaapRefUid);
			                coreCell.SetUnitUid(FindUnitLinkUid(xmlDetail.GetUnitRef(), unitLinks));
		                    coreCell.SetStartDate(FindDate(con, xmlDetail.GetContextRef(), contexts, true));
		                    coreCell.SetEndDate(FindDate(con, xmlDetail.GetContextRef(), contexts, false));
	                        coreCell.SetScale(columnFormatData.Scale);
		                    if(preLink != null) {
			                    coreCell.SetRole(preLink.GetRoleUid());
		                        coreCell.SetNegated(preLink.GetNegated());
			                }
		                    mySqlAccess.InsertCoreCell(con, coreCell);
		                    if(calCheckList.bIsParent == true) {
			                    calParents.get(calCheckList.ParentNdx).SetCoreCellUid(coreCell.GetUid());
			                    calCheckList.bPrevParent = true;
			                    if(calCheckList.ParentNdx == calParents.size())
				                    calCheckList.bDone = true;
			                }
		                    if(calCheckList.bIsChild == true) {
		                        int colPos = SetChildNdx(xmlDetail.GetContextRef(), rootNode.GetTableColumns());
		                        calParents.get(calCheckList.ParentNdx).GetThisChild(calCheckList.ChildNdx).SetCoreCellUid(coreCell.GetUid(), colPos);
		                    }
			                calCheckList.bIsParent = false;
		                    calCheckList.bIsChild = false;
		                    CheckForDimensionsToWrite(con, coreCell.GetUid(), rowDefinitions.get(myRowDef), memberGroup, groups, 0);
			                CheckForFootNotes(con, coreCell.GetUid(), xmlDetail.GetContextRef(), footNotes);
				        }
			        }
	        	//}
		    }
		}
		if(bFoundDetail == true) {
            if(calCheckList.bPrevParent == true) {  // if previous parent  
		        calCheckList.ParentNdx++;
				calCheckList.ChildNdx = 0;
				calCheckList.bLastChild = false;
				calCheckList.bPrevParent = false;
			}
		}
		if(bFoundDetail == false) { // it's an abstract
			taxonomyUid = mySqlAccess.GetTaxonomyUid(con, TaxonomyPathKey, subInfo.GetCompanyUid(), false);
			gaapRefUid = mySqlAccess.GetGaapRefNdx(con, GaapRefKey, taxonomyUid, xsdExtract);
			int RoleUid = 1;
			boolean bNegated = false;
			if(preLink != null) {
				RoleUid = preLink.GetRoleUid();
				bNegated = preLink.GetNegated();
			}
			mySqlAccess.InsertAbstract(con, RowLabel, gaapRefUid, subInfo, iTemplateParseUid, IsParenthetical,
					                   RoleUid, bNegated, hashedUid);
		}
		return(previousFieldRefs);
	}
	
	private ArrayList<Integer> WriteEquityDataToDb(Connection con, MySqlAccess mySqlAccess, SubmissionInfo subInfo, String abbrevStr, String tag, 
            String RowLabel, ArrayList<XmlGrouping> groupings, ArrayList<DimensionGroup> contexts, 
            int iTemplateParseUid, String GaapRefKey, String TaxonomyPathKey, ArrayList<UnitLink> unitLinks,
            int companyTaxonomyUid, ArrayList<NameSpaceLink> nameSpaceLink, ArrayList<FootnoteLoc> footNotes,
            String TaxonomySpaceKey, ArrayList<CalParent> calParents, CalCheckList calCheckList,
            boolean IsParenthetical, PreLink preLink, RootNode rootNode, XsdExtract xsdExtract, 
            ArrayList<Integer> previousFieldRefs, ArrayList<TableColumn> tableColumns, 
            Debug debug, long hashedUid, ArrayList<FactDetail> factDetails,
            UnsortedRow usr, ArrayList<EquityColumn> equityColumns) {
        boolean                   bFoundDetail = false;
        FieldRowInsertCls         fieldRowInsertCls = new FieldRowInsertCls();
        CoreCell                  coreCell = new CoreCell();
        int                       taxonomyUid = 0;
        int                       gaapRefUid = 0;
        String                    workStr;
        ColumnFormatData          columnFormatData = null;
        int                       iXmlDetailCnt = 0;
        XmlDetail                 xmlDetail;
        int                       fieldsLocatedUid = 0;
        

         for (iXmlDetailCnt = 0; iXmlDetailCnt < usr.GetRowDetails().size(); iXmlDetailCnt++) {
            int iGroupNdx = usr.GetRowDetails().get(iXmlDetailCnt).GetGroupNdx();
            int iDetailNdx =usr.GetRowDetails().get(iXmlDetailCnt).GetDetailNdx();
                
            xmlDetail = groupings.get(iGroupNdx).GetDetails().get(iDetailNdx);
            workStr = abbrevStr + "_" + tag;
            CheckForCalItem(workStr, calCheckList, calParents);
            int refUid = FindEquityRefId(xmlDetail.GetContextRef(), equityColumns);
            if(refUid != 0) {
                columnFormatData = FormatColumnData(con, xmlDetail);
                taxonomyUid = mySqlAccess.GetTaxonomyUid(con, TaxonomySpaceKey, subInfo.GetCompanyUid(), true);
                gaapRefUid = mySqlAccess.GetGaapRefNdx(con, GaapRefKey, taxonomyUid, xsdExtract);
                fieldRowInsertCls = mySqlAccess.InsertColumnData(con, RowLabel, fieldsLocatedUid, 
                                                                 columnFormatData.ColumnData, subInfo, iTemplateParseUid, refUid,
                                                                 IsParenthetical, gaapRefUid, hashedUid, xmlDetail.GetNil());
                if(fieldRowInsertCls.fieldsRowStrUid > 0) {
                	fieldsLocatedUid = fieldRowInsertCls.fieldsRowStrUid;
                    if(IsParenthetical == false) {
                        previousFieldRefs.add(fieldRowInsertCls.fieldsRowStrUid);
                    }
                    else {
                        int prevNdx = 0;
                        if(iXmlDetailCnt < previousFieldRefs.size()) 
                            prevNdx = previousFieldRefs.get(iXmlDetailCnt);
                        mySqlAccess.AddCoreParentheticalRef(con, prevNdx, fieldRowInsertCls.fieldsRowStrUid);
                    }
                    coreCell.SetUid(fieldRowInsertCls.fieldsLocatedUid);
                    coreCell.SetElementUid(gaapRefUid);
                    coreCell.SetUnitUid(FindUnitLinkUid(xmlDetail.GetUnitRef(), unitLinks));
                    coreCell.SetStartDate(FindDate(con, xmlDetail.GetContextRef(), contexts, true));
                    coreCell.SetEndDate(FindDate(con, xmlDetail.GetContextRef(), contexts, false));
                    coreCell.SetScale(columnFormatData.Scale);
                    if(preLink != null) {
                        coreCell.SetRole(preLink.GetRoleUid());
                        coreCell.SetNegated(preLink.GetNegated());
                    }
                    mySqlAccess.InsertCoreCell(con, coreCell);
                    if(calCheckList.bIsParent == true) {
                        calParents.get(calCheckList.ParentNdx).SetCoreCellUid(coreCell.GetUid());
                        calCheckList.bPrevParent = true;
                        if(calCheckList.ParentNdx == calParents.size())
                            calCheckList.bDone = true;
                    }
                    if(calCheckList.bIsChild == true) {
                        int colPos = SetChildNdx(xmlDetail.GetContextRef(), rootNode.GetTableColumns());
                        calParents.get(calCheckList.ParentNdx).GetThisChild(calCheckList.ChildNdx).SetCoreCellUid(coreCell.GetUid(), colPos);
                    }
                    calCheckList.bIsParent = false;
                    calCheckList.bIsChild = false;
                    //CheckForDimensionsToWrite(con, coreCell.GetUid(), rowDefinitions.get(myRowDef), memberGroup, groups, 0);
                    CheckForFootNotes(con, coreCell.GetUid(), xmlDetail.GetContextRef(), footNotes);
                }
            }
        }
        if(bFoundDetail == true) {
            if(calCheckList.bPrevParent == true) {  // if previous parent  
                calCheckList.ParentNdx++;
                calCheckList.ChildNdx = 0;
                calCheckList.bLastChild = false;
                calCheckList.bPrevParent = false;
            }
        }
        if(bFoundDetail == false) { // it's an abstract
            taxonomyUid = mySqlAccess.GetTaxonomyUid(con, TaxonomyPathKey, subInfo.GetCompanyUid(), false);
            gaapRefUid = mySqlAccess.GetGaapRefNdx(con, GaapRefKey, taxonomyUid, xsdExtract);
            int RoleUid = 1;
            boolean bNegated = false;
            if(preLink != null) {
                RoleUid = preLink.GetRoleUid();
                bNegated = preLink.GetNegated();
            }
            mySqlAccess.InsertAbstract(con, RowLabel, gaapRefUid, subInfo, iTemplateParseUid, IsParenthetical,
                                       RoleUid, bNegated, hashedUid);
        }
        return(previousFieldRefs);
    }

	/*
	private boolean IsEquityOmitted(ArrayList<FactDetail> factDetails, int iGroupNdx, int iDetailNdx) {
		boolean bRtn = true;  // if we cant find - dont write it
		int     iNdx = 0;
		boolean bFound = false;
		
		while((bFound == false) && (iNdx < factDetails.size())) {
			if((factDetails.get(iNdx).GetGroupNdx() == iGroupNdx) && 
			    (factDetails.get(iNdx).GetItemNdx() == iDetailNdx)) {
				bFound = true;
			}
			else {
				iNdx++;
			}
		}
		if(bFound) 
			bRtn= factDetails.get(iNdx).GetOmit();
		return(bRtn);
	}
	*/	
	private void FindDetails(XmlDetail xmlDetail, ArrayList<DimensionGroup> contexts, ArrayList<Integer> xmlDetails,
			                 ArrayList<Integer> facts, ArrayList<FactDimension> factDimensions) {
		
		int           iIndex = 0;
		boolean       bFound = false;
		FactDimension fd;
		
		while((bFound == false) && (iIndex < factDimensions.size())) {
			if(xmlDetail.GetDimension().equals(factDimensions.get(iIndex).GetDimension())) {
				bFound = true;
				facts.add(iIndex);
			}
			iIndex++;
		}
		if(bFound == false) {
			fd = new FactDimension();
			fd.SetDimension(xmlDetail.GetDimension());
			factDimensions.add(fd);
			facts.add(factDimensions.size()-1);
		}
		iIndex = 0;
		while(iIndex < contexts.size()) {
			if(contexts.get(iIndex).GetContextId().equals(xmlDetail.GetContextRef()))
					xmlDetails.add(iIndex);
			iIndex++;
		}
	}
	
	private void FindGroupForThisDetail(Connection con, ArrayList<Integer> xmlDetails, ArrayList<DimensionGroup> contexts, ArrayList<Group> groups,
			                            String exchangeSymbol, int companyTaxonomyUid, ArrayList<NameSpaceLink>  nameSpaceLink,
			                            XsdExtract xsdExtract, ArrayList<MemberGroup> memberGroups, ArrayList<Integer> groupDetailMember) {
	    Group                  group = null;
	    int                    dimensions = 0;
	    MySqlAccess            mySqlAccess = new MySqlAccess();
	    ArrayList<Integer>     myGroupList = null;
	    Integer                iPrevGroup = 0;
	    
	    //ArrayList<Integer> foundDetails = new ArrayList<Integer>();

	    /*
		while((iIndex < contexts.size()) && (bFound == false)) {
			if(contexts.get(iIndex).GetContextId().equals(xmlDetail.GetContextRef())) 
					bFound = true;
			else
				iIndex++;
		}
		*/
	    /*
	    while(iIndex < contexts.size()) {
			if(contexts.get(iIndex).GetContextId().equals(xmlDetail.GetContextRef())) 
	    	    foundDetails.add(iIndex);
			iIndex++;
	    }
		if(bFound) {
		*/
	    for(Integer iIndex: xmlDetails) {
	    	myGroupList = new ArrayList<Integer>();
	    	dimensions = 0;
			while(dimensions < contexts.get(iIndex).GetDimension().size()) {
				group = new Group();
			    group.SetAxis(contexts.get(iIndex).GetDimension().get(0).GetData());
			    group.SetMember(contexts.get(iIndex).GetDimension().get(0).GetDim());
			    iPrevGroup = DoesGroupExist(groups, group);
			    if(iPrevGroup == -1) {
			    	String tempAxis = group.GetAxis().replace(":", "_");
			    	String tempMember = group.GetMember().replace(":", "_");
			        if(group.GetAxis().indexOf(exchangeSymbol) == 0) {
			        	group.SetMemberUid(mySqlAccess.GetGaapRefNdx(con, tempAxis, companyTaxonomyUid, xsdExtract));
			        }
			        else {
			        	int thisTaxonomyUid =  FindTaxonomyUid(con, nameSpaceLink, group.GetAxis());
			        	group.SetMemberUid(mySqlAccess.GetGaapRefNdx(con, tempAxis,thisTaxonomyUid, xsdExtract));
			        }
			        if(group.GetMember().indexOf(exchangeSymbol) == 0) {
			        	group.SetAxisUid(mySqlAccess.GetGaapRefNdx(con, tempMember, companyTaxonomyUid, xsdExtract));
			        }
			        else {
			        	int thisTaxonomyUid =  FindTaxonomyUid(con, nameSpaceLink, group.GetMember());
			        	group.SetAxisUid(mySqlAccess.GetGaapRefNdx(con, tempMember,thisTaxonomyUid, xsdExtract));
			        }
			        groups.add(group);
			        myGroupList.add(groups.size() -1);
			    }
			    else
			        myGroupList.add(iPrevGroup);
			    dimensions++;
			}
			int iMemberGroup = DefineMemberGroup(memberGroups, myGroupList);
			groupDetailMember.add(iMemberGroup);  // this list is membergroup index for EACH fact IN ORDER!
		}

		//groups.add(group);
	}
	
	private int DefineMemberGroup(ArrayList<MemberGroup> memberGroups, ArrayList<Integer> myGroupList) {
		int iRtn = 0;
		//ArrayList<Integer> MT = new ArrayList<Integer>();
		
		if(myGroupList.size() == 0)   // it's an MT list
			myGroupList.add(-1);
		iRtn = CheckForThisGroupNdx(myGroupList, memberGroups);
		if(iRtn == -1)  { // member group not found add it
			iRtn = AddMemberGroup(memberGroups, myGroupList);
		}
		return(iRtn);
	}
	
	private int AddMemberGroup(ArrayList<MemberGroup> memberGroups, ArrayList<Integer> newMember) {
		int         iRtn = 0;
		MemberGroup mg = new MemberGroup();
		
		for(Integer group: newMember) {
			mg.AddMember(group);
		}
		memberGroups.add(mg);
		iRtn = memberGroups.size() -1;
		return(iRtn);
	}
	
	private int CheckForThisGroupNdx(ArrayList<Integer> checkNdx, ArrayList<MemberGroup> memberGroups) {
		int     iRtn = -1;
		boolean bFound = false;
		int     iNdx = 0;
		
		while((bFound == false) && ( iNdx < memberGroups.size())) {
			if(checkNdx.size() == memberGroups.get(iNdx).GetMembers().size()) { // only check if both have same size
				bFound = CheckIfMatchingGroups(checkNdx, memberGroups.get(iNdx));
				if(bFound == true)
				    iRtn = iNdx;  // set return
			}
			iNdx++;
		}
		return(iRtn);
	}
	
	private boolean CheckIfMatchingGroups(ArrayList<Integer> checkNdx, MemberGroup mg) {
		boolean bMatch = true;
		int     iCount = 0;
		int     iCheck = 0;
		boolean bFound = true;
		
		while((bMatch == true) && (iCount < checkNdx.size())) {
			iCheck = 0;
			bFound = false;
			while((bFound == false) && (iCheck < mg.GetMembers().size())) {
				if(checkNdx.get(iCount) == mg.GetMembers().get(iCheck))
					bFound = true;
				iCheck++;
			}
			bMatch = bFound;
			iCount++;
		}
		return(bMatch);
		
	}
		
	private int FindMyRowDefinition(ArrayList<RowDef> rowDefinitions, int memberGroupNdx) {
		int     iRtn = -1;
		boolean bFound = false;
		int     iNdx = 0;
		RowDef  newRowDef = null;
		
        while((iNdx < rowDefinitions.size()) && (bFound == false)) {
        	if(rowDefinitions.get(iNdx).GetMemberGroup() == memberGroupNdx) {
        		iRtn = iNdx;
        		bFound = true;
        	}
        	iNdx++;
        }
        if(iRtn == -1) { // Not Found - add new one
        	newRowDef = new RowDef();
        	newRowDef.SetMemberGroup(memberGroupNdx);
        	rowDefinitions.add(newRowDef);
        	iRtn = rowDefinitions.size() -1;
        }
		return(iRtn);
	}
	
	private int DoesGroupExist(ArrayList<Group> groups, Group posNew) {
		boolean bFound = false;
		int     i = 0;
		int     iRtn = -1;
		
		while((bFound == false) && (i < groups.size())) {
			if((posNew.GetAxis().equals(groups.get(i).GetAxis())) &&
			   (posNew.GetMember().equals(groups.get(i).GetMember()))) {
				bFound = true;
				iRtn = i;
			}
			i++;
		}
		return(iRtn);
	}
	
	private int FindTableColumnRefId(String contextRef, ArrayList<TableColumn> tableColumns) {
		int iRefRtn = -1;
		int iColumnNdx = 0;
		int iContextRefNdx = 0;
		
		while((iRefRtn == -1) && (iColumnNdx < tableColumns.size())) {
		    iContextRefNdx = 0;
			while((iRefRtn == -1) && (iContextRefNdx < tableColumns.get(iColumnNdx).GetContextRef().size())) {
			    if(tableColumns.get(iColumnNdx).GetContextRef().get(iContextRefNdx).GetContextRef().equals(contextRef))
			    	iRefRtn = tableColumns.get(iColumnNdx).GetRefUid();
				iContextRefNdx++;
			}
			iColumnNdx++;
		}
		return(iRefRtn);
	}
	
	private int FindEquityRefId(String contextRef, ArrayList<EquityColumn> equityColumns) {
		int   iRtn = 0;
		int   i = 0;
		
		while((iRtn == 0) && ( i < equityColumns.size())) {
			iRtn = equityColumns.get(i).IsThisMyContextRef(contextRef);
			//if(contextRef.equals(equityColumns.get(i).GetContextRef())) {
			//	iRtn = equityColumns.get(i).GetDateRef();
			//}
			i++;
		}
		return(iRtn);
	}
	//returns the column to which data associated
	private int SetChildNdx(String myContextRef, ArrayList<TableColumn> tableColumns) {
		int     iRtn = 0;
		boolean bFound = false;
		
		while(bFound == false) {
			// this checks all tags for a column
			for(ContextNdx text: tableColumns.get(iRtn).GetContextRef()) {
			    if(myContextRef.equals(text.GetContextRef())) {
				    bFound = true;
				    break;
			    }
			}
			// if not found - check next column
			if(bFound == false)
		        iRtn++;
		}
		return(iRtn);
	}
	
/**	
	private boolean ContextMatch(String myContext, ArrayList<TableColumn> tableColumns) {
		boolean bRtn = false;
		int     i = 0;
		
		while((bRtn == false) && (i < tableColumns.size())) {
			for(ContextNdx temp: tableColumns.get(i).GetContextRef()) {
			  if(myContext.equals(temp.GetContextRef()))
				bRtn = true;
			}
			i++;
		}
		return(bRtn);
	}
***/	
	private int FindGroupIndex(String abbrevStr, ArrayList<XmlGrouping> groupings) {
		boolean bFoundGroup = false;
		int     groupNdx = 0;
		int     iRtn = -1;
		
		while((bFoundGroup == false) && (groupNdx < groupings.size())) {
		    if(groupings.get(groupNdx).GetPrefix().equals(abbrevStr)) {
		    	bFoundGroup = true;
		    	iRtn = groupNdx;
		    }
		    else
		    	groupNdx++;
		}
		return(iRtn);
	}

	private void CheckForCalItem(String tag, CalCheckList calCheckList, ArrayList<CalParent> calParents) {
		boolean bFound = false;
		int     iParentNdx = 0;
		int     iChildNdx = 0;
		
		while(bFound == false) {
		    if(calParents.get(iParentNdx).GetMapped() == false) {
			    if(calParents.get(iParentNdx).GetUri().equals(tag)) {
				    calCheckList.bIsParent= true;
				    calCheckList.ParentNdx = iParentNdx;
				    bFound = true;
				    calCheckList.iLastParent = iParentNdx;
				    // once you find parent all children must be mapped
				    for(CalChild child: calParents.get(iParentNdx).GetChildren())
				    	child.SetMapped(true);
			    }		    	
		    }
		    if(bFound == false) { // check children
		    	boolean bCheckingChildren = true;
		    	iChildNdx = 0;
		    	while(bCheckingChildren == true) {
		    		if(calParents.get(iParentNdx).GetThisChild(iChildNdx).GetMapped() == true) {
		    		    iChildNdx++;
		    	        if(iChildNdx == calParents.get(iParentNdx).GetChildren().size())
		   		    	    bCheckingChildren = false;
		   		    }
		   		    else {
		   			    if(calParents.get(iParentNdx).GetThisChild(iChildNdx).GetUri().equals(tag)) {
	    				    bCheckingChildren = false;
		    			    calCheckList.bIsChild = true;
					        calCheckList.ParentNdx = iParentNdx;
					        calCheckList.ChildNdx = iChildNdx;
		   				    bFound = true;
		   			    }
		   			    else {
		   				    iChildNdx++;
	    				    if(iChildNdx == calParents.get(iParentNdx).GetChildren().size())
	    					    bCheckingChildren = false;
		    		    }
		    		}
		    	}
		    }
		    iParentNdx++;
		    if(iParentNdx == calParents.size())
		    	bFound = true;
		}
	}

	private void CheckForDimensionsToWrite(Connection con, int coreCellUid, RowDef rowDef, ArrayList<MemberGroup> memberGroups,
			                               ArrayList<Group> groups, int iType) {
		
		MySqlAccess     mySqlAccess = new MySqlAccess();
		int             i = 0;
		int             iGroup = 0;
		
		while(i < memberGroups.get(rowDef.GetMemberGroup()).GetMembers().size()) {
			iGroup = memberGroups.get(rowDef.GetMemberGroup()).GetMembers().get(i);
		    if(iGroup != -1)
		        mySqlAccess.WriteCoreDimensions(con, coreCellUid, groups.get(iGroup).GetAxisUid(), groups.get(iGroup).GetMemberUid(), iType);
		    i++;
		}
	}
	
	
	private int FindTaxonomyUid(Connection con, ArrayList<NameSpaceLink> nameSpaceLink, String origName) {
		int         iRtn = 0;
		int         i;
		String      workStr = "";
		String      taxIndex = "";
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		i = origName.indexOf(":");
		workStr = origName.substring(0, i);
		i = 0;
		while((i < nameSpaceLink.size()) && ( taxIndex.length() == 0) ) {
			if(nameSpaceLink.get(i).GetAbbrev().contains(workStr)) {
				taxIndex = nameSpaceLink.get(i).GetNameSpace();
			}
			else
				i++;
		}
		if(taxIndex.length() > 0) {
			iRtn = mySqlAccess.FindTaxonomyUid(con, taxIndex);
		}
		return(iRtn);
	}
	
	
	private Date FindDate(Connection con, String key, ArrayList<DimensionGroup> contexts, boolean bStart) {
		Date       dRtn = null;
		int        i = 0;
		boolean    bFound = false;
		String     dateStr = "";
		UTILITIES utilities = new UTILITIES();
		
		while((i < contexts.size() && (bFound == false))) {
			if(key.equals(contexts.get(i).GetContextId()))
				bFound = true;
			else
				i++;
		}
		if(bFound == true) {
			if(contexts.get(i).GetPeriod().GetInstant().length() > 0)
				dateStr = contexts.get(i).GetPeriod().GetInstant();
			else {
				if(bStart == true)
					dateStr = contexts.get(i).GetPeriod().GetStartDate();
				else
					dateStr = contexts.get(i).GetPeriod().GetEndDate();
			}
		    try {
		    dRtn = utilities.DateConverter(con,  dateStr);
		    }
		    catch (Exception e) {
			
		    }
		}
		return(dRtn);
	}
	
	private int FindUnitLinkUid(String unitNdx, ArrayList<UnitLink> unitLinks) {
		int iRtn = 0;
		int i = 0;
		
		while((iRtn == 0) && (i < unitLinks.size())) {
			if(unitNdx.equals(unitLinks.get(i).GetId()))
				iRtn = unitLinks.get(i).GetCoreUnitsUid();
			i++;
		}
		return(iRtn);
	}
	
	private ColumnFormatData FormatColumnData(Connection con, XmlDetail detail) {
		String             rtnStr = "";
		int                decimals = 0;
		float              longfloat = 0;
		int                NdxOfDecimal = 0;
		int                DecimalPoints = 0;
    	ColumnFormatData   columnFormatData = new ColumnFormatData();
		int                scaleMultiplier = 1;   // DEFAULT EXACT
		boolean            bDone = false;
		
		rtnStr = detail.GetData();
		if(rtnStr.length() > 0) {
		    if(detail.GetDecimals().length() > 0) { // convert to binary
		    	if(detail.GetDecimals().equals("INF") == false) {
		    	    NdxOfDecimal = rtnStr.indexOf(".");
		    	    if(NdxOfDecimal == -1) {
		    		    rtnStr += ".";
		    		    NdxOfDecimal = rtnStr.indexOf(".");
		    	    }
		    	    DecimalPoints = rtnStr.length() - NdxOfDecimal -1;
			        decimals = Integer.parseInt(detail.GetDecimals());
		    	    if(decimals < 0) {
		    		    rtnStr = rtnStr.replace(".", "");
		    		    scaleMultiplier = 0 - decimals;
		    		    NdxOfDecimal += decimals;
		    		    if(NdxOfDecimal > 0)
		    		        rtnStr = rtnStr.substring(0, NdxOfDecimal) + "." + rtnStr.substring(NdxOfDecimal);
		    	    }
		    	    else {
		    		    if(DecimalPoints == decimals) {
		    			    bDone = true;
		    		    }
		    		    else {
		    		        rtnStr = rtnStr.replace(".", "");
		    		        int remLen = rtnStr.length();
		    		        NdxOfDecimal += decimals;
		    		        int numZeros = 0;
		    		        while((NdxOfDecimal + numZeros)  < remLen) {
		    			        numZeros++;
		    			        rtnStr += "0";
		    		        }
		    		        String temp;
		    		        if(rtnStr.length() > NdxOfDecimal) {
		    		            temp = rtnStr.substring(0, NdxOfDecimal) + ".";
		    		            if(NdxOfDecimal < rtnStr.length()) {
		    			            temp += rtnStr.substring(NdxOfDecimal);
		    		            }
		    		            rtnStr = temp;
		    		        }
		    		        
		    		    }
		    	    }
		    	}
		    	if(bDone == false) {
		    	    longfloat = Float.parseFloat(rtnStr);
			        if(DecimalPoints > 0) {
			    	    BigDecimal bd = new BigDecimal(longfloat).setScale(DecimalPoints, BigDecimal.ROUND_HALF_EVEN);
			    	    rtnStr = bd.toString();
			    	    //longfloat = bd.floatValue();
			        }
			        else
			            rtnStr =  String.format("%,f", longfloat); // NumberFormat.getIntegerInstance().format(tempInt);
			        if(rtnStr.contains(".")) {
				        String Temp = rtnStr.substring(rtnStr.length()-1);
				        while(Temp.equals("0")) {
					        rtnStr = rtnStr.substring(0, rtnStr.length() -1);
					        Temp = rtnStr.substring(rtnStr.length()-1);
				        }
			        }  
				//rtnStr = rtnStr.replaceAll(".?0*$", "");
		    	}
		    }
		    // remove trailing decimal
		    int iDecimalNdx = rtnStr.indexOf(".");
		    if(iDecimalNdx != -1) {
		        if((rtnStr.length() -1) == iDecimalNdx)
		    	    rtnStr = rtnStr.substring(0, iDecimalNdx);
		    }
		    if(rtnStr.substring(0,1).equals("-")) {
			    rtnStr = rtnStr.replace("-", "(");
			    rtnStr = rtnStr + ")";
	        }
		}
		else
			rtnStr = "-";
		columnFormatData.ColumnData = rtnStr;
		columnFormatData.Scale = GetScaleIndex(con, scaleMultiplier);
		return(columnFormatData);
	}
	
	private int GetScaleIndex(Connection con, int scaleMultiplier) {
		int         iRtn = 1;
	    MySqlAccess mySqlAccess = new MySqlAccess();
	    
	    if(scaleMultiplier > 1) {
	    	while(scaleMultiplier > 0) {
	    		iRtn = iRtn *10;
	    		scaleMultiplier--;
	    	}
	    }
	    
	    iRtn = mySqlAccess.GetScaleNdx(con, iRtn);
		return(iRtn);
	}

	private boolean ValidateTemplates(Connection con, MySqlAccess mySqlAccess, ArrayList<RootNode> nodeChain,
			                          XsdExtract xsdExtract) { //PreExtract preExtract) {
		boolean                     bRtn = true;
		ArrayList<XbrlTemplateXref> xref = new ArrayList<XbrlTemplateXref>();
		int                         i = 0;
		ErrorCls                    errorCls = new ErrorCls();
		int                         iUnknownTemplateId = 100;
		
		errorCls.setFunctionStr("ValidateTemplates");
		errorCls.setCompanyUid(0);
		errorCls.setItemVersion(0);
		errorCls.setSubUid(0);
		xref = mySqlAccess.GetXbrlTemplateXrefs(con);
		for(RootNode curNode: nodeChain)
		{
			String lowered = curNode.GetRootId().toLowerCase();  //curGroup.GetRole().toLowerCase();
			i = 0;
			if(IsNote(curNode.GetRootId(), xsdExtract))
				curNode.SetTemplateId(4);
			else {
			    while((i < xref.size()) && (curNode.GetTemplateId() == CONSTANTS.INVALID_TEMPLATE_ID)) {
				    if(lowered.contains(xref.get(i).GetXbrlTag()))
					    curNode.SetTemplateId(xref.get(i).GetTemplateId());
			        i++;
			    }
			    if(curNode.GetTemplateId() == CONSTANTS.INVALID_TEMPLATE_ID) {
				    curNode.SetTemplateId(iUnknownTemplateId);
				    iUnknownTemplateId++;
				    errorCls.setErrorText("Not found Template: " + lowered);
				    mySqlAccess.WriteAppError(con, errorCls);
				}
			}
		}
		// now check if any were not defined in XbrlTemplatexref table
		for(RootNode curNode: nodeChain) {
			if(curNode.GetTemplateId() == CONSTANTS.INVALID_TEMPLATE_ID) {
				errorCls.setErrorText("Cannot identify: " + curNode.GetRootId() + " as template");
				mySqlAccess.WriteAppError(con, errorCls);
			}
		}
		return(bRtn);
	}
	
	private boolean IsNote(String posNote, XsdExtract xsdExtract) {
		boolean bRtn = false;
		int     i = 0;
		int     noteRoles = xsdExtract.GetNoteRoles().size();
		
		while((bRtn == false) && ( i < noteRoles)) {
			if(posNote.equals(xsdExtract.GetNoteRoles().get(i)))
				bRtn = true;
			else
				i++;
		}
		return(bRtn);
	}
	private String FindData(ArrayList<DeiObjects> deiObjects, String toFind) {
		String           RtnStr = "";
	    int              i = 0;
		double           dConfidence = 0;
		ConfidenceLevel  cl = new ConfidenceLevel();
	    
	    while((RtnStr.length() == 0) && (i < deiObjects.size())) {
            dConfidence = cl.compareToArrayList1(toFind, deiObjects.get(i).GetAl());    
            if(dConfidence == 1.0) {
            	RtnStr = deiObjects.get(i).GetXmlData().get(0);
            }
            i++;
	    }
		return(RtnStr);
	}
	
	private void CheckForFootNotes(Connection con, int coreCellUid, String contextRef, ArrayList<FootnoteLoc> footNotes){
		
		int         i = 0;
		boolean     bFound = false;
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		while((bFound == false) && (i < footNotes.size())) {
			if(contextRef.equals(footNotes.get(i).GetHRef())) {
				bFound  = true;
			}
			else
				i++;
		}
		if(bFound) 
			mySqlAccess.WriteFootnoteRef(con, coreCellUid, footNotes.get(i).GetUid());
	}
	
	private void WriteCoreSummations(Connection con, ArrayList<CalParent> calParents, Debug debug) {
		
		int i;
		MySqlAccess mySqlAccess  = new MySqlAccess();
		
		//PrintWriter          out = null;
		//try {
	    //    FileWriter outFile = new FileWriter("c:\\summation.out");
        //    out = new PrintWriter(outFile);
		ResolveParentsAsChildren(calParents);
		for(CalParent cp: calParents) {
		    i = 0;
		    while(i < cp.GetCoreCellUid().size()) {
		    	for(CalChild cc: cp.GetChildren()) {
		    		if((cp.GetThisCellUid(i) != 0) && (cc.GetThisCoreCellUid(i) != 0)) {
		    			//System.out.println("Child: " + cc.GetThisCoreCellUid(i) + "  Parent: " +  cp.GetThisCellUid(i));
		    		    mySqlAccess.WriteCoreSubmissionChild(con, cp.GetThisCellUid(i), cc.GetThisCoreCellUid(i), debug);
		    		}
		    	}
		    	i++;
		    }
		}
		//out.close();
		//}
		//catch (Exception e) {
			
		//}
		
	}

	private void ResolveParentsAsChildren(ArrayList<CalParent> calParents) {
	
		int i = 0;
		int iTargetParent = 0;
		int iTargetChild = 0;
		
		while(i < (calParents.size() -1)) {
			iTargetParent = i + 1;
			while(iTargetParent < calParents.size()) {
				iTargetChild = 0;
				while(iTargetChild < calParents.get(iTargetParent).GetChildren().size()) {
					if(calParents.get(iTargetParent).GetThisChild(iTargetChild).GetUri().equals(calParents.get(i).GetUri())) {
						for(int j = 0 ; j < calParents.get(i).GetCoreCellUid().size(); j++) 
					    calParents.get(iTargetParent).GetThisChild(iTargetChild).SetCoreCellUid(calParents.get(i).GetThisCellUid(j), j);	
					}
					iTargetChild++;
				}
				iTargetParent++;
			}
			i++;
		}
		
	}
	
}
