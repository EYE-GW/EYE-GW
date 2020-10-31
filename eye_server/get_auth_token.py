
import json
import requests
import urllib3
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
from requests.auth import HTTPBasicAuth

def get_auth_token():

    url = "https://sandboxdnac2.cisco.com/dna/system/api/v1/auth/token"
    headers = {'content-type': 'application/json'}
    resp = requests.post(url=url, auth=HTTPBasicAuth(username='devnetuser', password='Cisco123!'), headers=headers,verify=False)
    token = resp.json()['Token']
    return token

print (get_auth_token())
