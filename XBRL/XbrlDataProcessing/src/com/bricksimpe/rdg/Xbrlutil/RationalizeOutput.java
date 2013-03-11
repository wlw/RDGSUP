package com.bricksimpe.rdg.Xbrlutil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.util.UUID;
import java.util.ArrayList;

public class RationalizeOutput {

	private Functor []         eventFunctions = new Functor[13];
    private ArrayList          eventChars = new ArrayList(); //{'<', 't', 'd', '>', '/'};
    private String             writeBuffer = "";
    private String             inProgressBuffer = "";
	private boolean            bFoundTd = false;
	private int                curState = 0;
	private int[][]            eventStateTbl = {{ 1,11,11,11,12}, {11, 2,12,12,12}, {11,12, 3,12,12}, {11,12,12, 4,12},
			                                    { 5,12,12,12,12}, { 7, 6,12,12, 8}, {11, 9,12,12,12}, {11,12,10,12,12},
			                                    {11,12,12,12,12}};
    

	public void Execute(String outputFileName, Connection con) {
	    String           intermediateFile = "" + UUID.randomUUID().toString();
		FileInputStream  fstream;
		FileOutputStream ostream;
		DataInputStream  in;
		DataOutputStream out;
		BufferedReader   br;
		BufferedWriter   wr;
	    ErrorCls         errorCls = new ErrorCls();
	    String           strLine = "";
		char[]           strArray;
		int              i;
		int              charEvent;
	    boolean          bDisplayFlag = false;
	    MySqlAccess      mySqlAccess = new MySqlAccess();
	    int              iLineNum = 0;
	    
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("RationalizeOutput");
		errorCls.setItemVersion(0);
		errorCls.setBExit(false);

		try {
            fstream = new FileInputStream(outputFileName);
            ostream = new FileOutputStream(intermediateFile);
            in = new DataInputStream(fstream);
            out = new DataOutputStream(ostream);
            br = new BufferedReader(new InputStreamReader(in));
            wr = new BufferedWriter(new OutputStreamWriter(out));
            while((strLine = br.readLine()) != null) {
            	iLineNum++;
          	    strArray = strLine.toCharArray();
          	    for(i = 0; i < strArray.length; i++) {
         	    	charEvent = GetCharacterEvent(strArray[i]);
         	    	if(bDisplayFlag == true)
         	    	    System.out.println("Calling eventFunction: " + charEvent +
         	    	    		           " with character: '" + strArray[i] + "'");
         	    	if(charEvent < 100)
        	    		curState = eventFunctions[charEvent].execute(strArray[i]);
         	    	else
        	    		curState = eventFunctions[0].execute(strArray[i]);
         	   }
       	       wr.write(writeBuffer + inProgressBuffer);
       	       inProgressBuffer = "";
        	   writeBuffer = "";
               wr.newLine();
           }
           wr.flush();
           wr.close();
           out.close();
           ostream.close();
           br.close();
           in.close();
           fstream.close();
           File file = new File(outputFileName);
           file.delete();
           File filefrom = new File(intermediateFile);
           file = new File(outputFileName);
           filefrom.renameTo(file);
			
		}
	    catch (Exception e) {
    	    errorCls.setErrorText("Unable to access files: " + e.getMessage());
    	    mySqlAccess.WriteAppError(con, errorCls);
 	    }
	}
	
    private int GetCharacterEvent(char textChar) {
		int iRtn = 256;   // none event char
        int ThisCharIndex = 0;
        
		char lowerTextChar = Character.toLowerCase(textChar);
	    if(eventChars.contains(lowerTextChar)) {
			ThisCharIndex = eventChars.indexOf(lowerTextChar);
			iRtn = eventStateTbl[curState][ThisCharIndex];
	    }
        return(iRtn);
	}

	//private RationalFunction getEventFunction(int index) {
	//	return eventFunctions[index];
	//}

	abstract class Functor {
		protected abstract int execute(char inChar);
	}
	
	class Event0Func extends Functor {
		protected int execute(char inChar) {
			//WLW not required in formats
			if((bFoundTd) && (inProgressBuffer.length() > 0)) {
				String Temp = inProgressBuffer + inChar;
				// WAS = now checks for then end of the ROW
				//if(Temp.equals("tr") == false) {
				// the two filings that exploit the td conumdrum
				// arr_10q-093011v2 and fmbi_10q-2-093100
				if(Temp.equals("</tr") == true) {   // if the end and open td
				    writeBuffer += "</td>";
					bFoundTd = false;
				}
		    }
		    writeBuffer += inProgressBuffer + inChar;
		    inProgressBuffer = "";
		    bFoundTd = false;
		    return(0);			
		}
	}
	
	class Event1Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(1);
		}
	}
	
	class Event2Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(2);
		}
	}
	
	class Event3Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(3);
		}
	}
	
	class Event4Func extends Functor {
		protected int execute(char inChar) {
			writeBuffer += inProgressBuffer + inChar;
			inProgressBuffer = "";
			bFoundTd = true;
			return(4);
		}
	}
	
	class Event5Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(5);
		}
	}
	
	class Event6Func extends Functor {
		protected int execute(char inChar) {
			writeBuffer += "</td>";
			inProgressBuffer += inChar;
			return(2);
		}
	}
	
	class Event7Func extends Functor {
		protected int execute(char inChar) {
			writeBuffer += "</td>" + inProgressBuffer;
			inProgressBuffer = Character.toString(inChar);
			return(1);
		}
	}
	
	class Event8Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(6);
		}
	}
	
	class Event9Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(7);
		}
	}
	
	class Event10Func extends Functor {
		protected int execute(char inChar) {
			inProgressBuffer += inChar;
			return(8);
		}
	}
	
	class Event11Func extends Functor {
		protected int execute(char inChar) {
			writeBuffer += inProgressBuffer;
			inProgressBuffer = Character.toString(inChar);		
			return(1);
		}
	}
	
	class Event12Func extends Functor {
		protected int execute(char inChar) {
			writeBuffer += inProgressBuffer + inChar;
			inProgressBuffer = "";
			return(0);
		}
	}
	

	public RationalizeOutput() {
		eventChars.add('<');
		eventChars.add('t');
		eventChars.add('d');
		eventChars.add('>');
		eventChars.add('/');
		
	    // NON EVENT CHARACHER - push all outstanding chars and this one to write buffer
	    eventFunctions[0] = new Event0Func();
	    eventFunctions[1] = new Event1Func();
	    eventFunctions[2] = new Event2Func();
	    eventFunctions[3] = new Event3Func();
	    eventFunctions[4] = new Event4Func();
	    eventFunctions[5] = new Event5Func();
	    eventFunctions[6] = new Event6Func();
	    eventFunctions[7] = new Event7Func();
	    eventFunctions[8] = new Event8Func();
	    eventFunctions[9] = new Event9Func();
	    eventFunctions[10] = new Event10Func();
	    eventFunctions[11] = new Event11Func();
	    eventFunctions[12] = new Event12Func();
	}
}
