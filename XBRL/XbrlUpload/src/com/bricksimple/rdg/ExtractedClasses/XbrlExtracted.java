package com.bricksimple.rdg.ExtractedClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import com.bricksimple.rdg.xbrlUpload.*;

public class XbrlExtracted {

	public XsdExtract          xsdExtract = new XsdExtract();
	public DefExtract          defExtract = new DefExtract();
	public LabExtract          labExtract = new LabExtract();
	public PreExtract          preExtract = new PreExtract();
	public CalExtract          calExtract = new CalExtract();
	public XmlExtract          xmlExtract = new XmlExtract();
	public ArrayList<RootNode> NodeChain = new ArrayList<RootNode>();
    public ArrayList<RootNode> ParentheticalChain = new ArrayList<RootNode>();
	public String              WebAddress = "";
	
	public String DoExtraction(Connection con, XbrlFileStruct xbrlFileStruct, String companyId, boolean bWriteOutFile) {
		ArrayList<XbrlFiles> xbrlFiles = xbrlFileStruct.GetXbrlFiles();
		int                  iFileType;
		PrintWriter          out = null;
		String               outFileName = "";
		String               prefix = "";
		XbrlFiles            presentationNode = null;
		
		for(XbrlFiles xbrlFile: xbrlFiles) {
			if(outFileName.length() == 0) {
				outFileName = xbrlFileStruct.GetFilePath() + "\\Summary_" + companyId + ".out";
			}
			iFileType = xbrlFile.GetFileType();
			switch (iFileType) {
			    case CONSTANTS.XBRL_XML:
			    	String fileName = xbrlFile.GetFileName();
			    	int i = fileName.indexOf("-");
			    	prefix = fileName.substring(0, i);
			    	xmlExtract.DoXmlExtract(con, xbrlFile, prefix);
				    break;
				
			    case CONSTANTS.XBRL_XSD:
				    xsdExtract.DoExtract(xbrlFile.GetRootNode(), companyId);
				    xsdExtract.SetXsdFileName(xbrlFile.GetFileName());
				    break;
				
			    case CONSTANTS.XBRL_CAL:
			    	calExtract.DoCalExtract(xbrlFile);
				    break;
				
			    case CONSTANTS.XBRL_DEF:
			    	defExtract.DoDefExtract(xbrlFile);
				    break;
				
			    case CONSTANTS.XBRL_LAB:
			    	labExtract.DoLabExtract(xbrlFile);
			    	labExtract.ResolveXmlRoles(con);
				    break;
				
			    case CONSTANTS.XBRL_PRE:
			    	WebAddress = preExtract.WebAddress(xbrlFile);
			    	presentationNode = xbrlFile;
			    	//preExtract.DoPreExtract(xbrlFile);
			    	//ChainNodes();
			    	//OrderNodes();
			    	//MoveParentheticals();
				    break;
				
			    case CONSTANTS.XBRL_HTM:
				    break;
				
			}
		}
    	preExtract.DoPreExtract(presentationNode);
    	ChainNodes(xsdExtract);
    	OrderNodes();
    	GetLineItems();
   	    MoveParentheticals();

		labExtract.ResolveDocumentation(xsdExtract, companyId);
    	LabelNodes();
    	//GetLineItems();
    	GetNodeDimensions(NodeChain);
    	preExtract.SetSectionConcepts();
    	FindStockEquityComponents(NodeChain);
		if(bWriteOutFile) {
		    try {
			    FileWriter outFile = new FileWriter(outFileName);
	            out = new PrintWriter(outFile);
				xsdExtract.WriteExtractedInfo(out);
				defExtract.WriteExtractedInfo(out);
				labExtract.WriteExtractedInfo(out);
				preExtract.WriteExtractedInfo(out);
				WriteNodeChain(out, NodeChain, "Node Chain");
				WriteNodeChain(out, ParentheticalChain, "Parentheticals");
				WriteLineItems(out);
				calExtract.WriteExtractedInfo(out);
				xmlExtract.WriteExtractedInfo(out);
	            out.close();
	            outFile.close();
		    }
		    catch (Exception e) {
			    if(out != null)
			        out.println("Error writing XML: " + e.getMessage());
			    else
				    System.out.println("Error writing XML: " + e.getMessage());
		    }
		}
		return(prefix);
	}
	
	
	private void GetNodeDimensions(ArrayList<RootNode> NodeChain) {
	    String posDemension;
	    int    iState = 0;
	    
		for(RootNode rn: NodeChain) {
			iState = CONSTANTS.CHECKBEGINDIMENSION;
    		iState = CheckChildren(rn, rn.GetChildren(), iState);	
		}
	}
	
