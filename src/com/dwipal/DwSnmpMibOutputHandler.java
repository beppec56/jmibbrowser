package com.dwipal;


import javax.swing.*;
import java.io.*;
import java.util.*;

public class DwSnmpMibOutputHandler
{

	JTextArea outputText;
	JTextArea outputError;
	boolean doLog=false;
	boolean autoScroll=true;
	static BufferedWriter outfile;

	DwSnmpMibOutputHandler() {
	}

	public void setAutoScroll(boolean as) {
		autoScroll=as;
	}
	public void setOutput(JTextArea o) {
		outputText=o;

	}

	public void setOutputError (JTextArea e) {
		outputError=e;
	}


        void showErrorMessage(String s) {
                JOptionPane.showMessageDialog(outputText,s,"Mib Browser",JOptionPane.OK_OPTION);
        }


	public void setLogging(boolean log) {

		try {
			if(log==true) {
				String strFileName=getLogFileName();
				outfile=new BufferedWriter(new FileWriter(strFileName,true));
				outfile.write("\n**********************************************************\n");
				outfile.write("MIB Browser Started at : " + new Date());
				outfile.write("\n**********************************************************\n");
				System.out.println("Output log file: "+ strFileName);
				this.doLog=true;

				java.util.Timer  tmr=new java.util.Timer(true);
				class SnmpTimerTask extends java.util.TimerTask {
					public void run() {
						try	{
							outfile.flush();
						} catch (Exception e) {
							System.out.println("Error in writing to log file: " + e);
						}
					}
				};
				long lFlushTime=getFlushTime();
				System.out.println("Log will be refreshed every " + lFlushTime/1000 + " seconds.");
				tmr.schedule(new SnmpTimerTask(),lFlushTime,lFlushTime);


				Thread thrdFlush=new Thread(new Runnable() {
					public void run() {
						try	{
							System.out.println("Have a nice day !!");
							outfile.write("\n**********************************************************\n");
							outfile.write("MIB Browser Stopped at : " + new Date());
							outfile.write("\n**********************************************************\n");
							outfile.flush();
							outfile.close();
						} catch (Exception e) {
							System.out.println("Error while writing to log file: "+ e);
						}
					}
				});
				Runtime.getRuntime().addShutdownHook(thrdFlush);

			} else outfile.close();
		}catch(Exception e) {
			System.out.println("Error : Cannot log" + e.toString());
		//	doLog=false;
			return;
		}
		doLog=true;
	}

	private String getLogFileName() {
		String strFileName=System.getProperty("logfilename");
		if(strFileName==null) {
			strFileName="mibbrowser.log";
		}
		return strFileName;
	}

	private long getFlushTime() {
		long lTime=0;
		String strTime=System.getProperty("logrefreshtime");
		if(strTime!=null) {
			try	{
				lTime=Long.parseLong(strTime);
				lTime=lTime*1000;
			} catch (Exception e) {
				System.out.println("Invalid value for log refresh time. default will be used.");
			}
		}

		if(lTime<1000) { // minimum must be 1 second.
			lTime=60*1000; // default is 1 minute.
		}
		return lTime;
	}

	public void println(String s) {
		if(outputText !=null )	{
			outputText.append("\n"+s);
			if(autoScroll==true) outputText.setCaretPosition(outputText.getDocument().getLength() - 1);
		}
		//else System.out.println(s);
		try {//if(doLog==true)
			{
				 outfile.write(s+"\n");
			 }
		}catch(Exception e) {System.out.println(e.toString());}
	}

	public void print(String s) {
		if(outputText !=null) {
			outputText.append (s);
			if(autoScroll==true) outputText.setCaretPosition(outputText.getDocument().getLength() - 1);
		}
		else System.out.println(s);
		try {//if(doLog==true)
			outfile.write(s);
		}catch(Exception e) {System.out.println(e.toString());}

	}

	public void printError(String e) {
		if(outputError!=null) outputError.append("\n"+e);
		else System.err.println(e);
		try {//if(doLog==true)
			outfile.write("\n"+e+"\n");
		}catch(Exception ex) {System.out.println(e.toString());}

	}
}
