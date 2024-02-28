package com.istt.service.dto;

import java.util.HashSet;
import java.util.Set;

public class SctpClientAssocDTO {

  private String name = "ASS_CLIENT";
  private String hostIp = "127.0.0.1";
  private int hostPort = 8011;
  private boolean server = false;
  private String peerIp = "127.0.0.1";
  private int peerPort = 8012;

  @Override
  public String toString() {
    return "SctpClientAssocDTO [hostIp="
        + hostIp
        + ", hostPort="
        + hostPort
        + ", name="
        + name
        + ", peerIp="
        + peerIp
        + ", peerPort="
        + peerPort
        + ", extraHostAddresses="
        + extraHostAddresses
        + "]";
  }

  private Set<String> extraHostAddresses = new HashSet<>(); // for multi-homing

  public String getHostIp() {
    return hostIp;
  }

  public void setHostIp(String hostIp) {
    this.hostIp = hostIp;
  }

  public int getHostPort() {
    return hostPort;
  }

  public void setHostPort(int hostPort) {
    this.hostPort = hostPort;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPeerIp() {
    return peerIp;
  }

  public void setPeerIp(String peerIp) {
    this.peerIp = peerIp;
  }

  public int getPeerPort() {
    return peerPort;
  }

  public void setPeerPort(int peerPort) {
    this.peerPort = peerPort;
  }

  public Set<String> getExtraHostAddresses() {
    return extraHostAddresses;
  }

  public void setExtraHostAddresses(Set<String> extraHostAddresses) {
    this.extraHostAddresses = extraHostAddresses;
  }

  public boolean isServer() {
    return server;
  }

  public void setServer(boolean server) {
    this.server = server;
  }
}
