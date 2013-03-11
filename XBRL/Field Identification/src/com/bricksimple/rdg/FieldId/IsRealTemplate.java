package com.bricksimple.rdg.FieldId;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

import java.sql.*;

public class IsRealTemplate {

	public boolean Test(SubmissionInfo si, String TableStr, int iTemplateUid, FieldMatchStr[] tblMatchArray,
			            Connection con) {
		boolean                  bRtn = false;
		/*************************************************
		String                   ElementStr;
		boolean                  bFirstTd = true;
		String                   NodeData;
        ConfidenceLevel          conLevel = new ConfidenceLevel();
		
		try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(TableStr));

            Document doc = db.parse(is);
            DocumentTraversal traversal = (DocumentTraversal) doc;
            NodeIterator iterator = traversal.createNodeIterator(doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
            Node n = iterator.nextNode();
           
            while((n != null) && (bRtn == false)) {
                ElementStr = ((Element) n).getTagName();
                //System.out.println("ELEMENT: " + ElementStr);
                if(ElementStr.equals("tr") || ElementStr.equals("TR")) {
                    bFirstTd = true;
                }
                else {
                    if(ElementStr.equals("td") || ElementStr.equals("TD") && (bFirstTd == true)) {
                        bFirstTd = false;
                        NodeList fstNm = n.getChildNodes();
                        NodeData = null;
                        if(fstNm.getLength() > 0) {
                            if(fstNm.item(0) != null) {
                                NodeData = (fstNm.item(0)).getNodeValue();
                                if(NodeData != null) {
                            	    NodeData = NodeData.trim();
                            	    if(NodeData.length() > 0) {
                    	    	            int j = 0;
                   	    	            double dMaxConfidence = 0;
                   	    	            int    iMaxIndex = -1;
                   	    	            double dConfidence;
                   	    	            while(j < tblMatchArray.length) {
                                            dConfidence = conLevel.compareToArrayList(NodeData, tblMatchArray[j].Al, tblMatchArray[j].getFieldStr());
                                            if(dConfidence >= tblMatchArray[j].getThreshold()) {
                                    	        if(dConfidence > dMaxConfidence) {
                                    		        dMaxConfidence = dConfidence;
                                    		        iMaxIndex = j;
                                    	        }
                                            }
                                            j++;
                   	    	            }
                   	    	            if(iMaxIndex != -1) {
                                            if(tblMatchArray[iMaxIndex].getAbstract() == false ) {
                                            	bRtn = true;
                                            }
                   	    	            }
                            	    }
                                }
                           }
                        }
                    }
                }
                n = iterator.nextNode();
            }   // while
		}
		catch (Exception e) {
			ErrorCls errorCls = new ErrorCls();
			
			errorCls.setFunctionStr("IsRealTemplate");
		    errorCls.setSubUid(si.getUid());
		    errorCls.setCompanyUid(si.getCompanyId());
		    errorCls.setItemVersion(si.getCompanyId());    
            errorCls.setErrorText("Unable to parse expected template: " + e.getMessage());
    	    MySqlAccess.WriteAppError(con, errorCls);
    	    return(false);
		}	
		bRtn = true;    // force through
		if(bRtn == false) {
			MySqlAccess.MarkAsDummy(con, iTemplateUid);
		}
		************************/
		bRtn = true;    // force through
		return(bRtn);
	}
}
