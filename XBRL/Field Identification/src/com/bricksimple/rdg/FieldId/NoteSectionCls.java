package com.bricksimple.rdg.FieldId;

import java.sql.Connection;

public class NoteSectionCls {
     private int    NoteUid;
     private String Title;
     private String Text;
     private int    SectionUid;
     
     public void InitSection(int iNoteUid) {
    	 NoteUid = iNoteUid;
     }
     
     public int GetSectionUid() {
    	 return(SectionUid);
     }
     
     public void WriteSectionrecord(Connection con, String title) {
    	 MySqlAccess mySql = new MySqlAccess();
    	 
    	 Title = title;
    	 SectionUid = mySql.WriteNoteSectionRec(con, NoteUid, Title, "");
     }
}
