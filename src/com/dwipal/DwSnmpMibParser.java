package com.dwipal;

import java.util.Vector;
import java.io.*;

public class DwSnmpMibParser 
{
	
	String fileName;
	DwSnmpMibRecord currentRec;
	String prevToken,tokenVal="xx",tokenVal2="";
	PipedReader pr;
	PipedWriter pw;
	BufferedReader r;
	FileReader inFile;
	public static int TT_EOL=StreamTokenizer.TT_EOL;
	DwSnmpMibOutputHandler output;
	DwMibParserInterface tokens;
	
	
	DwSnmpMibParser(String fileName,DwMibParserInterface intf)		
	{
		this.fileName = fileName;	
		this.tokens=intf;
	}				
	
	
	int parseMibFile()	
	{
		DwSnmpMibRecord mibRec;
		
		BufferedReader in=null;
		StreamTokenizer st;
		String tok1=" " ,tok2= " ";
		
		try {
			inFile= new FileReader (new File(fileName));
			r = new BufferedReader(inFile);			
			st = new StreamTokenizer(r);				
		}
		catch (Exception e) {
			outputError("File open error : Cannot open file.\n" + e.toString());
			return -1;
		}
		
		String line=" ";
	
		st.resetSyntax();		
		st.eolIsSignificant(true);
		st.wordChars(33,126);
		
		//st.quoteChar('"');
	
		//st.quoteChar('"');
		//st.ordinaryChar(',');
		//st.ordinaryChar(';');

		String t1="a";
		int parseStatus=0;
		int parseStatusTemp=0;
		
		try {
			int flag=0;
			while (getNextToken(st).trim().length() > 0 || st.ttype == TT_EOL) {
				t1=getTokenVal(st);
				//if(t1.indexOf("AtmTrafficDescrParamIndex") != -1) flag++;
				//if(flag>0)  System.out.println(t1 + "   " + parseStatus );
				switch (parseStatus) {
				case 0: { 
						currentRec=new DwSnmpMibRecord();
						if(t1.indexOf("IMPORT")!=-1) { // Skip till ;
							parseStatus =100;
						}
						else						
						if(t1.equals("MODULE-IDENTITY")==true) {
							currentRec.name = prevToken;
							parseStatus =1;
						}
						else						
						if(t1.equals("OBJECT-TYPE")==true) {
							String temp=new String (prevToken.trim());
							temp=temp.substring(0,1);
							if (temp.toLowerCase().equals(temp)) {
								parseStatus=1;
								currentRec.name=prevToken;
							}						
							//System.out.print("*"+prevToken);
						}
						else if(t1.indexOf("OBJECT-GROUP")!=-1) { // Skip till ;
							String temp=new String (prevToken.trim());
							temp=temp.substring(0,1);
							if (temp.toLowerCase().equals(temp)) {
								parseStatus=1;
								currentRec.name=prevToken;		
							}
						}												
						else if(t1.equals("OBJECT")==true) {
							currentRec.name=prevToken;							
							parseStatus=2;
						}						
						else if(t1.equals("::=")==true)
						{
							currentRec.init();
							currentRec.name=prevToken;
							parseStatus=9; // Its a variable
						}					
						continue;
					}						
					
						
				case 1:	{        // GET " ::= " Token
						if(t1.equals("::=") == true)
							parseStatus=3;
						else if(t1.equals("SYNTAX") == true)
							parseStatus=5;
						else if(t1.indexOf("ACCESS")!= -1 )
							parseStatus=6;
						else if(t1.equals("STATUS") == true)
							parseStatus=7;
						else if(t1.equals("DESCRIPTION") == true)
							parseStatus=8;	
						else if(t1.equals("INDEX") == true)
							parseStatus=11;	
						else if(t1.equals("OBJECTS") == true)
							parseStatus=14;						
						continue;
					}						
						
				case 2:	{		// GET "IDENTIFIER "  else reset
						if(t1.equals("IDENTIFIER")==true) {
							parseStatus = 1; // GET ::= next
						}
						else {
							parseStatus=0;							
						}
						continue;
					}
						
				case 3: {		// get Group Name
						if(t1.trim().startsWith("{")==true || t1.trim().length()==0) continue;
						currentRec.parent=t1;
						parseStatus =4;  // next=GET Number.
						continue;				
					}
						
				case 4:	{		// Get sub-Group number
						try {							
							if(t1.trim().endsWith(")")==true) { // for chained server entries
									String numStr="";
									DwSnmpMibRecord newRec=new DwSnmpMibRecord();							
									numStr=t1.substring(t1.indexOf((int)'(')+1,t1.indexOf((int)')'));
									//System.out.println("Adding T1: " + t1 + "  Number Str : " + numStr);
								try {
									newRec.number =Integer.parseInt(numStr.trim());
								}
								catch(Exception ne2) {
									outputError("Error in line " + st.lineno()); 
									continue;
								}
								newRec.name=t1.substring(0,t1.indexOf("("));								
								newRec.parent =currentRec.parent;
								currentRec.parent = newRec.name;
								//System.out.println("Chained Rec. : " + newRec.name + " Parent : "  +  newRec.parent + "." + newRec.number );
								addToken(newRec);
								continue;
							}							
							//System.out.println("Name : "+currentRec.name +"T1 : " + t1);						
							currentRec.number = Integer.parseInt(t1.trim());
							//System.out.println("Rec. Added : " + currentRec.name + "  " + currentRec.parent + "." + currentRec.number );						
							addToken(currentRec);
							parseStatus=0;
							continue;			
						}
						catch(NumberFormatException ne) { 
							outputError("Error in getting number.."+t1+"\n" + ne.toString());						
						}
					}					
						//System.out.println("Record Added...." + currentRec.name );
					
				case 5: {		// Get Syntax data till EOL
						if(t1.indexOf((int)'{') != -1) {
							parseStatus =12;
							currentRec.syntax =currentRec.syntax.concat(" " +t1);
							continue;
						}
								
						if (st.ttype==TT_EOL || st.ttype==st.TT_EOF) {
							currentRec.syntax =currentRec.syntax.concat(t1);						
							if(parseStatusTemp==1 ) {
								//System.out.println("Syntax : "+ currentRec.name + " , " + currentRec.syntax );
								if(currentRec.syntax.indexOf('{') != -1){
									parseStatus=12;
									continue;
								}
								// See if it is a table. if so, set recordtype to 1
								if(currentRec.syntax.trim().startsWith("SEQUENCE") == true) {
									currentRec.recordType=1; 
									currentRec.tableEntry=1;
								}
								//addToken(currentRec);
								parseStatus =1;
								parseStatusTemp =0;
							}							
							continue;
						}						
						//System.out.println("Variable Found : " + currentRec.name+ "  Type : " + currentRec.syntax);						
						currentRec.syntax = currentRec.syntax.concat(" " + t1);
						if(currentRec.syntax.trim().length()>0) parseStatusTemp=1;
						continue;
							
							
						/*	
						if (st.ttype==TT_EOL) {
							parseStatus=1;
							continue;
						}
						currentRec.syntax = currentRec.syntax.concat(" " + t1);
						continue;
*/
					}
				case 6: {		// Get Access Mode Data till EOL
						if (st.ttype==TT_EOL) {
							parseStatus=1;
							continue;
						}
						currentRec.access = currentRec.access.concat(" " + t1);										
						continue;

					}
				case 7: {		// Get Status data till EOL
						if (st.ttype==TT_EOL) {
							parseStatus=1;
							continue;
						}
						currentRec.status = currentRec.status.concat(" " + t1);
						continue;

					}
						
				case 8: {		// Get Description till EOL
						if (st.ttype==st.TT_EOF) {
							break;
						}							
						currentRec.description = currentRec.description.concat(" " + t1 );					
						if(t1.trim().length()!=0) parseStatus=1;
						continue;
					}
				case 9: {		// Record is a variable
						currentRec.recordType = currentRec.recVariable;	
						if(t1.indexOf((int)'{') != -1) {
							parseStatus =10;
							currentRec.syntax =currentRec.syntax.concat(" " + t1);
							continue;
						}
					
						if (st.ttype==TT_EOL || st.ttype==st.TT_EOF) {
							currentRec.syntax =currentRec.syntax.concat(t1);
							if(parseStatusTemp==1 ) {
								//System.out.println("InVar : "+ currentRec.name + " , " + currentRec.syntax );
								if(currentRec.syntax.indexOf('{') != -1){
									parseStatus=10;
									continue;
								}
								// See if it is a table. if so, set recordtype to 1
								if(currentRec.syntax.trim().startsWith("SEQUENCE") == true) {
									currentRec.recordType=1; 
								}
								
								//System.out.println("Var added : " + currentRec.name+ "  SYN : " + currentRec.syntax );
								addToken(currentRec);
								parseStatus=0;
								parseStatusTemp =0;
							}							
							continue;
						}
						//System.out.println("Variable Found : " + currentRec.name+ "  Type : " + currentRec.syntax);						
						currentRec.syntax = currentRec.syntax.concat(" " + t1);
						if(currentRec.syntax.trim().length()>0) parseStatusTemp=1;
						continue;
					}
				case 10: {			// Variable Data in { } 
							// System.out.println(t1);							 
				 		 currentRec.syntax=currentRec.syntax.concat(t1);				
						 if(t1.indexOf((int)'}') != -1) {
							 parseStatus =0;
							 parseStatusTemp =0;
							// See if it is a table. if so, set recordtype to 1
							 if(currentRec.syntax.trim().startsWith("SEQUENCE")==true) {
								 currentRec.recordType=1; 
							 }							
							 addToken(currentRec);
							 //System.out.println(currentRec.name +"  " + currentRec.syntax );
							 continue;
						 }
						 continue;
					}
						 
				case 11: {			// INDEX (For tables)
							 
						if(t1.trim().startsWith("{")==true) continue;
						if(t1.indexOf((int)'}') != -1) {
							parseStatus = 1;  
							continue;
						}
						currentRec.index=currentRec.index.concat(t1);												
						continue;
					}
						 
						 
				case 12: {
 						currentRec.syntax=currentRec.syntax.concat(t1);
						if(t1.indexOf((int)'}') != -1) {
							parseStatus =1;
							parseStatusTemp =0;
							
							// See if it is a table. if so, set recordtype to 1
							if(currentRec.syntax.trim().startsWith("SEQUENCE")==true) {
								 currentRec.recordType=1; 
								 currentRec.tableEntry=1;
							}
						}		
						 //if(t1.indexOf((int)'}') != -1)
						continue;
					}
				// case 13: Not used because unlucky :(
				case 14 : {   // OBJECT-GROUP OBJECTS...
						currentRec.syntax=currentRec.syntax.concat(t1);
						if(t1.indexOf((int)'}') != -1) {
							parseStatus =1;
						}		
						 //if(t1.indexOf((int)'}') != -1)
						continue;
					}						 
				case 100: {
					    if(t1.indexOf(';')!= -1) parseStatus =0;
					}
				case 101: {
						if(t1.indexOf('}')!= -1) parseStatus =0;
					}
				
				}
			}			
		}	
		catch (Exception e)	{
			System.out.println("Error in parsing.. \n" + e.toString());
		}		
		return 0;
	}	
	
	
	// returns the next non blank token
	String getNextToken(StreamTokenizer st)
	{
		String tok="";	
		prevToken=getTokenVal(st);
		
		while(tok.equals("") == true )
		{		
			try	{				
				if(tokenVal.equals("xx")!=true) return(tokenVal);
				if(tokenVal2.equals("")!=true) {
					setTokenVal(tokenVal2);
					tokenVal2="";
					return tokenVal;
				}
				if(st.nextToken()!= StreamTokenizer.TT_EOF) {
					if(st.ttype==TT_EOL) return getTokenVal(st);
					if(st.ttype==StreamTokenizer.TT_WORD  )  {
						tok=st.sval;
						//System.out.println(tok);
						// if { is combined with something, seperate them
						if(tok.startsWith("{")==true) {
							if(tok.trim().length() !=1) {
								setTokenVal("{");
								tokenVal2=new String(tok.substring(1));
								return ("{");						
							}
						}
						if(tok.endsWith("}")==true) {
							if(tok.trim().length() !=1) {
								setTokenVal(tok.replace('}',' '));
								tokenVal2="}";
								return tok.replace('}',' ');						
							}
						}
						
						// Get "Quoted Text" as whole tokens :)
						if(tok.startsWith("\"")==true) {
							//System.out.println("Comment.");
							String strQuote =new String(tok);
							st.nextToken();
							tok=getTokenVal(st);
							while(tok!=null &&  tok.indexOf('"')==-1 ) {
								String temp=getTokenVal(st);
								if(temp.trim().length()>0) strQuote=strQuote.concat(" "+temp);
								if(st.nextToken()==st.TT_EOF) return tok;
								tok=getTokenVal(st);
							}
							strQuote=strQuote.concat(getTokenVal(st));
							//System.out.println (strQuote);
							if(strQuote.trim().length() > 0) tokenVal =strQuote;
						}
							
						if(tok.equals("--")==true) {
							//System.out.print("-- ");
							while(st.ttype != st.TT_EOL )
								st.nextToken();							
							//System.out.println("..." + getTokenVal(st));							
							break;
							//continue;
						}				
						//System.out.println("++ "+ tok+" ++");
						
						if(st.ttype == TT_EOL) return(" "); //st.ttype ;
						else
							continue;
					}
					else if(st.ttype == StreamTokenizer.TT_NUMBER ) {
						tok=String.valueOf(st.nval ); 
						if(tok.trim().length()>0) {
							//System.out.println("No : "+ tok);
							return tok;
						}
						else
							continue;
					}
					else tok="";
				}
				else return "";				
			}
			catch (Exception e) {
				if(e.getMessage().startsWith("Write end dead") != true)
					System.out.println("Error in reading file..." + e.toString());			
				//System.out.println("Errorin getting next token...\n" + e.toString());
				return "";
			}		
		}	
		return tok;	
	}
	
