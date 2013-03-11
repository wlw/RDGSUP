package com.bricksimple.rdg.FieldId;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

/*******************************************************************************/
/* the destTable parameter will determine the destination table for the        */
/*  extracted data:                                                            */
/*         0 - regular fieldslocated or abstracts                              */
/*         1 -    Company table                                                */
/*         2 -     Company EIN                                                 */

public class WriteFieldData {
	
	public boolean CheckForNoData(Connection con, Node n, ArrayList<String> dateForms) {  // static 
		boolean           bRtn = true;
        Node              dataNode = n;
        NodeList          fstNm;
        String            NodeData;
        MySqlAccess       mysql = new MySqlAccess();
        SupplementCls     supplementCls = new SupplementCls();
        SupplementRtn     rtnCls = null;
        
        dataNode = dataNode.getNextSibling();
        MatchStr[] DateSups =  mysql.GetListOfDateSup(con);
        while((dataNode != null) && (bRtn == true)) {
            fstNm = dataNode.getChildNodes();
        	if(fstNm.getLength() > 0) {
                NodeData = (fstNm.item(0)).getNodeValue();
                NodeData = NodeData.trim();
                if((NodeData.equals("")) || (NodeData.equals("$")))
            	    	NodeData = "";
                else {
                	rtnCls = supplementCls.IsSupplementStr(NodeData, DateSups, dateForms);
                	if(rtnCls.getiRtn() == -1)
                	    bRtn = false;
                }
         	}
           dataNode = dataNode.getNextSibling();
        }
		return(bRtn);
	}

	public boolean WriteEin(Connection con, SubmissionInfo subInfo, FieldMatchStr fieldMatch,  // static
			                       Node n, int iTrNdx, int iTdNdx, String NodeData) {
        Node        TempNode;
        Node        dataNode = n;
        int 		iTdCounter = 1;
        NodeList    fstNm;
        String      NodeData2;
        boolean     bRtn = true;
		String      dataLocStr = fieldMatch.getPathToData();
        CONSTANTS   constants = new CONSTANTS();
	    MySqlAccess mySqlAccess = new MySqlAccess();
	    
	    
        TempNode = dataNode.getParentNode();  
        TempNode = TempNode.getPreviousSibling();
        if(TempNode != null) { // not previous check NEXT !!
        	dataNode = TempNode.getFirstChild();
        }
        else {   // not previous must be next
        	dataNode = dataNode.getNextSibling();
        }
        // second chance - new Edgarizer
        if((dataNode == null) && (iTrNdx == 1)) {
           	TempNode = n.getParentNode();  // points to tr
        	TempNode = TempNode.getParentNode();   // points to table
        	NodeList children = TempNode.getChildNodes();  // list of tr
        	// now get second tr
        	dataNode = children.item(1);
        	dataNode = dataNode.getFirstChild();

        }
        if(dataNode != null ){
            while(iTdCounter < iTdNdx) {
                dataNode = dataNode.getNextSibling();
                iTdCounter++;
            }
            fstNm = dataNode.getChildNodes();
            while(fstNm.getLength() == 0) {
                dataNode = dataNode.getNextSibling();
                fstNm = dataNode.getChildNodes();
            }
            NodeData2 = (fstNm.item(0)).getNodeValue();
            NodeData2 = NodeData2.trim();
            int NodeDataLen = NodeData2.length();
            while(NodeDataLen == 0) {
                dataNode = dataNode.getNextSibling();
                if(dataNode == null)
                    return(false);
                fstNm = dataNode.getChildNodes();
                NodeData2 += fstNm.item(0).getNodeValue();
                NodeData2 = NodeData2.trim();
                NodeDataLen = NodeData2.length();
            }
        }
        else {  // check if on original line
        	NodeData2 = "";
        	boolean bContinue = true;
        	String TestStr = "";
        	int    iNdx = 0;
        	NodeData = NodeData.trim();
        	while (bContinue == true) {
        		if(iNdx < NodeData.length()) {
        			TestStr = NodeData.substring(iNdx, iNdx+1);
        			if(constants.isNumeric(TestStr) != -1)
        				NodeData2 = NodeData2 + TestStr;
        			else {
        				if(TestStr.equals("-"))
        					NodeData2 = NodeData2 + TestStr;
        				else
        					bContinue = false;
        			}
        			iNdx++;
        		}
        		else
        			bContinue = false;
        	}
        }
        if(NodeData2.length() > 0)
            mySqlAccess.WriteEin(con, subInfo.getCompanyId(), NodeData2);
        return(true);	
	}
	
	public boolean WriteConcatedEin(Connection con, SubmissionInfo subInfo, String nodeData, int ndxOfStr) {
		String      dbData = "";
	    MySqlAccess mySqlAccess = new MySqlAccess();
		
		dbData = nodeData.substring(0, ndxOfStr);
        mySqlAccess.WriteEin(con, subInfo.getCompanyId(), dbData);
		return(true);
	}
	
