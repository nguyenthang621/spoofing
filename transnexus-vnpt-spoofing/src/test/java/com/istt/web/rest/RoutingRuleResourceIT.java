package com.istt.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.istt.VnptSpoofingApp;
import com.istt.domain.RoutingRule;
import com.istt.repository.RoutingRuleRepository;
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

/** Integration tests for the {@link RoutingRuleResource} REST controller. */
@SpringBootTest(classes = VnptSpoofingApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class RoutingRuleResourceIT {

  private static final String DEFAULT_NAME = "AAAAAAAAAA";
  private static final String UPDATED_NAME = "BBBBBBBBBB";

  private static final Integer DEFAULT_STATE = 1;
  private static final Integer UPDATED_STATE = 2;

  private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
  private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

  private static final Long DEFAULT_APREFIX = 1L;
  private static final Long UPDATED_APREFIX = 2L;

  private static final Integer DEFAULT_ALENGTH = 1;
  private static final Integer UPDATED_ALENGTH = 2;

  @Autowired private RoutingRuleRepository routingRuleRepository;

  @Autowired private EntityManager em;

  @Autowired private MockMvc restRoutingRuleMockMvc;

  private RoutingRule routingRule;

  /**
   * Create an entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static RoutingRule createEntity(EntityManager em) {
    RoutingRule routingRule = new RoutingRule();
    routingRule.setName(UPDATED_NAME);
    routingRule.setState(UPDATED_STATE);
    routingRule.setDescription(UPDATED_DESCRIPTION);
    routingRule.setAprefix(UPDATED_APREFIX);
    routingRule.setAlength(UPDATED_ALENGTH);
    return routingRule;
  }
  /**
   * Create an updated entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static RoutingRule createUpdatedEntity(EntityManager em) {
    RoutingRule routingRule = new RoutingRule();
    routingRule.setName(UPDATED_NAME);
    routingRule.setState(UPDATED_STATE);
    routingRule.setDescription(UPDATED_DESCRIPTION);
    routingRule.setAprefix(UPDATED_APREFIX);
    routingRule.setAlength(UPDATED_ALENGTH);
    return routingRule;
  }

  @BeforeEach
  public void initTest() {
    routingRule = createEntity(em);
  }

  @Test
  @Transactional
  public void createRoutingRule() throws Exception {
    int databaseSizeBeforeCreate = routingRuleRepository.findAll().size();

    // Create the RoutingRule
    restRoutingRuleMockMvc
        .perform(
            post("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(routingRule)))
        .andExpect(status().isCreated());

    // Validate the RoutingRule in the database
    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeCreate + 1);
    RoutingRule testRoutingRule = routingRuleList.get(routingRuleList.size() - 1);
    assertThat(testRoutingRule.getName()).isEqualTo(DEFAULT_NAME);
    assertThat(testRoutingRule.getState()).isEqualTo(DEFAULT_STATE);
    assertThat(testRoutingRule.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    assertThat(testRoutingRule.getAprefix()).isEqualTo(DEFAULT_APREFIX);
    assertThat(testRoutingRule.getAlength()).isEqualTo(DEFAULT_ALENGTH);
  }

  @Test
  @Transactional
  public void createRoutingRuleWithExistingId() throws Exception {
    int databaseSizeBeforeCreate = routingRuleRepository.findAll().size();

    // Create the RoutingRule with an existing ID
    routingRule.setId(1L);

    // An entity with an existing ID cannot be created, so this API call must fail
    restRoutingRuleMockMvc
        .perform(
            post("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(routingRule)))
        .andExpect(status().isBadRequest());

    // Validate the RoutingRule in the database
    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeCreate);
  }

  @Test
  @Transactional
  public void checkNameIsRequired() throws Exception {
    int databaseSizeBeforeTest = routingRuleRepository.findAll().size();
    // set the field null
    routingRule.setName(null);

    // Create the RoutingRule, which fails.

    restRoutingRuleMockMvc
        .perform(
            post("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(routingRule)))
        .andExpect(status().isBadRequest());

    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkStateIsRequired() throws Exception {
    int databaseSizeBeforeTest = routingRuleRepository.findAll().size();
    // set the field null
    routingRule.setState(null);

    // Create the RoutingRule, which fails.

    restRoutingRuleMockMvc
        .perform(
            post("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(routingRule)))
        .andExpect(status().isBadRequest());

    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkAprefixIsRequired() throws Exception {
    int databaseSizeBeforeTest = routingRuleRepository.findAll().size();
    // set the field null
    routingRule.setAprefix(null);

    // Create the RoutingRule, which fails.

    restRoutingRuleMockMvc
        .perform(
            post("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(routingRule)))
        .andExpect(status().isBadRequest());

    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void getAllRoutingRules() throws Exception {
    // Initialize the database
    routingRuleRepository.saveAndFlush(routingRule);

    // Get all the routingRuleList
    restRoutingRuleMockMvc
        .perform(get("/api/routing-rules?sort=id,desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[*].id").value(hasItem(routingRule.getId().intValue())))
        .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
        .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
        .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
        .andExpect(jsonPath("$.[*].aprefix").value(hasItem(DEFAULT_APREFIX.intValue())))
        .andExpect(jsonPath("$.[*].alength").value(hasItem(DEFAULT_ALENGTH)));
  }

  @Test
  @Transactional
  public void getRoutingRule() throws Exception {
    // Initialize the database
    routingRuleRepository.saveAndFlush(routingRule);

    // Get the routingRule
    restRoutingRuleMockMvc
        .perform(get("/api/routing-rules/{id}", routingRule.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(routingRule.getId().intValue()))
        .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
        .andExpect(jsonPath("$.state").value(DEFAULT_STATE))
        .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
        .andExpect(jsonPath("$.aprefix").value(DEFAULT_APREFIX.intValue()))
        .andExpect(jsonPath("$.alength").value(DEFAULT_ALENGTH));
  }

  @Test
  @Transactional
  public void getNonExistingRoutingRule() throws Exception {
    // Get the routingRule
    restRoutingRuleMockMvc
        .perform(get("/api/routing-rules/{id}", Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateRoutingRule() throws Exception {
    // Initialize the database
    routingRuleRepository.saveAndFlush(routingRule);

    int databaseSizeBeforeUpdate = routingRuleRepository.findAll().size();

    // Update the routingRule
    RoutingRule updatedRoutingRule = routingRuleRepository.findById(routingRule.getId()).get();
    // Disconnect from session so that the updates on updatedRoutingRule are not directly saved in
    // db
    em.detach(updatedRoutingRule);
    updatedRoutingRule.setName(UPDATED_NAME);
    updatedRoutingRule.setState(UPDATED_STATE);
    updatedRoutingRule.setDescription(UPDATED_DESCRIPTION);
    updatedRoutingRule.setAprefix(UPDATED_APREFIX);
    updatedRoutingRule.setAlength(UPDATED_ALENGTH);

    restRoutingRuleMockMvc
        .perform(
            put("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedRoutingRule)))
        .andExpect(status().isOk());

    // Validate the RoutingRule in the database
    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeUpdate);
    RoutingRule testRoutingRule = routingRuleList.get(routingRuleList.size() - 1);
    assertThat(testRoutingRule.getName()).isEqualTo(UPDATED_NAME);
    assertThat(testRoutingRule.getState()).isEqualTo(UPDATED_STATE);
    assertThat(testRoutingRule.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    assertThat(testRoutingRule.getAprefix()).isEqualTo(UPDATED_APREFIX);
    assertThat(testRoutingRule.getAlength()).isEqualTo(UPDATED_ALENGTH);
  }

  @Test
  @Transactional
  public void updateNonExistingRoutingRule() throws Exception {
    int databaseSizeBeforeUpdate = routingRuleRepository.findAll().size();

    // Create the RoutingRule

    // If the entity doesn't have an ID, it will throw BadRequestAlertException
    restRoutingRuleMockMvc
        .perform(
            put("/api/routing-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(routingRule)))
        .andExpect(status().isBadRequest());

    // Validate the RoutingRule in the database
    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeUpdate);
  }

  @Test
  @Transactional
  public void deleteRoutingRule() throws Exception {
    // Initialize the database
    routingRuleRepository.saveAndFlush(routingRule);

    int databaseSizeBeforeDelete = routingRuleRepository.findAll().size();

    // Delete the routingRule
    restRoutingRuleMockMvc
        .perform(
            delete("/api/routing-rules/{id}", routingRule.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Validate the database contains one less item
    List<RoutingRule> routingRuleList = routingRuleRepository.findAll();
    assertThat(routingRuleList).hasSize(databaseSizeBeforeDelete - 1);
  }
}
