package com.bricksimple.rdg.xbrlUpload;


import java.sql.Connection;
import java.util.ArrayList;

import com.bricksimpe.rdg.XbrlTemplateId.XbrlTemplateIdentifier;
import com.bricksimpe.rdg.Xbrlutil.XbrlEdgarExtractor;
import com.bricksimple.rdg.ExtractedClasses.*;
import com.bricksimple.rdg.sqlaccess.MySqlAccess;
import com.bricksimple.rdg.pushxbrl.PushXbrl;
import com.bricksimple.rdg.pushxbrl.SubmissionInfo;
//import java.text.NumberFormat;


public class XbrlUpload {

	//import com.bricksimple.rdg.XbrlUpload.TemplateIdentifier;

	//public class XbrlUpload {
	    private String privInputFile = "";
	    private String privCompanyUid = "";
		private String privServerName = "";
		private String privPortNumber = "";
		private String privInstance = "";
		private String privDataBase = "";
	    private String privUserName = "";
	    private String privPassword = "";
	    private int    privNewVersion = 0;
		private Boolean useWindows = false;
		public void setPrivUseWindows(Boolean privOutputFile) {
			this.useWindows = privOutputFile;
		}
		public Boolean getPrivUseWindows() {
			return useWindows;
		}

		public XbrlUploadResults RunThis()
		{
			XbrlUploadResults   xbrlUploadResults = null;
			
			xbrlUploadResults = run(getPrivInputFile(), getPrivCompanyUid(),
					                getPrivServerName(), getPrivInstance(), 
					                getPrivPortNumber(), getPrivDataBase(),
					                getPrivUserName(), getPrivPassword(),
					                getPrivUseWindows());
		    return(xbrlUploadResults);
		}
		  
		public static void main(String[] args) {
			
			String                    Password = "";
			String                    InputFile = args[0];
			String                    inputCompanyUid = args[1];
			String                    ServerName = args[2];
			String                    PortNumber = args[3];
			String                    Instance = args[4];
			String                    DataBase = args[5];
			String                    UserName = args[6];
			boolean useWindows = false;
			if(args.length > 7)
				Password = args[7];
				useWindows = Boolean.valueOf(args[8]);
			//int                       NewVersion = Integer.parseInt(args[8]);
			//XbrlUploadResults   xbrlUploadResults = null;
			XbrlUpload main = new XbrlUpload();
			
			main.run(InputFile, inputCompanyUid, ServerName, Instance, PortNumber,
			         DataBase, UserName, Password, useWindows);
		}

