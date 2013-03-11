package com.bricksimple.rdg.FieldId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import java.sql.*;


public class CONSTANTS {

	// string terminators
	public static final ArrayList<String> Connectors = new ArrayList<String>();
	    static {
	    	Connectors.add(",");  // keep in sync with define below
	    	Connectors.add(";");
	    	Connectors.add(":");  
	    }
	public static final Integer Comma_Index = 0;
	public static final String NoteFactTag = " <div id = \"nnnn\" style=\"display:inline;\" title=\"Notefact\">";
	public static final String NoteFactTagNoSpace = "<div id = \"nnnn\" style=\"display:inline;\" title=\"Notefact\">";
	public static final String NoteFactEndTag = "</div>";
	public String SetNoteFactTag(int id, boolean bSpace) {
		String temp = "" + id;
		String rtnStr = "";
		
		if(bSpace)
		    rtnStr = NoteFactTag.replace("nnnn", temp);
		else
			rtnStr = NoteFactTagNoSpace.replace("nnnn", temp);
		return (rtnStr);
	}
	
	public String GetNoteFactEndTag() {
		return(NoteFactEndTag);
	}
	
	public static final String UnMappedRowLabel = "";  // was RDGjiffy_

	public static final ArrayList<String> StockWords = new ArrayList<String>();
	static  {
		StockWords.add(" ");
		StockWords.add("and");
		StockWords.add("or");
		StockWords.add("at");
		StockWords.add("respectively");
		StockWords.add("none");
		StockWords.add("no");
		StockWords.add("issued");
		StockWords.add("outstanding");
		StockWords.add("-");
		StockWords.add("series");
		StockWords.add("shares");
	}
	
	public int FindStockWord(String OrigStr) {  // static
		int iRtn = -1;
		int iCurIndex = 0;
		boolean bContinue = true;
		
		while(bContinue) {
		   if(OrigStr.equals(StockWords.get(iCurIndex))) {
			   bContinue = false;
			   iRtn = iCurIndex;
		   }
		   else {
			   iCurIndex++;
			   if(iCurIndex == StockWords.size())
				   bContinue = false;
		   }
		}
		
		return(iRtn);
	}
	
	public String StripTrailingComma(String origStr) {  // static
		String rtnStr = "";
		int    lenToCopy = origStr.length();
		
		if(origStr.length() > 0) {
		    String temp = origStr.substring(origStr.length() -1);
		    if(temp.equals(","))
		    	lenToCopy--;
		    rtnStr = origStr.substring(0, lenToCopy);
		}
		return(rtnStr);
	}
	
	public ArrayList<String> GetRestOfLine(NodeIterator iterator) {  // static
		
		ArrayList<String> strRtn = new ArrayList<String>();
		boolean bContinue = true;
		String  ElementStr;
		String  CurStr;
		
		while(bContinue) {
			Node n = iterator.nextNode();
			ElementStr = ((Element) n).getTagName();
            //System.out.println("ELEMENT: " + ElementStr);
            if(ElementStr.equals("tr"))
            	bContinue = false;
            else {
                NodeList fstNm = n.getChildNodes();
                if(fstNm.item(0) != null) {
                	CurStr = (fstNm.item(0)).getNodeValue();
                    if(CurStr == null) {
                        //System.out.println("No data at node");
                        strRtn.add("");
                    }
                    else {
                    	//System.out.println("Stock DATA:" + CurStr);
                    	strRtn.add(CurStr);
                    }
                }
            }
		}
		
		return (strRtn);
	}
	
	// matching threshold
	public static final double THRESHOLD = 0.95;
	
	// stock defines
	public static final Integer Stock_None = 0;
	public static final Integer Stock_Preferred = 1;
	public static final Integer Stock_Common = 2;
	public static final String  Stock_PreferredStr = "preferred stock";
	public static final String  Stock_CommonStr = "common stock";
	
    public static final Integer NO_DIMEMSION = 1;
    public static final Integer COMMONCLASSA = 2;
    public static final Integer COMMONCLASSB = 3;
    public static final Integer NONVOTING = 4;
    public static final Integer CONVERTIBLE = 5;
    public static final Integer PREFERRED = 6;
    
   public static final Integer MIN_NOTE_SENTENCE_LEN = 35;
   
