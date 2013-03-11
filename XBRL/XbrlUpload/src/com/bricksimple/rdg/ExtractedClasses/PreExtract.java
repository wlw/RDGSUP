package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.bricksimple.rdg.xbrlUpload.*;

public class PreExtract {
	   private ArrayList<PreGroup> preGroups = new ArrayList<PreGroup>();
	   private ArrayList<String> equityComponents = new ArrayList<String>();
	   
	    public ArrayList<PreGroup> GetPreArcs() {
	    	return(preGroups);
	    }
	   
	    public void AddEquityComponent(String iValue) {
	    	equityComponents.add(iValue);
	    }
	    
	    public ArrayList<String> GetEquityComponents() {
	    	return(equityComponents);
	    }
	    
	    public void AddDefinitionArc(PreGroup iValue) {
	    	preGroups.add(iValue);
	    	
	    }
	    
	    public ArrayList<PreGroup> GetPreGroups() {
	    	return(preGroups);
	    }
	    
	    public void SetSectionConcepts() {
	    
	    	for(PreGroup curGroup: preGroups) {
    			boolean bFoundConcept = false;
    			int     i = 0;
    			while((i < curGroup.GetLinks().size()) && (bFoundConcept == false)) {
	    		    if(curGroup.GetLinks().get(i).GetUriTo().contains("_StatementTable")) {
	    		    	bFoundConcept = true;
	    		    	int j = curGroup.GetLinks().get(i).GetUriFrom().indexOf("#");
	    		    	if(j != -1) {
	    		    	    curGroup.SetConcept(curGroup.GetLinks().get(i).GetUriFrom().substring(j+1));
	    		    	}
	    		    }
	    			i++;
	    		}
	    	}
	    }
	    
	    public void WriteExtractedInfo(PrintWriter out) {
	    	
	    	out.println("PRE");
	    	for(PreGroup curGroup: preGroups) {
	     		out.println("    PresentationLink: " + curGroup.GetRole());
	     		for(PreLink preLink: curGroup.GetLinks()) {
	     			out.println("       To: " + preLink.GetUriTo());
	     			out.println("       From: " + preLink.GetUriFrom());
	     			out.println("       Order: " + preLink.GetOrder());
	     		}
	    		out.println("");
	    	}
	     	out.println("");
	    	out.println("");
	    	out.println("END OF PRE FILE");
	    	out.println("");
	    	out.println("");
	   }
	    

	    public String WebAddress(XbrlFiles xbrlFile) {
	    	String webAddress = "";
	    	
	    	webAddress = CheckForWebAddr(xbrlFile.GetRootNode());
	    	return(webAddress);
	    }
	    
	    private String ROLE_REF = "roleref";
	    private String ROLE_URI = "roleuri";
	    private String ROLE     = "/role/";
	    
	    private String CheckForWebAddr(XbrlNode node) {
	    	
	    	String    testTag = node.GetTag().toLowerCase();
	    	UTILITIES utilities = new UTILITIES();
	    	String    workStr;
	    	int       iNdx;
	    	String    webAddress = "";
	    	
	    	if(utilities.TagMatch(testTag,  ROLE_REF)) {
	    		for(NodeAttribute attr: node.GetAttributes()) {
	    			workStr = attr.GetName().toLowerCase();
	    			if(utilities.AttMatch(workStr, ROLE_URI)) {
	    				workStr = attr.GetValue();
	    				iNdx = workStr.indexOf(ROLE);
	    				if(iNdx > 0 ) {
	    					webAddress = workStr.substring(0, iNdx);
	    				}
	    			}
	     		}
	    		
	    	}
	    	if(webAddress.length() == 0) {
	    		for(XbrlNode child: node.GetChildren()) {
	    			webAddress = CheckForWebAddr(child);
	    			if(webAddress.length() > 0)
	    				break;
	    		}
	    	}
	    	return(webAddress);
	    }
	    
	    public void DoPreExtract(XbrlFiles xbrlFile) {
	    	
	    	DoLinkExtraction(xbrlFile.GetRootNode());	    	
	    	DoResolveLocals(xbrlFile.GetRootNode());
	    	//FindStockEquity(xbrlFile.GetRootNode());
	    }
	    
	    private String EQUITYCOMPONENTS = "statementequitycomponentsaxis";
	    private String PRESENTATION_LINK = "presentationlink";
	    
	    private void DoLinkExtraction(XbrlNode node) {
	    	String    testTag = node.GetTag().toLowerCase();
	    	UTILITIES utilities = new UTILITIES();

	    	if(utilities.TagMatch(testTag, PRESENTATION_LINK)) {
	    		AddPresentationLink(node);
	    		AddPresentationArc(node);
	    	}
	    	for(XbrlNode child: node.GetChildren()) {
	    		DoLinkExtraction(child);
	    	}
	    	if(node.GetSibling() != null)
	    		DoLinkExtraction(node.GetSibling());
	    }
	    
	    private String PRESENTATION_ROLE = "role";
	    
	    private void AddPresentationLink(XbrlNode node) {
	    	PreGroup  preGroup = new PreGroup();
	    	String    workStr = "";
	    	UTILITIES utilities = new UTILITIES();
	    	
	    	
    		for(NodeAttribute attr: node.GetAttributes()) {
    			workStr = attr.GetName().toLowerCase();
    			if(utilities.AttMatch(workStr, PRESENTATION_ROLE))
    				preGroup.SetRole(attr.GetValue());
     		}
    		preGroups.add(preGroup);
	    }
	    
	    private String LINK_ARC = "presentationarc";
	    private String ARC_TO = "to";
	    private String ARC_FROM = "from";
	    private String ARC_ORDER = "order";
	    private String ARC_ROLE = "preferredlabel";
	    
	    private void AddPresentationArc(XbrlNode node) {
	    	UTILITIES utilities = new UTILITIES();
	    	String    testTag = "";
	    	PreLink   preLink = null;
	    	String    workStr;
	    	int       preGroupNdx = preGroups.size() -1;
	    	
	    	for(XbrlNode child: node.GetChildren()) {
	    		testTag = child.GetTag().toLowerCase();
		    	if(utilities.TagMatch(testTag, LINK_ARC)) {	    
		    		preLink = new PreLink();
		    		for(NodeAttribute attr: child.GetAttributes()) {
		    			workStr = attr.GetName().toLowerCase();
		    			if(utilities.AttMatch(workStr, ARC_TO)) {
		    				preLink.SetLocalTo(attr.GetValue());
		    				//XXX
		    			}
		    			else {
		    				if(utilities.AttMatch(workStr, ARC_FROM))
		    					preLink.SetLocalFrom(attr.GetValue());
		    				else {
		    					if(utilities.AttMatch(workStr, ARC_ORDER)) {
		    						String value = attr.GetValue();
		    						//value = value.replace(".0", "");
		    						preLink.SetOrder(value);
		    					}
		    					else {
		    						if(utilities.AttMatch(workStr, ARC_ROLE)) {
		    							String value = attr.GetValue();
		    							preLink.SetRole(value);
		    						}
		    					}
		    				}		
		    			}
		     		}
		    		preGroups.get(preGroupNdx).AddPreLink(preLink);
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
			// check all entries for match
			for(int i = 0; i < preGroups.size(); i++) {
				for(PreLink curLink: preGroups.get(i).GetLinks()) {
					if(curLink.GetLocalFrom().equals(local)) {
						curLink.SetUriFrom(uri);
					}
					if(curLink.GetLocalTo().equals(local)) {
						curLink.SetUriTo(uri);
					}
					
				}
			}
		}

}
