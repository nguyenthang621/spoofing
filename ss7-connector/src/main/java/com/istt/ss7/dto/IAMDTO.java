package com.istt.ss7.dto;

import lombok.Data;

@Data
public class IAMDTO {

  public String callingParty;

  public String calledParty;

  public String txnId;

  public int cic = 0;
}
