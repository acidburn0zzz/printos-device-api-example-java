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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;


/**
 * Contains basic operations for devices such as logging in and sending status data.
 */
public class Device {
  private static Logger log = LoggerFactory.getLogger(User.class);

  public static DeviceInfo create(String aaaPrefix, HttpClient client, String userToken,
                                Properties settings, Properties credentials) throws ParseException, IOException {
    String url = aaaPrefix + "/organizations/devices";
    String pspPassword = credentials.getProperty("psp_password");
    HttpPost post = new HttpPost(url);
    post.addHeader("Cookie", Constants.X_SMS_AUTH_TOKEN + "=" + userToken);

    // Device model and type must match correctly; see etc/devicelist.csv for a list of currently supported
    // combinations.  Note that serial number must be unique, or the same device will get replaced.

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode jsonNode = mapper.createObjectNode();

    String serialNumber = getSerialNumber(settings.getProperty("serial_number"));
    jsonNode.put("name", settings.getProperty("device_name"));
    jsonNode.put("description", settings.getProperty("device_description"));
    jsonNode.put("serialNumber", serialNumber);
    jsonNode.put("model", settings.getProperty("device_model"));
    jsonNode.put("type", settings.getProperty("device_type"));
    jsonNode.put("securityPassword", pspPassword);

    StringEntity entity = new StringEntity(jsonNode.toString());
    post.setEntity(entity);

    HttpResponse response = HttpClientWrapper.executeHttpCommand(client, post);
    if (response != null) {
      String resp = EntityUtils.toString(response.getEntity());

      if (response.getStatusLine().getStatusCode() == 200) {
        JsonParser jp = mapper.getJsonFactory().createJsonParser(resp);
        JsonNode rootNode = mapper.readTree(jp);
        JsonNode node = rootNode.findValue("credentials");
        JsonNode node2 = rootNode.findValue("device");
        String deviceId = node2.findValue("deviceId").getTextValue();

        String deviceLogin = null;
        String devicePassword = null;
        if (node == null || node.findValue("login") == null) {
          System.out.println("Device is marked as non-provisionable, no login/password will be returned.");
        } else {
          deviceLogin = node.findValue("login").getTextValue();
          devicePassword = node.findValue("password").getTextValue();
          System.out.println("device_login=" + deviceLogin);
          System.out.println("device_password=" + devicePassword);
        }

        System.out.println("device_id=" + deviceId);
        System.out.println("Device created successfully.  Remember these values, this is the only time they'll be shown.");
        EntityUtils.consumeQuietly(response.getEntity());
        return new DeviceInfo(deviceLogin, devicePassword, deviceId, serialNumber);

      } else {
        System.out.println("Error executing command.  Does that device already exist?");
        EntityUtils.consumeQuietly(response.getEntity());
        return null;
      }

    } else {
      System.out.println("No response returned from create device");
      return null;
    }
  }

  public static boolean postRealTimeData(HttpClient client, String url, String deviceToken) throws ParseException, IOException {
    HttpPost post = new HttpPost(url);
    post.addHeader("Cookie", Constants.X_SMS_AUTH_TOKEN + "=" + deviceToken);

    long MINS_AHEAD = 1000 * 60 * 30;   // Pretend we have 30 minutes of print time left
    int jobsInQueue = 6;                // and 6 jobs left in the queue
    String status = "DS_PRINTING";      // and we're currently printing

    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    Timestamp printCompleteTime = new Timestamp(cal.getTime().getTime() + MINS_AHEAD);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    String date = format.format(printCompleteTime.getTime());

    ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
    ObjectNode jsonNode = mapper.createObjectNode();

    jsonNode.put("deviceStatus", status);
    jsonNode.put("totalJobs", jobsInQueue);
    jsonNode.put("printTimeComplete", date);

    StringEntity entity = new StringEntity(jsonNode.toString());
    post.setEntity(entity);

    HttpResponse response = HttpClientWrapper.executeHttpCommand(client, post);
    if (response != null) {
      if (response.getStatusLine().getStatusCode() != 204) {
        HttpClientWrapper.logErrorResponse(response, "Failed To post data");
        return false;
      } else {
        EntityUtils.consumeQuietly(response.getEntity());
        log.info("Sucessfully posted data");
        System.out.println("Successfully posted data.");
        return false;
      }
    } else {
      log.error("No response returned from post data");
      return false;
    }
  }

  protected static String getSerialNumber(String sn) {
    if (sn == null || sn.isEmpty()) {
      Random random = new Random();
      return "EX" + random.nextInt(10000000);
    } else {
      return sn;
    }
  }

}
