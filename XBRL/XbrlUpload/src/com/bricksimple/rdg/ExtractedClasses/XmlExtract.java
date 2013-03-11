package com.bricksimple.rdg.ExtractedClasses;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.sql.Connection;
import java.util.Date;

import com.bricksimple.rdg.match.ConfidenceLevel;
import com.bricksimple.rdg.match.DeiObjects;
import com.bricksimple.rdg.xbrlUpload.*;
import com.bricksimple.rdg.sqlaccess.*;
import com.bricksimple.rdg.ExtractedClasses.DimensionGroup;
import com.bricksimple.rdg.ExtractedClasses.Dimension;
import com.bricksimple.rdg.pushxbrl.Stock;

public class XmlExtract {
    private String RootUri = "";
    private ArrayList<NameSpaceLink>  NameSpaceLinks = new ArrayList<NameSpaceLink>();
    private ArrayList<UnitLink>       unitLinks = new ArrayList<UnitLink>();
    private ArrayList<XmlDetail>      dei = new ArrayList<XmlDetail>();
    private ArrayList<XmlGrouping>    grouping = new ArrayList<XmlGrouping>();
    //private ArrayList<XmlContext>     xmlContexts = new ArrayList<XmlContext>();
    private ArrayList<DimensionGroup> dimensions = new ArrayList<DimensionGroup>();
    private ArrayList<FootnoteArc>    footNoteArc = new ArrayList<FootnoteArc>();
    private ArrayList<FootnoteLink>   footNoteLink = new ArrayList<FootnoteLink>();
    private ArrayList<FootnoteLoc>    footNoteLoc = new ArrayList<FootnoteLoc>();
    private String                    customNameSpace = "";
    
    public void SetCustomNameSpace (String iValue) {
    	customNameSpace = iValue;
    }
    
    public String GetCustomNameSpace() {
    	return(customNameSpace);
    }
    
    public ArrayList<XmlDetail> GetDei() {
    	return(dei);
    }
    
    public ArrayList<NameSpaceLink> GetNameSpaceLinks() {
    	return(NameSpaceLinks);
    }
    
    public NameSpaceLink GetThisNameSpaceLink(int i) {
    	return(NameSpaceLinks.get(i));
    }
    
    public ArrayList<UnitLink> GetUnitLinks() {
    	return(unitLinks);
    }
    
    public ArrayList<XmlGrouping> GetXmlGrouping() {
    	return(grouping);
    }
    
    public ArrayList<DimensionGroup> GetDimensionGroup() {
    	return(dimensions);
    }
    
    public ArrayList<FootnoteLoc> GetFootNoteLoc() {
    	return(footNoteLoc);
    }
    
    public void DoXmlExtract(Connection con, XbrlFiles xbrlFile, String prefix) {
    	ExtractCustomNameSpace(xbrlFile.GetRootNode(), prefix);
    	ExtractRootUri(xbrlFile.GetRootNode(), prefix);
    	ExtractNameSpaces(xbrlFile.GetRootNode());
    	ExtractUnits(con, xbrlFile.GetRootNode());
    	ExtractDetails(xbrlFile.GetRootNode(), prefix);
    	//ExtractContext(xbrlFile.GetRootNode());
    	ExtractDimensions(con, xbrlFile.GetRootNode());
    	ExtractFootnotes(con, xbrlFile.GetRootNode());
    	ResolveDimensions();
    	ResolveFootNotes();
    }
    
    private void ResolveDimensions() {
    	boolean bFound = false;
    	int     i = 0;
    	int     iGroups = 0;
    	int     iDetails = 0;
    	
    	while(iGroups < grouping.size()) {
    	//for(XmlGrouping group: grouping) {
    		iDetails = 0;
    		while(iDetails < grouping.get(iGroups).GetDetails().size()) {
    		//for(XmlDetail xmlDetail: group.GetDetails()) {
    			bFound = false;
    			i = 0;
    			while((bFound == false) && (i < dimensions.size())) {
    				if(grouping.get(iGroups).GetDetails().get(iDetails).GetContextRef().equals(dimensions.get(i).GetContextId())) {
    					if(dimensions.get(i).GetDimension().size() > 0) {
    						grouping.get(iGroups).GetDetails().get(iDetails).SetDimension(dimensions.get(i).GetDimension().get(0).GetData());
    						bFound = true;
    					}  						
    				}
    				i++;
    			}
    			if(bFound == false)
    				grouping.get(iGroups).GetDetails().get(iDetails).SetDimension("");
    			iDetails++;
    		}
    		iGroups++;
    	}
    }
    
