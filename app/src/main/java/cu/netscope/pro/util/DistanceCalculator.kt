package cu.netscope.pro.util

import kotlin.math.pow

object DistanceCalculator {
    
    /**
     * Estima la distancia a la torre basada en la intensidad de señal
     * usando un modelo de propagación simplificado (Friis/Okumura-Hata adaptado)
     */
    fun estimateDistance(dbm: Int, band: Int): Float {
        if (dbm >= 0) return 0f
        
        // Frecuencia típica por banda
        val frequencyMHz = when (band) {
            in 1..4 -> 900f    // GSM 900
            in 5..6 -> 850f    // GSM 850
            8 -> 900f
            12, 13, 14 -> 700f
            17 -> 700f
            20 -> 800f
            28 -> 700f
            in 1..26 -> 2100f  // WCDMA
            3 -> 1800f
            7 -> 2600f
            in 33..44 -> 2100f // TDD
            in 40..43 -> 2300f
            78 -> 3500f        // 5G NR n78
            41 -> 2500f
            else -> 1800f
        }
        
        // Potencia de transmisión típica de una torre: 43 dBm (20W)
        val txPower = 43f
        
        // Modelo simplificado de espacio libre + pérdidas urbanas
        val pathLoss = txPower - dbm
        
        // Fórmula de Friis: PL = 20*log10(d) + 20*log10(f) + 32.45
        // Despejando d: d = 10^((PL - 20*log10(f) - 32.45) / 20)
        val distance = 10f.pow((pathLoss - 20 * Math.log10(frequencyMHz.toDouble()) - 32.45) / 20)
        
        // Ajuste por entorno urbano (factor de corrección)
        val urbanFactor = 0.7f
        
        return (distance.toFloat() * 1000 * urbanFactor) // Convertir km a metros
    }
}