	private int CheckChildren(RootNode rn , ArrayList<PreNode> children, int iState) {
	    
	    for(PreNode child: children) {
	        iState = CheckForEvent(child.GetId(), iState);
	        switch(iState) {
	            case CONSTANTS.CHECKBEGINDIMENSION:
	        	    iState = CheckChildren(rn, child.GetChildren(), iState);
	        	    break;
	        	
	            case CONSTANTS.BEGINENDDIMENSION:
	                iState = CONSTANTS.CHECKENDDIMENSION;
	                iState = CheckChildren(rn, child.GetChildren(), iState);
	                break;
	                
	            case CONSTANTS.CHECKENDDIMENSION:
	            	rn.AddDimension(child.GetId());
	                iState = CheckChildren(rn, child.GetChildren(), iState);
	            	break;
	            	
	        }
	    }
	    return(iState);
	}

	private int CheckForEvent(String curId, int preState) {
		int iRtn;
		
		iRtn = preState;  // worst case no event sensed
		switch (preState) {
		    case CONSTANTS.CHECKBEGINDIMENSION:
			    if(curId.endsWith(CONSTANTS.BEGINDIMENSION))
				    iRtn = CONSTANTS.BEGINENDDIMENSION;
			    else {
				    if(curId.endsWith(CONSTANTS.NODIMENSION))
					    iRtn = CONSTANTS.FOUNDNODIMENSION;
			    }
			    break;
			    
		    case CONSTANTS.CHECKENDDIMENSION:
		    	if(curId.endsWith(CONSTANTS.ENDDIMENSION))
		    		iRtn = CONSTANTS.FOUNDENDDIMENSION;
		    	break;
			
		}
		return(iRtn);
	}
	
	private void StripParentheticals() {
		RootNode rootNode =null;
		boolean  bFoundStart = false;
		ArrayList<RootNode> newNodes = new ArrayList<RootNode>();
		
		for(RootNode thisNode: ParentheticalChain) {
			bFoundStart = false;
			rootNode =  new RootNode();
			//rootNode.SetRootId(ExtractRootSection(thisNode.GetRootId()));
			rootNode.SetRootId(thisNode.GetRootId());
			for(PreNode preNode: thisNode.GetChildren()) {
				bFoundStart = ChaseParentheticalChain(preNode, bFoundStart, rootNode);
			}
			for(LineItem li: thisNode.GetLineItems()) {
				rootNode.AddLineItem(li.GetItemStr(), li.GetIsParenthetical());
			}
			newNodes.add(rootNode);
		}
		int i = ParentheticalChain.size() -1;
		while(i >= 0) {
			ParentheticalChain.remove(i);
			i--;
		}
		for(RootNode newNode: newNodes) {
		    ParentheticalChain.add(newNode);
		}
	}
	
	private String ExtractRootSection(String testStr) {
		int    i;
		String rtnStr = "";
		
		i = testStr.lastIndexOf("_");
		rtnStr = testStr.substring(0, i);
		return(rtnStr);
	}
	private boolean ChaseParentheticalChain(PreNode child, boolean passedState, RootNode rootNode) {
		boolean bRtn = passedState;
		
		if(bRtn == true)
			rootNode.AddChild(child);
		else {
			if(child.GetId().toLowerCase().contains("statementlineitems")) {
				bRtn = true;
			}
		}
		for(PreNode preNode: child.GetChildren()) {
			bRtn = ChaseParentheticalChain(preNode, bRtn, rootNode);
		}		
	    return(bRtn);
	}
	
