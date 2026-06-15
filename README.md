# NetScope Pro

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

NetScope Pro es un analizador de redes móviles simplificado, offline y sin necesidad de root. Diseñado para Android 8.0+.

## Características
- Tema oscuro consistente.
- Pantalla Celdas: información de operador, generación, tipo, señal en dBm con descripción y lista de celdas detectadas.
- Pantalla Velocímetro: monitor pasivo de tráfico (TrafficStats) y gráfica de ráfagas (últimos 30s) con MPAndroidChart.
- Servicio en primer plano que monitorea la red y muestra notificación persistente con botón "Salir".
- Soporta CellInfoGsm, CellInfoWcdma, CellInfoLte (no 5G NR para compatibilidad API 26).

## Estructura del proyecto
- `app/src/main/java/cu/netscope/pro/` - código fuente.
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