	public boolean WriteStateIncorporated(Connection con, SubmissionInfo subInfo, String nodeData, int ndxOfStr) {
		
		String      dbData = "";
	    MySqlAccess mySqlAccess = new MySqlAccess();
		
		dbData = nodeData.substring(0, ndxOfStr);
        mySqlAccess.WriteIncorporationState(con, subInfo.getCompanyId(), dbData);
		return(true);
	}
	
	public boolean WriteFilerStatus(Connection con, SubmissionInfo subInfo, String nodeData) {
	    MySqlAccess mySqlAccess = new MySqlAccess();
	    String      dbStr = "";
	    boolean     bWrite = false;
	    boolean     bRtn = false;

        if(nodeData.contains("[x]")) {
        	bWrite = true;
        	if(nodeData.indexOf("Large accelerated filer") != -1)
        		dbStr = "Large Accelerated Filer";
        	if(nodeData.indexOf("Accelerated Filer") != -1)
        		dbStr = "Accelerated Filer";
        	if(nodeData.indexOf("Non-accelerated filer") != -1)
        		dbStr = "Non-Accelerated Filer";
        	if(nodeData.indexOf("Smaller reporting company") != -1)
        		dbStr = "Smaller Reporting Company";
       	
            if(bWrite) { // someone matched
	            mySqlAccess.InsertFilerStatus(con, subInfo.getCompanyId(), dbStr);
	            bRtn = true;
            }
        }
        return(bRtn);
    }
	
