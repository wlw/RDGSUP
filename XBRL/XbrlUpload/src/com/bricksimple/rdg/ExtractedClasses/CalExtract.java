package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.bricksimple.rdg.xbrlUpload.NodeAttribute;
import com.bricksimple.rdg.xbrlUpload.XbrlFiles;
import com.bricksimple.rdg.xbrlUpload.XbrlNode;
import com.bricksimple.rdg.xbrlUpload.*;

public class CalExtract {
	ArrayList<CalParent> calParents = new ArrayList<CalParent>();
	
    //private ArrayList<CalLink> calLinks = new ArrayList<CalLink>();
    
   // public ArrayList<CalLink> GetCalArcs() {
    //	return(calLinks);
    //}
    
    //public void AddDefinitionArc(CalLink iValue) {
    //	calLinks.add(iValue);
    	
    //}
    
   // public void UpdateDefinition(String LocalValue, String UriValue) {
   // 	int     i = 0;
    	
   // 	for(i = 0; i < calLinks.size(); i++) {
   // 		if(calLinks.get(i).GetLocalTo().equals(LocalValue)) {
   // 			calLinks.get(i).SetUriTo(UriValue);
   // 		}
   // 		if(calLinks.get(i).GetLocalFrom().equals(LocalValue)) {
   // 			calLinks.get(i).SetUriFrom(UriValue);
   // 		}
   // 	}
   // }
	
	public ArrayList<CalParent> GetCalParents() {
		return(calParents);
	}
   
    public void WriteExtractedInfo(PrintWriter out) {
    	
    	out.println("CAL");
    	/*
    	for(CalLink calLink: calLinks) {
     		out.println("    From: " + calLink.GetUriFrom());
    		out.println("    To:   " + calLink.GetUriTo());
    		out.println("                    Order: " + calLink.GetOrder() +  ":: Weight: " + calLink.GetWeight());
    		out.println("");
    	}
    	*/
    	for(CalParent thisParent: calParents) {
    		out.println("    Parent:" + thisParent.GetUri());
			out.println("      Children:");
    		for(CalChild thisChild: thisParent.GetChildren()) {
    			out.println("        " + thisChild.GetOrder() + " " + thisChild.GetUri());
    		}
    		out.println("");
    	}
     	out.println("");
    	out.println("");
    	out.println("END OF LAB FILE");
    	out.println("");
    	out.println("");
   }
    

    public void DoCalExtract(XbrlFiles xbrlFile) {
    	
    	DoCalLinkLookUp(xbrlFile.GetRootNode());
    	DoResolveLocals(xbrlFile.GetRootNode());
    }
    
    private String CAL_LINK = "calculationlink";
    
    private void DoCalLinkLookUp(XbrlNode node) {
    	CheckForKey(node);
    	for(XbrlNode child: node.GetChildren())
    		DoCalLinkLookUp(child);
    	if(node.GetSibling() != null)
    		DoCalLinkLookUp(node.GetSibling());
    	
    }
    
    private void CheckForKey(XbrlNode node) {
    	String    testTag = node.GetTag().toLowerCase();
    	UTILITIES utilities = new UTILITIES();
    	if(utilities.TagMatch(testTag, CAL_LINK))
    		DoArcExtraction(node);
    }
    private void DoArcExtraction(XbrlNode node) {
    	
        ExtractLinkArcs(node);
    	for(XbrlNode child: node.GetChildren()) {
    		DoArcExtraction(child);
    	}
    	if(node.GetSibling() != null)
    		DoArcExtraction(node.GetSibling());
    }
    
    private String LINK_ARC = "calculationarc";
    private String ARC_TO = "to";
    private String ARC_FROM = "from";
    private String ARC_ORDER = "order";
    private String ARC_WEIGHT = "weight";

    private void ExtractLinkArcs(XbrlNode node) {
    	String    testTag = node.GetTag().toLowerCase();
    	String    workStr = "";
    	CalChild  calChild;
    	UTILITIES utilities = new UTILITIES();
    	String    posParent  = "";
    	CalParent calParent = null;
    	
    	if(utilities.TagMatch(testTag, LINK_ARC)) {
    		calChild = new CalChild();
    		for(NodeAttribute attr: node.GetAttributes()) {
    			workStr = attr.GetName().toLowerCase();
    			if(utilities.AttMatch(workStr, ARC_TO))
    				calChild.SetLocal(attr.GetValue());
    			else {
    				if(utilities.AttMatch(workStr, ARC_FROM)) {
    					posParent = attr.GetValue();
    				}
    				else {
    					if(utilities.AttMatch(workStr, ARC_ORDER))
    						calChild.SetOrder(attr.GetValue());
    					else {
    						if(utilities.AttMatch(workStr, ARC_WEIGHT))
    							calChild.SetWeight(attr.GetValue());
    					}
    						
    				}
    			}
     		}
			boolean bFound = false;
   		    if(calParents.size() > 0) {
    			int i = 0;
    			while((i < calParents.size()) && (bFound == false)) {
    				if(calParents.get(i).GetLocal().equals(posParent)) {
    					bFound = true;
    					calParents.get(i).InsertChild(calChild);
    				}
    				else
    					i++;
    			}
    		}
    		// now check if parent exists 
    		if(bFound == false) {
    			calParent = new CalParent();
    			calParent.SetLocal(posParent);
    			calParents.add(calParent);
        		calParent.InsertChild(calChild);
     		}
    	}
    }
   
    private void DoResolveLocals(XbrlNode node) {
    	
        ResolveLinkArcs(node);
    	for(XbrlNode child: node.GetChildren()) {
    		DoResolveLocals(child);
    	}
    	if(node.GetSibling() != null)
    		DoResolveLocals(node.GetSibling());
   }
    
    private String TAG_LOC = "loc";
    
    private void ResolveLinkArcs(XbrlNode node) { 	
    	String    testTag = node.GetTag().toLowerCase();
    	UTILITIES utilities = new UTILITIES();
    	
    	if(utilities.TagMatch(testTag, TAG_LOC)) {
    		DoLocUpdate(node);
    	}
    }

    private String LOC_LABEL = "label";
    private String LOC_URI = "href";
    
    private void DoLocUpdate(XbrlNode node) {
        String    workStr;
        String    local = "";
        String    uri = "";
        UTILITIES utilities = new UTILITIES();
        
		for(NodeAttribute attr: node.GetAttributes()) {
			workStr = attr.GetName().toLowerCase();
			if(utilities.AttMatch(workStr, LOC_LABEL))
				local = attr.GetValue();
			else {
				if(utilities.AttMatch(workStr, LOC_URI))
					uri = attr.GetValue();
			}
 		}
		for(int i = 0; i < calParents.size(); i++) {
			if(calParents.get(i).GetLocal().equals(local))
				calParents.get(i).SetUri(uri);
			else
				calParents.get(i).SetUri(calParents.get(i).GetLocal());
			for(CalChild calChild: calParents.get(i).GetChildren()) {
				if(calChild.GetLocal().equals(local))
					calChild.SetUri(uri);
				else
					calChild.SetUri(calChild.GetLocal());
			}
		}
		/*
		for(int i = 0; i < calLinks.size(); i++) {
			if(calLinks.get(i).GetLocalFrom().equals(local)) {
				calLinks.get(i).SetUriFrom(uri);
			}
			if(calLinks.get(i).GetLocalTo().equals(local)) {
				calLinks.get(i).SetUriTo(uri);
			}
		}
		*/
    }
}
