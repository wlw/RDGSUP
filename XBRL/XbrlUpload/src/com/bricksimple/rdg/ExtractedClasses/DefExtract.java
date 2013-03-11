package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.ArrayList;
import com.bricksimple.rdg.xbrlUpload.*;


public class DefExtract {
	private ArrayList<DefinitionLink> definitionLinks = new ArrayList<DefinitionLink>();
	//private ArrayList<DefinitionLink> parentheticalLinks = new ArrayList<DefinitionLink>();
	
	
	public void AddDefinitionLink(DefinitionLink definitionLink) {
		definitionLinks.add(definitionLink);
	}
	
	public ArrayList<DefinitionLink> GetDefinitionLinks() {
		return(definitionLinks);
	}
	
	//public ArrayList<DefinitionLink> GetParentheticalLinks() {
	//	return(parentheticalLinks);
	//}
	
    public void DoDefExtract(XbrlFiles xbrlFile) {
    	int iIndex = -1;
    	
    	DoDefinitionExtraction(xbrlFile.GetRootNode());
    	DoResolveLocals(xbrlFile.GetRootNode(), iIndex);
    	//MoveParentheticals();
    }
    	//XbrlNode  node = xbrlFile.GetRootNode()
    private void DoDefinitionExtraction(XbrlNode node) {
    	ExtractDefinitionLink(node);
        ExtractDefinitionArcs(node);
    	for(XbrlNode child: node.GetChildren()) {
    		DoDefinitionExtraction(child);
    	}
    	if(node.GetSibling() != null)
    		DoDefinitionExtraction(node.GetSibling());
    }

    
    public void WriteExtractedInfo(PrintWriter out) {
    	
    	out.println("DEF");
    	for(DefinitionLink definitionLink: definitionLinks) {
    		out.println("DefinitionLink: " + definitionLink.GetDefRole());
    		ArrayList<DefinitionArc> definitionArcList = definitionLink.GetDefinitionArcs();
    		for(DefinitionArc definitionArc: definitionArcList) {
    			out.println("    From: " + definitionArc.GetUriFrom());
    			out.println("    To:   " + definitionArc.GetUriTo());
    			out.println("");
    		}
    		out.println("");
    	}
     	out.println("");
    	out.println("");
    	out.println("END OF DEF FILE");
    	out.println("");
    	out.println("");
   }
    
    private String DEFINITION_LOC = "loc";
    private String DEFINITION_LABEL = "label";
    private String DEFINITION_URI = "href";
    
    private int DoResolveLocals(XbrlNode node, int iIndex) {
    	String    testTag = node.GetTag().toLowerCase();
    	String    workStr = "";
        String    label = "";
        String    uri  = "";
    	UTILITIES utilities = new UTILITIES();
       
    	if(utilities.TagMatch(testTag, DEFINITION_LINK)) 
   	        iIndex++;
    	else {
    		if(utilities.TagMatch(testTag, DEFINITION_LOC)) {
        		for(NodeAttribute attr: node.GetAttributes()) {
        			workStr = attr.GetName().toLowerCase();
        			if(utilities.AttMatch(workStr, DEFINITION_LABEL))
        				label = attr.GetValue();
        			else {
        				if(utilities.AttMatch(workStr, DEFINITION_URI))
        					uri = attr.GetValue();
        			}
         		}
   			    definitionLinks.get(iIndex).UpdateDefinition(label, uri);
     		}
    	}
    	for(XbrlNode child: node.GetChildren()) {
    		iIndex = DoResolveLocals(child, iIndex);
    	}
    	if(node.GetSibling() != null)
    		iIndex = DoResolveLocals(node.GetSibling(), iIndex);
    	return(iIndex);
    }
    
    private String DEFINITION_LINK = "definitionlink";
    private String DEF_ROLE = "role";
    
    private void ExtractDefinitionLink(XbrlNode node) {
    	String testTag = node.GetTag().toLowerCase();
    	String workStr = "";
    	DefinitionLink newLink;
    	UTILITIES utilities = new UTILITIES();
    	
    	if(utilities.TagMatch(testTag, DEFINITION_LINK)) {
    		newLink = new DefinitionLink();
    		for(NodeAttribute attr: node.GetAttributes()) {
    			workStr = attr.GetName().toLowerCase();
    			if(utilities.AttMatch(workStr, DEF_ROLE))
    				newLink.SetDefRole(attr.GetValue());
     		}
    		definitionLinks.add(newLink);
    	}
    }
    
    private String DEFINITION_ARC = "definitionarc";
    private String ARC_TO = "to";
    private String ARC_FROM = "from";
    
    private void ExtractDefinitionArcs(XbrlNode node) {
    	String testTag = node.GetTag().toLowerCase();
    	String workStr = "";
    	DefinitionArc definitionArc;
    	UTILITIES utilities = new UTILITIES();
    	
    	if(utilities.TagMatch(testTag, DEFINITION_ARC)) {
    		definitionArc = new DefinitionArc();
    		for(NodeAttribute attr: node.GetAttributes()) {
    			workStr = attr.GetName().toLowerCase();
    			if(utilities.AttMatch(workStr, ARC_TO))
    				definitionArc.SetLocalTo(attr.GetValue());
    			else {
    				if(utilities.AttMatch(workStr, ARC_FROM))
    					definitionArc.SetLocalFrom(attr.GetValue());
    			}
     		}
    		int i = definitionLinks.size() -1;
    		definitionLinks.get(i).AddDefinitionArc(definitionArc);
    	}
    }
}
