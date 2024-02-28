package com.istt.web.rest;

import com.istt.service.IsupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest endpoint to configure SCTP Server configuration
 *
 * @author dinhtrung
 */
@RestController
@RequestMapping(value = "/api/public")
public class IsupResource {

  private final Logger log = LoggerFactory.getLogger(IsupResource.class);

  @Autowired IsupService isupSvc;

  /**
   * List all defined associations in SCTP layer
   *
   * @param message
   * @throws Exception
   */
  @GetMapping(value = "/send-iam-test")
  public ResponseEntity<Void> sendTestIam(
      @RequestParam(required = false) String calling,
      @RequestParam(required = false) String called,
      @RequestParam(required = false) Integer opc,
      @RequestParam(required = false) Integer dpc)
      throws Exception {
    log.info("[{} -> {}] Send Test IAM: {} -> {} ", opc, dpc, calling, called);
    isupSvc.sendTestIAMMessage(calling, called, opc, dpc);
    return ResponseEntity.accepted().body(null);
  }

  @GetMapping(value = "/send-rel-test")
  public ResponseEntity<Void> sendTestRel(
      @RequestParam Integer cic,
      @RequestParam(required = false) Integer opc,
      @RequestParam(required = false) Integer dpc)
      throws Exception {
    log.info("[{} -> {}] Send Test REL: {}", opc, dpc, cic);
    isupSvc.sendTestRELMessage(cic, opc, dpc);
    return ResponseEntity.accepted().body(null);
  }
}
