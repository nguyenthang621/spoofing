package com.istt.web.rest;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.istt.domain.ConnectorLog;
import com.istt.repository.ConnectorLogRepository;
import com.querydsl.core.types.Predicate;

import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/** REST controller for managing {@link com.istt.domain.ConnectorLog}. */
@RestController
@RequestMapping("/api")
@Transactional
public class ConnectorLogResource {

  private final Logger log = LoggerFactory.getLogger(ConnectorLogResource.class);

  @Value("${jhipster.clientApp.name}")
  private String applicationName;

  private final ConnectorLogRepository ConnectorLogRepository;

  public ConnectorLogResource(ConnectorLogRepository ConnectorLogRepository) {
    this.ConnectorLogRepository = ConnectorLogRepository;
  }

  /**
   * {@code GET /connector-logs} : get all the ConnectorLogs.
   *
   * @param pageable the pagination information.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ConnectorLogs in
   *     body.
   */
  @GetMapping("/connector-logs")
  public ResponseEntity<List<ConnectorLog>> getAllConnectorLogs(Predicate predicate, Pageable pageable) {
    log.debug(
        "REST request to get a page of ConnectorLogs, predicate {} pageable {}", predicate, pageable);
    Page<ConnectorLog> page =
        predicate == null
            ? ConnectorLogRepository.findAll(pageable)
            : ConnectorLogRepository.findAll(predicate, pageable);
    HttpHeaders headers =
        PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
    return ResponseEntity.ok().headers(headers).body(page.getContent());
  }

  /**
   * {@code GET /connector-logs/:id} : get the "id" ConnectorLog.
   *
   * @param id the id of the ConnectorLog to retrieve.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ConnectorLog, or
   *     with status {@code 404 (Not Found)}.
   */
  @GetMapping("/connector-logs/{id}")
  public ResponseEntity<ConnectorLog> getConnectorLog(@PathVariable Long id) {
    log.debug("REST request to get ConnectorLog : {}", id);
    Optional<ConnectorLog> ConnectorLog = ConnectorLogRepository.findById(id);
    return ResponseUtil.wrapOrNotFound(ConnectorLog);
  }

}
