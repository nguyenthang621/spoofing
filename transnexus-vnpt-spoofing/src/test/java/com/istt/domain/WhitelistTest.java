package com.istt.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.istt.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

public class WhitelistTest {

  @Test
  public void equalsVerifier() throws Exception {
    TestUtil.equalsVerifier(Whitelist.class);
    Whitelist whitelist1 = new Whitelist();
    whitelist1.setId(1L);
    Whitelist whitelist2 = new Whitelist();
    whitelist2.setId(whitelist1.getId());
    assertThat(whitelist1).isEqualTo(whitelist2);
    whitelist2.setId(2L);
    assertThat(whitelist1).isNotEqualTo(whitelist2);
    whitelist1.setId(null);
    assertThat(whitelist1).isNotEqualTo(whitelist2);
  }
}
