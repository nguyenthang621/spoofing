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

package com.istt.config.app;

import javax.annotation.PostConstruct;
import lombok.Data;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.smstpdu.NumberingPlanIdentification;
import org.mobicents.protocols.ss7.map.api.smstpdu.TypeOfNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sms-server", ignoreUnknownFields = true)
@Data
public class SmsServerConfiguration {

  private final Logger log = LoggerFactory.getLogger(SmsServerConfiguration.class);

  protected static final String ADDRESS_NATURE = "addressNature";
  protected static final String NUMBERING_PLAN = "numberingPlan";
  protected static final String SERVICE_CENTER_ADDRESS = "serviceCenterAddress";
  protected static final String MAP_PROTOCOL_VERSION = "mapProtocolVersion";
  protected static final String HLR_SSN = "hlrSsn";
  protected static final String VLR_SSN = "vlrSsn";
  protected static final String TYPE_OF_NUMBER = "typeOfNumber";
  protected static final String NUMBERING_PLAN_IDENTIFICATION = "numberingPlanIdentification";
  protected static final String SMS_CODING_TYPE = "smsCodingType";
  protected static final String SEND_SRSMDS_IF_ERROR = "sendSrsmdsIfError";
  protected static final String GPRS_SUPPORT_INDICATOR = "gprsSupportIndicator";

  protected AddressNature addressNature = AddressNature.international_number;
  protected NumberingPlan numberingPlan = NumberingPlan.ISDN;
  protected String serviceCenterAddress = "";
  protected int hlrSsn = 6;
  protected int vlrSsn = 8;
  protected TypeOfNumber typeOfNumber = TypeOfNumber.InternationalNumber;
  protected NumberingPlanIdentification numberingPlanIdentification =
      NumberingPlanIdentification.ISDNTelephoneNumberingPlan;
  protected boolean sendSrsmdsIfError = false;
  protected boolean gprsSupportIndicator = false;

  @PostConstruct
  private void init() {
    log.info("======== SmsClientConfiguration ===============\n{}", this);
  }
}
