# 📡 NetScope Pro

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-26%2B-orange.svg)](https://developer.android.com/about/versions/oreo)

**Analizador avanzado de redes móviles para Android**

NetScope Pro es una herramienta de diagnóstico y monitoreo de redes celulares que te permite visualizar información detallada sobre las celdas y torres de telefonía móvil cercanas, medir la velocidad de conexión en tiempo real, estimar la distancia a la antena y mucho más.

<p align="center">
  <img src="https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/banner.png" alt="NetScope Pro Banner" width="800">
</p>

---

## ✨ Características Principales

### 📊 Monitoreo de Celdas
- Lista detallada de todas las celdas cercanas con información precisa
- Visualización de celda conectada (serving cell) con indicador visual
- Datos: MCC, MNC, TAC, CID, PCI, banda, frecuencia (EARFCN/NRARFCN)
- Intensidad de señal en dBm con código de colores
- Indicador de intensidad de señal en tiempo real

### 🚀 Velocímetro de Conexión
- Monitoreo pasivo de velocidad de descarga y subida
- Gráfica de ráfagas en tiempo real (últimos 30 segundos)
- Indicadores de rendimiento (eficiencia espectral)
- Unidad dinámica (Kbps / Mbps) según velocidad

### 🧭 Brújula de Torre
- Modo brújula para localizar la dirección de la torre conectada
- Estimación de distancia basada en intensidad de señal
- Soporte para dispositivos con o sin sensor de brújula (GPS fallback)
- Información de operador, banda y señal en tiempo real

### 📷 Realidad Aumentada (AR)
- Visualización de información de red sobre la cámara en vivo
- Overlays con datos de la celda conectada
- Identificación visual de celdas cercanas

### 📱 Dual SIM
- Información detallada de ambas SIMs activas
- Operador, MCC/MNC, número, ICCID, roaming
- Tipo de red de datos para cada SIM

### 🔧 Modo de Red
- Selector de modo de red preferido
- Acceso rápido al menú de ingeniería (*#*#4636#*#*)
- Perfiles predefinidos (Solo 4G, Solo 5G, Automático)
- Atajos rápidos para cambio de modo

### 🔌 Escáner de Puertos
- Escaneo de puertos locales y remotos
- Identificación de servicios (HTTP, SSH, DNS, etc.)
- Escaneo de hosts comunes (Google DNS, Cloudflare)
- Resultados en tiempo real

### 🔔 Notificación Permanente
- Información de red en la barra de notificaciones
- Operador, tipo de red, banda, dBm, CID, BSIC
- Botón de salida rápida del servicio

### ⚙️ Ajustes Personalizables
- Unidad de distancia: Metros, Kilómetros o Millas
- Vibración al cambiar de celda
- Intervalo de actualización configurable
- Mostrar/ocultar eficiencia espectral
- Auto-escanear puertos locales

---

## 📸 Capturas de Pantalla

| Celdas | Velocímetro | Brújula | Dual SIM |
|:---:|:---:|:---:|:---:|
| ![Celdas](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/cells.png) | ![Velocímetro](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/speed.png) | ![Brújula](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/compass.png) | ![Dual SIM](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/dualsim.png) |

| Realidad Aumentada | Modo de Red | Escáner de Puertos | Ajustes |
|:---:|:---:|:---:|:---:|
| ![AR](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/ar.png) | ![Modo Red](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/netmode.png) | ![Puertos](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/portscan.png) | ![Ajustes](https://raw.githubusercontent.com/Ashlydev99/NetScopeAPK/main/screenshots/settings.png) |

---

## 📋 Requisitos

- **Android:** 8.0 (Oreo) o superior
- **Permisos necesarios:**
  - Ubicación (para detectar celdas cercanas)
  - Teléfono (para leer información de red)
  - Cámara (opcional, para realidad aumentada)
  - Notificaciones (para el servicio en primer plano)
- **No requiere ROOT**

---

## 🚀 Instalación

### Desde Google Play (Próximamente)