	public boolean IsCharacterConnector(String OrigStr) {  // static
		boolean bRtn = false;
		int     i = 0;
		boolean bContinue = true;
		
		while(bContinue) {
			if(i == Connectors.size())
				bContinue = false;
			else {
				if(OrigStr.equals(Connectors.get(i))) {
					bRtn = true;
					bContinue = false;
				}
				else
					i++;
			}
		}
		
		return(bRtn);
	}
	
	public String TrimString(String OrigStr) {  // static
		String RtnStr = "";
		int i = 0;
		
		RtnStr = OrigStr.trim();
		for(i= 0; i < Connectors.size(); i++) {
		    if(RtnStr.indexOf(Connectors.get(i)) == 0) {
		        RtnStr = RtnStr.substring(1);  // remove the puncuation mark
				RtnStr = RtnStr.trim();        // trim again
		        i = -1;  // start scanning again from the start
		    }
		}
		return(RtnStr);
	}
	
	public int FindPunctuation(String OrigStr) {  // static
	   int iRtn = 256;
	   int i = 0;
	   int iCur = 256;
	   
	   for(i = 0; i < Connectors.size(); i++) {
		   iCur = OrigStr.indexOf(Connectors.get(i));
		   if(iCur != -1) {
			   if(iCur < iRtn)
				   iRtn = iCur;
		   }
	   }
	   return(iRtn);
	}
	
	public int FindAuthorizedShares(String OrigStr) {  // static
		int iRtn = -1;
		
		iRtn = OrigStr.indexOf("authorized shares");
		if(iRtn == -1)
			iRtn = OrigStr.indexOf("shares authorized");
		if(iRtn != -1)
			iRtn += "shares authorized".length();
		return (iRtn);
	}
	
	
	public static final ArrayList<String> MonthName = new ArrayList<String>();
	static  {
		MonthName.add("january");
		MonthName.add("february");
		MonthName.add("march");
		MonthName.add("april");
		MonthName.add("may");
		MonthName.add("june");
		MonthName.add("july");
		MonthName.add("august");
		MonthName.add("september");
		MonthName.add("october");
		MonthName.add("november");
		MonthName.add("december");
	}

	public static final ArrayList<String> AbrevMonth = new ArrayList<String>();
	static {
		AbrevMonth.add("jan");
		AbrevMonth.add("feb");
		AbrevMonth.add("mar");
		AbrevMonth.add("apr");
		AbrevMonth.add("may");
		AbrevMonth.add("june");
		AbrevMonth.add("july");
		AbrevMonth.add("aug");
		AbrevMonth.add("sept");
		AbrevMonth.add("oct");
		AbrevMonth.add("nov");
		AbrevMonth.add("dec");
}
	public static final ArrayList<Integer> DaysInMonth = new ArrayList<Integer>();
	static  {
		DaysInMonth.add(31);  //Jan
		DaysInMonth.add(28);  //Feb
		DaysInMonth.add(31);  //Mar
		DaysInMonth.add(30);  //Apr
		DaysInMonth.add(31);  //May
		DaysInMonth.add(30);  //Jun
		DaysInMonth.add(31);  //Jul
		DaysInMonth.add(31);  //Aug
		DaysInMonth.add(30);  //Sep
		DaysInMonth.add(31);  //Oct
		DaysInMonth.add(30);  //Nov
		DaysInMonth.add(31);  //Dec
	}
	
	public String GetDisplayMonth(int iMonthNdx) {  // static
		String rtnStr = "";
		
		String WorkingStr = MonthName.get(iMonthNdx);
		rtnStr = WorkingStr.substring(0,1).toUpperCase() + WorkingStr.substring(1);
	    return(rtnStr);
	}
	
