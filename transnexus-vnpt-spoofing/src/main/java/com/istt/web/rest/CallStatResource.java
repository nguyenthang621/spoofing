package com.istt.web.rest;

import com.istt.repository.CallLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for managing {@link com.ft.domain.Cdr}. */
@RestController
@RequestMapping("/api")
@Transactional
public class CallStatResource {

  private final Logger log = LoggerFactory.getLogger(CallStatResource.class);

  @Value("${jhipster.clientApp.name}")
  private String applicationName;

  private final CallLogRepository callLogRepository;

  public CallStatResource(CallLogRepository callLogRepository) {
    this.callLogRepository = callLogRepository;
  }

  @GetMapping("/daily-stats")
  public ResponseEntity<List<Map<String, Object>>> dailyStatsByStateIn(
      @RequestParam LocalDate from, @RequestParam(required = false) LocalDate to) {
    to = Optional.ofNullable(to).orElse(LocalDate.now());
    log.debug("dailyStatsByStateIn: {} -> {}", from, to);
    return ResponseEntity.ok()
        .body(
            callLogRepository.dailyStatsByStateIn(
                from.atStartOfDay(), to.plusDays(1L).atStartOfDay()));
  }

  @GetMapping("/statistics/states")
  public ResponseEntity<List<Integer>> getDistinctState() {
    return ResponseEntity.ok().body(callLogRepository.findDistinctState());
  }

  @GetMapping("/statistics/error-codes")
  public ResponseEntity<List<Integer>> getDistinctErrorCodes() {
    return ResponseEntity.ok().body(callLogRepository.findDistinctErrorCode());
  }

  @GetMapping("/daily-report")
  public ResponseEntity<List<Map<String, Object>>> dailyReport(
      @RequestParam LocalDateTime from, @RequestParam LocalDateTime to) {
    log.debug("dailyReport: {} -> {}", from, to);
    return ResponseEntity.ok().body(callLogRepository.dailyStatsByStateIn(from, to));
  }
}
