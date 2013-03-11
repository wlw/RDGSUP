package com.bricksimple.rdg.pushxbrl;

import java.util.ArrayList;

import com.bricksimple.rdg.xbrlUpload.CONSTANTS;
import com.bricksimple.rdg.xbrlUpload.RootNode;
import com.bricksimple.rdg.xbrlUpload.PreNode;

public class MasterNoteTableConcepts {

	private ArrayList<NoteTableConcepts> nTC = new ArrayList<NoteTableConcepts>();
    private String                       DisclosureStr = "Disclosure -";
    private String                       TableStr      = "(Tables)";
    private String                       DetailStr     = "(Detail)";
    private String                       NoteTableTag = "";
    private ArrayList<String>            TableTags = new ArrayList<String>();
    private boolean                      bSaveTableTags = false;
    
	
	public String GetNoteTableConcept(String noteTag) {
		String rtnStr = "";
			
		return(rtnStr);
	}
	
	public void InsertNoteTableConcepts(RootNode curNode) {
		NoteTableConcepts newTableDesc = new NoteTableConcepts();
		
		NoteTableTag = curNode.GetLabel();
		FindNoteTag(curNode.GetChildren());
		newTableDesc.SetOrigTag(NoteTableTag);
		newTableDesc.SetNoteTag(StripTableTag(NoteTableTag));
		for(String temp: TableTags) {
			newTableDesc.AddTableConcept(temp);			
		}
		nTC.add(newTableDesc);
	}
	
	private void FindNoteTag(ArrayList<PreNode> parents) {
		
		for(PreNode thisNode: parents) {
			if(thisNode.GetId().contains(CONSTANTS.StatementLineItems))
				SaveChildren(thisNode);
			else
				FindNoteTag(thisNode.GetChildren());
		}
	}
	
	private void SaveChildren(PreNode parentNode) {
		String child;
		int    i;
		
		for(PreNode children: parentNode.GetChildren()) {
		    child = children.GetId();
		    i = child.indexOf("#");
		    child = child.substring(i + 1);
		    TableTags.add(child);
		}
	}
	
	private String StripTableTag(String origStr) {
		int    iNdx;
		String label;
		
    	iNdx = origStr.indexOf(DisclosureStr);
    	if(iNdx == -1)
    		label = origStr;
    	else {
    		label = origStr.substring(DisclosureStr.length() + iNdx+1).trim();
    		iNdx = label.indexOf(TableStr);
    		if(iNdx != -1)
    			label = label.substring(0, iNdx).trim();
    		else {
    			iNdx = label.indexOf(DetailStr);
    			if(iNdx != -1)
    				label = label.substring(0, iNdx).trim();
    		}
    	}
        return(label);
	}
	
	public String FindTableConcept(String tableTag) {
		String  label = "";
		boolean bFound = false;
		int     i = 0;
		String  workStr;
		
		workStr = StripTableTag(tableTag);
		while((bFound == false) && (i < nTC.size())) {
			if(workStr.equals(nTC.get(i).GetNoteTag()))
				bFound = true;
			else
				i++;
		}
		if(bFound) {
		    label = nTC.get(i).GetTableConcept();
		}
		return(label);
	}
}