	public int GetFirstMonthContained(String loweredOrig) {  // static
		int   iRtn = -1;
		int   iNdx;
		int   iMinNdx = 12;
		int   iMinOffset = loweredOrig.length();
		
		for(int j = 0; j < MonthName.size(); j++) {
			iNdx  = loweredOrig.indexOf(MonthName.get(j));
			if(iNdx != -1) {
				iNdx = EnsureDate(loweredOrig, iNdx, MonthName.get(j).length());
				if((iNdx < iMinOffset) && (iNdx != -1)) {
					iMinNdx = j;
					iMinOffset = iNdx;
				}
			}
		}
		if(iMinOffset == loweredOrig.length()) {  // must be abbrev
			for(int j = 0; j < AbrevMonth.size(); j++) {
				iNdx  = loweredOrig.indexOf(AbrevMonth.get(j));
				if(iNdx != -1) {
					iNdx = EnsureDate(loweredOrig, iNdx, AbrevMonth.get(j).length());
					if((iNdx < iMinOffset) && (iNdx != -1)) {
						iMinNdx = j;
						iMinOffset = iNdx;
					}
				}
			}
		}
		if(iMinNdx < 12)
			iRtn = iMinNdx;
		return(iRtn);
	}
	
	private int EnsureDate(String origStr, int iCurNdx, int matchLen) {  // static
		int    iRtn = -1;   // Assume miss
		String extract = "";
		int    tempInt;
		
		// Must have at least length
		if(origStr.length() > (matchLen + iCurNdx +2)) {
			extract = origStr.substring((matchLen+iCurNdx), (matchLen+iCurNdx+1));
			if(extract.contains(" "))  // must be space otherwise they forgot the space
			    extract = origStr.substring((matchLen+iCurNdx+1), (matchLen+iCurNdx+2)).trim();  
			try {
				if(extract.length() > 0)
				    tempInt = Integer.parseInt(extract);
				else {
					extract = origStr.substring((origStr.length() -1), origStr.length());
					if(extract.equals(",") == false)
							iRtn = -1;
				}	
				iRtn = iCurNdx;   // if we get here good indication this is a date
			}
			catch(Exception e) {
				return(iRtn);
			}
		}	
		return(iRtn);
	}
	
	public int MonthBegin(String origStr) {
		int      iRtn = -1;
		String   testStr = origStr.toLowerCase();
		int      i = 0;
		
		while((iRtn == -1) && (i < MonthName.size())) {
			iRtn = testStr.indexOf(MonthName.get(i));  // see if we find it
			i++;
		}
		return(iRtn);
	}

		
	public int GetBeginIndexOfMonth(String OrigStr, int iMonth) {  // static
		int iRtn = 0;
		
		iRtn = OrigStr.indexOf(MonthName.get(iMonth));
		if(iRtn == -1)
			iRtn = OrigStr.indexOf(AbrevMonth.get(iMonth));
		return(iRtn);
	}
	
	public int GetLengthOfMonth(String OrigStr, int iMonth) {  // static
		int iRtn = 0;
		
		iRtn = OrigStr.indexOf(MonthName.get(iMonth));
		if(iRtn != -1)
			iRtn = MonthName.get(iMonth).length();
		else {
			iRtn = AbrevMonth.get(iMonth).length();
		}
		return(iRtn);
	}
	
	public boolean DoesStrContainMonth(String OrigStr) {  // static
		boolean bRtn = false;
		String  CheckStr = OrigStr.toLowerCase();
		
		int i = 0;
		CheckStr = CheckStr.trim();
		while((bRtn == false) && (i < MonthName.size())) {
			if(CheckStr.indexOf(MonthName.get(i)) == 0)
				bRtn = true;
			i++;
		}
		return(bRtn);
	}
	                    
	public boolean DoesStrContainWithinMonth(String OrigStr) {  // static
		boolean bRtn = false;
		String  CheckStr = OrigStr.toLowerCase();
		
		int i = 0;
		CheckStr = CheckStr.trim();
		while((bRtn == false) && (i < MonthName.size())) {
			if(CheckStr.indexOf(MonthName.get(i)) != -1)
				bRtn = true;
			i++;
		}
		return(bRtn);
	}
	
	public boolean MonthInString(String OrigStr) {  // static
		boolean bRtn = false;
		
		String  CheckStr = OrigStr.toLowerCase();
		
		int i = 0;
		CheckStr = CheckStr.trim();
		while((bRtn == false) && (i < MonthName.size())) {
			if(CheckStr.indexOf(MonthName.get(i)) >= 0)
				bRtn = true;
			i++;
		}
		i = 0;
		while((bRtn == false) && (i < AbrevMonth.size())) {
			if(CheckStr.indexOf(AbrevMonth.get(i)) == 0)
				bRtn = true;
			i++;
		}
		return(bRtn);
	}
	
