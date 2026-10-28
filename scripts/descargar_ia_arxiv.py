import os
import sys
import json
import time
import hashlib
import re
import urllib.request
import urllib.parse
import xml.etree.ElementTree as ET

if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding='utf-8', errors='replace')
        sys.stderr.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
DOWNLOAD_DIR = os.path.join(ROOT_DIR, "libros_descargados")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) BibliotecaUNAMAD-Ingest/2.0"
}

def sanitize_filename(name):
    clean = re.sub(r'[\\/*?:"<>|]', "", name)
    clean = re.sub(r'[\s_]+', "_", clean.strip())
    clean = re.sub(r'[^a-zA-Z0-9_\-\.]', "", clean)
    return clean[:45]

def download_file(url, target_path):
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=25) as resp:
            content = resp.read()
            with open(target_path, "wb") as f:
                f.write(content)
            return len(content)
    except Exception as e:
        print(f"      [Error descargando]: {e}")
        return 0

def fetch_arxiv_ai(start_idx=81, end_idx=90):
    needed = end_idx - start_idx + 1
    print(f"\n[CATEGORIA] Inteligencia Artificial (Descargando libros #{start_idx} a #{end_idx} desde arXiv)...")
    url = f"https://export.arxiv.org/api/query?search_query=cat:cs.AI+OR+cat:cs.LG+OR+cat:cs.CV&start=0&max_results={needed + 5}&sortBy=relevance&sortOrder=descending"
    req = urllib.request.Request(url, headers=HEADERS)
    
    with urllib.request.urlopen(req, timeout=15) as resp:
        xml_text = resp.read().decode("utf-8")
        root = ET.fromstring(xml_text)
        ns = {"atom": "http://www.w3.org/2005/Atom"}
        
        current_idx = start_idx
        for entry in root.findall("atom:entry", ns):
            if current_idx > end_idx:
                break
                
            title = entry.find("atom:title", ns).text.strip().replace("\n", " ")
            summary = entry.find("atom:summary", ns).text.strip().replace("\n", " ")
            authors = [a.find("atom:name", ns).text for a in entry.findall("atom:author", ns)]
            autor = ", ".join(authors[:2]) if authors else "Investigadores AI"
            
            pdf_link = None
            for link in entry.findall("atom:link", ns):
                if link.get("title") == "pdf" or link.get("type") == "application/pdf":
                    pdf_link = link.get("href")
                    break
            if not pdf_link:
                id_elem = entry.find("atom:id", ns).text
                pdf_link = id_elem.replace("abs", "pdf") + ".pdf"
            
            clean_title = sanitize_filename(title)
            filename = f"{current_idx:02d}_ia_{clean_title}.pdf"
            filepath = os.path.join(DOWNLOAD_DIR, filename)
            
            if os.path.exists(filepath):
                current_idx += 1
                continue
                
            print(f"  [{current_idx:02d}/100] [arXiv] {title[:50]}...")
            start_t = time.time()
            bytes_dl = download_file(pdf_link, filepath)
            if bytes_dl > 0:
                elapsed = round(time.time() - start_t, 2)
                sz_mb = round(bytes_dl / (1024 * 1024), 2)
                print(f"       [OK] Descargado ({sz_mb} MB en {elapsed}s)")
                current_idx += 1
                time.sleep(0.3)

if __name__ == "__main__":
    fetch_arxiv_ai(81, 90)
