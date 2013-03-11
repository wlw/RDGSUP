package com.bricksimple.rdg.FieldId;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;

public class NormalizeExtractFile {
	
	public boolean Normalize(Connection con, String ExtractFile, String NormalizedFile, 
			              int Uid, int TemplateVersion) {
		FileInputStream  fstream;
		DataInputStream  in;
		BufferedReader   br;
		FileOutputStream ostream;
		DataOutputStream on;
		BufferedWriter   wr;
        int              inCurLineNum = 1;
        int              outCurLineNum = 1;  // this is the line we wrote to normalized file
        int              inBeginLineNum = 1;
        int              inEndLineNum;
        int              outBeginLineNum = 1;
        ResultSet        rs;
        String           CurLine; 
        boolean          bLookingForTD;
        boolean          bAddDollarSign = false;
        String           OutLine = "";
        int              iTd;
        int              iEndTd;
        //String           TD = "<td>";
        String           STD = "</td>";
        int              SubUid;
        boolean          bRtn = false;
        MySqlAccess      mySqlAccess = new MySqlAccess();
        CONSTANTS        constants = new CONSTANTS();
        
		try {
			boolean exists = (new File(NormalizedFile)).exists();
			if(exists) {
				exists =  (new File(NormalizedFile)).delete();
				if(!exists) {
					System.out.println("Cannot delete NORMALIZED file");
					return(false);
				}
			}
	        fstream = new FileInputStream(ExtractFile);
	        in = new DataInputStream(fstream);
	        br = new BufferedReader(new InputStreamReader(in));
	        ostream = new FileOutputStream(NormalizedFile);
	        on = new DataOutputStream(ostream);
	        wr = new BufferedWriter(new OutputStreamWriter(on));
	        int lastLine = mySqlAccess.CopyPreambleMarkers(con, Uid, TemplateVersion); // this just copies preamble/TOC markers
	        rs = mySqlAccess.GetTemplates(con, Uid, TemplateVersion, lastLine);
	        while(rs.next()) { // now we skip through templates
	        	bRtn = true;
	        	SubUid = rs.getInt(1);
	            inBeginLineNum = rs.getInt(5);
	            while(inCurLineNum < inBeginLineNum) {  //first we write any unmapped areas including preamble/TOC
	            	CurLine = br.readLine();
	            	wr.write(CurLine);
	            	wr.newLine();
			        wr.flush();
	            	outCurLineNum++;
	            	inCurLineNum++;
	            }
	            outBeginLineNum = outCurLineNum;  // now save this for writing Normal indicators
	            inEndLineNum = rs.getInt(6);   // this is the end of extraction template
	            outBeginLineNum = outCurLineNum;
	            bLookingForTD = true;  // always start looking here
	            OutLine = "";
	            bAddDollarSign = false;
	            while(inCurLineNum < inEndLineNum) { // now we read the 'table'
	            	CurLine = br.readLine();
	            	inCurLineNum++;
	            	//This is a debug location
		            //if(inCurLineNum == 4199)
		            //	System.out.println("BREAK HERE");
	            	while(CurLine.length() > 0) {
	            	    if(bLookingForTD) {
	            		   if((iTd = constants.CheckForBeginCell(CurLine)) == -1) {
	            			   wr.write(CurLine);
	            			   wr.newLine();
	           		           wr.flush();
	            			   outCurLineNum++;
	            			   CurLine = "";
	            		   }
	            		   else {
	            			   if(iTd > 0) {  // write chars previous to <td>
	            				   OutLine = CurLine.substring(0,iTd);
	            				   wr.write(OutLine);
	            				   wr.newLine();
	            			       wr.flush();
		            			   outCurLineNum++;
	            				   OutLine = "";
	            			   }
            				   CurLine = CurLine.substring(iTd + CONSTANTS.BeginCell.length()); // strip chars to  and including <td>
	            			   bLookingForTD = false;
	            		   }
	            	    }
	            	    else  { // must be looking for </td>
	            	    	if((iEndTd = CurLine.indexOf(STD)) == -1) {
	            	    		CurLine = ltrim(rtrim(CurLine));
	            	    	     if(CurLine.length() == 1)  {
	            	    	    	 if(CurLine.equals("$")) {
	            	    	    		 bAddDollarSign = true;
	            	    	    		 CurLine = "";
	            	    	    	 }
	            	    	    	 else {  // its not a space or $
	            	    	    		 OutLine = OutLine + CurLine;
	            	    	    		 CurLine = "";
	            	    	    	 }
	            	    	     }
	            	    	     if(CurLine.length() > 1)  {  // got some text So write it out
	            	    	    	 //OutLine = TD;
	            	    	    	 if(bAddDollarSign == true) {
	            	    	    		 bAddDollarSign = false;
	            	    	    		 OutLine = OutLine + "$";
	            	    	    	 }
	            	    	    	 OutLine = OutLine + CurLine;
	            	    	    	 CurLine = "";  // we still waiting for the </td>
	            	    	    	 //wr.write(OutLine);
	            	    	    	 //wr.newLine();
	            	    	    	 //outCurLineNum++;
	            	    	     }
	            	    	}
	            	    	else {  // found </td>
            	    			bLookingForTD = true;
	            	    		if(iEndTd  > 0) {   // there is some text before the </td>
	            	    			String Temp;
                                    Temp = CurLine.substring(0, iEndTd); // so get the text before </td>
                                    if(Temp.equals("$")) {
                                    	bAddDollarSign = true;
                                    	Temp = "";
                                    }
                                    else {  // not dollar sign check for space
                                    	if(Temp.equals(" ")) {
                                    		Temp = "";
                                    	}
                                    	else  {            // text between tds, so build td pair
                                    		if(bAddDollarSign)
                                    			OutLine = OutLine + "$";
                                    		OutLine = CONSTANTS.BeginCell + OutLine + Temp + STD;
                                    		wr.write(OutLine);
                                    		wr.newLine();
                            		        wr.flush();
                                  		   outCurLineNum++;
                                    		OutLine = "";
                                    	}
                                    }
	            	    		}
	            	    		else { // no text in front of </td>, check if we have some from previous line
	            	    			if(OutLine.length() > 0) { //yup, write it out
	            	    				OutLine = CONSTANTS.BeginCell + OutLine + STD;
	            	    				wr.write(OutLine);
	            	    				wr.newLine();
	            	    		        wr.flush();
	            	    				outCurLineNum++;
	            	    				OutLine = "";
	            	    			}
	            	    		}  // lastly remove the </td>
                                CurLine = CurLine.substring(iEndTd + STD.length()); // and remove </td>
	            	    	}
	            	    }
	            	}
	            } //end WHILE for table
	            // update the templateparse table
	            mySqlAccess.UpdateTemplateParse(con, SubUid, outBeginLineNum, outCurLineNum);
	        }
	        wr.flush();
	        wr.close();
	        ostream.close();
		}
		catch (Exception e) {
			System.out.println("Error Normalizing file: " + e.getMessage());
		}
		return(bRtn);
	}
	
	private static String ltrim(String source) {
		return source.replaceAll("^\\s+", "");
	}
	
	private static String rtrim(String source) {
		return source.replaceAll("\\s+$", "");
	}
}
