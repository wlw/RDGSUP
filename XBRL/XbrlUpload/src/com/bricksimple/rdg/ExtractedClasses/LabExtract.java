package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.sql.Connection;
import com.bricksimple.rdg.xbrlUpload.*;
import com.bricksimple.rdg.sqlaccess.*;


public class LabExtract {
    private ArrayList<LabelLink> labelLinks = new ArrayList<LabelLink>();
    
    public ArrayList<LabelLink> GetLabelArcs() {
    	return(labelLinks);
    }
    
    public void AddDefinitionArc(LabelLink iValue) {
    	labelLinks.add(iValue);
    	
    }
    
    public void UpdateDefinition(String LocalValue, String UriValue) {
    	int     i = 0;
    	
    	for(i = 0; i < labelLinks.size(); i++) {
    		if(labelLinks.get(i).GetLocalTo().equals(LocalValue)) {
    			labelLinks.get(i).SetUriTo(UriValue);
    		}
    		if(labelLinks.get(i).GetLocalFrom().equals(LocalValue)) {
    			labelLinks.get(i).SetUriFrom(UriValue);
    		}
    	}
    }
   
    public void ResolveXmlRoles(Connection con) {
    	
    	MySqlAccess mySql = new MySqlAccess();
    	String      xmlRole = "";
    	//int i = 0;
    	
    	for(LabelLink labelLink: labelLinks) {
    		xmlRole = mySql.FindXmlXrefRole(con, labelLink.GetXbrlRole());
    		labelLink.SetXmlRole(xmlRole);
    		//i++;
    	}
    }
    
    public void ResolveDocumentation(XsdExtract xsdExtract, String companyId) {
    
    	for(LabelLink thisLink: labelLinks) {
    		if(thisLink.GetLocalFrom().startsWith(companyId + "_")) {
    		//if(thisLink.GetXbrlRole().equals("documentation")) {
    			for(XsdCustomElement thisCustom: xsdExtract.GetXsdCustomElements()) {
    				if(thisCustom.GetId().equals(thisLink.GetLocalFrom())) {
    					thisCustom.SetDocumentation(thisLink.GetData());
    					break;
    				}
    			}
    		}
    	}
    }
    
    public void WriteExtractedInfo(PrintWriter out) {
    	
    	out.println("LAB");
    	for(LabelLink labelLink: labelLinks) {
     		out.println("    From: " + labelLink.GetUriFrom());
    		out.println("    To:   " + labelLink.GetUriTo());
    		out.println("    Data: " + labelLink.GetData());
    		out.println("    Xbrl Role: " + labelLink.GetXbrlRole() + " To Xml Role: " + labelLink.GetXmlRole());
    		out.println("");
    	}
     	out.println("");
    	out.println("");
    	out.println("END OF LAB FILE");
    	out.println("");
    	out.println("");
   }
    

    public void DoLabExtract(XbrlFiles xbrlFile) {
    	
    	DoArcExtraction(xbrlFile.GetRootNode());
    	DoResolveLocals(xbrlFile.GetRootNode());
    }
    
    private void DoArcExtraction(XbrlNode node) {
    	
        ExtractLinkArcs(node);
    	for(XbrlNode child: node.GetChildren()) {
    		DoArcExtraction(child);
    	}
    	if(node.GetSibling() != null)
    		DoArcExtraction(node.GetSibling());
    }
    
    private String LINK_ARC = "labelarc";
    private String ARC_TO = "to";
    private String ARC_FROM = "from";

    private void ExtractLinkArcs(XbrlNode node) {
    	String    testTag = node.GetTag().toLowerCase();
    	String    workStr = "";
    	LabelLink labelLink;
    	UTILITIES utilities = new UTILITIES();

    	
    	if(utilities.TagMatch(testTag, LINK_ARC)) {
    		labelLink = new LabelLink();
    		for(NodeAttribute attr: node.GetAttributes()) {
    			workStr = attr.GetName().toLowerCase();
    			if(utilities.AttMatch(workStr, ARC_TO))
    				labelLink.SetLocalTo(attr.GetValue());
    			else {
    				if(utilities.AttMatch(workStr, ARC_FROM))
    					labelLink.SetLocalFrom(attr.GetValue());
    			}
     		}
    		labelLinks.add(labelLink);
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
    private String TAG_LABEL = "label";
    
    private void ResolveLinkArcs(XbrlNode node) { 	
    	String    testTag = node.GetTag().toLowerCase();
    	UTILITIES utilities = new UTILITIES();
    	
    	if(utilities.TagMatch(testTag, TAG_LOC)) {
    		DoLocUpdate(node);
    	}
    	else {
        	if(utilities.TagMatch(testTag,TAG_LABEL)) {
        		DoLabelUpdate(node);
        	}   		
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
		for(int i = 0; i < labelLinks.size(); i++) {
			if(labelLinks.get(i).GetLocalFrom().equals(local)) {
				labelLinks.get(i).SetUriFrom(uri);
			}
		}
    }

    private String LABEL_LABEL = "label";
    private String LABEL_URI = "role";
    
    private void DoLabelUpdate(XbrlNode node) {    
        String     workStr;
        String     local = "";
        String     uri = "";
        UTILITIES  utilities = new UTILITIES();
        String     data = "";
        String     xbrlLabel = "";
        
        data = node.GetData();
        for(NodeAttribute attr: node.GetAttributes()) {
    		workStr = attr.GetName().toLowerCase();
    		if(utilities.AttMatch(workStr, LABEL_LABEL))
    			local = attr.GetValue();
    		else {
    			if(utilities.AttMatch(workStr, LABEL_URI)){
    				uri = attr.GetValue();
    				xbrlLabel = ExtractLabel(attr.GetValue());
    			}
    		}
     	}
            int llSize = labelLinks.size();
    		for(int i = 0; i < llSize; i++) {
    		if(labelLinks.get(i).GetLocalTo().equals(local)) {
    			if(labelLinks.get(i).GetUriTo().length() == 0) { //  removed this  && (data.length() > 0)) {  // populate
    			    labelLinks.get(i).SetUriTo(uri);
    			    labelLinks.get(i).SetData(data);
    			    labelLinks.get(i).SetXbrlRole(xbrlLabel);
    			}
    			else {
    				LabelLink newEntry;
    				int newLocation = i +1;  // next location to insert it
   					newEntry = new LabelLink();
    			    newEntry.Clone(labelLinks.get(i));
    				newEntry.SetUriTo(uri);
    				newEntry.SetXbrlRole(xbrlLabel);
    				boolean bCheckNext = true;
   				    while ((bCheckNext == true) && (newLocation < labelLinks.size())) {
   				    	if(labelLinks.get(newLocation).GetLocalTo().equals(local))
   				    		newLocation++;
   				    	else 
   				    		bCheckNext = false;
    			    }
   				    labelLinks.add(newLocation, newEntry);
    				i = newLocation;   // skip past the ones we just added
    			}
    		}
        }
    }
    
    private String ExtractLabel(String orig) {
    	String RtnStr = "";
    	int    i = 0;
    	
    	i = orig.lastIndexOf("/");
    	if(i != -1)
    		RtnStr = orig.substring(i+1);
    	return(RtnStr);
    }
}
