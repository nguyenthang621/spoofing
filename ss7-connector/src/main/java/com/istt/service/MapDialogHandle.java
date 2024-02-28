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

import com.istt.config.Constants;
import com.istt.config.ss7.MAPConfiguration;
import com.istt.config.ss7.SCTPConfiguration;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.mobicents.protocols.ss7.map.api.MAPApplicationContextVersion;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.MAPProvider;
import org.mobicents.protocols.ss7.map.api.dialog.MAPAbortProviderReason;
import org.mobicents.protocols.ss7.map.api.dialog.MAPAbortSource;
import org.mobicents.protocols.ss7.map.api.dialog.MAPNoticeProblemDiagnostic;
import org.mobicents.protocols.ss7.map.api.dialog.MAPRefuseReason;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortChoice;
import org.mobicents.protocols.ss7.map.api.primitives.AddressString;
import org.mobicents.protocols.ss7.map.api.primitives.MAPExtensionContainer;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MapDialogHandle implements MAPDialogListener {

  @Autowired private MAPConfiguration mapMan;

  @Autowired SCTPConfiguration sctpConfiguration;

  @Autowired Redisson redisson;

  @Autowired RedisTemplate<String, Serializable> serializableRedisTemplate;

  public static boolean needSendSend = false;

  public static boolean needSendClose = false;

  public static Map<String, MAPApplicationContextVersion> mapverCache = new HashMap<>();

  @PostConstruct
  public boolean start() {
    MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();
    mapProvider.addMAPDialogListener(this);
    log.debug("=====  MAP Service Handle is ready");
    return true;
  }

  /**
   * Unlock transaction based on local invokeID
   *
   * @param invokeId Local Invoke ID
   * @param status 2 = OK, 5 = Undeliverable
   */
  public void handleTxn(long invokeId, int status) {
    redisson.getCountDownLatch(Constants.TXN + invokeId).countDown();
  }

  @Override
  public void onDialogAccept(MAPDialog dialog, MAPExtensionContainer container) {
    log.debug("onDialogAccept: {}", dialog);
    log.debug("MAPExtensionContainer: {}", container);
  }

  @Override
  public void onDialogClose(MAPDialog dlg) {
    log.debug("onDialogClose: {}", dlg);
    handleTxn(dlg.getLocalDialogId(), 2);
  }

  @Override
  public void onDialogDelimiter(MAPDialog dlg) {
    log.debug("onDialogDelimiter: {}", dlg);
    handleTxn(dlg.getLocalDialogId(), 2);
    try {
      if (needSendSend) {
        needSendSend = false;
        dlg.send();
        return;
      }
    } catch (Exception e) {
      log.error("Exception when invoking send() : " + e.getMessage(), e);
      return;
    }
    try {
      if (needSendClose) {
        needSendClose = false;
        dlg.close(false);
        return;
      }
    } catch (Exception e) {
      log.error("Exception when invoking close() : " + e.getMessage(), e);
      return;
    }
  }

  @Override
  public void onDialogNotice(MAPDialog dlg, MAPNoticeProblemDiagnostic notice) {
    String uData = "dialogNotice=" + notice.toString() + ", dlgId=" + dlg.getLocalDialogId();
    log.debug("Rcvd: DialogNotice", uData);
  }

  @Override
  public void onDialogProviderAbort(
      MAPDialog dlg,
      MAPAbortProviderReason abortProviderReason,
      MAPAbortSource abortSource,
      MAPExtensionContainer extensionContainer) {
    log.debug(
        "onDialogProviderAbort: dialog {} reason {} source {} container {}",
        dlg,
        abortProviderReason,
        abortSource,
        extensionContainer);
    handleTxn(dlg.getLocalDialogId(), 5);
  }

  @Override
  public void onDialogReject(
      MAPDialog dlg,
      MAPRefuseReason refuseReason,
      ApplicationContextName alternativeApplicationContext,
      MAPExtensionContainer extensionContainer) {
    String uData =
        "refuseReason="
            + refuseReason
            + ", alternativeApplicationContext="
            + alternativeApplicationContext
            + ", dlgId="
            + dlg.getLocalDialogId();
    log.debug("Rcvd: DialogReject", uData);
    handleTxn(dlg.getLocalDialogId(), 5);
  }

  @Override
  public void onDialogRelease(MAPDialog dlg) {
    log.debug("onDialogRelease: {}", dlg);
    handleTxn(dlg.getLocalDialogId(), 2);
  }

  @Override
  public void onDialogRequest(
      MAPDialog arg0, AddressString arg1, AddressString arg2, MAPExtensionContainer arg3) {}

  @Override
  public void onDialogRequestEricsson(
      MAPDialog arg0,
      AddressString arg1,
      AddressString arg2,
      AddressString arg3,
      AddressString arg4) {}

  @Override
  public void onDialogTimeout(MAPDialog dlg) {
    log.debug("onDialogTimeout: {}", dlg);
    handleTxn(dlg.getLocalDialogId(), 5);
  }

  @Override
  public void onDialogUserAbort(
      MAPDialog dlg, MAPUserAbortChoice userReason, MAPExtensionContainer extensionContainer) {
    String uData = "userReason=" + userReason + ", dlgId=" + dlg.getLocalDialogId();
    log.debug("Rcvd: DialogUserAbort", uData);
    handleTxn(dlg.getLocalDialogId(), 5);
  }
}
