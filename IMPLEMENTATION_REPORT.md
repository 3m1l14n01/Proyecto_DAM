# SPORTSGD — Auditoría técnica de implementación

**Fecha de corte:** 24 de septiembre de 2026  
**Tipo de documento:** estado verificable del código fuente y de los artefactos locales  
**Aplicación:** Android nativa, paquete `com.example.sportsgd`

## 1. Alcance y fuentes

Este documento describe lo que existe en el repositorio. No sustituye al reporte académico final ni convierte una intención del diseño en una función implementada.

Fuentes contrastadas:

- `Actividad3_DAM_.pdf`: línea base de requisitos y estado histórico del proyecto.
- `SPORTSGD_Reporte_1.1.pdf`: antecedentes, objetivos y decisiones de diseño; no se toma como evidencia de ejecución.
- `Figma_Prototipo.pdf`: catálogo visual exportado del prototipo.
- Archivo Figma `Boceto--Copy-`, página de pantallas `2046:8`: referencia visual vigente.
- Código, recursos, reportes de Gradle y APK presentes en este repositorio.

Ante una diferencia entre un documento histórico, el Figma y el comportamiento ejecutable, esta auditoría informa por separado el diseño esperado y la implementación observable.

## 2. Resumen ejecutivo

SPORTSGD es una demostración local funcional que cubre acceso, panel, jugadores, calendario, metas, rutinas, notificaciones internas, menú y perfil. La aplicación usa una sola `MainActivity`, 16 destinos de Navigation Component, Fragments con ViewBinding y persistencia Room.

El proyecto compila como APK `debug` y cuenta con pruebas unitarias e instrumentadas. La corrida final del 24 de septiembre de 2026 terminó con `BUILD SUCCESSFUL`: aprobó 4 pruebas unitarias y 4 instrumentadas, generó el APK y ejecutó Lint con 0 errores y 128 advertencias. La sección 11 conserva el comando, el entorno y los resultados completos.

La aplicación no es todavía un producto conectado: autenticación, recuperación de acceso, datos y avisos son locales. No hay backend, correo real, Firebase Cloud Messaging, sincronización entre dispositivos ni asociación segura entre la cuenta estudiante y un registro de jugador.

## 3. Configuración reproducible

| Elemento | Valor observado | Evidencia |
|---|---:|---|
| Gradle Wrapper | 9.5.0 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.3.3 | `gradle/libs.versions.toml` |
| KSP | 2.3.10 | `gradle/libs.versions.toml` |
| `compileSdk` / `targetSdk` | 37 / 37 | `app/build.gradle.kts` |
| `minSdk` | 24 | `app/build.gradle.kts` |
| Java | 11 | `app/build.gradle.kts` |
| Room | 2.8.5 | `gradle/libs.versions.toml` |
| Navigation Component | 2.10.1 | `gradle/libs.versions.toml` |
| Lifecycle | 2.10.0 | `gradle/libs.versions.toml` |
| Coroutines | 1.10.2 | `gradle/libs.versions.toml` |
| ViewBinding | habilitado | `app/build.gradle.kts` |
| Versión de aplicación | `1.0` (`versionCode` 1) | `app/build.gradle.kts` |

La configuración actual es la fuente de verdad. Las versiones de SDK o Gradle mencionadas en entregas anteriores son históricas y no describen esta compilación.

## 4. Arquitectura

La aplicación sigue una separación por capas, sin framework de inyección de dependencias:

```text
com.example.sportsgd/
├── SportsGdApplication.kt
├── AppContainer.kt                # composición manual
├── data/
│   ├── local/                     # Room, DAOs, entidades, relaciones y seed
│   ├── mapper/                    # entidad ↔ dominio
│   ├── repository/                # implementaciones locales
│   └── session/                   # sesión en SharedPreferences
├── domain/
│   ├── model/                     # modelos sin dependencia de UI
│   ├── notification/              # límite para push futuro
│   └── repository/                # contratos
└── presentation/
    ├── adapters/                  # RecyclerView
    ├── fragments/                 # vistas y navegación
    └── viewmodels/                # estado y operaciones de pantalla
```

