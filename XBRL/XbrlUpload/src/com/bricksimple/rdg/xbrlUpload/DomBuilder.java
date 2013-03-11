package com.bricksimple.rdg.xbrlUpload;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.sql.Connection;

import org.w3c.dom.Document;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;

import com.bricksimple.rdg.sqlaccess.MySqlAccess;


public class DomBuilder {

	public boolean InsertFileIntoDom(Connection con, MySqlAccess mySql, String path, XbrlFiles xbrlFiles) {
		boolean bRtn = true;
		File fxmlFile = new File(path + xbrlFiles.GetFileName());
		ErrorCls errorCls = new ErrorCls();
		
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("InsertFileIntoDom");
		errorCls.setItemVersion(0);
		if(xbrlFiles.GetFileType() != CONSTANTS.XBRL_HTM) {
		    try {
                DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                Document doc = db.parse(fxmlFile);
	            DocumentTraversal traversal = (DocumentTraversal) doc;
	
                xbrlFiles.SetIterator(traversal.createNodeIterator(
                                      doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true));
		    }
		    catch (Exception e) {
    		    errorCls.setErrorText("Error building DOM: " + e.getMessage());
	            mySql.WriteAppError(con, errorCls);
			    bRtn = false;
		    }
		}
		return(bRtn);
	}
}
