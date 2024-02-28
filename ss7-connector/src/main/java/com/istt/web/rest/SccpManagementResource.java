package com.istt.web.rest;

import com.istt.config.ss7.SCCPConfiguration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.mobicents.protocols.ss7.sccp.RemoteSignalingPointCode;
import org.mobicents.protocols.ss7.sccp.RemoteSubSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest endpoint to configure SS7 configuration
 *
 * <p>sccpStack.getSccpResource().addRemoteSpc(0, SERVER_SPC, 0, 0);
 * sccpStack.getSccpResource().addRemoteSsn(0, SERVER_SPC, SSN, 0, false);
 *
 * <p>sccpStack.getRouter().addMtp3ServiceAccessPoint(1, 1, CLIENT_SPC, NETWORK_INDICATOR, 0);
 * sccpStack.getRouter().addMtp3Destination(1, 1, SERVER_SPC, SERVER_SPC, 0, 255, 255);
 *
 * <p>ParameterFactoryImpl fact = new ParameterFactoryImpl(); EncodingScheme ec = new
 * BCDEvenEncodingScheme(); GlobalTitle gt1 = fact.createGlobalTitle("-", 0,
 * org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, ec,
 * NatureOfAddress.INTERNATIONAL); GlobalTitle gt2 = fact.createGlobalTitle("-", 0,
 * org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, ec,
 * NatureOfAddress.INTERNATIONAL); SccpAddress localAddress = new
 * SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt1, CLIENT_SPC, 0);
 * sccpStack.getRouter().addRoutingAddress(1, localAddress); SccpAddress remoteAddress = new
 * SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt2, SERVER_SPC, 0);
 * sccpStack.getRouter().addRoutingAddress(2, remoteAddress);
 *
 * <p>GlobalTitle gt = fact.createGlobalTitle("*", 0,
 * org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY, ec,
 * NatureOfAddress.INTERNATIONAL); SccpAddress pattern = new
 * SccpAddressImpl(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gt, 0, 0);
 * sccpStack.getRouter().addRule(1, RuleType.SOLITARY, LoadSharingAlgorithm.Bit0,
 * OriginationType.REMOTE, pattern, "K", 1, -1, null, 0); sccpStack.getRouter().addRule(2,
 * RuleType.SOLITARY, LoadSharingAlgorithm.Bit0, OriginationType.LOCAL, pattern, "K", 2, -1, null,
 * 0);
 *
 * @author dinhtrung
 */
@RestController
@RequestMapping(value = "/api/public")
public class SccpManagementResource {

  private final Logger log = LoggerFactory.getLogger(SccpManagementResource.class);

  @Autowired SCCPConfiguration sccpConfiguration;

  @GetMapping("/sccp/spc")
  public ResponseEntity<String> getRemoteSpcs() {
    String result = "";
    for (Iterator<Entry<Integer, RemoteSignalingPointCode>> it =
            SCCPConfiguration.sccpStack.getSccpResource().getRemoteSpcs().entrySet().iterator();
        it.hasNext(); ) {
      Entry<Integer, RemoteSignalingPointCode> e = it.next();
      e.getValue().getRemoteSpc();
      e.getValue().getMask();
      e.getValue().getRemoteSpcFlag();
      result += e.getValue().toString();
    }
    return ResponseEntity.ok().body(result);
  }

  @PostMapping("/sccp/spc")
  public ResponseEntity<String> addRemoteSourcePointcode(@RequestBody List<Integer> pointcodes) {
    int i = 0;
    for (Integer spc : pointcodes) {
      try {
        SCCPConfiguration.sccpStack.getSccpResource().addRemoteSpc(i, spc, 0, 0);
        i++;
      } catch (Exception e) {
        log.error("Cannot add remote SPC: {}", spc, e);
      }
    }
    return ResponseEntity.ok().body(null);
  }

  @GetMapping("/sccp/ssn")
  public ResponseEntity<String> getRemoteSsn() {
    String result = "";
    for (Iterator<Entry<Integer, RemoteSubSystem>> it =
            SCCPConfiguration.sccpStack.getSccpResource().getRemoteSsns().entrySet().iterator();
        it.hasNext(); ) {
      Entry<Integer, RemoteSubSystem> e = it.next();
      e.getValue().getRemoteSpc();
      e.getValue().getRemoteSsn();
      e.getValue().getRemoteSsnFlag();
      result += e.getValue().toString();
    }
    return ResponseEntity.ok().body(result);
  }

  @PostMapping("/sccp/ssn")
  public ResponseEntity<String> addRemoteSsn(
      @RequestParam int remoteSsnid,
      @RequestParam int spc,
      @RequestParam int remoteSsn,
      @RequestParam int remoteSsnFlag,
      @RequestParam boolean markProhibitedWhenSpcResuming) {
    try {
      SCCPConfiguration.sccpStack
          .getSccpResource()
          .addRemoteSsn(remoteSsnid, spc, remoteSsn, remoteSsnFlag, markProhibitedWhenSpcResuming);
    } catch (Exception e) {
      log.error("Cannot add remote SSN: {}", spc, e);
    }
    return ResponseEntity.ok().body(null);
  }

  @DeleteMapping("/sccp")
  public ResponseEntity<Void> stopSccp() throws Exception {
    sccpConfiguration.shutdown();
    return ResponseEntity.ok(null);
  }
}
