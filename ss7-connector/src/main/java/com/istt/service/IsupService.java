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

import com.istt.config.app.IsupProxyConfiguration;
import com.istt.config.ss7.M3UAConfiguration;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.mobicents.protocols.ss7.isup.ISUPTimeoutEvent;
import org.mobicents.protocols.ss7.isup.ParameterException;
import org.mobicents.protocols.ss7.isup.impl.message.AbstractISUPMessage;
import org.mobicents.protocols.ss7.isup.impl.message.parameter.CalledPartyNumberImpl;
import org.mobicents.protocols.ss7.isup.impl.message.parameter.CallingPartyNumberImpl;
import org.mobicents.protocols.ss7.isup.impl.message.parameter.CauseIndicatorsImpl;
import org.mobicents.protocols.ss7.isup.message.InitialAddressMessage;
import org.mobicents.protocols.ss7.isup.message.ReleaseMessage;
import org.mobicents.protocols.ss7.isup.message.parameter.CalledPartyNumber;
import org.mobicents.protocols.ss7.isup.message.parameter.CallingPartyNumber;
import org.mobicents.protocols.ss7.isup.message.parameter.CauseIndicators;
import org.mobicents.protocols.ss7.isup.message.parameter.NatureOfAddressIndicator;
import org.mobicents.protocols.ss7.mtp.Mtp3TransferPrimitive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IsupService {

  @Autowired IsupProxyConfiguration isupProps;

  public void onTimeout(ISUPTimeoutEvent event) {
    log.error("ISUPTimeoutEvent: {}", event);
  }

  /**
   * Send test IAM message with given calling and called address
   *
   * @return
   * @throws ParameterException
   * @throws IOException
   */
  public boolean sendTestIAMMessage(
      String callingAddr, String calledAddr, Integer originatingPC, Integer destinationPC)
      throws ParameterException, IOException {
    InitialAddressMessage iam = M3uaListenerImpl.isupMessageFactory.createIAM(0);
    byte[] data = {
      (byte) 0x12,
      (byte) 0x03,
      (byte) 0x01,
      (byte) 0x10,
      (byte) 0x20,
      (byte) 0x01,
      (byte) 0x0a,
      (byte) 0x00,
      (byte) 0x02,
      (byte) 0x0a,
      (byte) 0x08,
      (byte) 0x84,
      (byte) 0x10,
      (byte) 0x48,
      (byte) 0x49,
      (byte) 0x73,
      (byte) 0x26,
      (byte) 0x01,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x08,
      (byte) 0x84,
      (byte) 0x13,
      (byte) 0x48,
      (byte) 0x09,
      (byte) 0x99,
      (byte) 0x73,
      (byte) 0x82,
      (byte) 0x06,
      (byte) 0x00
    };

    ((AbstractISUPMessage) iam)
        .decode(data, M3uaListenerImpl.isupMessageFactory, M3uaListenerImpl.isupParameterFactory);
    if (calledAddr != null) {
      CalledPartyNumber called =
          new CalledPartyNumberImpl(NatureOfAddressIndicator._INTERNATIONAL, calledAddr, 1, 0);
      iam.setCalledPartyNumber(called);
    }
    if (callingAddr != null) {
      CallingPartyNumber calling =
          new CallingPartyNumberImpl(
              NatureOfAddressIndicator._INTERNATIONAL, callingAddr, 1, 0, 0, 3);
      iam.setCallingPartyNumber(calling);
    }

    byte[] payload = ((AbstractISUPMessage) iam).encode();

    int si = isupProps.getSi();
    int ni = isupProps.getNi();
    int mp = isupProps.getMp();
    int opc = Optional.ofNullable(originatingPC).orElse(isupProps.getOriginalPoincode());
    int dpc = Optional.ofNullable(destinationPC).orElse(isupProps.getDestinationPointcode());
    int sls = iam.getSls();
    Mtp3TransferPrimitive mtp3TransferPrimitive =
        M3UAConfiguration.clientM3UAMgmt
            .getMtp3TransferPrimitiveFactory()
            .createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, payload);
    M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
    return true;
  }

  /**
   * Send Test REL message with given CIC
   *
   * @param cic
   * @return
   * @throws ParameterException
   * @throws IOException
   */
  public boolean sendTestRELMessage(Integer cic, Integer originatingPC, Integer destinationPC)
      throws ParameterException, IOException {
    ReleaseMessage rlc = M3uaListenerImpl.isupMessageFactory.createREL(cic);
    CauseIndicatorsImpl original =
        new CauseIndicatorsImpl(
            CauseIndicators._CODING_STANDARD_ITUT,
            CauseIndicators._LOCATION_USER,
            1,
            CauseIndicators._CV_CALL_REJECTED,
            null);
    rlc.setCauseIndicators(original);
    byte[] rel = ((AbstractISUPMessage) rlc).encode();

    int si = isupProps.getSi();
    int ni = isupProps.getNi();
    int mp = isupProps.getMp();
    int opc = Optional.ofNullable(originatingPC).orElse(isupProps.getOriginalPoincode());
    int dpc = Optional.ofNullable(destinationPC).orElse(isupProps.getDestinationPointcode());
    int sls = 0;
    log.debug(
        "REL: si {} ni {} mp {} opc {} dpc {} sls {} {}",
        si,
        ni,
        mp,
        opc,
        dpc,
        sls,
        Hex.encodeHex(rel));
    Mtp3TransferPrimitive mtp3TransferPrimitive =
        M3UAConfiguration.clientM3UAMgmt
            .getMtp3TransferPrimitiveFactory()
            .createMtp3TransferPrimitive(si, ni, mp, opc, dpc, sls, rel);
    M3UAConfiguration.clientM3UAMgmt.sendMessage(mtp3TransferPrimitive);
    return true;
  }
}