    public ArrayList<Stock> MapOutStock() {
    	ArrayList<Stock> rtnArray = new ArrayList<Stock>();
    	
    	Stock  stock = null;
    	int    i = 0;
   
    	while(i < dei.size()) {
    		if(dei.get(i).GetTag().toLowerCase().equals(CONSTANTS.DEI_STOCK_TAG)) {
    			stock = new Stock();
    			stock.SetConcept(dei.get(i).GetContextRef());
    			stock.SetStock(dei.get(i).GetData());
    			rtnArray.add(stock);
    			dei.get(i).SetTag("STOCK");
    			dei.get(i).SetMapped(CONSTANTS.MAPPED);
    		}
    		i++;
    	}
    	return(rtnArray);
    }
    
    public boolean MapData(Connection con, ArrayList<DeiObjects>  deiObjects) {
    
    	boolean          bRtn = true;
    	int              i =0;
		double           dConfidence = 0;
		ConfidenceLevel  cl = new ConfidenceLevel();
    	ErrorCls         errorCls = new ErrorCls();
    	MySqlAccess      mySql = new MySqlAccess();
    	
		errorCls.setCompanyUid(0);
		errorCls.setFunctionStr("MapData");
		errorCls.setItemVersion(0);
    	for(XmlDetail xd: dei) {
    		if(xd.GetMapped() == CONSTANTS.NOT_MAPPED) {
    		    i = 0;
    		    while( i < deiObjects.size()) {
                    dConfidence = cl.compareToArrayList1(xd.GetTag().toLowerCase(), deiObjects.get(i).GetAl());    
                    if(dConfidence == 1.0) {
                   	    deiObjects.get(i).SetFound(true);
                   	    deiObjects.get(i).SetXmlData(xd.GetData());
                   	    deiObjects.get(i).SetXmlContext(xd.GetContextRef());
                   	    xd.SetMapped(CONSTANTS.MAPPED);
                    }
    			    i++;
    		     }
    		}
    	}
    	// now we log the items we did not find
    	for(XmlDetail xd: dei) {
    		if(xd.GetMapped() == CONSTANTS.NOT_MAPPED) {
    			//bRtn = false;
    		    errorCls.setErrorText("UnMapped Dei Tag: " + xd.GetTag());
	            mySql.WriteAppError(con, errorCls);    			
    		}
    	}
    	return(bRtn);
    }
    
    public void WriteExtractedInfo(PrintWriter out) {
    	
    	out.println("XML");
    	out.println("   RootUri:"  + RootUri);
    	out.println("");
    	out.println("   NamedSpaces:");
    	for(NameSpaceLink nsl: NameSpaceLinks) {
    		out.println("       Abbrev:" + nsl.GetAbbrev() + " ::  NameSpace: " + nsl.GetNameSpace());
    	}
    	out.println("");
    	out.println("   UNITS:");
    	for(UnitLink cur: unitLinks) {
    		if(cur.GetUnitType() == CONSTANTS.UNIT_MEASURE) {
    			out.println("      Measure: " + cur.GetId());
    			out.println("         Data:" + cur.GetLocalMeasure());
    		}
    		else {
    			out.println("      Divde: " + cur.GetId());
    			out.println("         Numerator:" + cur.GetLocalNumerator());
    			out.println("         Denominator:" + cur.GetLocalDenominator());
   		    }
    	}
    	out.println("");
    	out.println("   DEI:");
    	for(XmlDetail cur: dei) {
    		cur.printXmlDetail(out);
    	}
    	out.println("");
    	/*****
    	out.println("   GAAP:");
    	for(XmlDetail cur: gaap) {
    		cur.printXmlDetail(out);
     	}
    	out.println("");
       	out.println("   MyDetails:");
    	for(XmlDetail cur: myDetails) {
    		cur.printXmlDetail(out);
     	}
     	***/
    	for(XmlGrouping xGroup: grouping) {
    		out.println("    " + xGroup.GetPrefix());
    		for(XmlDetail xmlDetail: xGroup.GetDetails()) {
    			xmlDetail.printXmlDetail(out);
    		}
    	}
    	out.println("");
     	out.println("   CONTEXTS:");
//    	for(XmlContext curContext: xmlContexts)  {
//    		curContext.PrintXmlContext(out);
//    	}
     	out.println("");
    	out.println("");
    	out.println("END OF XML FILE");
    	out.println("");
    	out.println("");
   }
    

