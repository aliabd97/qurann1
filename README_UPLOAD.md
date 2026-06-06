# Quran Package Upload

Generated package files in this folder are ready for GitHub Releases.

1. Create a public GitHub repository named `quran-packages`.
2. Create a release with tag `packages-v1`.
3. Upload all files from this folder to that release:
   - `manifest.json`
   - every `tafsir_*.db.zip`
4. Rebuild the app after changing:

```kotlin
// qurann1/app/src/main/java/com/example/data/PackageDownloadConfig.kt
const val MANIFEST_URL = "https://github.com/YOUR_GITHUB_NAME/quran-packages/releases/download/packages-v1/manifest.json"
```

The app downloads each ZIP, verifies its SHA-256 from `manifest.json`, extracts the SQLite database, then imports `tafsir_texts` into the local Room database.
