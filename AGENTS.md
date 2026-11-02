# AGENTS.md — Contexto oficial para agentes OpenCode

> **Proyecto:** AdoptMe — app Android de adopción responsable de mascotas.
>
> **Repositorio:** https://github.com/Madaboy/AdoptMe (privado)
>
> **Rama principal:** `main`
>
> **Copia local de referencia:** `/home/madaboy/Projects/AdoptMe/AdoptMe`
>
> **Última actualización de este documento:** 2026-11-02

Este archivo es el contexto que debe leer cualquier agente de OpenCode antes de
trabajar en el proyecto. Si algo no está claro, revisa también `README.md`,
`Informcion/AdoptMe_Especificacion_Tecnica.md` y el historial de Git.

---

## 1. Prompt de arranque

Si eres un agente que acaba de recibir este proyecto, empieza así:

```text
Eres un agente de OpenCode trabajando en AdoptMe, una app Android de adopción
responsable de mascotas. Antes de tocar código:

1. Lee completo AGENTS.md.
2. Lee README.md y revisa git status, git log -5 y el estado de CI.
3. No inventes datos ni agregues datos de ejemplo.
4. Propón un plan corto antes de cambios grandes.
5. Al terminar, ejecuta ./gradlew assembleDebug testDebugUnitTest lintDebug.
6. Actualiza README/docs si cambió el comportamiento.
7. Haz commit con Conventional Commits y push a main, salvo que el usuario
   pida otra rama.
```

No pidas contraseñas ni tokens por el chat. Si falta autenticación, usa el flujo
de dispositivo de GitHub CLI y pide al usuario que autorice.

---

## 2. Resumen del producto

AdoptMe conecta mascotas que buscan hogar con personas interesadas en adoptar.
El catálogo principal se alimenta **en vivo desde Huachitos.cl** y las
publicaciones propias se guardan localmente en el dispositivo.

Principio central: **ningún dato simulado**. Si una fuente no entrega un dato,
el campo queda en `null` y la UI muestra “No informado” o lo omite. Nunca se
rellenan valores para “completar” una ficha.

Alcance actual: MVP Android local. No hay backend, autenticación real,
sincronización entre dispositivos, mapas reales ni notificaciones push.

---

## 3. Estado actual

### Funciona

- Catálogo real de Huachitos.cl con paginación, fotos, descripciones y filtro
  por especie.
- Navegación Compose con 4 destinos inferiores y una acción central de
  adopción.
- Publicación de mascota con foto desde el Photo Picker del sistema.
- Ficha de detalle completa en `ModalBottomSheet`.
- Intereses persistentes en DataStore.
- Publicaciones propias persistentes.
- Reportes locales persistentes.
- Recuperación ante estado local corrupto.
- Tests unitarios y lint sin errores.
- Workflow de GitHub Actions para compilar, testear y ejecutar lint.

### Pendiente / bloqueado

- Supabase: Auth, Postgres, Storage y RLS. **Requiere credenciales.**
- Sincronización de intereses, publicaciones y fotos entre dispositivos.
- Google Maps para reportes y lugares pet-friendly. **Requiere API key.**
- Crashlytics. **Requiere configuración de Firebase.**
- Contacto/coordinación de adopciones dentro de la app.
- Notificaciones push.
- Chat, comunidad, tienda, pagos, gamificación y multi-país: fuera de alcance
  por ahora y deshabilitados por Feature Flags.

### Verificación de referencia al cierre de esta etapa

- `./gradlew assembleDebug testDebugUnitTest lintDebug` → **BUILD SUCCESSFUL**
- **13 tests unitarios** pasando.
- Lint: **0 errores, 27 warnings** (principalmente versiones de dependencias).
- APK debug: `app/build/outputs/apk/debug/app-debug.apk` (~12 MB).
- CI: `.github/workflows/android.yml`.

Antes de confiar en estos números, vuelve a ejecutar los comandos: el estado
puede haber cambiado.

---

## 4. Stack y entorno

| Elemento | Versión / decisión |
|---|---|
| Lenguaje | Kotlin 2.0.0 |
| UI | Jetpack Compose + Material 3 |
| Compose BOM | 2024.10.01 |
| AGP | 8.7.0 |
| Gradle | Wrapper 9.3.0 |
| JDK | **21** (obligatorio para lint) |
| minSdk | 26 |
| compileSdk / targetSdk | 35 |
| Persistencia | DataStore Preferences + Gson |
| Red | Retrofit 2.11 + Gson |
| Imágenes | Coil 2.6 |
| HTML | Jsoup 1.18.1 |
| Tests | JUnit 4 + kotlinx-coroutines-test |

### JDK