    private String CUSTOMTAG = "xmlns:";
    
    private void ExtractCustomNameSpace(XbrlNode node, String prefix) {
    	String workStr = "";
    	
    	for (NodeAttribute attr: node.GetAttributes()){
    		workStr = attr.GetName().toLowerCase();
    		if(workStr.equals(CUSTOMTAG + prefix)) {
    			customNameSpace = attr.GetValue();
    			break;
    		}
    	}
    }
    private String NODE_TAG = "xbrl";
   // private String[] knownAtt = {"xmlns", "xmlns:dei" ,"xmlns:iso4217","xmlns:link", "xmlns:us-gaap", "xmlns:xbrli",
    //		            "xmlns:xbrldi", "xmlns:xl", "xmlns:xlink", "xmlns:xs"};
    
    private void ExtractRootUri(XbrlNode node, String prefix) {
    	String testTag = node.GetTag().toLowerCase();
    	String workStr = "";
    	UTILITIES utilities = new UTILITIES();
    	
        if(utilities.TagMatch(testTag, NODE_TAG)) {
       		for(NodeAttribute attr: node.GetAttributes()) {
       			workStr = attr.GetName().toLowerCase();
       			if(workStr.equals(prefix))
       				RootUri = attr.GetValue();
       		}
        }
    }
    
    
    private String NAMESPACE = "xmlns";
    private void ExtractNameSpaces(XbrlNode node) {
    	String        workStr;
    	NameSpaceLink newEntry;
    	int           i;
    	
    	for(NodeAttribute attr: node.GetAttributes()) {
    		workStr = attr.GetName().toLowerCase();
    		if(workStr.indexOf(NAMESPACE) == 0) {
    			newEntry = new NameSpaceLink();
    			i = workStr.indexOf(":");
    			if(i > 0)
    				newEntry.SetAbbrev(attr.GetName().substring(i+1));
    			newEntry.SetNameSpace(attr.GetValue());
    			NameSpaceLinks.add(newEntry);
    		}
    	}
    }
    
    private String UNIT_TAG = "unit";
    private void ExtractUnits(Connection con, XbrlNode node) {
    	
    	UTILITIES utilities = new UTILITIES();
    	String    workStr = node.GetTag().toLowerCase();
    	
    	if(utilities.TagMatch(workStr, UNIT_TAG) == true) {
    		AddUnitLink(con, node);
    	}
    	for(XbrlNode child: node.GetChildren()) {
    		ExtractUnits(con, child);
    	}
    	if(node.GetSibling() != null)
    		ExtractUnits(con, node.GetSibling());
   	
    }
    
    
    private String UNIT_ID = "id";
    private String UNIT_MEASURE = "measure";
    private String UNIT_DIVIDE = "divide";
    private String DIV_NUMERATOR = "unitnumerator";
    private String DIV_DENOMINATOR = "unitdenominator";
    private int    Neither = 0;
    private int    Numerator = 1;
    private int    Divisor = 2;
    
