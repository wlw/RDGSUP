package com.bricksimple.rdg.FieldId;

import java.sql.*;
import java.util.ArrayList;

public class ResolveDupTemplates {
    public void Resolve(Connection con, SubmissionInfo subInfo) {
    	ArrayList<SubmissionTemplates> subT = new ArrayList<SubmissionTemplates>();
    	ArrayList<FieldLocatedXref>    firstTemplate = new ArrayList<FieldLocatedXref>();
    	ArrayList<FieldLocatedXref>    secondTemplate = new ArrayList<FieldLocatedXref>();
    	MySqlAccess                    mySqlAccess = new MySqlAccess();
    	
    	subT = mySqlAccess.GetSubmissionTemplates(con, subInfo);
    	for(int i = 0; i <subT.size()-1; i++) {
    		if(subT.get(i).TemplateId == subT.get(i+1).TemplateId) {  // same ids possible dups
    			firstTemplate = mySqlAccess.GetDistinctFieldRows(con, subInfo, subT.get(i).TemplateUid);
    			secondTemplate = mySqlAccess.GetDistinctFieldRows(con, subInfo, subT.get(i+1).TemplateUid);
    			if(TemplatesEqual(firstTemplate, secondTemplate)) {	
    				mySqlAccess.UpdateDuplicatedTemplate(con, subInfo, firstTemplate, subT.get(i).TemplateUid);
    			}
    		}
    	}
    }
  
    private boolean TemplatesEqual(ArrayList<FieldLocatedXref> template1, ArrayList<FieldLocatedXref> template2) {
    	boolean bRtn = true;
    	int     i = 0;
    	
    	if(template1.size() == template2.size()) {
    		while((i < template1.size() && (bRtn == true))) {
    			if(template1.get(i).RowData.equals(template2.get(i).RowData) == false)
    				bRtn = false;
    			i++;
    		}
    	}
    	else
    		bRtn = false;
    	return(bRtn);
    }
}
