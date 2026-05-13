# GitHub Releases — APK Admin (Livria)

Al pushear un tag **`v*`** (ej. `v1.0.0`), el workflow **Release Admin APK** compila con Gradle (`assembleRelease`) y sube el APK al **Release** de GitHub.

## Requisitos (una vez)

1. Código del repo en GitHub.
2. **Settings → Actions → General → Workflow permissions** → **Read and write** (si la organización lo permite). Si no, usá un PAT en un secret y `token: ${{ secrets.GH_RELEASE_TOKEN }}` en el paso del release (igual que en livria-user).
3. Si la org restringe acciones externas, permití **`softprops/action-gh-release`**, **`android-actions/setup-android`** y las de **`actions/*`** que use el workflow.

## Si el workflow falla

1. **Sin Android SDK en el runner:** el workflow instala el SDK con `android-actions/setup-android`. Si la org **no permite** esa acción, el job fallará: pedí que la añadan a la lista permitida.
2. **`google-services.json`:** debe existir en el repo en `app/google-services.json` (o generarlo en CI con un secret en base64).
3. **Token solo lectura:** usá PAT `GH_RELEASE_TOKEN` y en el YAML del paso *GitHub Release* → `token: ${{ secrets.GH_RELEASE_TOKEN }}`.
4. **Probar sin tag:** **Actions** → **Release Admin APK** → **Run workflow** (`workflow_dispatch`).

## Publicar una versión

1. (Opcional) Subí `versionCode` / `versionName` en `app/build.gradle.kts`.
2. Commiteá y pusheá.
3. Tag y push:

   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```

4. **Actions** → comprobar que **Release Admin APK** terminó en verde.
5. **Releases** → descargar **`livria-admin-v1.0.1.apk`**.

## APK “unsigned”

Si el `release` no tiene `signingConfig`, el artefacto puede llamarse `app-release-unsigned.apk`; el workflow copia el `.apk` que haya en `app/build/outputs/apk/release/`. Para Play Store o firma seria, configurá keystore en Gradle.

## Local (sin GitHub)

```powershell
.\gradlew.bat assembleRelease
```

Salida típica: `app\build\outputs\apk\release\`.
