###
#
# Lenovo examples - UDS API calls
#
# Copyright Notice:
#
# Copyright (c) 2022-present Lenovo. All right reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
###

import json
import zipfile
import io
import requests
import getpass

global VERBOSE
global LIMIT

VERBOSE = True
LIMIT = 5


class Organization:
    def __init__(self, url_prefix, org_name, username, password, client_id, client_secret):
        self.url_prefix = url_prefix
        self.org_name = org_name
        self.username = username  # Your Lenovo ID email
        self.password = password  # Your Lenovo ID password
        self.account_token = ""  # Token from Lenovo ID account
        self.client_token = ""  # Token from app information
        self.client_id = client_id  # App ID
        self.client_secret = client_secret  # App secret
        self.subscription_id = ""  # User Subscription ID


def create_org(username="", password="", url_prefix="", org_name="", client_id="", client_secret=""):
    """"
    Creates Organization object to store user information/credentials
    """
    return Organization(url_prefix,
                        org_name,
                        username,
                        password,
                        client_id,
                        client_secret
                        )


def get_api_url(org, path=""):
    """
    Gets API link based on user information and the path to the API.

    :param org: user information
    :param path: API path
    :return: API endpoint link
    """
    return "https://api.{}.lenovo.com/{}".format(org.url_prefix, path)


def get_auth_token_from_account(org):
    """
    Gets the authorization token based on username and password, and stores the token into user organization object.

    :param org: user information
    :return: False if token could not be received, True if it is received
    """
    if org.username is None:
        org.username = "{}@lenovo.com".format(getpass.getuser())

    print("Account Name: {}, Username: {}".format(org.org_name, org.username))
    if org.password is None:
        org.password = getpass.getpass()

    org_name = org.org_name.lower()
    url = "https://auth.{}.lenovo.com/auth/realms/{}/protocol/openid-connect/token".format(
        org.url_prefix, org_name)

    payload_fmt = 'grant_type=password&client_id={}&username={}&password={}'
    payload = payload_fmt.format(org_name, org.username, org.password)
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }

    response = requests.request("POST", url, headers=headers, data=payload)
    if response.status_code != 200:
        print("Failed to get username/password token:", response.text)
        return False
    resp_json = json.loads(response.text)
    org.account_token = resp_json['access_token']
    return True


def get_subscription_id(org):
    """
    Gets subscription ID based on authorization token.

    :param org: User information
    :return: subscription ID
    """
    url = get_api_url(
        org, "core/account-management/v1/organizations/") + org.org_name
    print("\n** RUNNING - GET {} **".format(url))

    bearer_token = "Bearer {}".format(org.account_token)

    headers = {
        'Authorization': bearer_token,
        'Content-Type': 'application/x-www-form-urlencoded',
    }

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
    except requests.exceptions.HTTPError as errh:
        print(errh)
        raise
    except requests.exceptions.RequestException as err:
        print(err)
        raise

    if VERBOSE:
        print("Status Code: ", response.status_code)
    resp_json = json.loads(response.text)
    return resp_json['subscriptionId']


