package com.dwipal;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;


public class DwSnmpMibTreeBuilder implements DwMibParserInterface,Runnable
{
	DwSnmpMibOutputHandler output=null;
	private DefaultMutableTreeNode rootNode;
	private DefaultMutableTreeNode treeRootNode;
	private DefaultMutableTreeNode rootOrphan;
	private DefaultMutableTreeNode rootVariable;
	private DefaultMutableTreeNode rootVariableTable;

	JTree tree;

	private Vector fileVect;
	private Vector orphanNodes;

	private String errorMsg="";

	public DwSnmpOidSupport oidSupport=new DwSnmpOidSupport();
	DwSnmpMibTreeHash treeHash;
	DwSnmpMibTreeHash variableHash;
	DwSnmpMibTreeHash orphanHash;

	public DwSnmpMibTreeBuilder() {
		DwSnmpMibRecord treeRootRec=new DwSnmpMibRecord();
		treeRootRec.name="MIB Browser";
		treeRootRec.parent="MIB Browser";
		treeRootRec.number=0;
		treeRootNode=new DefaultMutableTreeNode(treeRootRec);

		DwSnmpMibRecord rootRec=new DwSnmpMibRecord();
		rootRec.name="root";
		rootRec.parent="MIB Browser";
		rootRec.number=1;
		rootNode=new DefaultMutableTreeNode(rootRec);

		DwSnmpMibRecord rootOrphanRec=new DwSnmpMibRecord();
		rootOrphanRec.name="Orphans";
		rootOrphanRec.parent="MIB Browser";
		rootOrphanRec.description = "This subtree contains MIB Records whose parent cannot be traced";
		rootOrphanRec.number=10;
		rootOrphan =new DefaultMutableTreeNode(rootOrphanRec);


		DwSnmpMibRecord rootVariableRec=new DwSnmpMibRecord();
		rootVariableRec.name="Variables/Textual Conventions";
		rootVariableRec.parent="MIB Browser";
		rootVariableRec.description = "This subtree contains all the variables which map to the standard variables.";
		rootVariableRec.number=11;
		rootVariable =new DefaultMutableTreeNode(rootVariableRec);

		DwSnmpMibRecord rootVariableTableRec=new DwSnmpMibRecord();
		rootVariableTableRec.name="Table Entries";
		rootVariableTableRec.parent="Variables/Textual Conventions";
		rootVariableTableRec.description = "This branch contains a list of sequences for all the tables ";
		rootVariableTableRec.number=12;
		rootVariableTable  =new DefaultMutableTreeNode(rootVariableTableRec);

		treeHash=new DwSnmpMibTreeHash();
		treeHash.put(rootRec.name,rootNode);

		variableHash=new DwSnmpMibTreeHash();
		orphanHash =new DwSnmpMibTreeHash();

		orphanNodes=new Vector();
		fileVect=new Vector();
		clearError();
	}

	public DefaultMutableTreeNode getRootNode() {
		return rootNode;
	}

	public boolean addFile(String fName) {
		if(fName==null) return false;
		File mibFile=new File(fName);
		if(mibFile.exists()!=true) return false;
		fileVect.add(fName);
		return true;
	}

	public boolean addDirectory(String dirName) {
		System.out.println("Adding directory : " + dirName);
		File dir=new File(dirName);
		if(dir.isDirectory()!=true) return false;
		File files[]=dir.listFiles();
		if(files==null) return false;
		for(int i=0;i<files.length;i++) {
			fileVect.add(files[i].getPath());
		}
		return true;
	}


	public String[] getFiles() {

		try {
			Enumeration enu=fileVect.elements();
			String returnStr[]=new String[fileVect.size()];

			int i=0;
			while(enu.hasMoreElements()) {
				returnStr[i++] = (String)enu.nextElement();
			}
			clearError();
			return returnStr;
		}
		catch(Exception e) {
			setError("Error in getting filenames..\n" + e.toString());
			return null;
		}
	}

	private void clearError() {
		errorMsg = "";
	}

	private void setError(String err) {
		errorMsg=err;
	}

	public JTree buildTree() {
		// Check if files have been added to list
		if(fileVect.size()==0) {
			setError("Error : Please add files first");
			return null;
		}

		oidSupport=new DwSnmpOidSupport();
		Thread treeThread=new Thread(this);
		treeThread.setPriority(Thread.MAX_PRIORITY-1);
		treeThread.start();

		treeRootNode.add(rootNode);
		treeRootNode.add(rootOrphan);
		rootVariable.add(rootVariableTable);
		treeRootNode.add(rootVariable);
		tree=new JTree(treeRootNode);
		tree.putClientProperty("JTree.lineStyle", "Angled");
		tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
		return(tree);
	}

