package com.dwipal;

import java.io.*;
import java.util.*;

public class DwSettings
{
	public static ResourceBundle resources;
	public static DwIPRecord LAST_IP_RECORD=new DwIPRecord();
	public static String PROPERTY_FILENAME="ITraffic";
		
	String fileName;
	
	DwSettings() {
		loadSettingsFile("itraffic.conf");
	}
	
	DwSettings(String fileName) {
		loadSettingsFile(fileName);
		loadProperties();
	}
	
	private void loadSettingsFile(String fileName) {
		this.fileName=fileName;
	}
	public String getUser() {
		return getSetting("User");
	}
	public String getPass(String userName) {
		return getSetting("Passwd "+ userName );
	}
		
	
	public String getSetting(String settingName) {
		FileReader fin;
		try {
			fin=new FileReader(new File(fileName));
		}catch(Exception e) {
			dispError("Error.. Cannot load settings file " + fileName + " ...\n" + e.toString());
			return null;
		}				

		
 		String settingLine="";
		try {
			BufferedReader in=new BufferedReader(fin);			
			if(in==null) {
				dispError("Settings file not loaded");
				return null;
			}
		//	System.out.println(in); 
		
			while(settingLine!=null) {
				settingLine=in.readLine();
				if(settingLine==null) return null;
				if(settingLine.startsWith("#")) continue;
				if(settingLine.trim().length() <3) continue;
		//		System.out.println(settingLine);
				int Index=settingLine.indexOf("="); 
				if(settingLine.substring(0,Index).trim().equals(settingName)==true) 
					return(settingLine.substring(Index+1));					
			}		
		}	
		catch(Exception e) {
			dispError("Error.. Cannot read the settings file.\n"+e.toString());
		}
		return null;
	}	
	
	private void dispError(String e) {
		System.err.println(e);
	}		
	
	public static void loadProperties() {
		resources=ResourceBundle.getBundle(PROPERTY_FILENAME,Locale.getDefault());
		String parVal;
		
		parVal=getResourceParameter("lastipaddress");		
		LAST_IP_RECORD.ipAddress=(parVal!=null)?parVal:"127.0.0.1";
		
		parVal=getResourceParameter("lastport");
		int parInt;
		try {
			parInt=Integer.parseInt(getResourceParameter("lastport"));				   
		}catch(Exception e) {
			parInt=161;
		}
		LAST_IP_RECORD.port =parInt;
		
		parVal=getResourceParameter("lastgetcommunity");		
		LAST_IP_RECORD.getCommunity=(parVal!=null)?parVal:"public";
		
		parVal=getResourceParameter("lastsetcommunity");				
		LAST_IP_RECORD.setCommunity=(parVal!=null)?parVal:"public";		
		
	}
	
	
	private static String getResourceParameter(String parmName) {
		String param = null;
		try{
			
			param = resources.getString(parmName) ;
		}catch(Exception e){
			System.out.println("Exception in getting parameter: " + parmName + " " + e);
			param = null;
		}
		return param.trim();
	}
	
}


class DwIPRecord
{
	public String ipAddress="127.0.0.1";
	public int port=161;
	public String getCommunity="public";
	public String setCommunity="public";
}
