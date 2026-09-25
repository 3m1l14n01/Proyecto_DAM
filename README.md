# SPORTSGD

Aplicación Android académica para centralizar el seguimiento deportivo de estudiantes universitarios. SPORTSGD reúne en una sola experiencia el acceso por rol, el registro de jugadores, el calendario de actividades, las metas, las rutinas y las notificaciones internas.

> [!IMPORTANT]
> El estado actual es un **prototipo funcional local**. La aplicación persiste datos en el dispositivo y permite recorrer los flujos principales, pero todavía no constituye un servicio listo para producción: no tiene backend, sincronización entre usuarios, correo transaccional ni notificaciones push reales.

## Problema y objetivo

La información deportiva y académica de un equipo puede quedar dispersa entre mensajes, notas y reuniones. SPORTSGD busca ofrecer una referencia común para organizar jugadores, actividades, metas y rutinas, y así facilitar el seguimiento cotidiano sin sustituir el criterio del entrenador ni los sistemas institucionales.

Los usuarios contemplados por la demo son:

- **Entrenador:** consulta el panorama del equipo y puede registrar jugadores, metas y rutinas.
- **Estudiante deportista:** consulta la información disponible y ejecuta el seguimiento de rutinas con permisos de alta restringidos.

## Funcionalidad implementada

| Módulo | Alcance actual |
| --- | --- |
| Acceso | Inicio y cierre de sesión local por rol, validación de credenciales y persistencia opcional de sesión. |
| Recuperación | Flujo visual y validación de correo de demostración; no envía mensajes reales. |
| Inicio | Resumen dinámico de jugadores, metas, tareas y próxima actividad. |
| Jugadores | Listado, ficha individual y registro validado; el alta está reservada al rol de entrenador. |
| Calendario | Consulta de actividades próximas y detalle de actividad. |
| Metas | Consulta y creación validada de metas asociadas a un jugador. |
| Rutinas | Creación, inicio, avance por pasos, finalización y reinicio con persistencia local. |
| Notificaciones | Bandeja interna, contador de pendientes y marcado individual o global como leído. |
| Menú y perfil | Navegación principal, visualización de la sesión y cierre de sesión. |

La base local se inicializa con datos de ejemplo para que la aplicación sea utilizable inmediatamente después de instalarse.

## Arquitectura real

El proyecto utiliza una arquitectura por capas **orientada a MVVM**, sin framework de inyección de dependencias y sin afirmar una implementación estricta de Clean Architecture:

- una `MainActivity` aloja el `NavHost`;
- 16 destinos basados en `Fragment` componen la interfaz XML;
- los `ViewModel` mantienen estado y coordinan validaciones y operaciones asíncronas;
- las interfaces de repositorio separan presentación y acceso a datos;
- las implementaciones `Local*Repository` usan DAO de Room;
- `AppContainer` ensambla manualmente base de datos, sesión y repositorios;
- `StateFlow`, `Flow` y corrutinas propagan los cambios de datos a la interfaz.

```text
UI XML + Fragments
        |
    ViewModels
        |
Interfaces de repositorio
        |
Repositorios locales
        |
Room (DAO + entidades) / SharedPreferences (sesión)
```

## Tecnologías y configuración

| Elemento | Configuración del proyecto |
| --- | --- |
| Lenguaje | Kotlin |
| Interfaz | XML, Material Components y View Binding |
| Navegación | AndroidX Navigation Component 2.10.1 |
| Persistencia | Room 2.8.5 con KSP 2.3.10 |
| Concurrencia | Kotlin Coroutines 1.10.2 y Flow |
| Ciclo de vida | AndroidX Lifecycle 2.10.0 |
| Compilación | Gradle 9.5.0 y Android Gradle Plugin 9.3.3 |
| SDK | `compileSdk 37`, `targetSdk 37`, `minSdk 24` |
| Compatibilidad del código | Java 11 (`sourceCompatibility` y `targetCompatibility`) |
| JVM del daemon | Toolchain Java 25 configurado en `gradle/gradle-daemon-jvm.properties` |
| Pruebas | JUnit 4, AndroidX Test y Espresso |

## Estructura del proyecto

```text
SPORTSGD/
├── app/
│   ├── src/main/java/com/example/sportsgd/
│   │   ├── data/          # Room, DAO, entidades, repositorios locales y sesión
│   │   ├── domain/        # Modelos, contratos de repositorio y límites de servicio
│   │   └── presentation/  # Fragments, ViewModels, adaptadores y formateadores
│   ├── src/main/res/      # Layouts, navegación, estilos, colores e imágenes
│   ├── src/test/          # Pruebas unitarias JVM
│   └── src/androidTest/   # Pruebas instrumentadas
├── docs/                  # Recursos y documentación del proyecto
├── tools/                 # Utilidades de verificación local
├── verification/          # Evidencia visual real disponible
├── gradle/                # Wrapper, catálogo de versiones y toolchain
└── build.gradle.kts       # Configuración raíz
```

## Requisitos

- Git.
- Android Studio compatible con AGP 9.3.3.
- Android SDK 37 instalado.
- Un dispositivo o emulador Android con API 24 o posterior.
- Acceso a Internet durante la primera sincronización de dependencias y, si no está disponible localmente, para aprovisionar la toolchain Java 25 configurada por Gradle.