`SportsGdApplication` crea `AppContainer`; este construye la base, repositorios y sesión. Los Fragments consumen ViewModels y no consultan DAOs directamente. Los repositorios exponen `Flow`, y los ViewModels publican estado reactivo mediante `StateFlow`.

La separación es apropiada para la escala de la entrega, aunque la composición manual y la ausencia de módulos por función aumentarían el costo de mantenimiento si el proyecto crece.

## 5. Persistencia y datos de demostración

### 5.1 Room

La base `sportsgd.db`, versión 1, declara seis tablas:

- `players`: datos personales, deportivos y académicos.
- `activities`: entrenamientos, partidos, actividades académicas y otros eventos.
- `goals`: avance, objetivo, unidad, vencimiento y estado.
- `routines`: jugador, meta opcional, duración, inicio y finalización.
- `routine_steps`: pasos ordenados, vinculados a una rutina con borrado en cascada.
- `notifications`: avisos internos, lectura y destino de navegación opcional.

El esquema Room se exporta durante KSP mediante `room.schemaLocation`. El archivo versionado de referencia está en:

```text
app/schemas/com.example.sportsgd.data.local.SportsGdDatabase/1.json
```

Esto permite revisar cambios futuros del modelo. No existen migraciones porque la base sigue en versión 1; aumentar la versión requerirá implementar y probar una migración antes de distribuir la actualización.

### 5.2 Seed idempotente y calendario vigente

`DatabaseSeeder` inserta con identificadores estables:

- tres jugadores: Ana Martínez, Diego Torres y Sofía Hernández;
- tres actividades;
- una meta con avance 2/3;
- una rutina de cuatro pasos;
- tres notificaciones internas.

El entrenamiento de demostración está asociado a Ana, por lo que puede aparecer en su ficha. Si las tres actividades semilla ya terminaron, el seeder desplaza únicamente esas filas a fechas futuras relativas al reloj actual. No modifica actividades creadas por el usuario y conserva una asociación de jugador que ya hubiera sido establecida. Este comportamiento se cubre con `DatabaseSeederInstrumentedTest`.

### 5.3 Copias de seguridad

`backup_rules.xml` y `data_extraction_rules.xml` excluyen:

- `sports_gd_session.xml`;
- todo el dominio de base de datos, tanto en copia en la nube como en transferencia entre dispositivos.

La exclusión reduce la exposición y la restauración accidental de sesión y datos personales de la demo. `android:allowBackup` sigue habilitado para otros archivos permitidos; la base local tampoco está cifrada en reposo.

## 6. Sesión, roles y controles locales

Existen dos accesos ficticios, codificados únicamente para la demostración:

```text
Entrenador: coach@sportsgd.mx
Estudiante: student@sportsgd.mx
Contraseña: Sport2026!
```

`SessionManager` conserva correo, nombre y rol en `SharedPreferences` cuando el usuario elige recordar el acceso. La lectura es **fail-closed**: si falta el rol o su valor no pertenece a `UserRole`, se elimina la preferencia incompleta y no se crea sesión. En particular, un valor desconocido ya no puede recibir privilegios de entrenador por un valor predeterminado.

La interfaz oculta operaciones de alta al estudiante y los ViewModels vuelven a comprobar el rol antes de guardar jugadores, metas o rutinas. Esto es defensa local útil, pero no sustituye autorización de servidor: un APK modificable y una base en el dispositivo no constituyen un límite de seguridad confiable.

## 7. Funcionalidad observable

| Área | Estado | Alcance verificable |
|---|---|---|
| Acceso y cierre de sesión | Implementado localmente | Validación, dos roles demo, recordar acceso y limpieza del historial de navegación. |
| Recuperación de acceso | Simulación | Valida el correo y muestra “Revisa tu correo”; no envía mensajes. |
| Panel | Implementado | Totales y próximos elementos calculados desde repositorios locales. |
| Jugadores | Implementado | Lista, alta validada, confirmación y ficha por identificador. |
| Calendario | Implementado | Agenda y detalle de actividades persistidas. |
| Metas | Implementado | Lista, progreso y alta local para entrenador. |
| Rutinas | Implementado | Alta, pasos, inicio, marcado, finalización y persistencia. |
| Notificaciones | Implementado internamente | Lectura, destino y aviso al completar una rutina; no son push del sistema. |
| Menú y perfil | Implementado | Navegación modular, datos de sesión y rol. |
| Evidencia multimedia | No implementado | La actividad puede verse; no hay captura ni carga de archivos. |
| Sincronización multiusuario | No implementada | Todos los datos de negocio pertenecen a la instalación local. |

