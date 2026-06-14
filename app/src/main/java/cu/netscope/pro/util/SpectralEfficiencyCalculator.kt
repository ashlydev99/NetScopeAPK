package cu.netscope.pro.util

import kotlin.math.max

object SpectralEfficiencyCalculator {
    
    /**
     * Calcula la eficiencia espectral estimada para LTE en bits/s/Hz
     */
    fun calculateLTE(rsrp: Int, band: Int): Float {
        if (rsrp >= 0) return 0f
        
        // Ancho de banda típico por banda LTE (en MHz)
        val bandwidthMHz = when (band) {
            in 1..4 -> 10f
            in 5..8 -> 10f
            in 12..14 -> 5f
            17 -> 5f
            20 -> 10f
            28 -> 10f
            3 -> 15f
            7 -> 15f
            else -> 10f
        }
        
        // SINR estimado basado en RSRP
        val sinrEstimated = when {
            rsrp > -80 -> 25f
            rsrp > -90 -> 20f
            rsrp > -100 -> 15f
            rsrp > -110 -> 8f
            rsrp > -120 -> 3f
            else -> 0f
        }
        
        // Eficiencia espectral aproximada según SINR (256QAM)
        return max(0f, sinrEstimated * 0.3f)
    }
    
    /**
     * Calcula la eficiencia espectral estimada para 5G NR en bits/s/Hz
     */
    fun calculateNR(rsrp: Int, band: Int): Float {
        if (rsrp >= 0) return 0f
        
        // Ancho de banda típico para NR (más grande que LTE)
        val bandwidthMHz = when (band) {
            78 -> 100f // n78 típico 100MHz
            41 -> 60f
            28 -> 20f
            else -> 40f
        }
        
        val sinrEstimated = when {
            rsrp > -80 -> 30f
            rsrp > -90 -> 25f
            rsrp > -100 -> 18f
            rsrp > -110 -> 10f
            rsrp > -120 -> 5f
            else -> 0f
        }
        
        return max(0f, sinrEstimated * 0.35f)
    }
}