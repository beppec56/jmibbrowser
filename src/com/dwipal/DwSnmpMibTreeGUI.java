package com.dwipal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JTree;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;


public class DwSnmpMibTreeGUI
    implements ActionListener, MouseListener, TreeSelectionListener {

  DwSnmpMibTreeBuilder treeSupport;
  //DwSnmpOidSupport  oidSupport;
  DwSnmpMibBrowserFunctions snmp;
  DwSnmpMibOutputHandler output = new DwSnmpMibOutputHandler();
  DwSnmpSelectServerDialog m_ipSrv=new DwSnmpSelectServerDialog();

  JTree myTree;
  JScrollPane treeScrollPane;
  JPanel treePane;

  JButton btnLoadMib;

  JPanel paneMain = new JPanel(new BorderLayout());

  // Other GUI stuff
  JTextField selectedTreeOid = new JTextField("Selected oid..");
  JTextArea resultText;
  JButton btnGet = new JButton("Get");
  JButton btnSet = new JButton("Set");
  JButton btnStop = new JButton("Stop");
  JCheckBox chkScroll = new JCheckBox("Scroll Display");
  JButton btnOidDetails = new JButton("Details");
  JButton btnClear = new JButton("Clear");

  // Tooltips and Toolbars
  JToolBar mainToolbar;
  JButton toolbarBtnIP;
  JButton toolbarBtnAbout;

  JToolBar statusToolbar;
  // Initial Vars

  PipedInputStream pin;
  PipedOutputStream pout;
  PrintStream out;
  BufferedReader in;

  public DwSnmpMibTreeGUI() {
    output.setLogging(true);
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public DwSnmpOidSupport getOidSupport() {
    return treeSupport.oidSupport;
  }

  public JPanel getMainPane() {
    return paneMain;
  }

  public JTree getTree() {
    return myTree;
  }

  public Component createComponents() {
    // First initialise the output text area and Status areas
    btnOidDetails.setToolTipText("Get details of selected element");
    btnClear.setToolTipText("Clear the contents of result window");
    btnOidDetails.addActionListener(this);
    btnClear.addActionListener(this);

    resultText = new JTextArea();
    JScrollPane resultPane = new JScrollPane(resultText);

    // Set everyone's output to resulttext
    output = new DwSnmpMibOutputHandler();
    output.setOutput(resultText);
    output.setOutputError(resultText);
    snmp = new DwSnmpMibBrowserFunctions();
    snmp.setOutput(output);
    setIPConfig();

    selectedTreeOid = new JTextField("Your Selection");

    // Create a tooltip for jlabel, and also add a message handler to it.
    selectedTreeOid.setToolTipText(
        "Click here for more information on this variable");
    selectedTreeOid.setText("Selected Element's OID");
    selectedTreeOid.addMouseListener(this);

    // Create the TREE and Tree pane.

    outputText("Building tree..");
    treeSupport = new DwSnmpMibTreeBuilder();
    treeSupport.setOutput(output);
    String projectdir = System.getProperty("ProjectDir");
    if (projectdir == null) {
      projectdir = ".";
    }
    if (treeSupport.addDirectory(projectdir + "/mibs/") == false) {
      outputError("Directory " + projectdir +
                  "/mibs/ not found, or it is an empty directory!");
    }

    //treeSupport.addFile("mib_core.txt");
    //	treeSupport.addFile("mib_II.txt");

    myTree = treeSupport.buildTree();
    if (myTree == null || treeSupport.oidSupport == null) {
      outputError("Error in loading MIB tree, quitting");
      return null;
    }
    snmp.setOidSupport(treeSupport.oidSupport);
    myTree.addTreeSelectionListener(this);
    treeScrollPane = new JScrollPane(myTree);

    btnLoadMib = new JButton("Load MIB");
    treePane = new JPanel(new BorderLayout());
    treePane.add("Center", treeScrollPane);
    treePane.add("South", btnLoadMib);

    //buildOidToNameResolutionTable(rootNode,oidResolveHash);

    statusToolbar = new JToolBar();
    statusToolbar.add(btnClear);
    statusToolbar.add(btnOidDetails);
    statusToolbar.addSeparator();
    statusToolbar.add(selectedTreeOid);

    // Create the Right pane containing buttons,status and textbox
    btnGet.setToolTipText("Get the values for selected element");
    btnSet.setToolTipText("Set the value of selected element");
    btnStop.setToolTipText("Stop the current action");

    JPanel paneBtn = new JPanel(new FlowLayout());
    btnGet.addActionListener(this);
    btnSet.addActionListener(this);
    btnStop.addActionListener(this);
    chkScroll.setSelected(true);
    chkScroll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        output.setAutoScroll(chkScroll.isSelected());
      }
    });

    paneBtn.add(btnGet);
    paneBtn.add(btnSet);
    paneBtn.add(btnStop);
    paneBtn.add(chkScroll);

    JPanel paneStatus = new JPanel(new BorderLayout());
    paneStatus.add("South", paneBtn);
    paneStatus.add("North", statusToolbar);
    paneStatus.add("Center", resultPane);

    // Create the Main Toolbar
    mainToolbar = new JToolBar();
    toolbarBtnIP = new JButton("Select Server");
    toolbarBtnAbout = new JButton("About");

    toolbarBtnIP.addActionListener(this);
    toolbarBtnAbout.addActionListener(this);
    btnLoadMib.addActionListener(this);

    mainToolbar.add(toolbarBtnIP);
    mainToolbar.add(toolbarBtnAbout);

    // Create the Content pane and add other panes to it :)
    JSplitPane paneContent = new JSplitPane();
    paneContent.setLeftComponent(treePane);
    paneContent.setRightComponent(paneStatus);
    paneContent.setDividerLocation(250);

    // Finally create the Main Pane with the toolbar and content pane in it
    paneMain.add("Center", paneContent);
    paneMain.add("North", mainToolbar);

    m_ipSrv = new DwSnmpSelectServerDialog();


    return paneMain;
  }

  /** Returns the tree pane
   */
  public JPanel getTreePane() {
    return treePane;
  }

  public void setTreePane(JPanel treePanel) {

  }

  /** TREE SELECTION LISTENER.
   * LISTENS TO THE EVENTS IN THE TREE "myTree"
   */

  public void valueChanged(TreeSelectionEvent e) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) myTree.
        getLastSelectedPathComponent();
    if (node == null) {
      selectedTreeOid.setText(" ");
      return;
    }
    selectedTreeOid.setText(treeSupport.oidSupport.getNodeOid(node));
  }

  /** END OF TREE SELECTION EVENT LISTENER
   */
  public void mouseClicked(MouseEvent evt) {
    Object source = evt.getSource();
    if (source == selectedTreeOid) {
      DwSnmpMibRecord node = getSelectedTreeNode();
      if(node != null)
        outputText(node.getCompleteString());
    }
  }
  private DwSnmpMibRecord getSelectedTreeNode() {
    DwSnmpMibRecord ret=null;
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) myTree.getLastSelectedPathComponent();
    if(node!=null) {
      ret=(DwSnmpMibRecord) node.getUserObject();
    }
    return ret;
  }

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  public void actionPerformed(ActionEvent evt) {
    Object source = evt.getSource();

    try {
      if (source == btnGet) {
        sendGetRequest(selectedTreeOid.getText());
        return;
      }
      else if (source == btnSet) {
        DwSnmpMibRecord node=getSelectedTreeNode();
        if(!node.isWritable()) {
          JOptionPane.showMessageDialog(getMainPane(), "The selected node is not writable.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        String oid = selectedTreeOid.getText();
        String oidText=getOidSupport().resolveOidName(oid);
        String setValue = "";
        String message="Enter new value for " + oidText;
        if(node.getSyntaxID()!=DwSnmpMibRecord.VALUE_TYPE_NONE) {
          message=message+"\nValue Type: " + node.syntax.trim() + " [" + node.getSyntaxIDString() + "]";
        } else {
          message=message+"\nValue type " + node.syntax.trim() + " unknown, will use STRING.";
        }
        setValue = JOptionPane.showInputDialog(message);
        if(setValue!=null && node.checkValidValue(setValue)) {
          outputText("Request : Set  " + oid + "  Value : " + setValue);
          if (snmp.processSetRequest(node, oid, setValue) == null) {
            outputError("Error in processing variable data/set request");
            return;
          }
          //DwSnmpRequestSet(oid,setValue);
          outputText("Set command executed...");
          outputText("Getting new value of " + oid + " ...");
          sendGetRequest(oid);
        }
        return;
      }
      else if (source == btnStop) {
        snmp.destroySession();
        outputText(" ******** Cancelled *********\n");
        return;
      }
      else if (source == btnClear) {
        resultText.setText("");
        return;
      }
      else if (source == btnOidDetails) {
        mouseClicked(new MouseEvent(selectedTreeOid, 0, 0, 0, 0, 0, 0, true));
        return;
      }
      else if (source == toolbarBtnAbout) {
        JOptionPane.showMessageDialog(paneMain,
                                      "JMibBrowser version 1.11 Copyright (C) 2005 Dwipal Desai\n\n" +
                                      "This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are\n" +
                                      "welcome to redistribute it under certain conditions. See License.txt for details.\n\n" +
                                      "This software uses snmp4j from http://www.snmp4j.org.\n\n" +
                                      "Please email your suggestions to mibbrowser@dwipal.com.\n",
                                      "About JMibBrowser",
                                      JOptionPane.INFORMATION_MESSAGE);

        return;
      }
      else if (source == toolbarBtnIP) {
        try {
          String newIP = new String(" ");
          if (snmp == null) {
            snmp = new DwSnmpMibBrowserFunctions();
            snmp.setOidSupport(treeSupport.oidSupport);
          }
          getNewIPInfo();
        }
        catch (Exception e) {
          System.err.println("Error in changing IP..\n" + e.toString());
        }
        return;
      }
      else if (source == btnLoadMib) {
        loadNewMib();
        return;
      }
    }
    catch (Exception e) {
      outputError("\nError in processing user request : \n" + e.toString());
    }
  }

  void getNewIPInfo() {
    String ipInfo[] = m_ipSrv.show();

    if (ipInfo == null) {
      return;
    }
    setIPConfig();

  }
  void setIPConfig() {
    String ipInfo[] = m_ipSrv.getSelectedConfig();
    snmp.setIP(ipInfo[0]);
    snmp.setPort(Integer.parseInt(ipInfo[1]));
    snmp.setCommunity(ipInfo[2], ipInfo[3]);

  }

  public void loadNewMib() {

    try {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setVisible(true);
      fileChooser.setDialogTitle("Select the MIB File to Load");
      fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
      fileChooser.setCurrentDirectory(new File("."));
      fileChooser.setMultiSelectionEnabled(true);

      String strFileName = "";
      int returnVal = fileChooser.showOpenDialog(null);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File[] files = fileChooser.getSelectedFiles();
        if (files != null && files.length > 0) {
          for (int i = 0; i < files.length; i++) {
            try {
              loadSingleFile(files[i]);
            }
            catch (Exception e) {
              outputError("Error in loading file: " + files[i].getAbsolutePath());
            }
          }
        }
      }
      else {
        return;
      }

//			treeSupport.addFile(strFileName);
    }
    catch (Exception e) {
      System.out.println("Error in getting new MIB Filenames.\n" + e.toString());
    }

    /*		try {
       myTree.removeTreeSelectionListener(this);
       myTree.removeAll();
       treeScrollPane.remove(myTree);
       System.out.println("Building tree..");
       String fileNames[]=treeSupport.getFiles();
       treeSupport=new DwSnmpMibTreeBuilder();
       treeSupport.setOutput(output);
       try {
        for(int i=0;i<fileNames.length;i++) {
         treeSupport.addFile(fileNames[i]);
        }
       } catch(Exception e) {
     System.out.println("Error in loadin new MIB Filenames.\n" + e.toString());
       }

       try{
       myTree=treeSupport.buildTree();
       } catch(Exception e) {
       System.out.println("Error in building new MIB tree.\n" + e.toString());
       }

       myTree.addTreeSelectionListener(this);
       treeScrollPane.setEnabled(true);
       //treeScrollPane.add(myTree);
       treeScrollPane.setViewportView(myTree);
       treeScrollPane.repaint();
      }catch(Exception e){
       System.out.println("Error in loading new MIB.\n" + e.toString());
      }

     */
  }

  private void loadSingleFile(File file) {
    String strFileName = file.getAbsolutePath();
    treeSupport.loadNewFile(strFileName);
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(
          UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {}

    //Create the top-level container and add contents to it.
    JFrame frame = new JFrame("MIB Browser");
    frame.setSize(700, 550);
    DwSnmpMibTreeGUI tree1 = new DwSnmpMibTreeGUI();
    Component comp = tree1.createComponents();
    if (comp != null) {
      frame.getContentPane().add(comp);
    }
    else {
      JOptionPane.showMessageDialog(frame.getContentPane(),
                                    "Error in loading default MIBs.");
    }
    //Finish setting up the frame, and show it.
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    //frame.pack();
    frame.setVisible(true);
  }

  void sendGetRequest(String strReq) {
    if (strReq.endsWith("0")) {
      strReq = strReq.substring(0, strReq.lastIndexOf("."));
      outputText("Request : Get  " + strReq + "\n");
    } else if (strReq.endsWith("*")) {
      strReq = strReq.substring(0, strReq.lastIndexOf("*") - 1);
      outputText("Request : Walk " + strReq + "\n");
    } else if (strReq.endsWith(")")) {
      strReq = strReq.substring(0, strReq.indexOf("(") - 1);
      outputText("Request : Walk " + strReq + "\n");
    } else {
      outputError("Error in request. Please check the OID.");
    }

    final String strReqFin=strReq;
    Thread t=new Thread(new Runnable() {
      public void run() {
        snmp.snmpRequestGet(strReqFin);
      }
    });
    t.start();
  }


  void outputText(String s) {
    if (output != null) {
      output.println(s);
    }
    else {
      System.out.println(s);
    }
  }

  void outputError(String e) {
    if (output != null) {
      output.printError(e);
    }
    else {
      System.out.println(e);

    }
  }

  private void jbInit() throws Exception {
  }
} // END OF CLASS *****************************************
