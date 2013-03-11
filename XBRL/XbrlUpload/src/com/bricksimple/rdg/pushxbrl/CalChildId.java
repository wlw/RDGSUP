package com.bricksimple.rdg.pushxbrl;

import com.bricksimple.rdg.ExtractedClasses.CalParent;
import com.bricksimple.rdg.ExtractedClasses.CalChild;

public class CalChildId {
    private CalParent calParent;
    private CalChild  calChild;
    
    public void SetCalParent(CalParent iValue) {
    	calParent = iValue;
    }
    
    public CalParent GetCalParent() {
        return(calParent);
    }

    public void SetCalChild(CalChild iValue) {
    	calChild = iValue;
    }
    
    public CalChild GetCalChild() {
        return(calChild);
    }
}
