# Repositorio de 100 Libros Digitales en PDF — Biblioteca UNAMAD

Colección completa de **100 libros en formato PDF** con catálogo relacional para PostgreSQL y backend en VPS.

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
