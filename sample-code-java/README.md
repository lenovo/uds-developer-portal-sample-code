# UDS Developer Portal Sample Code using UDS API

More information and API specs can be found at Developer Portal.

Must have - Lenovo ID account and an Account name!

### Steps to run the code

1. Create a credentials.txt file in the src/main/java/com/company folder with your Lenovo ID and password.
   1. Lenovo ID in the first line and password in the second.
2. Put your information on Main class file, in the *getOrganization* method.
   1. Change the environment URL, account name, client ID and client secret to your own information. **OBS: The Client ID and Secret can be created at My Apps on Developer Portal**.
3. Put the desired Device information in the Main class file, in the *getCustomDevice* and *getClaimDevice* methods.
4. Run the *main* method on Main class file.

### Tasks to get used to UDS API - all done in this script
1. Create .credentials.txt file in 'src/main/java/com/company', and input Lenovo ID account username on line 1, and Lenovo ID pass on second line.
2. Get Authorization token A using Lenovo ID account username, password, and organization ID.
3. (Token A) Use informed Client ID and Secret to retrieve token.
    1. This will return client information, such as client secret
4. (Token A) Get subscription ID via /core/account-management/v1/organizations/{your-org-Name}
5. Get new token B with the clientID and client secret credential
6. (Token B) Create device in organization
7. (Token B) Get number of devices in the organization using device profile service API
8. (Token B) Get number of devices in the organization using device profile service API
9. (Token B) Get number of users in the organization
10. (Token B) Get number of Applications in the organization
11. (Token B) Claim your device (check hints below)
     1. Verify UDC is installed in the machine
     2. Claim device using developer portal API
         1. Will need to run script as administrator if you want files saved into root
         2. If not, save the files to any subdirectory, and manually move them into the root folder.
     4. restart the UDC client in Windows Services
     5. Verify that device is active in UDS portal
12. (Token B) API call to collect device information

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
