package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;

public class DefinitionLink {

    private String DefRole = "";
    private ArrayList<DefinitionArc> definitionArcs  = new ArrayList<DefinitionArc>();
    
    public void SetDefRole(String iValue) {
    	DefRole = iValue;
    }
    
    public String GetDefRole() {
    	return(DefRole);
    }
    
    public ArrayList<DefinitionArc> GetDefinitionArcs() {
    	return(definitionArcs);
    }
    
    public void AddDefinitionArc(DefinitionArc iValue) {
    	definitionArcs.add(iValue);
    	
    }
    
    public void UpdateDefinition(String LocalValue, String UriValue) {
    	int     i = 0;
    	
    	for(i = 0; i < definitionArcs.size(); i++) {
    		if(definitionArcs.get(i).GetLocalTo().equals(LocalValue)) {
    			definitionArcs.get(i).SetUriTo(UriValue);
    		}
    		if(definitionArcs.get(i).GetLocalFrom().equals(LocalValue)) {
    			definitionArcs.get(i).SetUriFrom(UriValue);
    		}
    	}
    }
}
