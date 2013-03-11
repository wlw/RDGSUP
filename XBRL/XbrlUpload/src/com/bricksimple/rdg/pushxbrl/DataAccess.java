package com.bricksimple.rdg.pushxbrl;

import java.sql.Connection;
import com.bricksimple.rdg.sqlaccess.*;
import java.util.ArrayList;
import com.bricksimple.rdg.match.*;

public class DataAccess {
	
    public DataAccessRtn FindCompany(Connection con, String CompanyName, String StockSymbol, String webAddress,
    		                         ArrayList<DeiObjects> dei) {
    	DataAccessRtn dataAccessRtn = null;
    	MySqlAccess   mySqlAccess = new MySqlAccess();
    	int           iRtn = 0;
    	int           companyUid;
    	String        query = "";
    	
    	dataAccessRtn= mySqlAccess.FindCompanyUid(con, CompanyName);
    	if(dataAccessRtn.GetSuccess() == dataAccessRtn.NOT_FOUND) {
    		//CompanyCls companyCls = new CompanyCls();
    		companyUid =mySqlAccess.InsertCompanyRow(con, dei, StockSymbol, webAddress);
            dataAccessRtn.SetUid(companyUid);
            mySqlAccess.InsertCikRecord(con, dei, companyUid);
            dataAccessRtn.SetTaxonomyUid(mySqlAccess.WriteTaxomonyUid(con, dataAccessRtn.GetUid()));
    	}
    	return(dataAccessRtn);
    }
    
}
