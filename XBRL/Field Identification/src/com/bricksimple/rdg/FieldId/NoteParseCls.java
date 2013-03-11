package com.bricksimple.rdg.FieldId;

//import java.io.BufferedReader;
//import java.io.DataInputStream;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

public class NoteParseCls {

	 
	NoteInfoCls noteInfoCls = new NoteInfoCls();  // static
	
	
	private String BuildNoteTextString(int iNoteCounter) {  // static
		String rtnStr = "note " + iNoteCounter;
		return(rtnStr);
	}
	
	private void InitializeNoteInfoCls(String IdentifiedText, int iNoteCounter) { //static
		String PreTestText = BuildNoteTextString(iNoteCounter);
		int iIndex;
		
		//noteInfoCls = new NoteInfoCls();
		
		noteInfoCls.bFoundText = false;
		noteInfoCls.PreString = "";
		if((IdentifiedText != null) && (IdentifiedText.length() > 0)) {
			String InspectStr = IdentifiedText.toLowerCase();
			noteInfoCls.PreString = IdentifiedText + " ";  // add space just in case
		    iIndex = InspectStr.indexOf(PreTestText);
		    if(iIndex == -1) {  // if cannot find Note X, then must have real text 
		    	noteInfoCls.bFoundText = true;  // just set the found indicator
		    }
		    else {   // remove the Note X text and see if anything else there
		    	String remStr = IdentifiedText.substring((iIndex + PreTestText.length()));
		    	remStr = remStr.trim();
		    	if(remStr.length() > 0)  { // if still something left
		    		noteInfoCls.bFoundText = true;   // must be some text
		    	}
		    }
		}
	}
	