	public int ReturnMonthIndex(String OrigStr) {  // static
		int   iRtn = -1;
		
		String  CheckStr = OrigStr.toLowerCase();
		
		int i = 0;
		CheckStr = CheckStr.trim();
		while((iRtn == -1) && (i < MonthName.size())) {
			iRtn  = CheckStr.indexOf(MonthName.get(i));
			i++;
		}	
		if(iRtn == 0)
			iRtn = i;
		return(iRtn);
	}
	
	public static final ArrayList<String> Numbers = new ArrayList<String>();
	static  {
		Numbers.add("0");
		Numbers.add("1");
		Numbers.add("2");
		Numbers.add("3");
		Numbers.add("4");
		Numbers.add("5");
		Numbers.add("6");
		Numbers.add("7");
		Numbers.add("8");
		Numbers.add("9");
	}
	
	public  boolean DoesWordBeginWithNumber(String OrigStr) {  // static
		boolean bRtn = false;
		int     i = 0;
		String  workingStr = "";
		int     iNdx = 0;
		
		// this is to skip by leading parens
		if(OrigStr.indexOf("(") == 0)
			iNdx = 1;
		workingStr = OrigStr.substring(iNdx);
		while((bRtn == false) && (i < Numbers.size())) {
			if(workingStr.indexOf(Numbers.get(i)) == 0)
				bRtn = true;
			i++;
		}
		return(bRtn);
	}
	
	public boolean DoesWordContainMoney(String OrigStr) {
	    boolean bRtn = false;
	    String  temp = "";
	    
	    temp = OrigStr.substring(0,1);
	    if(temp.equals("$")) {
	        temp = OrigStr.substring(1).trim();  // remove the dollar sign
	        bRtn = DoesWordBeginWithNumber(temp);
	    }
	    return(bRtn);
	}
	
	public boolean DoesWordBeginWithDollarSign(String OrigStr) {
		boolean bRtn = false;
		
		if(OrigStr.indexOf("$") == 0)
			bRtn = true;
		return(bRtn);
	}
	
	public static final String HYP_PATTERN = "\\d+-\\d+";
	
	public static final int NOTE_CURRENCY = 1;
	public static final int NOTE_NUMBER = 2;
	public static final int NOTE_SHARES = 3;
	public static final int NOTE_DATE = 4;
	
	
	public static final String StartOfTable =  "<table>"; 
	public static final String EndOfTblOfCont = "</table>"; 
	public static final String BeginRow = "<tr>";
	public static final String EndRow = "</tr>";
	public static final String BeginCell = "<td>";
	public static final String EndCell = "</td>";
	public static final String StartOfTableUC =  "<TABLE>";  // case 0
	public static final String EndOfTblOfContUC = "</TABLE>";  //case 1
	public static final String BeginRowUC = "<TR>";
	public static final String EndRowUC = "</TR>";
	public static final String BeginCellUC = "<TD>";
	public static final String EndCellUC = "</TD>";

	private static final ArrayList<String> RemHtmlTags = new ArrayList<String>();
	static {
		RemHtmlTags.add(CONSTANTS.StartOfTable);
		RemHtmlTags.add(CONSTANTS.EndOfTblOfCont);
		RemHtmlTags.add(CONSTANTS.BeginRow);
		RemHtmlTags.add(CONSTANTS.EndRow);
		RemHtmlTags.add(CONSTANTS.BeginCell);
		RemHtmlTags.add(CONSTANTS.EndCell);
		RemHtmlTags.add(CONSTANTS.StartOfTableUC);
		RemHtmlTags.add(CONSTANTS.EndOfTblOfContUC);
		RemHtmlTags.add(CONSTANTS.BeginRowUC);
		RemHtmlTags.add(CONSTANTS.EndRowUC);
		RemHtmlTags.add(CONSTANTS.BeginCellUC);
		RemHtmlTags.add(CONSTANTS.EndCellUC);
	}
	
	public String RemoveRemHtmlTags(String origStr) {  // static
		String rtnStr = origStr;
		int i;
		
		rtnStr = rtnStr.replace(EndCell, " "); // this is here so successive fields are not congugated
		rtnStr = rtnStr.replace(EndCellUC, " ");
		for(i = 0; i < RemHtmlTags.size(); i++)
		rtnStr = rtnStr.replace(RemHtmlTags.get(i), "");
		rtnStr = rtnStr.trim();
		return (rtnStr);
	}
	
