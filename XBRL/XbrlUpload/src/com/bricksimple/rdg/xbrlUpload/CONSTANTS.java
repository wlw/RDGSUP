package com.bricksimple.rdg.xbrlUpload;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class CONSTANTS {

	public static final int XBRL_XML = 1;
	public static final int XBRL_XSD = 2;
	public static final int XBRL_CAL = 3;
	public static final int XBRL_DEF = 4;
	public static final int XBRL_LAB = 5;
	public static final int XBRL_PRE = 6;
	public static final int XBRL_HTM = 7;
	
	public static final int UNIT_MEASURE = 0;
	public static final int UNIT_DIVIDE = 1;

	public static final int NO_DETAIL = 0;
	public static final int DEI_DETAIL = 1;
	//public static final int GAAP_DETAIL = 2;
	//public static final int PREFIX_DETAIL = 3;
	public static final int GROUPING_DETAIL = 2;
	
	public static final String DEI_TAG = "dei:";
	public static final String GAAP_TAG = "us-gaap:";
	
	public static final int ENTITY_ID = 1;
	public static final int ENTITY_SEGMENT = 2;
	
	public static final int PERIOD_STARTDATE = 1;
	public static final int PERIOD_ENDDATE = 2;
	public static final int PERIOD_INSTANT = 3;
	
	// this is the sections of the presentation file
	public static final int DEI_TYPE = 1;
	
	// these are the target data types in the SQL files
	public static final int DEI_NOMAP = -1;
	public static final int DEI_BIT = 0;
	public static final int DEI_STRING = 1;
	public static final int DEI_INTEGER = 2;
	public static final int DEI_DATE = 3;
	
	// tags in extracted data
	public static final String DEI_COMPANYNAME = "entityregistrantname";
	public static final String DEI_FORMID = "DOCUMENTTYPE";
	
	
	public static final int INVALID_TEMPLATE_ID = -99;
	
	public static final int NOT_MAPPED = 0;
	public static final int MAPPED = 1;
	
	public static final int GP_NONE = 0;
	public static final int GP_COVER = 1;
	public static final int GP_FINANCIAL = 2;
	public static final int GP_NOTE = 4;
	public static final int GP_NOTE_TBL_DETAIL = -6;  // this is table details
	public static final int GP_NOTE_TABLE = -5;
	public static final int GP_NOTE_DETAIL = -7;
	public static final int GP_POLICIES = -8;
	public static final int GP_DEI_STATEMENTS = -9;
	
	
	public static final int FOOTNOTE = 1;
	public static final int FOOTNOTEARC = 2;
	public static final int FOOTNOTELOC = 3;
	
	public static final boolean memoryDebug = false;
	
	public static final String THREE_MONTHS_END = "Three months ended";
	public static final String SIX_MONTHS_END = "Six months ended";
	public static final String NINE_MONTHS_END = "Nine months ended";
	public static final String TWELVE_MONTHS_END = "Twelve months ended";
	
	public static final int    THREE_MONTHS_NDX = 1;
	public static final int    SIX_MONTHS_NDX = THREE_MONTHS_NDX + 1;
	public static final int    NINE_MONTHS_NDX = SIX_MONTHS_NDX  + 1;
	public static final int    TWELVE_MONTHS_NDX = NINE_MONTHS_NDX + 1;
	
	public static final String UnMappedRowLabel = "";  // was RDGjiffy_
	public static final double THRESHOLD = 0.95;
	
	public static final String BEGINDIMENSION = "TypeAxis";
	public static final String ENDDIMENSION = "TypeDomain";
	public static final String NODIMENSION = "ScenarioAxis";
	
	public static final int CHECKBEGINDIMENSION = 0;
	public static final int BEGINENDDIMENSION = 1;
	public static final int CHECKENDDIMENSION = 2;
	public static final int FOUNDENDDIMENSION = 3;
	public static final int FOUNDNODIMENSION = 4;

    public static final String SECTION_ID = "Statement -";
    public static final String NOTE_ID = "Disclosure -";
    
    public static final String NIL = "Nil";
    public static final List<String> SUBSTITUTIONS = new ArrayList<String>(Arrays.asList("P6Y", "6 years"));
    
    public static final String DEI_STOCK_TAG = "entitycommonstocksharesoutstanding";
    public static final String NODIMENSION_KEY = "dei_EntityCommonStockSharesOutstanding";
    
    public static final String YEAR_DAYS = "P\\d+Y\\d+D";
    
    public static final String StatementLineItems = "StatementLineItems";
    public static final String StatementScenarioAxis = "StatementScenarioAxis";
}
