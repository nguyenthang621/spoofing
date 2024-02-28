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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.istt.service.dto.AddressTranslatorDTO;

@ConfigurationProperties(prefix = "isup", ignoreUnknownFields = true)
@Data
@Slf4j
public class IsupProxyConfiguration {

  private int originalPoincode = 111;

  private int destinationPointcode = 222;

  private int interceptorPointcode = 333;

  private int interceptorNetworkIndication = 0;

  private int ni = 0;

  private int si = 5;

  private int mp = 0;
  
  private int rlcCauseCode = 21;
  
  /**
   * List of Address Translator
   */
  private List<AddressTranslatorDTO> nats = new ArrayList<>();

  private String callbackUrl =
      "http://localhost:8089/api/public/spoofing?calling={callingParty}&called={calledParty}";

  @PostConstruct
  private void init() {
    log.info("======== IsupProxyConfiguration ===============\n{}", this);
  }
}
