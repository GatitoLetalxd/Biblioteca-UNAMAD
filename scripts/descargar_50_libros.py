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
os.makedirs(DOWNLOAD_DIR, exist_ok=True)

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) BibliotecaUNAMAD-Ingest/1.0"
}

CATEGORIES = [
    {
        "id_prefix": "ia",
        "categoria": "Inteligencia Artificial",
        "query": "(subject:\"artificial intelligence\" OR subject:\"machine learning\" OR subject:\"deep learning\") AND mediatype:texts",
        "meta_cat": "Inteligencia Artificial",
        "start_idx": 38,
        "end_idx": 40
    },
    {
        "id_prefix": "amb",
        "categoria": "Medio Ambiente e Investigaciones",
        "query": "(subject:\"medio ambiente\" OR subject:forestal OR subject:biodiversidad OR subject:amazonia OR subject:ecologia OR subject:investigacion) AND language:spa AND mediatype:texts",
        "meta_cat": "Ciencias Ambientales e Investigación",
        "start_idx": 41,
        "end_idx": 50
    }
]

def sanitize_filename(name):
    clean = re.sub(r'[\\/*?:"<>|]', "", name)
    clean = re.sub(r'[\s_]+', "_", clean.strip())
    clean = re.sub(r'[^a-zA-Z0-9_\-\.]', "", clean)
    return clean[:45]

def get_candidates_archive_org(query, rows=60):
    encoded_query = urllib.parse.quote(query)
    url = f"https://archive.org/advancedsearch.php?q={encoded_query}&fl[]=identifier,title,creator,description,year&sort[]=downloads+desc&rows={rows}&page=1&output=json"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            return data.get("response", {}).get("docs", [])
    except Exception as e:
        print(f"  [!] Error buscando en Archive.org ({query[:30]}...): {e}")
        return []

def get_pdf_from_item(identifier):
    url = f"https://archive.org/metadata/{identifier}"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            files = data.get("files", [])
            metadata = data.get("metadata", {})
            
            valid_pdfs = []
            for f in files:
                fname = f.get("name", "")
                if fname.lower().endswith(".pdf"):
                    try:
                        size = int(f.get("size", 0))
                        # Filtro de tamaño: 200 KB a 15 MB
                        if 200 * 1024 <= size <= 15 * 1024 * 1024:
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
        with urllib.request.urlopen(req, timeout=30) as resp:
            content = resp.read()
            with open(target_path, "wb") as f:
                f.write(content)
            return len(content)
    except Exception as e:
        print(f"      [Error descargando]: {e}")
        return 0

def fetch_arxiv_ai_papers(needed_count=5):
    url = f"https://export.arxiv.org/api/query?search_query=cat:cs.AI+OR+cat:cs.LG&start=0&max_results={needed_count * 2}&sortBy=relevance&sortOrder=descending"
    req = urllib.request.Request(url, headers=HEADERS)
    results = []
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            xml_text = resp.read().decode("utf-8")
            import xml.etree.ElementTree as ET
            root = ET.fromstring(xml_text)
            ns = {"atom": "http://www.w3.org/2005/Atom"}
            for entry in root.findall("atom:entry", ns):
                title = entry.find("atom:title", ns).text.strip().replace("\n", " ")
                summary = entry.find("atom:summary", ns).text.strip().replace("\n", " ")
                authors = [a.find("atom:name", ns).text for a in entry.findall("atom:author", ns)]
                autor = ", ".join(authors[:2])
                pdf_link = None
                for link in entry.findall("atom:link", ns):
                    if link.get("title") == "pdf" or link.get("type") == "application/pdf":
                        pdf_link = link.get("href")
                        break
                if not pdf_link:
                    id_elem = entry.find("atom:id", ns).text
                    pdf_link = id_elem.replace("abs", "pdf") + ".pdf"
                
                results.append({
                    "title": title,
                    "creator": autor,
                    "description": summary[:300] + "...",
                    "download_url": pdf_link,
                    "year": "2024"
                })
    except Exception as e:
        print(f"  [!] Error buscando en arXiv: {e}")
    return results

