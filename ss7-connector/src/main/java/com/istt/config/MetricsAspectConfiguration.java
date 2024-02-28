package com.istt.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class MetricsAspectConfiguration {

  @Autowired MeterRegistry registry;

  @Bean
  public CountedAspect countedAspect() {
    return new CountedAspect(registry);
  }

  @Bean
  public TimedAspect timedAspect() {
    return new TimedAspect(registry);
  }
}
