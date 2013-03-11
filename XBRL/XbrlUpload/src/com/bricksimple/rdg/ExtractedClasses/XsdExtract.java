package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.ArrayList;
import com.bricksimple.rdg.xbrlUpload.*;

public class XsdExtract {
	private String                      xsdFileName  = "";
    private ArrayList<XsdRoleType>      RoleTypes = new ArrayList<XsdRoleType>();
    private ArrayList<XsdCustomElement> CustomElements = new ArrayList<XsdCustomElement>();   
    private ArrayList<String>           NoteRoles = new ArrayList<String>();
    
    //private ArrayList<XsdRoleType> parentheticals =  new ArrayList<XsdRoleType>();
    
    public ArrayList<XsdRoleType> GetXsdRoleTypes() {
    	return(RoleTypes);
    }
    
    public ArrayList<XsdCustomElement> GetXsdCustomElements() {
    	return(CustomElements);
    }
    
    public ArrayList<String> GetNoteRoles() {
    	return(NoteRoles);
    }
    
    public void AddNoteRole(String role) {
    	NoteRoles.add(role);
    }
    
    //These are the TAGS we are looking for
    
    private String ROLE_KEY = "roletype";            //"link:roletype";
    private String ELEMENT_KEY = "element";     //"xsd:element";
    
    // key strings for role type
    private String ATT_ID = "id";
    private String ATT_ROLEURI = "roleuri";
    
    // key string for custom elements
    private String ELEMENT_ID                = "id";
    private String ELEMENT_NAME              = "name";
    private String ELEMENT_ABSTRACT          = "abstract";
    private String ELEMENT_NILLABLE          = "nillable";
    private String ELEMENT_SUBSTITUTIONGROUP = "substitutiongroup";
    private String ELEMENT_TYPE              = "type";
    private String ELEMENT_PERIOD            = "xbrli:period";
    private String ELEMENT_PERIOD_EXT        = "xbrli:periodtype";
    private String ELEMENT_BALANCE           = "xbrli:balance";
    //data associated with above
    private String TAG_LINK_DEFINITION = "definition"; //"link:definition";
        
    private void ExtractData(XbrlNode node, String companyId) {
    	
    	String  testTag = node.GetTag().toLowerCase();
    	String  workStr = "";
    	boolean bAdd = false;
    	
    	if(testTag.contains(ROLE_KEY)) {
    		XsdRoleType xsdRoleType = new XsdRoleType();
    		for(NodeAttribute attr: node.GetAttributes()) {
    			workStr = attr.GetName().toLowerCase();
    			if(workStr.equals(ATT_ID))
    				xsdRoleType.SetId(attr.GetValue());
    			else {
    				if(workStr.equals(ATT_ROLEURI))
    					xsdRoleType.SetRoleUri(attr.GetValue());
    			}   				
    		}
    		RoleTypes.add(xsdRoleType);
    	}    	
    	else {
    		if(testTag.contains(TAG_LINK_DEFINITION)) {  // add data to last entry
    			int i = RoleTypes.size() -1;
    			RoleTypes.get(i).SetData(node.GetData());
    		}
    		else {
    		    if(testTag.contains(ELEMENT_KEY)) {
    		    	XsdCustomElement xsdCustomElement = new XsdCustomElement();
    	    		for(NodeAttribute attr: node.GetAttributes()) {
    	    			workStr = attr.GetName().toLowerCase();
    	    			if(workStr.equals(ELEMENT_ID)) {
    	    				xsdCustomElement.SetId(attr.GetValue());
    	    				if(attr.GetValue().startsWith(companyId + "_"))
    	    					bAdd = true;
    	    			}
    	    			else {
    	    				if(workStr.equals(ELEMENT_NAME))
    	    					xsdCustomElement.SetName(attr.GetValue());
    	    				else {
    	    					if(workStr.equals(ELEMENT_ABSTRACT))
        	    					xsdCustomElement.SetAbstract(attr.GetValue());
    	    					else {
    	    						if(workStr.equals(ELEMENT_NILLABLE))
    	    	    					xsdCustomElement.SetNillable(attr.GetValue());
    	    						else {
    	    							if(workStr.equals(ELEMENT_SUBSTITUTIONGROUP))
    	        	    					xsdCustomElement.SetSubstitutionGroup(attr.GetValue());
    	    							else {
    	    								if(workStr.equals(ELEMENT_TYPE))
    	    	    	    					xsdCustomElement.SetType(attr.GetValue());
    	    								else {
    	    									if(workStr.equals(ELEMENT_PERIOD))
    	    		    	    					xsdCustomElement.SetPeriod(attr.GetValue());
    	    									else {
    	    										if(workStr.equals(ELEMENT_PERIOD_EXT))
        	    		    	    					xsdCustomElement.SetPeriod(attr.GetValue());
    	    									}
    	    								}
    	    							}
    	    						}
    	    					}
    	    				}
   	    			    }   				
    	    		}
    	    		if(bAdd)
    	    		    CustomElements.add(xsdCustomElement);
    		    }
    		}
    	}
    }
    	
    public void DoExtract(XbrlNode node, String companyId) {
    	DoXsdExtract(node, companyId);
    	FindNoteRoles();
    	//MoveParentheticals();
    }
    
    public void SetXsdFileName(String iValue) {
    	xsdFileName = iValue;
    }
    
    public String GetXsdFileName() {
    	return(xsdFileName);
    }
    
    private void DoXsdExtract(XbrlNode node, String companyId) {
    	
        ExtractData(node, companyId);
    	for(XbrlNode child: node.GetChildren()) {
    		DoXsdExtract(child, companyId);
    	}
    	if(node.GetSibling() != null)
    		DoXsdExtract(node.GetSibling(), companyId);
    }

    public void WriteExtractedInfo(PrintWriter out) {
    	
    	out.println("XSD");
    	out.println("  RoleTypes:");
    	for(XsdRoleType xsdRoleType: RoleTypes) {
    		out.println("    RoleURI: " + xsdRoleType.GetRoleUri());
    		out.println("    ID:      " + xsdRoleType.GetId());
    		out.println("    Data:    " + xsdRoleType.GetData());
    		out.println("");    		
    	}
    	out.println("");
    	out.println("  Custom Elements:");
    	for(XsdCustomElement xsdCustomElement: CustomElements) {
    		out.println("    ID :  " + xsdCustomElement.GetId());
    		out.println("    Name: " + xsdCustomElement.GetName());
    		out.println("");    		
    	}
    	out.println("");
    	out.println("");
    	out.println("END OF XSD FILE");
    	out.println("");
    	out.println("");
    }
    
    private void FindNoteRoles() {
    	String temp;
    	int    i;
    	String role;
    	
    	for( XsdRoleType thisRole: RoleTypes) {
    		temp = thisRole.GetData();
    		i = temp.indexOf("-");
    		temp = temp.substring(i+1).trim();
    		if(temp.indexOf("Disclosure") == 0) {
    			role = thisRole.GetRoleUri();
    			NoteRoles.add(role);
    		}
    		
    	}
    }
}
