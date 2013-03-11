package BatchSubmission;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;

public class LogNoteDetails {
	public int DoLogging(Connection con, String SourceHtmlFileName, String LogPath) {
		
		int            iRtn = 0;
		SubmissionInfo si = new SubmissionInfo();
		MySqlAccess    mySql = new MySqlAccess();
		ArrayList<Note> notes = new ArrayList<Note>();
		ArrayList<Integer> sectionUids = null;
		ArrayList<FactInfo> facts = null;
		int                 iCurSentence = 0;
		int                 iLineCount = 0;
		boolean             bFirst = true;
		
		try {
            FileWriter outFile = new FileWriter(LogPath);
            PrintWriter out = new PrintWriter(outFile);
            si = mySql.GetUidVersion(con, SourceHtmlFileName);
            if(si.iUid > 0) {
            	notes = mySql.GetNotes(con, si);
            	for(Note cur: notes) {
            		bFirst = true;
            		out.println("NOTE:: " + cur.IdentifiedText);
            		sectionUids = mySql.GetSections(con, cur.NoteUid);
            		for(Integer curSection: sectionUids) {
            			facts = mySql.GetFacts(con, curSection);
            			for(FactInfo curFact: facts) {
            				if(curFact.factSentenceUid != iCurSentence) {
            					if(iLineCount > 0)
            						out.println("");
            					iLineCount = 0;
            					iCurSentence = curFact.factSentenceUid;
            					String sentence = mySql.GetSentence(con, iCurSentence);
            					if(bFirst == false) {
            						out.println("");
            						out.println("<><><><> End Sentence <><><><>");
            					}
            					bFirst = false;
            					out.println("  SENTENCE: " + sentence);
            					out.println("");
            					out.println("FACTS: ");
            				}
            				out.print(curFact.fact + "     ");
            				iLineCount += curFact.fact.length() + 5;
            				if(iLineCount > 80) {
            					out.println("");
            					iLineCount = 80;
            				}
            			}
            			out.println("");
            		}
            		out.println("--------------- End Note ------------------");
            		out.println("");
            		out.println("");
            	}
            }
            else
            	iRtn = 2;
            out.close();
            outFile.close();
		}
		catch (Exception e) {
			iRtn  = 1;
		}
		return (iRtn);
	}
}
