

import json
import requests
import urllib3
from requests.auth import HTTPBasicAuth
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

dnac = {
    "host": "sandboxdnac2.cisco.com",
    "port": 443,
    "username": "devnetuser",
    "password": "Cisco123!",
    "devicename": "3504_WLC"

}

headers = {
              'content-type': "application/json",
              'x-auth-token': ""
          }

def dnac_login(host, username, password):
    url = "https://{}/api/system/v1/auth/token".format(host)
    response = requests.request("POST", url, auth=HTTPBasicAuth(username, password),
                                headers=headers, verify=False)
    return response.json()["Token"]


def network_device_detail(dnac, token):
    url = "https://{}/api/v1/network-device".format(dnac['host'])
    payload = {}
    headers = {
        'x-auth-token': token,
        'searchBy': dnac["devicename"],
        'identifier': 'nwDeviceName'
    }
    response = requests.request("GET", url, headers=headers, data = payload)
    return response.json()

login = dnac_login(dnac["host"], dnac["username"], dnac["password"])

print(network_device_detail(dnac, login))




