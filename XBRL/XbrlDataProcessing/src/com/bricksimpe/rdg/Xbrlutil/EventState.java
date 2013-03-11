package com.bricksimpe.rdg.Xbrlutil;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class EventState {
	private int[][] EventStateTbl = null;
	private ArrayList EventChars = new ArrayList();
	private int[][] NextStateTbl = null;
	private boolean bDiscoveredUserDef = false;
	public String WorkingBuffer = "";
	public String HoldingBuffer = "";
	public String CompletedBuffer = "";
	private int CurState = 0;
	private int LastStateDef = 0;
	//private int TargetBuffer = 0;  // if 0 put in working buffer else put in holding buffer
	//private String TagStack[];
	//private Boolean DiscardStack[] = null;
	public int SaveToTermTag = 0;
	public int DiscardToEndTag = 0;
	private EventStateFunctions[] eventFunctions = new EventStateFunctions[18];
    private String DebugEventChar = "";
    private int ThisCharIndex;
	public BufferedWriter wr  = null;
	private ArrayList<Integer> SubstitutionIndex =  new ArrayList<Integer>();
	private ArrayList<String> EventStrs =  new ArrayList<String>();
	private ArrayList<String> SubstitutionStrs = new ArrayList<String>();
	private boolean bInUserNoteDef = false;
	
	// event state function table values
    // NOTE: event function will set the set the discard flag appropriately
    // The values here are derived from the mySQL table html_tags and field operation
	// these are functions are executed to process a single character
	// 0 - Pass through to either working buffer or holding buffer depending upon TargetBuffer value
	// 1 - Pass through to either working buffer or holding buffer depending upon TargetBuffer value
	//   - using state change buffer set the CurState value
	// 2 - Event completion - discard characters in Workingbuffer  
    //   - using state change buffer set the CurState value
    //   - ensure pointing to working buffer
	// 3 - Event completion 
	//   - pop open event stack
	//   - discard characters from WorkingBuffer
	// 4 - Event completion
	//   - pop open event stack
	//   - move characters from WorkingBuffer to HoldingBuffer
	
	// this is the call 
	
	public EventStateFunctions getEventFunctions(int index) {
		return eventFunctions[index];
	}

    public void ResetCurState() {
    	CurState = 0;
    }
    
    public int GetCurState() {
    	int iRtn; 
    	iRtn = CurState;
    	
    	return (iRtn);
    }
 
    public int GetCharacterEvent(char textChar) {
		int iRtn = 256;   // none event char

		char lowerTextChar = Character.toLowerCase(textChar);
	    if(EventChars.contains(lowerTextChar)) {
			ThisCharIndex = EventChars.indexOf(lowerTextChar);
			iRtn = EventStateTbl[CurState][ThisCharIndex];
	    }
        return(iRtn);
	}
	
	public void insertSubstitutionStr(String subStr) {
		SubstitutionStrs.add(subStr);
	}
	public void insertEventStr(HtmlTag newEvent) {
		char test;
		int myIndex;
		boolean bcontinue;
	    char[] newarray = null;
	    int iLooper = 0;
		int iNew;
		//int iCur;
		int operation = 0;
		//int iPrevEventList;
		
		
		for(iLooper = 0; iLooper < 3; iLooper++) {
		    iNew = 0;
		    //iCur = 0;
		    switch (iLooper) {
		        case 0:
				    newarray = newEvent.tag.toCharArray();
				    operation = newEvent.operation;
                   break;
                
		        case 1:
		        	newarray = newEvent.endTag.toCharArray();
		        	operation = newEvent.endOperation;
		        	break;

		        case 2:
		        	newarray = newEvent.termTag.toCharArray();
		        	operation = newEvent.termOperation;
		        	break;
            }
        	if(newarray[0] != '~')
        		bcontinue = true;
        	else
        		bcontinue = false;
		
		    CurState = 0;  // not sure about this - should be set for new events - not sure about matching event strings
		    while(bcontinue) {
			    test = newarray[iNew];
			    iNew++;
			    //if(EventStateTbl == null)
			    //	iPrevEventList = 0;
			    //else
			    // iPrevEventList = EventStateTbl[0].length;
			    myIndex = IsCharInArray(test);
			    ExpandEventWidth(CurState);
			    //if at end of array this is the event function to call
			    if(iNew >= newarray.length) {
				    EventStateTbl[CurState][myIndex] = operation;
				    bcontinue = false;
			    }
			    else {
				    // if the this one is not used - assign a new state to it
			        if((NextStateTbl[CurState][myIndex] == -1) || (NextStateTbl[CurState][myIndex] == 0)) {
				        LastStateDef++;
				        NextStateTbl[CurState][myIndex] = LastStateDef;
				        CurState = LastStateDef;
				        ExpandEventWidth(LastStateDef);
			        }
			        // else this is our new state
			        else
			    	    CurState = NextStateTbl[CurState][myIndex];
			    }
		    }
			//System.out.println("Event Characters:");
			//System.out.println(DebugEventChar);
			//DumpTable("EventStateTable: ", EventStateTbl);
			//DumpTable("NextStateTable:", NextStateTbl);
			//System.out.println("End Inserting");
		}
		EventStrs.add(newEvent.tag);
		SubstitutionIndex.add(newEvent.xlationIndex);
	}
	
	//this function will fill the tables correctly to insert proper events and states
	// for any unused items
	public void FixTables() {
	    for(int r = 0; r < NextStateTbl.length; r++) {
	        int zeroNum = NextStateTbl[r][0];
	        if(zeroNum == 0) {
	    	    zeroNum = 0;
	            NextStateTbl[r][0] = 1; //was zeronum
	        }
	        for(int c = 1; c < NextStateTbl[r].length; c++) {
	    	    if(NextStateTbl[r][c] == -1)
	    		    NextStateTbl[r][c] = 0;
	        }
	    }
	    //System.out.println("HOLDING LINE");
	}
	private void DumpTable(String TableName, int[][] table) {
		
		String tempStr = "";
		System.out.println("TABLE: " + TableName);
		for(int r = 0; r < table.length; r ++) {
			tempStr = "";
		    for(int c = 0; c < table[r].length; c++)
		    	tempStr = tempStr + Integer.toString(table[r][c]) + " ";
		    System.out.println(tempStr);
		}
	}
	private void ExpandEventWidth(int iCurState) 
	{
	     int iColumnCnt;
	     int iRowCnt;
		 int  TempArray[][];
	     
	     iColumnCnt = EventStateTbl.length;
	     iRowCnt = EventStateTbl[0].length;
	     if(iColumnCnt <= iCurState) {
		     TempArray = CopyArray(EventStateTbl, iColumnCnt, iRowCnt);  // copy original
		     EventStateTbl = CopyArray(TempArray, iColumnCnt+1, iRowCnt);	
		     TempArray = CopyArray(NextStateTbl, iColumnCnt, iRowCnt);  // copy original
		     NextStateTbl = CopyArray(TempArray, iColumnCnt+1, iRowCnt);
	         for(int i = 0; i < EventStateTbl[0].length; i++) {
		         EventStateTbl[iColumnCnt][i] = 1;
		         NextStateTbl[iColumnCnt][i] = 0;
		     }
	     }     
	}
	private int IsCharInArray(char test) {
		int iRtn = -1;
		int iColumnCnt = 0;
		int iRowCnt = 0;
		int  TempArray[][];
		int iNewColumnCnt;
		
		if(EventStateTbl != null) {
			iColumnCnt = EventStateTbl.length;
			iRowCnt = EventStateTbl[iColumnCnt-1].length;
		//	iRowCopyCnt = iRowCnt;
		}
	    if(EventChars.contains(test))
			iRtn = EventChars.indexOf(test);
		else {
		    DebugEventChar += Character.toString(test) + " ";
			EventChars.add(test);
			iRtn = EventChars.indexOf(test);
			iNewColumnCnt = iColumnCnt;
			if(iNewColumnCnt == 0)
				iNewColumnCnt =1;
			TempArray = CopyArray(EventStateTbl, iColumnCnt, iRowCnt);  // copy original
			EventStateTbl = CopyArray(TempArray, iNewColumnCnt, iRowCnt+1);	
			TempArray = CopyArray(NextStateTbl, iColumnCnt, iRowCnt);  // copy original
			NextStateTbl = CopyArray(TempArray, iNewColumnCnt, iRowCnt+1);
		    for(int i = 0; i < EventStateTbl.length; i++) {
			    EventStateTbl[i][iRowCnt] = 1;
			    NextStateTbl[i][iRowCnt] = 0;
		    }
		}
		return(iRtn);
	}

	private int[][] CopyArray(int[][] SrcArray, int iColumnCnt, int iRowCnt) {
		int[][] CopiedArray = null;
		
		if(iColumnCnt > 0)
		    CopiedArray = new int[iColumnCnt][iRowCnt];
		if(SrcArray != null) {
            int iCopyColumnCnt = SrcArray.length;
            int iCopyRowCnt = SrcArray[iCopyColumnCnt -1].length;
		    for (int r = 0; r < iCopyColumnCnt; r++) {
			    for(int c = 0; c < iCopyRowCnt; c++ ) {
				    CopiedArray[r][c] = SrcArray[r][c];
			    }
		    }
		}
		return CopiedArray;
	}
	
	private String EscapeIt(String origStr) {
		String rtnStr;
		
		//rtnStr = origStr.replace("\"", "\\\"");
		rtnStr = origStr;
		return(rtnStr);
	}
	private void PushDiscardStack(int PushEvent) {
		
	}
	private static final String RdgNum  = "id=\"";
	
	private void IsUserNote(String origStr) {
		boolean bRtn = false;
		
		int     i = origStr.indexOf(RdgNum);
		if(i != -1) {
			String extract = origStr.substring(i+RdgNum.length());
			i = extract.indexOf("\"");
			if(i != -1) {
				extract = extract.substring(0 ,i);
				i = Integer.parseInt(extract);
				if(i == 4)
					bRtn = true;
			}
		}
		bInUserNoteDef = bRtn;
	}

	// Operations to do when event sensed entire event string
	public EventState() {
		//save character to either working or holding buffer
		// set state to 0
		eventFunctions[0] = new EventStateFunctions() {
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf,
					                int iOpenDivCount) {
				EventRtnCls rtnEventCls = new EventRtnCls();
				
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				String strToInsert = Character.toString(inChar);
				
				if(DiscardToEndTag > 0) {
					WorkingBuffer = "";
				}
				else {
					if((strToInsert.equals(" ")) && (WorkingBuffer.length() == 0) && (HoldingBuffer.length() == 0)) {
						if((CompletedBuffer.trim().length() > 0) && (CompletedBuffer.contains("<") == false))
							strToInsert = " ";
						else
						    strToInsert = "";
					}
				    WorkingBuffer += strToInsert;
				    if(SaveToTermTag > 0) {
					    HoldingBuffer += WorkingBuffer;		
					    WorkingBuffer = "";
				    }
				}
				CurState = 0;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				//rtnEventCls.bWroteLine = false;
				return (rtnEventCls);
			}
		};
		
		eventFunctions[1] = new EventStateFunctions() {

			// absorb event string + chars + endtag
			// set state to 0
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf,
					                int iOpenDivCount) {
				
				EventRtnCls rtnEventCls  = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				if((SaveToTermTag > 0)  && (DiscardToEndTag == 0) && (CurState == 0)){
					CompletedBuffer += HoldingBuffer;
					HoldingBuffer = WorkingBuffer;		
					WorkingBuffer = "";
				}
				else {
					//if((CurState == 11) && (DiscardToEndTag == 0)) {
					if((CurState == 11) && (DiscardToEndTag == 0)) {
						if(WorkingBuffer.equals("-")) {
						    CompletedBuffer += HoldingBuffer;
						    HoldingBuffer = WorkingBuffer;		
						    WorkingBuffer = "";
						}
						//else
						//	System.out.println("WorkingBuffer: " + WorkingBuffer);
					}					
				}
				WorkingBuffer += Character.toString(inChar);
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
		
		// clear working buffer - ie TAG
		// set state to value in NextStateTbl
		eventFunctions[2] = new EventStateFunctions() {

			public EventRtnCls call(char inChar, boolean bWroteWithoutLf,
					                int iOpenDivCount) {
				
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
                WorkingBuffer = "";
                //SaveToTermTag = 0;  //wlw removed as we should not reset save flag
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
		
		//replace the tag with a dash
		// since 0 is not a valid conversion index the value in the html_tags table is decremented by one
		// to index the substitutions table.
		eventFunctions[3] = new EventStateFunctions() {

			// save tag and push tag on stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				int myNdx = 0;
				int subNdx = 0;
				boolean bFound = false;
				int     eIndex = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				WorkingBuffer += inChar;
				while(bFound == false) {
					eIndex = WorkingBuffer.indexOf(EventStrs.get(myNdx));
				    if(eIndex == -1)
				    	myNdx++;
				    else {
				    	subNdx = SubstitutionIndex.get(myNdx);
				    	WorkingBuffer = WorkingBuffer.replace(EventStrs.get(myNdx), SubstitutionStrs.get(subNdx-1));
				    	bFound = true;
				    }
				}
				//myNdx = EventStrs.indexOf(WorkingBuffer);
				//subNdx = SubstitutionIndex.get(myNdx);
				//subStr = SubstitutionStrs.get(subNdx-1);  // zero based
				//CompletedBuffer += HoldingBuffer + subStr;
				CompletedBuffer += HoldingBuffer + WorkingBuffer;
				WorkingBuffer = "";
				HoldingBuffer = "";
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
			    return (rtnEventCls);
			}
		};
		
		eventFunctions[4] = new EventStateFunctions() {

			// save the end tag and save chars
			// pop stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
				DiscardToEndTag--;
				if(DiscardToEndTag < 0)
					DiscardToEndTag = 0;
				CompletedBuffer += HoldingBuffer;  // + Character.toString(inChar);
				WorkingBuffer = "";
				HoldingBuffer = "";
				if(bDiscoveredUserDef == true) {
				    try {
				    	CompletedBuffer += ">";
				    	CompletedBuffer = EscapeIt(CompletedBuffer);
				        wr.write(CompletedBuffer);
				        wr.newLine();
					    wr.flush();
				        bWroteWithoutLf = true;
				        rtnEventCls.bWroteLine = true;
				        IsUserNote(CompletedBuffer);
				    }
				    catch (Exception e) {
					    System.out.println("Unable to write output file: " + e.getMessage());
				    }
				    CompletedBuffer = "";
				    //bDiscoveredUserDef = false;
				}
				//if(SaveToTermTag > 0)
				//	SaveToTermTag--;
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
		
		// Throw away tag
		// set flag to discard all to and including endtag 
		eventFunctions[5] = new EventStateFunctions() {

			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
				CompletedBuffer += HoldingBuffer;
				HoldingBuffer = "";
				WorkingBuffer = "";
				DiscardToEndTag++;
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
			    return (rtnEventCls);
			}
		};
		
		//NOTE: this adds a '>' to the tag
		eventFunctions[6] = new EventStateFunctions() {

			// save tag and save chars
			// pop stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				WorkingBuffer += Character.toString(inChar) + '>';
				HoldingBuffer += WorkingBuffer;
				WorkingBuffer = "";
                CurState = NextStateTbl[CurState][ThisCharIndex];
                DiscardToEndTag++;
                SaveToTermTag++;
                //rtnEventCls.bWroteLine = false;
                rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
		
		// decrement the count of discard to end tag
		// pop stack
		eventFunctions[7] = new EventStateFunctions() {

			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				DiscardToEndTag--;
				if(DiscardToEndTag < 0)
					DiscardToEndTag = 0;
				WorkingBuffer = "";
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
	
		//save characters to completed buffer
		//discard event string
		// decrement saving
		eventFunctions[8] = new EventStateFunctions() {
  
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				WorkingBuffer = "";
				CompletedBuffer += HoldingBuffer;
				HoldingBuffer = "";
				//SaveToTermTag--;
				SaveToTermTag = 0;  // removed above and added this
				if(SaveToTermTag < 0)
					SaveToTermTag = 0;
				CurState = NextStateTbl[CurState][ThisCharIndex];
				try {
					CompletedBuffer = EscapeIt(CompletedBuffer);
				    wr.write(CompletedBuffer);
				    wr.newLine();
				    wr.flush();
				    rtnEventCls.bWroteWithoutLf = false;
				}
				catch (Exception e) {
					System.out.println("Unable to write output file: " + e.getMessage());
				}
				CompletedBuffer = "";
				rtnEventCls.bWroteLine = true;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = false;
				return(rtnEventCls);
			}
		};

		eventFunctions[9] = new EventStateFunctions() {
			  
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
				WorkingBuffer += Character.toString(inChar);
				CompletedBuffer += HoldingBuffer + WorkingBuffer;
				WorkingBuffer = "";
				HoldingBuffer = "";
				SaveToTermTag--;
				if(SaveToTermTag < 0)
					SaveToTermTag = 0;
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//if(CompletedBuffer.indexOf("</table>") != -1) {
				//	int ie = 0;
				//}
				try {
					CompletedBuffer = EscapeIt(CompletedBuffer);
				    wr.write(CompletedBuffer);
				    wr.flush();
				    //wr.newLine();
				    rtnEventCls.bWroteWithoutLf = true;
				}
				catch (Exception e) {
					System.out.println("Unable to write output file: " + e.getMessage());
				}
				CompletedBuffer = "";
				//rtnEventCls.bWroteLine = true;
				rtnEventCls.curState = CurState;
				//rtnEventCls.bWroteWithoutLf = true;
				return(rtnEventCls);
			}
		};
		
		eventFunctions[10] = new EventStateFunctions() {

			// save tag and save chars
			// pop stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				
				if(WorkingBuffer.equals("<di")) {
					iOpenDivCount++;
					//System.out.println("Div: " + iOpenDivCount);
					if((iOpenDivCount > 1) && (CompletedBuffer.length() > 0)) {
					    try {
					    	wr.write(CompletedBuffer);
					        wr.newLine();
						    wr.flush();
					        CompletedBuffer = "";
					    }
					    catch (Exception e) {
						    System.out.println("Unable to write output file: " + e.getMessage());
					    }
					    rtnEventCls.bWroteLine = true;
					}
				}
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				/********************************
				if(bRdgTemplate == true) {
				    try {
				        wr.newLine();
				    }
				    catch (Exception e) {
					    System.out.println("Unable to write output file: " + e.getMessage());
				    }
				    rtnEventCls.bWroteLine = true;
				    bWroteWithoutLf = false;
				}
				******************************/
				//System.out.println("Holding: " + HoldingBuffer);
				//System.out.println("Working: " + WorkingBuffer);
				WorkingBuffer = "";
                CurState = NextStateTbl[CurState][ThisCharIndex];
                DiscardToEndTag++;
                SaveToTermTag++;
                bDiscoveredUserDef = false;
                //rtnEventCls.bWroteLine = false;
                rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
		

		// this event function is used to discard the event string
		// which should live in the working buffer only
		eventFunctions[11] = new EventStateFunctions() {

			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
                WorkingBuffer = "";
 				CurState = NextStateTbl[CurState][ThisCharIndex];
 				//rtnEventCls.bWroteLine = false;
 				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				return (rtnEventCls);
			}
		};
		// was a 4 
		eventFunctions[12] = new EventStateFunctions() {

			// save the end tag and save chars
			// pop stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount -1;
				if(rtnEventCls.iOpenDivCount < 0)
					rtnEventCls.iOpenDivCount = 0;
				
				DiscardToEndTag--;
				if(DiscardToEndTag < 0)
					DiscardToEndTag = 0;
				CompletedBuffer += HoldingBuffer;  
				//if(CompletedBuffer.length() > 0) {
				    try {
					    CompletedBuffer = EscapeIt(CompletedBuffer);
				        wr.write(CompletedBuffer);
				        wr.newLine();
					    wr.flush();
				        //rtnEventCls.bWroteWithoutLf = false;
				    }
				    catch (Exception e) {
					    System.out.println("Unable to write output file: " + e.getMessage());
				    }
				    CompletedBuffer = "";
				    WorkingBuffer = "";
				    HoldingBuffer = "";
					rtnEventCls.bWroteLine = true;
				//}
				WorkingBuffer = "";   // WLW inserted here to remove </div
				CurState = NextStateTbl[CurState][ThisCharIndex];
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = false;
				return (rtnEventCls);
			}
		};

		// WAS a 9
		eventFunctions[13] = new EventStateFunctions() {
			  
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
				WorkingBuffer += Character.toString(inChar);
				CompletedBuffer += HoldingBuffer + WorkingBuffer;
				WorkingBuffer = "";
				HoldingBuffer = "";
				SaveToTermTag--;
				if(SaveToTermTag < 0)
					SaveToTermTag = 0;
				CurState = NextStateTbl[CurState][ThisCharIndex];			
				try {
					CompletedBuffer = EscapeIt(CompletedBuffer);
				    wr.write(CompletedBuffer);
				    wr.newLine();
				    wr.flush();
				    rtnEventCls.bWroteWithoutLf = false;
				}
				catch (Exception e) {
					System.out.println("Unable to write output file: " + e.getMessage());
				}
				CompletedBuffer = "";
				rtnEventCls.bWroteLine = true;
				rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = false;
				return(rtnEventCls);
			}
		};
		
		// was 6 moved <table here
		eventFunctions[14] = new EventStateFunctions() {

			// save tag and save chars
			// pop stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
				WorkingBuffer += Character.toString(inChar) + '>';
				if(bWroteWithoutLf == true) {
					if(bDiscoveredUserDef == false) {
					
				        try {
				            wr.newLine();
				        }
				        catch (Exception e) {
					        System.out.println("Unable to write output file: " + e.getMessage());
				        }
				    }
					bDiscoveredUserDef = false;
				    rtnEventCls.bWroteLine = true;
				    rtnEventCls.bWroteWithoutLf = false;
				}
				HoldingBuffer += WorkingBuffer;
				WorkingBuffer = "";
                CurState = NextStateTbl[CurState][ThisCharIndex];
                DiscardToEndTag++;
                SaveToTermTag++;
                //rtnEventCls.bWroteLine = false;
                rtnEventCls.curState = CurState;
                rtnEventCls.bIsTableTag = true;
 				return (rtnEventCls);
			}
		};
		
		// saves the tag and up to the termination tag
		// ie: <rdgtemplate  id= "0"> 
		// at termination adds a line feed
		eventFunctions[15] = new EventStateFunctions() {

			// save tag and save chars
			// pop stack
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
			
				WorkingBuffer += Character.toString(inChar);
				HoldingBuffer += WorkingBuffer;
				WorkingBuffer = "";
                CurState = NextStateTbl[CurState][ThisCharIndex];
                SaveToTermTag++;
                //rtnEventCls.bWroteLine = false;
                rtnEventCls.curState = CurState;
				rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				bDiscoveredUserDef = true;
				return (rtnEventCls);
			}
		};

		// saves the event character and adds a line feed
		
		eventFunctions[16] = new EventStateFunctions() {

			
			// writes a leading line feed, working buffer + '>' and a trailing line feed
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				DiscardToEndTag--;
				if(DiscardToEndTag < 0)
					DiscardToEndTag = 0;
				CompletedBuffer += HoldingBuffer + WorkingBuffer + ">";  // + Character.toString(inChar);
				WorkingBuffer = "";
				HoldingBuffer = "";
				try {
					if(bInUserNoteDef == false)
					    wr.newLine();					
					CompletedBuffer = EscapeIt(CompletedBuffer);
				    wr.write(CompletedBuffer);
				    wr.newLine();
				    wr.flush();
				    rtnEventCls.bWroteWithoutLf = false;
				}
				catch (Exception e) {
					System.out.println("Unable to write output file: " + e.getMessage());
				}
				bInUserNoteDef = false;
				CompletedBuffer = "";
				CurState = NextStateTbl[CurState][ThisCharIndex];
				//rtnEventCls.bWroteLine = false;
				rtnEventCls.bWroteLine = true;
				//rtnEventCls.bPreceededLf = true;   
				rtnEventCls.curState = CurState;
				return (rtnEventCls);
			}
		};


		eventFunctions[17] = new EventStateFunctions() {

			
			// writes a leading line feed, working buffer + '>' and a trailing line feed
			public EventRtnCls call(char inChar, boolean bWroteWithoutLf, int iOpenDivCount) {
				int iRtn = 0;
				EventRtnCls rtnEventCls = new EventRtnCls();
				rtnEventCls.iOpenDivCount = iOpenDivCount;
				
				DiscardToEndTag--;
				if(DiscardToEndTag < 0)
					DiscardToEndTag = 0;
				CompletedBuffer += HoldingBuffer;
				WorkingBuffer = "";
				HoldingBuffer = "";
				if((CompletedBuffer.length() > 0) || (bWroteWithoutLf == true)) {
				    try {
					    CompletedBuffer = EscapeIt(CompletedBuffer);
				        wr.write(CompletedBuffer);
				        wr.newLine();
					    wr.flush();
				        rtnEventCls.bWroteWithoutLf = false;
						rtnEventCls.bWroteLine = true;
				    }
				    catch (Exception e) {
					    System.out.println("Unable to write output file: " + e.getMessage());
				    }
				}
				else  { // preserve previous worte line info
					rtnEventCls.bWroteWithoutLf = bWroteWithoutLf;
				}
				bInUserNoteDef = false;
				CompletedBuffer = "";
				CurState = NextStateTbl[CurState][ThisCharIndex];
				rtnEventCls.curState = CurState;
				return (rtnEventCls);
			}
		};

	// this counts the carriage return
		/***
		eventFunctions[14] = new EventStateFunctions() {
			public EventRtnCls call(char inChar) {
				EventRtnCls rtnEventCls = new EventRtnCls();
				
				CarriageRtnCounter = CarriageRtnCounter + 1;
				return(rtnEventCls);
			}
		};
		***/

// this is a dummy for testing purposes - add new event process function		

	}

}
