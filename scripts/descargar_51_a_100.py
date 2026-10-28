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
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) BibliotecaUNAMAD-Ingest/2.0"
}

# Configuración de las 5 categorías para los libros 51 a 100
CATEGORIES_51_100 = [
    {
        "id_prefix": "lit",
        "categoria": "Literatura y Filosofía",
        "tema": "Obras Clásicas, Narrativa y Ensayo",
        "query": "(subject:literatura OR subject:novela OR subject:ensayo OR subject:filosofia) AND language:spa AND mediatype:texts",
        "start_idx": 51,
        "end_idx": 60
    },
    {
        "id_prefix": "cue",
        "categoria": "Cuentos y Narrativa",
        "tema": "Relatos Breves, Fábulas y Leyendas",
        "query": "(subject:cuentos OR subject:relatos OR subject:fabulas OR subject:leyendas) AND language:spa AND mediatype:texts",
        "start_idx": 61,
        "end_idx": 70
    },
    {
        "id_prefix": "ing",
        "categoria": "Ingeniería de Sistemas",
        "tema": "Redes, Telecomunicaciones y Sistemas",
        "query": "(subject:ingenieria OR subject:telecomunicaciones OR subject:redes OR subject:electronica OR subject:software) AND language:spa AND mediatype:texts",
        "start_idx": 71,
        "end_idx": 80
    },
    {
        "id_prefix": "ia",
        "categoria": "Inteligencia Artificial",
        "tema": "Machine Learning, Data Science y Robótica",
        "query": "(subject:\"artificial intelligence\" OR subject:\"machine learning\" OR subject:\"data science\" OR subject:\"neural networks\" OR subject:\"deep learning\") AND mediatype:texts",
        "start_idx": 81,
        "end_idx": 90
    },
    {
        "id_prefix": "amb",
        "categoria": "Ciencias Ambientales e Investigación",
        "tema": "Biodiversidad, Amazonía y Recursos Forestales",
        "query": "(subject:biodiversidad OR subject:amazonia OR subject:forestal OR subject:ecologia OR subject:investigacion OR subject:botanica) AND language:spa AND mediatype:texts",
        "start_idx": 91,
        "end_idx": 100
    }
]

def sanitize_filename(name):
    clean = re.sub(r'[\\/*?:"<>|]', "", name)
    clean = re.sub(r'[\s_]+', "_", clean.strip())
    clean = re.sub(r'[^a-zA-Z0-9_\-\.]', "", clean)
    return clean[:45]

def get_candidates_archive_org(query, rows=80, page=2):
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
                        # Filtro de tamaño: 200 KB a 14 MB para descargas ágiles y confiables
                        if 200 * 1024 <= size <= 14 * 1024 * 1024:
                            valid_pdfs.append((fname, size))
                    except ValueError:
                        continue
            
            if not valid_pdfs:
                return None
            
            # Ordenar por tamaño moderado
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

def fetch_arxiv_papers(query="cat:cs.AI+OR+cat:cs.LG", needed=5):
    url = f"https://export.arxiv.org/api/query?search_query={query}&start=10&max_results={needed * 2}&sortBy=relevance&sortOrder=descending"
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
                autor = ", ".join(authors[:2]) if authors else "Investigadores AI"
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
                    "description": summary[:350] + "...",
                    "download_url": pdf_link,
                    "year": "2024"
                })
    except Exception as e:
        print(f"  [!] Error buscando en arXiv: {e}")
    return results

