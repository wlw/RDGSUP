package com.bricksimpe.rdg.util;

import java.util.ArrayList;

public class CONSTANTS {

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

	
	// these are some Hex values used
	public static final byte UTF_DEF = (byte)0xE2;
	public static final byte UTF_CONT = (byte)0x80; 
	public static final byte UTF_FQUOTE = (byte)0x9D;
	public static final byte UTF_BQOUTE = (byte)0x9C;
	public static final byte UTF_Dash1 = (byte)0x93;
	public static final byte UTF_Dash2 = (byte)0x94;
	
	// replacement chararcters for above
	public static final byte UTF_FQUOTE_REP = (byte)'"';
	public static final byte UTF_BQOUTE_REP = (byte)'"';
	public static final byte UTF_Dash1_REP = (byte)'-';
	public static final byte UTF_Dash2_REP = (byte)'-';
		
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
	
	public String RemoveRemHtmlTags(String origStr) { // static
		String rtnStr = origStr;
		int i;
		
		for(i = 0; i < RemHtmlTags.size(); i++)
		rtnStr = rtnStr.replace(RemHtmlTags.get(i), "");
		return (rtnStr);
	}


}
