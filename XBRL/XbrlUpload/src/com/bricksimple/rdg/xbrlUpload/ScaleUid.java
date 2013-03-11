package com.bricksimple.rdg.pushxbrl;

public class ScaleUid {
	   private  int  Uid;
	   private String XbrlRefStr;
	    
	   public void SetUid(int iValue) {
	   	    Uid = iValue;
	   }
	    
	   public int GetUid() {
	       return(Uid);
	   }
	    
	   public void SetXbrlRefStr(int iValue) {
		   
		   if(iValue == 0)
			   XbrlRefStr = "inf";
		   else {
			   if(iValue == 3)
				   XbrlRefStr = "-3";
			   else {
				   if(iValue == 6)
					   XbrlRefStr = "-6";
				   else {
					   if(iValue == 9)
						   XbrlRefStr= "-9";
				   }
			   }
		   }
	   }
	    
	   public String GetXbrlRefStr() {
	       return(XbrlRefStr);
	   }

}