	private void MoveParentheticals() {
		int     i = NodeChain.size() -1;
		boolean bFound = false;
		PreNode foundNode = null;
		boolean bContinue = true;
		int     iCounter = 0;
		PreNode child = null;
		String  extractStr;
		
		while(i >= 0) {
			if(NodeChain.get(i).GetRootId().toLowerCase().contains("parenthetical")) {
				ParentheticalChain.add(NodeChain.get(i));
				NodeChain.remove(i);
			}
			i--;
		}
		i = 0;
		SetParentheticalChain();
    	StripParentheticals();  // this removes extraneous PreNodes
    	for(i =0; i < ParentheticalChain.size(); i++) {
    		int j = 0;   // first we find our section
    	    extractStr = ExtractRootSection(ParentheticalChain.get(i).GetRootId());	
    		while(NodeChain.get(j).GetRootId().contains(extractStr) == false) {
    			j++;
    		}
    		// we have our node now find 
    		child = NodeChain.get(j).GetChildren().get(iCounter);
    		foundNode = ChaseChainForStatement(child);
    		SearchForConcrete(foundNode, ParentheticalChain.get(i).GetChildren());
    	}
    	/*
		while(bContinue == true) {
			while(iCounter < NodeChain.get(i).GetChildren().size()) {
				child = NodeChain.get(i).GetChildren().get(iCounter);
				foundNode = ChaseChainForStatement(child);
				if(foundNode != null)
					break;
				iCounter++;
			}
			if(iCounter >= NodeChain.get(i).GetChildren().size())
				bContinue = false;
			if(foundNode != null) {
			    SearchForConcrete(foundNode);
			    iCounter++;  // skip past the one we just processes
			    foundNode = null;
			}
		}
		*/
	}
	
	private PreNode ChaseChainForStatement(PreNode children) {
		PreNode rtnNode = null;
		
		if(children.GetId().toLowerCase().contains("statementlineitems"))
			rtnNode = children;
		else {
			for(PreNode grandChild: children.GetChildren()) {
				rtnNode = ChaseChainForStatement(grandChild);
				if(rtnNode != null)
					break;
			}
		}
		
		return(rtnNode);
	}
	
	private void SearchForConcrete(PreNode child, ArrayList<PreNode> parentheticals) {
		boolean bMoved = false;
		int     iOrder = 0;
		
		for(PreNode grandChild: child.GetChildren()) {
			bMoved = TestForNonAbstract(grandChild, child, parentheticals);
			iOrder++;
			if(bMoved == true)
				break;			
		}		
	}
	
	private boolean TestForNonAbstract(PreNode child, PreNode parent, ArrayList<PreNode> parentheticals) {
		boolean rtnNdx = false;
		//int     iMyChildOrder = 0;
		
		if(child.GetId().toLowerCase().contains("abstract") == false) {
		    // now move in the parentheticals
			/*
			int i = 1;
			for(PreNode parenthetical: ParentheticalChain.get(0).GetChildren()) {
				parent.AddThisNode(i, parenthetical);
				i++;
			}
			*/
			int i = 0;
			for(PreNode parenthetical: parentheticals) {
			    parent.AddThisNode(i, parenthetical);
			    i++;
			}
			return(true);
		}
		else {
			for(PreNode grandchild: child.GetChildren()) {
				rtnNdx = TestForNonAbstract(grandchild, child, parentheticals);
				if(rtnNdx == true)
					break;
			}
		}
		return(rtnNdx);	
	}
	
	private void SetParentheticalChain() {
		for(RootNode rootNode: ParentheticalChain) {
			ChaseChain(rootNode.GetChildren());
		}
	}
	
	private void ChaseChain(ArrayList<PreNode> children) {
	    for(PreNode thisNode: children)	{
	    	thisNode.SetIsParenthetical(true);
	    	ChaseChain(thisNode.GetChildren());
	    }
	}
	
	private void OrderNodes() {
		
		for(RootNode root: NodeChain) {
			for(PreNode curNode: root.GetChildren()) {
				SortChildren(curNode);
			}
		}
	}
	
	private void LabelNodes() {
		int     i = 0;
		boolean bFoundFinancial = false;
		int     groupType = CONSTANTS.GP_NONE;
		
		for(RootNode root: NodeChain) {
			 i = 0;
			while(root.GetLabel().length() == 0) {
				if(root.GetRootId().equals(xsdExtract.GetXsdRoleTypes().get(i).GetRoleUri())) {
					root.SetLabel(xsdExtract.GetXsdRoleTypes().get(i).GetData());
					groupType = DetermineGroupType(root.GetLabel(), bFoundFinancial);
					root.SetGroupType(groupType);
					if(groupType == CONSTANTS.GP_FINANCIAL)
						bFoundFinancial = true;
				}
				i++;				
			}
		}
		
	}
	
