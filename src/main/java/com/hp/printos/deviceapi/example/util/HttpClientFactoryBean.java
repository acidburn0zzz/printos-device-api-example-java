// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example.util;

import com.google.common.base.Strings;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.FactoryBean;

import javax.net.ssl.SSLContext;

/**
 * Convenience class for obtaining a full-featured HTTP client.  Ignore for purposes of the example.
 *
 * Maven dependencies: Apache HttpComponents httpclient, spring-core and spring-context, guava
 */
public class HttpClientFactoryBean implements FactoryBean<HttpClient> {
  private boolean proxyEnabled;
  private String proxyScheme = "http";
  private String proxyHostname;
  private int proxyPort = 8080;

  private boolean insecureSsl = false;

  private int connectionPoolSize = 10;

  @Override
  public HttpClient getObject() throws Exception {
    HttpClientBuilder clientBuilder = HttpClients.custom();

    HttpRoutePlanner routePlanner = getRoutePlanner();
    if( routePlanner != null ) {
      clientBuilder.setRoutePlanner(routePlanner);
    }

    clientBuilder.disableCookieManagement();
    clientBuilder.setMaxConnPerRoute(connectionPoolSize);

    if (insecureSsl) {
      SSLContext context = SSLContexts.custom()
              .loadTrustMaterial(null, new TrustSelfSignedStrategy())
              .build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
              context,
              new String[]{"TLSv1.2"},
              null,
              SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      clientBuilder.setSSLSocketFactory(sslsf);
    }

    return clientBuilder.build();
  }

  private HttpRoutePlanner getRoutePlanner() {
    proxyHostname = Strings.nullToEmpty(proxyHostname);
    if (proxyHostname.isEmpty()) {
      proxyEnabled = false;
    }
    if (proxyEnabled) {
      HttpHost proxy = new HttpHost(proxyHostname, proxyPort, proxyScheme);

      HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy) {

        @Override
        public HttpRoute determineRoute(
                final HttpHost host,
                final HttpRequest request,
                final HttpContext context) throws HttpException {
          String hostname = host.getHostName();
          if (hostname.equals("127.0.0.1") || hostname.equalsIgnoreCase("localhost")) {
            // Return direct route
            return new HttpRoute(host);
          }
          return super.determineRoute(host, request, context);
        }
      };
      return routePlanner;
    }
    return null;
  }

  @Override
  public Class<?> getObjectType() {
    return HttpClient.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }

  public void setProxyEnabled(boolean proxyEnabled) {
    this.proxyEnabled = proxyEnabled;
  }

  public void setProxyScheme(String proxyScheme) {
    this.proxyScheme = proxyScheme;
  }

  public void setProxyHostname(String proxyHostname) {
    this.proxyHostname = proxyHostname;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }

  public void setInsecureSsl(boolean insecureSsl) {
    this.insecureSsl = insecureSsl;
  }

  public void setConnectionPoolSize(int connectionPoolSize) {
    this.connectionPoolSize = connectionPoolSize;
  }

}
