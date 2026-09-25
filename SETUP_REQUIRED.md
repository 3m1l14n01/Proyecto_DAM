# Configuración externa opcional

SPORTSGD funciona localmente sin cuentas, API keys ni servicios externos. Esta configuración solo es necesaria para activar notificaciones push reales con Firebase Cloud Messaging.

## Firebase Cloud Messaging

Para habilitar push real, el propietario del proyecto debe proporcionar y configurar:

1. Un proyecto de Firebase asociado al `applicationId` `com.example.sportsgd`.
2. El archivo generado por Firebase `app/google-services.json`.
3. El plugin Gradle oficial `com.google.gms.google-services`.
4. La dependencia oficial `com.google.firebase:firebase-messaging` mediante Firebase BoM.
5. Un servicio que extienda `FirebaseMessagingService` y convierta el payload remoto en el modelo local `AppNotification`.
6. Un backend confiable o Firebase Console para emitir mensajes. Las credenciales de servidor nunca deben incluirse en la aplicación Android.
7. En Android 13 o superior, solicitud en tiempo de ejecución de `POST_NOTIFICATIONS` antes de mostrar notificaciones del sistema.
8. Registro y eliminación del token mediante la abstracción `PushNotificationGateway` ya incluida.

No se añadió una configuración ficticia de Firebase, ningún `google-services.json`, token, API key ni credencial.

## Funciones de servidor no incluidas en la demo local

La sesión de entrenador y estudiante, los jugadores, actividades, metas, rutinas y avisos son locales al dispositivo. Para compartir datos entre cuentas o dispositivos se necesitarían una API de autenticación y sincronización, un servidor, sus contratos y credenciales de desarrollo proporcionados por el propietario. El flujo «Recuperar acceso» demuestra validación y navegación, pero para enviar un correo real se necesitaría un servicio de correo y una cuenta o backend autorizados. Ninguna de estas dependencias es necesaria para compilar o usar los recorridos locales de la entrega.
