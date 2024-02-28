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
import com.istt.ss7.dto.SriCallDTO;

import io.micrometer.core.instrument.Metrics;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContext;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextName;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPMessage;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.AlertingPattern;
import org.mobicents.protocols.ss7.map.api.primitives.EMLPPPriority;
import org.mobicents.protocols.ss7.map.api.primitives.ExtExternalSignalInfo;
import org.mobicents.protocols.ss7.map.api.primitives.ExternalSignalInfo;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.mobicents.protocols.ss7.map.api.service.callhandling.CUGCheckInfo;
import org.mobicents.protocols.ss7.map.api.service.callhandling.CallDiversionTreatmentIndicator;
import org.mobicents.protocols.ss7.map.api.service.callhandling.CallReferenceNumber;
import org.mobicents.protocols.ss7.map.api.service.callhandling.CamelInfo;
import org.mobicents.protocols.ss7.map.api.service.callhandling.InterrogationType;
import org.mobicents.protocols.ss7.map.api.service.callhandling.IstCommandRequest;
import org.mobicents.protocols.ss7.map.api.service.callhandling.IstCommandResponse;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPDialogCallHandling;
import org.mobicents.protocols.ss7.map.api.service.callhandling.MAPServiceCallHandlingListener;
import org.mobicents.protocols.ss7.map.api.service.callhandling.ProvideRoamingNumberRequest;
import org.mobicents.protocols.ss7.map.api.service.callhandling.ProvideRoamingNumberResponse;
import org.mobicents.protocols.ss7.map.api.service.callhandling.RoutingInfo;
import org.mobicents.protocols.ss7.map.api.service.callhandling.SendRoutingInformationRequest;
import org.mobicents.protocols.ss7.map.api.service.callhandling.SendRoutingInformationResponse;
import org.mobicents.protocols.ss7.map.api.service.callhandling.SuppressMTSS;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.ISTSupportIndicator;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.ExtBasicServiceCode;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ForwardingReason;
import org.mobicents.protocols.ss7.map.service.callhandling.CallReferenceNumberImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.BCDEvenEncodingScheme;
import org.mobicents.protocols.ss7.sccp.parameter.EncodingScheme;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** @author sergey vetyutnev */
@Service
@Slf4j
public class SriCallService implements MAPServiceCallHandlingListener {

  public static String SOURCE_NAME = "SRI_CALLER";

  public static String SRI_CALL_CACHE = "SRICALL_";

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
    mapProvider.getMAPServiceCallHandling().acivate();
    mapProvider.getMAPServiceCallHandling().addMAPServiceListener(this);
    log.info("MAP service call handling Server has been started");
    isStarted = true;