	void setTokenVal(String t) {
		tokenVal=t;
	}
					 
	String getTokenVal(StreamTokenizer st)
	{
		try {		
			if(tokenVal != "xx") {
				String temp=tokenVal.toString();
				tokenVal="xx";
				return temp;
			}
			if(st.ttype == TT_EOL) return String.valueOf('\n');
			if(st.ttype==StreamTokenizer.TT_WORD  )  
				return(st.sval);
			else if(st.ttype == StreamTokenizer.TT_NUMBER )			
				return(String.valueOf ((int)st.nval) ); 
			else return ("");
		}
		catch(Exception e)	{
			System.out.println("Error in retrieving token value..\n" + e.toString());
			
		}
		return("");
	}
					  
	
	void setOutput(DwSnmpMibOutputHandler output) {
		this.output=output;
	}
	
	void outputText(String s) {
		output.println(s);
	}
	void outputError(String s) {
		output.printError(s);
	}
	
	void addToken(DwSnmpMibRecord rec) {
		tokens.newMibParseToken(rec);
	}
/*	
	public static void main(String mainArgs[])
	{
		if(mainArgs.length ==0 ) return;
		DwSnmpMibParser sp=new DwSnmpMibParser(mainArgs[0]);
		sp.parseMibFile();
	}
	*/
}
