# Contexto actual de AdoptMe

> **Última actualización:** 2026-11-12  
> **Commit de referencia:** `7f9495c` (`main`)  
> **Repositorio:** https://github.com/Madaboy/AdoptMe (privado)  
> **Copia local:** `/home/madaboy/AndroidStudioProjects/AdoptMe`

Este documento resume el estado real del proyecto después de la conversación de
trabajo y de los últimos commits remotos. Es un complemento de `AGENTS.md` y
`README.md`.

---

## 1. Qué es AdoptMe

App Android de adopción responsable de mascotas. El catálogo se alimenta en vivo
desde **Huachitos.cl**. Las publicaciones propias, intereses, reportes y cuentas
se guardan localmente en el dispositivo.

Principio central: **ningún dato simulado**. Si una fuente no entrega un campo,
se deja en `null` y la UI muestra “No informado” o lo omite.

Estado: **MVP Android local**. No hay backend, sincronización entre dispositivos,
alertas compartidas ni notificaciones push.

---

## 2. Stack y entorno

| Elemento | Valor |
|---|---|
| Lenguaje | Kotlin 2.0.0 |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Persistencia | DataStore Preferences + Gson |
| Red | Retrofit 2.11 + Gson |
| Imágenes | Coil 2.6 |
| Mapas | MapLibre 11.8 + OpenStreetMap (OpenFreeMap) |
| Ubicación | `LocationManager` + permisos runtime |
| HTML | Jsoup 1.18.1 |
| Tipografía | Poppins (SIL OFL) |
| JDK | 21 |
| minSdk / compileSdk / targetSdk | 26 / 35 / 35 |
| Tests | JUnit 4 + kotlinx-coroutines-test |

Entorno local:

```bash
export JAVA_HOME=/home/madaboy/.local/share/jdks/jdk-21.0.13+9
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME=/home/madaboy/Android/Sdk
```

`local.properties` está ignorado y contiene `sdk.dir=/home/madaboy/Android/Sdk`.

Verificación:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Estado de referencia: **BUILD SUCCESSFUL**, **17 tests**, **0 errores de lint**,
**30 warnings** de versiones de dependencias, CI en verde.

---

## 3. Navegación actual

Barra inferior flotante:

**Publicar · Reportes · [Adoptar] · Intereses · Mi Cuenta**

- **Adoptar** es la cápsula circular coral central con huella, sin texto.
- **Publicar** abre la ficha de adopción por pasos.
- **Reportes** abre el mapa a pantalla completa.
- **Mi Cuenta** contiene registro/login local y, con sesión, el perfil.
- **Mi Mascota** ya no ocupa la barra; se entra desde “Mis publicaciones” en
  Mi Cuenta.
- **Más** se entra desde Mi Cuenta/Ajustes y desde Mi Mascota.

Rutas: `adopt`, `reports`, `publish`, `interests`, `account`, `myPet`, `more`.

---

## 4. Experiencia por pantalla

### Adoptar

- Catálogo real de Huachitos.cl paginado y deduplicado.
- Tarjeta a pantalla completa con foto, nombre, edad, ubicación y etiquetas.
- Swipe izquierda = descartar; derecha = guardar interés.
- Botones inferiores: **X** para descartar y **MATCH** para guardar interés.
- Tutorial de swipe la primera vez, persistido en DataStore.
- Ficha de detalle con galería de todas las fotos de la página de Huachitos,
  visor a pantalla completa y tarjetas pastel de sexo/edad/raza.

### Publicar

- Wizard de 3 pasos:
  1. Fotos (hasta 6, Photo Picker, tips de foto).
  2. Descripción y opciones (especie, edad, raza, comuna, tamaño, energía,
     salud, convivencia).
  3. Confirmación y casillas legales (veracidad, Ley 21.020, autorización de
     fotos, no venta).
- Estilo visual **liquid glass** (translúcido, bordes suaves, sin blur real).
- Al publicar, la mascota queda con `ownerId` de la cuenta activa.

### Reportes

- Mapa **MapLibre + OpenStreetMap** a pantalla completa.
- Permiso de ubicación en runtime; punto del usuario y botón para centrar.
- Radio de alertas: 1, 5, 10 y 25 km.
- Etiqueta flotante compacta sobre el mapa (“N alertas cerca · X km”).
- Al tocarla se abre una hoja con la lista de alertas.
- Al tocar un marcador o una alerta se abre la ficha de la alerta.
- Wizard de reporte en 3 pasos:
  1. 1 foto obligatoria.
  2. Tipo, referencia de ubicación y descripción.
  3. Confirmación y casillas legales.
- La foto se comprime a WebP (máx. 1280 px, ~600 KB), se corrige EXIF y se
  guarda en `filesDir/report_images`.

### Intereses

- Grilla de tarjetas con foto a pantalla completa.
- Nombre/ubicación y etiquetas de especie, edad y sexo sobre la foto.
- Corazón para quitar el interés.
- Ficha de detalle reutilizada.

### Mi Cuenta

- Registro e inicio de sesión **local** con contraseña PBKDF2 y sal.
- Sesión persistida en DataStore.
- Perfil con iniciales, estadísticas reales (publicaciones, intereses, reportes)
  y accesos directos.