def resume_downloads():
    print("=" * 70)
    print(" REANUDANDO DESCARGAS PARA COMPLETAR LOS 50 LIBROS EN PDF")
    print("=" * 70)
    
    # Comprobar qué archivos existen actualmente
    existing_files = sorted([f for f in os.listdir(DOWNLOAD_DIR) if f.endswith(".pdf")])
    print(f"Libros ya descargados en disco: {len(existing_files)} / 50")
    
    global_index = len(existing_files) + 1
    
    for cat_info in CATEGORIES:
        if global_index > 50:
            break
            
        cat_name = cat_info["categoria"]
        meta_cat = cat_info["meta_cat"]
        start_idx = cat_info["start_idx"]
        end_idx = cat_info["end_idx"]
        needed = (end_idx - max(start_idx, global_index)) + 1
        
        if needed <= 0:
            continue
            
        print(f"\n[CATEGORIA] {cat_name} (Descargando libros del #{global_index} al #{end_idx})")
        candidates = get_candidates_archive_org(cat_info["query"], rows=60)
        
        descargados_cat = 0
        for cand in candidates:
            if global_index > end_idx or global_index > 50:
                break
                
            identifier = cand.get("identifier")
            raw_title = cand.get("title", f"Libro_{global_index}")
            creator = cand.get("creator", "Autor Desconocido")
            if isinstance(creator, list):
                creator = ", ".join(creator[:2])
                
            pdf_info = get_pdf_from_item(identifier)
            if not pdf_info:
                continue
                
            clean_title = sanitize_filename(raw_title)
            filename = f"{global_index:02d}_{cat_info['id_prefix']}_{clean_title}.pdf"
            filepath = os.path.join(DOWNLOAD_DIR, filename)
            
            if os.path.exists(filepath):
                global_index += 1
                continue
                
            print(f"  [{global_index:02d}/50] Descargando: {raw_title[:45]}...")
            print(f"       Archivo: {filename} ({pdf_info['size_bytes'] / (1024*1024):.2f} MB)")
            
            start_t = time.time()
            bytes_dl = download_file(pdf_info["download_url"], filepath)
            if bytes_dl > 0:
                elapsed = round(time.time() - start_t, 2)
                print(f"       [OK] Completado en {elapsed}s")
                global_index += 1
                descargados_cat += 1
                time.sleep(0.4)
            else:
                if os.path.exists(filepath):
                    os.remove(filepath)
                    
        # Si faltan en IA, usar arXiv
        if global_index <= end_idx and cat_info["id_prefix"] == "ia":
            faltan = end_idx - global_index + 1
            print(f"  Complementando {faltan} con arXiv...")
            for item in fetch_arxiv_ai_papers(needed_count=faltan)[:faltan]:
                clean_title = sanitize_filename(item["title"])
                filename = f"{global_index:02d}_ia_{clean_title}.pdf"
                filepath = os.path.join(DOWNLOAD_DIR, filename)
                print(f"  [{global_index:02d}/50] [arXiv] Descargando: {item['title'][:45]}...")
                bytes_dl = download_file(item["download_url"], filepath)
                if bytes_dl > 0:
                    global_index += 1
                    time.sleep(0.4)

    # Ahora construir el catálogo completo de metadatos de los 50 libros en disco
    print("\n" + "=" * 70)
    print(" GENERANDO METADATOS Y SCHEMA PARA POSTGRESQL")
    print("=" * 70)
    
    all_pdfs = sorted([f for f in os.listdir(DOWNLOAD_DIR) if f.endswith(".pdf")])
    print(f"Total PDFs encontrados en disco: {len(all_pdfs)}")
    
    libros_totales = []
    for idx, fname in enumerate(all_pdfs[:50], start=1):
        fpath = os.path.join(DOWNLOAD_DIR, fname)
        size_bytes = os.path.getsize(fpath)
        size_mb = round(size_bytes / (1024 * 1024), 2)
        
        # Calcular SHA-256
        sha = hashlib.sha256()
        with open(fpath, "rb") as f:
            while chunk := f.read(65536):
                sha.update(chunk)
        checksum = sha.hexdigest()
        
        # Determinar categoría y título a partir del nombre
        parts = fname.replace(".pdf", "").split("_", 2)
        prefix = parts[1] if len(parts) > 1 else "gen"
        raw_name = parts[2] if len(parts) > 2 else fname
        readable_title = raw_name.replace("_", " ").title()
        
        cat_map = {
            "lit": ("Literatura", "Literatura Clásica y Contemporánea"),
            "cue": ("Cuentos", "Antología de Cuentos y Fábulas"),
            "ing": ("Ingeniería de Sistemas", "Ingeniería y Ciencias de la Computación"),
            "ia": ("Inteligencia Artificial", "Inteligencia Artificial y Aprendizaje Automático"),
            "amb": ("Ciencias Ambientales e Investigación", "Ciencias Ambientales, Forestal y Metodología")
        }
        categoria, tema_completo = cat_map.get(prefix, ("General", "Acervo General"))
        
        libros_totales.append({
            "id": idx,
            "titulo": readable_title,
            "autor": f"Autor / Colección ({categoria})",
            "categoria": categoria,
            "tema": tema_completo,
            "isbn": f"978-UNA-{idx:04d}",
            "archivo": fname,
            "tamano_mb": size_mb,
            "sha256": checksum,
            "fuente_relativa": f"/storage/libros/{fname}",
            "ubicacion_estante": "Repositorio Digital UNAMAD",
            "formato": "DIGITAL"
        })

    # Guardar manifiesto JSON
    json_path = os.path.join(DOWNLOAD_DIR, "catalogo_metadatos.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(libros_totales, f, indent=2, ensure_ascii=False)
    print(f"[OK] catalogo_metadatos.json generado ({len(libros_totales)} libros)")

    # Guardar script SQL PostgreSQL
    sql_path = os.path.join(DOWNLOAD_DIR, "schema_y_datos_postgres.sql")
    with open(sql_path, "w", encoding="utf-8") as f:
        f.write("-- ====================================================================\n")
        f.write("-- DDL Y SEED DATA PARA POSTGRESQL — BIBLIOTECA DIGITAL UNAMAD\n")
        f.write(f"-- Fecha de generacion: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"-- Total de libros importados: {len(libros_totales)}\n")
        f.write("-- ====================================================================\n\n")
        f.write("""-- 1. Tabla de Libros
CREATE TABLE IF NOT EXISTS libros (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    isbn VARCHAR(50) UNIQUE NOT NULL,
    formato VARCHAR(20) DEFAULT 'DIGITAL',
    ejemplares_totales INT DEFAULT 1,
    ejemplares_disponibles INT DEFAULT 1,
    ubicacion VARCHAR(100) DEFAULT 'Repositorio Digital UNAMAD',
    archivo_url VARCHAR(500) NOT NULL,
    archivo_nombre VARCHAR(255) NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    tamano_mb NUMERIC(6, 2) NOT NULL,
    resumen TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_libros_categoria ON libros(categoria);
CREATE INDEX IF NOT EXISTS idx_libros_titulo ON libros(titulo);
CREATE INDEX IF NOT EXISTS idx_libros_sha256 ON libros(sha256);

-- 2. Insercion masiva de los 50 libros digitales
""")
        for lib in libros_totales:
            t_esc = lib["titulo"].replace("'", "''")
            a_esc = lib["autor"].replace("'", "''")
            c_esc = lib["categoria"].replace("'", "''")
            f_esc = lib["archivo"].replace("'", "''")
            u_esc = lib["fuente_relativa"].replace("'", "''")
            r_esc = f"Libro en formato digital PDF correspondiente al area de {lib['tema']}."
            
            f.write(f"INSERT INTO libros (id, titulo, autor, categoria, isbn, formato, ejemplares_totales, ejemplares_disponibles, ubicacion, archivo_url, archivo_nombre, sha256, tamano_mb, resumen) VALUES "
                    f"({lib['id']}, '{t_esc}', '{a_esc}', '{c_esc}', '{lib['isbn']}', 'DIGITAL', 1, 1, 'Repositorio Digital UNAMAD', '{u_esc}', '{f_esc}', '{lib['sha256']}', {lib['tamano_mb']}, '{r_esc}') "
                    f"ON CONFLICT (id) DO NOTHING;\n")

    print(f"[OK] schema_y_datos_postgres.sql generado")

    # Guardar README explicativo
    readme_path = os.path.join(DOWNLOAD_DIR, "README.md")
    with open(readme_path, "w", encoding="utf-8") as f:
        f.write("""# Acervo de 50 Libros Digitales en PDF — Biblioteca UNAMAD

Directorio de prueba y producción inicial con **50 libros completos en formato PDF**, cubriendo Literatura, Cuentos, Ingeniería, Inteligencia Artificial y Medio Ambiente.

## 1. Estructura de Archivos
- `catalogo_metadatos.json`: Índice en JSON con metadatos de los 50 libros (título, autor, categoría, tamaño en MB, hash SHA-256).
- `schema_y_datos_postgres.sql`: Estructura DDL e instrucciones `INSERT` para poblar PostgreSQL en el VPS.
- `01_lit_*.pdf` a `10_lit_*.pdf`: 10 obras de Literatura.
- `11_cue_*.pdf` a `20_cue_*.pdf`: 10 colecciones de Cuentos y Fábulas.
- `21_ing_*.pdf` a `30_ing_*.pdf`: 10 libros de Ingeniería y Software.
- `31_ia_*.pdf` a `40_ia_*.pdf`: 10 libros e investigaciones de Inteligencia Artificial.
- `41_amb_*.pdf` a `50_amb_*.pdf`: 10 libros de Medio Ambiente, Amazonía y Metodología.

## 2. Cómo Almacenarlo en tu VPS

### A. Base de Datos (PostgreSQL)
Importar el archivo SQL directamente en tu base de datos:
```bash
psql -U usuario_biblioteca -d biblioteca_db -f schema_y_datos_postgres.sql
```

### B. Archivos Binarios (PDFs)
1. **Opción Recomendada (MinIO / S3):**
   - Levantar un contenedor Docker con MinIO.
   - Crear el bucket `libros-unamad`.
   - Subir todos los archivos `.pdf` de este directorio al bucket.
   - En PostgreSQL, la columna `archivo_url` apunta a `https://tu-vps.com/storage/libros/{archivo_nombre}`.

2. **Opción Directa (Nginx Estático con Docker):**
   - Montar esta carpeta en `/var/www/biblioteca/pdfs`.
   - Nginx sirve los PDFs con compresión gzip/brotli y cabeceras de caché:
     ```nginx
     location /storage/libros/ {
         alias /var/www/biblioteca/pdfs/;
         add_header Content-Disposition inline;
         add_header Cache-Control "public, max-age=86400";
     }
     ```
""")
    print(f"[OK] README.md generado")

if __name__ == "__main__":
    resume_downloads()
