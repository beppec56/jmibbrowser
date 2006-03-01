package com.dwipal;

import java.io.*;

public class DwSnmpMibRecord
    implements Serializable {
  public static final int VALUE_TYPE_NONE = 0;
  public static final int VALUE_TYPE_NULL = 1;
  public static final int VALUE_TYPE_INTEGER32 = 2;
  public static final int VALUE_TYPE_UNSIGNED_INTEGER32 = 3;
  public static final int VALUE_TYPE_OCTET_STRING = 4;
  public static final int VALUE_TYPE_OID = 5;
  public static final int VALUE_TYPE_TIMETICKS = 6;
  public static final int VALUE_TYPE_IP_ADDRESS = 7;

  public static int recNormal = 0;
  public static int recVariable = -1;
  public static int recTable = 1;

  public String name = "";
  public String parent = "";
  public int number = 0;
  public String description = "";
  public String access = "";
  public String status = "";
  public String syntax = "";
  public int recordType = recNormal;
  public int tableEntry = -1;
  public String index = "";

  /*
   recordType =  recNormal/recVariable/recTable(1,2,3)
   During parsing, it is always kept -1 ,1 or 0.

   This is used during tree generation...
   During parsing, recordType is always kept 0 for simplification reason
   recordType = 1  =>  it is ITSELF a table (i.e. its syntax is a Sequence)
        = 2  =>  it is a entry (elements under "Entry" are actual elements
        = 3  =>  it is an element. Index values are appended to it
        instead of .0
        = 0  =>  The entry is not in any table,i.e. it is a normal record :)

   Variable tableEntry is used to store the no. of entries the
   table contains,i.e. number of values  for each element of table.
   -1 means it has not been initialized yet.
   */

  DwSnmpMibRecord() {
    init();
  }

  public void init() {
    name = "";
    parent = "";
    number = 0;
    description = "";
    access = "";
    status = "";
    syntax = "";
    recordType = recNormal;
    index = "";
  }

  public String getCompleteString() {
    String returnVal = new String("");
    returnVal = returnVal.concat("Name   : " + name + "\n");
    returnVal = returnVal.concat("Parent : " + parent + "\n");
    returnVal = returnVal.concat("Number : " + number + "\n");
    returnVal = returnVal.concat("Access : " + access + "\n");
    returnVal = returnVal.concat("Syntax : " + syntax + "");
    returnVal = returnVal.concat("Status : " + status + "\n");
    if (index.equals("") != true) returnVal = returnVal.concat("Index : " + index + "\n");
    String desc = "";
    int i = 50;
    while (i < desc.length()) {
      desc = desc + description.substring(i - 50, i);
      desc = desc + "\n";
      i += 50;
    }

    desc = desc + description.substring(i - 50);
    returnVal = returnVal.concat("Description :" + desc + "\n");
    returnVal = returnVal.concat("Type :" + recordType + "\n");

    return returnVal;
  }

  public boolean isWritable() {
    if(access.toUpperCase().indexOf("WRITE")!=-1) {
      return true;
    }
    return false;
  }
  public String toString() {
    return name;
  }

  /**
   * checkValidValue
   *
   * @param setValue String
   * @return boolean
   */
  public boolean checkValidValue(String setValue) {
    return true;
  }

  public int getSyntaxID() {
    String strType=syntax.trim().toUpperCase();
    if(strType.indexOf("INTEGER")!=-1)
      return VALUE_TYPE_INTEGER32;
    else if(strType.indexOf("COUNTER") !=-1)
      return VALUE_TYPE_UNSIGNED_INTEGER32;
    else if(strType.indexOf("STRING") !=-1)
      return VALUE_TYPE_OCTET_STRING;
    else if(strType.indexOf("OID")!=-1)
      return VALUE_TYPE_OID;
    else if(strType.indexOf("TIMETICK") !=-1)
      return VALUE_TYPE_TIMETICKS;
    else if(strType.indexOf("IPADDRESS") !=-1)
      return VALUE_TYPE_IP_ADDRESS;
    else if(strType.indexOf("NULL") !=-1)
      return VALUE_TYPE_NULL;

    // Default: NONE.
    return VALUE_TYPE_NONE;
  }

  /**
   * getSyntaxIDString
   *
   * @return String
   */
  public String getSyntaxIDString() {
    switch(getSyntaxID()) {
      case VALUE_TYPE_INTEGER32:
        return "Integer";
      case VALUE_TYPE_UNSIGNED_INTEGER32:
        return "Unsigned Integer";
      case VALUE_TYPE_OCTET_STRING:
        return "Octet String";
      case VALUE_TYPE_OID:
        return "OID";
      case VALUE_TYPE_TIMETICKS:
        return "Time Ticks";
      case VALUE_TYPE_IP_ADDRESS:
        return "IP Address";
      case VALUE_TYPE_NULL:
        return "Null";
    }
    return "Unknown";
  }
}