	public int ParseNote(ArrayList<String> noteSection, int iTemplateType,  //static
			                     Connection con, SubmissionInfo si, int TemplateParseUid,
			                     String IdentifiedText, int iNoteCounter, 
			                     int iHtmlBeginLineNum, int iHtmlEndLineNum, int iCurrentLineNumber,
			                     ArrayList<String> dateForms, NoteDetailWord noteDetailWord,
			                     ArrayList<HtmlReplacementLines> hrlList, ArrayList<DefedFacts> DefdWords,
			                     int iNoteIndex, int iHtmlSrc) {
		String                       tempStr;
		int                          iCurNoteSection = 0;
		String                       TableStr = "";
		//String                       TermTbl = "</table>";
		ArrayList<TdColSpan>         noteTableColumns = new ArrayList<TdColSpan>();
		ArrayList<NoteTableBounds>   ntb = new ArrayList<NoteTableBounds>();
		//int                          iTableBoundsIndex = 0;
		//ArrayList<NoteTableLineNum>  TableLineNums = new ArrayList<NoteTableLineNum>();
		boolean                      bWithInTable = false;
		int                          iNoteTableIndex = 0;
		boolean                      bTextLine = true;;
		CONSTANTS                    constants = new CONSTANTS();
		MySqlAccess                  mySqlAccess = new MySqlAccess();
		//int                          iSectionUid = 0;
		NoteSectionCls               noteSectionCls = new NoteSectionCls();
		NoteDetailInfo               ndi = new NoteDetailInfo();
		String                       lastSentence = "";
		int                          htmlLineNum = 0;
		int                          iNumLines;
		
		InitializeNoteInfoCls(IdentifiedText, iNoteCounter);
		//ntb = GetNoteTableBounds(con, si.getHtmlFile(), iHtmlBeginLineNum, iHtmlEndLineNum);
		ntb = GetNoteTableBounds(con, si, TemplateParseUid);
		noteTableColumns = GetTableColumns(con, si.getHtmlFile(), ntb);
		iNumLines = RemoveMtSections(noteSection);
 	    //TableLineNums = MySqlAccess.GetNoteTableLineNums(con, si, TemplateParseUid);
        while(iCurNoteSection < noteSection.size()) {
        	if(noteSection.get(iCurNoteSection) != null) {
        	    tempStr = noteSection.get(iCurNoteSection);
		        if((tempStr != null) && (tempStr.length() > 0)) {   // left to right
                    //TestNoteIdentification(con, TemplateParseUid, tempStr, iNoteCounter);
                    if(bWithInTable == false) {
                	    if((ntb.size() == 0)  || (iNoteTableIndex == ntb.size())) // if no tables in this note OR no more tables - mark as text
                		    bTextLine = true;
                	    else {
                	        if(iCurrentLineNumber  == ntb.get(iNoteTableIndex).BeginLineNum) {
                	    	    bTextLine = false;
                	    	    TableStr = tempStr;
                	    	    bWithInTable = true;
                	        }
                	        else   // still not at beginning of table
                	    	    bTextLine = true;
                	    }
                    }
                    else  {  // reading in a table
                	    bTextLine = false;
                	    if(iCurrentLineNumber == ntb.get(iNoteTableIndex).EndLineNum) {
                		    if(ntb.get(iNoteTableIndex).bIsDefinitionTbl == false)  {  // if not note specified within a table construct
		                        if(constants.CheckForEndTable(tempStr) != -1) {   // use this to stip extra stuff off table
				                    int kk = constants.CheckForEndTable(tempStr);
		   		                    tempStr = tempStr.substring(0, kk + CONSTANTS.EndOfTblOfCont.length());
		   	                    }
			                    TableStr += tempStr;
                		        // COMPLETED TABLE
                		        // we process the completed table
			   	                if(IsThisARealTable(noteTableColumns.get(iNoteTableIndex)) == true) {
			   	            	    mySqlAccess.AddSentenceToTable(con, ntb.get(iNoteTableIndex).NoteTableUid, lastSentence);
		                            processTableNote(ntb.get(iNoteTableIndex).NoteTableUid, TableStr, con, si, TemplateParseUid,
		    	     	                             iNoteCounter, ntb.get(iNoteTableIndex).BeginLineNum,
			   	    	                             ntb.get(iNoteTableIndex).EndLineNum,
			   	 	    	                         noteTableColumns.get(iNoteTableIndex), dateForms);
			   	                }
			   	                else {  // it's not a note table, so delete from NoteTables!!
			   	       	            if((ntb.size() > 0) && (iNoteTableIndex < ntb.size())) {
				   	       	             // This is where it is a text line masquarding as a table
				   	       	            ProcessTextLine(TableStr, con, TemplateParseUid, iNoteCounter, si, noteDetailWord, ndi,
  	    		                                        ntb.get(iNoteTableIndex).HtmlBeginLineNum, hrlList, DefdWords,
  	    		                                        true, iHtmlSrc);
			   	       	                mySqlAccess.DeleteNoteTableRec(con, ntb.get(iNoteTableIndex).NoteTableUid);  // then delete from note tables
			   	       	            }
			   	                }
                		    }
               		        iNoteTableIndex++;
               		        bWithInTable = false;
                	    }
                	    else
                    	    TableStr += tempStr;                		
                    }
                    if(((bTextLine == true) && (iCurNoteSection != 0)) || (iNumLines == 1))  {   // process this text line as we skip the first line 
                	    if((tempStr.length() < constants.MIN_NOTE_SENTENCE_LEN) && (iHtmlSrc != 0)) {
                		    if(tempStr.trim().length() > 0) {
                		        noteSectionCls.InitSection(TemplateParseUid);
                		        noteSectionCls.WriteSectionrecord(con, tempStr);
                		    }
                	    }
                	    else {
                		    htmlLineNum = mySqlAccess.getHtmlLineNumber(con, si, iCurrentLineNumber);
                		    if(iNumLines == 1) { // only one line so remove everything up to first space
                			    int jj = tempStr.indexOf(" ");
                			    tempStr = tempStr.substring(jj+1).trim();
                		    }
                   	        String PoslastSentence = ProcessTextLine(tempStr, con, TemplateParseUid, iNoteCounter, si, noteDetailWord, ndi,
                   	    	                	                     htmlLineNum, hrlList, DefdWords, false, iHtmlSrc);
                   	        if(PoslastSentence.length() > 0)
                   	    	    lastSentence = PoslastSentence;
                	    }
                    }
			    }
        	}
		    iCurNoteSection++;
		    iCurrentLineNumber++;
		}
        iCurrentLineNumber--;  // decrement as we did not read this line
        return(iCurrentLineNumber);
	}
	
	private int RemoveMtSections(ArrayList<String> sections) {
		int i = sections.size() -1;
		int iNumLines = 0;
		
		while(i >= 0) {
			if(sections.get(i) != null) {
			    if(sections.get(i).trim().length() > 0) {
				    iNumLines++;
			    }
			}
		    i--;	
		}
		return(iNumLines);
	}
	