	public boolean DbWriteFieldData(Connection con, SubmissionInfo subInfo, int TemplateId,  // static
                                           int TemplateUid, FieldMatchStr fieldMatch, Node n,
                                           int iTrNdx, int iTdNdx,  ArrayList<Integer> DateRefUid,
                                           NodeIterator iterator, ColumnPosInfo columnPosInfo,
                                           ArrayList<Integer> colSpans, String RowDataStr,
                                           boolean bDateField) { //,
                                           //ArrayList<FieldModifier> fm) {

        boolean     bRtn = true;
        String      TraverseCmds = fieldMatch.getPathToData();
        Node        dataNode = n;
        int         commaLoc;
        String      curCommand;
        String      NodeData = "";
        int 		iTdCounter = 1;
        NodeList    fstNm;
        boolean     bRecordThisNode = true;
        int         iDateUid;
        boolean     bFieldRequired = false;
        //int         FlUid = 0;  // record UID of FieldsLocated
        boolean     bUseCurrentData = false;
        int         iCurColumnIndex = iTdNdx;
        int         iMatchedRowDataUid = 0;
        int         iOpenParens = 0;
        ErrorCls    errorCls = new ErrorCls();
	    MySqlAccess mySqlAccess = new MySqlAccess();
	    int         iMonthNdx = 0;
	    CONSTANTS   constants = new CONSTANTS();
	    int         NodeDataLen = 0;
	    boolean     bCheck = true;
	    
		errorCls.setFunctionStr("DbWriteFieldData");
	    errorCls.setSubUid(subInfo.getUid());
	    errorCls.setCompanyUid(subInfo.getCompanyId());
	    errorCls.setItemVersion(subInfo.getCompanyId());    
        //find the data node
        iMatchedRowDataUid = mySqlAccess.WriteMatchedDataStr(con, subInfo, fieldMatch.getUid(), TemplateUid, 
        		                                             RowDataStr, iTrNdx, iTdNdx, TemplateId);
        if((TemplateId > 0) && (iTdNdx > 2))  // this test should check against previous column 
        	TraverseCmds = "DZ";
        while(TraverseCmds.length() > 0)  {
            commaLoc = TraverseCmds.indexOf(',');
            if(commaLoc > 0) {
                curCommand = TraverseCmds.substring(0,(commaLoc-1));
                TraverseCmds = TraverseCmds.substring(commaLoc+1, TraverseCmds.length() -1);
            }
            else {
                curCommand = TraverseCmds;
                TraverseCmds = "";
            }
            if(curCommand.equals("SH"))  {
            	fstNm = dataNode.getChildNodes();
            	if(fstNm == null) 
            		NodeData = "";
            	else
            	    NodeData = fstNm.item(0).getNodeValue();
            	return( ExtractShareInfo(con, NodeData, iterator, subInfo));
            }            
            if(curCommand.equals("NC")) {
            	bFieldRequired = true;
            	Node Temp = dataNode.getNextSibling();
            	if(Temp != null)
            		dataNode = dataNode.getNextSibling();
            	else {
            		Temp = dataNode.getPreviousSibling();
            		if(Temp != null)
            			dataNode = dataNode.getPreviousSibling();
            		else
            			return(bRtn);
            	}
            }
            else {
                if((curCommand.equals("PL") || (curCommand.equals("PC")))) {
                    //if(curCommand.equals("PC"))
                    bFieldRequired = true;
                    Node TempNode;
                    if(iTrNdx > 1) {
                        TempNode = dataNode.getParentNode();  
                        TempNode = TempNode.getPreviousSibling();
                        if(TempNode != null) { // not previous check NEXT !!
                    	    dataNode = TempNode.getFirstChild();
                        }
                        else {   // not previous must be next
                    	    dataNode = dataNode.getNextSibling();
                        }
                    }
                    else { // move to next line
                    	
                    	//int temp = iTdNdx;
                    	TempNode = dataNode.getParentNode();  // points to tr
                    	TempNode = TempNode.getParentNode();   // points to table
                    	NodeList children = TempNode.getChildNodes();  // list of tr
                    	// now get second tr
                    	dataNode = children.item(1);
                    	dataNode = dataNode.getFirstChild();
                    	//while(iTdNdx > 0) {
                    	//    TempNode = TempNode.;
                    	//    iTdNdx--;
                    	//}
                    	//dataNode = TempNode.getFirstChild();
                    	
                    	//dataNode = dataNode.getNextSibling();
                    	//dataNode = TempNode.getLastChild();
                        //dataNode = TempNode.getNextSibling();
                        
                    }
                    //dataNode = dataNode.getPreviousSibling();
                    //dataNode = dataNode.getFirstChild();
                    while(iTdCounter < iTdNdx) {
                        dataNode = dataNode.getNextSibling();
                        iTdCounter++;
                    }
                }
                else {
                    if((curCommand.equals("DT")) || (curCommand.equals("DZ"))) {
                        bRecordThisNode = false;
                        if(curCommand.equals("DT"))
                            dataNode = dataNode.getNextSibling();
                        //fstNm = dataNode.getChildNodes();
                        iDateUid = 0;
                        while(dataNode != null){
                            iOpenParens = 1;
                       	    if(curCommand.equals("DZ")) {
                        		curCommand = "DT";
                        		NodeData = RowDataStr;
                        		bCheck = true;
                        		columnPosInfo.iCurColumn = columnPosInfo.iCurColumn -1;
                        	}
                        	else {
                        	    iTdNdx++;
                                fstNm = dataNode.getChildNodes();
                        	    if(fstNm.getLength() > 0) {
                                //System.out.println("DATE DATA: " + (fstNm.item(0)).getNodeValue());
                                    NodeData = (fstNm.item(0)).getNodeValue();
                                    bCheck = true;
                        	    }
                        	    else
                        	    	bCheck = false;
                        	}
                       	    if(bCheck) {
                                int iFoundOrphanPara = NodeData.indexOf("(");
                                String PostStr;
                                //iOpenParens = 1;  //MOVED TO ABOVE
                                if(iFoundOrphanPara != -1) {
                                    iFoundOrphanPara = NodeData.indexOf(")");  // check if here
                                     //WLW we should check here for possible missing end parathensis 
                                    if(iFoundOrphanPara == -1) { // we enter with found ( now we look for matching
                                        dataNode = dataNode.getNextSibling();  // get next row
                                        if(dataNode == null) {
                                        	//iFoundOrphanPara = 1;
                                		    errorCls.setErrorText("Invalid Number Format missing ')' on  value :"  + NodeData);
                                		    errorCls.setBExit(false);
                                		    mySqlAccess.WriteAppError(con, errorCls);
                                        }
                                        else {
                                            fstNm = dataNode.getChildNodes();
                                            if(fstNm.item(0) == null) {
                                            	//iFoundOrphanPara = 1;
                                    		    errorCls.setErrorText("Invalid Number Format missing ')' on  value :"  + NodeData);
                                    		    errorCls.setBExit(false);
                                    		    mySqlAccess.WriteAppError(con, errorCls);
                                            }
                                            else {
                                                PostStr = (fstNm.item(0)).getNodeValue();
                                                NodeData += PostStr;
                                                iFoundOrphanPara = NodeData.indexOf(")");
                                                iOpenParens += 1;
                                                if(iFoundOrphanPara == -1) {
                                        		    errorCls.setErrorText("Invalid Number Format missing ')' on  value :"  + NodeData);
                                        		    errorCls.setBExit(false);
                                        		    mySqlAccess.WriteAppError(con, errorCls);
                                                }
                                            }
                                        }
                                    }
                                    if(iFoundOrphanPara == -1)
                                    	NodeData += ")";
                                }
                                int dateRef = 0;
                                if(DateRefUid.size() > 0) {
                            	    if(columnPosInfo == null)
                            		    dateRef = DateRefUid.get(iDateUid);
                            	    else {
                            		    dateRef = DateRefUid.get(columnPosInfo.iCurColumn);
                            		    if(dateRef == 0)  // must have been a negative off the end
                            		    	dateRef = DateRefUid.get(columnPosInfo.iCurColumn -1);  // so get the previous
                            	    }
                                }
                                NodeData = NodeData.trim();
                                if((NodeData.equals("")) || (NodeData.equals("$")))
                            	    	NodeData = "";
                                else {
                                    mySqlAccess.writeFieldLocated(con, subInfo.getCompanyId(), subInfo.getUid(), 
	                                                                      subInfo.getVersion(), TemplateId, TemplateUid,
	                                                                      fieldMatch.getUid(), NodeData, dateRef,
	                                                                      fieldMatch.getAbstract(), fieldMatch.getDestination(),
	                                                                      iMatchedRowDataUid, iTrNdx, iTdNdx);
	                                                                  //DateRefUid.get(iDateUid), fieldMatch.getAbstract());
                                }
                                iDateUid++;  // not sure this should be here or below
                        	}
                   		    while(iOpenParens > 0) {
                		        iCurColumnIndex += 1;
                		        iOpenParens -= 1;
                		        if(iCurColumnIndex < colSpans.size())  // if equal we are at the end of the row
                		            columnPosInfo.iCurColumn += colSpans.get(iCurColumnIndex);
                		    }
                   		    if(dataNode != null)
                               dataNode = dataNode.getNextSibling();
                        }
                    }
                    //writes the beginning of line
                    if(curCommand.equals("RX")) {
                    	fstNm = dataNode.getChildNodes();
                    	NodeData = fstNm.item(0).getNodeValue();
                    	int iMatch = NodeData.indexOf(fieldMatch.getFieldStr());
                    	if(iMatch > 0) {
                    	    NodeData = NodeData.substring(0,iMatch);
                    	    bUseCurrentData = true;
                    	}
                    	else
                    	    bRecordThisNode = false;
                    }
                    if(curCommand.equals("RT")) {
                    	bRecordThisNode = false;
                    	fstNm = dataNode.getChildNodes();
                    	NodeData = fstNm.item(0).getNodeValue();
                    	int iMatch = NodeData.indexOf(fieldMatch.getFieldStr()) + fieldMatch.getFieldStr().length();
                        NodeData = NodeData.substring(iMatch).trim();
                        mySqlAccess.writeFieldLocated(con, subInfo.getCompanyId(), subInfo.getUid(), 
                                subInfo.getVersion(), TemplateId, TemplateUid, fieldMatch.getUid(), 
                                NodeData, 0,
                                fieldMatch.getAbstract(), fieldMatch.getDestination(),
                                iMatchedRowDataUid, iTrNdx, iTdNdx);
                                //DateRefUid.get(iDateUid), fieldMatch.getAbstract());
                       
                    }
                }
            }
        }
        if(bRecordThisNode == true) {
        	if(bUseCurrentData == false) {
                fstNm = dataNode.getChildNodes();
                while(fstNm.getLength() == 0) {
            	    dataNode = dataNode.getNextSibling();
            	    fstNm = dataNode.getChildNodes();
                }
                //System.out.println("DATA: " + (fstNm.item(0)).getNodeValue());
                NodeData = (fstNm.item(0)).getNodeValue();
                NodeData = NodeData.trim();
                if(bFieldRequired) {
                	if(bDateField == true) {
                		iMonthNdx = constants.ReturnMonthIndex(NodeData);
                		if(iMonthNdx != -1)
                	        NodeDataLen = NodeData.length();
                		else
                			NodeDataLen = 0;
                	}
                	else
                		NodeDataLen = NodeData.length();
                    while(NodeDataLen == 0) {
                    	//check if we have a next sibling
                        dataNode = dataNode.getNextSibling();
                        if(dataNode == null) {
                        	// if not then date on next row so we skip to it
                        	dataNode = iterator.nextNode();
                        	NodeData = "";
                        	if(dataNode == null)
                        		return(bRtn);
                        }
                        fstNm = dataNode.getChildNodes();
                        NodeData += fstNm.item(0).getNodeValue();
                        if(NodeData != null) {
                            NodeData = NodeData.trim();
                            if(bDateField) {
                		        iMonthNdx = constants.ReturnMonthIndex(NodeData);
                		        if(iMonthNdx != -1)
                	                NodeDataLen = NodeData.length();
                            }
                            else
                            	NodeDataLen = NodeData.length();
                        }
                    }
                }
            }
        	// NodeData = RemoveTrailingDashes(NodeData);
            mySqlAccess.writeFieldLocated(con, subInfo.getCompanyId(), subInfo.getUid(), subInfo.getVersion(),
                                          TemplateId, TemplateUid, fieldMatch.getUid(), NodeData, 0, fieldMatch.getAbstract(), 
                                          fieldMatch.getDestination(), iMatchedRowDataUid, iTrNdx, iTdNdx);
        }
        return(bRtn);
    }

