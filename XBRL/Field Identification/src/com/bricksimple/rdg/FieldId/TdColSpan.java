package com.bricksimple.rdg.FieldId;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.ArrayList;

public class TdColSpan {
    public ArrayList<ArrayList<Integer>> TblRow = new ArrayList<ArrayList<Integer>>();
    public ArrayList<ArrayList<Integer>> TblCol = new ArrayList<ArrayList<Integer>>();
  	private EventStateFunctions[] eventFunctions = new EventStateFunctions[14];                //    was 9
  	
  	/*****************************************************************************/
  	/* there are three event state tables in use                                 */
  	/*   1. event/state table that looks for '<tr'                               */
  	/*   2. event/state table that looks for '<td' or '</tr>'                    */
  	/*   3. event/state table that looks for 'colspan', 'rowspan' and '</td>'    */
  	/*****************************************************************************/
  	private int       StateTable = 0;
  	private ArrayList TrEventChars = new ArrayList();
  	private ArrayList TdEventChars = new ArrayList();
  	private ArrayList ColEventChars = new ArrayList();
	private int       CurState = 0;
	
  	private int[][] TrStateArray = {{ 1, 0, 4},
  			                        { 0, 1, 2},
  			                        { 0, 2, 3}};
  	
  	private int [][] TdStateArray = {{ 1, 0, 4, 4, 4, 4},
  			                         { 0, 1, 2, 1, 2, 2},
  			                         { 0, 2, 3, 2, 2, 2},
  			                         { 0, 5, 2, 2, 2, 2},
  			                         { 0, 2, 2, 2, 1, 2},
  			                         { 0, 2, 2, 2, 2, 6}};
  	