	public int CheckForBeginTable(String TestStr) {  // static
		int iRtn = -1;
		
		iRtn = TestStr.indexOf(CONSTANTS.StartOfTable);
		if(iRtn == -1)
			iRtn = TestStr.indexOf(CONSTANTS.StartOfTableUC);
		return(iRtn);
	}
	
	public int CheckForEndTable(String TestStr) {  // static
		int iRtn = -1;
		
		iRtn = TestStr.indexOf(CONSTANTS.EndOfTblOfCont);
		if(iRtn == -1)
			iRtn = TestStr.indexOf(CONSTANTS.EndOfTblOfContUC);
		return(iRtn);
	}
	
	public int CheckForBeginCell(String TestStr) {  // static
		int iRtn = -1;
		
		iRtn = TestStr.indexOf(CONSTANTS.BeginCell);
		if(iRtn == -1)
			iRtn = TestStr.indexOf(CONSTANTS.BeginCellUC);
		return(iRtn);
	}
	
	public boolean EqualsEndTable(String TestStr) {  // static
		boolean bRtn = false;
		
		bRtn = TestStr.equals(CONSTANTS.EndOfTblOfCont);
		if(bRtn == false)
			bRtn = TestStr.equals(CONSTANTS.EndOfTblOfContUC);
		return(bRtn);
	}
	public static final ArrayList<String> TextNumbers = new ArrayList<String>();
	private static final ArrayList<Integer> TextValues = new ArrayList<Integer>();
	
	private static final ArrayList<String> TensTextNumbers = new ArrayList<String>();
	private static final ArrayList<Integer> TensTextValues = new ArrayList<Integer>();
	//static {
	//	TensTextNumbers.add("twenty");
	//	TensTextNumbers.add("thirty");
	//}
	
	public static final Integer UNITS_CONVERSION = 0;
	public static final Integer TENS_CONVERSION = 1;

	public void PopulateTextConversionTable(Connection con) {  // static
		ArrayList<TextConversionCls> tableEntries = null;
		MySqlAccess                  mySqlAccess = new MySqlAccess();
		
		if(TextValues.size() == 0) {
		    tableEntries = mySqlAccess.GetTableEntries(con, UNITS_CONVERSION);
		    for (TextConversionCls curItem : tableEntries) {
			    TextNumbers.add(curItem.getTextString());
			    TextValues.add(curItem.getConversionValue());
		    }
		    tableEntries = mySqlAccess.GetTableEntries(con, TENS_CONVERSION);
		    for (TextConversionCls curItem : tableEntries) {
			    TensTextNumbers.add(curItem.getTextString());
			    TensTextValues.add(curItem.getConversionValue());
		    }
		}
	}
	
	//static {
	//	TensTextValues.add(20);
	//	TensTextValues.add(30);
	//}
	
	public int GetNumberOfMonths(String origStr) {  // static
		int iRtn = 0;
		
		int    i = 0;
		String CheckStr = origStr.toLowerCase();
		int    iTensValue = 0;
		
		while((iTensValue == 0) && (i < TensTextNumbers.size())) {
			if(CheckStr.contains(TensTextNumbers.get(i))) 
				iTensValue = TensTextValues.get(i);
			i++;
		}
		i = 0;
		while((iRtn == 0) && (i < TextNumbers.size())) {
			if(CheckStr.contains(TextNumbers.get(i)))
				iRtn = TextValues.get(i);
			i++;
		}
		iRtn += iTensValue;  // add the tens
	return(iRtn);
	}
	
	private static final String[] INCEPTIONSTR = {"inception", "cumulative"};
	
	public int IsInception(String SupStr) {  // static
		int iRtn = -1;
		int i    = 0;
		
		String  loweredSupStr = SupStr.toLowerCase();
		while((iRtn == -1) && ( i < INCEPTIONSTR.length)) {
			if(loweredSupStr.indexOf(INCEPTIONSTR[i]) != -1)
				iRtn = INCEPTIONSTR[i].length();
			else
				i++;
		}
		//if(loweredSupStr.indexOf("inception") != -1) || (loweredSupStr.indexOf("cumulative") != -1))
		//	bRtn = true;
		return(iRtn);
	}
	
