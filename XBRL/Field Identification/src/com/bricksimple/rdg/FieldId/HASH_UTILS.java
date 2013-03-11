package com.bricksimple.rdg.FieldId;

public class HASH_UTILS {

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
}
