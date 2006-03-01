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
		return db.getUserData();
	}

	public String[] show(String ip,int port,String get,String set) {
		db=new DwSnmpSelectServerDialogImpl(ip,port,get,set);
		//System.out.println("Showing for " + ip);
		return db.getUserData();
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

	public DwSnmpSelectServerDialogImpl() {
		DwSnmpSelectServerDialogImplFunc("192.168.2.9",161,"public","private");

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
			this.setVisible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getUserData() {
		if(returnString==null) return null;
		return returnString;
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source=evt.getSource();
		if(source==ipButtonOK) {
			returnString=new String[] { ipText1.getText(),ipText2.getText(),ipText3.getText(),ipText4.getText() };
		}
		else returnString=null;
		this.setVisible(false);
	}
}