	public int ContainsDOMbegin(String curLine) {  // static
		int iRtn = 0;
		
		iRtn = curLine.indexOf(StartOfTable);
		return(iRtn);
	}
	
	public String GetPriorDate(String FinalDate, String SupStr) {  // static
		String                     rtnStr = "";
		String                     lowerSup = SupStr.toLowerCase();
		int                        duration = 0;
		int                        interval = 0;
		String                     DATE_FORMAT = "MMM dd,yyyy";
		SimpleDateFormat           sdf = new SimpleDateFormat(DATE_FORMAT);
		Calendar                   c1 = Calendar.getInstance();
		Date                       date;
		InceptionExtract            inceptionExtract = new InceptionExtract();
		
		try {
		    if((IsThisADate(FinalDate) == true) && (SupStr.length() > 0)) {
			    duration = GetDuration(lowerSup);
		        interval = -GetInterval(lowerSup);  // negate
			    switch (duration) {
			        case 0:   // not an interval 
				        break;
				    
			        case 1:   // MONTHS
	            	    int    i;
		        	    FinalDate = FinalDate.trim();
		        	    i = FinalDate.indexOf(" ");
			            String monthStr = FinalDate.substring(0,i);
			       	    i = FinalDate.indexOf(",");
			       	    int year = Integer.parseInt(FinalDate.substring(i+1).trim());
			       	    i = ReturnMonthIndex(monthStr);
			       	    i = i + interval;
			       	    if ( i < 0) {
			       		    i += 11;
			       		    year--;
			       	    }
		       		    monthStr = MonthName.get(i);
		       		    rtnStr = monthStr.substring(0,1).toUpperCase() + 
		       		             monthStr.substring(1) + " 1," + year;
		    	        break;

			        case 2:   // WEEKS
			   	        date = (Date)sdf.parse(FinalDate);
			   	        c1.setTime(date);
		    	        c1.add(Calendar.DATE, (interval * 7));
		    	        c1.add(Calendar.DATE, 1);
		    	        rtnStr = sdf.format(c1.getTime());
			            break;
               
		            case 3:    // Year to date
			            int iIndx = FinalDate.indexOf(",");
			       	    if(iIndx != -1)
			       		    rtnStr = "Jan 01" + FinalDate.substring(iIndx);
			       	    break;
			    }
		    }
		}
		catch (Exception e) {
			//ErrorCls errorCls = new ErrorCls();
			
			return(rtnStr);
		}
		return(rtnStr);
	}
	
	private int GetDuration(String supStr) {  // static
		int iRtn = 0;
		
		if(supStr.indexOf("months ended") != -1) 
				iRtn = 1;
		else {
			if(supStr.indexOf("weeks ended") != -1)
				iRtn = 2;
			else {
				if(supStr.indexOf("year to date") != -1)
					iRtn = 3;
			}
		}			
		return(iRtn);
	}
	
	private int GetInterval(String supStr) {  // static
		int     iRtn;
		int     iNdx;
		String  curWord = "";
		boolean bContinue = true;
		
		iRtn = GetNumberOfMonths(supStr);
		if(iRtn == 0) { // must be a number so find it
		    while(bContinue == true) {
		    	if(supStr.length() == 0)
		    		bContinue = false;
		    	else {
		            iNdx = supStr.indexOf(" ");
		            if(iNdx == -1)  {
		            	curWord = supStr;
		            	supStr = "";
		            }
		            else {
		            	curWord = supStr.substring(0, iNdx).trim();
		            	supStr = supStr.substring(iNdx).trim();
		            }
		            iNdx = isNumeric(curWord);
		            if(iNdx != -1) {
		            	iRtn = iNdx;
		            	bContinue = false;
		            }
		    	}
		    }
		}
		return(iRtn);
	}
	
	public int ConvertMonthStringToNum(String OrigStr) {  // static
		int     iRtn = 0;
		String  CheckStr = OrigStr.toLowerCase();
		
		int i = 0;
		while((iRtn == 0) && (i < MonthName.size())) {
			if(CheckStr.contains(MonthName.get(i)))
				iRtn = i + 1;
			i++;
		}
		return(iRtn);
	}

