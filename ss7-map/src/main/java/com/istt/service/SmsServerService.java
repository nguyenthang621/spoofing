/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.istt.service;

import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.config.app.SmsServerConfiguration;
import com.istt.config.ss7.MAPConfiguration;
import com.istt.config.ss7.SCCPConfiguration;
import com.istt.service.util.MessageProcessingUtil;
import com.istt.ss7.dto.SriSMDTO;

import io.micrometer.core.instrument.Metrics;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.restcomm.protocols.ss7.indicator.NatureOfAddress;
import org.restcomm.protocols.ss7.indicator.RoutingIndicator;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContext;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextName;
import org.restcomm.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.restcomm.protocols.ss7.map.api.MAPDialog;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.restcomm.protocols.ss7.map.api.MAPMessage;
import org.restcomm.protocols.ss7.map.api.MAPProvider;
import org.restcomm.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.restcomm.protocols.ss7.map.api.primitives.AddressString;
import org.restcomm.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.restcomm.protocols.ss7.map.api.service.sms.AlertServiceCentreRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.AlertServiceCentreResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.ForwardShortMessageRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ForwardShortMessageResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.InformServiceCentreRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.LocationInfoWithLMSI;
import org.restcomm.protocols.ss7.map.api.service.sms.MAPDialogSms;
import org.restcomm.protocols.ss7.map.api.service.sms.MAPServiceSmsListener;
import org.restcomm.protocols.ss7.map.api.service.sms.MoForwardShortMessageRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.MoForwardShortMessageResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.MtForwardShortMessageRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.MtForwardShortMessageResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.NoteSubscriberPresentRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ReadyForSMRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ReadyForSMResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.ReportSMDeliveryStatusRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.ReportSMDeliveryStatusResponse;
import org.restcomm.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMRequest;
import org.restcomm.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMResponse;
import org.restcomm.protocols.ss7.sccp.impl.parameter.BCDEvenEncodingScheme;
import org.restcomm.protocols.ss7.sccp.parameter.EncodingScheme;
import org.restcomm.protocols.ss7.sccp.parameter.GlobalTitle;
import org.restcomm.protocols.ss7.sccp.parameter.SccpAddress;
import org.restcomm.protocols.ss7.tcap.asn.comp.Problem;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** @author sergey vetyutnev */
@Service
@Slf4j
public class SmsServerService implements MAPServiceSmsListener {
	
  public static final String SRI_CACHE = "SRISM";

  @Autowired SmsServerConfiguration smsServerProps;

  @Autowired ApplicationProperties props;

  @Autowired private MAPConfiguration mapMan;

  @Autowired private SCCPConfiguration sccp;

  @Autowired MapDialogHandle dialogHandler;

  @Autowired Redisson redisson;
  
  @Autowired RedisTemplate<String, Serializable> serializableRedisTemplate;

  private boolean isStarted = false;

  @PostConstruct
  public boolean start() {
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
    mapProvider.getMAPServiceSms().acivate();
    mapProvider.getMAPServiceSms().addMAPServiceListener(this);
    log.info("SMS Server has been started");
    isStarted = true;

    return true;
  }

  @PreDestroy
  public void stop() {
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
    isStarted = false;
    mapProvider.getMAPServiceSms().deactivate();
    mapProvider.getMAPServiceSms().removeMAPServiceListener(this);
    log.info("SMS Server has been stopped");
  }

  public long performSRIForSM(String destIsdnNumber) throws MAPException {
    if (!isStarted) return -1;
    if (destIsdnNumber == null || destIsdnNumber.equals("")) return -2;

    return doSendSri(destIsdnNumber, smsServerProps.getServiceCenterAddress());
  }

  /**
   * Send a request to SRI
   *
   * @param destIsdnNumber
   * @param serviceCentreAddr
   * @return
   * @throws MAPException
   */
  public long doSendSri(String destIsdnNumber, String serviceCentreAddr) throws MAPException {
	SriSMDTO dto = new SriSMDTO();
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

    MAPApplicationContextVersion vers =
        MapDialogHandle.mapverCache.getOrDefault(
            serviceCentreAddr, MAPApplicationContextVersion.version3);
    MAPApplicationContext mapAppContext =
        MAPApplicationContext.getInstance(MAPApplicationContextName.shortMsgGatewayContext, vers);

    ISDNAddressString msisdn =
        mapProvider
            .getMAPParameterFactory()
            .createISDNAddressString(
                smsServerProps.getAddressNature(),
                smsServerProps.getNumberingPlan(),
                destIsdnNumber);
    AddressString serviceCentreAddress =
        mapProvider
            .getMAPParameterFactory()
            .createAddressString(
                smsServerProps.getAddressNature(),
                smsServerProps.getNumberingPlan(),
                serviceCentreAddr);

    /** Generate the EC, GT and SccpAddress */
    EncodingScheme ec = new BCDEvenEncodingScheme();
    // GT = IAX_MAX, ssn = 8, pc = 0
    GlobalTitle gtSrc =
        this.sccp
            .getParameterFactory()
            .createGlobalTitle(
                props.getGtSrc(),
                0,
                org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
                ec,
                NatureOfAddress.INTERNATIONAL);
    SccpAddress origin =
        this.sccp
            .getParameterFactory()
            .createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gtSrc, 0, 8);

