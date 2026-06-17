# Buzón de Voz

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

Buzón de Voz es una aplicación de Buzón de Voz para móviles simplificado, offline y sin necesidad de root. Diseñado para Android 8.0+.

## Características
- Pantalla Principal: muestra mensajes de voz entrantes
- Pantalla Detalles: Detalles y reproducción del mensaje de voz
- Pantalla de Ajustes: Opciones de configuración variadas para el control de la aplicación

## Estructura del proyecto
- `app/src/main/java/cu/ashlydev/buzon/` - código fuente.
- `app/src/main/res/` - layouts, drawables, valores.
- `build.gradle.kts` y `settings.gradle.kts` - configuración Kotlin DSL.
- `.github/workflows/build-apk.yml` - workflow para compilar APK debug.

## Requisitos
- JDK 17
- Android SDK (compileSdk 34)
- Gradle 8.2 (ver workflow)

## Compilación
1. Clona el repositorio.
2. Abre en Android Studio (recomendado) o usa Gradle wrapper.
3. Compila y ejecuta en un dispositivo con Android 8.0+.

## Licencia
GPLv3 — ver archivo LICENSE.