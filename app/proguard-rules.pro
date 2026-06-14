# NetScope Pro - Reglas de ProGuard

# Mantener clases de modelo
-keep class cu.netscope.pro.data.model.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# CameraX
-keep class androidx.camera.** { *; }

# Mantener anotaciones
-keepattributes *Annotation*