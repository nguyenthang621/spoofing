package com.istt.web.rest;

import com.istt.config.ApplicationProperties;
import com.istt.config.ss7.M3UAConfiguration;
import com.istt.service.M3uaHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.mobicents.protocols.ss7.m3ua.Asp;
import org.mobicents.protocols.ss7.m3ua.AspFactory;
import org.mobicents.protocols.ss7.m3ua.ExchangeType;
import org.mobicents.protocols.ss7.m3ua.Functionality;
import org.mobicents.protocols.ss7.m3ua.IPSPType;
import org.mobicents.protocols.ss7.m3ua.impl.parameter.NetworkAppearanceImpl;
import org.mobicents.protocols.ss7.m3ua.impl.parameter.ParameterFactoryImpl;
import org.mobicents.protocols.ss7.m3ua.parameter.NetworkAppearance;
import org.mobicents.protocols.ss7.m3ua.parameter.RoutingContext;
import org.mobicents.protocols.ss7.m3ua.parameter.TrafficModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest end point to configure SS7 configuration
 *
 * @author dinhtrung
 */
@RestController
@RequestMapping(value = "/api/public")
public class M3uaManagementResource {

  @Autowired M3UAConfiguration m3uaConfiguration;
  @Autowired 	ApplicationProperties props;
  @Autowired M3uaHandle m3uaHandle;
  
  /**
   * ====== M3UA Stack ===========
   *
   * @param message
   * @throws Exception
   */
  @GetMapping(value = "/m3ua")
  public ResponseEntity<String> startupInfo() throws Exception {
    M3UAConfiguration.clientM3UAMgmt.getName();
    return ResponseEntity.ok(M3UAConfiguration.clientM3UAMgmt.toString());
  }

  @GetMapping(value = "/m3ua/application-server/{name}")
  public ResponseEntity<String> createAs(@PathVariable String name) throws Exception {
    ParameterFactoryImpl factory = new ParameterFactoryImpl();
    RoutingContext rc = factory.createRoutingContext(new long[] {0l});
    TrafficModeType trafficModeType = factory.createTrafficModeType(TrafficModeType.Loadshare);
    NetworkAppearance na = new NetworkAppearanceImpl();
    M3UAConfiguration.clientM3UAMgmt.createAs(
        name, Functionality.IPSP, ExchangeType.SE, IPSPType.SERVER, rc, trafficModeType, 1, na);
    return ResponseEntity.ok(m3uaConfiguration.toString());
  }

  @GetMapping(value = "/m3ua/application-factory/{name}")
  public ResponseEntity<String> createAspFactory(
      @PathVariable String name, @RequestParam String assocName) throws Exception {
    AspFactory aspFactory = M3UAConfiguration.clientM3UAMgmt.createAspFactory(name, assocName);
    return ResponseEntity.accepted().body(aspFactory.getName());
  }

  @GetMapping("/m3ua/application-factories")
  public ResponseEntity<List<String>> listAspFactories() {
    List<String> aspFactories = new ArrayList<String>();
    for (ListIterator<AspFactory> it =
            M3UAConfiguration.clientM3UAMgmt.getAspfactories().listIterator();
        it.hasNext(); ) {
      AspFactory f = it.next();
      if (f.getStatus()) aspFactories.add(f.getName());
    }
    return ResponseEntity.ok(aspFactories);
  }

  @PostMapping(value = "/m3ua/application-factory/{name}")
  public ResponseEntity<String> startAsp(@PathVariable String name) throws Exception {
    M3UAConfiguration.clientM3UAMgmt.startAsp(name);
    return ResponseEntity.ok(name);
  }

  @DeleteMapping(value = "/m3ua/application-factory/{name}")
  public ResponseEntity<String> removeAsp(@PathVariable String name) throws Exception {
    M3UAConfiguration.clientM3UAMgmt.stopAsp(name);
    return ResponseEntity.ok(name);
  }

  @GetMapping(value = "/m3ua/assign-asp")
  public ResponseEntity<String> assignAsp(@RequestParam String asp, @RequestParam String as)
      throws Exception {
    Asp aspObj = M3UAConfiguration.clientM3UAMgmt.assignAspToAs(as, asp);
    return ResponseEntity.ok(aspObj.getName());
  }

  @GetMapping(value = "/m3ua/assign-route")
  public ResponseEntity<String> assignRoute(
      @RequestParam String asp,
      @RequestParam int dpc,
      @RequestParam(required = false) Integer opc,
      @RequestParam(required = false) Integer si)
      throws Exception {
    M3UAConfiguration.clientM3UAMgmt.addRoute(dpc, opc, si, asp);
    return ResponseEntity.ok(asp);
  }

  @DeleteMapping("/m3ua")
  public ResponseEntity<Void> stopM3ua() throws Exception {
    m3uaConfiguration.shutdown();
    return ResponseEntity.ok(null);
  }
  
  @PatchMapping("/m3ua")
  public ResponseEntity<Void> startM3ua() throws Exception {
    M3UAConfiguration.clientM3UAMgmt.start();
    return ResponseEntity.ok(null);
  }
  
  @GetMapping(value = "/m3ua/handle-mode")
  public ResponseEntity<String> getHandleMode() throws Exception {
    
    return ResponseEntity.ok("handle mode: "+props.getHandleMode());
  }

  
  @PostMapping(value = "/m3ua/handle-mode/{mode}")
  public ResponseEntity<String> setHandleMode(@PathVariable int mode) throws Exception {
    props.setHandleMode(mode);
    return ResponseEntity.ok(mode+"0");
  }
  
  @PostMapping(value = "/m3ua/fsm/{apply}")
  public ResponseEntity<String> readIsupFsm(@PathVariable boolean apply ,@RequestBody Map<String, String> payload) throws Exception {
	  String path = payload.get("path");
    int err=m3uaHandle.readFsm(path,apply );
    return ResponseEntity.ok("error : "+err);
  }
  @GetMapping(value = "/m3ua/simu/states")
  public ResponseEntity<String> displayStates(){
	  
    return ResponseEntity.ok(m3uaHandle.displayStates());
  }
  
  @PostMapping(value = "/m3ua/config/set")
  public ResponseEntity<String> setConfig(@RequestBody Map<String, String> payload) throws Exception {
	  String res="";
	  try {
		  String value = payload.get("worker-thread-mode");
		  if (value!=null &&  !value.isEmpty()) {
			  int num=Integer.parseInt(value);
			  props.setWorkerThreadMode(num);
			  res+=String.format("set worker thread mode %d\n", num);
		  }
	  }catch(Exception e) {}
	  try {
		  String value = payload.get("handle-mode");
		  if (value!=null &&  !value.isEmpty()) {
			  int num=Integer.parseInt(value);
			  props.setHandleMode(num);
			  res+=String.format("set handle mode %d\n", num);
		  }
	  }catch(Exception e) {}

	  
    if (res.isEmpty()) res="nothing changed";
    return ResponseEntity.ok(res);
  }
  
  @GetMapping(value = "/m3ua/config")
  public ResponseEntity<String> displayConfig(){
	  
    return ResponseEntity.ok("simu config: "+m3uaHandle.displayConfig());
  }
}