  	private int[][] ColStateArray = {{ 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4},
  			                         { 0, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
  			                         { 0, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2},
  			                         { 0, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2},
  			                         { 0, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2},
  			                         { 0, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2},
  			                         { 0, 2, 2, 2, 2, 2, 7, 2, 2, 2, 2, 2, 9, 2, 2, 2, 2, 2},
  			                         { 8, 8, 8, 8, 8, 8, 8, 0, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8},
  			                         { 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2},
  			                         {10,10,10,10,10,10,10,10,10,10,10,10,10, 0,10,10,10,10},
  			                         { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2},
  			                         { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2},
  			                         {13, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2},
  			                         { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,11,12}};
  	
  	/* ENABLE THIS COMMMENT *****************************************************************************
   	private ArrayList EventChars = new ArrayList(); 
	//                            STATE   0  1  2  3  4  5  6  7  8  9 10 11 12 13 14
 	private int[][]   EventStateArray = {{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},   // <
                                         {0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},   // t
                                         {0, 0, 1, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0},   // r	
                                         {0, 0, 0, 0, 0, 0, 0, 6, 7, 0, 0, 0, 0, 0, 0},   // >		
                                         {0, 0, 2, 0, 0, 4, 1, 0, 0, 0, 0, 0, 0, 0, 0},   // d		
                                         {0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},   // /		
                                         {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5},   // c		
                                         {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},   // o		
                                         {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},   // l		
                                         {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},   // s		
                                         {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},   // p		
                                         {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},   // a		
                                         {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8}};  // n


  *******************************************/	
    public void getTableColumns(Connection con, String htmlFileName, int beginLineNum, int endLineNum) {
    	boolean            bContinue = true;
    	int                iCurLine = 1;
    	String             curLine = "";
    	ErrorCls           errorCls = new ErrorCls();
    	char[]             strArray;
    	int                i;
    	int                EventFunction;
    	RtnEventCls        rtnEventCls;
    	int                CurColumns = 1;
    	int                CurRows = 1;
    	boolean            bDebug = false;
    	MySqlAccess        mySqlAccess = new MySqlAccess();
    	boolean            bAltTdAdded = false;
    	
     	BuildEventChars();
    	try {
    	    FileInputStream    fstream = new FileInputStream(htmlFileName);
    	    DataInputStream    in = new DataInputStream(fstream);
    	    BufferedReader     br = new BufferedReader(new InputStreamReader(in));
    	    ArrayList<Integer> trRow = new ArrayList<Integer>();
    	    ArrayList<Integer> trCol = new ArrayList<Integer>();
    	    
    	    while(iCurLine < beginLineNum) {
    	        curLine = br.readLine();
    	        iCurLine++;
    	    }
    	    while((bContinue) && (iCurLine <= endLineNum)) {
    	    	curLine = br.readLine();
    	    	if(bDebug) {
    	    		System.out.println("CurLine: "  + curLine);
    	    	}
    	    	iCurLine++;
           	    strArray = curLine.toCharArray();
         	    for(i = 0; i < strArray.length; i++) {
         	    	if(bDebug) {
         	    		System.out.println("StateTbl= " + StateTable + ": CurState = " + CurState);
         	    	}
         	    	EventFunction = GetCharacterEvent(strArray[i], StateTable);
                    if(bDebug) {
         	    		System.out.println("Character: '" + strArray[i] +  "' :: " + i + " out of " + strArray.length);
         	    		System.out.println("EventFunction: " + EventFunction);

                    }
         	    	rtnEventCls = GetEventFunction(EventFunction).call(strArray[i], i, curLine);
         	    	if(bDebug) {
         	    		System.out.println("RETURN: ");
         	    		System.out.println("StateTbl = " + StateTable + " CurState = " + CurState);
         	    		System.out.println("AddTd = " +  ((rtnEventCls.bAddTd == true) ? "Yes" : "No") +
        	    		                   ": AddTr = " +  ((rtnEventCls.bAddTr == true) ? "Yes" : "No") +
        	    		                   ": AddCol = " +  ((rtnEventCls.bSaveCol == true) ? "Yes" : "No"));
        	    		System.out.println("");
         	    	}
        	    	if(rtnEventCls.bAddTd == true) {
                        if (bAltTdAdded == false){
        	    		    trRow.add(CurColumns);
        	    		    trCol.add(CurRows);
                        }
    	    		    rtnEventCls.bAddTd = false;
    	    		    bAltTdAdded = false;
        	    		CurColumns = 1;
        	    		CurRows = 1;
        	    	}
        	    	if(rtnEventCls.bAltTdEnd == true) {
        	    		trRow.add(CurColumns);
        	    		trCol.add(CurRows);
        	    		CurColumns = 1;
        	    		CurRows = 1;
        	    		bAltTdAdded = true;
        	    	}
        	    	if(rtnEventCls.bAddTr == true) {
        	    		TblRow.add(trRow);
        	    		trRow = new ArrayList<Integer>();
        	    		TblCol.add(trCol);
        	    		trCol = new ArrayList<Integer>();
        	    		rtnEventCls.bAddTr = false;
        	    		bAltTdAdded = false;
        	    	}
        	    	if(rtnEventCls.bSaveCol == true)
        	    		CurColumns = rtnEventCls.NumCols;
        	    	
        	    	if(rtnEventCls.bSaveRow == true)
        	    		CurRows = rtnEventCls.NumRows;
        	    	
         	    }
    	    }
    	    br.close();
    	    in.close();
    	    fstream.close();
    	}
    	catch (Exception e) {
		    errorCls.setSubUid(0);
		    errorCls.setCompanyUid(0);
		    errorCls.setItemVersion(0);
		    errorCls.setErrorText("Unable to parse input file " + htmlFileName + ": "  + e.getMessage());
		    errorCls.setBExit(false);
		    mySqlAccess.WriteAppError(con, errorCls);	
    	}
    }
    /* END OF COMMENT **********************************************************/
    private void BuildEventChars() {
    	/**********
    	String Events = "<tr>d/colspan";
    	int    i = 0;
    	
    	while(i < Events.length()) {
    	    EventChars.add(Events.charAt(i));
    	    i++;
    	}
    	******/
     	BuildEventTable(TrEventChars, "<tr");
       	BuildEventTable(TdEventChars, "<td/r>");
       	BuildEventTable(ColEventChars, "colspanrw<td/>");
    }
    
    private void BuildEventTable(ArrayList thisArray, String Events) {
    	int i = 0;
    	
    	while(i < Events.length()) {
    		thisArray.add(Events.charAt(i));
    		i++;
    	}
    }
    

    private int GetCharacterEvent(char thisChar, int iTable) {
        int iRtn = 2;  // non event character default
        int iEvent = 0;
        char lowerCased =  Character.toLowerCase(thisChar);
        
        switch (iTable) {
        case 0:
    	    if(TrEventChars.contains(lowerCased)) {
    			iEvent = TrEventChars.indexOf(lowerCased);
    			iRtn = TrStateArray[iEvent][CurState];
    	    }
       	break;
        	
        case 1:
    	    if(TdEventChars.contains(lowerCased)) {
    			iEvent = TdEventChars.indexOf(lowerCased);
    			iRtn = TdStateArray[iEvent][CurState];
    	    }
        	break;
        	
        case 2:
    	    if(ColEventChars.contains(lowerCased)) {
    			iEvent = ColEventChars.indexOf(lowerCased);
    			iRtn = ColStateArray[iEvent][CurState];
    	    }
       	break;
        }

        return(iRtn);
    }
    
    /************************
       private int GetCharacterEvent(char thisChar) {
            int iRtn = 0;  // non event character default
            int iEvent = 0;
            char lowerCased =  Character.toLowerCase(thisChar);
	    if(EventChars.contains(lowerCased)) {
			iEvent = EventChars.indexOf(lowerCased);
			iRtn = EventStateArray[iEvent][CurState];
	    }
        return(iRtn);
    }
    ***************/
    private EventStateFunctions GetEventFunction(int iIndex) {
    	return eventFunctions[iIndex];
    }
   	public TdColSpan() {
 
   		//DO NOTHING 
   	   	eventFunctions[0] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			return (rtnEventCls);
   	   		}
   	   	};
   	   	