    // GT = MSISDN, ssn = 6, pc = 0
    GlobalTitle gtDest =
        this.sccp
            .getParameterFactory()
            .createGlobalTitle(
                destIsdnNumber,
                0,
                org.restcomm.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
                ec,
                NatureOfAddress.INTERNATIONAL);
    SccpAddress dest =
        this.sccp
            .getParameterFactory()
            .createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gtDest, 0, 6);

    // Create a MAP dialog to initiate this session
    MAPDialogSms curDialog =
        mapProvider.getMAPServiceSms().createNewDialog(mapAppContext, origin, null, dest, null);

    curDialog.addSendRoutingInfoForSMRequest(
        msisdn,
        true,
        serviceCentreAddress,
        null,
        smsServerProps.isGprsSupportIndicator(),
        null,
        null,
        null, false, null, false, false, null, null);

    dto.setAddress(destIsdnNumber);
    curDialog.setUserObject(dto);
    long invokeId = curDialog.getLocalDialogId();
    serializableRedisTemplate.opsForValue().set(SRI_CACHE + invokeId, destIsdnNumber, props.getTimeout(), TimeUnit.SECONDS);
    redisson.getCountDownLatch(Constants.TXN + invokeId).trySetCount(1);
    curDialog.sendDelayed();

    Metrics.globalRegistry
        .counter("map_request", "type", "SendRoutingInfoForSM", "exception", "none")
        .increment();
    return invokeId;
  }

  @Override
  public void onErrorComponent(
      MAPDialog mapDialog, Long invokeId, MAPErrorMessage mapErrorMessage) {
    log.debug(
        "onErrorComponent: mapDialog {} invokeId {} mapErrorMessage {}",
        mapDialog,
        invokeId,
        mapErrorMessage);
    // Unlock the door, and pass the cementary gates
    long localInvokeId = mapDialog.getLocalDialogId();
    // Extract data from index
    SriSMDTO meta = (SriSMDTO) mapDialog.getUserObject();
    String error = MessageProcessingUtil.extractError(mapErrorMessage.toString());
    meta.setError(error);
    meta.setErrorCode(mapErrorMessage.getErrorCode());
    meta.setRemoteGT(mapDialog.getRemoteAddress().getGlobalTitle().getDigits());

    serializableRedisTemplate.opsForValue().set(SRI_CACHE + localInvokeId, meta);

    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "SendRoutingInfoForSM", "exception", error)
        .increment();
  }

  @Override
  public void onRejectComponent(
      MAPDialog mapDialog, Long invokeId, Problem problem, boolean isLocalOriginated) {
    log.debug(
        "onRejectComponent: mapDialog {} invokeId {} problem {} isLocalOriginated {}",
        mapDialog,
        invokeId,
        problem,
        isLocalOriginated);

    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "SendRoutingInfoForSM", "exception", "REJECT")
        .increment();
  }

  @Override
  public void onInvokeTimeout(MAPDialog mapDialog, Long invokeId) {
    log.debug("onInvokeTimeout: mapDialog {} invokeId {}", mapDialog, invokeId);
    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "SendRoutingInfoForSM", "exception", "TIMEOUT")
        .increment();
  }

  @Override
  public void onSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse ind) {
    if (!isStarted) return;

    Metrics.globalRegistry
        .counter("map_response", "type", "SendRoutingInfoForSM", "exception", "none")
        .increment();

    MAPDialogSms curDialog = ind.getMAPDialog();
    long invokeId = curDialog.getLocalDialogId();
    LocationInfoWithLMSI li = ind.getLocationInfoWithLMSI();

    // Extract VLR
    String vlrNum = "";
    if (li != null && li.getNetworkNodeNumber() != null)
      vlrNum = li.getNetworkNodeNumber().getAddress();

    // Extract IMSI
    String destImsi = "";
    if (ind.getIMSI() != null) destImsi = ind.getIMSI().getData();

    // Extract MSISDN
    SriSMDTO meta = (SriSMDTO) curDialog.getUserObject();

    meta.setVlr(vlrNum);
    meta.setDestinationImsi(destImsi);
    meta.setDialogID(invokeId);
    meta.setRemoteGT(ind.getMAPDialog().getRemoteAddress().getGlobalTitle().getDigits());
    
   	serializableRedisTemplate.opsForValue().set(SRI_CACHE + invokeId, meta, props.getTimeout(), TimeUnit.SECONDS);
    redisson.getCountDownLatch(Constants.TXN + invokeId).countDown();
  }

  @Override
  public void onMAPMessage(MAPMessage mapMessage) {
    // TODO Auto-generated method stub
    log.debug("onMAPMessage: {}", mapMessage);
  }

  @Override
  public void onForwardShortMessageRequest(ForwardShortMessageRequest forwSmInd) {
    // TODO Auto-generated method stub
    log.debug("+ onForwardShortMessageRequest: {}", forwSmInd);
  }

  @Override
  public void onForwardShortMessageResponse(ForwardShortMessageResponse forwSmRespInd) {
    // TODO Auto-generated method stub
    log.debug("+ onForwardShortMessageResponse: {}", forwSmRespInd);
  }

  @Override
  public void onMoForwardShortMessageRequest(MoForwardShortMessageRequest moForwSmInd) {
    // TODO Auto-generated method stub
    log.debug("+ onMoForwardShortMessageRequest: {}", moForwSmInd);
  }

  @Override
  public void onMoForwardShortMessageResponse(MoForwardShortMessageResponse moForwSmRespInd) {
    // TODO Auto-generated method stub
    log.debug("+ onMoForwardShortMessageResponse: {}", moForwSmRespInd);
  }

  @Override
  public void onMtForwardShortMessageRequest(MtForwardShortMessageRequest mtForwSmInd) {
    // TODO Auto-generated method stub
    log.debug("+ MtForwardShortMessageRequest: {}", mtForwSmInd);
  }

  @Override
  public void onMtForwardShortMessageResponse(MtForwardShortMessageResponse mtForwSmRespInd) {
    // TODO Auto-generated method stub
    log.debug("+ onMtForwardShortMessageResponse: {}", mtForwSmRespInd);
  }

  @Override
  public void onSendRoutingInfoForSMRequest(SendRoutingInfoForSMRequest sendRoutingInfoForSMInd) {
    // TODO Auto-generated method stub
    log.debug("+ onSendRoutingInfoForSMRequest: {}", sendRoutingInfoForSMInd);
  }

  @Override
  public void onReportSMDeliveryStatusRequest(
      ReportSMDeliveryStatusRequest reportSMDeliveryStatusInd) {
    // TODO Auto-generated method stub
    log.debug("+ ReportSMDeliveryStatusRequest: {}", reportSMDeliveryStatusInd);
  }

  @Override
  public void onReportSMDeliveryStatusResponse(
      ReportSMDeliveryStatusResponse reportSMDeliveryStatusRespInd) {
    // TODO Auto-generated method stub
    log.debug("+ onReportSMDeliveryStatusResponse: {}", reportSMDeliveryStatusRespInd);
  }

  @Override
  public void onInformServiceCentreRequest(InformServiceCentreRequest informServiceCentreInd) {
    // TODO Auto-generated method stub
    log.debug("+ onInformServiceCentreRequest: {}", informServiceCentreInd);
  }

  @Override
  public void onAlertServiceCentreRequest(AlertServiceCentreRequest alertServiceCentreInd) {
    // TODO Auto-generated method stub
    log.debug("+ onAlertServiceCentreRequest: {}", alertServiceCentreInd);
  }

  @Override
  public void onAlertServiceCentreResponse(AlertServiceCentreResponse alertServiceCentreInd) {
    // TODO Auto-generated method stub
    log.debug("+ onAlertServiceCentreResponse: {}", alertServiceCentreInd);
  }

  @Override
  public void onReadyForSMRequest(ReadyForSMRequest request) {
    // TODO Auto-generated method stub
    log.debug("+ onReadyForSMRequest: {}", request);
  }

  @Override
  public void onReadyForSMResponse(ReadyForSMResponse response) {
    // TODO Auto-generated method stub
    log.debug("+ onReadyForSMResponse: {}", response);
  }

  @Override
  public void onNoteSubscriberPresentRequest(NoteSubscriberPresentRequest request) {
    // TODO Auto-generated method stub
    log.debug("+ onNoteSubscriberPresentRequest: {}", request);
  }
}
