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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.config.app.AtiClientConfiguration;
import com.istt.config.ss7.MAPConfiguration;
import com.istt.config.ss7.SCCPConfiguration;
import com.istt.service.util.MessageProcessingUtil;
import com.istt.ss7.dto.AtiDTO;

import io.micrometer.core.instrument.Metrics;

import java.io.Serializable;
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
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.SubscriberIdentity;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobilityListener;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.AuthenticationFailureReportRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.AuthenticationFailureReportResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.SendAuthenticationInfoRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.SendAuthenticationInfoResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.ForwardCheckSSIndicationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.ResetRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.RestoreDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.RestoreDataResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.CheckImeiRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.CheckImeiResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.CancelLocationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.CancelLocationResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.PurgeMSRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.PurgeMSResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.SendIdentificationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.SendIdentificationResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.UpdateGprsLocationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.UpdateGprsLocationResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.UpdateLocationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.UpdateLocationResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.oam.ActivateTraceModeRequest_Mobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.oam.ActivateTraceModeResponse_Mobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.AnyTimeInterrogationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.AnyTimeInterrogationResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.ProvideSubscriberInfoRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.ProvideSubscriberInfoResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.RequestedInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.DeleteSubscriberDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.DeleteSubscriberDataResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.InsertSubscriberDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.InsertSubscriberDataResponse;
import org.mobicents.protocols.ss7.sccp.impl.parameter.BCDEvenEncodingScheme;
import org.mobicents.protocols.ss7.sccp.parameter.EncodingScheme;
import org.mobicents.protocols.ss7.sccp.parameter.GlobalTitle;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AtiClientService implements MAPServiceMobilityListener {

	public static final String ATI_CACHE = "ATI_";

  @Autowired private MAPConfiguration mapMan;

  @Autowired private AtiClientConfiguration atiProps;

  @Autowired ApplicationProperties props;

  @Autowired private SCCPConfiguration sccp;

  @Autowired MapDialogHandle dialogHandler;

  @Autowired Redisson redisson;

  @Autowired RedisTemplate<String, Serializable> serializableRedisTemplate;

  @Autowired ObjectMapper objectMapper;

  private boolean isStarted = false;

  @PostConstruct
  public boolean start() {
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
    mapProvider.getMAPServiceMobility().acivate();
    mapProvider.getMAPServiceMobility().addMAPServiceListener(this);
    //        mapProvider.addMAPDialogListener(this);
    log.info("ATI Client has been started");
    isStarted = true;

    return true;
  }

  @PreDestroy
  public void stop() {
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
    isStarted = false;
    mapProvider.getMAPServiceMobility().deactivate();
    mapProvider.getMAPServiceMobility().removeMAPServiceListener(this);
    log.info("ATI Client has been stopped");
  }

  public void execute() {}

  public String closeCurrentDialog() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Perform ATI Request
   *
   * @param address
   * @return
   * @throws MAPException
   */
  public long performAtiRequest(String address, AtiClientConfiguration override) throws MAPException {
    if (!isStarted) return -1;
    if (address == null || address.equals("")) return -2;

    return doAtiRequest(address, override != null ? override : atiProps);
  }

  /**
   * Perform ATI request
   *
   * @param address
   * @return
   * @throws MAPException
   */
  private long doAtiRequest(String address, AtiClientConfiguration override) throws MAPException {
	AtiDTO dto = new AtiDTO();
	dto.setAddress(address);
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

    MAPApplicationContextName acn = MAPApplicationContextName.anyTimeEnquiryContext;
    MAPApplicationContextVersion vers = MAPApplicationContextVersion.version3;
    MAPApplicationContext mapAppContext = MAPApplicationContext.getInstance(acn, vers);

    SubscriberIdentity subscriberIdentity;

    /** Generate the EC, GT and SccpAddress */
    EncodingScheme ec = new BCDEvenEncodingScheme();
    GlobalTitle gtDest;
    if (atiProps.isSubscriberIdentityTypeIsImsi()) {
      IMSI imsi = mapProvider.getMAPParameterFactory().createIMSI(address);
      subscriberIdentity = mapProvider.getMAPParameterFactory().createSubscriberIdentity(imsi);
      gtDest =
          this.sccp
              .getParameterFactory()
              .createGlobalTitle(
                  imsi.getData(),
                  0,
                  org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
                  ec,
                  NatureOfAddress.INTERNATIONAL);
    } else {
      ISDNAddressString msisdn =
          mapProvider
              .getMAPParameterFactory()
              .createISDNAddressString(atiProps.getAddressNature(), atiProps.getNumberingPlan(), address);
      subscriberIdentity = mapProvider.getMAPParameterFactory().createSubscriberIdentity(msisdn);
      gtDest =
          this.sccp
              .getParameterFactory()
              .createGlobalTitle(
                  msisdn.getAddress(),
                  0,
                  org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
                  ec,
                  NatureOfAddress.INTERNATIONAL);
    }
    RequestedInfo requestedInfo =
        mapProvider
            .getMAPParameterFactory()
            .createRequestedInfo(
            	override.isGetLocationInformation(),
            	override.isGetSubscriberState(),
                null,
                override.isGetCurrentLocation(),
                override.getGetRequestedDomain(),
                override.isGetImei(),
                override.isGetMsClassmark(),
                override.isGetMnpRequestedInfo());
    ISDNAddressString gsmSCFAddress =
        mapProvider
            .getMAPParameterFactory()
            .createISDNAddressString(
                atiProps.getAddressNature(), atiProps.getNumberingPlan(), atiProps.getGsmScfAddress());

    GlobalTitle gtSrc =
        this.sccp
            .getParameterFactory()
            .createGlobalTitle(
                atiProps.getSrcGt(),
                0,
                org.mobicents.protocols.ss7.indicator.NumberingPlan.ISDN_TELEPHONY,
                ec,
                NatureOfAddress.INTERNATIONAL);
    SccpAddress origin =
        this.sccp
            .getParameterFactory()
            .createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gtSrc, 0, 8);
    SccpAddress dest =
        this.sccp
            .getParameterFactory()
            .createSccpAddress(RoutingIndicator.ROUTING_BASED_ON_GLOBAL_TITLE, gtDest, 0, 6);

    MAPDialogMobility curDialog =
        mapProvider
            .getMAPServiceMobility()
            .createNewDialog(mapAppContext, origin, null, dest, null);
    curDialog.addAnyTimeInterrogationRequest(
        subscriberIdentity, requestedInfo, gsmSCFAddress, null);

    // Prepare the transaction
    long invokeId = curDialog.getLocalDialogId();
    curDialog.setUserObject(dto);
    curDialog.send();

    redisson.getCountDownLatch(Constants.TXN + invokeId).trySetCount(1);
    Metrics.globalRegistry
        .counter("map_request", "type", "AnyTimeInterrogation", "exception", "none")
        .increment();

    return invokeId;
  }

  /** Event received when HLR return ATI info */
  @Override
  public void onAnyTimeInterrogationResponse(AnyTimeInterrogationResponse ind) {
    if (!isStarted) return;

    MAPDialogMobility curDialog = ind.getMAPDialog();
    long invokeId = curDialog.getLocalDialogId();
    try {
		AtiDTO meta = (AtiDTO) curDialog.getUserObject();
      // Extract data from index
      if (ind.getSubscriberInfo() != null) {
        SubscriberInfo subInfo = ind.getSubscriberInfo();
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
              meta.setMcc(subInfo
                          .getLocationInformation()
                          .getCellGlobalIdOrServiceAreaIdOrLAI()
                          .getCellGlobalIdOrServiceAreaIdFixedLength()
                          .getMCC()
                     );
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

        if (subInfo.getLocationInformationGPRS() != null) {
            if (subInfo.getLocationInformationGPRS().getSGSNNumber() != null)
              meta.setVlr(subInfo.getLocationInformationGPRS().getSGSNNumber().getAddress());
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
      // Retrieve the counter part from hazelcast
      serializableRedisTemplate.opsForValue().set(ATI_CACHE + invokeId, meta, props.getTimeout(), TimeUnit.SECONDS);
      redisson.getCountDownLatch(Constants.TXN + invokeId).countDown();
      Metrics.globalRegistry
          .counter("map_response", "type", "AnyTimeInterrogation", "exception", "none")
          .increment();

    } catch (Exception e) {
      log.error("Cannot send callback URL", e);
    }
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
    if (isLocalOriginated) MapDialogHandle.needSendClose = true;
    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "AnyTimeInterrogation", "exception", "REJECT")
        .increment();
  }

  @Override
  public void onErrorComponent(
      MAPDialog mapDialog, Long invokeId, MAPErrorMessage mapErrorMessage) {
    log.debug(
        "onErrorComponent: mapDialog {} invokeId {} mapErrorMessage {}",
        mapDialog,
        invokeId,
        mapErrorMessage);
    // Extract data from index
    AtiDTO meta = (AtiDTO) mapDialog.getUserObject();
    String error = MessageProcessingUtil.extractError(mapErrorMessage.toString());
    meta.setError(error);
    // Retrieve the counter part from hazelcast

    try {
      serializableRedisTemplate.opsForValue().set(ATI_CACHE + invokeId, meta, props.getTimeout(), TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("Cannot encode error to json", e);
    }
    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "AnyTimeInterrogation", "exception", error)
        .increment();
  }

  @Override
  public void onInvokeTimeout(MAPDialog mapDialog, Long invokeId) {
    log.debug("onInvokeTimeout: mapDialog {} invokeId {}", mapDialog, invokeId);
    // TODO Auto-generated method stub
    dialogHandler.handleTxn(mapDialog.getLocalDialogId(), 5);
    Metrics.globalRegistry
        .counter("map_response", "type", "AnyTimeInterrogation", "exception", "TIMEOUT")
        .increment();
  }

  @Override
  public void onMAPMessage(MAPMessage message) {
    // TODO Auto-generated method stub
    log.debug("onMAPMessage: MAPMessage {}", message);
  }

  @Override
  public void onUpdateLocationRequest(UpdateLocationRequest ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onUpdateLocationResponse(UpdateLocationResponse ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCancelLocationRequest(CancelLocationRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCancelLocationResponse(CancelLocationResponse response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSendIdentificationRequest(SendIdentificationRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSendIdentificationResponse(SendIdentificationResponse response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onUpdateGprsLocationRequest(UpdateGprsLocationRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onUpdateGprsLocationResponse(UpdateGprsLocationResponse response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPurgeMSRequest(PurgeMSRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPurgeMSResponse(PurgeMSResponse response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSendAuthenticationInfoRequest(SendAuthenticationInfoRequest ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onSendAuthenticationInfoResponse(SendAuthenticationInfoResponse ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAuthenticationFailureReportRequest(AuthenticationFailureReportRequest ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAuthenticationFailureReportResponse(AuthenticationFailureReportResponse ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onResetRequest(ResetRequest ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onForwardCheckSSIndicationRequest(ForwardCheckSSIndicationRequest ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onRestoreDataRequest(RestoreDataRequest ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onRestoreDataResponse(RestoreDataResponse ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onAnyTimeInterrogationRequest(AnyTimeInterrogationRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProvideSubscriberInfoRequest(ProvideSubscriberInfoRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProvideSubscriberInfoResponse(ProvideSubscriberInfoResponse response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onInsertSubscriberDataRequest(InsertSubscriberDataRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onInsertSubscriberDataResponse(InsertSubscriberDataResponse request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDeleteSubscriberDataRequest(DeleteSubscriberDataRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDeleteSubscriberDataResponse(DeleteSubscriberDataResponse request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCheckImeiRequest(CheckImeiRequest request) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCheckImeiResponse(CheckImeiResponse response) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onActivateTraceModeRequest_Mobility(ActivateTraceModeRequest_Mobility ind) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onActivateTraceModeResponse_Mobility(ActivateTraceModeResponse_Mobility ind) {
    // TODO Auto-generated method stub

  }
}
