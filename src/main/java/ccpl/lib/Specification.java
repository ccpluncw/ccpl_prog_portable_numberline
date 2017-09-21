package ccpl.lib;

import java.io.*;
import java.util.*;

public class Specification {
		public Specification (String s) {
			allSpecs = s;
                       
		}
   
		public Specification () { }
                
                public static String stripComments(String line){
                    int idx = 0;
                    if((idx = line.indexOf("//")) >= 0){
                        line = line.substring(0, idx).trim();
                    }
                    return line;
                }
		
		public void writeData(PrintWriter out) throws IOException {
			out.println(allSpecs);
		}

		public boolean readData(BufferedReader in) throws IOException {
                        if((allSpecs = in.readLine()) != null){
                            allSpecs = stripComments(allSpecs);
                            return true;
                        }else{
                            return false;
                        }
                }

		public void print() {
                    System.out.println(allSpecs);
		}

                public boolean isEmpty(){
                    if(allSpecs.equals(""))
                        return true;
                    else
                        return false;
                }

		
/***			The following method, and the ones below it, return one piece of
****			information from the input string.  You specify the token number, and the 
****      method returns the info at that point in the string.  Use the appropriate
****			method for the type of info you are returning, i.e., String, int, double,
****			or char.
***/
		public String getParsedStringSpec (int stringPosition) {
			int i;
			String outString = new String ();

			StringTokenizer t = new StringTokenizer(allSpecs, "\t");
			
			for (i=0; i<stringPosition; i++) {
				outString = t.nextToken();
			}
			return outString;
		}
	
		public int getParsedIntSpec (int stringPosition) {
			int i;
			int out = 0;
			String outString = new String ();

			StringTokenizer t = new StringTokenizer(allSpecs, "\t");
			
			for (i=0; i<stringPosition; i++) {
				outString = t.nextToken();
			}
			out = Integer.parseInt(outString);
			return out;
		}

		public double getParsedDoubleSpec (int stringPosition) {
			int i;
			double out = 0.0;
			String outString = new String ();
			
			StringTokenizer t = new StringTokenizer(allSpecs, "\t");
			
			for (i=0; i<stringPosition; i++) {
				outString = t.nextToken();
			}
			out = Double.parseDouble(outString);
			return out;
		}

		public char getParsedCharSpec (int stringPosition) {
			int i;
			String outString = new String ();
			
			StringTokenizer t = new StringTokenizer(allSpecs, "\t");
			
			for (i=0; i<stringPosition; i++) {
				outString = t.nextToken();
			}
			char outChar = outString.charAt(0);
			return outChar;
		}
		
/***			This returns the entire string, unparsed.
***/		
		public String getAllSpecs () {
			return allSpecs;
		}
		
		private String allSpecs;
}

