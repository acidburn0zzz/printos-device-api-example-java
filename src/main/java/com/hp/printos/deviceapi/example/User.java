// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example;

import com.hp.printos.deviceapi.example.util.HttpClientWrapper;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class User {
  private static Logger log = LoggerFactory.getLogger(User.class);

  public static String loginAndGetToken(HttpClient client, String url, String login, String password)
    throws ParseException, IOException
  {
    String token = null;
    HttpPost loginPost = new HttpPost(url);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsonNode = mapper.createObjectNode();

    jsonNode.put("login", login);
    jsonNode.put("password", password);

    StringEntity entity = new StringEntity(jsonNode.toString());
    loginPost.setEntity(entity);

    HttpResponse response = HttpClientWrapper.executeHttpCommand(client, loginPost);
    if (response != null)
    {
      if(response.getStatusLine().getStatusCode() == 200)
      {
        token = HttpClientWrapper.getTokenFromResponse(response);
        EntityUtils.consumeQuietly(response.getEntity());
      }
      else {
        HttpClientWrapper.logErrorResponse(response, "Failed To login");
      }
    }else {
      log.error("No response returned from login call");
    }

    return token;
  }
}