	public  void DbWriteNoteFieldData(Connection con, SubmissionInfo subInfo, int iTemplateId, int iRowLabelUid, Node n,   // static
                                            int iTdNdx, ArrayList<Integer> DateRefUid, ColumnPosInfo columnPosInfo, 
                                            ArrayList<Integer> colSpans, String NodeData, int iTrNdx) {
        Node        dataNode = n;
        int         iDateUid;
        int         iOpenParens;
        NodeList    fstNm;
        int         iCurColumnIndex = iTdNdx;
	    MySqlAccess mySqlAccess = new MySqlAccess();
        ErrorCls    errorCls = new ErrorCls();
        int         iExtraTds = 0;
        
        dataNode = dataNode.getNextSibling();
        //fstNm = dataNode.getChildNodes();
        iDateUid = 0;
        while(dataNode != null){
        //while(fstNm.item(0) != null) {XXXXX
            fstNm = dataNode.getChildNodes();
            iTdNdx++;
            iOpenParens = 1;
        	if(fstNm.getLength() > 0) {
                //System.out.println("DATE DATA: " + (fstNm.item(0)).getNodeValue());
                NodeData = (fstNm.item(0)).getNodeValue();
                int iFoundOrphanPara = NodeData.indexOf("(");
                String PostStr;
                //iOpenParens = 1;  //MOVED TO ABOVE
                iExtraTds = 0;
                if(iFoundOrphanPara != -1) {
                    iFoundOrphanPara = NodeData.indexOf(")");  // check if here
                     //WLW we should check here for possible missing end parathensis 
                    while(iFoundOrphanPara == -1) { // we enter with found ( now we look for matching
                    	iExtraTds++;
                        dataNode = dataNode.getNextSibling();  // get next row
                        if(dataNode != null) {
                            fstNm = dataNode.getChildNodes();
                            if(fstNm.item(0) != null) {
                                PostStr = (fstNm.item(0)).getNodeValue();
                                NodeData += PostStr;
                            }
                            iFoundOrphanPara = NodeData.indexOf(")");
                            iOpenParens += 1;
                        }
                        else { // missing end parenthesis
                    		errorCls.setFunctionStr("DbWriteNoteFieldData");
                    	    errorCls.setSubUid(subInfo.getUid());
                    	    errorCls.setCompanyUid(subInfo.getCompanyId());
                    	    errorCls.setItemVersion(subInfo.getCompanyId());    
                		    errorCls.setErrorText("Invalid Number Format missing ')' on  value :"  + NodeData);
                		    errorCls.setBExit(false);
                		    iFoundOrphanPara = 0;
                		    mySqlAccess.WriteAppError(con, errorCls);
                        }
                    }
                }
                int dateRef = 0;
                if(DateRefUid.size() > 0) {
            	    if(columnPosInfo == null)
            		    dateRef = DateRefUid.get(iDateUid);
            	    else {
            		    dateRef = DateRefUid.get(columnPosInfo.iCurColumn);
            		    if(dateRef == 0)  // must have been a negative off the end
            		    	dateRef = DateRefUid.get(columnPosInfo.iCurColumn -1);  // so get the previous
            	    }
                }
                NodeData = NodeData.trim();
                if((NodeData.equals("")) || (NodeData.equals("$")) || (NodeData.equals("%")))
            	    	NodeData = "";
                else {
                	mySqlAccess.InsertNoteTableColumn(con, subInfo, iTemplateId, iRowLabelUid, dateRef, NodeData, iTrNdx, iTdNdx);
                }
                iDateUid++;  // not sure this should be here or below
                iTdNdx += iExtraTds;
        	}
   		    while(iOpenParens > 0) {
		        iCurColumnIndex += 1;
		        iOpenParens -= 1;
		        if(iCurColumnIndex < colSpans.size())  // if equal we are at the end of the row
		            columnPosInfo.iCurColumn += colSpans.get(iCurColumnIndex);
		    }
   		    if(dataNode != null)
               dataNode = dataNode.getNextSibling();
        }
    }	

