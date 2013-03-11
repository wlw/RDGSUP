package com.bricksimpe.rdg.templateId;

import java.util.ArrayList;

/*******************************************************************/
/* This class is used to maintain the list of note tags sensed     */
/* There will be only be a maximum of three notes searched for at  */
/* at any one time. We always look for 1. The Main list which will */
/* be 2 to n and the children list                                 */
/*******************************************************************/

public class MultiNoteCls {
    private ArrayList<Integer> LookFors = new ArrayList<Integer>();
    private ArrayList<HtmlTraceItem> NoteItems = new ArrayList<HtmlTraceItem>();
    private ArrayList<Integer> MainList = new ArrayList<Integer>();
    private ArrayList<Integer> ChildList = new ArrayList<Integer>();
    
    public void MultiNoteCls() {
    	LookFors.add(1);
    }
    
    public void AddToLookList(int iNumToAdd) {
    	LookFors.add(iNumToAdd);
    }
    
    public Integer AddTraceItem(CheckForTemplateStr checkForTemplateStr, int LineNumber,
    		                    TemplateIdStr[] TemplateInfo, String NoteStr,
    		                    int iIdLine) {
    	HtmlTraceItem htm = new HtmlTraceItem();
        CONSTANTS     constants = new CONSTANTS();
        
    	htm.setBeginLine(checkForTemplateStr.GetBeginLineNum());
    	htm.setConfidenceLevel(1);
        if(checkForTemplateStr.GetFoundTableBegin() == false) {
        	htm.setEndLine(LineNumber -1);  // as this is the start of the next 
            //checkForTemplateStr.SetBeginLineNum(LineNumber);  // this is the start of this note
        }
        else {
        	htm.setEndLine(checkForTemplateStr.GetLastTableTag() -1);  // as this is the start of the next 
            //checkForTemplateStr.SetBeginLineNum(checkForTemplateStr.GetLastTableTag());  // this is the start of this note
        }
        htm.setTemplateId(TemplateInfo[checkForTemplateStr.GetLastMatchIndex()].getTemplateId());
        htm.setTermStr(NoteStr);
        String Extracted = checkForTemplateStr.GetMatchedText().replace("&#160;", "");
        htm.setIdentifiedText(constants.RemoveTableTags(Extracted));
        htm.setNoteWithinTable(checkForTemplateStr.GetNoteWithinTable());
        htm.setIdLine(iIdLine);
        NoteItems.add(htm);
    	return(NoteItems.size() -1);
    }
    
    public void AddToMainList(CheckForTemplateStr checkForTemplateStr, int LineNumber,
    		                    TemplateIdStr[] TemplateInfo, String NoteStr,
    		                    int iIdLine) {
    	int  iIndex = AddTraceItem(checkForTemplateStr, LineNumber, TemplateInfo, NoteStr, iIdLine);
    	MainList.add(iIndex);
    }

    public void AddToChildList(CheckForTemplateStr checkForTemplateStr, int LineNumber,
            TemplateIdStr[] TemplateInfo, String NoteStr,
            int iIdLine) {
        int  iIndex = AddTraceItem(checkForTemplateStr, LineNumber, TemplateInfo, NoteStr, iIdLine);
        ChildList.add(iIndex);
    }
    
    // will integrate the child list into the main list
    public void UnitifyList() {
    	
    	int    iNumMainList = MainList.size();
    	
    	while(iNumMainList < ChildList.size()) {
    		MainList.add(ChildList.get(iNumMainList));
    		iNumMainList++;
    	}
    	// now strip child list
     	ChildList.clear();
     	//now update the lookfor list
     	LookFors.clear();
     	LookFors.add(1);
     	LookFors.add(MainList.size());
    }
    
}
