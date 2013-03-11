package BatchSubmission;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Commons {

	public String ConvertLengthToString(long lOrig) {
		String rtnStr = "";
		long   KiloBytes = lOrig / 1000;
		long   MegaBytes = 0;
		
		if(KiloBytes > 999) {
			MegaBytes = KiloBytes/1000;
			KiloBytes = KiloBytes % 1000;
		}
		if(MegaBytes > 0)
			rtnStr += String.valueOf(MegaBytes) + ",";
		rtnStr += String.valueOf(KiloBytes);
		return(rtnStr);
	}
	
	public String GetDuration(String beginTimeStr, String endTimeStr) {
		String rtnStr = "";
		String beginTime = "";
		String endTime = "";
		
		//first establish that we have the appropriate strings
		if((beginTimeStr.contains("invoking Edgar version:"))  && (endTimeStr.contains("Exit FieldId version:"))) {
			//okay now extract time strimg
			rtnStr = "";
			beginTime  = ExtractTimeStr(beginTimeStr);
			endTime =  ExtractTimeStr(endTimeStr);
			rtnStr = DeltaTimes(beginTime, endTime);
		}
		return(rtnStr);
	}
	
	public String ExtractTimeStr(String orig) {
		String rtnStr = "";
		int    i;
		
		i = orig.indexOf(":");
		rtnStr = orig.substring(i+1).trim();
		i = rtnStr.indexOf(" : ");
		rtnStr = rtnStr.substring(0, i).trim();
		return(rtnStr);
	}
	
	public String DeltaTimes(String beginTime, String endTime) {
		String rtnStr = "";
		
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		try {
		 Date dateBegin = formatter1.parse(beginTime);
		 Date dateEnd   = formatter2.parse(endTime);
		 long duration = (dateEnd.getTime() - dateBegin.getTime())/1000;   // we be in seconds
		 long  seconds = duration % 60;
		 long minutes = duration/60;
		 if(minutes > 0)
		     rtnStr = ":DURATION of Parse = " + minutes + " Min and " + seconds + " seconds"; 
		 else
			 rtnStr = ":DURATION of Parse = " + seconds + " seconds"; 
		}
		catch (Exception e) {
			rtnStr = "Invalid date formats";
		}
		return(rtnStr);
	}
	
	public boolean CopyFile(String fileName, String sourcePath, String destPath) {
		boolean bRtn = true;
		/*****
		    File sourceFile = new File( sourcePath + "\\" + fileName );
		    File destinationFile = new File( destPath + "\\" + fileName );
		    
		    try
		    {
		      FileInputStream sourceInputStream = new FileInputStream( sourceFile );
		      FileOutputStream destinationOutputStream = new FileOutputStream( destinationFile );
		      
		      destinationOutputStream.getChannel( ).transferFrom( sourceInputStream.getChannel( ), 0, sourceInputStream.getChannel( ).size( ) );
		      
		      destinationOutputStream.close( );
		      sourceInputStream.close( );
		    }
		    catch ( FileNotFoundException e )
		    {
		      System.out.println( "moveFile::File not found exception: " + e.getMessage( ) );
		      return false;
		    }
		    catch ( IOException e )
		    {
		      System.out.println( "moveFile::IOexception: " + e.getMessage( ) );
		      return false;
		    }
		    ************/
		    return true;

		    //return ( sourceFile != null && destinationFile != null && sourceFile.renameTo( destinationFile ) );
	}
	
	public ArrayList<String> GetListOfFiles(String SourceDirectory) {
		ArrayList<String>  RtnArray = new ArrayList<String>();
		
		/*************
		File       dir = new File(SourceDirectory);
		File[]     listOfFiles = dir.listFiles();
		String     tempFileName;
		boolean    extension;
		
		for(int i = 0; i < listOfFiles.length; i++) {
			tempFileName = listOfFiles[i].getName();
			extension = tempFileName.contains(".htm");
			if(extension == false)
				extension = tempFileName.contains(".html");
			if(extension == true)
			    RtnArray.add(tempFileName);
		}
		***************/
		String SourceFile = SourceDirectory + "\\BatchSubmission.files";
		String CurFile  = "";
		FileInputStream  fstream;
		DataInputStream  in;
		BufferedReader   br;
		
		try {
	        fstream = new FileInputStream(SourceFile);
	        in = new DataInputStream(fstream);
	        br = new BufferedReader(new InputStreamReader(in));
            while((CurFile = br.readLine()) != null) {
                RtnArray.add(CurFile);
            }
            br.close();
            in.close();
            fstream.close();
		}
        catch (Exception e) {
        	RtnArray = null;
        }
		return(RtnArray);
	}

	public String ConstructNoteDetailFileName(String htmlFileName) {
		String rtnStr = "";
		String reverse = new StringBuffer(htmlFileName).reverse().toString();
		
		int i = reverse.indexOf(".");
		reverse =reverse.substring(i+1); // string file extension + '.'
		rtnStr = new StringBuffer(reverse).reverse().toString();
		rtnStr = rtnStr + "_NoteDetails.txt";
		return (rtnStr);
	}
}
