//###
//
//  Lenovo examples - UDS API calls
//
//  Copyright Notice:
//
//  Copyright (c) 2022-present Lenovo. All right reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may
//  not use this file except in compliance with the License. You may obtain
//  a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
//  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
//  License for the specific language governing permissions and limitations
//  under the License.
//
//###

package com.company;

public class Device {
  String deviceName;
  String deviceManufacturer;
  String deviceModelType;
  String deviceSerialnumber;
  String deviceCategory;


  public Device(
      String deviceName,
      String deviceManufacturer,
      String deviceModelType,
      String deviceSerialnumber,
      String deviceCategory) {
    this.deviceName = deviceName;
    this.deviceManufacturer = deviceManufacturer;
    this.deviceModelType = deviceModelType;
    this.deviceSerialnumber = deviceSerialnumber;
    this.deviceCategory = deviceCategory;
  }

}
