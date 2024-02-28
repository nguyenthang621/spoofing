package com.istt.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.istt.VnptSpoofingApp;
import com.istt.domain.Whitelist;
import com.istt.repository.WhitelistRepository;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for the {@link WhitelistResource} REST controller. */
@SpringBootTest(classes = VnptSpoofingApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class WhitelistResourceIT {

  private static final Integer DEFAULT_STATE = 1;
  private static final Integer UPDATED_STATE = 2;

  private static final Long DEFAULT_PREFIX = 1L;
  private static final Long UPDATED_PREFIX = 2L;

  @Autowired private WhitelistRepository whitelistRepository;

  @Autowired private EntityManager em;

  @Autowired private MockMvc restWhitelistMockMvc;

  private Whitelist whitelist;

  /**
   * Create an entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static Whitelist createEntity(EntityManager em) {
    Whitelist whitelist = new Whitelist();
    whitelist.setState(UPDATED_STATE);
    whitelist.setPrefix(UPDATED_PREFIX);
    return whitelist;
  }
  /**
   * Create an updated entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static Whitelist createUpdatedEntity(EntityManager em) {
    Whitelist whitelist = new Whitelist();
    whitelist.setState(UPDATED_STATE);
    whitelist.setPrefix(UPDATED_PREFIX);
    return whitelist;
  }

  @BeforeEach
  public void initTest() {
    whitelist = createEntity(em);
  }

  @Test
  @Transactional
  public void createWhitelist() throws Exception {
    int databaseSizeBeforeCreate = whitelistRepository.findAll().size();

    // Create the Whitelist
    restWhitelistMockMvc
        .perform(
            post("/api/whitelists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(whitelist)))
        .andExpect(status().isCreated());

    // Validate the Whitelist in the database
    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeCreate + 1);
    Whitelist testWhitelist = whitelistList.get(whitelistList.size() - 1);
    assertThat(testWhitelist.getState()).isEqualTo(DEFAULT_STATE);
    assertThat(testWhitelist.getPrefix()).isEqualTo(DEFAULT_PREFIX);
  }

  @Test
  @Transactional
  public void createWhitelistWithExistingId() throws Exception {
    int databaseSizeBeforeCreate = whitelistRepository.findAll().size();

    // Create the Whitelist with an existing ID
    whitelist.setId(1L);

    // An entity with an existing ID cannot be created, so this API call must fail
    restWhitelistMockMvc
        .perform(
            post("/api/whitelists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(whitelist)))
        .andExpect(status().isBadRequest());

    // Validate the Whitelist in the database
    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeCreate);
  }

  @Test
  @Transactional
  public void checkStateIsRequired() throws Exception {
    int databaseSizeBeforeTest = whitelistRepository.findAll().size();
    // set the field null
    whitelist.setState(null);

    // Create the Whitelist, which fails.

    restWhitelistMockMvc
        .perform(
            post("/api/whitelists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(whitelist)))
        .andExpect(status().isBadRequest());

    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkPrefixIsRequired() throws Exception {
    int databaseSizeBeforeTest = whitelistRepository.findAll().size();
    // set the field null
    whitelist.setPrefix(null);

    // Create the Whitelist, which fails.

    restWhitelistMockMvc
        .perform(
            post("/api/whitelists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(whitelist)))
        .andExpect(status().isBadRequest());

    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void getAllWhitelists() throws Exception {
    // Initialize the database
    whitelistRepository.saveAndFlush(whitelist);

    // Get all the whitelistList
    restWhitelistMockMvc
        .perform(get("/api/whitelists?sort=id,desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[*].id").value(hasItem(whitelist.getId().intValue())))
        .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
        .andExpect(jsonPath("$.[*].prefix").value(hasItem(DEFAULT_PREFIX.intValue())));
  }

  @Test
  @Transactional
  public void getWhitelist() throws Exception {
    // Initialize the database
    whitelistRepository.saveAndFlush(whitelist);

    // Get the whitelist
    restWhitelistMockMvc
        .perform(get("/api/whitelists/{id}", whitelist.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(whitelist.getId().intValue()))
        .andExpect(jsonPath("$.state").value(DEFAULT_STATE))
        .andExpect(jsonPath("$.prefix").value(DEFAULT_PREFIX.intValue()));
  }

  @Test
  @Transactional
  public void getNonExistingWhitelist() throws Exception {
    // Get the whitelist
    restWhitelistMockMvc
        .perform(get("/api/whitelists/{id}", Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateWhitelist() throws Exception {
    // Initialize the database
    whitelistRepository.saveAndFlush(whitelist);

    int databaseSizeBeforeUpdate = whitelistRepository.findAll().size();

    // Update the whitelist
    Whitelist updatedWhitelist = whitelistRepository.findById(whitelist.getId()).get();
    // Disconnect from session so that the updates on updatedWhitelist are not directly saved in db
    em.detach(updatedWhitelist);
    updatedWhitelist.setState(UPDATED_STATE);
    updatedWhitelist.setPrefix(UPDATED_PREFIX);

    restWhitelistMockMvc
        .perform(
            put("/api/whitelists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedWhitelist)))
        .andExpect(status().isOk());

    // Validate the Whitelist in the database
    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeUpdate);
    Whitelist testWhitelist = whitelistList.get(whitelistList.size() - 1);
    assertThat(testWhitelist.getState()).isEqualTo(UPDATED_STATE);
    assertThat(testWhitelist.getPrefix()).isEqualTo(UPDATED_PREFIX);
  }

  @Test
  @Transactional
  public void updateNonExistingWhitelist() throws Exception {
    int databaseSizeBeforeUpdate = whitelistRepository.findAll().size();

    // Create the Whitelist

    // If the entity doesn't have an ID, it will throw BadRequestAlertException
    restWhitelistMockMvc
        .perform(
            put("/api/whitelists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(whitelist)))
        .andExpect(status().isBadRequest());

    // Validate the Whitelist in the database
    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeUpdate);
  }

  @Test
  @Transactional
  public void deleteWhitelist() throws Exception {
    // Initialize the database
    whitelistRepository.saveAndFlush(whitelist);

    int databaseSizeBeforeDelete = whitelistRepository.findAll().size();

    // Delete the whitelist
    restWhitelistMockMvc
        .perform(
            delete("/api/whitelists/{id}", whitelist.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Validate the database contains one less item
    List<Whitelist> whitelistList = whitelistRepository.findAll();
    assertThat(whitelistList).hasSize(databaseSizeBeforeDelete - 1);
  }
}
