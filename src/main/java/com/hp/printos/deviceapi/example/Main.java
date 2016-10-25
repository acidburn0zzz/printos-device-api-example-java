// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example;

import com.hp.printos.deviceapi.example.util.HttpClientWrapper;
import com.hp.printos.deviceapi.example.util.ExampleValidator;
import com.hp.printos.deviceapi.example.util.PropertyWrapper;
import org.apache.http.client.HttpClient;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

public class Main {
  public static void main(String[] args) {
    PropertyConfigurator.configure("log4j.xml");

    System.out.println("PrintOS device API example\n");
    System.out.println("Before running, review " + Constants.SETTINGS_FILE + " and " + Constants.CREDS_FILE);
    System.out.println("and change the settings as appropriate.\n");

    Properties settings = PropertyWrapper.getProperties(Constants.SETTINGS_FILE);
    Properties credentials = PropertyWrapper.getProperties(Constants.CREDS_FILE);
    ExampleValidator.checkCredentials(credentials);    // Make sure the user has edited creds.properties

    String pspLogin = credentials.getProperty("psp_login");
    String pspPassword = credentials.getProperty("psp_password");

    try {

      // Step 1.  Log in as the PSP administrator.

      System.out.println("Logging in as user " + pspLogin + ".");
      HttpClient client = HttpClientWrapper.getClient(settings);
      String url = settings.getProperty("base_url") + Constants.AAA_PATH + "/users/login";
      String userToken = User.loginAndGetToken(client, url, pspLogin, pspPassword);
      ExampleValidator.checkToken(userToken);

      // Step 2.  Create the device.

      System.out.println("Creating device \"" + settings.getProperty("device_name") + "\".\n");
      DeviceInfo deviceInfo = Device.create(settings.getProperty("base_url") + Constants.AAA_PATH, client, userToken, settings, credentials);
      ExampleValidator.checkDevice(deviceInfo);

      if (deviceInfo.getDeviceLogin() != null) {

        // Step 3.  Log in as the device.

        url = settings.getProperty("base_url") + Constants.AAA_PATH + "/devices/login";
        String deviceToken = User.loginAndGetToken(client, url, deviceInfo.getDeviceLogin(), deviceInfo.getDevicePassword());
        ExampleValidator.checkToken(deviceToken);

      // Step 4.  Send in some statistics for the device.

        url = settings.getProperty("base_url") + Constants.SS_PATH + "/devices";
        Device.postRealTimeData(client, url, deviceToken);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
