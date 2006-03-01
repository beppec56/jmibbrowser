package com.dwipal;

public class SnmpException extends Exception {
  public SnmpException() {
    super("SNMP Exception");
  }
  public SnmpException(String msg) {
    super(msg);
  }
  public SnmpException(Exception exception) {
    super(exception);
  }
}
