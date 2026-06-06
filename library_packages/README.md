# Library package upload format

Place downloadable library packages in this repository branch and add each package to `manifest.json`.

Supported package types:

- `library`: text/page based books.
- `library_pdf`: PDF based books.

Each package must be a `.db.zip` file containing one SQLite database with:

```sql
CREATE TABLE package_meta(
  packageId TEXT PRIMARY KEY,
  type TEXT NOT NULL,
  name TEXT NOT NULL,
  version INTEGER NOT NULL,
  generatedAt TEXT NOT NULL
);

CREATE TABLE library_books(
  bookId TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  author TEXT NOT NULL,
  authorDeathHijri INTEGER,
  category TEXT NOT NULL,
  description TEXT NOT NULL,
  totalPages INTEGER NOT NULL,
  packageId TEXT NOT NULL,
  format TEXT NOT NULL DEFAULT 'text',
  fileName TEXT NOT NULL DEFAULT ''
);

CREATE TABLE library_pages(
  id INTEGER PRIMARY KEY,
  bookId TEXT NOT NULL,
  pageNumber INTEGER NOT NULL,
  pageContent TEXT NOT NULL
);
```

Manifest entry example:

```json
{
  "id": "library_example_v1",
  "name": "مكتبة المثال",
  "type": "library",
  "version": 1,
  "rowCount": 1200,
  "sizeBytes": 1234567,
  "sha256": "ZIP_SHA256",
  "fileName": "library_example_v1.db.zip",
  "url": "https://raw.githubusercontent.com/aliabd97/qurann1/packages-v1/library_example_v1.db.zip"
}
```

After the manifest is updated, the app will show the package in `حزم التنزيل`; after install, its books appear in `المكتبة`.
