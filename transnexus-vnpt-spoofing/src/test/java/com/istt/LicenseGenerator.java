package com.istt;

import com.istt.config.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

public class LicenseGenerator {

  @Test
  public void generateKey() {

    LocalDate dt = LocalDate.of(2023, 8, 18);
    Date validity = Date.from(dt.atStartOfDay().toInstant(ZoneOffset.UTC));

    byte[] keyBytes = Decoders.BASE64.decode(Constants.LICENSE_KEY);
    SecretKey key = Keys.hmacShaKeyFor(keyBytes);

    String license =
        Jwts.builder()
            .setSubject("istt VNPT SPOOFING 2020")
            .claim("CLIENT", "VNPT")
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();

    System.out.println(license);
  }

  @Test
  public void parseKey() {
    String token =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJKVU5PIFZOUFQgU1BPT0ZJTkcgMjAyMCIsIkNMSUVOVCI6IlZOUFQiLCJleHAiOjE1OTc3MDg4MDB9.k8Jn77d4XNlSsRtsarAOwZ7SiS9cKnnZDKnLgstdFdd4mCKBhdYdziM1PGn5TTFwvgLmnSlbl8ulQKbsshGQ4Q";
    Claims claim =
        Jwts.parserBuilder()
            .setSigningKey(Constants.LICENSE_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody();
    if (claim.getExpiration().before(new Date())) {
      throw Problem.builder()
          .withStatus(Status.EXPECTATION_FAILED)
          .withDetail("License expired.")
          .build();
    }
    System.out.println("OK");
  }

  @Test
  public void checkPrefixes() {
    String hlrGt = "84900406";
    List<String> hlrPrefixes = Arrays.asList("8490");
    boolean result = hlrPrefixes.stream().anyMatch(hlrGt::startsWith);
    System.out.println("Result: " + result);
  }
  
  @Test
  public void testMsrn() {
	  String msrn = "84004977014185";
	  String prefix = msrn.substring(0, 5);
		String msisdn = msrn.substring(0,2) + msrn.substring(5);
	  System.out.println("msrn: " + msrn + " prefix: " + prefix + " msisdn: " + msisdn);
  }
}
