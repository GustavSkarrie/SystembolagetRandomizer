import json
import requests
import time

start = time.time()

data = []
headers = {
    'baseURL': 'https://api-systembolaget.azure-api.net/sb-api-ecommerce/v1',
    'User-Agent': 'curl/7.54.0',  # No idea why the default 'python-requests/2.28.2' doesn't work. Bad scraping protection?
}

try:
  for i in range(0, 10):
	  response = requests.get(f'https://www.systembolaget.se/api/gateway/productsearch/search/?page={i}', headers=headers)
	  data.extend(response.json()['products'])
	  time.sleep(0.1)
finally:
  with open('data.json', 'w', encoding='utf-8') as file:
      json.dump(data, file, ensure_ascii=False, indent=4)

end = time.time()
print(end - start)