	private boolean ExtractShareInfo(Connection con, String OrigData, NodeIterator iterator, SubmissionInfo subInfo) {  // static
		boolean       bRtn = true;
		boolean       bContinue = true;
		String        ElementStr;
		String        NodeData = "";
		Node          node;
		NodeList      fstNm;
		int           state = 0;
		CombinedStock cs = new CombinedStock();
		String        dateStr = "";
		MySqlAccess   mysql = new MySqlAccess();
		boolean       bCheckNodeData = false;
		int           sharesstate = 1;
		boolean       bFoundParValueStr = false;
		boolean       bFoundParValue = false;
		boolean       bFoundAsharesValue = false;
		CONSTANTS     constants = new CONSTANTS();
		
		try {
		    while(bContinue) {
		    	if(OrigData.length() > 0) {
		    		OrigData = OrigData.replace("Shares Outstanding", "");
		    		NodeData = OrigData.trim();
		    		OrigData = "";
		    		bCheckNodeData = true;
		    	}
		    	else {
			        node = iterator.nextNode();
			        if(node == null)
			    	    break;
		     	    ElementStr = ((Element) node).getTagName();
		     	    bCheckNodeData = false;
        	        if(ElementStr.equals("td")) {
        		        fstNm = node.getChildNodes();
        	            int iLen = fstNm.getLength();
        	            if(iLen > 0) {
        	                NodeData = (fstNm.item(0)).getNodeValue();
        	                if(NodeData != null) {
        	    	            NodeData = NodeData.trim();
        	    	            if(NodeData.length() > 0) 
        	    	        	    bCheckNodeData = true;
        	                }
        	            }
        	        }
		    	}
		    	if(bCheckNodeData == true) {
		    		if(constants.MonthInString(NodeData) == true) {
        	            if(constants.IsThisADate(NodeData)) {
        	    	        dateStr = NodeData;
        	    	        state++;
        	            }
        	    	    else {
        	    	    	//String TempDate = "";
        	    	    	int iIdx = constants.ReturnMonthIndex(NodeData);
        	    	    	dateStr = NodeData.substring(iIdx);
        	    	    	//iIdx = TempDate.indexOf(" ");
        	    	    	
        	    	    }
        	    	}
        	    	else {
        	    	    if(NodeData.indexOf("par value") != -1) {
        	    	        bFoundParValueStr = true;
        	    	        NodeData = NodeData.replace("par value", "");
       	    	        }
        	    	    if(bFoundParValue == false) {
        	    	 	    if(NodeData.indexOf("$") != -1) {
        	    	            NodeData = NodeData.replace("$", "");
        	    	            String theValue = constants.ExtractDecimalNumber(NodeData);
        	    	            if (bFoundAsharesValue == false) {
        	    	    	        cs.CommonStockAValue = Float.valueOf(theValue);
        	    	    	        bFoundAsharesValue = true;
        	    	            }
        	    	            else 
        	    	    	        cs.CommonStockBValue = Float.valueOf(theValue);
        	    	            state++;
        	    	        }
        	    	        else {
        	    	            NodeData = NodeData.replace(",",  "");
        	    	    	    int shares = constants.isNumeric(NodeData);
        	    	    	    if(shares != -1) {
        	    	    	        if(sharesstate == 1) {
        	    	    	            cs.CommonStockAShares = shares;
        	    	    	            sharesstate++;
        	    	    	        }
        	    	    	        else {
        	    	    	            cs.CommonStockBShares = shares;
        	    	    	            state++;
        	    	    	            bContinue = false;
        	    	    	        }
        	    	    	        state++;
        	    	            }
        	    	    	    else {
        	    	    	        String TempStr = NodeData.toLowerCase();
        	    	    	        if(TempStr.indexOf("class a") != -1)
        	    	    	    	    sharesstate = 1;
        	    	    	    }
        	    	    	}	
        	            }
        	        }
        	    }
		    }
		}
		catch (Exception e) {
			return(true);
		}
		if(state >= 4)
		    mysql.WriteCombinedStockRecord(con, subInfo, cs, dateStr);
		return(bRtn);
	}
	
	
	private String RemoveTrailingDashes(String OrigStr) {  // static
		String rtnStr = "";
	    int    i;
	    
	    rtnStr = ReverseString(OrigStr);
        i = 0;
        while(i == 0) {
        	i = rtnStr.indexOf("-");
        	if(i == 0)
        		rtnStr = rtnStr.substring(1);
        }
        rtnStr = ReverseString(rtnStr);
		return (rtnStr);
	}
	