### Mutaciones de rutina

`RoutineRepository` devuelve `Boolean` en `startRoutine`, `setStepCompleted`, `completeRoutine` y `resetRoutine`. La implementación local comprueba que existan la rutina y el paso, rechaza cambios sobre una rutina ya finalizada y usa el número de filas afectadas por Room. Los ViewModels solo presentan éxito cuando el repositorio confirma la mutación.

La finalización ocurre dentro de una transacción: completa los pasos, marca la rutina, incrementa una sola vez la meta enlazada y crea un aviso. Un segundo intento devuelve `false`, por lo que no vuelve a sumar el progreso.

## 8. Navegación y estados de pantalla

`nav_graph.xml` contiene 16 destinos:

```text
Login → Recuperar acceso → Revisa tu correo
Login → Panel
Panel ↔ Jugadores → Registro → Confirmación → Lista/Ficha
Panel ↔ Calendario → Detalle de actividad
Panel ↔ Metas → Rutina en curso → Rutina completada
Panel → Notificaciones
Panel → Menú → Perfil / módulos / Cerrar sesión
```

Los argumentos de identificador se declaran como `long` y usan `-1L` como valor centinela. Esto evita el error de inflación producido cuando Navigation intentaba leer un entero donde esperaba un `long`.

## 9. Correspondencia con Figma

La interfaz usa los principales tokens observados en el prototipo:

| Token | Valor |
|---|---:|
| Marca | `#00A991` |
| Primario oscuro | `#007F6D` |
| Primario profundo | `#004C41` |
| Fondo | `#E6F6F4` |
| Superficie | `#B0E4DD` |
| Texto principal | `#1E1E1E` |
| Texto secundario | `#5E5A5A` |
| Espaciado base | 4, 8, 12, 16, 24 y 32 dp |
| Radios base | 8, 12, 16 y 24 dp |

Se implementaron los estados representativos de acceso, panel, jugadores, registro, calendario, metas, rutina, menú, notificaciones, recuperación y confirmaciones. Los desplegables de rutina muestran el nombre del jugador sin concatenar el correo, y el campo multilínea de pasos reserva altura suficiente para que ayuda y contenido no se superpongan.

Las capturas reales conservadas en `verification/` son evidencia de ejecución, no sustitutos de pruebas automatizadas. Pueden diferir del Figma en tipografía disponible, barras del sistema y relación de aspecto. No se ha realizado una auditoría formal completa de accesibilidad; en particular, algunas combinaciones heredadas del prototipo requieren revisar contraste y escalado de fuente.

## 10. Pruebas existentes

### Pruebas unitarias JVM: 4 casos

- cálculo y límite del progreso de una meta;
- progreso de pasos de una rutina;
- composición del nombre completo de un jugador;
- presencia de los 16 destinos requeridos en el grafo.

### Pruebas instrumentadas en el código: 4 casos

- arranque de `MainActivity` e inflación del grafo;
- transacción de finalización de rutina e idempotencia;
- rechazo fail-closed de una sesión con rol desconocido;
- actualización de fechas del seed sin perder la asignación del entrenamiento.

Los cuatro casos instrumentados se ejecutaron y aprobaron en la corrida final sobre `Medium_Phone(AVD) - 17`.

## 11. Estado de validación

### 11.1 Corrida final

<!-- FINAL_VALIDATION_START -->

**Estado:** APROBADA.  
**Fecha:** 24 de septiembre de 2026, hora local.  
**Dispositivo instrumentado:** `Medium_Phone(AVD) - 17`.