		private XbrlUploadResults run(String InputFile, String inputCompanyUid, String ServerName,  // static
				                String Instance, String PortNumber, String DataBase,
				                String UserName, String Password, boolean useWindows) {

			boolean                 debugFlag = false;
			
			XbrlUploadResults       xbrlUploadResults = new XbrlUploadResults();
            int                     iRtn = 0;
			Connection              con = null;
			MySqlAccess             mysq = new MySqlAccess();
			XbrlFileStruct          xbrlFileStruct = new XbrlFileStruct();
			Zipping                 zipping = new Zipping();
			boolean                 bWriteOutFile = true;
			boolean                 bWriteFileDetails = false;
			XbrlExtracted           xbrlExtracted = new XbrlExtracted();
			PushXbrl                pushXbrl = new PushXbrl();
			FilingInfo              filingInfo = new FilingInfo();
			SubmissionInfo          subInfo = null;           
			int                     CompanyTaxonomyUid = 0;
			ErrorCls                errorCls = new ErrorCls();
			MySqlAccess             mySqlAccess = new MySqlAccess();
			Version                 version = new Version();
			String                  exchangeSymbol = "";
			ArrayList<String>       FilesToUnZip = new ArrayList<String>();
			String                  path = "";
			Debug                   debug = new Debug();
			int                     SupportRtn = 0;
			SubmissionList          thisSub = null;
			UTILITIES               utilities = new UTILITIES();
			ArrayList<RoleGaapXref> roleGaapXref = new ArrayList<RoleGaapXref>();
			int                     userSuppliedCompanyUid = 0;
			
			utilities.MemoryUsage("Run");
			errorCls.setFunctionStr("run");
			errorCls.setCompanyUid(0);
			errorCls.setItemVersion(0);
			errorCls.setSubUid(0);
			debug.InitDebug(debugFlag);
			try {
			    con = mysq.OpenConnection(ServerName, Instance, PortNumber, DataBase, UserName, Password, useWindows);
			    if(con == null)  {
			    	xbrlUploadResults.error = -1;
			    	return(xbrlUploadResults);
			    }
			    else {
			    	roleGaapXref = mySqlAccess.GetRoleGaapRefs(con);
					errorCls.setErrorText("Starting XbrlUpLoad Ver: " + version.getVersion());
					mySqlAccess.WriteAppError(con, errorCls);
					int j = InputFile.lastIndexOf("\\");
					path = InputFile.substring(0, j+ 1);
					FilesToUnZip = zipping.FindZipFiles(con, InputFile, path);
					if(FilesToUnZip.size() == 1)
						userSuppliedCompanyUid = utilities.ConvertStringToInt(con, inputCompanyUid);
					for(String thisZip: FilesToUnZip) {
						try {
							xbrlFileStruct = new XbrlFileStruct();
							xbrlExtracted = new XbrlExtracted();
			    	        xbrlFileStruct = zipping.UnZip(con,  thisZip);
			    	        filingInfo.ExtractInfo(xbrlFileStruct.GetXbrlFiles(), xbrlFileStruct.GetFilePath());
							errorCls.setErrorText("Processing: " + filingInfo.GetSrcFile());
							mySqlAccess.WriteAppError(con, errorCls);
			    	        iRtn = xbrlFileStruct.GetStatus();
			    	        if( iRtn == 0) {
			    		        iRtn = xbrlFileStruct.OpenDoms(con, mysq);
			    		        TestIterator testIterator = new TestIterator();
			    		        testIterator.Iterate(xbrlFileStruct, bWriteFileDetails);
			    		        exchangeSymbol = xbrlExtracted.DoExtraction(con, xbrlFileStruct, filingInfo.GetCompanyAbbrev(), bWriteOutFile);
			    		        subInfo = pushXbrl.WriteBasicSubInfo(con, xbrlExtracted.xmlExtract, xbrlExtracted.NodeChain, 
			    		        		                             filingInfo, xbrlExtracted.xsdExtract, exchangeSymbol, xbrlExtracted.WebAddress,
			    		        		                             userSuppliedCompanyUid);
			    		        if(subInfo.GetCompanyUid() != 0) {
			    		            CompanyTaxonomyUid = mysq.ValidateTaxonomyExists(con, subInfo.GetCompanyUid());
			    		            //mysq.WriteCustomElements(con, CompanyTaxonomyUid, xbrlExtracted.xsdExtract.GetXsdCustomElements(),
			    		            //		                 xbrlExtracted.xsdExtract);
			    		            pushXbrl.DoPush(con, xbrlExtracted.xsdExtract, xbrlExtracted.defExtract, 
			    				                         xbrlExtracted.labExtract, xbrlExtracted.preExtract, 
			    				                         xbrlExtracted.calExtract, xbrlExtracted.xmlExtract, filingInfo,
			    				                         xbrlExtracted.NodeChain, xbrlExtracted.ParentheticalChain, subInfo, CompanyTaxonomyUid,
			    				                         exchangeSymbol, roleGaapXref, debug);
			    		            if(xbrlFileStruct.GetHaveHtm() == true)
			    		                SupportRtn = RunExternalProcedures(InputFile, ServerName, Instance, PortNumber, DataBase,
			    			            	                               UserName, Password, subInfo, con);
				    	        	thisSub = new SubmissionList();
				    	        	thisSub.zipDirectoryName = thisZip;
				    	        	thisSub.uid = subInfo.GetSubmissionUid();
				    	        	mysq.RemoveUnMappedNotes(con, subInfo);
			    		            if(SupportRtn == 0) 
					    	        	xbrlUploadResults.successfulSubmissions.add(thisSub);
			    		            else
					    	        	xbrlUploadResults.failedSubmissions.add(thisSub);
			    		        }
			    		    }
			    	        else {   // failed on unzip
			    	        	thisSub = new SubmissionList();
			    	        	thisSub.zipDirectoryName = thisZip;
			    	        	thisSub.uid = 0;
			    	        	xbrlUploadResults.failedSubmissions.add(thisSub);
			    	        	
			    	        }
			    	    }
						catch (Exception e) {
							errorCls.setErrorText("Error processing: " + thisZip + "::" + e.getMessage());
							mySqlAccess.WriteAppError(con, errorCls);
		    	        	thisSub = new SubmissionList();
		    	        	thisSub.uid = 0;
		    	        	thisSub.zipDirectoryName = thisZip;
		    	        	xbrlUploadResults.failedSubmissions.add(thisSub);
						}
			        }
			    }
			}
			catch (Exception e) {
				errorCls.setErrorText(e.getMessage());
				mySqlAccess.WriteAppError(con, errorCls);
				
				iRtn = 1;
			}
			errorCls.setErrorText("XbrlUpLoad completed");
			mySqlAccess.WriteAppError(con, errorCls);
			return(xbrlUploadResults);
		}

