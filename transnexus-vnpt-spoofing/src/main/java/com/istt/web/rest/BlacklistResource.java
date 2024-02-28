package com.istt.web.rest;

import com.istt.domain.Blacklist;
import com.istt.repository.BlacklistRepository;
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

/** REST controller for managing {@link com.istt.domain.Blacklist}. */
@RestController
@RequestMapping("/api")
@Transactional
public class BlacklistResource {

  private final Logger log = LoggerFactory.getLogger(BlacklistResource.class);

  private static final String ENTITY_NAME = "vnptSpoofingBlacklist";

  @Value("${jhipster.clientApp.name}")
  private String applicationName;

  private final BlacklistRepository blacklistRepository;

  public BlacklistResource(BlacklistRepository blacklistRepository) {
    this.blacklistRepository = blacklistRepository;
  }

  /**
   * {@code POST /blacklists} : Create a new blacklist.
   *
   * @param blacklist the blacklist to create.
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new
   *     blacklist, or with status {@code 400 (Bad Request)} if the blacklist has already an ID.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PostMapping("/blacklists")
  public ResponseEntity<Blacklist> createBlacklist(@Valid @RequestBody Blacklist blacklist)
      throws URISyntaxException {
    log.debug("REST request to save Blacklist : {}", blacklist);
    if (blacklist.getId() != null) {
      throw new BadRequestAlertException(
          "A new blacklist cannot already have an ID", ENTITY_NAME, "idexists");
    }
    Blacklist result = blacklistRepository.save(blacklist);
    return ResponseEntity.created(new URI("/api/blacklists/" + result.getId()))
        .headers(
            HeaderUtil.createEntityCreationAlert(
                applicationName, true, ENTITY_NAME, result.getId().toString()))
        .body(result);
  }

  /**
   * {@code PUT /blacklists} : Updates an existing blacklist.
   *
   * @param blacklist the blacklist to update.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated
   *     blacklist, or with status {@code 400 (Bad Request)} if the blacklist is not valid, or with
   *     status {@code 500 (Internal Server Error)} if the blacklist couldn't be updated.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PutMapping("/blacklists")
  public ResponseEntity<Blacklist> updateBlacklist(@Valid @RequestBody Blacklist blacklist)
      throws URISyntaxException {
    log.debug("REST request to update Blacklist : {}", blacklist);
    if (blacklist.getId() == null) {
      throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    }
    Blacklist result = blacklistRepository.save(blacklist);
    return ResponseEntity.ok()
        .headers(
            HeaderUtil.createEntityUpdateAlert(
                applicationName, true, ENTITY_NAME, blacklist.getId().toString()))
        .body(result);
  }

  /**
   * {@code GET /blacklists} : get all the blacklists.
   *
   * @param pageable the pagination information.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of blacklists in
   *     body.
   */
  @GetMapping("/blacklists")
  public ResponseEntity<List<Blacklist>> getAllBlacklists(Predicate predicate, Pageable pageable) {
    log.debug(
        "REST request to get a page of Blacklists, predicate: {} pageable {}", predicate, pageable);
    Page<Blacklist> page =
        predicate == null
            ? blacklistRepository.findAll(pageable)
            : blacklistRepository.findAll(predicate, pageable);
    HttpHeaders headers =
        PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
    return ResponseEntity.ok().headers(headers).body(page.getContent());
  }

  /**
   * {@code GET /blacklists/:id} : get the "id" blacklist.
   *
   * @param id the id of the blacklist to retrieve.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the blacklist, or
   *     with status {@code 404 (Not Found)}.
   */
  @GetMapping("/blacklists/{id}")
  public ResponseEntity<Blacklist> getBlacklist(@PathVariable Long id) {
    log.debug("REST request to get Blacklist : {}", id);
    Optional<Blacklist> blacklist = blacklistRepository.findById(id);
    return ResponseUtil.wrapOrNotFound(blacklist);
  }

  /**
   * {@code DELETE /blacklists/:id} : delete the "id" blacklist.
   *
   * @param id the id of the blacklist to delete.
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
   */
  @DeleteMapping("/blacklists/{id}")
  public ResponseEntity<Void> deleteBlacklist(@PathVariable Long id) {
    log.debug("REST request to delete Blacklist : {}", id);
    blacklistRepository.deleteById(id);
    return ResponseEntity.noContent()
        .headers(
            HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
        .build();
  }
}