	private String ReverseString(String OrigStr) {  // static
		
	    int i, len = OrigStr.length();
	    StringBuffer dest = new StringBuffer(len);

	    for (i = (len - 1); i >= 0; i--)
	      dest.append(OrigStr.charAt(i));
	    return( dest.toString());
		
	}
	public UnMappedRowCls DbWriteFieldRawData(Connection con, SubmissionInfo si, int iTemplateId, int TemplateUid,  // static
	                                                 int iTrNdx, int iTdNdx, ArrayList<Integer> DateRefUid, int  iCurLine,
	                                                 ColumnPosInfo columnPosInfo, String RowDataStr, int iCompanyId, 
	                                                 UnMappedRowCls PrevRawDataUid) {
		
		UnMappedRowCls rtnCls = new UnMappedRowCls();
		int            dateRef = DateRefUid.get(columnPosInfo.iCurColumn) ;
	    MySqlAccess    mySqlAccess = new MySqlAccess();
	    NormalizeStrRtn normalizeStrRtn = null;
	    
	    normalizeStrRtn = NormalizeData(RowDataStr);
		RowDataStr = normalizeStrRtn.GetRtnStr();
		if(RowDataStr.length() > 0) {
		    if(PrevRawDataUid.getTaggedFieldUid() == 0) {
			    rtnCls = mySqlAccess.WriteRawDataRow(con, RowDataStr, si.getUid(), si.getVersion(), iCurLine,
	                    iTemplateId, TemplateUid, iCompanyId, iTrNdx, iTdNdx);
		    }
		    else {
			    rtnCls.setFieldsRowUid(PrevRawDataUid.getFieldsRowUid());
			    rtnCls.setTaggedFieldUid(PrevRawDataUid.getTaggedFieldUid());
		    }
            mySqlAccess.writeFieldLocated(con, si.getCompanyId(), si.getUid(), 
                    si.getVersion(), iTemplateId, TemplateUid, rtnCls.getTaggedFieldUid(), 
                    RowDataStr, dateRef,
                    false, 0, rtnCls.getFieldsRowUid(), iTrNdx, iTdNdx);
		}
		else {  // return class passe in
			rtnCls.setFieldsRowUid(PrevRawDataUid.getFieldsRowUid());
			rtnCls.setSpareJiffy(PrevRawDataUid.getSpareJiffy());
			rtnCls.setTaggedFieldUid(PrevRawDataUid.getTaggedFieldUid());
		}
		rtnCls.SetAddedParen(normalizeStrRtn.GetAddedParen());
		return (rtnCls);
	}
	