	public void run() {
		// Get filenames and add nodes to rootnode
		Enumeration enu=fileVect.elements();
		String fileName="";
		JTree newTree=null;
		while(enu.hasMoreElements()) {
			fileName=(String) enu.nextElement();
			loadFile(fileName);
		}
		updateOrphans();
		output.println("*****COMPLETED******");
	}

	private void loadFile(String fileName) {
		output.print("Adding file " + fileName);
		if(parseFile(fileName)<0) outputError(".. Error");
		else output.print("..Done\n");
	}

	public boolean loadNewFile(String fName) {
		if(fName==null) return false;
		File mibFile=new File(fName);
		if(mibFile.exists()!=true) return false;
		if(fileVect.indexOf(fName)==-1) {
			tree.collapsePath(tree.getSelectionPath());
			fileVect.add(fName);
			loadFile(fName);
			updateOrphans();
			return true;
		}
		return false;
	}

	private void  updateOrphans() {
		// Check if orphan's parents have arrived. if yes, remove them from orphan list
		//outputText("Scanning orphans..");
		outputText("Updating orphans.");
		DwSnmpMibRecord orphanRec=null;
		Enumeration orphanEnu;
		boolean contFlag=true;

		while(contFlag==true) {
			contFlag=false;
			orphanEnu=orphanNodes.elements();
			while(orphanEnu.hasMoreElements()) {
				DefaultMutableTreeNode orphanNode = (DefaultMutableTreeNode)orphanEnu.nextElement();

				if (addNode(orphanNode)==true) {
					contFlag=true;
					orphanNodes.remove(orphanNode);
					continue;
				}
				// If orphan rec. type=1, then it is a table variable.
				/*
				orphanRec = (DwSnmpMibRecord) orphanNode.getUserObject();
				if(orphanRec.recordType==orphanRec.recTable) {
					rootVariableTable.add(orphanNode);
					orphanNodes.remove(orphanNode);
				}
				*/
			}
			output.print(".");
		}
		output.print("Done");
		output.print("\nBuilding OID Name resolution table...");
		oidSupport.buildOidToNameResolutionTable(rootNode);

		//Add remaining orphans to treeroot.orphans
		//System.out.print("Updating orphan table....");
		orphanEnu=orphanNodes.elements();
		while(orphanEnu.hasMoreElements()) {
			DefaultMutableTreeNode orphanNode = (DefaultMutableTreeNode)orphanEnu.nextElement();
			orphanRec=(DwSnmpMibRecord) orphanNode.getUserObject();
			if(orphanRec.recordType==orphanRec.recVariable) continue;
			if(orphanRec.recordType==orphanRec.recTable) {
				rootVariable.add(orphanNode);
				continue;
			}
			if(treeHash.containsKey(orphanRec.name)!=true) rootOrphan.add(orphanNode);
		}

		//System.out.print("Completed!\n");
		// Add variables to varroot
		outputText("Updating variables table..");
		Enumeration enuVar=variableHash.elements();
		DwSnmpMibRecord varRec;
		while(enuVar.hasMoreElements ()) {
			varRec=(DwSnmpMibRecord) enuVar.nextElement();
			rootVariable.add(new DefaultMutableTreeNode(varRec));
		}

		if(tree!=null && tree.getModel()!=null) {
			((DefaultTreeModel)tree.getModel()).reload();
			tree.revalidate();
			tree.repaint();
		}
		outputText("Done");
	}

	private int parseFile(String fName) {

		DwSnmpMibParser fParser=new DwSnmpMibParser(fName,this);
		if(output!=null) fParser.setOutput(output);
		return fParser.parseMibFile();
	}