- Cierre de sesión.
- “Continuar con Google” es **demo visual**, no OAuth real.
- Casillas legales base: Ley 21.719 (datos personales) y Ley 21.020 (tenencia
  responsable). Los textos deben ser revisados por asesoría legal.

### Mi Mascota

- Accesible desde Mi Cuenta → “Mis publicaciones”.
- Muestra las publicaciones asociadas al `ownerId` de la sesión.
- Ficha de detalle y acceso a “Más”.

---

## 5. Diseño

Paleta **Coral & Crema**:

| Token | Color |
|---|---|
| Primario | `#D9534F` |
| Secundario | `#B23B37` |
| Fondo | `#F7F5F3` |
| Tinta | `#1E1B1A` |
| Peligro | `#C0392B` |
| Advertencia | `#F2A93B` |

- Tipografía **Poppins**.
- Cards de 24–32 dp, botones tipo píldora, barra inferior flotante.
- Modo claro y oscuro.
- UI en español de Chile; código en inglés.
- “Liquid glass” es una simulación visual con gradientes translúcidos y bordes;
  no usa blur real ni una librería externa.

---

## 6. Persistencia y datos

- DataStore `adoptme_local`: estado local (`PersistedState v1`) con intereses,
  mascotas propias y reportes.
- DataStore `user_session`: cuentas locales, sesión activa y onboarding.
- DataStore `adoptme_tutorial`: tutorial de swipe visto.
- `Dog` incluye `ownerId` para asociar publicaciones a una cuenta.
- `Report` incluye `latitude`, `longitude`, `createdAt` e `imagePath`.
- Las fotos de reportes se guardan como archivo WebP; el JSON solo conserva la
  ruta.
- La restauración local es asíncrona y tolera JSON corrupto.

### Optimización de fotos para la futura base de datos

- No guardar binarios ni base64 en la base de datos.
- Subir la imagen a **object storage** (Supabase Storage/S3).
- Guardar en `reports` solo `image_path` o `storage_key`.
- Servir por CDN.
- Borrar el objeto al eliminar el reporte.
- Usar hash para deduplicar.
- Considerar imagen WebP de 1280 px como tamaño máximo.

---

## 7. Calidad y CI

- 17 tests unitarios: contrato Huachitos, HTML, paginación/deduplicación,
  galería, distancia Haversine, intereses, persistencia de coordenadas/foto y
  recuperación de estado corrupto.
- `assembleDebug`, `testDebugUnitTest` y `lintDebug` en verde.
- GitHub Actions compila, prueba, ejecuta lint y publica el APK debug.
- JDK 21 obligatorio; no usar Java 25 para lint.

---

## 8. Limitaciones actuales

- Todo es local; no hay backend ni sincronización.
- Las cuentas son locales; no hay recuperación de contraseña ni autenticación
  con backend.
- La separación de datos entre múltiples cuentas aún no es completa.
- El mapa solo muestra reportes de este dispositivo.
- Google Sign-In es demo.
- “Más” y varias funciones comunitarias siguen deshabilitadas por Feature Flags.
- No hay pruebas instrumentadas/UI.
- Los textos legales son base de prototipo y requieren revisión legal.

---

## 9. Historial reciente relevante

- `7f9495c` Botones de adopción con descarte y match.
- `0932546` Tutorial inicial de swipe y mejora visual del paso de fotos.
- `fa40c4b` Restaurar Mi Cuenta sin estado de navegación obsoleto.
- `24d61d3` Perfil de cuenta local con registro e inicio de sesión.
- `753732f` Tarjetas de intereses con foto completa y etiquetas.
- `117fb6d` Wizard de reportes con foto optimizada.
- `e9dde4d` Etiqueta flotante y pin azul en reportes.
- `5be80c9` Ficha de adopción por pasos con estilo liquid glass.
- `d7f9f7d` Mapa de reportes a pantalla completa e iconos SVG.
- `7766361` Acceso con Google demo y casillas legales.
- `064fd95` Creación de cuenta movida a Mi Cuenta.
- `bf3e61d` Mapa local de reportes con ubicación y radio.

---

## 10. Próximos pasos sugeridos

1. Conectar Supabase Auth/Postgres/Storage/RLS.
2. Sincronizar publicaciones, intereses, reportes y fotos.
3. Alertas compartidas y lugares pet-friendly.
4. Recuperación de contraseña y Google Sign-In real.
5. Compresión/EXIF también para fotos de publicaciones.
6. Editar/cerrar publicaciones propias.
7. Contacto y notificaciones.
8. Pruebas instrumentadas/UI y accesibilidad.

---

## 11. Reglas para futuros agentes

- Leer `AGENTS.md` completo antes de tocar código.
- No inventar datos ni rellenar campos ausentes.
- Mantener paleta Coral & Crema, Poppins y bordes redondeados.
- No romper el flujo local sin backend.
- Actualizar README/AGENTS/CONTEXT si cambia el comportamiento.
- Ejecutar `assembleDebug testDebugUnitTest lintDebug` antes de commitear.
- Commit con Conventional Commits y push a `main`, salvo indicación contraria.
- No commitear `local.properties`, tokens, keystores ni secretos.
