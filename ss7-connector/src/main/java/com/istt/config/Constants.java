package com.istt.config;

/** Application constants. */
public final class Constants {

  public static final String SYSTEM_ACCOUNT = "system";

  private Constants() {}

  public static final String PENDING_REQ = "SS7";
  
  public static final String SOURCE_NAME = "TestSmsServer";

  public static final String OAM = "OAM";

  public static final String TXN = "TXN_";

  public static final String PENDING_COP = "PENDING_COP";

  public static final String PENDING_IAM = "PENDING_IAM";

  public static final String CIC_MASK = "CIC_MASK";
  
  // Migrate from logic
  
  public static final String REQ_CHANNEL = "channel";
  
  public static final String REQ_INSTANCE = "instance";
  
  public static final String REQ_CALLREF = "callref";
  
  public static final String BLACKLIST_PREFIX = "BLK_";

  // Bitmask for call flow
  public static final int IAM = 1; // Binary 000001
  public static final int COT = 2; // Binary 000010
  public static final int ACM = 4; // Binary 000100
  public static final int CPG = 8; // Binary 000100
  public static final int ANM = 16; // Binary 001000
  public static final int REL = 32; // Binary 010000
  public static final int RLC = 64; // Binary 100000
  public static final int API = 128; // Mark if API is successfully processed or not
  public static final int BLK = 256; // Mark if this call is block or not

  // States
  public static final int STATE_PENDING = -1;
  public static final int STATE_REJECT = 0; // Binary 000001
  public static final int STATE_ACCEPT = 1; // Binary 000010
}
