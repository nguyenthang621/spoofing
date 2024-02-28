package com.istt.service.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StreamUtils;

import com.istt.config.Constants;
import com.istt.domain.ConnectorLog;
import com.istt.repository.ConnectorLogRepository;

public class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {


	  private final Logger log = LoggerFactory.getLogger(HeaderRequestInterceptor.class);

	  @Override
	  public ClientHttpResponse intercept(
	      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
	    HttpRequest wrapper = new HttpRequestWrapper(request);
	    for (String k : headers.keySet()) wrapper.getHeaders().set(k, headers.get(k));
	    log.debug("=== {} URL: {}", request.getMethod(), request.getURI());

	    Map<String, String> queryParams = splitQuery(request.getURI());
	    log.debug(">>> QueryParams: {}", queryParams);
	    String requestBody = new String(body);
	    log.debug(">>> RequestBody: {}", requestBody);
	    Instant requestAt = Instant.now();
	    
	    ClientHttpResponse result = execution.execute(wrapper, body);
	    
	    Instant responseAt = Instant.now();
	    int errorCode = result.getRawStatusCode();
	    String errorDesc = result.getStatusText();
	    String responsePayload = StreamUtils.copyToString(result.getBody(), Charset.defaultCharset()); 
	    log.debug("<<< RESP: {} - {} -- {}", errorCode, errorDesc, responsePayload);
	    
	    logRepo.save(new ConnectorLog()
	    		.callref(queryParams.getOrDefault(Constants.REQ_CALLREF, ""))
	    		.requestChannel(queryParams.getOrDefault(Constants.REQ_CHANNEL, ""))
	    		.requestInstance(queryParams.getOrDefault(Constants.REQ_INSTANCE, ""))
	    		.requestParams(request.getURI().getRawQuery())
	    		.requestBody(requestBody)
	    		// + timing
	    		.requestAt(requestAt)
	    		.responseAt(responseAt)
	    		.jitter(responseAt.getNano() - requestAt.getNano())
	    		// + response
	    		.errorCode(errorCode)
	    		.errorDesc(errorDesc)
	    		.responsePayload(responsePayload)
	    );
	    return result;
	  }

	  private final Map<String, String> headers;
	  
	  private final ConnectorLogRepository logRepo;

	  public HeaderRequestInterceptor(Map<String, String> headers, ConnectorLogRepository logRepo) {
	    this.headers = headers;
	    this.logRepo = logRepo;
	  }
	  
	  public static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
		    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		    String query = url.getQuery();
		    String[] pairs = query.split("&");
		    for (String pair : pairs) {
		        int idx = pair.indexOf("=");
		        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		    }
		    return query_pairs;
		}
}
