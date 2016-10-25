// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example.util;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Convenience wrapper for obtaining an HTTP client for use in the example code.
 */
public class HttpClientWrapper {
  private static Logger log = LoggerFactory.getLogger(HttpClientWrapper.class);

  public static HttpClient getClient(Properties p) throws Exception
  {
    HttpClientFactoryBean cf = new HttpClientFactoryBean();
    cf.setProxyEnabled(Boolean.parseBoolean(p.getProperty("proxy_enabled")));
    cf.setProxyHostname(p.getProperty("proxy_host"));
    cf.setProxyPort(Integer.parseInt(p.getProperty("proxy_port")));
    cf.setProxyScheme(p.getProperty("proxy_scheme"));
    cf.setInsecureSsl(true);

    return cf.getObject();
  }

  public static HttpResponse executeHttpCommand(HttpClient client, HttpUriRequest req)
  {
    HttpResponse response = null;
    req.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    req.addHeader(HttpHeaders.ACCEPT, "application/json");
    try {
      response = client.execute(req);
    } catch (Exception e) {
      log.error("Exception caught", e);
    }

    return (response);
  }

  public static String getTokenFromResponse(HttpResponse resp)
  {
    String cookie = resp.getFirstHeader("Set-Cookie").getValue();
    String cookieToken = cookie.split(";")[0];
    cookieToken = cookieToken.substring(cookieToken.indexOf('=')+1);
    return cookieToken;
  }

  public static void logErrorResponse(HttpResponse response, String message) throws ParseException, IOException
  {
    ObjectMapper mapper = new ObjectMapper();
    String resp = EntityUtils.toString(response.getEntity());
    JsonParser jp = mapper.getJsonFactory().createJsonParser(resp);
    JsonNode rootNode = mapper.readTree(jp);
    JsonNode node = rootNode.findValue("smsError");

    int statusCode = response.getStatusLine().getStatusCode();
    String subCode = node.findValue("subCode").getTextValue();
    log.error(message+ statusCode + "(" + subCode + ")");
  }

}
