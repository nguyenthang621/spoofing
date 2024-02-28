package com.istt.web.rest;

import com.istt.config.ss7.SCTPConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mobicents.protocols.api.Association;
import org.mobicents.protocols.api.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
public class SctpManagementResource {

    private final Logger log = LoggerFactory.getLogger(SctpManagementResource.class);

    @Autowired
    SCTPConfiguration sctpConfiguration;

    /**
     * List all defined associations in SCTP layer
     *
     * @param message
     * @throws Exception
     */
    @GetMapping(value = "/sctp/associations")
    public ResponseEntity<List<Map>> sctpListAssociate() throws Exception {
        return ResponseEntity.ok(
            SCTPConfiguration.sctpManagement
                .getAssociations()
                .values()
                .stream()
                .map(
                    as -> {
                        HashMap<String, String> obj = new HashMap<String, String>();
                        obj.put("ipChannelType", as.getIpChannelType().toString());
                        obj.put("associationType", as.getAssociationType().toString());
                        obj.put("name", as.getName());
                        //        			obj.put("associationListener",
                        // as.getAssociationListener().toString());
                        obj.put("hostAddress", as.getHostAddress());
                        obj.put("hostPort", as.getHostPort() + "");
                        obj.put("peerAddress", as.getPeerAddress());
                        obj.put("peerPort", as.getPeerPort() + "");
                        obj.put("serverName", as.getServerName());
                        obj.put("extraHostAddresses", StringUtils.arrayToCommaDelimitedString(as.getExtraHostAddresses()));
                        obj.put("congestionLevel", as.getCongestionLevel() + "");

                        obj.put("connected", as.isConnected() ? "CONNECTED" : "DISCONNECTED");
                        obj.put("started", as.isStarted() ? "STARTED" : "STOPPED");
                        obj.put("up", as.isUp() ? "ACTIVE" : "DOWN");
                        return obj;
                    }
                )
                .collect(Collectors.toList())
        );
    }

    @DeleteMapping("/sctp/associations/{name}")
    public ResponseEntity<Void> stopAssociation(@PathVariable String name) throws Exception {
        SCTPConfiguration.sctpManagement.stopAssociation(name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/sctp/associations/{name}")
    public ResponseEntity<Void> startAssociation(@PathVariable String name) throws Exception {
        Association assoc = SCTPConfiguration.sctpManagement.getAssociation(name);
        SCTPConfiguration.sctpManagement.startAssociation(name);
        return ResponseEntity.accepted().build();
    }

    /**
     * Return the list of all started servers
     *
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/sctp/servers")
    public ResponseEntity<List<String>> startSctpServer() throws Exception {
        List<String> startedServers = new ArrayList<String>();
        for (Iterator<Server> it = SCTPConfiguration.sctpManagement.getServers().iterator(); it.hasNext();) {
            Server entry = it.next();
            if (entry.isStarted()) startedServers.add(entry.getName());
        }
        return ResponseEntity.ok(startedServers);
    }

    /**
     * Create a SCTP server to handling traffic
     *
     * @param name
     * @param hostAddress
     * @param port
     * @return
     * @throws Exception
     */
    @GetMapping("/sctp/server/{name}")
    public ResponseEntity<String> createSctpServer(@PathVariable String name, @RequestParam String hostAddress, @RequestParam int port)
        throws Exception {
        SCTPConfiguration.sctpManagement.addServer(name, hostAddress, port);
        return ResponseEntity.accepted().body(name);
    }

    @PostMapping("/sctp/server/{name}")
    public ResponseEntity<String> startSctpServer(@PathVariable String name) throws Exception {
        SCTPConfiguration.sctpManagement.startServer(name);
        return ResponseEntity.accepted().body(name);
    }

    @PutMapping("/sctp/server/{name}")
    public ResponseEntity<String> stopSctpServer(@PathVariable String name) throws Exception {
        SCTPConfiguration.sctpManagement.stopServer(name);
        return ResponseEntity.accepted().body(name);
    }

    /**
     * Delete one existing server
     *
     * @param name
     * @return
     * @throws Exception
     */
    @DeleteMapping("/sctp/server/{name}")
    public ResponseEntity<String> dropSctpServer(@PathVariable String name) throws Exception {
        SCTPConfiguration.sctpManagement.removeServer(name);
        return ResponseEntity.accepted().body(name);
    }

    @DeleteMapping("/sctp")
    public ResponseEntity<Void> stopSctp() throws Exception {
        sctpConfiguration.cleanUp();
        return ResponseEntity.ok(null);
    }

    /**
     * Create association with existing SCTP server
     *
     * @param name
     * @param link
     * @param peerAddress
     * @param peerPort
     * @return
     * @throws Exception
     */
    @GetMapping("/sctp/server/{name}/assoc/{link}")
    public ResponseEntity<String> addSctpServerAssoc(
        @PathVariable String name,
        @PathVariable String link,
        @RequestParam String peerAddress,
        @RequestParam int peerPort
    ) throws Exception {
        Association assoc = SCTPConfiguration.sctpManagement.addServerAssociation(peerAddress, peerPort, name, link);
        return ResponseEntity.accepted().body(assoc.getName());
    }

    /**
     * Create a client association
     *
     * @param link
     * @param hostAddress
     * @param hostPort
     * @param peerAddress
     * @param peerPort
     * @return
     * @throws Exception
     */
    @GetMapping("/sctp/assoc/{link}")
    public ResponseEntity<String> addSctpAssoc(
        @PathVariable String link,
        @RequestParam String hostAddress,
        @RequestParam int hostPort,
        @RequestParam String peerAddress,
        @RequestParam int peerPort
    ) throws Exception {
        Association assoc = SCTPConfiguration.sctpManagement.addAssociation(hostAddress, hostPort, peerAddress, peerPort, link);
        return ResponseEntity.accepted().body(assoc.getName());
    }

    @DeleteMapping("/sctp/server/{name}/assoc/{link}")
    public ResponseEntity<String> removeSctpServerAssoc(@PathVariable String name, @PathVariable String link) throws Exception {
        SCTPConfiguration.sctpManagement.removeAssociation(link);
        return ResponseEntity.accepted().body(name);
    }

    /**
     * Should start the Association up
     *
     * <p>FIXME: Need to add an AssociationListenerImpl to this
     *
     * @param name
     * @param link
     * @return
     * @throws Exception
     */
    @PostMapping("/sctp/server/{name}/assoc/{link}")
    public ResponseEntity<String> startSctpServerAssoc(@PathVariable String name, @PathVariable String link) throws Exception {
        SCTPConfiguration.sctpManagement.startAssociation(link);
        return ResponseEntity.accepted().body(name);
    }

    @PutMapping("/sctp/server/{name}/assoc/{link}")
    public ResponseEntity<String> stopSctpServerAssoc(@PathVariable String name, @PathVariable String link) throws Exception {
        SCTPConfiguration.sctpManagement.stopAssociation(link);
        return ResponseEntity.accepted().body(name);
    }
}
