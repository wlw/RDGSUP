package com.bricksimpe.rdg.XbrlTemplateId;

import java.util.ArrayList;

public class CONSTANTS {

	public static final String StartOfTable =  "<table>";  // case 0
	public static final String StartOfTableUC =  "<TABLE>";  // case 0
	public static final String EndOfTblOfCont = "</table>";  //case 1
	public static final String FoundTemplate = "Template";
	public static final String BeginRow = "<tr>";
	public static final String EndRow = "</tr>";
	public static final String BeginCell = "<td>";
	public static final String EndCell = "</td>";
	public static final String BeginRowUC = "<TR>";
	public static final String EndRowUC = "</TR>";
	public static final String BeginCellUC = "<TD>";
	public static final String EndCellUC = "</TD>";
	
	public static final int PreambleId = -2;
	public static final int TableOfContentsId = -1;
	public static final int DummySection = -3;
	public static final int MINIMUMPREAMBLELINES = 10;   // minimum size of the preamble
	
	public static final String UserDefinedStartTemplate = "<rdgtemplate"; // "<RDGTemplate";
	public static final String FinancialScheduleBegin = "RDGScheduleParseBegin";
	public static final String FinancialScheduleEnd = "RDGScheduleParseEnd";
	public static final String UserDefinedIdTag = "id=\"";
	public static final String UserDefinedDisplayTag = "displayname=\"";
	public static final String UserDefinedNoteTag = "noteindex=\"";
	
	public static final String FalseIdentifier = "accompanying notes";
	public static final String FalseIdentifier2 = "notes to";
	
	public static final int I_MAX_LINES_TO_DISCARD_TABLE = 8;
	
	//this is the minimum sections that MUST be found to complete a filing
	public static final int[] MIN_SECTION_IDS = {1,2,3,5};
	public static final int[] SECTION_INDEX = {0,1,2,1};
	public static final int   NUM_MIN_SECTIONS = 3;
	public static final String[] MIN_SECTION_STRS = {"Balance Sheets", "Income Statements", "Cash Flow"};
	// When doing K forms this is the number of lines to check on table before ignoring
	//       the rest of the table
	public static final int MAX_LINES_OFF_START = 15;  
	
	public static final String UserDefinedEndTemplate = "</rdgtemplate>"; //"</RDGTemplate>";
	public int CheckForIdentifier(String curLine, int iStringId) {  //static
		int    iRtn = -1;   // this is the not found condition
		String lowerCased = curLine.toLowerCase();
		switch (iStringId) {
		    case 0:
		    	iRtn = lowerCased.indexOf(CONSTANTS.StartOfTable);
			    break;
		    case 1:
		    	iRtn = lowerCased.indexOf(CONSTANTS.EndOfTblOfCont);
			    break;
		}
		return(iRtn);
	}
	
	//this removes superflurish text 
	
	public String RemoveLineJunk(String origStr) {  //static
	    String rtnStr = "";
		
	    rtnStr = origStr.replace("<table></table>", "");
	    return(rtnStr);
	}
	
	
    public String RemoveHtmlCodeNoCase(String strLine) {   //static
    	String extracted = strLine;
    	
        extracted = extracted.replace(BeginRow, "");
        extracted = extracted.replace(StartOfTable, "");
        extracted = extracted.replace(BeginCell, "");
        extracted = extracted.replace(EndRow, "");
        extracted = extracted.replace(EndCell, "");     
        extracted = extracted.replace(BeginRowUC, "");
        extracted = extracted.replace(StartOfTableUC, "");
        extracted = extracted.replace(BeginCellUC, "");
        extracted = extracted.replace(EndRowUC, "");
        extracted = extracted.replace(EndCellUC, "");
        return(extracted);
    }

    public String RemoveHtmlCode(String strLine) {   //static
    	String extracted = strLine.toLowerCase();
    	
        extracted = extracted.replace(BeginRow, "");
        extracted = extracted.replace(StartOfTable, "");
        extracted = extracted.replace(BeginCell, "");
        extracted = extracted.replace(EndRow, "");
        extracted = extracted.replace(EndCell, "");
        return(extracted);
    }
	
    public String RemoveHtmlCodeSaveCase(String strLine) {  //static
    	String extracted = "";
    	
        extracted = strLine.replace(BeginRow, "");
        extracted = extracted.replace(StartOfTable, "");
        extracted = extracted.replace(BeginCell, "");
        extracted = extracted.replace(EndRow, "");
        extracted = extracted.replace(EndCell, "");
        extracted = extracted.replace(BeginRowUC, "");
        extracted = extracted.replace(StartOfTableUC, "");
        extracted = extracted.replace(BeginCellUC, "");
        extracted = extracted.replace(EndRowUC, "");
        extracted = extracted.replace(EndCellUC, "");
        return(extracted);
    }
	
	public int isNumeric(String testStr)  //static
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
	
	public String RemoveTableTags(String curLine) {  //static
		String rtnStr = curLine.replace(CONSTANTS.StartOfTable.toUpperCase(), "");
		rtnStr = rtnStr.replace(CONSTANTS.BeginRow.toUpperCase(), "");
		rtnStr  = rtnStr.replace(CONSTANTS.BeginCell.toUpperCase(), "");
		rtnStr = rtnStr.replace(CONSTANTS.EndCell.toUpperCase(), "");
		return(rtnStr);
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

	public boolean DoesStrContainMonth(String OrigStr) {  // static
		boolean bRtn = false;
		
		int i = 0;
 
		while((bRtn == false) && (i < MonthName.size())) {
			if(OrigStr.contains(MonthName.get(i)) == true)
				bRtn = true;
			i++;
		}
		return(bRtn);
	}
	
	
	public static final ArrayList<String> Modifiers = new ArrayList<String>();
	static  {
		Modifiers.add("in thousands");
		Modifiers.add("unaudited");
		Modifiers.add("expressed in united states dollars");
		Modifiers.add("<table></table>");
	}
	
	public boolean DoesStrContainModifier(String origStr) {
		boolean bRtn = false;
		
		int i = 0;
		 
		while((bRtn == false) && (i < Modifiers.size())) {
			if(origStr.contains(Modifiers.get(i)) == true)
				bRtn = true;
			i++;
		}
		return(bRtn);
	}
}
