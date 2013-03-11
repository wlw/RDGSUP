package com.bricksimple.rdg.pushxbrl;

public class DimensionTaxUid {
    private int DimTaxonomyUid = 0;
    private int DataTaxonomyUid = 0;
    
    public void SetDimTaxonomyUid(int iValue) {
    	DimTaxonomyUid = iValue;
    }
    
    public int GetDimTaxonomyUid() {
    	return(DimTaxonomyUid);
    }
    
    public void SetDataTaxonomyUid(int iValue) {
    	DataTaxonomyUid = iValue;
    }
    
    public int GetDataTaxonomyUid() {
    	return(DataTaxonomyUid);
    }
    
    
}
