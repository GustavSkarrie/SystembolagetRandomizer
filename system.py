import json
import requests
from time import sleep

data = []
headers = {
		'access-control-allow-origin': '*',
		'ocp-apim-subscription-key': 'cfc702aed3094c86b92d6d4ff7a54c84',
    'baseURL': 'https://api-systembolaget.azure-api.net/sb-api-ecommerce/v1',
    'User-Agent': 'curl/7.54.0',  # No idea why the default 'python-requests/2.28.2' doesn't work. Bad scraping protection?
}

try:
  for i in range(0, 1000):
    response = requests.get(f'https://api-extern.systembolaget.se/sb-api-ecommerce/v1/productsearch/search?size=30&page={i}', headers=headers)
    data.extend(response.json()['products'])
    print(str(i + 1) + "/1000", flush=True)
    sleep(0.1)
finally:
  with open('data.json', 'w', encoding='utf-8') as file:
      json.dump(data, file, ensure_ascii=False, indent=4)