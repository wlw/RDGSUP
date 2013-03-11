package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

public class XbrlFiles {
	
    private int          iFileType;
    private String       FileName;
    private String       Root;
    private NodeIterator iterator = null;
    private XbrlNode     RootNode = new XbrlNode();

    public int GetFileType() {
    	return(iFileType);
    }
    
    public String GetRoot() {
    	return(Root);
    }
    
    public void SetIterator(NodeIterator nodeIterator) {
    	iterator = nodeIterator;
    }
    
    public NodeIterator GetIterator() {
    	return iterator;
    }
    
    public String GetFileName() {
    	return(FileName);
    }
    
    public XbrlNode GetRootNode() {
    	return(RootNode);
    }
    
    public int SetXbrlFile(String fileName) {
    	
    	int    iRtn = 0;
    	String workStr = fileName.toLowerCase();
    	int    iTemp = fileName.lastIndexOf(".");
    	String sTemp = fileName.substring(iTemp +1 );
    	
    	Root = fileName.substring(0, iTemp);
    	FileName = fileName;
    	if(sTemp.equals("htm"))
    		iFileType = CONSTANTS.XBRL_HTM;
    	else {
    		if(sTemp.equals("xsd"))
    			iFileType = CONSTANTS.XBRL_XSD;
    		else {
    			if(sTemp.equals("xml")) {
    			    sTemp = workStr.substring(0, iTemp);
    			    iTemp = sTemp.lastIndexOf("_");
    			    if(iTemp == -1)  
    			    	iFileType = CONSTANTS.XBRL_XML;
    			    else {
    			    	Root = sTemp.substring(0, iTemp);
    				    sTemp = sTemp.substring(iTemp + 1);
    				    if(sTemp.equals("cal"))
    					    iFileType = CONSTANTS.XBRL_CAL;
    				    else {
        				    if(sTemp.equals("def"))
        					    iFileType = CONSTANTS.XBRL_DEF;
        				    else {
            				    if(sTemp.equals("lab"))
            					    iFileType = CONSTANTS.XBRL_LAB;
            				    else {
                				    if(sTemp.equals("pre"))
                					    iFileType = CONSTANTS.XBRL_PRE;
                				    else 
                					    iFileType = CONSTANTS.XBRL_XML;
                				}

            				}

        				}
    					
    				}
    			}
    		}
    	}
    	return(iRtn);
    }
}
