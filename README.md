# AdoptMe 🐾

Aplicación Android de adopción responsable de mascotas. El catálogo se alimenta en vivo desde **Huachitos.cl** y la app prioriza un principio simple: **ningún dato simulado**. Si la fuente no entrega un dato, no se inventa.

## Estado actual

Fase 0 completada en lo funcional:

- Catálogo real de Huachitos.cl con paginación, fotos y descripciones.
- Navegación Compose con 5 posiciones y acción central de adopción.
- ViewModel por pantalla y repositorio como interfaz.
- Persistencia local de intereses, publicaciones propias y reportes.
- Pantalla de creación de cuenta en la pestaña **Mi Cuenta** (solo front-end; sin persistencia ni login real todavía).
- Publicación con foto desde el selector del sistema, sin permisos de almacenamiento.
- Ficha de detalle en formato carta con galería de todas las fotos de Huachitos y visor a pantalla completa, accesible desde Adoptar e Intereses.
- Sin datos de ejemplo visibles en producción.
- Tests unitarios, lint sin errores y CI de GitHub Actions.

Pendiente de infraestructura: Supabase/backend para alertas compartidas, Crashlytics y sincronización entre dispositivos. El mapa local ya usa MapLibre + OpenStreetMap.

## Repositorio y colaboración

- Repositorio privado: [github.com/Madaboy/AdoptMe](https://github.com/Madaboy/AdoptMe)
- Contexto completo para agentes: [AGENTS.md](AGENTS.md)
- Prompt de arranque para una sesión nueva: [docs/PROMPT_AGENTE.md](docs/PROMPT_AGENTE.md)

## Funcionalidades

| Sección | Estado |
| :--- | :--- |
| **Adoptar** | Catálogo real, swipe, foto a pantalla completa con etiquetas y ficha con galería |
| **Reportes** | Crear y listar reportes locales; mapa MapLibre/OSM con ubicación y radio de alertas |
| **Publicar** | Ficha de adopción con foto, especie, edad, salud, convivencia y descripción |
| **Intereses** | Mascotas guardadas, ficha de detalle, eliminar interés, badge en la barra |
| **Mi Cuenta** | Creación de cuenta front-end: Google demo, casillas Ley 21.719/21.020; área logeada pendiente |
| **Más** | Pendiente de la cuenta logeada; funcionalidades futuras con Feature Flags |

El botón central **Adoptar** es un círculo coral con la huella, sin texto. **Publicar** ocupa la primera posición de la barra y abre el formulario a pantalla completa; no usa “+”: publicar una adopción es un acto de cuidado.

## Arquitectura

```text
UI (Jetpack Compose)
        │
        ▼
ViewModel por pantalla (StateFlow)
        │
        ▼
DogRepository (interfaz)
        │
        ├── HuachitosApiService (Retrofit + Gson)
        └── LocalDataSource (DataStore + caché en memoria)
```

- **AppContainer** (`core/di`): inyección manual con una única instancia del repositorio.
- **LocalDataSource**: mantiene el estado inmediato en memoria y lo persiste en DataStore.
- **PersistedState v1**: intereses, publicaciones propias y reportes.
- **HtmlText**: limpia el HTML de Huachitos con Jsoup antes de mostrarlo.
- **PetImageStore**: copia la foto elegida a `filesDir/pet_images` y guarda un URI `file://` en la publicación.
- **DogDetailSheet**: ficha completa en `ModalBottomSheet`, sin ruta de navegación adicional.
- **Regla de datos**: los campos ausentes quedan en `null`; la UI los omite o muestra “No informado”.

### Estructura principal

```text
app/src/main/java/com/adoptme/
├── AdoptMeApplication.kt          # Inicializa AppContainer
├── MainActivity.kt                # Scaffold, barra inferior y toasts
├── core/
│   ├── common/HtmlText.kt
│   ├── di/AppContainer.kt
│   ├── media/PetImageStore.kt       # Copia de fotos al almacenamiento interno
│   └── navigation/                # Rutas + NavHost
├── data/
│   ├── api/                       # Retrofit
│   ├── datasource/                # DataStore y estado local
│   ├── model/                     # Dog, Report, contrato Huachitos
│   └── repository/
├── ui/
│   ├── components/                # Barra, tarjetas, chips, formularios
│   ├── screens/                   # Una pantalla por destino
│   └── theme/                     # Paleta Coral & Crema + Poppins
└── viewmodel/ScreenViewModels.kt
```

## Diseño

Paleta **Coral & Crema** con modo claro/oscuro:

| Token | Color |
| :--- | :--- |
| Primario (coral) | `#D9534F` |
| Secundario (coral profundo) | `#B23B37` |
| Fondo crema | `#F7F5F3` |
| Tinta | `#1E1B1A` |
| Peligro | `#C0392B` |
| Advertencia | `#F2A93B` |

Tipografía **Poppins** (SIL OFL 1.1, licencia en `app/src/main/assets/licenses/Poppins-OFL.txt`), cards de 24–32 dp, barra inferior flotante y componentes Material 3.

## Compilar y probar

Requisitos: Android SDK 35 y **JDK 21**. El proyecto fija el daemon de Gradle a JDK 21 porque el lint de AGP 8.7 no es compatible con Java 25.

```bash
export JAVA_HOME=/ruta/a/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew assembleDebug testDebugUnitTest lintDebug
```

Salida:

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Tests: 17 pruebas unitarias
- Lint: 0 errores

## Tests cubiertos

- Contrato real de Huachitos.cl y mapeo de especies.
- Limpieza de HTML y entidades.
- Paginación, filtro por estado y deduplicación.
- Intereses sin duplicados y eliminación.
- Persistencia de intereses, mascotas, reportes y ruta de la foto entre instancias.
- Extracción y deduplicación de la galería de fotos de Huachitos.
- Cálculo de distancia para alertas cercanas.
- Persistencia de coordenadas y fecha de reportes.
- Recuperación ante estado local corrupto.

## Integración continua

`.github/workflows/android.yml` ejecuta en cada push y pull request:

1. JDK 21 (JetBrains Runtime) y Android SDK.
2. `./gradlew assembleDebug testDebugUnitTest lintDebug`.
3. Publica el APK debug como artefacto descargable.

## Limitaciones conocidas

- Las publicaciones, intereses y reportes son locales: no se sincronizan entre dispositivos. El mapa muestra únicamente los reportes de este teléfono.
- Las fotos se copian al almacenamiento privado del dispositivo y no se suben a la nube.
- La creación de cuenta es solo front-end: no persiste cuentas, no autentica y aún no existe la pantalla logeada.
- Los textos de términos y privacidad son una base de prototipo alineada a las leyes 21.719 y 21.020; requieren revisión legal antes de producción.
- Las alertas compartidas entre usuarios requieren backend; el mapa local ya funciona con MapLibre/OSM.
- Las secciones de chat, comunidad, tienda y gamificación están deshabilitadas por Feature Flags.

## Próximos pasos

1. Supabase: Auth, Postgres, Storage y RLS.
2. Sincronización de publicaciones, intereses y fotos entre dispositivos.
3. Contacto y coordinación de adopciones dentro de la app.
4. Backend para alertas compartidas y lugares pet-friendly.
5. Crashlytics y métricas de uso.
