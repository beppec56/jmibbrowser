package com.dwipal;

public class SnmpOidValuePair {
  public SnmpOidValuePair() {
  }

  public String oid;
  public String value_str;

  public String toString() {
    return "OID: " + oid + " Value: " + value_str;
  }
}