El daemon de Gradle está fijado a **JDK 21** en
`gradle/gradle-daemon-jvm.properties` porque el lint de AGP 8.7 falla con
Java 25. No cambies esa configuración a Java 25.

En la máquina de referencia:

```bash
export JAVA_HOME=/home/madaboy/.local/share/jdks/jdk-21.0.13+9
export PATH="$JAVA_HOME/bin:$PATH"
```

Si esa ruta no existe, localiza un JDK 21 y exporta su `JAVA_HOME`. No uses un
JDK 25 para compilar o hacer lint.

### Android SDK

`local.properties` está ignorado por Git y **no debe subirse**. Android Studio
lo genera solo. En CI se usa el SDK preinstalado del runner de GitHub.

---

## 5. Compilar, testear y lint

Comando único de verificación:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Salidas útiles:

- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Reporte de tests: `app/build/reports/tests/testDebugUnitTest/index.html`
- Reporte de lint: `app/build/reports/lint-results-debug.html`

Regla: **no entregar un cambio si el comando anterior no termina en
`BUILD SUCCESSFUL`**. Si un warning nuevo es inevitable, explícalo.

---

## 6. Arquitectura

```text
MainActivity / AdoptMeApplication
        │
        ▼
Navigation Compose (AdoptMeNavHost)
        │
        ▼
Screens (Compose)
        │
        ▼
ViewModels por pantalla (StateFlow)
        │
        ▼
DogRepository (interfaz)
        │
        ├── HuachitosApiService (Retrofit + Gson)
        └── LocalDataSource (DataStore + estado en memoria)
```

- **DI manual:** `AppContainer` es un objeto singleton. `AdoptMeApplication`
  llama `AppContainer.init(context)` en `onCreate`. No instancies repositorios
  directamente en la UI.
- **Repositorio como interfaz:** `DogRepository` permite testear y, en el
  futuro, cambiar la implementación local por Supabase sin tocar pantallas.
- **ViewModels:** `HomeViewModel`, `PublishViewModel`, `MyAdoptionsViewModel`,
  `ReportsViewModel`, `MyPetViewModel` viven en
  `viewmodel/ScreenViewModels.kt`.
- **Estado:** los ViewModels exponen `StateFlow`; las pantallas recolectan con
  `collectAsState()`.
- **Persistencia:** `LocalDataSource` mantiene el estado en memoria y lo
  persiste en DataStore con `PersistedState v1`. La restauración inicial es
  asíncrona; los cambios hechos durante la restauración tienen prioridad.
- **Fotos:** `PetImageStore` copia la imagen elegida a
  `filesDir/pet_images/<uuid>.<ext>` y guarda un URI `file://` en
  `Dog.imageUrl`. No se piden permisos de almacenamiento.
- **Detalle:** `DogDetailSheet` es una hoja inferior reutilizable. No crea una
  ruta de navegación nueva.

### Flujo del catálogo

1. `HomeViewModel.init` llama a `repository.fetchDogsFromApi()`.
2. El repositorio consulta `https://huachitos.cl/api/animales?page=N`.
3. Lee `meta.last_page` y recorre todas las páginas.
4. Filtra animales con `estado` vacío o `adopcion`.
5. Mapea con `HuachitosAnimal.toDog()` y limpia HTML con `HtmlText`.
6. Deduplica por `source + externalId`.
7. `HomeViewModel` aplica el filtro de especie y expone la lista.

La API no entrega tamaño, energía, compatibilidad ni distancia: esos campos
quedan en `null`. No los inventes.

---

## 7. Archivos clave

```text
app/src/main/java/com/adoptme/
├── AdoptMeApplication.kt          # Inicializa AppContainer
├── MainActivity.kt                # Scaffold, barra inferior, toasts
├── core/
│   ├── common/HtmlText.kt         # Limpieza de HTML de Huachitos
│   ├── di/AppContainer.kt         # DI manual
│   ├── flags/FeatureFlags.kt      # Funciones futuras deshabilitadas
│   ├── media/PetImageStore.kt     # Copia de fotos al almacenamiento interno
│   ├── navigation/                # Destinations + NavHost
│   └── session/SessionManager.kt  # Sesión local preparada para auth futura
├── data/
│   ├── api/HuachitosApiService.kt
│   ├── datasource/LocalDataSource.kt
│   ├── datasource/LocalStore.kt
│   ├── model/Dog.kt
│   ├── model/HuachitosModels.kt
│   ├── model/Report.kt
│   └── repository/DogRepository.kt
├── ui/
│   ├── components/                # Barra inferior, chips, formularios, toasts
│   ├── screens/                   # Home, Publish, Reports, Interests, MyPet, More
│   └── theme/Theme.kt             # Paleta Azul & Naranja + Poppins
└── viewmodel/ScreenViewModels.kt
```

Tests:

