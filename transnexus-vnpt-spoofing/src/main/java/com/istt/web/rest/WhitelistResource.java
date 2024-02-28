package com.istt.web.rest;

import com.istt.domain.Whitelist;
import com.istt.repository.WhitelistRepository;
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

/** REST controller for managing {@link com.istt.domain.Whitelist}. */
@RestController
@RequestMapping("/api")
@Transactional
public class WhitelistResource {

  private final Logger log = LoggerFactory.getLogger(WhitelistResource.class);

  private static final String ENTITY_NAME = "vnptSpoofingWhitelist";

  @Value("${jhipster.clientApp.name}")
  private String applicationName;

  private final WhitelistRepository whitelistRepository;

  public WhitelistResource(WhitelistRepository whitelistRepository) {
    this.whitelistRepository = whitelistRepository;
  }

  /**
   * {@code POST /whitelists} : Create a new whitelist.
   *
   * @param whitelist the whitelist to create.
   * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new
   *     whitelist, or with status {@code 400 (Bad Request)} if the whitelist has already an ID.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PostMapping("/whitelists")
  public ResponseEntity<Whitelist> createWhitelist(@Valid @RequestBody Whitelist whitelist)
      throws URISyntaxException {
    log.debug("REST request to save Whitelist : {}", whitelist);
    if (whitelist.getId() != null) {
      throw new BadRequestAlertException(
          "A new whitelist cannot already have an ID", ENTITY_NAME, "idexists");
    }
    Whitelist result = whitelistRepository.save(whitelist);
    return ResponseEntity.created(new URI("/api/whitelists/" + result.getId()))
        .headers(
            HeaderUtil.createEntityCreationAlert(
                applicationName, true, ENTITY_NAME, result.getId().toString()))
        .body(result);
  }

  /**
   * {@code PUT /whitelists} : Updates an existing whitelist.
   *
   * @param whitelist the whitelist to update.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated
   *     whitelist, or with status {@code 400 (Bad Request)} if the whitelist is not valid, or with
   *     status {@code 500 (Internal Server Error)} if the whitelist couldn't be updated.
   * @throws URISyntaxException if the Location URI syntax is incorrect.
   */
  @PutMapping("/whitelists")
  public ResponseEntity<Whitelist> updateWhitelist(@Valid @RequestBody Whitelist whitelist)
      throws URISyntaxException {
    log.debug("REST request to update Whitelist : {}", whitelist);
    if (whitelist.getId() == null) {
      throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
    }
    Whitelist result = whitelistRepository.save(whitelist);
    return ResponseEntity.ok()
        .headers(
            HeaderUtil.createEntityUpdateAlert(
                applicationName, true, ENTITY_NAME, whitelist.getId().toString()))
        .body(result);
  }

  /**
   * {@code GET /whitelists} : get all the whitelists.
   *
   * @param pageable the pagination information.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of whitelists in
   *     body.
   */
  @GetMapping("/whitelists")
  public ResponseEntity<List<Whitelist>> getAllWhitelists(Predicate predicate, Pageable pageable) {
    log.debug(
        "REST request to get a page of Whitelists, predicate {} pageable {}", predicate, pageable);
    Page<Whitelist> page =
        predicate == null
            ? whitelistRepository.findAll(pageable)
            : whitelistRepository.findAll(predicate, pageable);
    HttpHeaders headers =
        PaginationUtil.generatePaginationHttpHeaders(
            ServletUriComponentsBuilder.fromCurrentRequest(), page);
    return ResponseEntity.ok().headers(headers).body(page.getContent());
  }

  /**
   * {@code GET /whitelists/:id} : get the "id" whitelist.
   *
   * @param id the id of the whitelist to retrieve.
   * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the whitelist, or
   *     with status {@code 404 (Not Found)}.
   */
  @GetMapping("/whitelists/{id}")
  public ResponseEntity<Whitelist> getWhitelist(@PathVariable Long id) {
    log.debug("REST request to get Whitelist : {}", id);
    Optional<Whitelist> whitelist = whitelistRepository.findById(id);
    return ResponseUtil.wrapOrNotFound(whitelist);
  }

  /**
   * {@code DELETE /whitelists/:id} : delete the "id" whitelist.
   *
   * @param id the id of the whitelist to delete.
   * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
   */
  @DeleteMapping("/whitelists/{id}")
  public ResponseEntity<Void> deleteWhitelist(@PathVariable Long id) {
    log.debug("REST request to delete Whitelist : {}", id);
    whitelistRepository.deleteById(id);
    return ResponseEntity.noContent()
        .headers(
            HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
        .build();
  }
}
