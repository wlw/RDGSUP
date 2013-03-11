package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;
import java.sql.*;
import java.io.*;



public class NoteFactHtmlDetail {
	public  int                      HTML_TOKEN = 1;
	public  int                      HTML_TEXT = 2;
    private int                      factSentenceUid = 0;
    private String                   factSentence = "";
    private int                      curTokenIdx = 0;
    private int                      prevProcessedIdx = 0;
    private String                   fileLine;
    private ArrayList<TokenizedHtml> arrayTokens = new ArrayList<TokenizedHtml>();
    
    public void SetFactSentenceUid (int iValue) {
    	factSentenceUid = iValue;
    }
    
    public int GetFactSentenceUid() {
    	return(factSentenceUid);
    }

    public void SetFactSentence (String iValue) {
    	factSentence = iValue;
    }
    
    public void ParseHtmlLine(String iValue) {
    	fileLine = iValue;
    	String        remStr = iValue;
    	int           LessThan;
    	int           MoreThan;
    	String        holdingStr = "";
    	
    	// may have to trim - but trying without it
    	while(remStr.length() > 0) {
    		LessThan = remStr.indexOf("<");
    		MoreThan = remStr.indexOf(">");
    		if(LessThan == -1)  { // we got straight text
    			AddTokenized(HTML_TOKEN, holdingStr);
    			AddTokenized(HTML_TEXT, remStr);
    			remStr = "";
    			holdingStr = "";
    		}
    		else {
    			if(LessThan > 0) {
        			AddTokenized(HTML_TOKEN, holdingStr);
        			holdingStr = "";
        			AddTokenized(HTML_TEXT, remStr.substring(0, LessThan));
   				    remStr = remStr.substring(LessThan);
    			}
    			else {   // we got <....>....
    				holdingStr += remStr.substring(0, MoreThan+1);
    				remStr = remStr.substring(MoreThan +1);
    			}
    		}
    	}
    	if(holdingStr.length() > 0)
			AddTokenized(HTML_TOKEN, holdingStr);
    }
    
    public int ModifyHtmlLine(Connection con, SubmissionInfo SubInfo, BufferedWriter wr, ArrayList<HtmlReplacementLines> hrlList,  int iNdx) {
    	int               iRtn = iNdx;
    	boolean           bContinue = true;
    	String            outStr = "";
    	ErrorCls          errorCls = new ErrorCls();
    	MySqlAccess       mySqlAccess = new MySqlAccess();
    	//int               iProcessedChars = 0;
    	//String            workingStr = "";
    	int               tokenNdx = -1;  // we bump first then test if at end
    	//String            remStr = "";
    	//String            prevStr = "";
    	//int               foundFactNdx = 0;
    	CONSTANTS         constants = new CONSTANTS();
    	ArrayList<String> words = null;
    	int               wordProcessing = 0;
    	int               remWords = 0;
    	
    	//words = constants.ListOfWords(sentence);
    	while(bContinue) {
    		if(iRtn == hrlList.size())
    			bContinue = false;
    		else {
    			if(hrlList.get(iRtn).GetMatchType() != 3)
    				bContinue = false;
    			else {
    				while((remWords == 0)  && (bContinue == true)){  // need to break up a sentence
    					tokenNdx++;
    					if(tokenNdx == arrayTokens.size())  // check if we completed checking all
    						bContinue = false;
    					else {
    						if(arrayTokens.get(tokenNdx).GetTokenType() == HTML_TEXT) {
    							words = constants.ListOfWords(arrayTokens.get(tokenNdx).GetToken());
    							arrayTokens.get(tokenNdx).SetToken("");
    							remWords = words.size();
    							wordProcessing = 0;
    						}
    					}
    				}
    				if(remWords > 0) {
    				    if(words.get(wordProcessing).equals(hrlList.get(iRtn).GetFact())) {
    	    			    words.set(wordProcessing, constants.SetNoteFactTag(hrlList.get(iRtn).GetFactUid(), true) + words.get(wordProcessing)  + 
    	    					                                                                          constants.GetNoteFactEndTag());
                            iRtn++;
    				    }
                        remWords--;
    		   			arrayTokens.get(tokenNdx).AppendToken(words.get(wordProcessing));
    	                wordProcessing++;
     				}
     			}
    		}
    	}
    	// clean up remaining words if present
    	while(remWords > 0) {
			arrayTokens.get(tokenNdx).AppendToken(words.get(wordProcessing));
            wordProcessing++;
            remWords--;
    	}
    	//now we build the file line to write
    	for(int i = 0; i < arrayTokens.size(); i++) {
    		outStr += arrayTokens.get(i).GetToken();
    	}
    	//delete the tokens
    	int j =  arrayTokens.size();
    	while(j > 0) {
    		j--;
    		arrayTokens.remove(j);
    	}
    	try {
    	    wr.write(outStr);
    	}
    	catch (Exception e) {
    		errorCls.setCompanyUid(SubInfo.getCompanyId());
    		errorCls.setFunctionStr("ModifyHtmlLine");
    		errorCls.setItemVersion(SubInfo.getVersion());
    		errorCls.setBExit(false);
		     errorCls.setErrorText("Error rewriting html file:"  + e.getMessage());
		     errorCls.setBExit(false);
		     mySqlAccess.WriteAppError(con, errorCls);
   		
    	}
    	return(iRtn);
    }
    
    private void AddTokenized(int iTokenType, String strToken) {
    	TokenizedHtml newToken;
 	
    	if(strToken.length() > 0) {
			newToken = new TokenizedHtml();
			newToken.SetToken(strToken);
			newToken.SetTokenType(iTokenType);
			arrayTokens.add(newToken);  		
    	}
    }
    
    public String GetFactSentence() {
    	return(factSentence);
    }

    public void SetPrevProcessedIdx (int iValue) {
    	prevProcessedIdx = iValue;
    }
    
    public int GetPrevProcessedIdx() {
    	return(prevProcessedIdx);
    }
    
    public void SetCurTokenIdx(int iValue) {
    	curTokenIdx = iValue;
    }
    
    public int GetCurTokenIdx() {
    	return(curTokenIdx);
    }
}
