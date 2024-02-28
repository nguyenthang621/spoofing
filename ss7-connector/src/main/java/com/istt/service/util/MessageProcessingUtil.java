package com.istt.service.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

public class MessageProcessingUtil {

  /**
   * Split String helper
   *
   * @param buf
   * @param maxLen
   * @return
   */
  public static ArrayList<String> splitStr(String buf, int maxLen) {
    ArrayList<String> res = new ArrayList<String>();

    String prevBuf = buf;

    while (true) {
      if (prevBuf.length() <= maxLen) {
        res.add(prevBuf);
        break;
      }

      String segm = prevBuf.substring(0, maxLen);
      String newBuf = prevBuf.substring(maxLen, prevBuf.length());

      res.add(segm);
      prevBuf = newBuf;
    }

    return res;
  }

  /**
   * Convert error message into map
   *
   * <p>MAPErrorMessageCallBarred [callBarringCause=operatorBarring]
   *
   * @param errorMessage
   * @return
   */
  private static final Pattern errorMsgPattern = Pattern.compile("^(\\w+) \\[(\\w+)=(\\w+)\\]");

  public static String extractError(String errorMessage) {
    Matcher m = errorMsgPattern.matcher(errorMessage);
    if (m.matches()) {
      return m.group(3);
    }
    return "UNKNOWN";
  }
  
  /**
   * Helper function
   *
   * @param value
   * @return
   */
  public static Set<Long> toPrefixSet(String value) {
    Set<Long> result = new HashSet<>();
    if (value == null) return result;
    for (int i = 1; i <= value.length(); i++) {
      result.add(Long.parseLong(value.substring(0, i)));
    }
    return result;
  }
}