    return true;
  }

  @PreDestroy
  public void stop() {
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
    isStarted = false;
    mapProvider.getMAPServiceCallHandling().deactivate();
    mapProvider.getMAPServiceCallHandling().removeMAPServiceListener(this);
    log.info("MAP service call handling Server has been stopped");
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
	SriCallDTO dto = new SriCallDTO();
	dto.setAddress(destIsdnNumber);
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

    MAPApplicationContextVersion vers =
        MapDialogHandle.mapverCache.getOrDefault(
            serviceCentreAddr, MAPApplicationContextVersion.version3);
    MAPApplicationContext mapAppContext =
        MAPApplicationContext.getInstance(
            MAPApplicationContextName.locationInfoRetrievalContext, vers);

    ISDNAddressString msisdn =
        mapProvider
            .getMAPParameterFactory()
            .createISDNAddressString(
                smsServerProps.getAddressNature(),
                smsServerProps.getNumberingPlan(),
                destIsdnNumber);
    ISDNAddressString gmscAddress =
        mapProvider
            .getMAPParameterFactory()
            .createISDNAddressString(
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
                org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
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
                org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
                ec,
                NatureOfAddress.INTERNATIONAL);
    SccpAddress dest =
        this.sccp
            .getParameterFactory()
            .createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gtDest, 0, 6);

    // Create a MAP dialog to initiate this session
    MAPDialogCallHandling curDialog =
        mapProvider
            .getMAPServiceCallHandling()
            .createNewDialog(mapAppContext, origin, null, dest, null);

    InterrogationType interrogationType = InterrogationType.basicCall;

    CUGCheckInfo cugCheckInfo = null;
    Integer numberOfForwarding = null;
    boolean orInterrogation = false;
    Integer orCapability = 1;
    byte[] callRef = {(byte) 0xdc, (byte) 0xca, (byte) 0x1b, (byte) 0x00, (byte) 0x04};
    CallReferenceNumber callReferenceNumber = new CallReferenceNumberImpl(callRef);

    ForwardingReason forwardingReason = null;
    ExtBasicServiceCode basicServiceGroup = null;
    ExternalSignalInfo networkSignalInfo = null;
    CamelInfo camelInfo = null;
    boolean suppressionOfAnnouncement = false;
    MAPExtensionContainer extensionContainer = null;
    AlertingPattern alertingPattern = null;
    boolean ccbsCall = false;
    Integer supportedCCBSPhase = null;
    ExtExternalSignalInfo additionalSignalInfo = null;
    ISTSupportIndicator istSupportIndicator = null;
    boolean prePagingSupported = false;
    CallDiversionTreatmentIndicator callDiversionTreatmentIndicator = null;
    boolean longFTNSupported = false;
    boolean suppressVtCSI = false;
    boolean suppressIncomingCallBarring = false;
    boolean gsmSCFInitiatedCall = false;
    ExtBasicServiceCode basicServiceGroup2 = null;
    ExternalSignalInfo networkSignalInfo2 = null;
    boolean mtRoamingRetrySupported = false;
    EMLPPPriority callPriority = null;

    SuppressMTSS supressMTSS = null;
    curDialog.addSendRoutingInformationRequest(
        msisdn,
        cugCheckInfo,
        numberOfForwarding,
        interrogationType,
        orInterrogation,
        orCapability,
        gmscAddress,
        callReferenceNumber,
        forwardingReason,
        basicServiceGroup,
        networkSignalInfo,
        camelInfo,
        suppressionOfAnnouncement,
        extensionContainer,
        alertingPattern,
        ccbsCall,
        supportedCCBSPhase,
        additionalSignalInfo,
        istSupportIndicator,
        prePagingSupported,
        callDiversionTreatmentIndicator,
        longFTNSupported,
        suppressVtCSI,
        suppressIncomingCallBarring,
        gsmSCFInitiatedCall,
        basicServiceGroup2,
        networkSignalInfo2,
        supressMTSS,
        mtRoamingRetrySupported,
        callPriority);

    curDialog.setUserObject(dto);
    long invokeId = curDialog.getLocalDialogId();
    redisson.getCountDownLatch(Constants.TXN + invokeId).trySetCount(1);
    curDialog.sendDelayed();

    Metrics.globalRegistry
        .counter("map_request", "type", "SendRoutingInfoCall", "exception", "none")
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
    SriCallDTO meta = (SriCallDTO) mapDialog.getUserObject();
    String error = MessageProcessingUtil.extractError(mapErrorMessage.toString());
    meta.setError(error);
    meta.setErrorCode(mapErrorMessage.getErrorCode());
    meta.setRemoteGT(mapDialog.getRemoteAddress().getGlobalTitle().getDigits());

    serializableRedisTemplate.opsForValue().set(SRI_CALL_CACHE + localInvokeId, meta, props.getTimeout(), TimeUnit.SECONDS);

    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "SendRoutingInfoCall", "exception", error)
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
        .counter("map_response", "type", "SendRoutingInfoCall", "exception", "REJECT")
        .increment();
  }

  @Override
  public void onInvokeTimeout(MAPDialog mapDialog, Long invokeId) {
    log.debug("onInvokeTimeout: mapDialog {} invokeId {}", mapDialog, invokeId);
    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "SendRoutingInfoCall", "exception", "TIMEOUT")
        .increment();
  }

  @Override
  public void onMAPMessage(MAPMessage mapMessage) {
    // TODO Auto-generated method stub
    log.info("MAPMessage: {}", mapMessage);
  }

  @Override
  public void onSendRoutingInformationRequest(SendRoutingInformationRequest request) {
    // TODO Auto-generated method stub
    log.info("SendRoutingInformationRequest: {}", request);
  }

  /** Parse response from HLR and save to hazelcast for pickup */
  @Override
  public void onSendRoutingInformationResponse(SendRoutingInformationResponse response) {
    log.info("SendRoutingInformationResponse: {}", response);
    if (!isStarted) return;

    MAPDialogCallHandling curDialog = response.getMAPDialog();
    long invokeId = curDialog.getLocalDialogId();

    try {
      // Extract data from index
      SriCallDTO meta = (SriCallDTO) curDialog.getUserObject();
      if (response.getSubscriberInfo() != null) {
        SubscriberInfo subInfo = response.getSubscriberInfo();
        if (subInfo.getIMEI() != null) meta.setImei(subInfo.getIMEI().getIMEI());

        if (subInfo.getLocationInformation() != null) {
          if (subInfo.getLocationInformation().getVlrNumber() != null)
            meta.setVlr(subInfo.getLocationInformation().getVlrNumber().getAddress());
          if (subInfo.getLocationInformation().getMscNumber() != null)
            meta.setMsc(subInfo.getLocationInformation().getMscNumber().getAddress());
          if ((subInfo.getLocationInformation().getCellGlobalIdOrServiceAreaIdOrLAI() != null)
              && (subInfo
                      .getLocationInformation()
                      .getCellGlobalIdOrServiceAreaIdOrLAI()
                      .getCellGlobalIdOrServiceAreaIdFixedLength()
                  != null)) {
            try {
              meta.setMcc(
                  subInfo
                          .getLocationInformation()
                          .getCellGlobalIdOrServiceAreaIdOrLAI()
                          .getCellGlobalIdOrServiceAreaIdFixedLength()
                          .getMCC());
              meta.setMnc(
                  subInfo
                          .getLocationInformation()
                          .getCellGlobalIdOrServiceAreaIdOrLAI()
                          .getCellGlobalIdOrServiceAreaIdFixedLength()
                          .getMNC()
                      );
            } catch (MAPException e) {
              log.warn("Cannot extract MNC and MCC", e);
            }
          }
        }

        if (subInfo.getSubscriberState() != null)
          meta.setSubscriberState(subInfo.getSubscriberState().getSubscriberStateChoice().name());
        if (subInfo.getMNPInfoRes() != null) {
          if (subInfo.getMNPInfoRes().getIMSI() != null)
            meta.setImsi(subInfo.getMNPInfoRes().getIMSI().getData());
          if (subInfo.getMNPInfoRes().getMSISDN() != null)
            meta.setMsisdn(subInfo.getMNPInfoRes().getMSISDN().getAddress());
          if (subInfo.getMNPInfoRes().getRouteingNumber() != null)
            meta.setMsrn(subInfo.getMNPInfoRes().getRouteingNumber().getRouteingNumber());
          if (subInfo.getMNPInfoRes().getNumberPortabilityStatus() != null)
            meta.setMnp(subInfo.getMNPInfoRes().getNumberPortabilityStatus().toString());
        }
      }

      Optional.ofNullable(response.getExtendedRoutingInfo())
          .flatMap(
              extendedInfo ->
                  Optional.ofNullable(extendedInfo.getRoutingInfo())
                      .map(RoutingInfo::getRoamingNumber))
          .ifPresent(msrn -> meta.setMsrn(msrn.getAddress()));
      Optional.ofNullable(response.getIMSI())
          .map(IMSI::getData)
          .ifPresent(imsi -> meta.setImsi(imsi));
      Optional.ofNullable(response.getVmscAddress())
          .map(ISDNAddressString::getAddress)
          .ifPresent(
              msc -> {
                meta.setMsc(msc);
                meta.setVlr(msc);
              });
      serializableRedisTemplate.opsForValue().set(SRI_CALL_CACHE + invokeId, meta, props.getTimeout(), TimeUnit.SECONDS);

      redisson.getCountDownLatch(Constants.TXN + invokeId).countDown();
      Metrics.globalRegistry
          .counter("map_response", "type", "SendRoutingInfoCall", "exception", "none")
          .increment();

    } catch (Exception e) {
      log.error("Cannot send callback URL", e);
    }
  }

  @Override
  public void onProvideRoamingNumberRequest(ProvideRoamingNumberRequest request) {
    // TODO Auto-generated method stub
    log.info("ProvideRoamingNumberRequest: {}", request);
  }

  @Override
  public void onProvideRoamingNumberResponse(ProvideRoamingNumberResponse response) {
    // TODO Auto-generated method stub
    log.info("ProvideRoamingNumberResponse: {}", response);
  }

  @Override
  public void onIstCommandRequest(IstCommandRequest request) {
    // TODO Auto-generated method stub
    log.info("IstCommandRequest: {}", request);
  }

  @Override
  public void onIstCommandResponse(IstCommandResponse response) {
    // TODO Auto-generated method stub
    log.info("IstCommandResponse: {}", response);
  }
}
