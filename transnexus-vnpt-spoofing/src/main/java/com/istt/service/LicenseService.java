package com.istt.service;

import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Service
public class LicenseService {

  @Autowired ApplicationProperties props;

  public void checkLicense() {
    try {
      Claims claim =
          Jwts.parserBuilder()
              .setSigningKey(Constants.LICENSE_KEY)
              .build()
              .parseClaimsJws(props.getLicense())
              .getBody();
      if (claim.getExpiration().before(new Date())) {
        throw Problem.builder()
            .withStatus(Status.EXPECTATION_FAILED)
            .withDetail("License expired.")
            .build();
      }
    } catch (Exception e) {
      throw Problem.builder()
          .withStatus(Status.EXPECTATION_FAILED)
          .withDetail("Invalid license.")
          .build();
    }
  }
}