	public  String RemoveMonth(String OrigStr) {  // static
		String   rtnStr = "";
		int      iIndex;
		String   Temp = OrigStr.toLowerCase();
		
		iIndex = ConvertMonthStringToNum(OrigStr);
		rtnStr = Temp.replace(MonthName.get(iIndex -1), "");
		return(rtnStr);
	}

	public String getMonthStr(int iMonth) {  // static
		String rtnStr = "";
		
		rtnStr = MonthName.get(iMonth -1);
		String firstChar = rtnStr.substring(0,1);
		rtnStr = rtnStr.substring(1);
		firstChar = firstChar.toUpperCase();
		rtnStr = firstChar + rtnStr;
		return (rtnStr);
	}
	
	public String ExtractDecimalNumber(String origStr) {  // static
		String  rtnStr = "";
		int     i = 0;
		boolean bFoundEndOfDecimal = false;
		String  testStr = "";
		
		while((bFoundEndOfDecimal == false) && ( i < origStr.length()))  {
			if((i+1) < origStr.length())
			    testStr = origStr.substring(i,(i+1));
			else
				testStr = origStr.substring(i);
			if(testStr.indexOf(".") != -1)
				rtnStr = rtnStr + testStr;
			else {
			    if(isNumeric(testStr) != -1)
			    	rtnStr = rtnStr + testStr;
			    else
			    	bFoundEndOfDecimal = true;
			}
			i++;
		}
		return(rtnStr);
	}

	public int isNumeric(String testStr)  // static
	{
		int iRtn = -1;
		int iIndex;
		String Temp;
		
		try {
			iIndex = testStr.indexOf(" ");
			if(iIndex == -1)
				Temp = testStr;
			else
				Temp = testStr.substring(0,iIndex);
			iRtn = Integer.parseInt(Temp);
		}
		catch(Exception e) {
		    return(iRtn);
		}
		return(iRtn);
	}
	
	public boolean IsThisADate(String testStr){  // static
		boolean bRtn = false;
		
    	DateFormat formatter;
    	Date       ts;
    	
    	testStr = testStr.trim();
    	formatter = new SimpleDateFormat("MMM dd,yyyy");
    	try {
    		    ts = (Date)formatter.parse(testStr);
    		    bRtn = true;
    		}
    		catch (Exception e) {
    			return(false);
    		}
		
		return(bRtn);
	}
	
	public double IsThisDecimalMoney(String testStr) {  // static
		double dbRtn = 0.0;
		
		try {
			dbRtn = Double.parseDouble(testStr);
		}
		catch (Exception e) {
			return(0.0);
		}
		return(dbRtn);
	}
	
	public CheckConceptRtn CheckForConcepts(String NodeData, FieldMatchStr[] tblMatchArray ) {
		CheckConceptRtn    checkConceptRtn = new CheckConceptRtn();;
		int                j = 0;
		double             dConfidence = 0;
        ConfidenceLevel    conLevel = new ConfidenceLevel();
		
        if(tblMatchArray != null) {
		    while(j < tblMatchArray.length) {
			    dConfidence = conLevel.compareToArrayList(NodeData, tblMatchArray[j].Al, tblMatchArray[j].getFieldStr());
			    if(dConfidence >= tblMatchArray[j].getThreshold()) {
				    if(dConfidence > checkConceptRtn.dConfidence) {
					    checkConceptRtn.dConfidence = dConfidence;
					    checkConceptRtn.iMatchIndex = j;
				    }
			    }
			    j++;
		    }
        }
		return(checkConceptRtn);
	}
	
	public ArrayList<String> ListOfWords(String CurText) { //static
		ArrayList<String> rtnArray = new ArrayList<String>();
		int               iLen;
		String            aWord;
		
 	    while(CurText.length() > 0) {
 	    	iLen = CurText.indexOf(" ");
 	    	if(iLen == -1) { // no spaces just a word left
 	    		rtnArray.add(CurText);
 	    		CurText = "";    // empty
 	    	}
 	    	else {
 	    		aWord = CurText.substring(0, iLen);
 	    		rtnArray.add(aWord);
 	    		CurText = CurText.substring(iLen);
 	    		CurText = CurText.trim();
 	    	}
 	    }	

		return (rtnArray);
	}
	

}
