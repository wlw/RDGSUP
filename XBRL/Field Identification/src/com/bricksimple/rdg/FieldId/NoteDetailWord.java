package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

import java.sql.Connection;
//import com.mysql.jdbc.Connection;

public class NoteDetailWord {
    private ArrayList<String> Conjunctions = new ArrayList<String>();
    private ArrayList<NoteDetailKeyWord> KeyWords    = new ArrayList<NoteDetailKeyWord>();
    private ArrayList<NoteComboList> Combos = new ArrayList<NoteComboList>();
    private ArrayList<String>OmissionWords = new ArrayList<String>();
    
    public void GetNoteDetailItems(Connection con) {
    	MySqlAccess mySql = new MySqlAccess();
    	
        KeyWords = 	mySql.GetNoteDetails(con);
        OmissionWords = mySql.GetOmissionWords(con);
        Conjunctions = mySql.GetNoteConjunctions(con);
        Combos = mySql.GetNoteCombos(con);
    }
    
    public boolean IsConjunction(String myWord) {
    	boolean   bRtn = false;
    	int       iCount = 0;
    	
    	while((bRtn == false) && (iCount < Conjunctions.size())) {
    		if(myWord.equals(Conjunctions.get(iCount)))
    			bRtn = true;
    		iCount++;
    	}
    	return(bRtn);
    }
    
    public boolean IsOmission(String myWord) {
    	boolean   bRtn = false;
    	int       iCount = 0;
    	String    lowMyWord = myWord.toLowerCase();
    	
    	while((bRtn == false) && (iCount < OmissionWords.size())) {
    		if(lowMyWord.equals(OmissionWords.get(iCount)))
    			bRtn = true;
    		iCount++;
    	}
    	return(bRtn);
    }
    
   public boolean DoesWordAppearInKeyWord(String myWord, int iWordPosition) {
    	boolean bFound = false;
    	int     iCounter = 0;
    	
    	while((iCounter < KeyWords.size()) && (bFound == false)) {
    		if(KeyWords.get(iCounter).GetPosition() == iWordPosition) {
    		    if(KeyWords.get(iCounter).GetPlural() == true)
    	    		bFound = myWord.equals(KeyWords.get(iCounter).GetKeyWord() + "s");
    		    if(bFound == false)
    			    bFound = myWord.equals(KeyWords.get(iCounter).GetKeyWord());
    		    if((bFound == false) && (KeyWords.get(iCounter).GetContain() == true))
    			    bFound = myWord.contains(KeyWords.get(iCounter).GetKeyWord());
    		}
    		iCounter++;
    	}
    	return(bFound);
    }
    
    public boolean DoesWordAppearInConJunctions(String myWord) {
    	boolean bFound = false;
    	int     iCounter = 0;
    	
    	while((iCounter < Conjunctions.size()) && (bFound == false)) {
    	    bFound = myWord.equals(Conjunctions.get(iCounter));
    	    iCounter++;
    	}
    	return(bFound);
    }
    
    public int DoesWordAppearInAnyList(String myWord) {
        int     iRtn = 0;
        boolean bRtn = false;
        
        bRtn = DoesWordAppearInConJunctions(myWord);
        if(bRtn == false) {
        	bRtn = DoesWordAppearInKeyWord(myWord, NoteDetailKeyWord.POSTWORD);
        	if(bRtn)
        		iRtn = 2;
        }
        else
        	iRtn = 1;
        return(iRtn);
    }
    
    public int CheckComboList(ArrayList<String> words, int iNdx) {
    	int       iRtn = -1;
    	boolean   bFoundMatch = false;
    	int       iMyIndx = 0;  // combo list index we checking
    	int       iComboWordNdx = 0; // word index into the combo list
    	int       iWordNdx = 0;      // index into the words we checking 
    	String    caseLessWord = "";
    	boolean   bNotComboList = true;
    	CONSTANTS constants = new CONSTANTS();
    	
    	while((bFoundMatch == false) && (iMyIndx < Combos.size())) {
    		iComboWordNdx = 0;   
    		iWordNdx      = 0;
    		bNotComboList = false;
    		while((bFoundMatch == false) && (iComboWordNdx < Combos.get(iMyIndx).words.size()) && (bNotComboList == false)) {
    			if(Combos.get(iMyIndx).words.get(iComboWordNdx).equals("%~")) {  // check for number
    				bNotComboList = constants.DoesWordBeginWithNumber(words.get(iWordNdx));
    			}
    			else {
        			if(Combos.get(iMyIndx).words.get(iComboWordNdx).equals("%$")) {  // check for monies
        				bNotComboList = constants.DoesWordContainMoney(words.get(iWordNdx));
        			}
        			else {
    			        caseLessWord = words.get(iWordNdx).toLowerCase();
    			        if(caseLessWord.equals(Combos.get(iMyIndx).words.get(iComboWordNdx))) {  // this word matches
    				        iComboWordNdx++;
    				        if(iComboWordNdx == Combos.get(iMyIndx).words.size()) { //if at end we done else check next
    					        iRtn = iMyIndx;
    					        bFoundMatch = true;
    				        }
    			        }
    			        else
    			    	    bNotComboList = true;
        			}
    			}
    		}
    		iMyIndx++;   //always bump to next
    	}
    	return(iRtn);
    }
    
    public int GetNoteComboItemLength(int iNdx) {
    	int iRtn = 0;
    	
    	iRtn = Combos.get(iNdx).words.size();
    	return(iRtn);
    }
    
    public void AddPreWords(Connection con, int factUid, ArrayList<FactPreWordList> fpwlList, 
    		                int iFactNdx, MySqlAccess mySqlAccess) {

    	ArrayList<Integer> dummy = new ArrayList<Integer>();
    	
    	dummy.add(factUid);
    	for(int i = 0; i < fpwlList.size(); i++) {
    		if (fpwlList.get(i).WordIndex + 4 >= iFactNdx)
    	    	  mySqlAccess.WriteFactAssociation(con, fpwlList.get(i).PreWord, dummy);
    	}
    }
}