	private boolean IsThisARealTable(TdColSpan tdColSpan) { //static
		boolean bRtn = true;
		
		if(tdColSpan.TblRow.size() <= 1)
			bRtn = false;
		else {
			bRtn = false;
			for(int i =0; i < tdColSpan.TblRow.size(); i++) {
				if(tdColSpan.TblRow.get(i).size() > 1)
					bRtn = true;
			}
		}
		return(bRtn);
		
	}
	
	private ArrayList<NoteTableBounds> GetNoteTableBounds(Connection con, SubmissionInfo si, int iNoteUid) {  // static
		ArrayList<NoteTableBounds> rtnArray = new ArrayList<NoteTableBounds>();
        ArrayList<Integer>         listNoteTableUids;
        int                        iCount = 0;
        NoteTableBounds            ntb;
        MySqlAccess                mySqlAccess = new MySqlAccess();
        
        listNoteTableUids = mySqlAccess.GetListOfNoteTableUids(con, si, iNoteUid);
        while(iCount < listNoteTableUids.size()) {
        	ntb = new NoteTableBounds();
        	ntb = mySqlAccess.GetNoteTableBounds(con, listNoteTableUids.get(iCount));
        	rtnArray.add(ntb);
        	iCount++;
        }
		
		return(rtnArray);
	}
	
	private ArrayList<TdColSpan> GetTableColumns(Connection con, String htmlFileName, ArrayList<NoteTableBounds> ntb)  { //static
		int                  i = 0;
		ArrayList<TdColSpan> rtnArray = new ArrayList<TdColSpan>();
		TdColSpan            thisColSpan;
		
		while(i < ntb.size()) {
			thisColSpan = new TdColSpan();
			thisColSpan.getTableColumns(con, htmlFileName, ntb.get(i).HtmlBeginLineNum, ntb.get(i).HtmlEndLineNum);
			rtnArray.add(thisColSpan);
			i++;			
		}
		return(rtnArray);
	}
	
	
	private void processTableNote(int iNoteTableUid, String TableStr, Connection con, SubmissionInfo si, //static
			                             int TemplateParseUid, int iNoteCounter, int beginLineNum,
			                             int endLineNum, TdColSpan tdColSpan, ArrayList<String> dateForms) {
		String               ElementStr;
		String               NodeData;
		ColumnPosInfo        columnPosInfo = new ColumnPosInfo();
		int                  iTdNdx = 0;
		int                  iTrNdx = 0;
		int                  iNumDateLines = 0;
        DateRefCls           dateRefUid = new DateRefCls();
        int                  iRowLabelUid = 0;
        boolean              bSkipToNextRow = false;
        ErrorCls             errorCls = new ErrorCls();
        //String               ColumnZero = "";
        TemplateHdr          templateHdr = new TemplateHdr();
        DateFmts             dateFmts = new DateFmts();
        MySqlAccess          mySqlAccess = new MySqlAccess();
        WriteFieldData       writeFieldData = new WriteFieldData();
        boolean              bBumpTdCount = false;
        
        errorCls.setFunctionStr("processTableNote");
        templateHdr.Date1 = "";
        templateHdr.Date2 = "";
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(TableStr));

