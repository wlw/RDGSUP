package com.bricksimpe.rdg.XbrlTemplateId;

import java.sql.Connection;

public class TemplateLogger {
	Integer iNumTemplates = 0;
	Integer iNumNotes     = 0;
	Integer iNumTablesInNotes = 0;
	

	public  void LogIdentifiedSections(Connection con, ErrorCls errorCls, SubmissionInfo si) {
		MySqlAccess mySql = new MySqlAccess();
		
		mySql.GetItemsIdentified(con, si, this);
		errorCls.setErrorText("Templates Identified = " + iNumTemplates +": Notes Identified = " + iNumNotes +
				              ": Tables in Notes = " + iNumTablesInNotes);
		mySql.WriteAppError(con, errorCls);
	}

}
