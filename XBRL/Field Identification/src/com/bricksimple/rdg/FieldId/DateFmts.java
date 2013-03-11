package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;
//import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

//import java.text.*;



import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DateFmts {

	public DateRefCls GetNoteTableColumns(Connection con, Document NormlDoc , Document PreNormlDoc,  // static
                                          SubmissionInfo si,  int iTemplateUid, TdColSpan tdColSpan,  TemplateHdr templateHdr, 
                                          ArrayList<String> dateForms, FieldMatchStr[] tblMatchArray) {
		
		DateRefCls             rtnCls = new DateRefCls();
        DateExtractSummary     PreNormlEs = new DateExtractSummary();

        PreNormlEs = CheckIfFirstRowFirstColumnInUse(PreNormlDoc, con, si, tdColSpan);
        if(PreNormlEs.iNumOfRows == -1) {
            PreNormlEs = ExtractNoteColumns(PreNormlDoc, con, si, tdColSpan, dateForms, tblMatchArray);
        }
        else {
        	if(PreNormlEs.AbstractStr.length() > 0) {
        		PreNormlEs = ExtractNoteColumnsWithAbstract(PreNormlDoc, con, si, tdColSpan, dateForms, tblMatchArray, PreNormlEs.AbstractStr);
        	}
        }
        rtnCls.DateRefUid = LogAllDateRefs(con, PreNormlEs, si, iTemplateUid);
        if(rtnCls.DateRefUid.size() > 0) {
            rtnCls.DateRefUid.add(PreNormlEs.iNumOfRows);
	    }
		return(rtnCls);
	}
	
	private DateExtractSummary CheckIfFirstRowFirstColumnInUse(Document doc, Connection con, SubmissionInfo si, TdColSpan tdColSpan) {
        DateExtractSummary     PreNormlEs = new DateExtractSummary();
	    int                    iNumColumns = GetNumberOfColumns(tdColSpan);
	    DateExtract            de = null;
	    DocumentTraversal      traversal = null;
	    NodeIterator           iterator = null;
	    String                 ElementStr, NodeData;
        int                    iTrCount = 0;
        int                    iTdCount = 0;
        boolean                bHaveRowText = false;
        String                 posAbstract = "";
        boolean                bMultipleColumnsInUse = false;
        
        PreNormlEs.iNumOfRows = -1;  // in case not ours
        for(int tempI = 0; tempI < iNumColumns; tempI++) {
        	de = new DateExtract();
        	PreNormlEs.dateExtract.add(de);
        }
        traversal = (DocumentTraversal) doc;
        iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
        	if((iTrCount > 1) && (bMultipleColumnsInUse == true))
        		break;
            ElementStr = ((Element) n).getTagName();
            if((ElementStr.equals("tr")) || (ElementStr.equals("TR"))) {
            	if(bHaveRowText == false)   // reset  trCount if read a row with nothing in it
            		iTrCount = 0;   
            	iTrCount++;
            	iTdCount = 0;
            	bMultipleColumnsInUse = false;
            }
            else {
            	iTdCount++;
            }
            NodeList fstNm = n.getChildNodes();
            if(fstNm.item(0) != null) {
                NodeData = (fstNm.item(0)).getNodeValue();
                if((NodeData != null) && (NodeData.trim().length() > 0)){  // we got some text
                	bHaveRowText = true;
                	if(iTdCount == 1) {
                		if(DataIsToBeIgnored(NodeData) == false) {
                			if(iTrCount == 1)  {
                				PreNormlEs.iNumOfRows = 0;
                				posAbstract = NodeData.trim();
                			}
                			else {
                				posAbstract = posAbstract + " " + NodeData.trim();
                			}
                		}
                	}
                	else {  // not the first column with data
                	    bMultipleColumnsInUse = true;	
                	}
                }
            }       	
        }
        if((PreNormlEs.iNumOfRows == 0) && (iTrCount > 1)) {
            PreNormlEs.AbstractStr = posAbstract;
            PreNormlEs.iNumOfRows = iTrCount;
        }
	    return(PreNormlEs);
	}
	
	
	private boolean DataIsToBeIgnored(String fieldData) {
		boolean bRtn = false;
		
		if(fieldData.toLowerCase().contains("in thousands"))
			bRtn = true;
		else {
			if(fieldData.toLowerCase().contains("in $000's"))
				bRtn = true;
		}
		return(bRtn);
	}
	
	private DateExtractSummary ExtractNoteColumns(Document doc, Connection con, SubmissionInfo si, TdColSpan tdColSpan,
			                                      ArrayList<String> dateForms,FieldMatchStr[] tblMatchArray) {
		
	    ArrayList<ArrayList<Integer>> RowUsage = new ArrayList<ArrayList<Integer>>();
	    DateExtractSummary            rtn = new DateExtractSummary();
	    DocumentTraversal             traversal = null;
	    NodeIterator                  iterator = null;
	    DateExtract                   de = null;
	    int                           iNumColumns = GetNumberOfColumns(tdColSpan);
	    boolean                       bDateFromDomDone = false, bFoundSomeData = false, bMtRow = false;
	    String                        ElementStr, NodeData;
        int                           iTrCount = 0, iTdCount = 0, iRealTdCount = 0, iPreviousColumnWidths = 0, iThisColumnWidth = 0;
        boolean                       bFirstRowFirstColumnUsed = false;
        boolean                       SkipByFirstColumnData = false;
        boolean                       bRecordData = true;
        RowUsage = InitializeRowUsage(tdColSpan);
        // First allocate the slots
        for(int tempI = 0; tempI < iNumColumns; tempI++) {
        	de = new DateExtract();
        	rtn.dateExtract.add(de);
        }
        traversal = (DocumentTraversal) doc;
        iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
        	if( bDateFromDomDone == true)
        		break;   // alternate exit if found end of what we think is the date info
             ElementStr = ((Element) n).getTagName();
             if((ElementStr.equals("tr")) || (ElementStr.equals("TR"))) {
                 iTrCount++;
                 iTdCount = 0;  // as we bump it when we get it first
           	     iRealTdCount = 0;
            	 iPreviousColumnWidths = 0;
                 iThisColumnWidth = 0;
            	 if((bFoundSomeData == true) && (bMtRow == true)) {// MT row following data is end of it
            		 // we fake out abstractStr as not part of the test here
            		 if(CheckIfDataFound(rtn.dateExtract, "x",  4))
            		     bDateFromDomDone = true;
            	 }
            	 bMtRow = true;
             }
             else {
                 if((ElementStr.equals("td") || (ElementStr.equals("TD")))) {
                	 iPreviousColumnWidths += iThisColumnWidth;
                	 //int jj = tdColSpan.TblRow.get(iTrCount-1).size();
                	 iTdCount++;
                	 int TempTdCount = iTdCount;  // this is to adjust iPreviousColumnWidths when rowSpan encountered
                	 iTdCount = GetOpenColumn(RowUsage, iTrCount, iTdCount);
                	 iPreviousColumnWidths += (iTdCount - TempTdCount);  // this completes the previous columns in case of RowSpan
                	 if(iRealTdCount < tdColSpan.TblRow.get(iTrCount-1).size())  // moved to below the get
                	     iThisColumnWidth = tdColSpan.TblRow.get(iTrCount-1).get(iRealTdCount);
                	 iRealTdCount += 1;
                  }
             }
             NodeList fstNm = n.getChildNodes();
             if(fstNm.item(0) != null) {
                 NodeData = (fstNm.item(0)).getNodeValue();
                 if((NodeData != null) && (NodeData.trim().length() > 0)){  // we got some text
                	 bMtRow = false;
                	 bFirstRowFirstColumnUsed = true;
                     if((iTdCount == 1)	&& (bFoundSomeData == true) && (SkipByFirstColumnData == false)) {// if we have previous data just get out
                         bDateFromDomDone = IsDataSpecialStr(NodeData);
                     }
                     else {
                    	 bRecordData = true;
                    	 if((iTrCount >= 1) && (iTdCount == 1))
                    			 bRecordData = IsDataSpecialStr(NodeData);
                    	 if(bRecordData) {
                   	         bFoundSomeData = true;   // set up to get out next time first row has data
	        	             for(int ii =0; ii < iThisColumnWidth; ii++) {
			                     if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
   			    	                 rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
	       				         rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += NodeData;
       	        	         }
	        	             SkipByFirstColumnData = CheckIfPartialDate(NodeData, rtn.dateExtract);
                    	 }
                     }
                 }
             }
        }
        if(bFirstRowFirstColumnUsed == true) {
        	bFirstRowFirstColumnUsed = DoesFirstRowContainFact(rtn);
        }
        if (bFirstRowFirstColumnUsed == true) {
            CleanUpDateExtract(rtn);
            rtn.iNumOfRows = 0;
        }
        else {
            rtn.iNumOfRows = iTrCount -1;
        }
        rtn.dii.GetDateInfo(rtn.dateExtract);
        rtn.iNumOfColumns = GetNumberOfDataColumns(iterator, rtn.dii.iCountOfColumns);
        iterator.detach(); 
        return(rtn);
	}
	
	private DateExtractSummary ExtractNoteColumnsWithAbstract(Document doc, Connection con, SubmissionInfo si, TdColSpan tdColSpan,
                                                              ArrayList<String> dateForms,FieldMatchStr[] tblMatchArray, String abstractStr) {
	    ArrayList<ArrayList<Integer>> RowUsage = new ArrayList<ArrayList<Integer>>();
	    DateExtractSummary            rtn = new DateExtractSummary();
	    DocumentTraversal             traversal = null;
	    NodeIterator                  iterator = null;
	    DateExtract                   de = null;
	    int                           iNumColumns = GetNumberOfColumns(tdColSpan);
	    boolean                       bDateFromDomDone = false, bFoundSomeData = false, bMtRow = false;
	    String                        ElementStr, NodeData;
        int                           iTrCount = 0, iTdCount = 0, iRealTdCount = 0, iPreviousColumnWidths = 0, iThisColumnWidth = 0;
        boolean                       SkipByFirstColumnData = false;
        boolean                       bRecordData = true;
        
        RowUsage = InitializeRowUsage(tdColSpan);
        // First allocate the slots
        for(int tempI = 0; tempI < iNumColumns; tempI++) {
        	de = new DateExtract();
        	rtn.dateExtract.add(de);
        }
        traversal = (DocumentTraversal) doc;
        iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
        	if( bDateFromDomDone == true)
        		break;   // alternate exit if found end of what we think is the date info
             ElementStr = ((Element) n).getTagName();
             if((ElementStr.equals("tr")) || (ElementStr.equals("TR"))) {
                 iTrCount++;
                 iTdCount = 0;  // as we bump it when we get it first
           	     iRealTdCount = 0;
            	 iPreviousColumnWidths = 0;
                 iThisColumnWidth = 0;
            	 if((bFoundSomeData == true) && (bMtRow == true)) {// MT row following data is end of it
            		 // we fake out abstractStr as not part of the test here
            		 if(CheckIfDataFound(rtn.dateExtract, "x",  4))
            		     bDateFromDomDone = true;
            	 }
            	 bMtRow = true;
             }
             else {
                 if((ElementStr.equals("td") || (ElementStr.equals("TD")))) {
                	 iPreviousColumnWidths += iThisColumnWidth;
                	 //int jj = tdColSpan.TblRow.get(iTrCount-1).size();
                	 iTdCount++;
                	 int TempTdCount = iTdCount;  // this is to adjust iPreviousColumnWidths when rowSpan encountered
                	 iTdCount = GetOpenColumn(RowUsage, iTrCount, iTdCount);
                	 iPreviousColumnWidths += (iTdCount - TempTdCount);  // this completes the previous columns in case of RowSpan
                	 if(iRealTdCount < tdColSpan.TblRow.get(iTrCount-1).size())  // moved to below the get
                	     iThisColumnWidth = tdColSpan.TblRow.get(iTrCount-1).get(iRealTdCount);
                	 iRealTdCount += 1;
                  }
             }
             NodeList fstNm = n.getChildNodes();
             if(fstNm.item(0) != null) {
                 NodeData = (fstNm.item(0)).getNodeValue();
                 if((NodeData != null) && (NodeData.trim().length() > 0)){  // we got some text
                	 bMtRow = false;
                     if((iTdCount == 1)	&& (bFoundSomeData == true) && (SkipByFirstColumnData == false)) {// if we have previous data just get out
                         bDateFromDomDone = IsDataSpecialStr(NodeData);
                     }
                     else {
                    	 bRecordData = true;
                    	 if((iTrCount >= 1) && (iTdCount == 1))
                    			 bRecordData = IsDataSpecialStr(NodeData);
                    	 if(bRecordData) {
                    		 if(iTdCount == 1) {
                    			 if(rtn.AbstractStr.length() > 0)
                    				 rtn.AbstractStr = rtn.AbstractStr + " ";
                    			 rtn.AbstractStr = rtn.AbstractStr + NodeData;
                    			 if(rtn.AbstractStr.equals(abstractStr))
                    				 bFoundSomeData = true;
                    		 }
                    		 else {
                   	             bFoundSomeData = true;   // set up to get out next time first row has data
	        	                 for(int ii =0; ii < iThisColumnWidth; ii++) {
			                         if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
   			    	                     rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
	       				             rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += NodeData;
       	        	             }
	        	                 SkipByFirstColumnData = CheckIfPartialDate(NodeData, rtn.dateExtract);
                    		 }
                    	 }
                     }
                 }
             }
        }
        //if(bFirstRowFirstColumnUsed == true) {
        //	bFirstRowFirstColumnUsed = DoesFirstRowContainFact(rtn);
        //}
        //if (bFirstRowFirstColumnUsed == true) {
        //    CleanUpDateExtract(rtn);
        //    rtn.iNumOfRows = 0;
        //}
        //else {
            rtn.iNumOfRows = iTrCount -1;
        //}
        rtn.dii.GetDateInfo(rtn.dateExtract);
        rtn.iNumOfColumns = GetNumberOfDataColumns(iterator, rtn.dii.iCountOfColumns);
        iterator.detach(); 
        return(rtn);
	
	}
	
	private boolean CheckIfPartialDate(String nodeData, ArrayList<DateExtract> dateExtract) {
		boolean bRtn = false;
		String  extract = "";
		boolean bAllExtractsTheSame = true;
		int     dups = 0;
		
		for(DateExtract de: dateExtract) {
			if(de.SupStr.length() > 0) {
				if(extract.length() == 0)
					extract = de.SupStr;
				else {
					if(extract.equals(de.SupStr) == false)
						bAllExtractsTheSame = false;
					else
						dups++;
				}
			}
		}
		// check if all extracts the same
		if((dups > 1) && (bAllExtractsTheSame == true)) {
			// we could check for a partial date
			bRtn = true;
		}
		return(bRtn);
	}
	
	private boolean IsDataSpecialStr(String NodeData) {
		boolean bRtn = true;
		String  testStr = NodeData.toLowerCase();
		
   	    if(testStr.contains("in $000's") == true)  // if this just ignore
   	    	bRtn = false;
   	    else {
   	    	if(testStr.contains("in thousands") == true)
   	    		bRtn = false;
   	    }
        return(bRtn);
	}
	
	private boolean DoesFirstRowContainFact(DateExtractSummary des) {
		boolean bRtn = false;
        String  temp = "";
        
		for(DateExtract de: des.dateExtract) {
			temp = temp + de.SupStr;
		}
        if(temp.contains("%"))
        	bRtn = true;
        else {
        	if(temp.contains("$"))
        		bRtn = true;
        }
		return bRtn;
	}
	
	private void CleanUpDateExtract(DateExtractSummary des) {
		
		for(DateExtract de: des.dateExtract) {
			de.SupStr = "";
		}
	}
	
	public DateRefCls GetTableDates(Connection con, Document NormlDoc , Document PreNormlDoc,  // static
			                                       int iMyDateFmt, SubmissionInfo si, int iTemplateId,
			                                       int iTemplateUid, TdColSpan tdColSpan, 
			                                       TemplateHdr templateHdr, ArrayList<String> dateForms,
			                                       FieldMatchStr[] tblMatchArray) {
		DateRefCls             rtnCls = new DateRefCls();
        DateExtractSummary     PreNormlEs = new DateExtractSummary();
        MySqlAccess            mySql = new MySqlAccess();
        MatchStr[] DateSups =  mySql.GetListOfDateSup(con);
        
        PreNormlEs = ProcessExtractSummary(PreNormlDoc, DateSups, con, si, iTemplateId, tdColSpan, dateForms, tblMatchArray);
        
        rtnCls.AbstractStr = PreNormlEs.AbstractStr;
        rtnCls.DateRefUid = LogUsedDateRefs(con, PreNormlEs, si, templateHdr, iTemplateId, iTemplateUid, DateSups);
        if(rtnCls.DateRefUid.size() > 0) {
        	if((PreNormlEs.bUseRowAbstract == true) && (PreNormlEs.iRowAbstractStr > 0))
        		rtnCls.DateRefUid.add(PreNormlEs.iRowAbstractStr);
        	else
        	    rtnCls.DateRefUid.add(PreNormlEs.iNumOfRows);
	    }
        return (rtnCls);
	 }
	
	
	public DateRefCls GetPreviousRefs(Document doc, Connection con, SubmissionInfo si, int iTemplateType,  // static
			                                 DateRefCls prevDateRefCls) {
		DateRefCls             rtnCls = new DateRefCls();
        DocumentTraversal      traversal = (DocumentTraversal) doc;
        String                 ElementStr;
        int                    iTrNdx = -1;
        int                    iTdNdx = 0;
        String                 NodeData;
        int                    iNdx = 0;
        
        //dateRefs = MySqlAccess.GetContinuationDateRefs(con, si, iTemplateType);
        //for(int thisRef : dateRefs) {
       // 	rtnCls.DateRefUid.add(thisRef);
        //}
        while(iNdx < (prevDateRefCls.DateRefUid.size() -1)) {
        	rtnCls.DateRefUid.add(prevDateRefCls.DateRefUid.get(iNdx));
        	iNdx++;
        }
       	NodeIterator iterator = traversal.createNodeIterator(
                doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        Node n = iterator.nextNode();
        boolean bIterate = true;
        while((n != null) && (bIterate)) {
 	        ElementStr = ((Element) n).getTagName();
	        if(ElementStr.equals("tr") || ElementStr.equals("TR")) {
		        iTrNdx++;
		        iTdNdx = 0;
	        }
	        else {
		        if(ElementStr.equals("td") || ElementStr.equals("TD")) {
		            iTdNdx++;    	
           			NodeList fstNm = n.getChildNodes();
        			NodeData = null;
        			if(fstNm.getLength() > 0) {
        				if(fstNm.item(0) != null) {
        					NodeData = (fstNm.item(0)).getNodeValue();
               				if(NodeData != null) {
            					NodeData = NodeData.trim();
            					if(NodeData.length() > 0) {
            						if(iTdNdx == 1) {
            							bIterate = false;
            						}
            						else {
            							rtnCls.AbstractStr = NodeData;
            						}
       				            }
               				}
        				}
        			}
		        }
	        }
	        n = iterator.nextNode();
        }
        iterator.detach();
        rtnCls.DateRefUid.add(iTrNdx);
		return(rtnCls);
	}
	
	/****************************************************************************/
	/* note the last entry in the list returned is the number of lines(tr's) to */
	/*      skip. They contain the date definitions                             */
	/****************************************************************************/
	
	private DateExtractSummary ProcessExtractSummary (Document doc, MatchStr[] DateSups, Connection con,  // static
			                                                SubmissionInfo si, int iTemplateId, TdColSpan tdColSpan,
			                                                ArrayList<String> dateForms, FieldMatchStr[] tblMatchArray) {
		
		DateExtractSummary            rtn = new DateExtractSummary();
        DocumentTraversal             traversal = null;
        NodeIterator                  iterator = null;
        String                        ElementStr;
        boolean                       bDateFromDomDone = false;
        int                           iTrCount = 0;
        int                           iTdCount = 0;
        String                        NodeData;
        DateExtract                   de = null;
        SupplementRtn                 supplementRtn = null;
        boolean                       bDebug = false;
        int                           iNumColumns = GetNumberOfColumns(tdColSpan);
        int                           iPreviousColumnWidths= 0;
        int                           iThisColumnWidth = 0;
        int                           iAddedColumnInfo = 0;
        int                           ii = 0;
        SupplementCls                 supplementCls = new SupplementCls();
        Template7                     template7 = new Template7();
        boolean                       bNotePosExit = false;
        boolean                       bMtRow = true;
        boolean                       bFoundSomeData = false;
        UndoNoteStr                   undoNoteStr = new UndoNoteStr();
        CONSTANTS                     constants = new CONSTANTS();
        MySqlAccess                   mySqlAccess = new MySqlAccess();
        ArrayList<ArrayList<Integer>> RowUsage = new ArrayList<ArrayList<Integer>>();
        boolean                       bBumpTdCount = false;
        int                           iRealTdCount = 0;
        String                        totalRowText = "";
        
        RowUsage = InitializeRowUsage(tdColSpan);
        // First allocate the slots
        for(int tempI = 0; tempI < iNumColumns; tempI++) {
        	de = new DateExtract();
        	de.MyColumnNum = tempI;
        	rtn.dateExtract.add(de);
        }
        traversal = (DocumentTraversal) doc;
        iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
        for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
        	if( bDateFromDomDone == true)
        		break;   // alternate exit if found end of what we think is the date info
             ElementStr = ((Element) n).getTagName();
             if((ElementStr.equals("tr")) || (ElementStr.equals("TR"))) {
            	 totalRowText = "";
            	 bBumpTdCount = false;
            	 if(bNotePosExit == true) {
		    		 ErrorCls errorCls = new ErrorCls();
			 		 errorCls.setSubUid(0);
					 errorCls.setCompanyUid(0);
					 errorCls.setItemVersion(0);
					 errorCls.setErrorText("Log: Note alternate exit SUCCESS");
					 errorCls.setBExit(false);
					 mySqlAccess.WriteAppError(con, errorCls);	
            		 break;
            	 }
            	 if((iTemplateId == 4) && (undoNoteStr.bInUse == true)) {
            		 undoNoteStr.RemovePreviousStr(rtn);
            		 bDateFromDomDone = true;
           	     }
            	 else {
            	     iTrCount++;
            	     iTdCount = 0;  // as we bump it when we get it first
            	     iRealTdCount = 0;
            	     //iTdCount =  GetOpenColumn(RowUsage, iTrCount, iTdCount);
            	     iPreviousColumnWidths = 0;
            	     iThisColumnWidth = 0;
            	     if((bFoundSomeData == true) && (bMtRow == true)) {// MT row following data is end of it
            		     if(CheckIfDataFound(rtn.dateExtract, rtn.AbstractStr, iTemplateId))
            		         bDateFromDomDone = true;
            	     }
            	     bMtRow = true;
            	 }
             }
             else {
                 if((ElementStr.equals("td") || (ElementStr.equals("TD")))) {
                	 iPreviousColumnWidths += iThisColumnWidth;
                	 //int jj = tdColSpan.TblRow.get(iTrCount-1).size();
                	 if(bBumpTdCount == true)
                	     iTdCount++;
                	 int TempTdCount = iTdCount;  // this is to adjust iPreviousColumnWidths when rowSpan encountered
                	 iTdCount = GetOpenColumn(RowUsage, iTrCount, iTdCount);
                	 if(bDebug) {
                		 System.out.println("iPreviousColumnWiths adjustment: " + (iTdCount - TempTdCount));
                	 }
                	 iPreviousColumnWidths += (iTdCount - TempTdCount);  // this completes the previous columns in case of RowSpan
                	 if(iRealTdCount < tdColSpan.TblRow.get(iTrCount-1).size())  // moved to below the get
                	     iThisColumnWidth = tdColSpan.TblRow.get(iTrCount-1).get(iRealTdCount);
                	 iRealTdCount += 1;
                	 if(bBumpTdCount == false)
                		 bBumpTdCount = true;
                  }
             }
             NodeList fstNm = n.getChildNodes();
             if(fstNm.item(0) != null) {
                 NodeData = (fstNm.item(0)).getNodeValue();
                 if((NodeData != null) && (NodeData.trim().length() > 0)){  // we got some text
                	 bNotePosExit = false;
                	 bMtRow = false;  // got data to process
                	 if(iTemplateId == 4)
                		 undoNoteStr.Reset();
                	 bFoundSomeData = true;
                	 if(bDebug) {
                		 System.out.println("TrCount = " + iTrCount + ": TdCount = " + iTdCount + " : NodeData = " + NodeData + "::");
                	 }
                	 if(iTemplateId == 7) {
                		 String StockStr = "";
                		 StockStr = IsTemplate7Str(NodeData, rtn.dateExtract.get(iPreviousColumnWidths).SupStr);
                		 if(StockStr.length() > 0) {
                    		 template7.FirstColumn.add(iPreviousColumnWidths);                
                    		 template7.LastColumn.add(iPreviousColumnWidths + iThisColumnWidth -1);
                    		 template7.myUid.add(mySqlAccess.InsertTemplate7Column(con, si, StockStr));
                		 }
               	    }
                	totalRowText += NodeData.trim();
                 	supplementRtn = supplementCls.IsSupplementStr(NodeData, DateSups, dateForms);
        	        if( supplementRtn.getiRtn() != -1) {
        	        	String lowerNodeData = NodeData.toLowerCase();
        	        	if((iAddedColumnInfo == 0) && (lowerNodeData.equals("(unaudited)"))) {
        	        		mySqlAccess.SetTemplateUnaudited(con, si, iTemplateId);
        	        	}
        	        	else {
        	        		if((lowerNodeData.indexOf("in thousands") != -1) ||
        	        			(lowerNodeData.indexOf("in 000's") != -1) ||
        	        			(lowerNodeData.indexOf("in $000's") != -1)) {   // REMOVED (iAddedColumnInfo < 2) &&  
        	        			mySqlAccess.SetTemplateScale(con, si, iTemplateId, 1000);
        	        			iAddedColumnInfo++;
        	        		}
        	        	    else {
            	        		if (lowerNodeData.indexOf("in millions") != -1) {
            	        			mySqlAccess.SetTemplateScale(con, si, iTemplateId, 1000000);
            	        			iAddedColumnInfo++;
            	        		}
            	        	    else {
                	        		if (lowerNodeData.indexOf("in billions") != -1) {
                	        			mySqlAccess.SetTemplateScale(con, si, iTemplateId, 1000000000);
                	        			iAddedColumnInfo++;
                	        		}
                	        	    else {
      	        	    	            iAddedColumnInfo++;
        	        		            for(ii =0; ii < iThisColumnWidth; ii++) {
	    				    		        if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
   	    				    		            rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
        	        			            rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr +=
        	        				                                  supplementRtn.getSupplementStr();
       	        		
            	           	                if(supplementRtn.getPartialDateStr().length() > 0) {
            	        		                rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate = 
            	        		    	                             supplementRtn.getPartialDateStr();
            	           	                }
            	           	                if(supplementRtn.getCompleteDateStr().length() > 0) {
            	           	            	    rtn.dateExtract.get(iPreviousColumnWidths +ii).CompletedDate = 
            	           	            		                     supplementRtn.getCompleteDateStr();
            	           	                }
        	        		            }
            	        	        }
        	        		    }
        	        	    }
        	        	}
        	        }
        	        else {
        	        	NodeData = RemoveAbrevMonths(NodeData.trim());
        	        	if(ContainsTwoDates(NodeData) == false) {
        	    	        if(supplementCls.CompleteDate(NodeData, dateForms) == true) { // it's a date
        	    	        	if((iTdCount == 0) && (iTemplateId == 7))
        	    	        		// we fake out abstractstr as it is not required testing
        	    	        		bDateFromDomDone = CheckIfDataFound(rtn.dateExtract, "x", iTemplateId);
        	    	        	if(bDateFromDomDone == false) {
        	    	    	        iAddedColumnInfo++;
        	        		        for(ii =0; ii < iThisColumnWidth; ii++) {
        	        			        if(rtn.dateExtract.get(iPreviousColumnWidths + ii).CompletedDate.length() > 0) {
	    				    		        if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
   	    				    	    	        rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
        	        				        rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += NodeData;
               	        	            }
        	        		            else {
        	        			            if(rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate.length() > 0) {
        	        			    	        rtn.dateExtract.get(iPreviousColumnWidths + ii).CompletedDate =
        	        			    		        rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate + " " + NodeData;
        	        			    	        rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate = "";
        	        			            }
        	        			            else
        	        			    	        rtn.dateExtract.get(iPreviousColumnWidths + ii).CompletedDate = NodeData;
        	        		            }
        	        		        }  // End of FOR LOOP
        	    	        	}
        	    	        }
        	    	        else  {  // not a date - do we ignore or append to previous
        	    			    if(constants.DoesStrContainMonth(NodeData)) {
        	    				    iAddedColumnInfo++;
        	    				    for(ii = 0; ii < iThisColumnWidth; ii++) {
        	    				        if(rtn.dateExtract.get(iPreviousColumnWidths + ii).CompletedDate.length() > 0) {
	    				    		        if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
   	    				    		            rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
       	    					            rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += NodeData;
         	    				        }
       	    					        else {      	    						
       	    						        rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate = NodeData;   
       	    					        }
       	    					    }
        	    			    }
        	    			    else { // does this complete a partial
	    				    	    String IsLen = NodeData.trim();
	    				            if(IsLen.length() > 0) {
       	    				           if((iAddedColumnInfo > 0) && (iTdCount == 0)) {
       	    				    	       bDateFromDomDone = IsNoPartialDates(rtn.dateExtract);
       	    				    	       if(bDateFromDomDone == false)  {   // if TRUE we be done so get out as this is first line of data
       	    				    	    	   // check if supplements present
       	    				    	    	   bDateFromDomDone = IsSupplementsPresent(rtn.dateExtract);
       	    				    	    	   if(bDateFromDomDone == false) {
       	    				    	    	       if(iTemplateId == 4) {  // NO ABSTRACTS IN NOTES!! note really we may want to undo
       	    				    	    		       undoNoteStr.bInUse = true;
       	    				    	    		       undoNoteStr.iNumCol = iThisColumnWidth;
       	    				    	    		       undoNoteStr.iStartCol = iPreviousColumnWidths;
       	    				    	    		       undoNoteStr.strNodeData = NodeData;
       	    				    	    		       undoNoteStr.iTrCount = iTrCount;
  	    		        	    				       for(ii = 0; ii < iThisColumnWidth; ii++) {
  	    		        	    					       if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
  	    		        	    						        rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
   	    		   	    				                   rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr = NodeData;
   	    		       	    					       }    	    				    				      	    				    			    			   

       	    				    	    	       }
       	    				    	    	       else {
       	    				    		               if(rtn.AbstractStr.length() == 0) {
       	    				    	                       rtn.AbstractStr = NodeData;
      	    				    			               rtn.iRowAbstractStr = iTrCount -1;
      	    				    			               bMtRow = true;  // abstracts do not count
       	    				    		               }
       	    				    		               else {
       	    				    		        	       // we fake out abstractStr here so not to test
       	    				    		                   bDateFromDomDone = CheckIfDataFound(rtn.dateExtract, "x", iTemplateId);
       	    				    		                   if(bDateFromDomDone == false) {
       	    				    		            	       rtn.iRowAbstractStr = 0;
       	    				    		            	       rtn.AbstractStr = "";  // clear as must be header info
       	    				    		                   }
       	    				    		               }
       	    				    		           }
       	    				    	    	   }
       	    				    	       }
       	    				           }
       	    				           else  {
       	    				    	       if((iAddedColumnInfo > 0) && (iTdCount > 0)) {
       	    				    		       iAddedColumnInfo++;
       	    				    		       NumericCls numericCls = new NumericCls();
       	    				    		       numericCls.ExtractNumeric(NodeData);
       	    				    		    //int aInt = CONSTANTS.isNumeric(NodeData);
       	    				    		       for(ii =0; ii < iThisColumnWidth; ii++) {
       	    				    		    	    if((numericCls.GetNumericValue() > 2000) && (rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate.length() > 0)) {
       	    				    		    		    String strComma = " ";
       	    				    		    		    if(rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate.indexOf(",") == -1)
       	    				    		    			    strComma = ",";
       	    				    		    		    rtn.dateExtract.get(iPreviousColumnWidths + ii).CompletedDate =
       	    				    		    			//rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate + strComma + NodeData;
       	    				    		    			    rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate + strComma + numericCls.GetNumericStr();
       	    				    		    		    rtn.dateExtract.get(iPreviousColumnWidths + ii).PartialDate = "";
       	    				    		    	    }
       	    				    		    	    else {
       	    				    		    		    if((NonEquivalents(rtn.dateExtract.get(iPreviousColumnWidths+ii).SupStr, NodeData) == false)  ||
       	    				    		    		       (iTemplateId == 7)) {
       	    				    		    		    	int iBeginMonth = constants.ReturnMonthIndex(NodeData);
       	    				    		    		    	if((iBeginMonth  == 0) || (iBeginMonth == -1)) {
       	    				    		    		    		//check here for running into data
       	    				    		    		    		bDateFromDomDone = bDateFromDomDone | CheckForData(NodeData, totalRowText);
       	    				    		    		    		if(bDateFromDomDone == false) {
       	    				    		                            if(rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr.length() > 0)
       	    				    		    	                        rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += " ";
       	    				    		                            rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr += NodeData;
       	    				    		    		    		}
       	    				    		    		    	}
       	    				    		    		    	else { // move text before month to sup else goes to partial
       	    				    		    		    		String supStr = NodeData.substring(0, (iBeginMonth));
       	    				    		    		    		String PartialStr = NodeData.substring(iBeginMonth);
            	    				    				           rtn.dateExtract.get(iPreviousColumnWidths +ii).PartialDate = PartialStr;
            	    				    				           if(rtn.dateExtract.get(iPreviousColumnWidths +ii).SupStr.length() > 0)
            	    				    				        	   rtn.dateExtract.get(iPreviousColumnWidths +ii).SupStr += " ";
               	    				    				           rtn.dateExtract.get(iPreviousColumnWidths +ii).SupStr += supStr;
      	    				    		    		    	}
       	    				    		    		    }
       	    				    		    		    else { 
       	    				    		    			    rtn.bUseRowAbstract = true;  // this is an row to record
       	    				    		    			    bDateFromDomDone = true;
       	    				    		    			    rtn.iRowAbstractStr = 0;
       	    				    		    			    rtn.AbstractStr = "";   // clear as it is not an abstract
       	    				    		    		    }
       	    				    		    	    }
       	    				    		        }
       	    				    	       }  // this is the fall off end - for header lines
       	    				    	       else {  // one last try
       	    				    		   // check if month  was this but found one that iTdCount was 3 
       	    				    		   //if((CONSTANTS.MonthInString(NodeData)) && (iAddedColumnInfo == 0) && (iTdCount == 1)) {
       	    				    		       if((constants.MonthInString(NodeData)) && (iAddedColumnInfo == 0)) {
       	    				    			   // this is one 
       	    				    			       int iBeginMonth = constants.ReturnMonthIndex(NodeData);
       	    				    			       if(iBeginMonth >= 0) {
                                                       String SupStr = "";
       	    				    			           if(iBeginMonth > 1)  //SupStr
       	    				    				           SupStr = NodeData.substring(0, (iBeginMonth));
       	    				    			           if(constants.MonthInString(SupStr) == false) {  // only supp + date if no month in proceeding
       	    				    			               String PartialStr = NodeData.substring(iBeginMonth);
       	    				    			               for(ii= 0; ii < iThisColumnWidth; ii++) {
       	    				    				               rtn.dateExtract.get(iPreviousColumnWidths +ii).PartialDate = PartialStr;
       	    				    				               rtn.dateExtract.get(iPreviousColumnWidths +ii).SupStr = SupStr;
       	    				    			               }
       	    				    			               iAddedColumnInfo++;
       	    				    			           }
       	    				    			       }
       	    				    		       }
       	    				    		       else { // may be just year??
          	    				    		       NumericCls numericCls = new NumericCls();
           	    				    		       numericCls.ExtractNumeric(NodeData);
      	    				    		        //int aInt = CONSTANTS.isNumeric(NodeData);
       	    				    			    //if(aInt > 2000) {
           	    				    		       if(numericCls.GetNumericValue() > 2000) {
       	    		        	    				   iAddedColumnInfo++;
       	    		        	    				   for(ii = 0; ii < iThisColumnWidth; ii++) {
       	    		   	    				            //rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr = NodeData;
       	    		   	    				               rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr = numericCls.GetNumericStr();
       	    		   	    				       	   }    	    				    				
       	    				    			       }
       	    				    			       else {  // it may be sup only
       	    				    			           if((iAddedColumnInfo == 0) && (iPreviousColumnWidths  > 0)) {
           	    		        	    				   iAddedColumnInfo++;
           	    		        	    				   for(ii = 0; ii < iThisColumnWidth; ii++) {
           	    		   	    				               rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr = NodeData;
           	    		       	    					   } 
           	    		        	    				   bNotePosExit = false;  // NOPE undos below!!
       	    				    			    	   }
       	    				    			    	   else  {
       	    				    			    		   if((iTemplateId == 4) && (iTrCount == 1) && (iTdCount == 0)) {
               	    		        	    				   for(ii = 0; ii < iThisColumnWidth; ii++) {
               	    		   	    				               rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr = NodeData;
               	    		       	    					   }    	    				    				      	    				    			    			   
       	    				    			    		       bNotePosExit = true;
       	    				    			    			   ErrorCls errorCls = new ErrorCls();
       	    				    			 		           errorCls.setSubUid(0);
       	    				    					           errorCls.setCompanyUid(0);
       	    				    					           errorCls.setItemVersion(0);
       	    				    					           errorCls.setErrorText("Log: Note alternate exit with: " + NodeData);
       	    				    					           errorCls.setBExit(false);
       	    				    					           mySqlAccess.WriteAppError(con, errorCls);	
       	    				    			    	       }
       	    				    			    		   else {  // maybe an abstract so put aside
       	                            					       CheckConceptRtn checkConceptRtn = new CheckConceptRtn();
       	                         						       checkConceptRtn = constants.CheckForConcepts(NodeData, tblMatchArray);
       	                         						       if(checkConceptRtn.iMatchIndex != -1) {
       	                         						    	    if(tblMatchArray[checkConceptRtn.iMatchIndex ].getAbstract() == true ) {
       	                         						    	    	rtn.AbstractStr = NodeData;
       	                         						    	    }
       	                         						       }
       	    				    			    		   }
       	    				    			    	   }
       	    				    			       }
       	    				    		       }
       	    				    	       }
       	    				           }
         	    				   }
        	    		       }
        	    	        }
        	        	} // End of 'if it contains two dates
        	        	else {
        	        		if((iTemplateId == 7) &&(iTdCount == 0) &&(iTrCount > 1))
        	        			bDateFromDomDone = CheckSupStrForData(rtn.dateExtract);
        	        		else {
        	        			// check if this is a inception to
        	        			if(PreviousOrCompletedDates(rtn.dateExtract, iPreviousColumnWidths) == true) {
   				    		       for(ii =0; ii < iThisColumnWidth; ii++) {
    				    		       rtn.dateExtract.get(iPreviousColumnWidths + ii).SupStr = NodeData;
   				    		    	}
        	        			}
        	        		}
        	        	}
       	            }
                } 
                 else {
                	 if((rtn.iRowAbstractStr > 0) && (iTdCount == 0)) { // MT line and down some
                		 bDateFromDomDone = CheckIfDataFound(rtn.dateExtract, rtn.AbstractStr, iTemplateId);;
                	 }
                 }
            }
        }                	 
        rtn.dii.GetDateInfo(rtn.dateExtract);
        rtn.iNumOfColumns = GetNumberOfDataColumns(iterator, rtn.dii.iCountOfColumns);
        rtn.iNumOfRows = iTrCount -1;
        if(template7.myUid.size() > 0)  {  // map stockholderuids onto the rows
        	for(int i = 0; i < template7.myUid.size(); i++) {
        		int j = template7.FirstColumn.get(i);
        		while(j <= template7.LastColumn.get(i)) {
        	        rtn.dateExtract.get(j).StockHolderUid = template7.myUid.get(i);
        	        j++;
        		}
        	}
        }
        iterator.detach(); 
		return(rtn);
		
	}
	
    private boolean CheckForData(String nodeData, String totalRowText) {
    	boolean bRtn = false;
    	
    	if((nodeData.contains("$")) && (nodeData.trim().length() == 1) && (totalRowText.equals(nodeData.trim()))) {
    		bRtn = true;
    	}
    	else {
    		if(nodeData.equals("-") && (totalRowText.equals(nodeData.trim())))
    			bRtn = true;
    	}
    	return(bRtn);
    }
	private boolean PreviousOrCompletedDates(ArrayList<DateExtract> dateExtract, int iPreviousColumns) {  // static
		boolean bRtn = false;
	  
		int     iNum = 0;
		int     iCount = 0;
		
		for(DateExtract curItem : dateExtract) {
			if(iCount < iPreviousColumns)
			    if(curItem.CompletedDate.length() > 0) {
				iNum++;
			}
			if(curItem.PartialDate.length() > 0) {
				iNum++;
			}
			iCount++;
		}
		if(iNum >0)
			bRtn = true;
		return(bRtn);
	}
	
	private boolean CheckSupStrForData(ArrayList<DateExtract> ddi) {  // static
		boolean bRtn = true;
		int     iNumSups = 0;
		int     i;
		
		for(i = 0; i < ddi.size(); i++) {
			if(ddi.get(i).SupStr.length() > 0)
				iNumSups++;
		}
		if(iNumSups <= 5)
			bRtn = false;
		return(bRtn);
	}
	
 // removes abbreviated month strings
	private String RemoveAbrevMonths(String origStr) {  // static
		String rtnStr = origStr;
		rtnStr = origStr.replace("Sept.", "September");
		
		return(rtnStr);
	}
	
	// checks the return array to ensure data being passed back
	// otherwise we just encountered an MT row with some submission info
	private boolean CheckIfDataFound(ArrayList<DateExtract>  dateExtract, String abstractStr, int iTemplateId) {  // static
		boolean           bRtn = false;
		ArrayList<String> CompletedDates = new ArrayList<String>();
		ArrayList<String> PartialDates = new ArrayList<String>();
		ArrayList<String> SupStrs = new ArrayList<String>();
		int               iMinimum = 2;
		
		// determine the minimum number of columns that must be labled
		iMinimum = (dateExtract.size()/6 > 2) ? dateExtract.size()/6 : 2;
		for(DateExtract curItem : dateExtract) {
			if(curItem.CompletedDate.length() > 0) {
				bRtn = true;
				AddToList(CompletedDates, curItem.CompletedDate);
			}
			if(curItem.PartialDate.length() > 0) {
				bRtn = true;
				AddToList(PartialDates, curItem.PartialDate);
			}
			// not really sure about this - I have found some with just year as column header
			if((curItem.SupStr.length() > 0)) { // && (iTemplateId == 7)) {
				bRtn = true;
				AddToList(SupStrs, curItem.SupStr);
			}
		}
		if(bRtn == true) {  // we have some data - now enough or just header junk
			       // if all less than two - something amiss  does not count for NOTES
			if((CompletedDates.size() < iMinimum)  && (PartialDates.size() < iMinimum) && 
			   (SupStrs.size() < iMinimum) && (iTemplateId != 4)) {
				if(abstractStr.length() == 0) {
				    bRtn = false;
				    // clear all data previously found if below minimum 
				    if(SupStrs.size() < 3) {
				        for(DateExtract curItem: dateExtract) {
					        curItem.CompletedDate = "";
					        curItem.PartialDate = "";
					        curItem.SupStr = "";
				        }
				    }
				}
			}
		}
		return(bRtn);
	}
	
	private void AddToList(ArrayList<String> prevStr, String newStr) {  // static
		
		if(prevStr.size() == 0)
			prevStr.add(newStr);
		else {
			boolean bFound = false;
			for(int i = 0; i < prevStr.size(); i++) {
				if(prevStr.get(i).equals(newStr))
					bFound = true;
			}
			if(bFound == false)
				prevStr.add(newStr);
		}
	}
	
	private boolean ContainsTwoDates(String origStr) {  // static
		boolean   bRtn = false;  // no two dates
		String    loweredOrig = origStr.toLowerCase();
		CONSTANTS constants = new CONSTANTS();
		
		int iMonthNdx = constants.GetFirstMonthContained(loweredOrig);
		if(iMonthNdx != -1) {  // found at least one month
			int iMonthIndex = loweredOrig.indexOf(CONSTANTS.MonthName.get(iMonthNdx));
			loweredOrig = loweredOrig.substring(CONSTANTS.MonthName.get(iMonthNdx).length() + iMonthIndex);
			iMonthNdx = constants.GetFirstMonthContained(loweredOrig);
			if(iMonthNdx != -1)
				bRtn = true;
		}
		return(bRtn);
	}
	
	private boolean NonEquivalents(String str1, String str2) {  // static
		boolean bRtn = false;  // not equivalent
		
		if(str1.length() > 0) {  // string 2 definitely has length
			int iNumStr1 = CheckForNumber(str1);
			int iNumStr2 = CheckForNumber(str2);
			if(iNumStr1 != -1) {   // only check num if num
			    if((iNumStr1 == iNumStr2) && (iNumStr1 != 0))
				    bRtn = true;
			}
		}
		return(bRtn);
	}
	
	private int CheckForNumber(String numStr) {  // static
		int iRtn = -1;
		
		if(numStr.contains("three"))
			iRtn = 3;
		if(numStr.contains("four"))
			iRtn = 4;
		if(numStr.contains("six"))
			iRtn = 6;
		if(numStr.contains("nine"))
			iRtn = 9;
		if(numStr.contains("twelve"))
			iRtn = 12;
		return(iRtn);
	}
	
	private boolean IsSupplementsPresent(ArrayList<DateExtract> dateExtract) {
		boolean bRtn = false;
		int     i = 0;
		
		while((bRtn == false) && (i < dateExtract.size())) {
			if(dateExtract.get(i).SupStr.length() > 0)
				bRtn = true;
			i++;
		}
		
		return(bRtn);
	}
	private boolean IsNoPartialDates(ArrayList<DateExtract>  dateExtract) {  // static
		boolean bRtn = false;
		int     i = 0;
		boolean bFoundDate = false;
		
		while((bFoundDate == false) && (i < dateExtract.size())) {
			if(dateExtract.get(i).CompletedDate.length() > 0) {
				bFoundDate = true;
				bRtn = true;
			}
			i++;
		}
		// this was added to stop sportchatel_10k-040311 which  has only supstr's
		if(bRtn == false) {
			i = 0;
			bRtn = true;  // we assume the end
			while((bFoundDate == false) && (i < dateExtract.size())) {
                if(dateExtract.get(i).PartialDate.length() > 0) {  // if we have a partial date, we assume year to com
                	bFoundDate = true;
                	bRtn = false;
                }
                i++;
			}
			if(bRtn == true) {
			// check that we have at least 3 different sups
			    ArrayList<String> sups = new ArrayList<String>();
			    for(i = 0; i < dateExtract.size();i++) {
			    	if(dateExtract.get(i).SupStr.length() > 0)
			    		AddToList(sups, dateExtract.get(i).SupStr);
			    }
			    if(sups.size() > 2)
			    	bRtn = true;
			    else
			    	bRtn = false;
			}
			// this is to check if we have a 'for the period'
			if(bRtn == false)  { 
				i = 0;
				while((bFoundDate == false) && (i < dateExtract.size())) {
					if(dateExtract.get(i).SupStr.contains("for the period") == true) { 
						bFoundDate = true;
						bRtn = true;
					}
					i++;
				}
			}
		}
		return(bRtn);
	}
	
	private int GetNumberOfColumns(TdColSpan tdColSpan) {  // static
		int iRtn = 0;
		int iMax = 0;
		
		for(int j = 0; j < tdColSpan.TblRow.size(); j++) {
		    iMax = 0;
		    for(int i = 0; i < tdColSpan.TblRow.get(j).size(); i++)
			    iMax += tdColSpan.TblRow.get(j).get(i);
		    if(iMax > iRtn)
		    	iRtn = iMax;
		}
		return(iRtn);
	}

	private String IsTemplate7Str(String NodeData, String SupStr) {  // static
		String rtnStr = "";
		String  testStr = NodeData.toLowerCase();
		
		if(testStr.contains("preferred stock"))
			rtnStr = NodeData;
		else {
			if(testStr.contains("common stock"))
				rtnStr = NodeData;
			else {   // may have been split so check if last word
				if(testStr.contains("stock")) {  // getting closer check if previous line
					testStr = SupStr.toLowerCase();
					if((testStr.contains("preferred")) || (testStr.contains("common")))
						rtnStr = SupStr + " " + NodeData;
				}
			}
		}
		if(rtnStr.length() > 64) // too long must be something else
			rtnStr = "";
		return(rtnStr);
	}
	private ArrayList<Integer> LogAllDateRefs (Connection con, DateExtractSummary des, SubmissionInfo si, int iTemplateUid) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
		int                iDateRefUID; 
	    MySqlAccess        mySqlAccess = new MySqlAccess();
		
		for(int i = 0; i < des.dateExtract.size(); i++) {
            iDateRefUID = mySqlAccess.WriteDateRef(con, si.getUid(), 
                    si.getVersion(), iTemplateUid, "", 
                    des.dateExtract.get(i).SupStr, 0, des.dateExtract.get(i).MyColumnNum);
            rtnArray.add(iDateRefUID);	    			    			
			
		}
		return(rtnArray);
		
	}
	
	private ArrayList<Integer> LogUsedDateRefs(Connection con, DateExtractSummary des,  // static
			                                          SubmissionInfo si, TemplateHdr templateHdr,
			                                          int iTemplateId, int iTemplateUid, MatchStr[] DateSups) {
	    ArrayList<Integer> rtnArray = new ArrayList<Integer>();
	    int                           i = 0;
	    int                           iDateRefUID;
	    ArrayList<ReformatedDateRefs> ArrayRDR = new ArrayList<ReformatedDateRefs>();
	    MySqlAccess                   mySqlAccess = new MySqlAccess();
	    boolean                       bUseAllDates = false;
	    
	    ReformatedDateRefs reformatedDateRefs; 
	    
	    for(i= 0; i < des.dateExtract.size(); i++) {
	    	if(bUseAllDates == false) {
	    		if(des.dateExtract.get(i).CompletedDate.length() > 0)
	    			bUseAllDates = true;
	    		else {
	    			if(des.dateExtract.get(i).PartialDate.length() > 0)
	    				bUseAllDates = true;
	    		}
	    	}
	    }
	    for(i =0; i < des.dateExtract.size(); i++) {
	    	reformatedDateRefs = new ReformatedDateRefs();
	    	reformatedDateRefs.SetUsed(false);
	    	if(des.dateExtract.get(i).CompletedDate.length() > 0)
	    		reformatedDateRefs.SetUsed(true);
	    	else {
	    		if((des.dateExtract.get(i).SupStr.length() > 0) &&
	    		   (des.dateExtract.get(i).PartialDate.length() == 0))
	    			reformatedDateRefs.SetUsed(true);
	    		else {
	    			if((bUseAllDates == true) && des.dateExtract.get(i).PartialDate.length() > 0)
	    				reformatedDateRefs.SetUsed(true);
	    		}
	    	}
	    	if(reformatedDateRefs.GetUsed() == true) {
	    		reformatedDateRefs.SetCompletedDate(des.dateExtract.get(i).CompletedDate);
	    		reformatedDateRefs.SetSupStr(des.dateExtract.get(i).SupStr);
	    		if((des.dateExtract.get(i).CompletedDate.length() == 0) && (des.dateExtract.get(i).SupStr.length() == 0))
	    			reformatedDateRefs.SetSupStr(des.dateExtract.get(i).PartialDate);
	    		reformatedDateRefs.FixUpDates(templateHdr, iTemplateId, DateSups);
	    	}
    		ArrayRDR.add(reformatedDateRefs);
	    }
	    ArrayList<Integer> existingDateRefs;
	    int iExistingIndex = 0;
	    existingDateRefs = CheckForVerticalMerge(con, ArrayRDR, si, iTemplateId, iTemplateUid);
	    for(i = 0; i < ArrayRDR.size(); i++) {
	   		if(ArrayRDR.get(i).GetUsed() == true) {
	   			if(existingDateRefs.size() > 0) {
	   				rtnArray.add(existingDateRefs.get(iExistingIndex));
	   				iExistingIndex++;
	   			}
	   			else {
                    iDateRefUID = mySqlAccess.WriteDateRef(con, si.getUid(), 
	                            si.getVersion(), iTemplateUid, ArrayRDR.get(i).GetCompletedDate(), 
	                            ArrayRDR.get(i).GetSupStr(), des.dateExtract.get(i).StockHolderUid,
	                            des.dateExtract.get(i).MyColumnNum);
	                rtnArray.add(iDateRefUID);	    			    			
	   			}
	   		}
	    	else
	    		rtnArray.add(0);
	    }
	    return(rtnArray);
	}
	
	private ArrayList<Integer> CheckForVerticalMerge(Connection con, ArrayList<ReformatedDateRefs> ArrayRDR,  // static
			                                                SubmissionInfo si, int iTemplateId, int iTemplateUid) {
		ArrayList<Integer> rtnArray = new ArrayList<Integer>();
	       MySqlAccess     mySqlAccess = new MySqlAccess();
	       
		ArrayList<Integer> dupTemplates = mySqlAccess.GetDuplicateTemplateUids(con, si.getUid(), si.getVersion(),
				                                                               iTemplateId, iTemplateUid);
		ArrayList<Integer> posDupDateRefs;
		int i = 0;
		while((i < dupTemplates.size()) && (rtnArray.size() == 0)) {
			posDupDateRefs = mySqlAccess.GetDateRefs(con, dupTemplates.get(i));
			if(posDupDateRefs.size() > 0) {
				if(ArrayRDR.size() == posDupDateRefs.size()) {
			        if(ColumnsMatch(ArrayRDR, posDupDateRefs, con)) {
			           for(int kk = 0; kk < posDupDateRefs.size(); kk++) {
				           rtnArray.add(posDupDateRefs.get(kk));
			           }
			       }
			    }
			}
			i++;
		}
		return(rtnArray);
	}
	
	private boolean ColumnsMatch(ArrayList<ReformatedDateRefs> ArrayRDR, ArrayList<Integer> posDupDateRefs, Connection con) {  // static
		boolean     bRtn = true;
		int         iPosDupIndex = 0;
		int         iArrayRDRindex = 0;
	    MySqlAccess mySqlAccess = new MySqlAccess();
	
		DateRefRec DRR = new DateRefRec();
		boolean    bContinue = true;
		
		while((bContinue) && (bRtn)) {
			if(ArrayRDR.get(iArrayRDRindex).GetUsed() == true) {
		        DRR = mySqlAccess.GetDateRefRec(con, posDupDateRefs.get(iPosDupIndex));
		        iPosDupIndex++;  // bump to next DB record
		        bRtn = DbRecSameAsCurRec(DRR, ArrayRDR.get(iArrayRDRindex));
			}
			iArrayRDRindex++;
			if(iArrayRDRindex == ArrayRDR.size())  // if we checked all
				bContinue = false;                 // leave
		}
		return(bRtn);
	}
	
	private boolean DbRecSameAsCurRec(DateRefRec DRR, ReformatedDateRefs RDR) {  // static
		boolean bRtn = false;
		
		if(DRR.GetCompletedDate().equals(RDR.GetCompletedDate())) {
			if(DRR.GetSupStr().equals(RDR.GetSupStr()))
				bRtn = true;
		}
		return(bRtn);
	}
	
	private int GetNumberOfDataColumns(NodeIterator iterator, int iNumCompletedDates) {  // static
		int    iNumColumns = 0;
        String ElementStr;
        int    iCurCounter = 0;
        String NodeData = "";
        int    iTdNdx = 0;
        
        for(Node n = iterator.nextNode(); n != null; n=iterator.nextNode()) {
              ElementStr = ((Element) n).getTagName();
            //System.out.println("ELEMENT: " + ElementStr);
            if((ElementStr.equals("tr")) || (ElementStr.equals("TR"))) {
            	if(iCurCounter >= iNumCompletedDates)
            		break;
            	iCurCounter = 0;
            	iTdNdx = 0;
             }
            else {
                if((ElementStr.equals("td")) || (ElementStr.equals("TD"))) {
                	if(iTdNdx > 0) {  // we don't count the first one
                        NodeList fstNm = n.getChildNodes();
                        if(fstNm.item(0) != null) {
                            NodeData = (fstNm.item(0)).getNodeValue();
                            if(NodeData != null) {
                        	    String Temp = NodeData.trim();
                        	    if(Temp.length() > 0) {
                        	    	if(Temp.compareTo("$") != 0)
                	                    iCurCounter++;
                        	    }
                        	}
                        }
                    }
                	iTdNdx++;  // bump column index
                }
            }
        }
        iNumColumns = iCurCounter;
		return(iNumColumns);
	}
	
	
	public Document GetPreNorml(Connection con, SubmissionInfo si, int beginLineNum, int endLineNum) {  // static
		Document         doc  = null;
		FileInputStream  fstream;
		DataInputStream  in;
	    String           dataStr = "";
		BufferedReader   br;
        int              iCurLine = 1;	
        String           CurLine = "";
        int              iTableNdx = 0;
        CONSTANTS        constants = new CONSTANTS();
        boolean          bFirstLine = true;
        
 	    try {
            fstream = new FileInputStream(si.getExtractFile());
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));
            while(iCurLine <= endLineNum) {  //first we write any unmapped areas including preamble/TOC
            	CurLine = br.readLine();
            	if(iCurLine >= beginLineNum) {
            		if(bFirstLine) {  // remove stuff preceding <table>
            			bFirstLine = false;
            			int iOffset = constants.CheckForBeginTable(CurLine);
            			if(iOffset > 0)
            				CurLine = CurLine.substring(iOffset);
            		}
            		iTableNdx = constants.CheckForEndTable(CurLine);
            		if(iTableNdx == -1)
            			dataStr += CurLine;
            		else
            			dataStr += CurLine.substring(0, (iTableNdx + 8));
            	}
            	iCurLine++;
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(dataStr));
            doc = db.parse(is);
	    }
	    catch (Exception e) {
	    }
		return(doc);
	}
	
	private ArrayList<ArrayList<Integer>> InitializeRowUsage(TdColSpan tdColSpan) {
		ArrayList<ArrayList<Integer>> rtnArray = new ArrayList<ArrayList<Integer>>();
		
		int                iRowSize;
		int                iRowOffset;
		int                iCurColumn;
		int                iColSize;
		int                iColOffset;
		int                iNumColumns = 0;	
		int                iCurColumnsCount =0;
		ArrayList<Integer> dummy = null;
		
		
		for(int i = 0; i < tdColSpan.TblRow.size(); i++) {
			iCurColumnsCount = 0;
			for(int j = 0; j < tdColSpan.TblRow.get(i).size(); j++)
				iCurColumnsCount += tdColSpan.TblRow.get(i).get(j).intValue();
			if(iCurColumnsCount > iNumColumns)
		        iNumColumns = iCurColumnsCount;
		}
		for(int i = 0; i < tdColSpan.TblCol.size(); i++) {
			dummy = new ArrayList<Integer>(); 
		    for(int j = 0; j < iNumColumns; j++) 
			    dummy.add(0);
			rtnArray.add(dummy);
		}
		for(int i = 0; i < tdColSpan.TblCol.size(); i++) {
			iCurColumn = 0;
			while(rtnArray.get(i).get(iCurColumn) == 1)
				iCurColumn++;
			for(int j = 0; j < tdColSpan.TblCol.get(i).size(); j++) {
				iRowSize = tdColSpan.TblCol.get(i).get(j).intValue();
				iColSize = tdColSpan.TblRow.get(i).get(j).intValue();
				iRowOffset = 1;
				// if we span rows??
				while(iRowOffset < iRowSize) { // for each ROWSPAN
					iColOffset = 0;
					while(iColOffset < iColSize) {
						// set lower row column used  with my columen(i) then my column which my SPAN multiple columns
						rtnArray.get(i + iRowOffset).set(iCurColumn + iColOffset, 1);
						iColOffset++;
					}
					iRowOffset++;
				}
				iCurColumn += iColSize;  // adjust to our current column
			}
		}
		return(rtnArray);
	}
	
	private int GetOpenColumn(ArrayList<ArrayList<Integer>> ColUsage, int iTrCount, int iTdCount) {
		int iRtn = iTdCount;
		
		// TrCount and TdCount have  already been bumped
		try {
			while(ColUsage.get(iTrCount-1).get(iRtn).intValue() == 1)
				iRtn++;
		}
		catch (Exception e) {
			
			//iRtn = -1;
		}
		return(iRtn);
	}
}
