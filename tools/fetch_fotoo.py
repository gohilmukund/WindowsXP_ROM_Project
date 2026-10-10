import urllib.request
import re
import sys

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}

url = 'https://apkpure.com/fotoo-photo-frame-slideshow/com.bo.fotoo/versions'
req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req, timeout=10) as resp:
        html = resp.read().decode('utf-8', errors='ignore')
        matches = re.findall(r'href="([^"]+/download/[^"]+)"', html)
        print("Matches:", len(matches))
        for m in matches[:10]:
            print("Link:", m)
except Exception as e:
    print("Error:", e)
