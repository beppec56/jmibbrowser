package com.dwipal;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ISnmpLib {
  public void setHost(String host);
  public String getHost();
  public void setPort(int port);
  public int getPort();

}