Comando ejecutado:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:connectedDebugAndroidTest --stacktrace
```

| Comprobación final | Resultado |
|---|---:|
| Gradle | `BUILD SUCCESSFUL` en 13 s |
| Tareas | 86: 5 ejecutadas y 81 `up-to-date` |
| Pruebas unitarias | 4 aprobadas, 0 fallos |
| Pruebas instrumentadas | 4 aprobadas, 0 fallos |
| Lint | 0 errores, 128 advertencias |
| APK debug | 9,766,942 bytes; SHA-256 `d09933bef9d563bf7311c54764ac8eb3e6fcf9e61c6dbee1c75bcc6d5e280754` |

La corrida cubre en una misma invocación la compilación, el APK, las pruebas JVM, el análisis Lint y las cuatro pruebas instrumentadas posteriores a las correcciones descritas en la sección 12.

<!-- FINAL_VALIDATION_END -->

### 11.2 Contexto de validaciones anteriores

Antes de las últimas correcciones se registró otra corrida completa con 4 pruebas unitarias aprobadas, 0 errores y 124 advertencias de Lint. Después se realizó una compilación incremental satisfactoria. Esos resultados ayudan a seguir la evolución, pero la evidencia de aceptación vigente es la corrida final de la sección 11.1.

## 12. Correcciones técnicas consolidadas

- Se corrigieron los argumentos `long` del grafo con valores `-1L` para evitar el cierre al arrancar.
- La sesión con rol faltante o desconocido falla cerrada y elimina el estado persistido inválido.
- La base Room y la sesión se excluyen de backup y transferencia.
- La actividad semilla de entrenamiento se vincula a Ana y el calendario demo se renueva cuando todas sus actividades caducan.
- Las mutaciones de rutina informan éxito o rechazo con `Boolean`; una fila inexistente ya no produce una confirmación engañosa.
- La finalización de una rutina permanece transaccional e idempotente.
- Room exporta el esquema v1 a `app/schemas`.
- El selector de jugador de una rutina evita etiquetas largas con correo y el campo de pasos evita superposición visual.
- Los colores, espaciados y radios se alinearon con los fundamentos del Figma vigente.

## 13. Limitaciones y riesgos abiertos

1. **Autenticación de demostración.** Las credenciales viven en el cliente y no hay identidad verificable ni cambio de contraseña.
2. **Autorización solo local.** Los controles de rol mejoran el flujo, pero no protegen datos frente a una aplicación modificada.
3. **Estudiante no vinculado a jugador.** La cuenta estudiante consulta el conjunto local; no existe un identificador de usuario de servidor que delimite “mis datos”.
4. **Sin backend ni sincronización.** No hay API, resolución de conflictos, respaldo funcional ni colaboración entre dispositivos.
5. **Recuperación y push simulados.** No se envían correos ni notificaciones remotas. `PushNotificationGateway` es solo un punto de extensión.
6. **Datos locales sin cifrado.** Las reglas impiden su backup, pero SQLite y SharedPreferences no usan cifrado en reposo.
7. **Migraciones pendientes.** El esquema v1 está exportado; aún no existe una prueba de migración para una versión posterior.
8. **Cobertura limitada.** Hay pocos casos unitarios y cuatro instrumentados; faltan pruebas de ViewModels, validaciones, accesibilidad, rotación, restauración de proceso y recorridos UI completos.
9. **Distribución no preparada.** El `applicationId` sigue bajo `com.example`, no hay firma de producción, configuración segura por ambiente ni optimización de release habilitada.
10. **Deuda de Lint.** La corrida final conserva 128 advertencias; además, `Window.statusBarColor` está deprecado en APIs recientes.
11. **Accesibilidad por confirmar.** No se ha certificado contraste, navegación con lector de pantalla ni comportamiento con fuentes grandes.
12. **Evidencia multimedia ausente.** No existe cámara, selector de archivos, carga ni almacenamiento remoto de evidencias.

## 14. Configuración externa opcional

La demo local no necesita claves ni servicios externos. Para convertir recuperación, notificaciones y sincronización en funciones reales se requieren decisiones y credenciales del propietario. Los requisitos de Firebase Cloud Messaging, backend y correo están delimitados en [`SETUP_REQUIRED.md`](SETUP_REQUIRED.md); el repositorio no incluye `google-services.json`, tokens ni secretos reales.

## 15. Criterio de entrega técnica

El estado puede considerarse una **demo Android local completa para los flujos implementados**: la corrida final de la sección 11.1 confirmó compilación, análisis estático y pruebas automatizadas. No debe describirse como sistema productivo, plataforma multiusuario ni servicio conectado hasta resolver las limitaciones de la sección 13.
