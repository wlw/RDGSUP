package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

import org.w3c.dom.traversal.NodeIterator;

public class DateInfoInfo {

	public int iCountOfColumns = 0;
    public int iCountOfComDates = 0;
    public int iCountOfParDates = 0;
    public int iCountOfNonSpanSups = 0;
    public int iCountOfSpanSups = 0;
    
    public void GetDateInfo(ArrayList<DateExtract> myArray) {
        for(int i = 0; i < myArray.size(); i++) {
    	    if(myArray.get(i).CompletedDate.length() > 0)
    		    iCountOfComDates++;
    	    if(myArray.get(i).PartialDate.length() > 0)
    		    iCountOfParDates++;
    	    if(myArray.get(i).SupStr.length() > 0) {
    		    if(myArray.get(i).SupSpan == false)
     		        iCountOfNonSpanSups++;
    		    else
    		        iCountOfSpanSups++;
    	    }
        }
        iCountOfColumns = iCountOfComDates;
        if((iCountOfNonSpanSups + iCountOfSpanSups) > iCountOfColumns)
        	iCountOfColumns = iCountOfNonSpanSups + iCountOfSpanSups;
    }
    
    public ArrayList<DateRefOrdered> orderCompletedDate(ArrayList<DateExtract> myArray) {
    	ArrayList<DateRefOrdered> rtnArray = new ArrayList<DateRefOrdered>();
    	ArrayList<Integer>        ordered = new ArrayList<Integer>();  	
    	int                       iInsertAtThisLocation = 0;
    	int                       i;
    	DateRefOrdered            newItem = null;
    	
    	for(i = 0; i < myArray.size(); i++) {
    		if(myArray.get(i).ComTrNdx != 0) {
    			if(ordered.size() == 0)  //first entry
    				ordered.add(i);
    			else {  // insert into list with dups before me
    				for(int k = 0; k < ordered.size(); k++) {	
   					    if(myArray.get(ordered.get(k)).ComTrNdx > myArray.get(i).ComTrNdx)  {
   					    	iInsertAtThisLocation = k;
    						break;
   					    }
   					    iInsertAtThisLocation = k + 1;
    				}
   					if(iInsertAtThisLocation == ordered.size())
   						ordered.add(i);
   					else
   						ordered.add(iInsertAtThisLocation, i);
    			}
    		}  // foot of test for completed text
    	}
        for( i = 0; i < ordered.size(); i++) {
    		newItem = new DateRefOrdered();
    		newItem.CompletedDate = myArray.get(ordered.get(i)).CompletedDate;
    		rtnArray.add(newItem);
    	}
    	return(rtnArray);
    }
    
