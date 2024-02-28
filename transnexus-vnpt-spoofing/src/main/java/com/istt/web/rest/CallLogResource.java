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

import com.istt.domain.CallLog;
import com.istt.repository.CallLogRepository;
import com.querydsl.core.types.Predicate;

import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/** REST controller for managing {@link com.istt.domain.CallLog}. */
@RestController
@RequestMapping("/api")
@Transactional
public class CallLogResource {

  private final Logger log = LoggerFactory.getLogger(CallLogResource.class);

  @Value("${jhipster.clientApp.name}")
  private String applicationName;

  private final CallLogRepository callLogRepository;

  public CallLogResource(CallLogRepository callLogRepository) {
    this.callLogRepository = callLogRepository;
  }

  /**
   * {@code GET /call-logs} : get all the callLogs.
   *
   * @param pageable the pagination information.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of callLogs in
   *     body.
   */
  @GetMapping("/call-logs")
  public ResponseEntity<List<CallLog>> getAllCallLogs(Predicate predicate, Pageable pageable) {
    log.debug(
        "REST request to get a page of CallLogs, predicate {} pageable {}", predicate, pageable);
    Page<CallLog> page =
        predicate == null
            ? callLogRepository.findAll(pageable)
            : callLogRepository.findAll(predicate, pageable);
    HttpHeaders headers =
        PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
    return ResponseEntity.ok().headers(headers).body(page.getContent());
  }

  /**
   * {@code GET /call-logs/:id} : get the "id" callLog.
   *
   * @param id the id of the callLog to retrieve.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the callLog, or
   *     with status {@code 404 (Not Found)}.
   */
  @GetMapping("/call-logs/{id}")
  public ResponseEntity<CallLog> getCallLog(@PathVariable Long id) {
    log.debug("REST request to get CallLog : {}", id);
    Optional<CallLog> callLog = callLogRepository.findById(id);
    return ResponseUtil.wrapOrNotFound(callLog);
  }
}
