# UDS Developer Portal Sample Code using UDS API

More information and API specs can be found at Developer Portal.

Must have - Lenovo ID account and an Account name!

### Steps to run the code

1. Install requests library (pip install requests or pip3 install requests). s
2. Put your information on Hello-World file, in the *driver* method.
   1. Change the environment URL, account name, client ID and client secret to your own information. **OBS: The Client ID and Secret can be created at My Apps on Developer Portal**.
3. Put the desired Device information in the Hello-World file, in the *driver* method (*my_device* and *custom_device*).
4. Run the Hello-World script. When prompted, you need to type your Lenovo ID login and password.
   1. You can do that by running either:
      1. python Hello-World.py
      2. python3 Hello-World.py

### Tasks to get used to UDS API - all done in this script
1. Get Authorization token A using Lenovo ID account username, password, and organization ID.
2. (Token A) Use informed Client ID and Secret to retrieve token.
   1. This will return client information, such as client secret
3. (Token A) Get subscription ID via /core/account-management/v1/organizations/{your-org-Name}
4. Get new token B with the clientID and client secret credential
5. (Token B) Create device in organization
6. (Token B) Get number of devices in the organization using device profile service API
6. (Token B) Get number of devices in the organization using device profile service API
7. (Token B) Get number of users in the organization
8. (Token B) Get number of Applications in the organization
9. (Token B) Claim your device (check hints below)
   1. Verify UDC is installed in the machine
   2. Claim device using developer portal API
      1. Will need to run script as administrator if you want files saved into root
      2. If not, save the files to any subdirectory, and manually move them into the root folder.
   4. restart the UDC client in Windows Services
   5. Verify that device is active in UDS portal
10. (Token B) API call to collect device information

### Ensure UDC is installed
- Verify universal device client (UDC) is installed on device
  - Open Windows Services, double check whether Universal Device Client Service is installed and running

### Claim your Device
- Find device information - serial number, manufacturer, Model type
  - Using windows powershell as admin, run the following commands:
    - To find serial number: `wmic bios get serialnumber`
    - To find manufacturer and model type: `Get-CimInstance -ClassName Win32_ComputerSystem`

Copyright and License
---------------------

Copyright (c) 2022 Lenovo

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.
