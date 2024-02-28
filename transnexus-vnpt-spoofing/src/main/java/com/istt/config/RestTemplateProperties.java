package com.istt.config;

import com.istt.repository.ConnectorLogRepository;
import com.istt.service.util.HeaderRequestInterceptor;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configure default rest template to enforce content type and log request and response
 *
 * @author dinhtrung
 */
@Configuration
public class RestTemplateProperties {
	
	/**
     * Create a new REST Template to connect to other services
     *
     * @return
     */
    @Bean
    public RestTemplate restTemplate(ApplicationProperties props, ConnectorLogRepository connectorLogRepo) {
    	Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(props.getTimeout() * 1000);
        httpRequestFactory.setReadTimeout(props.getTimeout() * 1000);
        httpRequestFactory.setBufferRequestBody(true);
        ClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(httpRequestFactory);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new HeaderRequestInterceptor(headers, connectorLogRepo));
        return restTemplate;
    }
}
