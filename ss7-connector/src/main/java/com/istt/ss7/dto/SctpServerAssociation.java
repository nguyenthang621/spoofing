package com.istt.ss7.dto;

import java.util.Arrays;

public class SctpServerAssociation {
  private String name;
  private String host;
  private int port;
  private boolean acceptAnonymousConnections = false;
  private int maxConcurrentConnectionsCount = 0;
  private String[] extraHostAddresses = null;

  @Override
  public String toString() {
    return "SctpServerAssociation [name="
        + name
        + ", host="
        + host
        + ", port="
        + port
        + ", acceptAnonymousConnections="
        + acceptAnonymousConnections
        + ", maxConcurrentConnectionsCount="
        + maxConcurrentConnectionsCount
        + ", extraHostAddresses="
        + Arrays.toString(extraHostAddresses)
        + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isAcceptAnonymousConnections() {
    return acceptAnonymousConnections;
  }

  public void setAcceptAnonymousConnections(boolean acceptAnonymousConnections) {
    this.acceptAnonymousConnections = acceptAnonymousConnections;
  }

  public int getMaxConcurrentConnectionsCount() {
    return maxConcurrentConnectionsCount;
  }

  public void setMaxConcurrentConnectionsCount(int maxConcurrentConnectionsCount) {
    this.maxConcurrentConnectionsCount = maxConcurrentConnectionsCount;
  }

  public String[] getExtraHostAddresses() {
    return extraHostAddresses;
  }

  public void setExtraHostAddresses(String[] extraHostAddresses) {
    this.extraHostAddresses = extraHostAddresses;
  }
}