	private NormalizeStrRtn NormalizeData(String origStr) {  // static
		NormalizeStrRtn rtnCls = new NormalizeStrRtn();
		
		rtnCls.SetRtnStr(origStr.trim());
		if((origStr.equals(")")) || 
		   (origStr.equals("(")) ||
		   (origStr.equals("$")))
		   rtnCls.SetRtnStr("");
	 else {
		 if((origStr.contains("(")) &&
		    (origStr.contains(")") == false)) {
			 rtnCls.SetRtnStr(origStr + ")");
			 rtnCls.SetAddedParen(true);
		 }
	 }
	return(rtnCls);	 
	}
	
	public boolean DbWriteFieldNotFound(Connection con, SubmissionInfo si, int iTemplateId, int TemplateUid, int iMatchedTagUid,   // static
			                                   Node n, int iTrNdx, int iTdNdx, ArrayList<Integer> DateRefUid, int  iCurLine,
                            		           ColumnPosInfo columnPosInfo, ArrayList<Integer> colSpans, String RowDataStr,
                            		           int iCompanyId) {
		boolean        bRtn = true;
	    boolean        bIsAbstract = false;
	    UnMappedRowCls unMappedRowCls = null;
	    boolean        bInsertedData  = false;
        Node           dataNode = n;
	    int            iDateUid = 0;
        NodeList       fstNm;
        String         NodeData = "";
        int            iCurColumnIndex = iTdNdx;
        //int            FlUid;
        int            iOpenParens = 1;
	    MySqlAccess    mySqlAccess = new MySqlAccess();
        
        unMappedRowCls = mySqlAccess.writeUnmappedNode(con, si.getUid(), si.getVersion(), RowDataStr, iCurLine,
                iTemplateId, TemplateUid, bIsAbstract, iCompanyId, iTrNdx, iTdNdx);
		// THIS IS END OF WRITING FIRST COLUMN
        
        dataNode = dataNode.getNextSibling();
         while(dataNode != null){
        	 iTdNdx++;
            fstNm = dataNode.getChildNodes();
            int iLen = fstNm.getLength();
            iOpenParens = 1;
           if(iLen > 0) {
                NodeData = (fstNm.item(0)).getNodeValue();
                int iFoundOrphanPara = NodeData.indexOf("(");
                String PostStr;
                // iOpenParens = 1;  // Moved to above
                if(iFoundOrphanPara != -1) {
                    iFoundOrphanPara = NodeData.indexOf(")");  // check if here
                    while(iFoundOrphanPara == -1) { // we enter with found ( now we look for matching
                        dataNode = dataNode.getNextSibling();  // get next row
                        fstNm = dataNode.getChildNodes();
                        PostStr = (fstNm.item(0)).getNodeValue();
                        NodeData += PostStr;
                        iFoundOrphanPara = NodeData.indexOf(")");
                        iOpenParens += 1;
                     }
                }
                int dateRef = 0;
                if(DateRefUid.size() > 0) {
            	    if(columnPosInfo == null)
            		    dateRef = DateRefUid.get(iDateUid);
            	    else {
            		    dateRef = DateRefUid.get(columnPosInfo.iCurColumn);
            		    if(dateRef == 0)   // must have been a negative which is off to next
            		    	dateRef = DateRefUid.get(columnPosInfo.iCurColumn-1);  // therfore get the previous
            	    }
                }
                NodeData = NodeData.trim();
                if((NodeData.equals("")) || (NodeData.equals("$")))
            	    NodeData = "";
                else {
                    mySqlAccess.writeFieldLocated(con, si.getCompanyId(), si.getUid(), 
                                                          si.getVersion(), iTemplateId, TemplateUid, 
                                                          unMappedRowCls.getTaggedFieldUid(), NodeData, dateRef,
                                                          false, 0, unMappedRowCls.getFieldsRowUid(), iTrNdx, iTdNdx);
                    bInsertedData = true;
                }
                iDateUid++;
            }
		    while(iOpenParens > 0) {
		        iCurColumnIndex += 1;
		        iOpenParens -= 1;
		        if(iCurColumnIndex < colSpans.size())  // if equal we are at the end of the row
		            columnPosInfo.iCurColumn += colSpans.get(iCurColumnIndex);
		    }
           dataNode = dataNode.getNextSibling(); //XXXXX
        }
      
        
        // THIS IS THE END
	    if((bIsAbstract == false) && (bInsertedData == false)) {
	    	bRtn = false;
	        mySqlAccess.setTaggedFieldToAbstract(con, unMappedRowCls.getTaggedFieldUid());
	        mySqlAccess.WriteAbstractFieldLocated(con, si.getCompanyId(), si.getUid(), si.getVersion(),
	        		                              iTemplateId, TemplateUid, unMappedRowCls.getTaggedFieldUid(),
	        		                              RowDataStr, unMappedRowCls.getSpareJiffy(), false, iTrNdx, iTdNdx);
	    }
		return(bRtn);
	}
	