    public ArrayList<DateRefOrdered> orderWithSupplements(ArrayList<DateExtract> myArray,
    		                                              int iNumofColumns) {
    	ArrayList<DateRefOrdered> rtnArray = new ArrayList<DateRefOrdered>();

    	DateRefOrdered dro = null;
    	
    	boolean bUniteNonSpanSups = true;
    	boolean bAllCompDatesOnSameRow = true;
    	int     i;
    	int     localLine;
    	int     kk; 
    	int     iRowIndex = -1;
    	int     iSupIndex = -1;
    	ArrayList<DateRefOrdered> supsOnHold = new ArrayList<DateRefOrdered>();
    	
    	// first check if completed dates all on same row
    	for(kk =0; kk < myArray.size(); kk++) {
    		if(myArray.get(kk).CompletedDate.length() > 0) {
     			if(iRowIndex == -1)
    				iRowIndex = myArray.get(kk).ComTrNdx;
    			else {
    				if(iRowIndex != myArray.get(kk).ComTrNdx)
    					bAllCompDatesOnSameRow = false;
    			}
    		}
    		else {
    			if(myArray.get(kk).SupStr.length() > 0) {
    				if(iSupIndex == -1)
    					iSupIndex = myArray.get(kk).SupTrNdx;
    				else {
    					if(iSupIndex == myArray.get(kk).SupTrNdx)
    						bUniteNonSpanSups = false;
    				}
    			}
    		}
     	}
    	iRowIndex = iCountOfComDates;
    	if(bUniteNonSpanSups) 
    		iRowIndex += iCountOfNonSpanSups/2;
    	else
    		iRowIndex += iCountOfNonSpanSups;
    	if(iRowIndex == iNumofColumns) {
    		// we got it
    		int iCurSpanNdx = 0;
    		int iCurNonSpanNdx2 = 0;   // this is used for combining  spans
    		int iSpanReuse = 0;   // this is a toggle to use span items twice before moving on
    		int iSpanInWaiting = 0;
    		int iLastCompletedDateRow = 0;
    		
    		int iFind = 0;   // used as local counter
    		boolean bFoundMatch = false;
    		// now move through the columns matching up 
    		for(iRowIndex = 0; iRowIndex < myArray.size(); iRowIndex++) {
    			// first check if sup COLUMN
    			if(myArray.get(iRowIndex).SupTrNdx != 0) {  // this column contains a SUP
    				if(myArray.get(iRowIndex).SupSpan == true) {  // we use it and continue
    					if(iCurSpanNdx == 0) { //check if NOT already using span
    					    iCurSpanNdx = iRowIndex;
    					    iSpanReuse = 0;
    					}
    					else {
    						iSpanInWaiting = iRowIndex;
    					}
    				}
    				else {  // found a sup that does not span
    					if(bUniteNonSpanSups == false) { // if we do not compbine sups make this one
    						dro = new DateRefOrdered();
    						dro.SupStr = myArray.get(iRowIndex).SupStr;
    						rtnArray.add(dro);
    					}
    					else {  // we are combining sups here so find the next one on LOWER row
            				iFind = (iCurNonSpanNdx2 > iRowIndex) ? (iCurNonSpanNdx2+ 1) : (iRowIndex + 1);  // first find starting row
            				bFoundMatch = false;
						    dro = new DateRefOrdered();
						    dro.SupStr = myArray.get(iRowIndex).SupStr;
						    dro.iMyRow = iRowIndex;
            				while((iFind < myArray.size()) && (bFoundMatch == false)) {
            				    if((myArray.get(iFind).SupTrNdx != 0) && // this has sup AND
            				      (myArray.get(iFind).SupTrNdx > myArray.get(iRowIndex).SupTrNdx)) { // this row below me
            				    	bFoundMatch = true;  // set up to exit loop
        						    dro.SupStr = dro.SupStr + " " + myArray.get(iFind).SupStr;
        						    dro.iMyRow = iFind;
            						myArray.get(iFind).SupTrNdx = 0;  // clearing this
            						iCurNonSpanNdx2 = iFind;   // set up for next
            				    }
 						        iFind++;
    					    }  // Now check if we insert here or later
   				    	    if((dro.iMyRow > iLastCompletedDateRow)   && (dro.iMyRow <= iRowIndex) &&
   				    	    		(bFoundMatch == true))// insert here
    						    rtnArray.add(dro);
    						else
    							supsOnHold.add(dro);
    				    }
   			        }	
    			} // END of processing the sup string 
    			// now we process completed dates
    			if(iNumofColumns == iCountOfComDates) {
 					dro = new DateRefOrdered();
					dro.CompletedDate = myArray.get(iRowIndex).CompletedDate;
					dro.SupStr = myArray.get(iRowIndex).SupStr;
					rtnArray.add(dro);
    				while(supsOnHold.size() > 0)
    					supsOnHold.remove(0);
    			}
    			else {
    			    if(myArray.get(iRowIndex).ComTrNdx != 0) { 
    				    String SupStr = "";
    				    if(iCurSpanNdx != 0)  { // we got a span
    					    SupStr = myArray.get(iCurSpanNdx).SupStr;
     					    iSpanReuse++;  // bump span usage
    					    // now check if span completed
    					    if(iSpanReuse > 1) {  // used up set to next
    						    iSpanReuse = 0;
    						    iCurSpanNdx = iSpanInWaiting;
    						    iSpanInWaiting = 0;
    					    }
    				    }
    				    iLastCompletedDateRow = iRowIndex;
   					    dro = new DateRefOrdered();
					    dro.CompletedDate = myArray.get(iRowIndex).CompletedDate;
					    dro.SupStr = SupStr;
					    // first we check if we insert any 
					    while((supsOnHold.size() > 0) && (supsOnHold.get(0).iMyRow < iRowIndex)) {
						    rtnArray.add(supsOnHold.get(0));
						    supsOnHold.remove(0);
					    }
					    rtnArray.add(dro);
    			    }
    			}
    		}  // foot of checking rows - now add any remaining 
    		while(supsOnHold.size() > 0) {
				rtnArray.add(supsOnHold.get(0));
			    supsOnHold.remove(0);
            }
    	}
     	return (rtnArray);
    }
}