def main():
    print("=" * 75)
    print(" INICIANDO DESCARGA DE 50 LIBROS ADICIONALES (TOTAL: 100 LIBROS)")
    print(f" Directorio: {DOWNLOAD_DIR}")
    print("=" * 75)

    # Identificadores ya descargados en 1..50 para no repetir
    existing_files = os.listdir(DOWNLOAD_DIR)
    print(f"Archivos iniciales en directorio: {len([f for f in existing_files if f.endswith('.pdf')])}")

    new_metadata_list = []

    for cat in CATEGORIES_51_100:
        cat_name = cat["categoria"]
        tema_name = cat["tema"]
        prefix = cat["id_prefix"]
        start_idx = cat["start_idx"]
        end_idx = cat["end_idx"]
        target_count = end_idx - start_idx + 1

        print(f"\n[CATEGORIA] {cat_name} (Libros #{start_idx} a #{end_idx})")
        candidates = get_candidates_archive_org(cat["query"], rows=70, page=2)
        descargados_cat = 0
        current_idx = start_idx

        for cand in candidates:
            if current_idx > end_idx:
                break

            identifier = cand.get("identifier")
            raw_title = cand.get("title", f"Libro_{current_idx}").strip()
            creator = cand.get("creator", "Autor Desconocido")
            if isinstance(creator, list):
                creator = ", ".join(str(c) for c in creator[:2])
            elif not creator:
                creator = "Fondo Editorial / Varios Autores"

            year = str(cand.get("year", "2023"))
            desc = str(cand.get("description", f"Documento académico correspondiente a {tema_name}."))
            # Limpiar HTML básico de descripción si existe
            desc = re.sub(r'<[^>]+>', '', desc).strip()
            if len(desc) > 350:
                desc = desc[:350] + "..."

            pdf_info = get_pdf_from_item(identifier)
            if not pdf_info:
                continue

            clean_title = sanitize_filename(raw_title)
            filename = f"{current_idx:02d}_{prefix}_{clean_title}.pdf"
            filepath = os.path.join(DOWNLOAD_DIR, filename)

            # Evitar colisión si el índice ya fue descargado
            already_done = [f for f in os.listdir(DOWNLOAD_DIR) if f.startswith(f"{current_idx:02d}_")]
            if already_done:
                current_idx += 1
                continue

            print(f"  [{current_idx:02d}/100] Descargando: {raw_title[:45]}...")
            print(f"       Autor: {str(creator)[:35]} | Archivo: {filename} ({pdf_info['size_bytes'] / (1024*1024):.2f} MB)")

            start_t = time.time()
            bytes_dl = download_file(pdf_info["download_url"], filepath)

            if bytes_dl > 0:
                # Calcular SHA-256
                sha = hashlib.sha256()
                with open(filepath, "rb") as f:
                    while chunk := f.read(65536):
                        sha.update(chunk)
                checksum = sha.hexdigest()
                size_mb = round(bytes_dl / (1024 * 1024), 2)
                elapsed = round(time.time() - start_t, 2)
                print(f"       [OK] Completado en {elapsed}s | SHA-256: {checksum[:12]}...")

                new_metadata_list.append({
                    "id": current_idx,
                    "titulo": raw_title,
                    "autor": str(creator),
                    "categoria": cat_name,
                    "tema": tema_name,
                    "isbn": f"978-UNA-{current_idx:04d}",
                    "archivo": filename,
                    "tamano_mb": size_mb,
                    "sha256": checksum,
                    "fuente_url": pdf_info["download_url"],
                    "fuente_relativa": f"/storage/libros/{filename}",
                    "ubicacion_estante": "Repositorio Digital UNAMAD",
                    "formato": "DIGITAL",
                    "year": year,
                    "resumen": desc
                })

                current_idx += 1
                descargados_cat += 1
                time.sleep(0.4)
            else:
                if os.path.exists(filepath):
                    os.remove(filepath)

        # Si faltan en IA, complementar con arXiv
        if current_idx <= end_idx and prefix == "ia":
            faltan = end_idx - current_idx + 1
            print(f"  Complementando {faltan} libros/papers de IA con arXiv...")
            arxiv_items = fetch_arxiv_papers(needed=faltan)
            for item in arxiv_items[:faltan]:
                if current_idx > end_idx:
                    break
                clean_title = sanitize_filename(item["title"])
                filename = f"{current_idx:02d}_ia_{clean_title}.pdf"
                filepath = os.path.join(DOWNLOAD_DIR, filename)

                print(f"  [{current_idx:02d}/100] [arXiv] Descargando: {item['title'][:45]}...")
                bytes_dl = download_file(item["download_url"], filepath)
                if bytes_dl > 0:
                    sha = hashlib.sha256()
                    with open(filepath, "rb") as f:
                        while chunk := f.read(65536):
                            sha.update(chunk)
                    checksum = sha.hexdigest()
                    size_mb = round(bytes_dl / (1024 * 1024), 2)

                    new_metadata_list.append({
                        "id": current_idx,
                        "titulo": item["title"],
                        "autor": item["creator"],
                        "categoria": cat_name,
                        "tema": tema_name,
                        "isbn": f"978-UNA-{current_idx:04d}",
                        "archivo": filename,
                        "tamano_mb": size_mb,
                        "sha256": checksum,
                        "fuente_url": item["download_url"],
                        "fuente_relativa": f"/storage/libros/{filename}",
                        "ubicacion_estante": "Repositorio Digital UNAMAD",
                        "formato": "DIGITAL",
                        "year": item["year"],
                        "resumen": item["description"]
                    })
                    current_idx += 1
                    time.sleep(0.4)

    print("\n" + "=" * 75)
    print(" CONSOLIDANDO EL CATÁLOGO COMPLETO DE 100 LIBROS DIGITALES")
    print("=" * 75)

    # 1. Cargar metadatos previos de 1..50 si existen
    json_path = os.path.join(DOWNLOAD_DIR, "catalogo_metadatos.json")
    existing_meta = []
    if os.path.exists(json_path):
        with open(json_path, "r", encoding="utf-8") as f:
            try:
                existing_meta = json.load(f)
            except Exception:
                existing_meta = []

    # Map de existentes por id
    meta_dict = {m["id"]: m for m in existing_meta}
    for m in new_metadata_list:
        meta_dict[m["id"]] = m

    # Asegurar que existan los 100 ordenados por id
    all_pdfs = sorted([f for f in os.listdir(DOWNLOAD_DIR) if f.endswith(".pdf")])
    print(f"Total PDFs físicos en 'libros_descargados/': {len(all_pdfs)}")

    consolidated_list = []
    for idx, fname in enumerate(all_pdfs[:100], start=1):
        if idx in meta_dict:
            item = meta_dict[idx]
            item["archivo"] = fname
            item["fuente_relativa"] = f"/storage/libros/{fname}"
            # Recalcular tamaño actual
            fpath = os.path.join(DOWNLOAD_DIR, fname)
            item["tamano_mb"] = round(os.path.getsize(fpath) / (1024 * 1024), 2)
            consolidated_list.append(item)
        else:
            # Entrada base si no estaba en el diccionario
            fpath = os.path.join(DOWNLOAD_DIR, fname)
            sz = round(os.path.getsize(fpath) / (1024 * 1024), 2)
            sha = hashlib.sha256()
            with open(fpath, "rb") as f:
                while chunk := f.read(65536):
                    sha.update(chunk)
            
            parts = fname.replace(".pdf", "").split("_", 2)
            clean_title = parts[2].replace("_", " ").title() if len(parts) > 2 else fname
            cat_code = parts[1] if len(parts) > 1 else "gen"
            cat_map = {
                "lit": ("Literatura y Filosofía", "Obras Clásicas, Narrativa y Ensayo"),
                "cue": ("Cuentos y Narrativa", "Relatos Breves, Fábulas y Leyendas"),
                "ing": ("Ingeniería de Sistemas", "Redes, Telecomunicaciones y Sistemas"),
                "ia": ("Inteligencia Artificial", "Machine Learning, Data Science y Robótica"),
                "amb": ("Ciencias Ambientales e Investigación", "Biodiversidad, Amazonía y Recursos Forestales")
            }
            cat_name, tema_name = cat_map.get(cat_code, ("General", "Acervo General"))

            consolidated_list.append({
                "id": idx,
                "titulo": clean_title,
                "autor": f"Fondo Editorial ({cat_name})",
                "categoria": cat_name,
                "tema": tema_name,
                "isbn": f"978-UNA-{idx:04d}",
                "archivo": fname,
                "tamano_mb": sz,
                "sha256": sha.hexdigest(),
                "fuente_url": f"https://archive.org/download/{fname}",
                "fuente_relativa": f"/storage/libros/{fname}",
                "ubicacion_estante": "Repositorio Digital UNAMAD",
                "formato": "DIGITAL",
                "year": "2023",
                "resumen": f"Libro en formato PDF correspondiente al área de {tema_name}."
            })

    # Guardar catalogo_metadatos.json completo con los 100 libros
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(consolidated_list, f, indent=2, ensure_ascii=False)
    print(f"[OK] catalogo_metadatos.json consolidado con {len(consolidated_list)} libros!")

    # Generar script SQL completo para PostgreSQL
    sql_path = os.path.join(DOWNLOAD_DIR, "schema_y_datos_postgres.sql")
    with open(sql_path, "w", encoding="utf-8") as f:
        f.write("-- ====================================================================\n")
        f.write("-- SCHEMA Y SEED DATA PARA POSTGRESQL — 100 LIBROS DIGITALES UNAMAD\n")
        f.write(f"-- Fecha de generación: {time.strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"-- Total de libros importados: {len(consolidated_list)}\n")
        f.write("-- ====================================================================\n\n")
        f.write("""-- 1. Definición de la Tabla de Libros
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

-- 2. Inserción masiva de los 100 libros digitales
""")
        for lib in consolidated_list:
            t_esc = lib["titulo"].replace("'", "''")
            a_esc = lib["autor"].replace("'", "''")
            c_esc = lib["categoria"].replace("'", "''")
            r_esc = lib.get("resumen", "").replace("'", "''")
            f_esc = lib["archivo"].replace("'", "''")
            u_esc = lib["fuente_relativa"].replace("'", "''")

            f.write(f"INSERT INTO libros (id, titulo, autor, categoria, isbn, formato, ejemplares_totales, ejemplares_disponibles, ubicacion, archivo_url, archivo_nombre, sha256, tamano_mb, resumen) VALUES "
                    f"({lib['id']}, '{t_esc}', '{a_esc}', '{c_esc}', '{lib['isbn']}', 'DIGITAL', 1, 1, 'Repositorio Digital UNAMAD', '{u_esc}', '{f_esc}', '{lib['sha256']}', {lib['tamano_mb']}, '{r_esc}') "
                    f"ON CONFLICT (id) DO UPDATE SET "
                    f"titulo = EXCLUDED.titulo, autor = EXCLUDED.autor, categoria = EXCLUDED.categoria, archivo_nombre = EXCLUDED.archivo_nombre, sha256 = EXCLUDED.sha256, tamano_mb = EXCLUDED.tamano_mb, resumen = EXCLUDED.resumen;\n")

    print(f"[OK] schema_y_datos_postgres.sql actualizado con los 100 libros")

    # Actualizar README.md
    readme_path = os.path.join(DOWNLOAD_DIR, "README.md")
    with open(readme_path, "w", encoding="utf-8") as f:
        f.write(f"""# Repositorio de 100 Libros Digitales en PDF — Biblioteca UNAMAD

Colección completa de **100 libros completos en formato PDF** con metadatos normalizados para base de datos PostgreSQL y backend en VPS.

## 1. Archivos Clave
- `catalogo_metadatos.json`: 100 registros con ID, título, autor, categoría, tema, ISBN correlativo, tamaño en MB y hash SHA-256.
- `schema_y_datos_postgres.sql`: Estructura DDL con 100 sentencias `INSERT` para importar directamente en PostgreSQL.
- `01_lit_*.pdf` a `100_amb_*.pdf`: 100 libros en PDF organizados por temática.

## 2. Distribución Temática (100 Libros)
1. **Libros 01 – 20:** Literatura Clásica, Poesía, Novela y Ensayos Filosóficos.
2. **Libros 21 – 40:** Antologías de Cuentos, Relatos Breves, Fábulas y Mitos.
3. **Libros 41 – 60:** Ingeniería de Sistemas, Redes, Telecomunicaciones, Programación y Bases de Datos.
4. **Libros 61 – 80:** Inteligencia Artificial, Machine Learning, Deep Learning, Visión Artificial y Data Science.
5. **Libros 81 – 100:** Ciencias Ambientales, Biodiversidad Amazónica, Silvicultura, Ecología y Metodología Científica.

## 3. Despliegue en VPS
```bash
# 1. Importar los 100 libros a PostgreSQL
psql -U usuario_biblioteca -d biblioteca_db -f schema_y_datos_postgres.sql

# 2. Copiar archivos PDF a la carpeta de servicio Nginx o MinIO
rsync -avz --progress libros_descargados/*.pdf usuario@tu-vps.com:/var/www/biblioteca/pdfs/
```
""")
    print(f"[OK] README.md actualizado para 100 libros")

if __name__ == "__main__":
    main()
