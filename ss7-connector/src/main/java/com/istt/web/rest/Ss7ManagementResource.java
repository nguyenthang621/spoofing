package com.istt.web.rest;

import com.istt.config.ApplicationProperties;
import com.istt.config.app.IsupProxyConfiguration;
import com.istt.config.ss7.MAPConfiguration;
import com.istt.config.ss7.TCAPConfiguration;
import com.istt.service.dto.AddressTranslatorDTO;
import com.istt.service.util.MessageProcessingUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

/**
 * Rest endpoint to configure SS7 configuration
 *
 * @author dinhtrung
 */
@RestController
@RequestMapping(value = "/api/public")
public class Ss7ManagementResource {

  private final Logger log = LoggerFactory.getLogger(Ss7ManagementResource.class);

  @Autowired MAPConfiguration mapConfiguration;

  @Autowired TCAPConfiguration tcapConfiguration;
  
  @Autowired IsupProxyConfiguration isupProps;
  
  @Autowired ApplicationProperties props;

  @GetMapping(value = "/nats")
  public ResponseEntity<Void> nat(@RequestParam String address, @RequestParam int noa) {
	  Set<Long> prefixes = MessageProcessingUtil.toPrefixSet(address);
	  List<Integer> lengths = Arrays.asList(0, address.length());
	  List<Integer> noas = Arrays.asList(0, noa);
	  AddressTranslatorDTO rule = isupProps.getNats().stream().filter(i -> (prefixes.contains(i.getPrefix()) && noas.contains(i.getNoa()) && lengths.contains(i.getLength())) )
	  .sorted(Comparator.comparing(AddressTranslatorDTO::getLength).reversed())
	  .findFirst()
	  .orElseThrow(() -> Problem.builder().withTitle("Not modified").withStatus(Status.NOT_IMPLEMENTED).with("address", address).with("noa", noa).withDetail(Long.parseLong(address) + "").build());
	  throw Problem.builder().withTitle("Translated").withStatus(Status.MOVED_PERMANENTLY)
	  .with("address", address)
	  .with("noa", noa)
	  .with("rule", rule)
	  .withDetail(Long.parseLong(rule.isEnabled() ? props.msisdn(address) : address) + "")
	  .build();
  }
  /**
   * ====== TCAP Stack ===========
   *
   * @param message
   * @throws Exception
   */
  @GetMapping(value = "/tcap")
  public ResponseEntity<String> startupTcap() throws Exception {
    tcapConfiguration.startUp();
    return ResponseEntity.ok(tcapConfiguration.toString());
  }

  @DeleteMapping("/tcap")
  public ResponseEntity<Void> stopTcap() throws Exception {
    tcapConfiguration.shutdown();
    return ResponseEntity.ok(null);
  }

  /**
   * ====== TCAP Stack ===========
   *
   * @param message
   * @throws Exception
   */
  @GetMapping(value = "/map")
  public ResponseEntity<String> startupMap() throws Exception {
    tcapConfiguration.startUp();
    return ResponseEntity.ok(tcapConfiguration.toString());
  }

  @DeleteMapping("/map")
  public ResponseEntity<Void> stopMap() throws Exception {
    tcapConfiguration.shutdown();
    return ResponseEntity.ok(null);
  }
}
