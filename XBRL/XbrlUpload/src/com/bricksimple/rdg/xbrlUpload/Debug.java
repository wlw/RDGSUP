package com.bricksimple.rdg.xbrlUpload;

import java.io.FileWriter;
import java.io.PrintWriter;

public class Debug {
    private FileWriter outFile;
    private PrintWriter out;
    private boolean  Enabled = false;
    
    public void InitDebug(boolean bEnable) {
    	
    	Enabled = bEnable;
    	if(bEnable) {
    		try {
    		outFile = new FileWriter("Debug.out");
    		out = new PrintWriter(outFile);
    		}
    		catch (Exception e) {
    			Enabled = false;
    		}
    	}
    }
    
    public void WriteDebug (String outStr) {
    	if(Enabled) {
    	    out.println(outStr);
    	    out.flush();
    	}
    }
    
    public boolean IsEnabled() {
    	return(Enabled);
    }

}
