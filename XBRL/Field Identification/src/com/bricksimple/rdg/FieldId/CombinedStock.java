package com.bricksimple.rdg.FieldId;

import java.util.ArrayList;

public class CombinedStock {
	public float NoDimensionValue = 0;
	public int   NoDimensionShares = 0;
    public float CommonStockAValue = 0;
    public int   CommonStockAShares = 0;
    public float CommonStockBValue = 0;
    public int   CommonStockBShares = 0;
    public float PreferredStockValue = 0;
    public int   PreferredStockShares = 0;
    public float NonVotingCommonStockValue = 0;
    public int   NonVotingCommonStockShares = 0;
    public float ConvertibleCommonStockValue = 0;
    public int   ConvertibleCommonStockShares = 0;
    
    public String ProcessTableStockNumbers(ArrayList<String> tableNodes) {
        String dateStr = "";
        int    i;
        int    j;
        String workStr = "";
        String extractStr = "";
        float  testfloat;
        CONSTANTS constants = new CONSTANTS();
        
        for(i = 0; i < tableNodes.size(); i++) {
        	workStr = tableNodes.get(i).toLowerCase();
        	j = constants.ReturnMonthIndex(workStr);
        	if(j != -1) {  // got month
        		dateStr = workStr.substring(j);
        	}
        	else {
        		if(workStr.indexOf(CONSTANTS.Stock_CommonStr) != -1) {
        			j = workStr.indexOf("$");
        			if(j != -1) {
        				j++;
        				extractStr = workStr.substring(j);
        				extractStr = constants.ExtractDecimalNumber(extractStr.trim());
        				this.NoDimensionValue = Float.valueOf(extractStr);
        			}
        		}
        		else {
        			extractStr = workStr.replace(",", "");
        			extractStr = extractStr.trim();
        			testfloat = constants.isNumeric(extractStr);
        			if(testfloat != -1)
        				this.NoDimensionShares = (int)testfloat;
        		}
        	}
        }
        return(dateStr);
    }
}
