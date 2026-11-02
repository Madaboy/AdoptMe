# Expansión 4.6 de AdoptMe — Diseño y Decisiones

## 1. Visión General

La Expansión 4.6 amplía las capacidades del producto AdoptMe agregando:
- Chat interno entre usuarios (adopción y consultas).
- Comunidad / red social de animales y adopciones.
- Tienda, pagos y donaciones a fundaciones.
- Gamificación y acciones responsables.
- Soporte Multi-país (iniciando en Chile `CL`).

Quedan expresamente fuera de alcance: llamadas de voz, videollamadas y funciones de IA.

---

## 2. Arquitectura de Módulos / Carpetas

```
app/
core/
  designsystem/
  common/
  session/
  flags/
  analytics/
  navigation/
feature/
  chat/
  community/
  store/
  gamification/
  localization/
  more/
docs/
  EXPANSION_4_6.md
  PROGRESS.md
  adr/
```

---

## 3. Estrategia de Backend e Interfaces

Todas las nuevas características de la Expansión 4.6 se construyen detrás de interfaces de repositorio con proveedores `LocalDemoDataSource` y esquemas de base de datos SQL / Supabase con RLS predefinidos.

---

## 4. Moderación y Seguridad

- Reportes de usuarios y publicaciones.
- Ocultamiento de PII en logs y analíticas.
- Feature flags independientes para cada funcionalidad.
