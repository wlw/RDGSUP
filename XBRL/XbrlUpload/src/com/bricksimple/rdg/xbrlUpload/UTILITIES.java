package com.bricksimple.rdg.xbrlUpload;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

import com.bricksimple.rdg.sqlaccess.MySqlAccess;

public class UTILITIES {

	public void MemoryUsage(String location) {
	    NumberFormat format = NumberFormat.getInstance();

	    if(CONSTANTS.memoryDebug == true) {
		    Runtime runtime = Runtime.getRuntime();
	        StringBuilder sb = new StringBuilder();
	        long maxMemory = runtime.maxMemory();
	        long allocatedMemory = runtime.totalMemory();
	        long freeMemory = runtime.freeMemory();

	        sb.append("Loc:" + location + ":: ");
	        sb.append("free memory: " + format.format(freeMemory / 1024) + "K:: ");
	        sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "K:: ");
	        sb.append("max memory: " + format.format(maxMemory / 1024) + "K:: ");
	        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "K::");
            System.out.println(sb.toString());
	    }
	}
	
	// WLW to enhance
	public String convertFact(String orig) {
	    String rtnStr = orig;
	    int    i;
	    float  f = 0;
	    int    years = 0;
	    int    days = 0;
	    String parsed = "";
	    String extract = "";
	    float  divisor = 365f;
	    
	    if(orig.matches(CONSTANTS.YEAR_DAYS)) {
	    	parsed = orig.substring(1);  // remove the 'P'
	    	i = parsed.indexOf("Y");
	    	extract = parsed.substring(0,i);
	    	years = Integer.parseInt(extract);
	    	extract = parsed.substring(i+1);   // strip Y
	    	i = extract.indexOf("D");
	    	extract = extract.substring(0, i);
	    	days = Integer.parseInt(extract);
	    	f = (days/divisor) + years;
	    	rtnStr = "" + f;
	    	
	    }
	    return(rtnStr);
	}
	
	public int ConvertStringToInt(Connection con, String toConvert) {
		int iRtn = 0;
		ErrorCls errorCls = new ErrorCls();
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		try {
			iRtn = Integer.parseInt(toConvert);
			if(iRtn != 0) {
				if(mySqlAccess.DoesCompanyExist(con, iRtn) == false) {
					errorCls.setFunctionStr("ConvertStringToInt");
					errorCls.setCompanyUid(0);
					errorCls.setItemVersion(0);
					errorCls.setSubUid(0);
					errorCls.setErrorText("Invalid companyUid: " + toConvert + ": does not exist");
					mySqlAccess.WriteAppError(con, errorCls);		
				}
			}
		}
		catch (Exception e) {
			errorCls.setFunctionStr("ConvertStringToInt");
			errorCls.setCompanyUid(0);
			errorCls.setItemVersion(0);
			errorCls.setSubUid(0);
			errorCls.setErrorText("Invalid companyUid: " + toConvert + " : conversion");
			mySqlAccess.WriteAppError(con, errorCls);		
		}
		return(iRtn);
	}
	
	public String DoSubstitutions(String orig) {
		String rtnStr = orig;
		int    i = 0;
		boolean bReplaced = false;
		
		while((i < CONSTANTS.SUBSTITUTIONS.size()) && (bReplaced == false)) {
			if(orig.equals(CONSTANTS.SUBSTITUTIONS.get(i))) {
				rtnStr = CONSTANTS.SUBSTITUTIONS.get(i+1);
				bReplaced = true;
			}
			else
				i += 2;
		}
		
		
		return(rtnStr);
	}
	public static long emptyReferenceIdentifier( )
	  {
	    return 0;
	  }
	  
	  public String hashableString( String string )
	  {
		    String hashableString = new String( string );
		    
		    // Replace all numbers that aren't enclosed in letters:
		    hashableString = hashableString.replaceAll( "\\b(?<![\\w'])\\d+\\b", "" );
		    
		    // Replace any single character, capitalized letters to be retained:
		    hashableString = hashableString.replaceAll( "\\b([A-Z]){1,1}\\b", "$1RetainedIdentifier" );
		    
		    // Lowercase:
		    hashableString = hashableString.toLowerCase( );
		    
		    // Replace all lower case words containing less than four characters:
		    hashableString = hashableString.replaceAll( "\\b[a-z']{1,3}\\b", "" );
		    
		    // Replace any months or month abbreviations:
		    hashableString = hashableString.replaceAll( "\\bjanuary\\b|\\bfeburary\\b|\\bmarch\\b|\\bapril\\b|\\bmay\\b|\\bjune\\b|\\bjuly\\b|\\baugust\\b|\\bseptember\\b|\\bnovember\\b|\\bdecember\\b", "" );
		    
		    // Replace any days:
		    hashableString = hashableString.replaceAll( "\\bmonday\\b|\\btuesday\\b|\\bwednesday\\b|\\bthursday\\b|\\bfriday\\b|\\bsaturday\\b|\\bsunday\\b", "" );
		    
		    // Strip everything that's not a valid alphabet character or number:
		    hashableString = hashableString.replaceAll( "[^A-z0-9]", "" );
		    
		    return hashableString;

		  /*
	    String hashableString = new String( string );
	    
	    // Replace all numbers that aren't enclosed in letters:
	    hashableString = hashableString.replaceAll( "\\b(?<![\\w'])\\d+\\b", "" );
	    
	    // Replace all words inside parenthesis:
	    hashableString = hashableString.replaceAll( "\\(.*\\)", "" );
	    
	    // Replace all words containing less than four characters:
	    hashableString = hashableString.replaceAll( "\\b[a-z']{1,3}\\b", "" );
	    
	    // Replace any months or month abbreviations:
	    hashableString = hashableString.replaceAll( "january|jan|feburary|feb|march|mar|april|apr|may|june|jun|july|jul|august|aug|september|sept|november|nov|december|dec", "" );
	    
	    // Replace any days:
	    hashableString = hashableString.replaceAll( "monday|tuesday|wednesday|thursday|friday|saturday|sunday", "" );
	    
	    // Strip everything that's not a valid alphabet character or number:
	    hashableString = hashableString.replaceAll( "[^A-z0-9]", "" );
	    
	    // Lowercase:
	    hashableString = hashableString.toLowerCase( );
	    
	    return hashableString;
	    */
	  }
	  
	  public long hashValue( final String string )
	  {
	    try
	    {
	      // Create a hashable string:
	      String hashableString = hashableString( string );
	      
	      if ( hashableString.isEmpty( ) == true )
	        return emptyReferenceIdentifier( );
	      
	      // Start the hash value at an arbitrary prime number:
	      long hash = 5381;
	      
	      // Begin hashing each character:
	      for ( int i = 0; i != hashableString.length( ); ++i )
	        hash = hash * 31 + hashableString.charAt( i );
	      
	      return hash;
	    }
	    catch ( Exception e )
	    {
	    }
	    
	    return emptyReferenceIdentifier( );
	  }