	private boolean addRecord(DwSnmpMibRecord childRec) {
		int parseStatus=0;
		if(childRec==null) return false;
		DefaultMutableTreeNode newNode=new DefaultMutableTreeNode(childRec);
		if(addNode(newNode)==false) {
//			if(orphanHash.containsKey(childRec.name)==false) {
				orphanNodes.add(newNode);
				orphanHash.put(childRec.name,newNode);
//			}
			return false;
		}
		return true;

		/*
		// See if parent exists. if no parent, add it to orphans
		if (treeHash.containsKey(childRec.parent) == false) {
		//	outputText("Orphan : " + childRec.name + "  Parent : " + childRec.parent );
			DefaultMutableTreeNode orphanNode=new DefaultMutableTreeNode(childRec,true);
			treeHash.put(childRec.name,orphanNode);
			orphanNodes.add(orphanNode);
			return false;
		}
		// Get the parent node (current node will be added to it)
		DefaultMutableTreeNode parentNode =(DefaultMutableTreeNode) treeHash.get(childRec.parent);

		// Check if parent node contains a child of same name as new node
		// If  child exists, return true
		if(isChildPresent(childRec)!=null) return true;

		// Check if parent is a Table, and set the node tableEntry accordingly
		DwSnmpMibRecord parentRec=(DwSnmpMibRecord)parentNode.getUserObject();
		if(parentRec.recordType > 0) childRec.recordType =parentRec.recordType+1;
		//outputText("Added Child : " + childRec.name  + "  Parent : " + childRec.parent );
		DefaultMutableTreeNode childNode=new DefaultMutableTreeNode (childRec,true);
		// Add  node to  its parent
		parentNode.add(childNode);
		childNode.setParent(parentNode);
		treeHash.put(childRec.name,childNode);
		return true;
		*/
	}


	private boolean addNode(DefaultMutableTreeNode newNode) {
		DwSnmpMibRecord newRec=(DwSnmpMibRecord) newNode.getUserObject();

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treeHash.get(newRec.parent);
		//if(parentNode==null) { // See if parent is an orphan
		//	parentNode = (DefaultMutableTreeNode)orphanHash.get(newRec.parent);
		//}
		if(parentNode==null) return false;

		// Check if parent is a Table, and set the node tableEntry accordingly
		DwSnmpMibRecord parentRec=(DwSnmpMibRecord)parentNode.getUserObject();
		if(parentRec.recordType > 0) newRec.recordType =parentRec.recordType+1;

		DefaultMutableTreeNode dupNode=isChildPresent(newNode);
		if(dupNode == null){		// Add  node to  its parent
			try {
				parentNode.add(newNode);
				newNode.setParent(parentNode);
				// See if parent is not an orphan
				treeHash.put(newRec.name,newNode);
				return true;
			} catch(Exception e) {
				System.out.println("Err in Child : " + newRec.name + "Parent : " + newRec.parent);
				return false;
			}
		}
		else {      // Node already present. add all its children to the existing node
			Enumeration dupChildren=newNode.children();
			while(dupChildren.hasMoreElements()) {
				DefaultMutableTreeNode dupChildNode=(DefaultMutableTreeNode)dupChildren.nextElement();
				if(isChildPresent(dupChildNode)==null) dupNode.add(dupChildNode);
			}
			return true;
		}
	}

	DefaultMutableTreeNode isChildPresent(DefaultMutableTreeNode childNode) {
		DwSnmpMibRecord childRec=(DwSnmpMibRecord)childNode.getUserObject();
		return(isChildPresent(childRec));
	}

	DefaultMutableTreeNode isChildPresent(DwSnmpMibRecord rec) {
		DefaultMutableTreeNode parentNode =(DefaultMutableTreeNode) treeHash.get(rec.parent);
		if(parentNode==null) parentNode =(DefaultMutableTreeNode) orphanHash.get(rec.parent);
		if(parentNode==null) return null;
		Enumeration enuChildren=parentNode.children();
		if (enuChildren!=null) {
			DefaultMutableTreeNode childNode;
			DwSnmpMibRecord childRec;
			while(enuChildren.hasMoreElements()) {
				childNode=(DefaultMutableTreeNode) enuChildren.nextElement();
				childRec=(DwSnmpMibRecord )childNode.getUserObject();
				if(childRec.name.equals(rec.name)== true) {
					//outputText("ChildCheck, Rec. Present.. Parent : " + rec.parent + "  Name : " + rec.name);
					return childNode;
				}
			}
		}
		return null; // Child does not exist
	}

	public void setOutput(DwSnmpMibOutputHandler output) {
		this.output=output;
	}

	void outputText(String s) {
		try {
		output.println(s);
		} catch(Exception e) {
			System.out.println(s);
		}
	}
	void outputError(String s) {
		try {
		output.printError(s);
		} catch(Exception e) {
			System.out.println(s);
		}
	}

	public void newMibParseToken(DwSnmpMibRecord rec) {
		addRecord(rec);

	}


	public void parseMibError(String s) {
		outputError(s);
	}

}// End of class.






