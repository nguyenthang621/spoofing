package com.istt.web.rest;

import com.istt.domain.RoutingRule;
import com.istt.repository.RoutingRuleRepository;
import com.istt.web.rest.errors.BadRequestAlertException;
import com.querydsl.core.types.Predicate;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** REST controller for managing {@link com.istt.domain.RoutingRule}. */
@RestController
@RequestMapping("/api")
@Transactional
public class RoutingRuleResource {

  private final Logger log = LoggerFactory.getLogger(RoutingRuleResource.class);

  private static final String ENTITY_NAME = "vnptSpoofingRoutingRule";

  @Value("${jhipster.clientApp.name}")
  private String applicationName;

  private final RoutingRuleRepository routingRuleRepository;

  public RoutingRuleResource(RoutingRuleRepository routingRuleRepository) {
    this.routingRuleRepository = routingRuleRepository;
  }

  /**
   * {@code POST /routing-rules} : Create a new routingRule.
   *
   * @param routingRule the routingRule to create.
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new
   *     routingRule, or with status {@code 400 (Bad Request)} if the routingRule has already an ID.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PostMapping("/routing-rules")
  public ResponseEntity<RoutingRule> createRoutingRule(@Valid @RequestBody RoutingRule routingRule)
      throws URISyntaxException {
    log.debug("REST request to save RoutingRule : {}", routingRule);
    if (routingRule.getId() != null) {
      throw new BadRequestAlertException(
          "A new routingRule cannot already have an ID", ENTITY_NAME, "idexists");
    }
    RoutingRule result = routingRuleRepository.save(routingRule);
    return ResponseEntity.created(new URI("/api/routing-rules/" + result.getId()))
        .headers(
            HeaderUtil.createEntityCreationAlert(
                applicationName, true, ENTITY_NAME, result.getId().toString()))
        .body(result);
  }

  /**
   * {@code PUT /routing-rules} : Updates an existing routingRule.
   *
   * @param routingRule the routingRule to update.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated
   *     routingRule, or with status {@code 400 (Bad Request)} if the routingRule is not valid, or
   *     with status {@code 500 (Internal Server Error)} if the routingRule couldn't be updated.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PutMapping("/routing-rules")
  public ResponseEntity<RoutingRule> updateRoutingRule(@Valid @RequestBody RoutingRule routingRule)
      throws URISyntaxException {
    log.debug("REST request to update RoutingRule : {}", routingRule);
    if (routingRule.getId() == null) {
      throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    }
    RoutingRule result = routingRuleRepository.save(routingRule);
    return ResponseEntity.ok()
        .headers(
            HeaderUtil.createEntityUpdateAlert(
                applicationName, true, ENTITY_NAME, routingRule.getId().toString()))
        .body(result);
  }

  /**
   * {@code GET /routing-rules} : get all the routingRules.
   *
   * @param pageable the pagination information.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of routingRules in
   *     body.
   */
  @GetMapping("/routing-rules")
  public ResponseEntity<List<RoutingRule>> getAllRoutingRules(
      Predicate predicate, Pageable pageable) {
    log.debug(
        "REST request to get a page of RoutingRules, predicate {} pageable {}",
        predicate,
        pageable);
    Page<RoutingRule> page =
        predicate == null
            ? routingRuleRepository.findAll(pageable)
            : routingRuleRepository.findAll(predicate, pageable);
    HttpHeaders headers =
        PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
    return ResponseEntity.ok().headers(headers).body(page.getContent());
  }

  /**
   * {@code GET /routing-rules/:id} : get the "id" routingRule.
   *
   * @param id the id of the routingRule to retrieve.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the routingRule,
   *     or with status {@code 404 (Not Found)}.
   */
  @GetMapping("/routing-rules/{id}")
  public ResponseEntity<RoutingRule> getRoutingRule(@PathVariable Long id) {
    log.debug("REST request to get RoutingRule : {}", id);
    Optional<RoutingRule> routingRule = routingRuleRepository.findById(id);
    return ResponseUtil.wrapOrNotFound(routingRule);
  }

  /**
   * {@code DELETE /routing-rules/:id} : delete the "id" routingRule.
   *
   * @param id the id of the routingRule to delete.
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
   */
  @DeleteMapping("/routing-rules/{id}")
  public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
    log.debug("REST request to delete RoutingRule : {}", id);
    routingRuleRepository.deleteById(id);
    return ResponseEntity.noContent()
        .headers(
            HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
        .build();
  }
}