    private void AddUnitLink(Connection con, XbrlNode node) {
    	String      workStr = "";
    	UnitLink    unitLink = new UnitLink();
    	int         iDivideType = Neither;
    	UTILITIES   utilities = new UTILITIES();
    	MySqlAccess mySqlAccess = new MySqlAccess();
    	
    	for(NodeAttribute attr: node.GetAttributes()) {
    		workStr =  attr.GetName().toLowerCase();
    		if(workStr.equals(UNIT_ID))
    			unitLink.SetId(attr.GetValue());
    	}
    	for(XbrlNode child: node.GetChildren()) {
    		workStr = child.GetTag().toLowerCase();
    		if(utilities.XbrlMatch(workStr, UNIT_MEASURE)) {
    			unitLink.SetLocalMeasure(child.GetData());
    			unitLink.SetUnitType(CONSTANTS.UNIT_MEASURE);
    		}
    		else {
    			if(utilities.XbrlMatch(workStr, UNIT_DIVIDE)) {
    				unitLink.SetUnitType(CONSTANTS.UNIT_DIVIDE);
    				for(XbrlNode grandChild: child.GetChildren()) {
    					iDivideType = Neither;
    					workStr = grandChild.GetTag().toLowerCase();
    					if(utilities.XbrlMatch(workStr, DIV_NUMERATOR)) 
    						iDivideType = Numerator;
    					if(utilities.XbrlMatch(workStr, DIV_DENOMINATOR))
    						iDivideType = Divisor;
    					if(iDivideType != Neither) {
    						for(XbrlNode greatGrandChild: grandChild.GetChildren()) {
    							workStr = greatGrandChild.GetTag().toLowerCase();
    							if(utilities.XbrlMatch(workStr, UNIT_MEASURE)) {
    								if(iDivideType == Numerator)
    		    				        unitLink.SetLocalNumerator(greatGrandChild.GetData());
    								if(iDivideType == Divisor)
    		    						unitLink.SetLocalDenominator(greatGrandChild.GetData());
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	String query = "";
    	
    	if(unitLink.GetLocalMeasure().length() > 0)
    		query = unitLink.GetLocalMeasure();
    	else
    		query = unitLink.GetLocalNumerator() + "/" + unitLink.GetLocalDenominator();
    	unitLink.SetCoreUnitsUid(mySqlAccess.ExtractCoreUnitId(con, query));
    	unitLinks.add(unitLink);
    }
    
    private void ExtractDetails(XbrlNode node, String prefix) {
    	
    	UTILITIES utilities = new UTILITIES();
    	String    workStr = node.GetTag().toLowerCase();
    	int       iType;
    	XmlDetail newNode = new XmlDetail();
    	String    groupStr = "";
    	
    	iType = utilities.DetailMatch(workStr, prefix);
    	if(iType != CONSTANTS.NO_DETAIL) {
    		groupStr = newNode.ConstructNode(node, prefix.length() + 1, iType);
    	    switch(iType) {
    	        case CONSTANTS.DEI_DETAIL:
    		        dei.add(newNode);
    		        break;
    		      
    	        case CONSTANTS.GROUPING_DETAIL:
    	        	// first find group we belong
    	        	int j = 0;
    	        	boolean bFound = false;
    	        	while((bFound == false) && (j < grouping.size())) {
    	        		if(grouping.get(j).GetPrefix().equals(groupStr))
    	        			bFound = true;
    	        		else
    	        			j++;
    	        	}
    	        	// not found so add new grouping
    	        	if(bFound == false) {
    	        		XmlGrouping newGroup = new XmlGrouping();
    	        		newGroup.SetPrefix(groupStr);
    	        		newGroup.AddDetail(newNode);
    	        		grouping.add(newGroup);
    	        	}
    	        	else
    	        		grouping.get(j).AddDetail(newNode);
    	        	break;
    	    }
    	}
    	for(XbrlNode child: node.GetChildren()) {
    		ExtractDetails(child, prefix);
    	}
    	if(node.GetSibling() != null)
    		ExtractDetails(node.GetSibling(), prefix);
    }
    
    public String GetRootUri() {
    	return(RootUri);
    }
    
    /*********************
    private void ExtractContext(XbrlNode node) {
    	
    	UTILITIES  utilities = new UTILITIES();
    	String     workStr = node.GetTag().toLowerCase();
    	boolean    bFound = false;
    	XmlContext newContext = null;
    	
    	if(utilities.TagMatchContext(workStr) == true) {
    		bFound = true;
    		newContext = new XmlContext();
    		newContext.PopulateContext(node);
    		xmlContexts.add(newContext);
    	}
    	if(bFound == false) {
    	    for(XbrlNode child: node.GetChildren()) {
    	    	ExtractContext(child);
    	    }
    	}
    	if(node.GetSibling() != null)
    		ExtractContext(node.GetSibling());
   	
    }
 **************/
    
    private void ExtractDimensions(Connection con, XbrlNode node) {
    	
    	UTILITIES  utilities = new UTILITIES();
    	String     workStr = node.GetTag().toLowerCase();
    	boolean    bFound = false;
     	
    	if(utilities.TagMatchContext(workStr) == true) {
    		bFound = true;
    		CheckForDimensionData(con, node);
    	}
    	if(bFound == false) {
    	    for(XbrlNode child: node.GetChildren()) {
    	    	ExtractDimensions(con, child);
    	    }
    	}
    	if(node.GetSibling() != null)
    		ExtractDimensions(con, node.GetSibling());
   	
    }
    
    private void CheckForDimensionData(Connection con, XbrlNode node) {
    	String id = "";
    	String workStr;
    	DimensionGroup dg = new DimensionGroup();
    	
       	for(NodeAttribute attr: node.GetAttributes()) {
    		workStr =  attr.GetName().toLowerCase();
    		if(workStr.equals(UNIT_ID))
    			dg.SetContextId(attr.GetValue());
    	}
        dimensions.add(dg);   // add to list
        ProcessContextChildren(con, node.GetChildren(), dg);
    }
    
    private String ENTITY = "entity";
    private String PERIOD = "period";
    
    private void ProcessContextChildren(Connection con, ArrayList<XbrlNode> child, DimensionGroup dg) {
    	UTILITIES utilities = new UTILITIES();
    	
    	for(XbrlNode xn: child) {
    		if(utilities.XbrlMatch(xn.GetTag(), PERIOD)) {
    		//if(xn.GetTag().equals(PERIOD)) {
    			AddPeriodItem(con, xn, dg);
    		}
    		else {
    			if(utilities.XbrlMatch(xn.GetTag(), ENTITY))
    			//if(xn.GetTag().equals(ENTITY))
    				CheckForSegment(xn, dg);
    		}
    	}
    }

    private String STARTDATE = "startdate";
    private String ENDDATE   = "enddate";
    private String INSTANT   = "instant";
    
    private void AddPeriodItem(Connection con, XbrlNode child, DimensionGroup dg) {
    	Period period = new Period();
    	String lowered;
    	UTILITIES utilities = new UTILITIES();
    	Date      startDate;
    	Date      endDate;
    	
    	for(XbrlNode xn: child.GetChildren()) {
    		lowered = xn.GetTag().toLowerCase();
    		if(utilities.XbrlMatch(lowered, STARTDATE))
     			period.SetStartDate(xn.GetData());
    		else {
        		if(utilities.XbrlMatch(lowered, ENDDATE))
    				period.SetEndDate(xn.GetData());
    			else {
            		if(utilities.XbrlMatch(lowered, INSTANT)) {
     					//period.SetStartDate(xn.GetData());
    					//period.SetEndDate(xn.GetData());
            			period.SetInstant(xn.GetData());    				}
    			}
    		}
    	}
    	if(period.GetStartDate().length() > 0) {
    	    startDate = utilities.DateConverter(con, period.GetStartDate());
    	    endDate = utilities.DateConverter(con, period.GetEndDate());
    	    period.SetDuration(endDate.getTime() - startDate.getTime());
    	}
    	else
    		period.SetDuration(0);
    	dg.AddPeriod(period);
    }
    
    private String SEGMENT = "segment";
    
    private void CheckForSegment(XbrlNode child, DimensionGroup dg) {
    	UTILITIES utilities = new UTILITIES();
    	
    	for(XbrlNode xn: child.GetChildren()) {
    		if(utilities.XbrlMatch(xn.GetTag(), SEGMENT)) {
   		//if(xn.GetTag().equals(SEGMENT)) {
    			CheckForMember(xn, dg);
    		}
    	}
    }
    
    private String MEMBER = "explicitmember";
    private String DIMENSION = "dimension";
    
    private void CheckForMember(XbrlNode child, DimensionGroup dg) {
    	Dimension dimension = new Dimension();
    	String    workStr;
    	UTILITIES utilities = new UTILITIES();
    	String   lower;
    	
    	for(XbrlNode xn: child.GetChildren()) {
    		lower = xn.GetTag().toLowerCase();
    		if(utilities.XbrlMatch(lower, MEMBER)) {
    		//if(xn.GetTag().toLowerCase().contains(MEMBER))
    			dimension.SetData(xn.GetData());
          	    for(NodeAttribute attr: xn.GetAttributes()) {
        		    workStr =  attr.GetName().toLowerCase();
        		    if(workStr.equals(DIMENSION))
        			    dimension.SetDim(attr.GetValue());
          	    }
        	}
    	}
    	if(dimension.GetData().length() > 0)
            dg.AddDimension(dimension);
    }
    
   
    private void ExtractFootnotes(Connection con, XbrlNode node) {
    	UTILITIES  utilities = new UTILITIES();
    	String     workStr = node.GetTag().toLowerCase();
    	boolean    bFound = false;
    	
    	if(utilities.EnterFootnote(workStr)) {
    		ProcessFootNoteLinks(con, node);
    	    bFound  = true;
    	}
    	if(bFound == false) {
    		for(XbrlNode child: node.GetChildren()){
    		    ExtractFootnotes(con, child);
    		}
    	}
    	if(node.GetSibling() != null)
    		ExtractFootnotes(con, node.GetSibling());
    }
    
    private void ProcessFootNoteLinks(Connection con, XbrlNode node) {
    	UTILITIES  utilities = new UTILITIES();
    	String     workStr = node.GetTag().toLowerCase();
    	boolean    bFound = false;
     	int        iFootNoteType = 0;
     	
     	for(XbrlNode child: node.GetChildren()){
     	    iFootNoteType = utilities.FootnoteCheck(workStr);
     	    switch(iFootNoteType) {
     	        case CONSTANTS.FOOTNOTE:
     	    	    ProcessFootNote(con, child);
     	    	    break;
     	    	    
     	        case CONSTANTS.FOOTNOTEARC:
     	        	ProcessFootNoteArc(child);
     	    	    break;
     	    	    
     	        case CONSTANTS.FOOTNOTELOC:
     	        	ProcessFootNoteLoc(child);
     	    	    break;
     	    }
     	}
    }

    private String FOOTNOTE_LABEL = "label";
    
    private void ProcessFootNote(Connection con, XbrlNode node) {
    	String       theFootNote = node.GetData();
    	FootnoteLink thisfootNoteLink = new FootnoteLink();
    	UTILITIES    utilities = new UTILITIES();
    	MySqlAccess  mySqlAccess = new MySqlAccess();
    	
    	for(NodeAttribute attr: node.GetAttributes()) {
    		if(utilities.AttMatch(attr.GetName(), FOOTNOTE_LABEL))
    			thisfootNoteLink.SetLabel(attr.GetValue());
    		}
        thisfootNoteLink.SetUid(mySqlAccess.WriteFootNoteRec(con, theFootNote));
        footNoteLink.add(thisfootNoteLink);
    }
    
    private String FOOTNOTE_TO = "to";
    private String FOOTNOTE_FROM = "from";
    
    private void ProcessFootNoteArc(XbrlNode node) {
    	FootnoteArc thisfootnoteArc = new FootnoteArc();
    	UTILITIES   utilities = new UTILITIES();
   	
    	for(NodeAttribute attr: node.GetAttributes()) {
    		if(utilities.AttMatch(attr.GetName(), FOOTNOTE_TO))
    			thisfootnoteArc.SetToStr(attr.GetValue());
    		else {
        		if(utilities.AttMatch(attr.GetName(), FOOTNOTE_FROM))
        			thisfootnoteArc.SetFromStr(attr.GetValue());
    		}
    	}
    	footNoteArc.add(thisfootnoteArc);
    }

    private String FOOTNOTE_HREF = "href";
    
    private void ProcessFootNoteLoc(XbrlNode node) {
    	FootnoteLoc thisfootnoteLoc = new FootnoteLoc();
    	UTILITIES   utilities = new UTILITIES();
   	
    	for(NodeAttribute attr: node.GetAttributes()) {
    		if(utilities.AttMatch(attr.GetName(), FOOTNOTE_LABEL))
    			thisfootnoteLoc.SetLabel(attr.GetValue());
    		else {
        		if(utilities.AttMatch(attr.GetName(), FOOTNOTE_HREF))
        			thisfootnoteLoc.SetHRef(attr.GetValue());
    		}
    	}
    	footNoteLoc.add(thisfootnoteLoc);
    }
    
    private void ResolveFootNotes() {
    	boolean bFoundArc = false;
    	int     iArc;
    	String  workStr;
    	int     iFoot;
    	boolean bFoundFoot = false;
    	String  locLabel = "";
    	
    	for(FootnoteLoc thisLoc: footNoteLoc) {
    		bFoundArc = false;
    		iArc = 0;
      		while((bFoundArc == false) && (iArc < footNoteArc.size())) {
    			if(footNoteArc.get(iArc).GetFromStr().equals(thisLoc.GetLabel())) {
    				bFoundArc = true;
    			}
    			    iFoot = 0;
    			    bFoundFoot = false;
    			    while((bFoundFoot == false) && (iFoot < footNoteLink.size())) {
    			    	if(footNoteLink.get(iFoot).GetLabel().equals(footNoteArc.get(iArc).GetToStr())) {
    			    		bFoundFoot = true;
    			    		thisLoc.SetUid(footNoteLink.get(iFoot).GetUid());
    			    	}
    			    }
    		}
    	}
    }
}
