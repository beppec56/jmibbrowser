package com.dwipal;

import java.io.*;
import java.util.*;

public class DwSettings
{
	public static PropertyResourceBundle resources;
	DwIPRecord iprecord=null;

	DwSettings() {
          try {
            iprecord=new DwIPRecord();
            loadProperties (iprecord);
          } catch (Exception e) {
            System.out.println("Cannot load user settings file.");
            System.out.println(e);
          }
	}
	public void loadProperties(DwIPRecord iprecord) throws FileNotFoundException, IOException {
		FileInputStream file=new FileInputStream("jmibbrowser.conf");
		resources=new PropertyResourceBundle(file);
		String parVal;

		parVal=getResourceParameter("lastipaddress");
		iprecord.ipAddress=(parVal!=null)?parVal:"127.0.0.1";

		parVal=getResourceParameter("lastport");
		int parInt;
		try {
			parInt=Integer.parseInt(getResourceParameter("lastport"));
		}catch(Exception e) {
			parInt=161;
		}
		iprecord.port =parInt;

		parVal=getResourceParameter("lastgetcommunity");
		iprecord.getCommunity=(parVal!=null)?parVal:"public";

		parVal=getResourceParameter("lastsetcommunity");
		iprecord.setCommunity=(parVal!=null)?parVal:"public";

		file.close();
	}
	public void saveProperties(DwIPRecord iprecord) throws FileNotFoundException, IOException {
          FileOutputStream file=new FileOutputStream("jmibbrowser.conf",false);

          file.write(("lastipaddress=" + iprecord.ipAddress + "\n").getBytes());
          file.write(("lastport=" + iprecord.port + "\n").getBytes());
          file.write(("lastgetcommunity=" + iprecord.getCommunity + "\n").getBytes());
          file.write(("lastsetcommunity=" + iprecord.setCommunity + "\n").getBytes());
          file.close();

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

