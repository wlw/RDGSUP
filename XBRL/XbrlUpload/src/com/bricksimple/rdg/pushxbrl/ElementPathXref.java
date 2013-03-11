package com.bricksimple.rdg.pushxbrl;

public class ElementPathXref {
    private String ElementsPath = "";
    private String ElementsNameSpace = "";
    
    public void SetElementPath(String iValue) {
    	ElementsPath = iValue;
    }
    
    public String GetElementsPath() {
    	return(ElementsPath);
    }

    public void SetElementsNameSpace(String iValue) {
    	ElementsNameSpace = iValue;
    }
    
    public String GetElementsNameSpace() {
    	return(ElementsNameSpace);
    }
}
