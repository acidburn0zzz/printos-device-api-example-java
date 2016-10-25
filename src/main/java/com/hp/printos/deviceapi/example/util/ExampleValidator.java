// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example.util;

import com.hp.printos.deviceapi.example.Constants;
import com.hp.printos.deviceapi.example.DeviceInfo;

import java.util.Properties;

/**
 * Provide error checks for the example code.  This makes the example easier to read.  Validate things
 * along the way such as credentials, tokens, and device info.
 */
public class ExampleValidator {

  public static void checkCredentials(Properties p) {
    String login = p.getProperty("psp_login");
    String password = p.getProperty("psp_password");
    if (login == null || password == null || login.isEmpty() ||
      password.isEmpty() || "REPLACEME".equals(login) || "REPLACEME".equals(password)) {
      System.out.println("Error: please edit " + Constants.CREDS_FILE + " and replace the values for");
      System.out.println("psp_login and psp_password.  These are the credentials you created");
      System.out.println("when you accepted the invite to PrintOS.");
      System.exit(1);
    }
  }

  public static void checkToken(String token) {
    if (token == null) {
      System.out.println("Error: failed to log in with the given credentials.  If this is a user login, please check");
      System.out.println(Constants.CREDS_FILE + " and make sure psp_login and psp_password are set to your credentials");
      System.out.println("and base_url is pointing to the correct stack.  If this is a device login,");
      System.out.println("make sure the device exists and was not replaced by a subsequent run; devices that are");
      System.out.println("re-added to the same org with the same device type, model, and serial number will replace");
      System.out.println("the previous device with a new one (with a new login and password).");
      System.exit(1);
    }
  }

  public static void checkDevice(DeviceInfo deviceInfo) {
    if (deviceInfo == null) {
      System.out.println("Device creation failed.  Common causes of this: ");
      System.out.println("  - Device serial number in use in another org/account (for same device type and model)");
      System.out.println("  - Invalid values used for device type or model");
      System.out.println("  - Invalid characters used in certain field (e.g. special chars attempted in the serial number field");
      System.exit(1);
    }
  }
}