	private static final String Statement = "- Statement";
	private static final String Parenthetical = "(Parenthetical";
	private static final String Table = "(Table";
	private static final String Detail = "(Detail";
	private static final String Policies = "(Policies";
	private static final String Entity = "Document And Entity Information";
	
	private int DetermineGroupType(String origStr, boolean bFoundFinancial) {
		int iRtn = CONSTANTS.GP_NONE;
		
		if((origStr.contains(Statement)) || (origStr.contains(Parenthetical))) {
			iRtn = CONSTANTS.GP_FINANCIAL;
		}
		else {
			if(bFoundFinancial == false)
				iRtn = CONSTANTS.GP_COVER;
			else {
				if(origStr.contains(Table))
					iRtn = CONSTANTS.GP_NOTE_TABLE;
				else {
					if(origStr.contains(Policies))
						iRtn = CONSTANTS.GP_POLICIES; 
					else {
					    if(origStr.contains(Detail)) {
						    int j = origStr.indexOf(Detail) + Detail.length() + 1;  // one to adjust for size
					        if(j < origStr.length())
						        iRtn = CONSTANTS.GP_NOTE_TBL_DETAIL;
					        else
					    	    iRtn = CONSTANTS.GP_NOTE_DETAIL;
				        }
					    else  {
							if(origStr.contains(Entity))  {
								iRtn = CONSTANTS.GP_DEI_STATEMENTS;
							}
							else 
							    iRtn = CONSTANTS.GP_NOTE;
					    }
					}
				}
			}
		}
		
		return(iRtn);
	}
	
	private void SortChildren(PreNode curNode) {
		
		if(curNode.GetChildren().size() > 1) {   // sort
			Collections.sort(curNode.GetChildren(), new customComparator());
			for(PreNode child: curNode.GetChildren()) {
				SortChildren(child);
			}
		}

	}
	
	private void GetLineItems() {
		boolean bFoundLineItems = false;
		
		for(RootNode root: NodeChain) {
			CheckForLineItems(root, root.GetChildren(), bFoundLineItems);
		}
	}
	
	public boolean CheckForLineItems(RootNode root, ArrayList<PreNode> preNodes, boolean bFoundLineItems) {
		
		for(PreNode preNode: preNodes) {
			if(bFoundLineItems == true)
				root.AddLineItem(preNode.GetId(), preNode.GetIsParenthetical());
			else {
				String lowered = preNode.GetId().toLowerCase();
				if(lowered.contains("us-gaap_statementlineitems"))
					bFoundLineItems = true;
			}
			bFoundLineItems = CheckForLineItems(root, preNode.GetChildren(), bFoundLineItems);
		}
		return(bFoundLineItems);
	}
	
	private String STOCKEQUITYCOMPONENT = "statementequitycomponentsaxis";
	
	private void FindStockEquityComponents(ArrayList<RootNode> thisChain) {
		boolean bContinue = true;
		
		for(RootNode rNode: thisChain) {
			bContinue = CheckChildren(rNode.GetChildren());
			if(bContinue == false)
				break;
		}
	}
	
	private boolean CheckChildren(ArrayList<PreNode> children) {
		boolean bContinue = true;

	    for(PreNode child: children) {
		   if(child.GetId().toLowerCase().contains(STOCKEQUITYCOMPONENT)) {
			   ExtractEquityInfo(child.GetChildren());
			   bContinue = false;
		   }
		   if(bContinue)
		    bContinue = CheckChildren(child.GetChildren());
		   if(bContinue == false)
			   break;
	   }
       return(bContinue);
	}
	
	private void ExtractEquityInfo(ArrayList<PreNode> children) {
		String temp;
		int    i;
		
		for(PreNode child: children) {
			temp = child.GetId();
			i = temp.indexOf("#");
			temp = temp.substring(i+1);
			temp = temp.replaceFirst("_", ":");
			preExtract.AddEquityComponent(temp);
		}
		
	}
	