		private int RunExternalProcedures(String InputFile, String ServerName, String Instance, String PortNumber,
				                           String databaseName, String userName, String password, SubmissionInfo subInfo,
				                           Connection con) {
			
			  int iRtn = 1;
			  int Uid ;
			  
		      XbrlEdgarExtractor edgarExtractor = new XbrlEdgarExtractor( );
		      edgarExtractor.setPrivDisplayName( subInfo.GetCompanyName());
		      edgarExtractor.setPrivInputFile( subInfo.GetFullSrcFile() );
		      String edgarFilePath = subInfo.GetFullSrcFile().replace(".htm", ".txt");
		      edgarExtractor.setPrivOutputFile( edgarFilePath );
		      edgarExtractor.setPrivCompanyName( subInfo.GetCompanyName() );
		      edgarExtractor.setPrivUserName( userName );
		      edgarExtractor.setPrivPassword( password );
		      edgarExtractor.setPrivUseWindows( useWindows );
		      edgarExtractor.setPrivDataBase( databaseName );
		      edgarExtractor.setPrivServerName( Instance );  //test ?? WAS Instance
		      edgarExtractor.setPrivPortNumber( PortNumber );
		      edgarExtractor.setPrivInstance( ServerName );
		      edgarExtractor.setPrivPrevUid( Integer.toString(subInfo.GetSubmissionUid() ));
		      edgarExtractor.setPrivCon(con);
		      Uid = edgarExtractor.RunThis( );
              if(Uid != 0) {
		          XbrlTemplateIdentifier templateIdentifier = new XbrlTemplateIdentifier( );
		          templateIdentifier.setPrivInputFile( edgarFilePath );
		          templateIdentifier.setPrivCreateOutputFile( "N" );
		          templateIdentifier.setPrivUserName( userName );
		          templateIdentifier.setPrivPassword( password );
		          templateIdentifier.setPrivUseWindows( useWindows );
		          templateIdentifier.setPrivDataBase( databaseName );
		          templateIdentifier.setPrivServerName( Instance );
		          templateIdentifier.setPrivPortNumber( PortNumber );
		          templateIdentifier.setPrivInstance( ServerName );
		          templateIdentifier.setPrivSubUid(subInfo.GetSubmissionUid());
		          templateIdentifier.setPrivVersion( 1 );
		          templateIdentifier.setPrivCon(con);
		          iRtn = templateIdentifier.RunThis( );
              }
              return(iRtn);
		}
		
		/* SUPPORT ROUTINES FOR SETTING PARAMETERS */
		
		public void setPrivInputFile(String inputFile) {
			privInputFile = inputFile;
		}

		public String getPrivInputFile() {
			return privInputFile;
		}
		
		public void setPrivCompanyUid(String companyUid) {
			privCompanyUid = companyUid;
		}
		
		public String getPrivCompanyUid() {
			return privCompanyUid;
		}

		public void setPrivServerName(String privServerName) {
			this.privServerName = privServerName;
		}
		
		public String getPrivServerName() {
			return privServerName;
		}
		
		public void setPrivPortNumber(String privPortNumber) {
			this.privPortNumber = privPortNumber;
		}
		
		public String getPrivPortNumber() {
			return privPortNumber;
		}
		
		public void setPrivDataBase(String privDataBase) {
			this.privDataBase = privDataBase;
		}
		
		public String getPrivDataBase() {
			return privDataBase;
		}

		public void setPrivUserName(String privUserName) {
			this.privUserName = privUserName;
		}

		public String getPrivUserName() {
			return privUserName;
		}

		public void setPrivPassword(String privPassword) {
			this.privPassword = privPassword;
		}

		public String getPrivPassword() {
			return privPassword;
		}

		public void setPrivInstance(String privInstance) {
			this.privInstance = privInstance;
		}

		public String getPrivInstance() {
			return privInstance;
		}

		public void setPrivNewVersion(int privNewVersion) {
			this.privNewVersion = privNewVersion;
		}

		public int getPrivNewVersion() {
			return privNewVersion;
		}
	
}
