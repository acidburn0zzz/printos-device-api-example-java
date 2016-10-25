// © Copyright 2016 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.printos.deviceapi.example;

/**
 * Represents the items of interest when a device is created.  Returned from Device.create().
 */
public class DeviceInfo {
  private String deviceLogin;
  private String devicePassword;
  private String deviceId;
  private String serialNumber;

  public DeviceInfo(String deviceLogin, String devicePassword, String deviceId, String serialNumber) {
    this.deviceLogin = deviceLogin;
    this.devicePassword = devicePassword;
    this.deviceId = deviceId;
    this.serialNumber = serialNumber;
  }

  public String getDeviceLogin() {
    return deviceLogin;
  }

  public void setDeviceLogin(String deviceLogin) {
    this.deviceLogin = deviceLogin;
  }

  public String getDevicePassword() {
    return devicePassword;
  }

  public void setDevicePassword(String devicePassword) {
    this.devicePassword = devicePassword;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;


  }
}
