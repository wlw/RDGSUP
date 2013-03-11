package com.bricksimple.rdg.xbrlUpload;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeIterator;

import static org.w3c.dom.Node.ATTRIBUTE_NODE;
import static org.w3c.dom.Node.CDATA_SECTION_NODE;
import static org.w3c.dom.Node.COMMENT_NODE;
import static org.w3c.dom.Node.DOCUMENT_TYPE_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.ENTITY_NODE;
import static org.w3c.dom.Node.ENTITY_REFERENCE_NODE;
import static org.w3c.dom.Node.NOTATION_NODE;
import static org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

public class TestIterator {

	public void Iterate(XbrlFileStruct xbrlFileStruct, boolean bWriteOutFile) {
		
		NodeIterator iterator = null;
		String       NodeData;
		XbrlNode     xbrlNode;
		
		for(XbrlFiles xbrlFiles: xbrlFileStruct.GetXbrlFiles()) {
		   	if(xbrlFiles.GetFileType() != CONSTANTS.XBRL_HTM) {
			    if(xbrlFiles.GetIterator() != null) {
				    iterator = xbrlFiles.GetIterator();
	                Node n = iterator.nextNode();
	                xbrlNode = xbrlFiles.GetRootNode();
	                ProcessNode(xbrlNode, n);
	                iterator.detach();
	            }
			    if(bWriteOutFile)
		            WriteDataToFile(xbrlFileStruct.GetFilePath(), xbrlFiles);
		    }
		}
	}
	
	private void WriteDataToFile(String path, XbrlFiles xbrlFiles) {
		
		PrintWriter  out = null;
        XbrlNode     node = xbrlFiles.GetRootNode();
        int          iNumSpaces = 0;
        
		try {
		    FileWriter outFile = new FileWriter(path + "\\" + xbrlFiles.GetFileName() + ".out");
            out = new PrintWriter(outFile);
            PrintNodeInfo(out, node, iNumSpaces);
            out.close();
            outFile.close();
		    }
		catch (Exception e) {
			if(out != null)
			    out.println("Error writing XML: " + e.getMessage());
			else
				System.out.println("Error writing XML: " + e.getMessage());
		}
	}
	
	private void PrintNodeInfo(PrintWriter out, XbrlNode  node, int iNumSpaces) {
		String preSpaces = "";
		
		preSpaces = AppendSpaces(iNumSpaces);
		out.println(preSpaces + "Tag: " + node.GetTag());
		for(NodeAttribute attr: node.GetAttributes()) {
			out.println(preSpaces + " Att: " + attr.GetName() + " Value: " + attr.GetValue());
		}
		if(node.GetData().length() > 0)
			out.println(preSpaces + " Data:" + node.GetData());
		for(XbrlNode child: node.GetChildren()) {
			PrintNodeInfo(out, child, iNumSpaces + 1);
		}
		if(node.GetSibling() != null)
			PrintNodeInfo(out, node.GetSibling(), iNumSpaces);
	}
	
	private String AppendSpaces(int iNumSpaces) {
		String rtnStr = "";
		int    i = 0;
		
		while(i < iNumSpaces) {
			rtnStr += "     ";
			i++;
		}
		return(rtnStr);
	}
	
	private void ProcessNode(XbrlNode xbrlNode, Node n) {
		int  i;
		Node curNode;
		XbrlNode curXbrlNode;
		XbrlNode Sibling;
		
		xbrlNode.SetTag(n.getNodeName());
		short type  = n.getNodeType();
		switch (type) {
		    case ELEMENT_NODE:
		        Element eElement = (Element) n;
		        AddAttributes(eElement, xbrlNode);
			    SetNodeData(n, xbrlNode);
               break;
               
		    case TEXT_NODE:  // ignore for now
		    	break;
		}
	    NodeList children = n.getChildNodes();
	    if(children.getLength() > 0) {
	    	int len = children.getLength();
	        for(i = 0; i < children.getLength(); i++) {
	    	    curNode = children.item(i);
		    	if(RealNode(curNode) == true) {
	    	    	curXbrlNode = new XbrlNode();
	    	        xbrlNode.SetChild(curXbrlNode);
	    	   	    ProcessNode(curXbrlNode, curNode);
	    		}
	        }
	    }
	    Node siblingNode = n.getNextSibling();
	    if(siblingNode != null) {
	    	if(RealNode(siblingNode) == true) {
     		    Sibling = new XbrlNode();
    		    xbrlNode.SetSibling(Sibling);
    	        ProcessNode(Sibling, siblingNode);
    		}
	    }
	}
	
	private boolean RealNode(Node checkNode) {
		boolean bRtn = false;
		
		short siblingtype  = checkNode.getNodeType();
		switch (siblingtype) {
		    case ELEMENT_NODE:
		    	bRtn = true;
               break;
               
		    case TEXT_NODE:  // ignore for now
		    	break;
		}
		return(bRtn);
	}
	
	private void SetNodeData(Node n, XbrlNode xbrlNode) {
		
		String NodeData = "";
		
       	NodeList fstNm = n.getChildNodes();
    	int iLen = fstNm.getLength();
    	if(iLen > 0) {
    	    NodeData = (fstNm.item(0)).getNodeValue();
    	    if(NodeData != null) {
    	    	NodeData = NodeData.trim();
     	    }
    	    else
    	    	NodeData = "";
    	}
		if(NodeData == null)
			xbrlNode.SetData("");
		else
			xbrlNode.SetData(NodeData);
	}
	
	private void AddAttributes(Element eElement, XbrlNode xbrlNode) {
		int            nnmLength = 0;
		int            i = 0;
		Attr           attribute;
		NamedNodeMap   nnm = null;
		NodeAttribute  curAttribute;
		
   	    nnm = eElement.getAttributes();
   	    nnmLength = nnm.getLength();
   	    for(i = 0; i < nnmLength; i++) {
   		    attribute = (Attr)nnm.item(i);
   		    curAttribute = new NodeAttribute();
   		    curAttribute.SetName(attribute.getName());
   		    curAttribute.SetValue(attribute.getValue());
   		    xbrlNode.AddAttribute(curAttribute);
   	    }
		
	}
}
