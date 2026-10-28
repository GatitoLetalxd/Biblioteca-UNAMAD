import os
import sys
import json
import time
import hashlib
import re
import urllib.request
import urllib.parse

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

def get_candidates_archive_org(query, rows=60, page=1):
    encoded_query = urllib.parse.quote(query)
    url = f"https://archive.org/advancedsearch.php?q={encoded_query}&fl[]=identifier,title,creator,description,year&sort[]=downloads+desc&rows={rows}&page={page}&output=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return data.get("response", {}).get("docs", [])
    except Exception as e:
        print(f"  [!] Error buscando en Archive.org: {e}")
        return []

def get_pdf_from_item(identifier):
    url = f"https://archive.org/metadata/{identifier}"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=12) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            files = data.get("files", [])
            metadata = data.get("metadata", {})
            
            valid_pdfs = []
            for f in files:
                fname = f.get("name", "")
                if fname.lower().endswith(".pdf"):
                    try:
                        size = int(f.get("size", 0))
                        # Tamaño ideal para rapidez: 200 KB a 7 MB
                        if 200 * 1024 <= size <= 7 * 1024 * 1024:
                            valid_pdfs.append((fname, size))
                    except ValueError:
                        continue
            
            if not valid_pdfs:
                return None
            
            valid_pdfs.sort(key=lambda x: x[1])
            chosen_name, chosen_size = valid_pdfs[0]
            
            encoded_name = urllib.parse.quote(chosen_name)
            download_url = f"https://archive.org/download/{identifier}/{encoded_name}"
            
            return {
                "download_url": download_url,
                "file_name": chosen_name,
                "size_bytes": chosen_size,
                "metadata": metadata
            }
    except Exception as e:
        return None

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

def download_environmental_books(start_idx=91, end_idx=100):
    needed = end_idx - start_idx + 1
    print(f"\n[CATEGORIA] Ciencias Ambientales e Investigaciones (Libros #{start_idx} a #{end_idx})...")
    
    queries = [
        "(subject:amazonia OR subject:selva OR subject:biodiversidad) AND language:spa AND mediatype:texts",
        "(subject:forestal OR subject:ecologia OR subject:botanica) AND language:spa AND mediatype:texts",
        "(subject:investigacion OR subject:metodologia) AND language:spa AND mediatype:texts"
    ]
    
    current_idx = start_idx
    for q in queries:
        if current_idx > end_idx:
            break
        candidates = get_candidates_archive_org(q, rows=40, page=1)
        for cand in candidates:
            if current_idx > end_idx:
                break
            
            identifier = cand.get("identifier")
            raw_title = cand.get("title", f"Libro_{current_idx}").strip()
            creator = cand.get("creator", "Investigadores Ambientales")
            if isinstance(creator, list):
                creator = ", ".join(str(c) for c in creator[:2])
                
            pdf_info = get_pdf_from_item(identifier)
            if not pdf_info:
                continue
                
            clean_title = sanitize_filename(raw_title)
            filename = f"{current_idx:02d}_amb_{clean_title}.pdf"
            filepath = os.path.join(DOWNLOAD_DIR, filename)
            
            # Si ya existe, avanzar
            if os.path.exists(filepath):
                current_idx += 1
                continue
                
            print(f"  [{current_idx:02d}/100] Descargando: {raw_title[:45]}...")
            print(f"       Archivo: {filename} ({pdf_info['size_bytes'] / (1024*1024):.2f} MB)")
            
            start_t = time.time()
            bytes_dl = download_file(pdf_info["download_url"], filepath)
            if bytes_dl > 0:
                elapsed = round(time.time() - start_t, 2)
                print(f"       [OK] Completado en {elapsed}s")
                current_idx += 1
                time.sleep(0.3)
            else:
                if os.path.exists(filepath):
                    os.remove(filepath)

if __name__ == "__main__":
    download_environmental_books(91, 100)
