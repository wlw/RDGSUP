package com.bricksimple.rdg.FieldId;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class IdentifiedHtmlSource {

	public int FindSourceOfHtml(String fileName) {
		int              iRtn = -1;
		FileInputStream  fstream;
		DataInputStream  in;
		BufferedReader   br;
		String           strLine;
		
		try {
	        fstream = new FileInputStream(fileName);
	        in = new DataInputStream(fstream);
	        br = new BufferedReader(new InputStreamReader(in));
            while(((strLine = br.readLine()) != null) && (iRtn == -1)) {
            	strLine = strLine.toLowerCase();
                if(strLine.contains("created")) {
                	if(strLine.contains("rdg")) 
                		iRtn = 1;
                	if(strLine.contains("edgarizer"))
                		iRtn = 0;
                }
            }
            br.close();
            in.close();
            if(iRtn == -1)
            	iRtn = 0;
		}
		catch (Exception e) {
			iRtn = 0;
		}
		return(iRtn);
	}
}
