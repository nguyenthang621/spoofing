package com.istt.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.istt.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

public class CallLogTest {

  @Test
  public void equalsVerifier() throws Exception {
    TestUtil.equalsVerifier(CallLog.class);
    CallLog callLog1 = new CallLog();
    callLog1.setId(1L);
    CallLog callLog2 = new CallLog();
    callLog2.setId(callLog1.getId());
    assertThat(callLog1).isEqualTo(callLog2);
    callLog2.setId(2L);
    assertThat(callLog1).isNotEqualTo(callLog2);
    callLog1.setId(null);
    assertThat(callLog1).isNotEqualTo(callLog2);
  }
}
