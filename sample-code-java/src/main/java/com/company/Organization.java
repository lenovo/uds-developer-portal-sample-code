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

public class Organization {
    String urlOrg;
    String realm;
    String username;
    String password;
    String token;
    String client_id;
    String client_secret;
    String subscription_id;
    String client_token;

    public Organization(String urlOrg, String realm, String username, String password, String clientId, String clientSecret) {
        this.urlOrg = urlOrg;
        this.realm = realm;
        this.username = username;
        this.password = password;
        this.token = "";
        this.client_id = clientId;
        this.client_secret = clientSecret;
        this.subscription_id = "";
        this.client_token = "";

    }

}
