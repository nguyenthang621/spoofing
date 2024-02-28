package com.istt.web.rest;

import com.istt.config.ss7.M3UAConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import org.restcomm.protocols.ss7.m3ua.As;
import org.restcomm.protocols.ss7.m3ua.Asp;
import org.restcomm.protocols.ss7.m3ua.AspFactory;
import org.restcomm.protocols.ss7.m3ua.ExchangeType;
import org.restcomm.protocols.ss7.m3ua.Functionality;
import org.restcomm.protocols.ss7.m3ua.IPSPType;
import org.restcomm.protocols.ss7.m3ua.impl.parameter.NetworkAppearanceImpl;
import org.restcomm.protocols.ss7.m3ua.impl.parameter.ParameterFactoryImpl;
import org.restcomm.protocols.ss7.m3ua.parameter.NetworkAppearance;
import org.restcomm.protocols.ss7.m3ua.parameter.RoutingContext;
import org.restcomm.protocols.ss7.m3ua.parameter.TrafficModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class M3uaManagementResource {

    @Autowired
    M3UAConfiguration m3uaConfiguration;

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

    @PatchMapping(value = "/m3ua")
    public ResponseEntity<String> start() throws Exception {
        M3UAConfiguration.clientM3UAMgmt.start();
        return ResponseEntity.ok(M3UAConfiguration.clientM3UAMgmt.toString());
    }

    @GetMapping(value = "/m3ua/application-servers")
    public ResponseEntity<List<String>> listAs() throws Exception {
        List<String> output = M3UAConfiguration.clientM3UAMgmt.getAppServers().stream().map(As::getName).collect(Collectors.toList());
        return ResponseEntity.ok(output);
    }

    @GetMapping(value = "/m3ua/application-server/{name}")
    public ResponseEntity<String> createAs(@PathVariable String name) throws Exception {
        ParameterFactoryImpl factory = new ParameterFactoryImpl();
        RoutingContext rc = factory.createRoutingContext(new long[] { 0l });
        TrafficModeType trafficModeType = factory.createTrafficModeType(TrafficModeType.Loadshare);
        NetworkAppearance na = new NetworkAppearanceImpl();
        As as = M3UAConfiguration.clientM3UAMgmt.createAs(
            name,
            Functionality.IPSP,
            ExchangeType.SE,
            IPSPType.CLIENT,
            rc,
            trafficModeType,
            1,
            na
        );
        return ResponseEntity.ok(as.getName());
    }

    @DeleteMapping(value = "/m3ua/application-server/{name}")
    public ResponseEntity<String> removeAs(@PathVariable String name) throws Exception {
        if (!M3UAConfiguration.clientM3UAMgmt.getAppServers().removeIf(i -> i.getName().equalsIgnoreCase(name))) {
            throw Problem.builder().withStatus(Status.NOT_FOUND).withTitle("Application Server not found").with("name", name).build();
        }
        return ResponseEntity.ok(name);
    }

    @GetMapping(value = "/m3ua/application-factory/{name}")
    public ResponseEntity<String> createAspFactory(@PathVariable String name, @RequestParam String assocName) throws Exception {
        AspFactory aspFactory = M3UAConfiguration.clientM3UAMgmt.createAspFactory(name, assocName);
        return ResponseEntity.accepted().body(aspFactory.getName());
    }

    @GetMapping("/m3ua/application-factories")
    public ResponseEntity<List<String>> listAspFactories() {
        List<String> aspFactories = new ArrayList<String>();
        for (ListIterator<AspFactory> it = M3UAConfiguration.clientM3UAMgmt.getAspfactories().listIterator(); it.hasNext();) {
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
    public ResponseEntity<String> assignAsp(@RequestParam String asp, @RequestParam String as) throws Exception {
        Asp aspObj = M3UAConfiguration.clientM3UAMgmt.assignAspToAs(as, asp);
        return ResponseEntity.ok(aspObj.getName());
    }

    @GetMapping(value = "/m3ua/assign-route")
    public ResponseEntity<String> assignRoute(
        @RequestParam String asp,
        @RequestParam int dpc,
        @RequestParam(required = false) Integer opc,
        @RequestParam(required = false) Integer si
    ) throws Exception {
        M3UAConfiguration.clientM3UAMgmt.addRoute(dpc, opc, si, asp);
        return ResponseEntity.ok(asp);
    }

    @DeleteMapping("/m3ua")
    public ResponseEntity<Void> stopM3ua() throws Exception {
        m3uaConfiguration.shutdown();
        return ResponseEntity.ok(null);
    }
}
