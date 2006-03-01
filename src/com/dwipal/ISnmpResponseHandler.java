package com.dwipal;

public interface ISnmpResponseHandler {
  public void responseReceived(SnmpOidValuePair resp_values);
  public void requestStats(int totalRequests, int totalResponses, long timeInMillis);
}