   	   	// bump to next state
   	   	eventFunctions[1] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState += 1;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	// reset to start state (0)
   	   	eventFunctions[2] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState = 0;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	// select the next event state table
   	   	eventFunctions[3] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			StateTable += 1;
   	   			CurState = 0;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	// Reset to first event character encountered
   	   	eventFunctions[4] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState = 1;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	// event encountered a '\'
   	   	eventFunctions[5] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState = 3;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	// set to previous event/state table Save Tr row
   	   	eventFunctions[6] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			StateTable = 0;
   	   			CurState = 0;
   	   			rtnEventCls.bAddTr = true;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	// encountered the colspan event string
   	   	eventFunctions[7] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
 
   	            int    iNumColumns = -1;
   	            
   	            iNumColumns = ExtractNumber(curLine.substring(iIndex+1));
   	            if(iNumColumns != -1) {
   	            	rtnEventCls.NumCols = iNumColumns;
   	                rtnEventCls.bSaveCol = true;
   	            }    
   	   			CurState = 0;
   	   			return (rtnEventCls);
   	   		}
   	   	};

  	   	eventFunctions[8] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState = 7;
   	   			return (rtnEventCls);
   	   		}
   	   	};

 
   	   	// encountered the rowspan event string
   	   	eventFunctions[9] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			int         iNumColumns;
   	   			
   	   			CurState = 0;
   	   			iNumColumns = ExtractNumber(curLine.substring(iIndex+1));
   	   			if(iNumColumns != -1) {
   	   				rtnEventCls.NumRows = iNumColumns;
   	   				rtnEventCls.bSaveRow = true;
   	   			}
   	   			CurState = 0;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	eventFunctions[10] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState = 13;
   	   			return (rtnEventCls);
   	   		}
   	   	};

   	   	//completed td
   	   	eventFunctions[11] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			StateTable -= 1;
   	   			CurState = 0;
   	   			rtnEventCls.bAddTd = true;
   	   			return (rtnEventCls);
   	   		}
   	   	};
   	   	
   	   	// found the '/>' to end '<td'  
   	  	eventFunctions[12] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   	   		RtnEventCls rtnEventCls = new RtnEventCls();
   	   	   			
   	   	   		CurState = 0;
   	   	   		StateTable -=1;
   	   	   		rtnEventCls.bAltTdEnd = true;
   	   	   		return (rtnEventCls);
   	   	   	}
   	   	};

  // orphan '/' looking for alternate >
   	   	eventFunctions[13] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   	   		RtnEventCls rtnEventCls = new RtnEventCls();
   	   	   			
   	   	   		CurState = 17;
   	   	   		return (rtnEventCls);
   	   	   	}
   	   	};
 	   	/*  PREVIOUS EVENT FUNCTIONS
   	   	eventFunctions[0] = new EventStateFunctions() {
   	   		public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	   			RtnEventCls rtnEventCls = new RtnEventCls();
   	   			
   	   			CurState = 0;
   	   			return (rtnEventCls);
   	   		}
   	   	};
   	   	
   	    eventFunctions[1] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	       		RtnEventCls rtnEventCls = new RtnEventCls();
   	       			
   	       		CurState = CurState + 1;
   	       		return (rtnEventCls);
   	       	}
   	    };
   	    
   	   
   	    eventFunctions[2] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	                   			
   	            CurState = 4;
   	            return (rtnEventCls);
   	        }
   	    };
   	    
   	    eventFunctions[3] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	                       			
   	            CurState = 5;
   	            return (rtnEventCls);
   	        }
   	    };
   	    eventFunctions[4] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	                           			
   	            CurState = 8;
   	            return (rtnEventCls);
   	        }
   	    };
   	    eventFunctions[5] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	                           			
   	            CurState = 9;
   	            return (rtnEventCls);
   	        }
   	    };
   	    //was </tr> now </td> completed
   	    eventFunctions[6] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	            
   	            rtnEventCls.bAddTd = true;
   	            CurState = 0;
   	            return (rtnEventCls);
   	        }
   	    };
   	    //was </td>  now </tr> completed
   	    eventFunctions[7] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	                           			
   	            CurState = 0;
   	            rtnEventCls.bAddTr = true;
   	            return (rtnEventCls);
   	        }
   	    };
   	    // colspan completed
   	    eventFunctions[8] = new EventStateFunctions() {
   	        public RtnEventCls call(char inChar, int iIndex, String curLine) {	
   	            RtnEventCls rtnEventCls = new RtnEventCls();
   	            String Temp = curLine.substring(iIndex+1);
   	            int    index;
   	            
   	            try {
   	                index = Temp.indexOf("=");
   	                if(index != -1) {
   	            	    Temp = Temp.substring(index+1);
   	            	    String ColWidth = "";
   	            	    int    iValue = 0;
   	            	    int    ixIndex = 0;
   	            	    int    iLen = 0;
   	            	    if(Temp.substring(0,1).equals("\""))
   	            	    		Temp = Temp.substring(1);
   	            	    while(iValue != -1) {
   	            	    	iValue = CONSTANTS.isNumeric(Temp.substring(ixIndex, ixIndex+1));
   	            	    	if(iValue != -1)
   	            	    		iLen = ixIndex;
   	            	    	ixIndex++;
   	            	    	if(ixIndex == Temp.length())
   	            	    		iValue = -1;
   	            	    }
   	            	    //index = Temp.indexOf("\"");
   	            	    //if(index != -1) {
   	            		//    Temp = Temp.substring(index+1);
   	            		//    index = Temp.indexOf("\"");
   	            		//    if(index != -1) { // we got our number
   	            			    Temp = Temp.substring(0, ixIndex-1);
   	            			    rtnEventCls.NumCols = Integer.parseInt(Temp);
   	            			    rtnEventCls.bSaveCol = true;
   	            		//    }
   	            		//}
   	            	}
   	            }
   	            catch( Exception e) {
   	            	CurState = 0;
   	            }
   	            CurState = 0;
   	            return (rtnEventCls);
   	        }
   	    };
   	    END OF COMMENT ****************************************/
  	 }
   	
   	private int ExtractNumber(String origStr) {
   		int       iRtn = -1;
   		CONSTANTS constants = new CONSTANTS();
   		
           int    index;
	            
           try {
               index = origStr.indexOf("=");
               if(index != -1) {
            	   origStr = origStr.substring(index+1);
           	       String ColWidth = "";
           	       int    iValue = 0;
           	       int    ixIndex = 0;
           	       int    iLen = 0;
           	       if(origStr.substring(0,1).equals("\""))
           	    	    origStr = origStr.substring(1);
           	        while(iValue != -1) {
           	    	    iValue = constants.isNumeric(origStr.substring(ixIndex, ixIndex+1));
           	    	    if(iValue != -1)
           	    		    iLen = ixIndex;
           	    	    ixIndex++;
           	    	    if(ixIndex == origStr.length())
           	    		    iValue = -1;
           	        }
           	        origStr = origStr.substring(0, ixIndex-1);
           	        iRtn = Integer.parseInt(origStr);
           	   }
           }
           catch( Exception e) {
           	 iRtn = -1;
           }  	   			
  		return(iRtn);
   	}
}
     		