            Document doc = db.parse(is);
            DocumentTraversal traversal = (DocumentTraversal) doc;
            NodeIterator iterator = traversal.createNodeIterator(
                                    doc.getDocumentElement(), NodeFilter.SHOW_ELEMENT, null, true);
            Node n = iterator.nextNode();
            Document docx = dateFmts.GetPreNorml(con, si, beginLineNum, endLineNum);
            dateRefUid = dateFmts.GetNoteTableColumns(con, doc, docx, si, TemplateParseUid, tdColSpan, templateHdr, dateForms, null);
            if(dateRefUid.DateRefUid.size() == 0)
            	iNumDateLines = 0;
            else {
                iNumDateLines = dateRefUid.DateRefUid.get((dateRefUid.DateRefUid.size() -1));
               // WriteAnyAbstractsFoundInDate(con, si, 4, null, DateRefUid);
            }
        	columnPosInfo.iCurColumn = 0;
        	columnPosInfo.iThisColumnWidth = 0;
            while(n != null) {
                ElementStr = ((Element) n).getTagName();
                if(ElementStr.equals("tr")) {
                	bSkipToNextRow = false;
                	iTdNdx = 0;
                	iTrNdx++;
                    columnPosInfo.iCurColumn = 0;
                    columnPosInfo.iThisColumnWidth = 0;
                    bBumpTdCount = false;
               }
                else {  
                	if(ElementStr.equals("td")) {
                       	columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;
                    	if(tdColSpan.TblRow.get(iTrNdx-1).size() > iTdNdx)
                            columnPosInfo.iThisColumnWidth = tdColSpan.TblRow.get(iTrNdx-1).get(iTdNdx);
                        //iTdNdx++;

                	}
                }
                NodeList fstNm = n.getChildNodes();
                NodeData = null;
                if(fstNm.item(0) != null)
                    NodeData = (fstNm.item(0)).getNodeValue();
                //if((iTrNdx <= iNumDateLines) && (iTdNdx == 1) && (NodeData != null)) { // we be checking for column zero text
                //	if(NodeData.length() > 0) {
                //		if(ColumnZero.length() > 0)
                //			ColumnZero += " ";
                //		ColumnZero += NodeData;
                //	}
                //}
                if((iTrNdx > iNumDateLines)  && (bSkipToNextRow == false)) {
                	//if(ColumnZero.length() > 0) {
                	//	mySqlAccess.WriteColumnZero(con, iNoteTableUid, ColumnZero);
                	//	ColumnZero = "";
                	//}
                    if(NodeData != null) {
                        NodeData = NodeData.trim();
                        boolean bGotColumnData = CheckForNoData(n);
                        if((NodeData.length() > 0)  || (bGotColumnData == true)) {
                        	bSkipToNextRow = true;
                        	//if(iTdNdx == 1)
                        	    iRowLabelUid = mySqlAccess.InsertNoteTableRow(con, si, iNoteTableUid, NodeData, iTrNdx, iTdNdx);
                        	//else {
                         	    columnPosInfo.iCurColumn += columnPosInfo.iThisColumnWidth;  // prime with current
                                writeFieldData.DbWriteNoteFieldData(con, si, iNoteTableUid, iRowLabelUid, n, iTdNdx, dateRefUid.DateRefUid,
                                    	    	                 columnPosInfo,tdColSpan.TblRow.get(iTrNdx-1), NodeData, iTrNdx); 
                        }
                    }
                }
                if(bBumpTdCount == false)
                	bBumpTdCount = true;
                else
                	iTdNdx++;
                n = iterator.nextNode();
           }
        }
		catch (Exception e) {
			//System.out.println(TableStr);
		    errorCls.setSubUid(0);
		    errorCls.setCompanyUid(0);
		    errorCls.setItemVersion(0);
		    errorCls.setErrorText("Error parsing note table: "  + e.getMessage());
		    errorCls.setBExit(false);
		    mySqlAccess.WriteAppError(con, errorCls);	
		}
	}
	
	public boolean CheckForNoData(Node n) { //static
		boolean           bRtn = false;
        Node              dataNode = n;
        NodeList          fstNm;
        String            NodeData;
        
        dataNode = dataNode.getNextSibling();
        while((dataNode != null) && (bRtn == false)) {
            fstNm = dataNode.getChildNodes();
        	if(fstNm.getLength() > 0) {
                NodeData = (fstNm.item(0)).getNodeValue();
                NodeData = NodeData.trim();
                if(NodeData.length() > 0)
            	    	bRtn = true;
          	}
           dataNode = dataNode.getNextSibling();
        }
		return(bRtn);
	}
	
	private boolean CheckScaling(Connection con, int iNoteTableUid, String NodeData) { //static
		
		String      LowerCase = NodeData.toLowerCase();
		boolean     bRtn = true;
		MySqlAccess mySqlAccess = new MySqlAccess();
		
		if(LowerCase.indexOf("in thousands") != -1) {
			bRtn = false;
			mySqlAccess.SetNoteTableScale(con, iNoteTableUid, 1000);
		}
		else {
			if(LowerCase.indexOf("in millions") != -1) {
				bRtn = false;
				mySqlAccess.SetNoteTableScale(con, iNoteTableUid, 1000000);
			}
			else {
				if(LowerCase.indexOf("in billions") != -1) {
					bRtn = false;
					mySqlAccess.SetNoteTableScale(con, iNoteTableUid, 1000000000);
				}
			}
		}
		return(bRtn);
	}
	
	private String ProcessTextLine(String CurText, Connection con, int TemplateParseUid, int iNoteCounter, 
			                     SubmissionInfo si, NoteDetailWord noteDetailWord, NoteDetailInfo ndi,
			                     int htmlLineNum, ArrayList<HtmlReplacementLines> hrlList,
			                     ArrayList<DefedFacts> DefdWords, boolean bIsNoteTable, int iHtmlSrc) {
		CONSTANTS   constants = new CONSTANTS();
		MySqlAccess mySql = new MySqlAccess();
		String      lastSentence = "";
		
		//boolean bSetNodeText = false;
		String   residualStr = constants.RemoveRemHtmlTags(CurText);
		if(bIsNoteTable == false) {		
		    if(residualStr.length() > 0) {
			    if((residualStr.length() < 50) && (iHtmlSrc != 0))
				    ndi.SetSectionUid(mySql.WriteNoteSectionRec(con, TemplateParseUid, residualStr, ""));
			    else {
				    if(ndi.GetSectionUid() == 0)
					    ndi.SetSectionUid(mySql.WriteNoteSectionRec(con, TemplateParseUid, "", CurText));
				    else
					    mySql.UpdateNoteSectionText(con, ndi.GetSectionUid(), CurText);
		            lastSentence = CheckLineForKeyWords(residualStr, con, TemplateParseUid, si, iNoteCounter, noteDetailWord, ndi,
		        	    	                            htmlLineNum, hrlList, DefdWords, bIsNoteTable, iHtmlSrc);				
			    }
		    }
		}
		else {
			if(residualStr.length() > 0)
			    CheckLineForKeyWords(residualStr, con, TemplateParseUid, si, iNoteCounter, noteDetailWord, ndi,
                                     htmlLineNum, hrlList, DefdWords, bIsNoteTable, iHtmlSrc);				
		}
		return(lastSentence);
	}
	
	//This is to process the text portion of either a table or just text
	
	private ArrayList<String> ListOfSentences(String CurText) {
		ArrayList<String> rtnArray = new ArrayList<String>();
		int               iLen;
		String            sentence = "";
		String            remText = CurText;
		String            curSentence = "";
		
		while(remText.length() > 0) {
			iLen = remText.indexOf(".");
			if(iLen == -1) {
				sentence = curSentence + remText;
				remText = "";
			}
			else {
				curSentence += remText.substring(0, iLen + 1);
				if(iLen < remText.length())  // if period not the last character
				    remText = remText.substring(iLen+1);
				else  // else we got em all
					remText = "";
				if(NextCharUpper(remText)) {
					sentence = curSentence;
					curSentence = "";
				}
			}
			if(sentence.length() > 0) {
			    rtnArray.add(sentence);
			    sentence = "";
			}
		}
		return(rtnArray);
	}
	
	private boolean NextCharUpper(String remText) {
		boolean bRtn = true;
		char    chr;
		String  textStr = remText.trim();
		
		if(textStr.length() > 0) {
			chr = textStr.charAt(0);
			bRtn = Character.isUpperCase(chr);
		}
		
		return(bRtn);
	}
	
	private String CheckLineForKeyWords(String CurText, Connection con, int TemplateParseUid, //static
			                          SubmissionInfo si, int NoteIndex, NoteDetailWord noteDetailWord,
			                          NoteDetailInfo ndi, int htmlLineNum,  ArrayList<HtmlReplacementLines> hrlList,
			                          ArrayList<DefedFacts> DefdWords, boolean bIsNoteTable, int iHtmlSrc) {
		String                 caseLessWord = "";
		ArrayList<String>      words = new ArrayList<String>();
		int                    iWordNdx = 0;
		NoteDetailParseCls     noteDetailParse = new NoteDetailParseCls();
		CONSTANTS              constants = new CONSTANTS();
		MySqlAccess            mySqlAccess = new MySqlAccess();
		//int                ComboNdx =  -1;
		ArrayList<String>      sentences = new ArrayList<String>();
		int                    iSentenceUid = 0;
		ArrayList<Integer>     factUid = new ArrayList<Integer>();
		int                    lastFactNdx = 0;
		HtmlReplacementLines   hrl  = null;
		int                    iPrevWordCount = 0;
		ErrorCls               errorCls = new ErrorCls();
		FactDefRtn             factDefRtn = null;
		
		errorCls.setCompanyUid(si.getCompanyId());
		errorCls.setFunctionStr("CheckLineForKeyWords");
		errorCls.setItemVersion(si.getVersion());
		errorCls.setBExit(false);

		noteDetailParse.InitNoteDetailPasseCls(TemplateParseUid, si, NoteIndex);
		sentences = ListOfSentences(CurText);
		for(String sentence : sentences) {
			iSentenceUid = 0;
			words = constants.ListOfWords(sentence);
			if(words.get(0).length() == 0) // this is to fix the double spaces between sentences
				words.remove(0);
			ArrayList<FactPreWordList> fpwlList = new ArrayList<FactPreWordList>();
			for(iWordNdx = 0; iWordNdx < words.size(); iWordNdx++) {
				iWordNdx += SkipWords(words, iWordNdx);
				if(iWordNdx < words.size()) {
					if(noteDetailWord.DoesWordAppearInKeyWord(words.get(iWordNdx).toLowerCase(), NoteDetailKeyWord.PREWORD)) {
						FactPreWordList fpwl = new FactPreWordList();
						fpwl.PreWord = words.get(iWordNdx);
						fpwl.WordIndex = iWordNdx;
						fpwlList.add(fpwl);
					}
					else {
					    factDefRtn = IsFactWord(words.get(iWordNdx), DefdWords);
				        if(factDefRtn.GetStatus() != 0) {
				        	if(bIsNoteTable)
				        		factDefRtn.SetStatus(3);
				        	if(((iWordNdx > 0) && (noteDetailWord.IsOmission(words.get(iWordNdx -1)) == false)) ||
				        			(iHtmlSrc == 0)) {
					            if(iSentenceUid == 0)
						            iSentenceUid = mySqlAccess.WriteSentence(con, sentence);
					            factDefRtn.CheckForAppend(words.get(iWordNdx), DefdWords);
					            if(factDefRtn.GetStatus() == 4) {
					            	String temp = "";
					            	int    i = words.get(iWordNdx).indexOf("-");
					            	temp = words.get(iWordNdx).substring(0, i);
					                factUid.add(mySqlAccess.WriteSectionFact(con, ndi.GetSectionUid(), temp, iSentenceUid));
					                hrl = new HtmlReplacementLines();
					                hrl.Populate(htmlLineNum, temp, factUid.get(factUid.size() -1), iPrevWordCount, iWordNdx, 
					            	    	     factDefRtn.GetStatus(), iSentenceUid);
					                hrlList.add(hrl);
					            	temp = words.get(iWordNdx).substring(i+1);
					                factUid.add(mySqlAccess.WriteSectionFact(con, ndi.GetSectionUid(), temp, iSentenceUid));
					                hrl = new HtmlReplacementLines();
					                hrl.Populate(htmlLineNum, words.get(iWordNdx), factUid.get(factUid.size() -1), iPrevWordCount, iWordNdx, 
					            	    	     factDefRtn.GetStatus(), iSentenceUid);
					                hrlList.add(hrl);
					            }
					            else {
					                factUid.add(mySqlAccess.WriteSectionFact(con, ndi.GetSectionUid(), factDefRtn.GetDetailStr(), iSentenceUid));
					                lastFactNdx = iWordNdx;
					                hrl = new HtmlReplacementLines();
					                hrl.Populate(htmlLineNum, words.get(iWordNdx), factUid.get(factUid.size() -1), iPrevWordCount, iWordNdx, 
					            	    	     factDefRtn.GetStatus(), iSentenceUid);
					                hrlList.add(hrl);
					                noteDetailWord.AddPreWords(con, factUid.get(factUid.size() -1), fpwlList, iWordNdx, mySqlAccess);
					            }
				        	}
				        }
				        if(iWordNdx > (lastFactNdx + 3)) {
					        lastFactNdx = 0;
					        while(factUid.size() > 0)
						        factUid.remove(0);
				        }
				        else {
				            if(noteDetailWord.DoesWordAppearInKeyWord(words.get(iWordNdx).toLowerCase(), NoteDetailKeyWord.POSTWORD)) {
					            mySqlAccess.WriteFactAssociation(con, words.get(iWordNdx), factUid);
				            }
				        }
				    }
				}
			}
			iPrevWordCount += words.size();
		}
		return(sentences.get(sentences.size() -1));
	}
	
	private int SkipWords(ArrayList<String> words, int iCurWord) {
		int       iRtn = 0;
		CONSTANTS constants = new CONSTANTS();
		
		if(constants.MonthInString(words.get(iCurWord))) {
			iRtn = 1;
			if((iRtn + iCurWord) < words.size()) {
			    if(constants.DoesWordBeginWithNumber(words.get((iCurWord + iRtn)))) {
				    iRtn++;
				    if((iRtn + iCurWord) < words.size()) {
				        if(constants.DoesWordBeginWithNumber(words.get((iCurWord + iRtn)))) 
					        iRtn++;
				    }
			    }
			}
		}
		return(iRtn);
	}
	
	private FactDefRtn IsFactWord(String word, ArrayList<DefedFacts> DefdWords) {
		FactDefRtn factDefRtn = new FactDefRtn();
		boolean    bRtn = false;
		
		factDefRtn.SetStatus(0);
		//caseLessWord = word.toLowerCase();
		bRtn = word.contains("$");
		if(bRtn == true) {  // got ourselves a dollar
			factDefRtn.SetDetailStr(1, word.replace("$", ""));
			factDefRtn.SetMatchNdx(-1);
		}
		else {
			bRtn = word.contains("%");
			if(bRtn == true) {
				factDefRtn.SetDetailStr(1, word.replace("%", ""));
				factDefRtn.SetMatchNdx(-1);
			}
			else {
				bRtn = CheckForNumberWithExceptions(word); 
				if(bRtn == true) {
					factDefRtn.SetDetailStr(1, word);
					factDefRtn.SetMatchNdx(-1);
				}
				else {
					int i = 0;
					while((bRtn == false) && (i < DefdWords.size())) {
						if(word.equals(DefdWords.get(i).GetFact())) {
							factDefRtn.SetDetailStr(2, DefdWords.get(i).GetSubstitution());
							factDefRtn.SetMatchNdx(i);
							bRtn = true;
						}
						else {
							if(DefdWords.get(i).GetContain() == true) {
								if(word.contains(DefdWords.get(i).GetFact())) {
									bRtn = true;
									factDefRtn.SetDetailStr(2, DefdWords.get(i).GetSubstitution());
									factDefRtn.SetMatchNdx(i);
								}
							}
							else {
								if(word.indexOf(DefdWords.get(i).GetFact()) == 0) {
									bRtn = true;
									factDefRtn.SetDetailStr(2, DefdWords.get(i).GetSubstitution());
									factDefRtn.SetMatchNdx(i);
								}
							}
						}
					    i++;
					}
				}
			}
		}
		if(factDefRtn.GetStatus() == 0) {
			if(word.matches(CONSTANTS.HYP_PATTERN)) {
				factDefRtn.SetPatternMatch(true);
				factDefRtn.SetMatchNdx(-1);
				factDefRtn.SetDetailStr(4, "");
			}
		}
		return(factDefRtn);
	}
	
	
	private boolean CheckForNumberWithExceptions(String word) {
		boolean           bRtn = false;
		CONSTANTS         constants = new CONSTANTS();
		ArrayList<String> exceptions = new ArrayList<String>(Arrays.asList("-", "/", "\\"));
		
		bRtn = constants.DoesWordBeginWithNumber(word);
		if(bRtn == true) { // now check for exceptions 
			boolean bFound = false;
			int     iCount = 0;
			while((iCount < exceptions.size()) && (bFound == false)) {
				if(word.contains(exceptions.get(iCount))) {
					bFound = true;
					bRtn = false;
				}
				else 
					iCount++;
			}
			if((bFound == false) && ((iCount = word.length()) > 3)) {
			   String lastChar = word.substring(0, iCount -1);  // strip the last chararcter which may be a comma
			   if(lastChar.indexOf(",") == -1) //must have a comma in the middle somewhere
			   {
				   if((word.indexOf(".") == -1)  && (word.indexOf("'") == -1)) // or must contain a decimal or last character is comma
				       bRtn = false;  // no comma must be a non number
			   }
			}
		}
		return(bRtn);
	}

}
