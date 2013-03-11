package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;
import com.bricksimple.rdg.xbrlUpload.CONSTANTS;


public class PreGroup {
    private String             Role = "";
    private ArrayList<PreLink> PreLinks = new ArrayList<PreLink>();
    private String  Concept = "";
    
    public void SetRole(String iValue) {
    	Role = iValue;
    }
    
    public String GetRole() {
    	return(Role);
    }
    
    public void AddPreLink(PreLink iValue) {
        PreLinks.add(iValue);
    }
    
    public ArrayList<PreLink> GetLinks() {
    	return(PreLinks);
    }
    
    public void SetConcept(String iValue) {
    	Concept = iValue;
    }
    
    public String GetConcept() {
    	return(Concept);
    }
    
 }