Android Studio crea `local.properties` con la ruta del SDK. Ese archivo es específico de cada equipo y está excluido del repositorio.

## Instalación y apertura

```bash
git clone https://github.com/3m1l14n01/Proyecto_DAM.git
cd Proyecto_DAM
```

1. Abrir la carpeta raíz en Android Studio.
2. Confirmar que el IDE detecte el SDK 37 y aceptar la sincronización de Gradle.
3. Esperar a que Gradle descargue las dependencias y la toolchain configurada, si hace falta.
4. Seleccionar la configuración `app` y un dispositivo con API 24 o superior.
5. Ejecutar **Run**.

No se requieren claves API, archivos `google-services.json` ni variables de entorno para la demo local.

## Compilación y comprobaciones

En Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

En macOS o Linux:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

Para ejecutar las pruebas instrumentadas se necesita un emulador o dispositivo conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

El APK de depuración se genera en `app/build/outputs/apk/debug/app-debug.apk`.

### Última verificación registrada

Verificación local del 24 de septiembre de 2026:

- `:app:testDebugUnitTest :app:lintDebug :app:assembleDebug`: **BUILD SUCCESSFUL**.
- Pruebas unitarias JVM: **4 aprobadas, 0 fallidas**.
- Pruebas instrumentadas: **4 aprobadas, 0 fallidas** en `Medium_Phone(AVD) - 17`.
- Lint: **0 errores y 128 advertencias**.
- APK debug generado correctamente (9,766,942 bytes; SHA-256 `d09933bef9d563bf7311c54764ac8eb3e6fcf9e61c6dbee1c75bcc6d5e280754`).

Las advertencias de lint se concentran principalmente en recursos sin uso, cadenas establecidas desde código, sobre-dibujo y actualizaciones de dependencias. Las pruebas instrumentadas cubren arranque, persistencia de rutinas, rechazo de una sesión con rol inválido y renovación de la agenda de demostración.

## Credenciales de demostración

Estas credenciales forman parte de la demo pública; **no son secretos ni cuentas reales**.

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Entrenador | `coach@sportsgd.mx` | `Sport2026!` |
| Estudiante | `student@sportsgd.mx` | `Sport2026!` |

## Servicios y datos

- **Room:** jugadores, actividades, metas, rutinas, pasos y notificaciones.
- **SharedPreferences:** sesión local.
- **Datos semilla:** contenido de demostración insertado de forma idempotente.
- **Servicios externos:** ninguno en esta versión.

Existe un contrato `PushNotificationGateway` para una integración futura, pero la implementación actual es local y no registra tokens ni envía notificaciones. La recuperación de contraseña tampoco conecta con un proveedor de correo.

## Diseño y repositorio

- [Diseño y prototipo en Figma](https://www.figma.com/design/6k5n20lXzkAdAV046vQNsf/Boceto--Copy-?node-id=2046-3&p=f&t=ArnnZKDi80BUIyud-0) — el acceso depende de los permisos del archivo.
- [Repositorio oficial](https://github.com/3m1l14n01/Proyecto_DAM)
- Rama principal: `main`

La implementación Android toma como referencia el sistema visual del prototipo: paleta verde, jerarquía tipográfica, tarjetas, formularios y navegación inferior. Algunas diferencias se mantienen cuando el comportamiento nativo o el alcance local de la demo lo requieren.

## Entregables documentales

- [Reporte técnico final en PDF](docs/SPORTSGD_Reporte_Final.pdf)
- [Reporte técnico final editable en DOCX](docs/SPORTSGD_Reporte_Final.docx)
- [Fuente del reporte](docs/REPORT_CONTENT.md)

El reporte final contiene 55 páginas, índices actualizados, 16 figuras, 16 tablas, referencias y anexos del repositorio y del diseño en Figma. Las capturas del prototipo están identificadas como diseño y las capturas de `verification/` como evidencia real de ejecución.

## Limitaciones conocidas

- La autenticación usa credenciales locales fijas; no hay alta de cuentas, servidor de identidad ni recuperación real.
- Los datos viven en una base Room del dispositivo; no existe sincronización, respaldo de aplicación ni colaboración multiusuario.
- La sesión de estudiante representa un rol de demostración y no está vinculada a un jugador específico del modelo.
- Las notificaciones son registros internos, no push del sistema ni Firebase Cloud Messaging.
- El calendario permite consultar actividades, pero la interfaz no ofrece todavía una gestión completa de horarios.
- Room se encuentra en el esquema inicial y exporta `app/schemas/.../1.json`; una evolución de producción necesitaría migraciones versionadas y sus pruebas.
- La variante `release` todavía requiere endurecimiento: identificador definitivo, firma, optimización, política de privacidad y configuración segura de servicios.
- Persisten advertencias de lint que no impiden compilar, pero conviene resolver antes de una publicación comercial.

## Datos académicos

- **Institución:** Universidad Tecmilenio
- **Proyecto:** SPORTSGD
- **Materia:** Desarrollo de Aplicaciones Móviles
- **Profesor:** Jose Luis Suchil Miranda
- **Autores:** Emiliano Iturralde Velazquez y Antonio de Jesus Juarez Padilla
- **Fecha de entrega:** 25 de septiembre de 2026
