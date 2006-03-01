package com.dwipal;

import javax.swing.*;

public class MibTreeTester
{
	public static void main(String[] args)
	{
		DwSnmpMibTreeBuilder tree =  new DwSnmpMibTreeBuilder();	
		tree.addFile("root_mib.txt");
		tree.addFile("mib_core.txt");
		tree.addFile("mib_demo.txt");

		JTree myTree=tree.buildTree();
		
		JFrame myFrame=new javax.swing.JFrame("Mib Tree Tester");
		JScrollPane sp=new JScrollPane(myTree);
		myFrame.getContentPane().add(sp);
		myFrame.setVisible(true);
	}
}
