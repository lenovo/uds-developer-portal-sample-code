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

import java.io.*;
import java.net.*;

import org.json.*;
import com.google.gson.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

  public static Organization getOrganization(String username, String password) {
    /*
        Creates Organization entity object to store user information/credentials.
        CHANGE TO YOUR INFORMATION BELLOW!
    */

    String environmentUrl = "Put the environment value";
    String accountName = "Enter your account name";
    String clientId = "Enter your client ID";
    String clientSecret = "Enter your client secret";

    return new Organization(environmentUrl, accountName, username, password, clientId, clientSecret);
  }

    private static Device getClaimDevice() {
    /*
        Create a Device object with your device information.
        CHANGE TO THE INFORMATION OF YOUR DEVICE BELLOW!
    */
        return new Device(
            "Enter the deviceName here",
            "Enter the deviceManufacturer here",
            "Enter the deviceModelType here",
            "Enter the deviceSerialnumber here",
            "Enter the deviceCategory here"
        );
    }

  public static void main(String[] args) throws IOException {

    Scanner fileScan = new Scanner(new File("src/main/java/com/company/credentials.txt"));
    String username = fileScan.next();
    String password = fileScan.next();

    Organization myOrganization = getOrganization(username, password);
    Device myDevice = getClaimDevice();

    getToken(myOrganization);
    System.out.println("Account Token: " + myOrganization.token + "\n");
    getSubscriptionId(myOrganization);
    System.out.println("Subscription ID: " + myOrganization.subscription_id + "\n");
    getTokenFromClient(myOrganization);
    System.out.println("Client Auth Token: " + myOrganization.client_token + "\n");

    printTotalApplications(myOrganization);

    claim_device(myOrganization, myDevice);

    printDevices(myOrganization);

    String orgDeviceId = getOneDeviceProfile(myOrganization, myDevice.deviceModelType, myDevice.deviceSerialnumber);
    System.out.println("orgDeviceId: " + orgDeviceId + "\n");

    printUsers(myOrganization);

    System.out.println("Hello Device!");
  }

  public static String getApiUrl(Organization myOrg, String path) {
    /*
        Gets API link based on user information and the path to the API.
    */

    return String.format("https://api.%s.lenovo.com/%s", myOrg.urlOrg, path);
  }

  public static void getToken(Organization myOrg) throws IOException {
    /*
        Gets the authorization token based on username and password,
        and stores the token into user organization object.
    */

    String apiUrl = String.format("https://auth.%s.lenovo.com/auth/realms/%s/protocol/openid-connect/token",
        myOrg.urlOrg, myOrg.realm);
    System.out.println("RUNNING - Get Token : " + apiUrl);

    URL url = new URL(apiUrl);
    String grantType = "password";
    String postData = "grant_type=" + grantType +
        "&client_id=" + myOrg.realm +
        "&username=" + myOrg.username +
        "&password=" + myOrg.password;

    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
    conn.setDoOutput(true);
    conn.getOutputStream().write(postDataBytes);
    try {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      String line = "";

      StringBuilder responseStrBuilder = new StringBuilder();
      while ((line = in.readLine()) != null) {
        responseStrBuilder.append(line);
      }
      in.close();

      JSONObject result = new JSONObject(responseStrBuilder.toString());
      HashMap<String, String> jsonData = new Gson().fromJson(result.toString(), HashMap.class);
      myOrg.token = jsonData.get("access_token");
    } catch (Exception e) {
      System.out.println("Error getting Access token" + e);
    }
  }

  public static void getSubscriptionId(Organization myOrg) throws IOException {
    /*
        Gets subscription ID based on authorization token.
    */

    String apiUrl = getApiUrl(myOrg, "core/account-management/v1/organizations/") + myOrg.realm;
    System.out.println("RUNNING - Get Subscription : " + apiUrl);
    URL url = new URL(apiUrl);

    String token = "Bearer " + myOrg.token;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", token);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      StringBuilder response = new StringBuilder();
      String responseLine = null;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      JSONObject result = new JSONObject(response.toString());
      Map<String, String> jsonData = new Gson().fromJson(result.toString(), HashMap.class);
      myOrg.subscription_id = jsonData.get("subscriptionId");
    }
  }

  public static void getTokenFromClient(Organization myOrg) throws IOException {
    /*
        Gets the authorization token based on client information.
    */

    String apiUrl = String.format("https://auth.%s.lenovo.com/auth/realms/%s/protocol/openid-connect/token",
        myOrg.urlOrg, myOrg.realm);
    System.out.println("RUNNING - Get Token from Client: " + apiUrl);

    URL url = new URL(apiUrl);
    String grantType = "client_credentials";

    String postData = "grant_type=" + grantType +
        "&client_id=" + myOrg.client_id +
        "&client_secret=" + myOrg.client_secret;

    byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    conn.setDoOutput(true);
    conn.getOutputStream().write(postDataBytes);
    try {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
      String line = "";

      StringBuilder responseStrBuilder = new StringBuilder();
      while ((line = in.readLine()) != null) {
        responseStrBuilder.append(line);
      }
      in.close();

      JSONObject result = new JSONObject(responseStrBuilder.toString());
      HashMap<String, String> jsonData = new Gson().fromJson(result.toString(), HashMap.class);
      myOrg.client_token = jsonData.get("access_token");
    } catch (Exception e) {
      System.out.println("Error getting Access token" + e);
    }
  }

  public static void printTotalApplications(Organization myOrg) throws IOException {
    /*
        Prints number of devices in the organization
    */
    String apiUrl = getApiUrl(myOrg, "core/application-management/v1/apps/search?key=''");
    System.out.println("RUNNING - Get Total Applications : " + apiUrl);
    URL url = new URL(apiUrl);

    String token = "Bearer " + myOrg.client_token;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", token);
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      StringBuilder response = new StringBuilder();
      String responseLine = null;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      JSONObject result = new JSONObject(response.toString());
      Map<String, Object> jsonData = new Gson().fromJson(result.toString(), HashMap.class);
      System.out.println(jsonData.get("totalElements") + "\n");
    }
  }

  public static void claim_device(Organization myOrg, Device userDevice) throws IOException {
    /*
        Claims user device in lenovo portal, stores response in root, C:/
    */

    JSONArray json = new JSONArray();
    Map<String, String> myDeviceInfo = new HashMap<>();
    myDeviceInfo.put("deviceName", userDevice.deviceName);
    myDeviceInfo.put("deviceManufacturer", userDevice.deviceManufacturer);
    myDeviceInfo.put("deviceModelType", userDevice.deviceModelType);
    myDeviceInfo.put("deviceSerialnumber", userDevice.deviceSerialnumber);
    myDeviceInfo.put("deviceCategory", userDevice.deviceCategory);
    json.put(myDeviceInfo);

    String params = json.toString();
    String apiUrl = getApiUrl(myOrg, "device-profile-service/v2/devices/batch");
    System.out.println("RUNNING - Claim Device : " + apiUrl);

    URL url = new URL(apiUrl);
    String token = "Bearer " + myOrg.client_token;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Authorization", token);
    conn.setRequestProperty("Content-Type", "application/json");
    byte[] input = params.getBytes(StandardCharsets.UTF_8);
    conn.getOutputStream().write(input);
    try (InputStream inputStream = conn.getInputStream()) {
      Files.copy(inputStream, Paths.get("device-info.zip"), StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception ex) {
      throw ex;
    }
    unzip("device-info.zip", "device-info");
  }

  private static void unzip(String zipFilePath, String destDir) {
    /*
        Unzips the contents of a zipped folder into a destination directory
    */

    File dir = new File(destDir);
    // create output directory if it doesn't exist
    if (!dir.exists())
      dir.mkdirs();
    FileInputStream fis;
    //buffer for read and write data to file
    byte[] buffer = new byte[1024];
    try {
      fis = new FileInputStream(zipFilePath);
      ZipInputStream zis = new ZipInputStream(fis);
      ZipEntry ze = zis.getNextEntry();
      while (ze != null) {
        String fileName = ze.getName();
        File newFile = new File(destDir + File.separator + fileName);
        System.out.println("Unzipping to " + newFile.getAbsolutePath());
        //create directories for sub directories in zip
        new File(newFile.getParent()).mkdirs();
        FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        //close this ZipEntry
        zis.closeEntry();
        ze = zis.getNextEntry();
      }
      //close last ZipEntry
      zis.closeEntry();
      zis.close();
      fis.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getOneDeviceProfile(Organization myOrg, String mt, String sn) throws IOException {
    /*
        Get orgDeviceId value of the device by given org, mt and sn
    */

    String apiUrl = getApiUrl(myOrg, "device-profile-service/v1/devices/mt/") + mt + "/sn/" + sn;
    System.out.println("RUNNING - Get Device Profile : " + apiUrl);
    URL url = new URL(apiUrl);

    String token = "Bearer " + myOrg.client_token;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", token);
    conn.setRequestProperty("Content-Type", "application/json");

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      StringBuilder response = new StringBuilder();
      String responseLine = null;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      JSONObject result = new JSONObject(response.toString());
      Map<String, Object> jsonData = new Gson().fromJson(result.toString(), HashMap.class);
      return (String) jsonData.get("orgDeviceId");
    }
  }

  public static void printDevices(Organization myOrg) throws IOException {
    /*
        Prints number of devices in the organization
    */

    String apiUrl = getApiUrl(myOrg, "device-profile-service/v2/devices");
    System.out.println("RUNNING - Print Devices : " + apiUrl);
    URL url = new URL(apiUrl);

    String token = "Bearer " + myOrg.client_token;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", token);
    conn.setRequestProperty("Content-Type", "application/json");

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      StringBuilder response = new StringBuilder();
      String responseLine = null;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      JSONObject result = new JSONObject(response.toString());
      Map<String, Object> jsonData = new Gson().fromJson(result.toString(), HashMap.class);
      System.out.println("Number of Devices : " + jsonData.get("numberOfElements") + "\n");
    }
  }

  public static void printUsers(Organization myOrg) throws IOException {
    /*
        Prints number of users in organization
    */

    String apiUrl = getApiUrl(myOrg, "core/account-management/v1/users?skip_myself=false");
    System.out.println("RUNNING - Number of Users : " + apiUrl);
    URL url = new URL(apiUrl);

    String token = "Bearer " + myOrg.client_token;
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", token);
    conn.setRequestProperty("Content-Type", "application/json");

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      int responseCode = conn.getResponseCode();
      System.out.println("Response Code:" + responseCode);
      StringBuilder response = new StringBuilder();
      String responseLine = null;
      while ((responseLine = br.readLine()) != null) {
        response.append(responseLine.trim());
      }
      JSONObject result = new JSONObject(response.toString());
      Map<String, Object> jsonData = new Gson().fromJson(result.toString(), HashMap.class);

      System.out.println("Number of users in org: " + ((Map) jsonData.get("page")).get("totalElements") + "\n");
    }
  }

}
