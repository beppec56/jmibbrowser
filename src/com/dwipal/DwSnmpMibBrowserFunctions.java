package com.dwipal;

import java.util.*;
import java.net.*;
import org.snmp4j.*;
import org.snmp4j.event.*;
import org.snmp4j.transport.*;
import org.snmp4j.smi.*;
import org.snmp4j.mp.*;

public class DwSnmpMibBrowserFunctions {
  private DwSnmpMibOutputHandler output = null;
  private DwSnmpOidSupport oidSupport = null;

  private String agentIP = "192.168.2.9";
  private int agentPort = 161;
  private String setCommunity = "public";
  private String getCommunity = "public";

  private SnmpLib m_snmpLib;

  public DwSnmpMibBrowserFunctions() {
  }

  public void setOutput(DwSnmpMibOutputHandler output) {
    this.output = output;
  }

  public void setOidSupport(DwSnmpOidSupport oidSupport) {
    this.oidSupport = oidSupport;
  }

  public void setIP(String s) {
    m_snmpLib=null;
    this.agentIP = s;
  }

  public void setPort(int p) {
    m_snmpLib=null;
    this.agentPort = p;
  }

  public void setCommunity(String get, String set) {
    m_snmpLib=null;
    this.getCommunity = get;
    this.setCommunity = set;
  }

  public String getIP() {
    return agentIP;
  }

  public int getPort() {
    return agentPort;
  }

  public String getReadCommunity() {
    return getCommunity;
  }

  public String getWriteCommunity() {
    return setCommunity;
  }

  void destroySession() {
    getSnmpLib().destroySession();
  }

  public void outputRecord(SnmpOidValuePair oidval) {
    try {
      if (oidSupport != null) {
        outputText("Oid : " + oidval.oid + " (" +
                   oidSupport.resolveOidName(oidval.oid) + " )");
      }
      else {
        outputText("Oid : " + oidval.oid);
      }
    }
    catch (Exception e) {
      outputError("Cannot resolve Name from OID..\n" + e.toString());
    }
    outputText("Value: " + oidval.value_str);

  }

  void outputError(String s) {
    try {
      output.printError(s);
    }
    catch (Exception e) {
      System.out.println(s);
    }

  }

  void outputText(String s) {
    try {
      output.println(s);
    }
    catch (Exception e) {
      System.out.println(s);
    }
  }

  public void snmpRequestGet(String strVar, String strTo) {
    try {
      getSnmpLib().snmpWalk(strVar);
    } catch (Exception e) {
      outputError("\nError in executing GET request : \n" + e.toString());
      e.printStackTrace();
     }
  }

  public DwSnmpRequest snmpRequestGet(String strVar) {
    snmpRequestGet(strVar, null);
    return null;
  }

  String processSetRequest(DwSnmpMibRecord setRec, String oid, String setVal) {
    try {
      getSnmpLib().snmpSetValue(oid, setRec.getSyntaxID(), setVal);
    } catch (Exception e) {
      outputError("\nError in executing SET request : \n" + e.toString());
      e.printStackTrace();
     }
     return "";
  }

  private SnmpLib getSnmpLib() {
    if(m_snmpLib==null) {
      m_snmpLib = new SnmpLib();
      m_snmpLib.setHost(getIP());
      m_snmpLib.setPort(getPort());
      m_snmpLib.setCommunity(getReadCommunity(), getWriteCommunity());

      m_snmpLib.setSnmpResponseHandler(new ISnmpResponseHandler() {
        public void responseReceived(SnmpOidValuePair resp_values) {
          outputRecord(resp_values);
        }

        public void requestStats(int totalRequests, int totalResponses,
                                 long timeInMillis) {
          if (totalResponses == 0) totalResponses = 1;
          outputText("\nReceived " + (totalResponses - 1) + " record(s) in " +
                     timeInMillis + " milliseconds.");
        }
      });
    }
    return m_snmpLib;
  }
}
