package com.bricksimple.rdg.ExtractedClasses;

import com.bricksimple.rdg.xbrlUpload.PreNode;
import java.util.Comparator;

public class customComparator implements Comparator<PreNode> {
       @Override
	    public int compare(PreNode obj1, PreNode obj2) {
	    	int iRtn = -1;
	    	if(obj1.GetOrder() > obj2.GetOrder())
	    		iRtn = 1;
	    	return(iRtn);
	    }	
}