	private void WriteNodeChain(PrintWriter out, ArrayList<RootNode> thisChain, String caption) {
		
    	out.println(caption);
    	for(RootNode rNode: thisChain) {
    		out.println("ROOT: " + rNode.GetRootId());
    		WriteNodeChildren(out, rNode.GetChildren(), 3);
    		out.println("");
    		out.println("END CHAIN");
    		out.println("");
    		out.println("");
    	}
     	out.println("");
    	out.println("");


	}
	
	
	private void WriteNodeChildren(PrintWriter out, ArrayList<PreNode> children, int indent) {
	    String spacer = "";
	    
	    for(int i = 0; i < indent; i++)
	    	spacer += " ";
	    for(PreNode child: children) {
		   out.println(spacer + child.GetOrder() + " - "+ child.GetId());
		   WriteNodeChildren(out, child.GetChildren(), indent + 3);
	   }
	}
	
	private void WriteLineItems(PrintWriter out) {
		
    	out.println("Line Items");
    	for(RootNode rNode: NodeChain) {
    		out.println("ROOT: " + rNode.GetRootId());
    		for(LineItem curItem: rNode.GetLineItems()){
    		    out.println(curItem.GetItemStr());
    		}
     		out.println("");
    		out.println("END Line Items");
    		out.println("");
    		out.println("");
    	}
     	out.println("");
    	out.println("");
	}
	
	private void ChainNodes(XsdExtract xsdExtract) {
		//this is the list of sections in presentation
		ArrayList<PreGroup> preGroups = preExtract.GetPreGroups();
		String              testStr;
		RootNode            curNode = null;
		boolean             bFoundEntry = false;
		PreNode             child = null;
		
    	for(PreGroup curGroup: preGroups) {
    		testStr = curGroup.GetRole().toLowerCase();
     		//if(testStr.contains("documentandentityinformation") == false){
     			curNode = new RootNode();
     			curNode.SetRootId(curGroup.GetRole());
     			NodeChain.add(curNode);
				PreNode preNode = new PreNode();
				preNode.SetId("us-gaap_StatementTable");
				preNode.SetOrder(0);
				curNode.AddChild(preNode);
				bFoundEntry = false;
     			for(PreLink preLink: curGroup.GetLinks()) {
     				if(preLink.GetUriFrom().contains("us-gaap_StatementTable")) {
     					if(bFoundEntry == false) {
     						preNode.SetId(preLink.GetUriFrom());
     						bFoundEntry = true;
     					}
     					child = new PreNode();
     					child.SetId(preLink.GetUriTo());
     					child.SetOrder(preLink.GetOrder());
     					preNode.addNode(child);
     				}
     			}
				FindChildren(curGroup.GetLinks(), preNode);
     		//}
    	}
	}
	
	//private boolean CheckForExistingNode(ArrayList)
	private void FindChildren(ArrayList<PreLink> PreLinks, PreNode preNode) {
	
		String  parentId = "";
		PreNode newNode = null;
		
		 // this loop checks all the children
		for(PreNode thisNode: preNode.GetChildren()) {
			parentId = thisNode.GetId();
			//now see if we find a matching entry
			for(PreLink thisLink: PreLinks) {
				if(thisLink.GetUriFrom().equals(parentId)) { // found a child
					if(DoesNodeExist(thisLink.GetUriTo(), preNode) == false) {
						newNode = new PreNode();
						newNode.SetId(thisLink.GetUriTo());
						newNode.SetOrder(thisLink.GetOrder());
						thisNode.addNode(newNode);
					}
				}
			}
			if(thisNode.GetChildren().size() > 0)  // only look for grandchildren when have children
			    FindChildren(PreLinks, thisNode);
		}
/*********		for(PreLink preLink: PreLinks) {
			if(preLink.GetUriFrom().equals(childId)) {
				//if(DoesNodeExist(preLink.GetUriFrom()) == false)
				//PreNode newNode = new PreNode();
				//newNode.SetId(preLink.GetUriFrom());
				//newNode.SetOrder(preLink.GetOrder());
				//preNode.addNode(newNode);
				//FindChildren(PreLinks, newNode, preLink.GetUriTo(), preLink.GetOrder());
			}
		}
		**************/
	}
	
	private boolean DoesNodeExist(String ToLink, PreNode preNode) {
		boolean bRtn = false;
		
		for(PreNode child: preNode.GetChildren()) {
			if(child.GetId().equals(ToLink))
				bRtn = true;
		}
		return(bRtn);
	}
}

