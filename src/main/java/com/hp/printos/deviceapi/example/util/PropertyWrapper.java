// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyWrapper {

  public static Properties getProperties(String filename) {
    if (filename == null) return null;
    Properties props = new Properties();
    try {
      InputStream is = new FileInputStream(filename);
      props.load(is);
      return props;
    } catch (IOException e) {
      System.out.println("Error reading " + filename);
      return null;
    }
  }
}