def print_devices(org):
    """
    Prints number of devices in the organization

    :param org: user information
    """

    url = get_api_url(org, "device-profile-service/v2/devices")
    print("\n** RUNNING - GET {} **".format(url))
    bearer_token = "Bearer {}".format(org.client_token)
    headers = {
        'Authorization': bearer_token,
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
    except requests.exceptions.HTTPError as errh:
        print(errh)
        raise
    except requests.exceptions.RequestException as err:
        print(err)
        raise

    if VERBOSE:
        print("Status Code: ", response.status_code)
    resp_json = json.loads(response.text)
    if VERBOSE:
        print('Number of devices in org: ', resp_json['numberOfElements'])


def print_users(org):
    """
    Prints number of users in organization

    :param org: user information
    """
    url = get_api_url(
        org, "core/account-management/v1/users?skip_myself=false")
    print("\n** RUNNING - GET {} **".format(url))
    bearer_token = "Bearer {}".format(org.client_token)
    headers = {
        'Authorization': bearer_token,
        'Content-Type': 'application/x-www-form-urlencoded'
        # 'skip_myself': 'false'
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
    except requests.exceptions.HTTPError as errh:
        print(errh)
        raise
    except requests.exceptions.RequestException as err:
        print(err)
        raise

    if VERBOSE:
        print("Status Code: ", response.status_code)
    resp_json = json.loads(response.text)
    if VERBOSE:
        print('Number of users in org: ', len(
            resp_json['_embedded']['userList']))


def claim_device(org, user_device, directory_to_save='claim-device-info'):
    """
    Claims user device in Lenovo Portal, stores response to directory

    :param directory_to_save: directory where zip file is extracted to
    :param org: User information
    """

    new_device_str = json.dumps(user_device)
    url = get_api_url(org, "device-profile-service/v2/devices/batch")
    print("\n** RUNNING - POST {} **".format(url))

    bearer_token = "Bearer {}".format(org.client_token)

    headers = {
        'Authorization': bearer_token,
        'Content-Type': 'application/json'
    }

    try:
        response = requests.post(url, headers=headers, data=new_device_str)
        if response.status_code == 200:
            z = zipfile.ZipFile(io.BytesIO(response.content))
            z.extractall(directory_to_save)
        response.raise_for_status()
    except requests.exceptions.HTTPError as errh:
        print(errh)
        raise
    except requests.exceptions.RequestException as err:
        print(err)
        raise

    print('Status Code: ', response.status_code)


def get_one_device_profile(org, mt, sn):
    """
    Get device information from given org and device mt and sn

    :param org: user information
    :param mt: desired device model type
    :param sn: desired device serial number
    :return:
    """
    url = get_api_url(
        org, "device-profile-service/v1/devices/mt/" + mt + "/sn/" + sn)
    print("\n** RUNNING - {} GET **".format(url))

    bearer_token = "Bearer {}".format(org.client_token)

    headers = {
        'Authorization': bearer_token,
        'Content-Type': 'application/json'
    }

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
    except requests.exceptions.HTTPError as errh:
        print(errh)
        raise
    except requests.exceptions.RequestException as err:
        print(err)
        raise

    print(response.status_code)
    resp_json = json.loads(response.text)
    return resp_json


def get_token_from_client(org):
    """
    Gets the authorization token based on client information.

    :param org: user information
    :return: False if token could not be received, True if it is received
    """

    print("Client ID: {}, Client Secret: {}".format(
        org.client_id, org.client_secret))

    org_name = org.org_name.lower()
    url = "https://auth.{}.lenovo.com/auth/realms/{}/protocol/openid-connect/token".format(
        org.url_prefix, org_name)
    print("\n** RUNNING - GET {} **".format(url))

    payload_fmt = 'grant_type=client_credentials&client_id={}&client_secret={}'
    payload = payload_fmt.format(org.client_id, org.client_secret)
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }

    response = requests.request("POST", url, headers=headers, data=payload)
    if response.status_code != 200:
        print("Failed to get client token:", response.text)
        return False
    resp_json = json.loads(response.text)
    org.client_token = resp_json['access_token']
    return True


def print_total_applications(org):
    """
        Prints number of applications


        :param org: user information
        """
    url = get_api_url(org, "core/application-management/v1/apps/search?key=''")
    print("\n** RUNNING - GET {} **".format(url))
    bearer_token = "Bearer {}".format(org.client_token)
    headers = {
        'Authorization': bearer_token,
        'Content-Type': 'application/x-www-form-urlencoded'
        # 'key': ''
    }
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
    except requests.exceptions.HTTPError as errh:
        print(errh)
        raise
    except requests.exceptions.RequestException as err:
        print(err)
        raise

    if VERBOSE:
        print("Status Code: ", response.status_code)
    resp_json = json.loads(response.text)
    print('Number of apps: ', resp_json['totalElements'])
    # if VERBOSE:
    #     print('number of apps in org: ', len(resp_json['_embedded']['userList']))


def driver():
    username = input("Enter Username \n")
    try:
        password = getpass.getpass("Enter Password \n")
    except Exception:
        print('Error getting password!')
        return

    environment_url = "Put the environment value"
    account_name = "Enter your Account Name"
    client_id = "Enter your Client ID"
    client_secret = "Enter your Client Secret"

    my_device = {
        'deviceManufacturer': 'Enter the deviceManufacturer here',
        'deviceModelType': 'Enter the valid deviceModelType here',
        'deviceName': 'Enter the deviceName here',
        'deviceSerialnumber': 'Enter the deviceSerialnumber here',
        "deviceCategory": 'Enter the deviceCategory here'
    }
    my_devices =[ my_device ]

    org = create_org(username=username, password=password, url_prefix=environment_url,
                     org_name=account_name, client_id=client_id, client_secret=client_secret)

    # Get auth token based on user credentials
    if not get_auth_token_from_account(org):
        print("Error getting token from Lenovo ID account!")

    if VERBOSE:
        print("Org Token: ", org.account_token)

    if VERBOSE:
        print("Secret: ", org.client_secret)

    # Get Subscription ID
    subscription_id = get_subscription_id(org)
    print('subscription ID: ', subscription_id)
    org.subscription_id = subscription_id

    # Get token from Client Secret
    if not get_token_from_client(org):
        print("Error getting token from Client Secret!")
    if VERBOSE:
        print("Org Token: ", org.client_token)
    #
    print_total_applications(org)
    #
    claim_device(org, my_devices)
    print_devices(org)
    device_profile = get_one_device_profile(org, my_device['deviceModelType'], my_device['deviceSerialnumber'])
    print(device_profile)
    org_device_id = device_profile["orgDeviceId"]
    if VERBOSE:
        print("orgDeviceId", org_device_id)
    print_users(org)
    #
    print("Hello Device!")


if __name__ == '__main__':
    driver()
