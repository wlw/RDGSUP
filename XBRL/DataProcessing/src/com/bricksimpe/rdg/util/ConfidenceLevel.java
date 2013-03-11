package com.bricksimpe.rdg.util;
import java.util.ArrayList;
import java.util.Collections;

public class ConfidenceLevel {
		
	public int Levenshtein ( String s, String t) {
		int n = s.length();
		int m = t.length();
		
		if(n== 0){
			return m;
		} else if (m == 0){
			return n;
	    }
		int p[] = new int[n+1]; //'previous' cost array, horizontally
		  int d[] = new int[n+1]; // cost array, horizontally
		  int _d[]; //placeholder to assist in swapping p and d

		  // indexes into strings s and t
		  int i; // iterates through s
		  int j; // iterates through t

		  char t_j; // jth character of t

		  int cost; // cost

		  for (i = 0; i<=n; i++) {
		     p[i] = i;
		  }
				
		  for (j = 1; j<=m; j++) {
		     t_j = t.charAt(j-1);
		     d[0] = j;
				
		     for (i=1; i<=n; i++) {
		        cost = s.charAt(i-1)==t_j ? 0 : 1;
		        // minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
		        d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
		     }

		     // copy current distance counts to 'previous row' distance counts
		     _d = p;
		     p = d;
		     d = _d;
		  } 
				
		  // our last action in the above loop was to switch d and p, so p now 
		  // actually has the most recent cost counts
		  return p[n];		
	}
	
	public ArrayList<String> RecurringSting( String str) {  // static
		ArrayList<String> rtn = wordLetterPairs(str.toUpperCase());
		return rtn;
	}
	
	public double compareToArrayList(String str1, ArrayList<String> pairs2) {
		    ArrayList<String>  pairs3 = new ArrayList<String>();
		    if(str1.length() == 0)
		    	return 0;
	        ArrayList<String> pairs1 = wordLetterPairs(str1.toUpperCase());
	        for(int i = 0; i < pairs2.size(); i++) {
	        	pairs3.add(pairs2.get(i));
	        }
	        //ArrayList pairs2 = wordLetterPairs(str2.toUpperCase());
	        int intersection = 0;
	        int union = pairs1.size() + pairs2.size();
	        for (int i = 0; i < pairs1.size(); i++) {
	            Object pair1 = pairs1.get(i);
	            for (int j = 0; j < pairs3.size(); j++) {
	                Object pair3 = pairs3.get(j);
	                if (pair1.equals(pair3)) {
	                    intersection++;
	                    pairs3.remove(j);
	                    break;
	                }
	            }
	        }
	        return (2.0 * intersection) / union;
	    }
	
    public double compareStrings(String str1, String str2) {  // static
    	if(str1.length() == 0)
    		return 0;
        ArrayList<String> pairs1 = wordLetterPairs(str1.toUpperCase());
        ArrayList<String> pairs2 = wordLetterPairs(str2.toUpperCase());
        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i = 0; i < pairs1.size(); i++) {
            Object pair1 = pairs1.get(i);
            for (int j = 0; j < pairs2.size(); j++) {
                Object pair2 = pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }
            }
        }
        return (2.0 * intersection) / union;
    }

    /**
     * @return an ArrayList of 2-character Strings.
     */
    private ArrayList<String> wordLetterPairs(String str) { // static
        ArrayList<String> allPairs = new ArrayList<String>();
        // Tokenize the string and put the tokens/words into an array
        String[] words = str.split("s");
        // For each word
        for (int w = 0; w < words.length; w++) {
            // Find the pairs of characters
            String[] pairsInWord = letterPairs(words[w]);
            for (int p = 0; p < pairsInWord.length; p++) {
                allPairs.add(pairsInWord[p]);
            }
        }
        return allPairs;
    }

    /**
     * @return an array of adjacent letter pairs contained in the input string
     */
    private String[] letterPairs(String str) { // static
        int numPairs = str.length() - 1;
        String[] pairs = new String[numPairs];
        for (int i = 0; i < numPairs; i++) {
            pairs[i] = str.substring(i, i + 2);
        }
        return pairs;
    }

}
