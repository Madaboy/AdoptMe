# ADR 001: Arquitectura de la Expansión 4.6 e Infraestructura Base

## Estatus
Aceptado

## Contexto
El proyecto AdoptMe requería completar la reparación de arquitectura base (Fase 0) antes de abordar la Expansión 4.6 (Chat, Comunidad, Tienda, Gamificación y Multi-país). El MVP inicial carecía de navegación tipada mediante Compose Navigation, persistencia local abstracta, FeatureFlags y ViewModels dedicados por pantalla.

## Decisiones
1. **Navegación**: Adoptar `androidx.navigation:navigation-compose` con rutas desacopladas en `Screen` y `AdoptMeNavHost`.
2. **Abstracción de Datos**: Convertir `DogRepository` en una interfaz e implementar `DogRepositoryImpl` respaldado por `LocalDemoDataSource` explícito.
3. **Manejo de Estados**: Dividir el ViewModel monolítico en `HomeViewModel`, `PublishViewModel`, `MyAdoptionsViewModel`, `ReportsViewModel`, `MapViewModel`.
4. **Infraestructura de Expansión**: Implementar `FeatureFlags` e `SessionManager` (DataStore) en el paquete `core/`.
5. **Nuevos Destinos**: Agregar el hub "Más" (`MoreHubScreen`) para albergar las secciones de la Expansión 4.6 deshabilitadas por defecto bajo Feature Flags.

## Consecuencias
- La aplicación compila limpiamente, mantiene compatibilidad con la API externa Huachitos y posee pruebas unitarias.
- Las futuras fases (9 a 14) pueden ser implementadas de forma aislada e incremental sin romper el flujo principal.
