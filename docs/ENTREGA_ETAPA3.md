# Entrega Etapa 3 — AdoptMe MVP

**Fecha:** 27-11-2026  
**Versión:** `0.1-mvp`  
**Repositorio:** https://github.com/Madaboy/AdoptMe  
**Release:** https://github.com/Madaboy/AdoptMe/releases/tag/v0.1-mvp-etapa3

## Archivos de la entrega

| Archivo | Descripción |
| :--- | :--- |
| `AdoptMe_Etapa3.apk` | APK de prueba firmado con clave de desarrollo. Instalable en Android 8.0 o superior. |
| `AdoptMe_Integrativa_Etapa3.pptx` | Presentación corregida de la Etapa 3. |
| `AdoptMe_Integrativa_Etapa3.pdf` | Copia PDF de la presentación. |
| `AdoptMe_Usabilidad_3_Puntos.pptx` | Presentación breve de los 3 puntos de usabilidad. |
| `AdoptMe_Usabilidad_3_Puntos.pdf` | Copia PDF de la presentación de usabilidad. |

La copia local de estos archivos queda en la carpeta de la asignatura. Los archivos también están adjuntos en la release de GitHub.

## Instalación del APK

1. Descargar `AdoptMe_Etapa3.apk`.
2. En el dispositivo, permitir la instalación desde fuentes desconocidas para la app de archivos o WhatsApp.
3. Abrir el APK y confirmar la instalación.
4. La app requiere Internet para consultar el catálogo de Huachitos.cl y cargar mapas.

> El APK es una compilación **debug** firmada con la clave de desarrollo. Sirve para pruebas y demostración; no es una versión de producción ni está publicada en Google Play.

## Cambios incorporados en la Etapa 3

- APK funcional del MVP para pruebas en dispositivo.
- Presentación de Etapa 3 corregida y completada.
- Se agregó la captura real de la app en la lámina de Solución.
- Se completó la tabla de versiones reales del proyecto.
- Se completó la fila de pruebas unitarias: **JUnit 4.13.2, 17 pruebas**.
- Se agregó una lámina de evidencia con el reporte real de Gradle.
- Se reemplazaron los placeholders de los flujos 1 y 2 por capturas reales de la app.
- Se corrigieron afirmaciones que no correspondían al MVP: Supabase, AdMob, planes pagados, publicidad, filtros y contacto directo.
- Se aclaró que el Lean Canvas es una hipótesis de negocio y que el MVP es gratuito, sin publicidad ni backend.
- Se actualizó el documento para indicar que la base de datos de AdoptMe se conectará en la siguiente etapa.

## Pruebas unitarias

Resultado real de `./gradlew testDebugUnitTest`:

- **17 pruebas**
- **0 fallos**
- **100% de éxito**
- **JUnit 4.13.2**

| Suite | Pruebas |
| :--- | ---: |
| `DogRepositoryTest` | 6 |
| `HuachitosMapperTest` | 4 |
| `LocalDataSourcePersistenceTest` | 3 |
| `GalleryParserTest` | 2 |
| `LocationProviderTest` | 2 |

Reporte local: `app/build/reports/tests/testDebugUnitTest/index.html`.

## Versiones principales

| Componente | Versión |
| :--- | :--- |
| Android mínimo | API 26 (Android 8.0) |
| Android objetivo / compilación | API 35 (Android 15) |
| Kotlin | 2.0.0 |
| Jetpack Compose BOM | 2024.10.01 |
| DataStore Preferences | 1.1.1 |
| Retrofit + Gson | 2.11.0 |
| MapLibre | 11.8.1 |
| JUnit | 4.13.2 |
| Git / repositorio | GitHub: `Madaboy/AdoptMe` |

## Alcance y limitaciones

- Persistencia local con DataStore; la base de datos de AdoptMe, Room/SQLite y el backend se incorporarán en la siguiente etapa.
- Google Sign-In es una demostración visual; no hay OAuth real.
- No hay publicidad, analytics, Crashlytics ni planes pagados en el MVP.
- Los textos legales son una base de prototipo alineada a las leyes 21.719 y 21.020; requieren revisión legal antes de producción.
- El APK es debug y no reemplaza una build de release firmada para Google Play.