	/******************************************* 
	public static int ORIG_DbWriteFieldNotFound(Connection con, SubmissionInfo si, String NodeData,
            Node n,  ArrayList<Integer> DateRefUid, NodeIterator iterator, int iCurLine, int iTemplateId) {
        Node           dataNode = n;
        NodeList       fstNm;
        int            iDateUid = 0;
        int 		   iRtn = 1;   // we skip to next line 
        boolean        bContinue = true;
        String         ElementStr;
        String         CurStr;
        boolean        bIsAbstract = false;
        UnMappedRowCls unMappedRowCls = null;
        boolean        bInsertedData  = false;

         unMappedRowCls = MySqlAccess.writeUnmappedNode(con, si.getUid(), si.getVersion(), NodeData, iCurLine,
        		                                   iTemplateId, bIsAbstract);

        while(bContinue) {
            Node node = iterator.nextNode();
            if(node == null) {
                bContinue = false;
            }
            else {
                ElementStr = ((Element) node).getTagName();
                //System.out.println("ELEMENT: " + ElementStr);
                if(ElementStr.equals("tr"))
                    bContinue = false;
                else {
                    fstNm = node.getChildNodes();
                    if(fstNm.item(0) != null) {
                        CurStr = (fstNm.item(0)).getNodeValue();
                        if(CurStr != null) {
                            //System.out.println("No data at node");
                        //}
                        //else {
                           // System.out.println("NotFound DATA:" + CurStr);
                            if(CurStr.indexOf(("(")) != -1) {  // we got a negative number here
                                //i = NodeData.indexOf((")"));
                                while(CurStr.indexOf(")") == -1 )  {  // loop here to find the trailing )
                                    node = iterator.nextNode();
                                    fstNm = node.getChildNodes();
                                    CurStr += (fstNm.item(0).getNodeValue());
                                }
                            }
                            CurStr = CurStr.trim();
                            if(CurStr.length() > 0 ) {
                                MySqlAccess.writeFieldLocated(con, si.getCompanyId(), si.getUid(),si.getVersion(),
                            	    	                           iTemplateId, unMappedRowCls.getTaggedFieldUid(), CurStr,
                            		                               (DateRefUid.size() > 0) ? DateRefUid.get(iDateUid) : 0,
                            		                               false, 0, unMappedRowCls.getFieldsRowUid());
                                bInsertedData = true;
                            }
                            //MySqlAccess.writeUnmappedData(con, si.getUid(), iParentUid, si.getVersion(),
                            //                              (DateRefUid.size() > 0) ? DateRefUid.get(iDateUid) : 0, CurStr);
                        }
                    }
                }
                //node = iterator.nextNode();
           }
        }
        if((bIsAbstract == false) && (bInsertedData == false))
        	MySqlAccess.setTaggedFieldToAbstract(con, unMappedRowCls.getTaggedFieldUid());
        return(iRtn);  // number of nodes to skip in parent
    }
*****************************************/
}
