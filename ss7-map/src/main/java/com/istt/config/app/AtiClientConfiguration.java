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

import org.restcomm.protocols.ss7.map.api.primitives.AddressNature;
import org.restcomm.protocols.ss7.map.api.primitives.NumberingPlan;
import org.restcomm.protocols.ss7.map.api.service.mobility.subscriberInformation.DomainType;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** @author sergey vetyutnev */
//@ConfigurationProperties(prefix = "ati-client", ignoreUnknownFields = true)
@Configuration
@Data
@Slf4j
public class AtiClientConfiguration {

	protected static final String ADDRESS_NATURE = "addressNature";
	protected static final String NUMBERING_PLAN = "numberingPlan";
	protected static final String SUBSCRIBER_IDENTITY_TYPE = "subscriberIdentityType";
	protected static final String GET_LOCATION_INFORMATION = "getLocationInformation";
	protected static final String GET_SUBSCRIBER_STATE = "getSubscriberState";
	protected static final String GET_CURRENT_LOCATION = "getCurrentLocation";
	protected static final String GET_REQUESTED_DOMAIN = "getRequestedDomain";
	protected static final String GET_IMEI = "getImei";
	protected static final String GET_MS_CLASSMARK = "getMsClassmark";
	protected static final String GET_MNP_REQUESTED_INFO = "getMnpRequestedInfo";
	protected static final String GSM_SCF_ADDRESS = "gsmScfAddress";

	private AddressNature addressNature = AddressNature.international_number;
	private NumberingPlan numberingPlan = NumberingPlan.ISDN;
	private boolean subscriberIdentityTypeIsImsi = false;
	private boolean getLocationInformation = false;
	private boolean getSubscriberState = false;
	private boolean getCurrentLocation = false;
	private DomainType getRequestedDomain = null;
	private boolean getImei = false;
	private boolean getMsClassmark = false;
	private boolean getMnpRequestedInfo = false;
	private String gsmScfAddress = "000";

	private String srcGt;

//   @PostConstruct
	private void init() {
		log.info("======== SmsClientConfiguration ===============\n{}", this);
	}
}
