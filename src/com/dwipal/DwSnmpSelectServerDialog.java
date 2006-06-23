package com.dwipal;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.GridLayout;

public class DwSnmpSelectServerDialog  {

	DwSnmpSelectServerDialogImpl db;

	public static void main(String s[]) {
		DwSnmpSelectServerDialog sd=new DwSnmpSelectServerDialog();
		System.out.println(sd.show());
	}

	public DwSnmpSelectServerDialog() {
	}


	public String[] show() {
		db=new DwSnmpSelectServerDialogImpl();
                db.setVisible(true);
		return db.getUserData();
	}

	public String[] show(String ip,int port,String get,String set) {
		db=new DwSnmpSelectServerDialogImpl(ip,port,get,set);
                db.setVisible(true);
		//System.out.println("Showing for " + ip);
		return db.getUserData();
	}

        public String[] getSelectedConfig() {
          db=new DwSnmpSelectServerDialogImpl();
          return db.getSelectedConfig();
        }

	/*
	public IPRecord getIPRec() {
		String ip[]=db.getUserData();
		if(ip==null) return null;
		IPRecord ipRec=new IPRecord(ip[0],ip[1],ip[2],ip[3]);
		return ipRec;
	}
	*/

}


class DwSnmpSelectServerDialogImpl extends JDialog
implements ActionListener
{
	JButton ipButtonOK;
	JButton ipButtonCl;
	JFrame ipFrame;
	JTextField  ipText1;
	JTextField ipText2;
	JTextField ipText3;
	JTextField ipText4;
	String returnString[];
	boolean flag;

        DwSettings m_settings;

	public DwSnmpSelectServerDialogImpl() {
          m_settings=new DwSettings();
          if(m_settings.iprecord != null) {
            DwSnmpSelectServerDialogImplFunc(m_settings.iprecord.ipAddress, m_settings.iprecord.port, m_settings.iprecord.getCommunity, m_settings.iprecord.setCommunity);
          } else {
            System.out.println("Loading default settings.");
            m_settings.iprecord=new DwIPRecord();
            m_settings.iprecord.ipAddress="127.0.0.1";
            m_settings.iprecord.port=161;
            m_settings.iprecord.getCommunity="public";
            m_settings.iprecord.setCommunity="public";

            DwSnmpSelectServerDialogImplFunc("127.0.0.1", 161, "public", "private");
          }
	}

	public DwSnmpSelectServerDialogImpl(String ip,int port,String get,String set) {
		DwSnmpSelectServerDialogImplFunc(ip,port,get,set);
	}

	public void DwSnmpSelectServerDialogImplFunc(String ip,int port,String get,String set) {

		this.setModal(true);
		returnString=null;
		flag=false;
		try{

			JPanel ipPane=new JPanel(new GridLayout(6,2));
			JLabel ipLabel1=new JLabel("IP Address     ");
			JLabel ipLabel2=new JLabel("Port           ");
			JLabel ipLabel3=new JLabel("Get Community  ");
			JLabel ipLabel4=new JLabel("Set Community  ");
			JLabel ipLabel5=new JLabel(" ");
			JLabel ipLabel6=new JLabel(" ");

			ipText1=new JTextField(ip);
			ipText2=new JTextField(String.valueOf(port));
			ipText3=new JTextField(get);
			ipText4=new JTextField(set);

			ipPane.add(ipLabel1);
			ipPane.add(ipText1);
			ipPane.add(ipLabel2);
			ipPane.add(ipText2);
			ipPane.add(ipLabel3);
			ipPane.add(ipText3);
			ipPane.add(ipLabel4);
			ipPane.add(ipText4);
			ipPane.add(ipLabel5);
			ipPane.add(ipLabel6);

			ipButtonOK=new JButton("OK");
			ipButtonCl=new JButton("Cancel");
			ipPane.add(ipButtonOK);
			ipPane.add(ipButtonCl);

			ipButtonOK.addActionListener(this);
			ipButtonCl.addActionListener(this);

			this.setLocation(200,200);
			this.setTitle("Please select a SNMP Server..");
			this.setContentPane(ipPane);
			this.setSize(250,170);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getUserData() {
		if(returnString==null) return null;
		return returnString;
	}

        public String[] getSelectedConfig() {
          DwIPRecord ipr=m_settings.iprecord;
          return new String[] { ipr.ipAddress, new Integer(ipr.port).toString(), ipr.getCommunity, ipr.setCommunity };
        }

	public void actionPerformed(ActionEvent evt)
	{
		Object source=evt.getSource();
		if(source==ipButtonOK) {
                  DwIPRecord ipr=m_settings.iprecord;
                  ipr.ipAddress=ipText1.getText();
                  ipr.port=Integer.parseInt(ipText2.getText());
                  ipr.getCommunity=ipText3.getText();
                  ipr.setCommunity=ipText4.getText();

                  try {
                    m_settings.saveProperties(ipr);
                  } catch(Exception e) {
                    System.out.println("Hmmm... cant save preferences." + e);
                  }
                  returnString=getSelectedConfig();
		}
		else returnString=null;
		this.setVisible(false);
	}
}
