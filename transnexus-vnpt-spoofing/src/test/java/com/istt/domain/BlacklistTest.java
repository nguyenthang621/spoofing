package com.istt.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.istt.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

public class BlacklistTest {

  @Test
  public void equalsVerifier() throws Exception {
    TestUtil.equalsVerifier(Blacklist.class);
    Blacklist blacklist1 = new Blacklist();
    blacklist1.setId(1L);
    Blacklist blacklist2 = new Blacklist();
    blacklist2.setId(blacklist1.getId());
    assertThat(blacklist1).isEqualTo(blacklist2);
    blacklist2.setId(2L);
    assertThat(blacklist1).isNotEqualTo(blacklist2);
    blacklist1.setId(null);
    assertThat(blacklist1).isNotEqualTo(blacklist2);
  }
}
