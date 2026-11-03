# Prompt de arranque para un agente OpenCode

Copia este bloque en la sesión del agente y completa `TAREA ACTUAL`.

---

```text
Trabaja en AdoptMe, una app Android de adopción responsable de mascotas.

Repositorio privado: https://github.com/Madaboy/AdoptMe
Rama: main
Copia local de referencia: /home/madaboy/Projects/AdoptMe/AdoptMe

Antes de tocar código:
1. Si no existe la copia local, clónala con:
   git clone https://github.com/Madaboy/AdoptMe.git
   cd AdoptMe
2. Verifica autenticación:
   gh auth status
   Si Git no puede leer la credencial, ejecuta: gh auth setup-git
3. Lee completo AGENTS.md en la raíz del repositorio.
4. Lee README.md y revisa:
   git status
   git log -5 --oneline
   gh run list --repo Madaboy/AdoptMe --limit 5
5. Confirma que el proyecto compila antes de cambiar nada:
   export JAVA_HOME=/home/madaboy/.local/share/jdks/jdk-21.0.13+9
   export PATH="$JAVA_HOME/bin:$PATH"
   ./gradlew assembleDebug testDebugUnitTest lintDebug

Reglas obligatorias:
- No inventes datos ni agregues mascotas, reportes o intereses de ejemplo.
- Si la fuente no entrega un dato, usa null y muestra "No informado".
- Mantén la paleta Coral & Crema, la tipografía Poppins y la navegación acordada.
- "Adoptar" es el botón circular central con la huella, sin texto. "Publicar"
  ocupa la primera posición y usa casa + corazón; nunca uses "+".
- No agregues Supabase, Google Maps, Firebase ni otras credenciales sin que el
  usuario las entregue y las configure fuera del repositorio.
- No commitees local.properties, keystores, tokens ni secretos.
- No hagas force-push ni reescribas el historial de main.
- No implementes chat, red social, tienda, pagos, gamificación, multi-país,
  iOS ni push: están fuera de alcance por ahora.

TAREA ACTUAL:
[Describe aquí la tarea concreta. Ejemplo: "Implementa el share sheet para
compartir una ficha desde Mi Mascota, con tests y README actualizado."]

Al terminar:
1. Ejecuta ./gradlew assembleDebug testDebugUnitTest lintDebug.
2. Corrige cualquier error de compilación, test o lint.
3. Actualiza README.md y/o AGENTS.md si cambió el comportamiento.
4. Haz commit con Conventional Commits (feat:, fix:, docs:, ci:, test:).
5. Haz push a main.
6. Verifica que el CI quede en verde con:
   gh run list --repo Madaboy/AdoptMe --limit 5
7. Reporta: archivos tocados, comandos ejecutados, resultado de tests/lint,
   estado del CI y cualquier pendiente real.
```

---

## Notas para quien entrega el proyecto

- `AGENTS.md` contiene el contexto completo y OpenCode lo carga automáticamente
  cuando trabaja en la raíz del repositorio.
- Este archivo es solo el mensaje de arranque para una sesión nueva o para otro
  agente que no cargue `AGENTS.md` por sí solo.
- Si el agente no tiene acceso al repositorio privado, debe pedir acceso al
  usuario; no debe intentar crear credenciales por su cuenta.
