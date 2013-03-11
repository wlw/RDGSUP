package com.bricksimple.rdg.FieldId;

import java.io.*;
import java.util.ArrayList;

public class MyFileAccess {
	
	public ArrayList<String> ReadSectionIntoMem(BufferedReader br, int CurrentPos,
			                                    int StartPos, int EndPos, boolean bToTable) {
		ArrayList<String> rtnArray = new ArrayList<String>();
		String            strLine = null;
		boolean           bAddedTableLine = false;
		CONSTANTS         constants = new CONSTANTS();
		
		try {
		    //first skip to our first line
		    while(CurrentPos < StartPos) {
			    strLine = br.readLine();
			    CurrentPos++;
		    }
		    if(strLine == null) {  // we were at position so read the first
		    	strLine  = br.readLine();
		    	CurrentPos++;
		    	if(((bToTable == false) && (strLine.length() > 0)) || ((bToTable == true) && (strLine.contains("<table>")))) {
		    		bAddedTableLine = true;
		    		rtnArray.add(strLine);
		    	}
		    }
		    // note that search points to line after tag which is not the beginning of the table
		    if(bToTable) {
		    	while(constants.CheckForBeginTable(strLine) == -1) {
		    		bAddedTableLine = false;
		    		strLine = br.readLine();
		    		CurrentPos++;
		    	}
		    	if(bAddedTableLine == false)
	    		    rtnArray.add(strLine);
		    }
		    // now read section into array
		    while(CurrentPos <= EndPos){
		    	strLine = br.readLine();
		    	rtnArray.add(strLine);
		    	CurrentPos++;
		    }
		}
		catch (Exception e) {
			//System.out.println("ERROR reading Extract file: " + e.getMessage());
		}
		return(rtnArray);
	}

	public TemplateHdr ReadHeaderIntoMem(BufferedReader br, int CurrentLineNumber, int iIdLine, int beginLineNum) {
		TemplateHdr templateHdr = new TemplateHdr();
		String      strLine = null;
		
		if(iIdLine > beginLineNum) {
			templateHdr.currentLine= CurrentLineNumber;
			templateHdr.IdLine = iIdLine;
		}
		else {		
			try {
			    //first skip to our first line
			    while(CurrentLineNumber < iIdLine) {
				    strLine = br.readLine();
				    CurrentLineNumber++;
			    }
			    if(strLine == null) {  // we were at position so read the first
			    	if(CurrentLineNumber == beginLineNum)
			    		strLine = "";
			    	else {
			    	    strLine  = br.readLine();
			    	    CurrentLineNumber++;
			    	}
			    	templateHdr.HeaderLines.add(strLine);
			    }
			    // note that search points to line after tag which is not the beginning of the table
			    // now read section into array
			    while(CurrentLineNumber < beginLineNum){
			    	strLine = br.readLine();
			    	templateHdr.HeaderLines.add(strLine);
			    	CurrentLineNumber++;
			    }
			}
			catch (Exception e) {
				//System.out.println("ERROR reading Extract file: " + e.getMessage());
			}
			templateHdr.currentLine = CurrentLineNumber;
		}
		return(templateHdr);
	}
}