```text
app/src/test/java/com/adoptme/
├── data/datasource/LocalDataSourcePersistenceTest.kt
├── data/model/HuachitosMapperTest.kt
└── data/repository/DogRepositoryTest.kt
```

---

## 8. Reglas de producto y datos

### Honestidad de datos

- **Nunca** agregues mascotas, reportes, intereses o publicaciones de ejemplo en
  producción.
- Un campo ausente se representa con `null`, no con `""`, `0`, `false` ni un
  valor inventado.
- En la UI, un `null` se muestra como “No informado” o se omite si no aporta.
- No inventes compatibilidades, salud, distancia, edad, raza ni ubicación.
- Mantén la atribución a Huachitos.cl y el enlace “Ver original”.
- No presentes una publicación local como si estuviera verificada por
  Huachitos.cl.

### Navegación

- Barra inferior: **Publicar · Reportes · [Adoptar] · Intereses · Mi Mascota**.
- “Adoptar” es la **cápsula ovalada central** y la acción principal: abre el
  catálogo de mascotas.
- “Publicar” ocupa la primera posición y abre el formulario a pantalla completa.
- “Más” se abre desde Mi Mascota; no ocupa una posición en la barra.
- No cambies la navegación a una barra de 5 pestañas.
- No uses “+” para publicar: el icono es casa + corazón.

### Ficha de detalle

- Se abre desde Adoptar, Intereses y Mi Mascota.
- Debe mostrar foto, nombre, especie, ubicación, descripción completa, ficha,
  salud/convivencia, necesidades especiales y fuente.
- Para publicaciones propias, indica “Esta ficha es tu publicación” y no
  ofrezcas guardarla como interés.
- Mantén el detalle en `ModalBottomSheet` salvo que el usuario pida otra cosa.

### Intereses

- Se guardan localmente y sobreviven al reinicio.
- La identidad de una mascota es `source + id`.
- No dupliques intereses.
- El badge de la barra muestra la cantidad.

---

## 9. Diseño y estilo

Paleta **Azul & Naranja**:

| Token | Color |
|---|---|
| Primario (azul) | `#2F80ED` |
| Secundario (naranja) | `#F47B2A` |
| Fondo nube | `#F4F6FA` |
| Tinta | `#111318` |
| Peligro | `#E5484D` |
| Advertencia | `#F2A93B` |

- Material 3, tipografía **Poppins**, cards de 24–32 dp y barra inferior flotante.
- Modo claro y oscuro.
- UI en **español de Chile**; nombres de código en inglés.
- Mantén consistencia con los componentes existentes antes de crear nuevos.
- No introduzcas una librería de diseño nueva sin justificarlo.

---

## 10. Persistencia

- DataStore Preferences, archivo `adoptme_local`.
- Modelo persistido: `PersistedState` versión 1.
- Contenido: intereses, mascotas propias y reportes.
- Serialización: Gson.
- Si el JSON está corrupto, la app debe iniciar vacía sin crashear.
- Los cambios se persisten mediante un `Channel.CONFLATED`; el último estado
  gana.
- La restauración es asíncrona y se mezcla con lo que el usuario ya hizo en la
  sesión.
- Las fotos locales se guardan en `filesDir/pet_images`; el JSON solo conserva
  el URI.

Si agregas un campo persistido, actualiza `PersistedState`, la serialización,
los tests de persistencia y considera una migración de versión.

---

## 11. Tests y calidad

Tests existentes (13):

- Contrato de Huachitos y mapeo de especies.
- Limpieza de HTML/entidades.
- Paginación, filtro de estado y deduplicación.
- Intereses sin duplicados y eliminación.
- Persistencia de intereses, mascotas, reportes y ruta de foto.
- Recuperación ante estado corrupto.

Reglas:

- Agrega tests para lógica de repositorio, mapeo, persistencia y filtros.
- No agregues tests que dependan de red real.
- Usa `InMemoryLocalStore` para persistencia.
- Ejecuta `testDebugUnitTest` y `lintDebug` antes de commitear.
- No silencies errores de lint sin justificación.

---

## 12. GitHub y CI

- Repositorio: **https://github.com/Madaboy/AdoptMe** (privado).
- Rama principal: `main`.
- El remoto `origin` ya está configurado.
- Workflow: `.github/workflows/android.yml`.
- CI se ejecuta en push y pull request:
  1. checkout
  2. JDK 21 JetBrains Runtime
  3. caché de Gradle
  4. `./gradlew assembleDebug testDebugUnitTest lintDebug`
  5. subida del APK debug como artefacto

Notas importantes del CI:

- Se usa el Android SDK preinstalado del runner.
- **No vuelvas a agregar `android-actions/setup-android@v3`**: intenta instalar
  el paquete `tools`, que ya no existe, y hace fallar el job.
