package com.istt.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.istt.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

public class RoutingRuleTest {

  @Test
  public void equalsVerifier() throws Exception {
    TestUtil.equalsVerifier(RoutingRule.class);
    RoutingRule routingRule1 = new RoutingRule();
    routingRule1.setId(1L);
    RoutingRule routingRule2 = new RoutingRule();
    routingRule2.setId(routingRule1.getId());
    assertThat(routingRule1).isEqualTo(routingRule2);
    routingRule2.setId(2L);
    assertThat(routingRule1).isNotEqualTo(routingRule2);
    routingRule1.setId(null);
    assertThat(routingRule1).isNotEqualTo(routingRule2);
  }
}