/*
	public String hashableString( String string )
	{
	    String hashableString = new String( string.toLowerCase( ) );
	    
	    // Replace all words inside parenthesis:
	    hashableString = hashableString.replaceAll( "\\(.*\\)", "" );
	    
	    // Replace all words containing less than four characters:
	    hashableString = hashableString.replaceAll( "\\b[\\w']{1,3}\\b", "" );
	    
	    // Replace any months or month abbreviations:
	    hashableString = hashableString.replaceAll( "january|jan|feburary|feb|march|mar|april|apr|may|june|jun|july|jul|august|aug|september|sept|november|nov|december|dec", "" );
	    
	    // Replace any days:
	    hashableString = hashableString.replaceAll( "monday|tuesday|wednesday|thursday|friday|saturday|sunday", "" );
	    
	    // Strip everything that's not a valid alphabet character:
	    hashableString = hashableString.replaceAll( "[^A-z]", "" );
	    
	    // Lowercase:
	    hashableString.toLowerCase( );
	    
	    return hashableString;
	}

	public long hashValue(String string )
	{
	    // Create a hashable string:
	    String hashableString = hashableString( string );
	    
	    // Start the hash value at an arbitrary prime number:
	    long hash = 5381;
	    
	    // Begin hashing each character:
	    for ( int i = 0; i != hashableString.length( ); ++i )
	      hash = hash * 31 + hashableString.charAt( i );
	    
	    return hash;
	}

*/
	public boolean TagMatch(String testStr, String matchStr) {
		
		String base = "";		
		boolean bRtn = false;
		
		if(testStr.equals(matchStr))
			bRtn = true;
		else {
			base = "link:" + matchStr;
			if(testStr.equals(base))
				bRtn = true;
			else {
				base = "xbrli:" + matchStr;
				if((testStr.equals(base)) && (matchStr.equals("xbrl") == false)) // thus eliminating xbrli:xbrli
					bRtn = true;
			}
		}
		return(bRtn);
	}

	public boolean XsMatch(String testStr, String matchStr) {
		boolean bRtn = false;
		
		if(testStr.equals(matchStr))
			bRtn = true;
		else {
			if(testStr.equals("xs:" + matchStr))
				bRtn = true;
		}
		return(bRtn);
	}

	public boolean XbrlMatch(String testStr, String matchStr) {
		
		String base = "";		
		boolean bRtn = false;
		
		if(testStr.equals(matchStr))
			bRtn = true;
		else {
			base = "xbrli:" + matchStr;
			if(testStr.equals(base))
				bRtn = true;
			else {
				base = "xbrldi:" + matchStr;
				if(testStr.equals(base))
					bRtn = true;
			}
		}
		return(bRtn);
	}
	
	public int DetailMatch(String testStr, String prefixStr) {
		int iRtn = CONSTANTS.NO_DETAIL;

		/************************
		if(testStr.indexOf(prefixStr + ":") == 0)
				iRtn = CONSTANTS.PREFIX_DETAIL;
		else {
			if(testStr.indexOf(CONSTANTS.DEI_TAG) == 0)
				iRtn = CONSTANTS.DEI_DETAIL;
			else {
				if(testStr.indexOf(CONSTANTS.GAAP_TAG) == 0)
					iRtn = CONSTANTS.GAAP_DETAIL;
			}
		}
		************/
		if(testStr.indexOf(CONSTANTS.DEI_TAG) == 0)
			iRtn = CONSTANTS.DEI_DETAIL;
		else {
			if((testStr.contains("xbrli:")) || (testStr.contains("link:")) || (testStr.contains("xbrl")))
					iRtn = CONSTANTS.NO_DETAIL;
			else
				iRtn = CONSTANTS.GROUPING_DETAIL;
		}
		return(iRtn);
	}
	
	public boolean AttMatch(String testStr, String matchStr) {
		
		String base = "";		
		boolean bRtn = false;
		
		if(testStr.equals(matchStr))
			bRtn = true;
		else {
			base = "xlink:" + matchStr;
			if(testStr.equals(base))
				bRtn = true;
		}
		return(bRtn);
	}
	

	public boolean TagMatchContext(String testStr) {
		boolean bRtn = false;
		
		if(testStr.equals("context"))
			bRtn = true;
		else {
			if(testStr.equals("xbrli:context"))
					bRtn = true;
		}
		return(bRtn);	
	}
	
    private String FOOTNOTELINK = "link:footnotelink";
    
    public boolean EnterFootnote(String origStr) {
    	boolean bRtn = false;
    	
    	if(origStr.contains(FOOTNOTELINK))
    		bRtn = true;
    	return(bRtn);
    }
	
	private String FOOTNOTE = "link:footnote";
	private String FOOTNOTEARC = "link:footnotearc";
	private String FOOTNOTELOC = "link:loc";
	public int FootnoteCheck(String origStr) {
		int iRtn = 0;
		
		if(origStr.contains(FOOTNOTE))
			iRtn = CONSTANTS.FOOTNOTE;
		else {
			if(origStr.contains(FOOTNOTEARC))
				iRtn = CONSTANTS.FOOTNOTEARC;
			else {
				if(origStr.contains(FOOTNOTELOC))
					iRtn = CONSTANTS.FOOTNOTELOC;
			}
		}
		return(iRtn);
	}
	
	public String AddADay(Connection con, String myStrDate){
	    Date   converter = null;
	    String rtn = "";
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    
	    converter = DateConverter(con, myStrDate);
	    converter = AddDays(converter, 1);
	    rtn = formatter.format(converter);
	    return(rtn);
	}
	
	public Date DateConverter(Connection con, String myStrDate) {
		Date        rtnDate = null;
		DateFormat  formatter;
		ErrorCls    errorCls = new ErrorCls();
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			rtnDate = (Date)formatter.parse(myStrDate);
		}
		catch (Exception e) {
			errorCls.setFunctionStr("DateConverter");
			errorCls.setCompanyUid(0);
			errorCls.setItemVersion(0);
			errorCls.setSubUid(0);
			errorCls.setErrorText("Cannot convert to Date : " + myStrDate);
			mySqlAccess.WriteAppError(con, errorCls);
		}
		return(rtnDate);
	}

	public Date AddDays(Date date, int days) {
		
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
	    return cal.getTime();

	}
}