- Si cambias el workflow, verifica que el push no sea rechazado por falta del
  scope `workflow`.
- Revisa el estado real con `gh run list --repo Madaboy/AdoptMe`.

Autenticación local:

```bash
gh auth status
gh auth setup-git   # si Git no puede leer la credencial
```

No subas tokens, contraseñas, keystores ni `local.properties`.

---

## 13. Limitaciones y deuda técnica conocida

- Todo es local: no hay sincronización entre dispositivos.
- No hay registro/login real; `SessionManager` está preparado pero sin Supabase.
- Las fotos no se comprimen ni se corrigen según EXIF; se copian tal cual.
- El formulario de publicación usa `remember`, no `rememberSaveable`: una
  rotación o muerte del proceso puede perder el borrador.
- No hay indicador de carga mientras DataStore restaura el estado local.
- “Compartir ficha” en Mi Mascota todavía muestra un toast de “próximamente”.
- No se pueden editar ni cerrar publicaciones propias.
- Los reportes son locales y no tienen mapa ni foto.
- El detalle no tiene contacto directo ni reporte de ficha.
- No hay pruebas instrumentadas/UI.
- Lint tiene 27 warnings de versiones; no son errores.

Estas son oportunidades, no excusas para inventar datos o romper el MVP.

---

## 14. Próximos pasos sugeridos

### Sin credenciales

1. Compartir ficha con el share sheet de Android.
2. Indicador de carga mientras se restaura DataStore.
3. Guardar el borrador de publicación con `rememberSaveable` o persistencia.
4. Comprimir y rotar fotos antes de copiarlas.
5. Editar/cerrar publicaciones propias.
6. Reportar una ficha desde el detalle.
7. Accesibilidad y pruebas de UI.

### Con credenciales (pedir al usuario)

1. Supabase Auth, Postgres, Storage y RLS.
2. Sincronización de intereses, publicaciones y fotos.
3. Contacto y notificaciones.
4. Google Maps para reportes y lugares pet-friendly.
5. Crashlytics y analítica.

### Fuera de alcance por ahora

Chat, red social, tienda, pagos, donaciones, gamificación, multi-país, iOS y
notificaciones push. No los implementes sin una instrucción explícita.

---

## 15. Convenciones de trabajo

- Cambios pequeños y revisables.
- No reescribas archivos completos si basta un cambio puntual.
- Sigue el estilo existente de Compose, nombres y comentarios.
- Comentarios y UI en español; código en inglés.
- No elimines documentación, PDFs ni presentaciones sin permiso.
- No hagas `force-push` ni reescribas el historial de `main`.
- No borres datos locales del usuario ni archivos ajenos al proyecto.
- Si un cambio requiere credenciales, detente y pide acceso; no inventes claves.
- Si un requisito es ambiguo, propone una opción y pregunta.

Commits sugeridos:

```text
feat: ...
fix: ...
ci: ...
docs: ...
test: ...
refactor: ...
```

---

## 16. Seguridad y privacidad

- El repositorio es **privado**; no lo hagas público sin autorización.
- Nunca commitees:
  - `local.properties`
  - `*.jks`, `*.keystore`
  - `.env`, `secrets.properties`
  - tokens de GitHub, Supabase, Google Maps o Firebase
- Usa variables de entorno o archivos locales ignorados.
- No incluyas datos personales de adoptantes, teléfonos o direcciones en
  ejemplos o tests.
- Las fotos de mascotas son datos del usuario: no las subas a servicios
  externos sin consentimiento y sin backend definido.

---

## 17. Definition of Done

Un cambio está terminado cuando:

- [ ] Compila con `assembleDebug`.
- [ ] Los tests pasan con `testDebugUnitTest`.
- [ ] `lintDebug` no tiene errores nuevos.
- [ ] No se agregaron datos simulados.
- [ ] La UI mantiene la paleta y la navegación acordadas.
- [ ] Los campos ausentes se muestran como “No informado” o se omiten.
- [ ] README/docs se actualizaron si cambió el comportamiento.
- [ ] El cambio está commiteado y subido a GitHub.
- [ ] El CI queda en verde.

---

## 18. Contexto para el agente entrante

Este proyecto ya pasó por:

1. Reparación del contrato de Huachitos y eliminación de datos simulados.
2. Persistencia local de intereses, mascotas y reportes.
3. Publicación con foto y ficha de detalle.
4. Configuración de CI y subida a GitHub.

La siguiente etapa debe elegir **una** mejora concreta, preferentemente de la
lista sin credenciales, implementarla completa y dejar el CI en verde. No
empieces varias fases a la vez.

Si el usuario pide Supabase, Google Maps o Crashlytics, primero confirma que
existen las credenciales y que están configuradas fuera del repositorio.
