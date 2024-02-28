package com.istt.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.istt.VnptSpoofingApp;
import com.istt.domain.CallLog;
import com.istt.repository.CallLogRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

/** Integration tests for the {@link CallLogResource} REST controller. */
@SpringBootTest(classes = VnptSpoofingApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CallLogResourceIT {

  private static final String DEFAULT_CALLING = "AAAAAAAAAA";
  private static final String UPDATED_CALLING = "BBBBBBBBBB";

  private static final String DEFAULT_CALLED = "AAAAAAAAAA";
  private static final String UPDATED_CALLED = "BBBBBBBBBB";

  private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
  private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  private static final Instant DEFAULT_REQUEST_AT = Instant.ofEpochMilli(0L);
  private static final Instant UPDATED_REQUEST_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  private static final Instant DEFAULT_RESPONSE_AT = Instant.ofEpochMilli(0L);
  private static final Instant UPDATED_RESPONSE_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  private static final Integer DEFAULT_STATE = 1;
  private static final Integer UPDATED_STATE = 2;

  private static final String DEFAULT_ROUTE = "AAAAAAAAAA";
  private static final String UPDATED_ROUTE = "BBBBBBBBBB";

  private static final Long DEFAULT_ROUTE_PREFIX = 1L;
  private static final Long UPDATED_ROUTE_PREFIX = 2L;

  private static final Integer DEFAULT_ROUTE_LENGTH = 1;
  private static final Integer UPDATED_ROUTE_LENGTH = 2;

  private static final Boolean DEFAULT_DRY_RUN = false;
  private static final Boolean UPDATED_DRY_RUN = true;

  private static final Boolean DEFAULT_WHITELISTED = false;
  private static final Boolean UPDATED_WHITELISTED = true;

  private static final Boolean DEFAULT_BLACKLISTED = false;
  private static final Boolean UPDATED_BLACKLISTED = true;

  private static final Integer DEFAULT_ERROR_CODE = 1;
  private static final Integer UPDATED_ERROR_CODE = 2;

  private static final String DEFAULT_ERROR_DESC = "AAAAAAAAAA";
  private static final String UPDATED_ERROR_DESC = "BBBBBBBBBB";

  private static final String DEFAULT_VLR = "AAAAAAAAAA";
  private static final String UPDATED_VLR = "BBBBBBBBBB";

  private static final Instant DEFAULT_SRI_AT = Instant.ofEpochMilli(0L);
  private static final Instant UPDATED_SRI_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  private static final Instant DEFAULT_SRI_RESP_AT = Instant.ofEpochMilli(0L);
  private static final Instant UPDATED_SRI_RESP_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  @Autowired private CallLogRepository callLogRepository;

  @Autowired private EntityManager em;

  @Autowired private MockMvc restCallLogMockMvc;

  private CallLog callLog;

  /**
   * Create an entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static CallLog createEntity(EntityManager em) {
    CallLog callLog = new CallLog();
    callLog.setCalling(DEFAULT_CALLING);
    callLog.setCalled(DEFAULT_CALLED);
    callLog.setCreatedAt(DEFAULT_CREATED_AT);
    callLog.setRequestAt(DEFAULT_REQUEST_AT);
    callLog.setResponseAt(DEFAULT_RESPONSE_AT);
    callLog.setState(DEFAULT_STATE);
    callLog.setRoute(DEFAULT_ROUTE);
    callLog.setRoutePrefix(DEFAULT_ROUTE_PREFIX);
    callLog.setRouteLength(DEFAULT_ROUTE_LENGTH);
    callLog.setDryRun(DEFAULT_DRY_RUN);
    callLog.setWhitelisted(DEFAULT_WHITELISTED);
    callLog.setBlacklisted(DEFAULT_BLACKLISTED);
    callLog.setErrorCode(DEFAULT_ERROR_CODE);
    callLog.setErrorDesc(DEFAULT_ERROR_DESC);
    callLog.setVlr(DEFAULT_VLR);
    callLog.setSriAt(DEFAULT_SRI_AT);
    callLog.setSriRespAt(DEFAULT_SRI_RESP_AT);
    return callLog;
  }
  /**
   * Create an updated entity for this test.
   *
   * <p>This is a static method, as tests for other entities might also need it, if they test an
   * entity which requires the current entity.
   */
  public static CallLog createUpdatedEntity(EntityManager em) {
    CallLog callLog = new CallLog();
    callLog.setCalling(DEFAULT_CALLING);
    callLog.setCalled(DEFAULT_CALLED);
    callLog.setCreatedAt(DEFAULT_CREATED_AT);
    callLog.setRequestAt(DEFAULT_REQUEST_AT);
    callLog.setResponseAt(DEFAULT_RESPONSE_AT);
    callLog.setState(DEFAULT_STATE);
    callLog.setRoute(DEFAULT_ROUTE);
    callLog.setRoutePrefix(DEFAULT_ROUTE_PREFIX);
    callLog.setRouteLength(DEFAULT_ROUTE_LENGTH);
    callLog.setDryRun(DEFAULT_DRY_RUN);
    callLog.setWhitelisted(DEFAULT_WHITELISTED);
    callLog.setBlacklisted(DEFAULT_BLACKLISTED);
    callLog.setErrorCode(DEFAULT_ERROR_CODE);
    callLog.setErrorDesc(DEFAULT_ERROR_DESC);
    callLog.setVlr(DEFAULT_VLR);
    callLog.setSriAt(DEFAULT_SRI_AT);
    callLog.setSriRespAt(DEFAULT_SRI_RESP_AT);
    return callLog;
  }

  @BeforeEach
  public void initTest() {
    callLog = createEntity(em);
  }

  @Test
  @Transactional
  public void createCallLog() throws Exception {
    int databaseSizeBeforeCreate = callLogRepository.findAll().size();

    // Create the CallLog
    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isCreated());

    // Validate the CallLog in the database
    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeCreate + 1);
    CallLog testCallLog = callLogList.get(callLogList.size() - 1);
    assertThat(testCallLog.getCalling()).isEqualTo(DEFAULT_CALLING);
    assertThat(testCallLog.getCalled()).isEqualTo(DEFAULT_CALLED);
    assertThat(testCallLog.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
    assertThat(testCallLog.getRequestAt()).isEqualTo(DEFAULT_REQUEST_AT);
    assertThat(testCallLog.getResponseAt()).isEqualTo(DEFAULT_RESPONSE_AT);
    assertThat(testCallLog.getState()).isEqualTo(DEFAULT_STATE);
    assertThat(testCallLog.getRoute()).isEqualTo(DEFAULT_ROUTE);
    assertThat(testCallLog.getRoutePrefix()).isEqualTo(DEFAULT_ROUTE_PREFIX);
    assertThat(testCallLog.getRouteLength()).isEqualTo(DEFAULT_ROUTE_LENGTH);
    assertThat(testCallLog.getDryRun()).isEqualTo(DEFAULT_DRY_RUN);
    assertThat(testCallLog.getWhitelisted()).isEqualTo(DEFAULT_WHITELISTED);
    assertThat(testCallLog.getBlacklisted()).isEqualTo(DEFAULT_BLACKLISTED);
    assertThat(testCallLog.getErrorCode()).isEqualTo(DEFAULT_ERROR_CODE);
    assertThat(testCallLog.getErrorDesc()).isEqualTo(DEFAULT_ERROR_DESC);
    assertThat(testCallLog.getVlr()).isEqualTo(DEFAULT_VLR);
    assertThat(testCallLog.getSriAt()).isEqualTo(DEFAULT_SRI_AT);
    assertThat(testCallLog.getSriRespAt()).isEqualTo(DEFAULT_SRI_RESP_AT);
  }

  @Test
  @Transactional
  public void createCallLogWithExistingId() throws Exception {
    int databaseSizeBeforeCreate = callLogRepository.findAll().size();

    // Create the CallLog with an existing ID
    callLog.setId(1L);

    // An entity with an existing ID cannot be created, so this API call must fail
    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    // Validate the CallLog in the database
    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeCreate);
  }

  @Test
  @Transactional
  public void checkCallingIsRequired() throws Exception {
    int databaseSizeBeforeTest = callLogRepository.findAll().size();
    // set the field null
    callLog.setCalling(null);

    // Create the CallLog, which fails.

    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkCalledIsRequired() throws Exception {
    int databaseSizeBeforeTest = callLogRepository.findAll().size();
    // set the field null
    callLog.setCalled(null);

    // Create the CallLog, which fails.

    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkCreatedAtIsRequired() throws Exception {
    int databaseSizeBeforeTest = callLogRepository.findAll().size();
    // set the field null
    callLog.setCreatedAt(null);

    // Create the CallLog, which fails.

    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkRequestAtIsRequired() throws Exception {
    int databaseSizeBeforeTest = callLogRepository.findAll().size();
    // set the field null
    callLog.setRequestAt(null);

    // Create the CallLog, which fails.

    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void checkStateIsRequired() throws Exception {
    int databaseSizeBeforeTest = callLogRepository.findAll().size();
    // set the field null
    callLog.setState(null);

    // Create the CallLog, which fails.

    restCallLogMockMvc
        .perform(
            post("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeTest);
  }

  @Test
  @Transactional
  public void getAllCallLogs() throws Exception {
    // Initialize the database
    callLogRepository.saveAndFlush(callLog);

    // Get all the callLogList
    restCallLogMockMvc
        .perform(get("/api/call-logs?sort=id,desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.[*].id").value(hasItem(callLog.getId().intValue())))
        .andExpect(jsonPath("$.[*].calling").value(hasItem(DEFAULT_CALLING)))
        .andExpect(jsonPath("$.[*].called").value(hasItem(DEFAULT_CALLED)))
        .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
        .andExpect(jsonPath("$.[*].requestAt").value(hasItem(DEFAULT_REQUEST_AT.toString())))
        .andExpect(jsonPath("$.[*].responseAt").value(hasItem(DEFAULT_RESPONSE_AT.toString())))
        .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
        .andExpect(jsonPath("$.[*].route").value(hasItem(DEFAULT_ROUTE)))
        .andExpect(jsonPath("$.[*].routePrefix").value(hasItem(DEFAULT_ROUTE_PREFIX.intValue())))
        .andExpect(jsonPath("$.[*].routeLength").value(hasItem(DEFAULT_ROUTE_LENGTH)))
        .andExpect(jsonPath("$.[*].dryRun").value(hasItem(DEFAULT_DRY_RUN.booleanValue())))
        .andExpect(jsonPath("$.[*].whitelisted").value(hasItem(DEFAULT_WHITELISTED.booleanValue())))
        .andExpect(jsonPath("$.[*].blacklisted").value(hasItem(DEFAULT_BLACKLISTED.booleanValue())))
        .andExpect(jsonPath("$.[*].errorCode").value(hasItem(DEFAULT_ERROR_CODE)))
        .andExpect(jsonPath("$.[*].errorDesc").value(hasItem(DEFAULT_ERROR_DESC)))
        .andExpect(jsonPath("$.[*].vlr").value(hasItem(DEFAULT_VLR)))
        .andExpect(jsonPath("$.[*].sriAt").value(hasItem(DEFAULT_SRI_AT.toString())))
        .andExpect(jsonPath("$.[*].sriRespAt").value(hasItem(DEFAULT_SRI_RESP_AT.toString())));
  }

  @Test
  @Transactional
  public void getCallLog() throws Exception {
    // Initialize the database
    callLogRepository.saveAndFlush(callLog);

    // Get the callLog
    restCallLogMockMvc
        .perform(get("/api/call-logs/{id}", callLog.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.id").value(callLog.getId().intValue()))
        .andExpect(jsonPath("$.calling").value(DEFAULT_CALLING))
        .andExpect(jsonPath("$.called").value(DEFAULT_CALLED))
        .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
        .andExpect(jsonPath("$.requestAt").value(DEFAULT_REQUEST_AT.toString()))
        .andExpect(jsonPath("$.responseAt").value(DEFAULT_RESPONSE_AT.toString()))
        .andExpect(jsonPath("$.state").value(DEFAULT_STATE))
        .andExpect(jsonPath("$.route").value(DEFAULT_ROUTE))
        .andExpect(jsonPath("$.routePrefix").value(DEFAULT_ROUTE_PREFIX.intValue()))
        .andExpect(jsonPath("$.routeLength").value(DEFAULT_ROUTE_LENGTH))
        .andExpect(jsonPath("$.dryRun").value(DEFAULT_DRY_RUN.booleanValue()))
        .andExpect(jsonPath("$.whitelisted").value(DEFAULT_WHITELISTED.booleanValue()))
        .andExpect(jsonPath("$.blacklisted").value(DEFAULT_BLACKLISTED.booleanValue()))
        .andExpect(jsonPath("$.errorCode").value(DEFAULT_ERROR_CODE))
        .andExpect(jsonPath("$.errorDesc").value(DEFAULT_ERROR_DESC))
        .andExpect(jsonPath("$.vlr").value(DEFAULT_VLR))
        .andExpect(jsonPath("$.sriAt").value(DEFAULT_SRI_AT.toString()))
        .andExpect(jsonPath("$.sriRespAt").value(DEFAULT_SRI_RESP_AT.toString()));
  }

  @Test
  @Transactional
  public void getNonExistingCallLog() throws Exception {
    // Get the callLog
    restCallLogMockMvc
        .perform(get("/api/call-logs/{id}", Long.MAX_VALUE))
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  public void updateCallLog() throws Exception {
    // Initialize the database
    callLogRepository.saveAndFlush(callLog);

    int databaseSizeBeforeUpdate = callLogRepository.findAll().size();

    // Update the callLog
    CallLog updatedCallLog = callLogRepository.findById(callLog.getId()).get();
    // Disconnect from session so that the updates on updatedCallLog are not directly saved in db
    em.detach(updatedCallLog);
    updatedCallLog.setCalling(UPDATED_CALLING);
    updatedCallLog.setCalled(UPDATED_CALLED);
    updatedCallLog.setCreatedAt(UPDATED_CREATED_AT);
    updatedCallLog.setRequestAt(UPDATED_REQUEST_AT);
    updatedCallLog.setResponseAt(UPDATED_RESPONSE_AT);
    updatedCallLog.setState(UPDATED_STATE);
    updatedCallLog.setRoute(UPDATED_ROUTE);
    updatedCallLog.setRoutePrefix(UPDATED_ROUTE_PREFIX);
    updatedCallLog.setRouteLength(UPDATED_ROUTE_LENGTH);
    updatedCallLog.setDryRun(UPDATED_DRY_RUN);
    updatedCallLog.setWhitelisted(UPDATED_WHITELISTED);
    updatedCallLog.setBlacklisted(UPDATED_BLACKLISTED);
    updatedCallLog.setErrorCode(UPDATED_ERROR_CODE);
    updatedCallLog.setErrorDesc(UPDATED_ERROR_DESC);
    updatedCallLog.setVlr(UPDATED_VLR);
    updatedCallLog.setSriAt(UPDATED_SRI_AT);
    updatedCallLog.setSriRespAt(UPDATED_SRI_RESP_AT);

    restCallLogMockMvc
        .perform(
            put("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(updatedCallLog)))
        .andExpect(status().isOk());

    // Validate the CallLog in the database
    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeUpdate);
    CallLog testCallLog = callLogList.get(callLogList.size() - 1);
    assertThat(testCallLog.getCalling()).isEqualTo(UPDATED_CALLING);
    assertThat(testCallLog.getCalled()).isEqualTo(UPDATED_CALLED);
    assertThat(testCallLog.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
    assertThat(testCallLog.getRequestAt()).isEqualTo(UPDATED_REQUEST_AT);
    assertThat(testCallLog.getResponseAt()).isEqualTo(UPDATED_RESPONSE_AT);
    assertThat(testCallLog.getState()).isEqualTo(UPDATED_STATE);
    assertThat(testCallLog.getRoute()).isEqualTo(UPDATED_ROUTE);
    assertThat(testCallLog.getRoutePrefix()).isEqualTo(UPDATED_ROUTE_PREFIX);
    assertThat(testCallLog.getRouteLength()).isEqualTo(UPDATED_ROUTE_LENGTH);
    assertThat(testCallLog.getDryRun()).isEqualTo(UPDATED_DRY_RUN);
    assertThat(testCallLog.getWhitelisted()).isEqualTo(UPDATED_WHITELISTED);
    assertThat(testCallLog.getBlacklisted()).isEqualTo(UPDATED_BLACKLISTED);
    assertThat(testCallLog.getErrorCode()).isEqualTo(UPDATED_ERROR_CODE);
    assertThat(testCallLog.getErrorDesc()).isEqualTo(UPDATED_ERROR_DESC);
    assertThat(testCallLog.getVlr()).isEqualTo(UPDATED_VLR);
    assertThat(testCallLog.getSriAt()).isEqualTo(UPDATED_SRI_AT);
    assertThat(testCallLog.getSriRespAt()).isEqualTo(UPDATED_SRI_RESP_AT);
  }

  @Test
  @Transactional
  public void updateNonExistingCallLog() throws Exception {
    int databaseSizeBeforeUpdate = callLogRepository.findAll().size();

    // Create the CallLog

    // If the entity doesn't have an ID, it will throw BadRequestAlertException
    restCallLogMockMvc
        .perform(
            put("/api/call-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(callLog)))
        .andExpect(status().isBadRequest());

    // Validate the CallLog in the database
    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeUpdate);
  }

  @Test
  @Transactional
  public void deleteCallLog() throws Exception {
    // Initialize the database
    callLogRepository.saveAndFlush(callLog);

    int databaseSizeBeforeDelete = callLogRepository.findAll().size();

    // Delete the callLog
    restCallLogMockMvc
        .perform(delete("/api/call-logs/{id}", callLog.getId()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Validate the database contains one less item
    List<CallLog> callLogList = callLogRepository.findAll();
    assertThat(callLogList).hasSize(databaseSizeBeforeDelete - 1);
  }
}
