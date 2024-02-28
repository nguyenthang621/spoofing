package com.istt.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.istt.VnptSpoofingApp;
import com.istt.domain.Blacklist;
import com.istt.repository.BlacklistRepository;
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

/** Integration tests for the {@link BlacklistResource} REST controller. */
@SpringBootTest(classes = VnptSpoofingApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class BlacklistResourceIT {

  private static final Integer DEFAULT_STATE = 1;
  private static final Integer UPDATED_STATE = 2;

  private static final Long DEFAULT_PREFIX = 1L;
  private static final Long UPDATED_PREFIX = 2L;

  @Autowired private BlacklistRepository blacklistRepository;

  @Autowired private EntityManager em;

  @Autowired private MockMvc restBlacklistMockMvc;

  private Blacklist blacklist;

  /**
   * Create an entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static Blacklist createEntity(EntityManager em) {
    Blacklist blacklist = new Blacklist().state(DEFAULT_STATE).prefix(DEFAULT_PREFIX);
    return blacklist;
  }
  /**
   * Create an updated entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static Blacklist createUpdatedEntity(EntityManager em) {
    Blacklist blacklist = new Blacklist().state(UPDATED_STATE).prefix(UPDATED_PREFIX);
    return blacklist;
  }

  @BeforeEach
  public void initTest() {
    blacklist = createEntity(em);
  }

  @Test
  @Transactional
  public void createBlacklist() throws Exception {
    int databaseSizeBeforeCreate = blacklistRepository.findAll().size();

    // Create the Blacklist
    restBlacklistMockMvc
        .perform(
            post("/api/blacklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(blacklist)))
        .andExpect(status().isCreated());

    // Validate the Blacklist in the database
    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeCreate + 1);
    Blacklist testBlacklist = blacklistList.get(blacklistList.size() - 1);
    assertThat(testBlacklist.getState()).isEqualTo(DEFAULT_STATE);
    assertThat(testBlacklist.getPrefix()).isEqualTo(DEFAULT_PREFIX);
  }

  @Test
  @Transactional
  public void createBlacklistWithExistingId() throws Exception {
    int databaseSizeBeforeCreate = blacklistRepository.findAll().size();

    // Create the Blacklist with an existing ID
    blacklist.setId(1L);

    // An entity with an existing ID cannot be created, so this API call must fail
    restBlacklistMockMvc
        .perform(
            post("/api/blacklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(blacklist)))
        .andExpect(status().isBadRequest());

    // Validate the Blacklist in the database
    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeCreate);
  }

  @Test
  @Transactional
  public void checkStateIsRequired() throws Exception {
    int databaseSizeBeforeTest = blacklistRepository.findAll().size();
    // set the field null
    blacklist.setState(null);

    // Create the Blacklist, which fails.

    restBlacklistMockMvc
        .perform(
            post("/api/blacklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(blacklist)))
        .andExpect(status().isBadRequest());

    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkPrefixIsRequired() throws Exception {
    int databaseSizeBeforeTest = blacklistRepository.findAll().size();
    // set the field null
    blacklist.setPrefix(null);

    // Create the Blacklist, which fails.

    restBlacklistMockMvc
        .perform(
            post("/api/blacklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(blacklist)))
        .andExpect(status().isBadRequest());

    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void getAllBlacklists() throws Exception {
    // Initialize the database
    blacklistRepository.saveAndFlush(blacklist);

    // Get all the blacklistList
    restBlacklistMockMvc
        .perform(get("/api/blacklists?sort=id,desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[*].id").value(hasItem(blacklist.getId().intValue())))
        .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
        .andExpect(jsonPath("$.[*].prefix").value(hasItem(DEFAULT_PREFIX.intValue())));
  }

  @Test
  @Transactional
  public void getBlacklist() throws Exception {
    // Initialize the database
    blacklistRepository.saveAndFlush(blacklist);

    // Get the blacklist
    restBlacklistMockMvc
        .perform(get("/api/blacklists/{id}", blacklist.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(blacklist.getId().intValue()))
        .andExpect(jsonPath("$.state").value(DEFAULT_STATE))
        .andExpect(jsonPath("$.prefix").value(DEFAULT_PREFIX.intValue()));
  }

  @Test
  @Transactional
  public void getNonExistingBlacklist() throws Exception {
    // Get the blacklist
    restBlacklistMockMvc
        .perform(get("/api/blacklists/{id}", Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateBlacklist() throws Exception {
    // Initialize the database
    blacklistRepository.saveAndFlush(blacklist);

    int databaseSizeBeforeUpdate = blacklistRepository.findAll().size();

    // Update the blacklist
    Blacklist updatedBlacklist = blacklistRepository.findById(blacklist.getId()).get();
    // Disconnect from session so that the updates on updatedBlacklist are not directly saved in db
    em.detach(updatedBlacklist);
    updatedBlacklist.state(UPDATED_STATE).prefix(UPDATED_PREFIX);

    restBlacklistMockMvc
        .perform(
            put("/api/blacklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedBlacklist)))
        .andExpect(status().isOk());

    // Validate the Blacklist in the database
    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeUpdate);
    Blacklist testBlacklist = blacklistList.get(blacklistList.size() - 1);
    assertThat(testBlacklist.getState()).isEqualTo(UPDATED_STATE);
    assertThat(testBlacklist.getPrefix()).isEqualTo(UPDATED_PREFIX);
  }

  @Test
  @Transactional
  public void updateNonExistingBlacklist() throws Exception {
    int databaseSizeBeforeUpdate = blacklistRepository.findAll().size();

    // Create the Blacklist

    // If the entity doesn't have an ID, it will throw BadRequestAlertException
    restBlacklistMockMvc
        .perform(
            put("/api/blacklists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(blacklist)))
        .andExpect(status().isBadRequest());

    // Validate the Blacklist in the database
    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeUpdate);
  }

  @Test
  @Transactional
  public void deleteBlacklist() throws Exception {
    // Initialize the database
    blacklistRepository.saveAndFlush(blacklist);

    int databaseSizeBeforeDelete = blacklistRepository.findAll().size();

    // Delete the blacklist
    restBlacklistMockMvc
        .perform(
            delete("/api/blacklists/{id}", blacklist.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Validate the database contains one less item
    List<Blacklist> blacklistList = blacklistRepository.findAll();
    assertThat(blacklistList).hasSize(databaseSizeBeforeDelete - 1);
  }
}
