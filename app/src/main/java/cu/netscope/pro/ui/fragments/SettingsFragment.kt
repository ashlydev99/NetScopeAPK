package cu.netscope.pro.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import cu.netscope.pro.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        // Unidad de distancia
        val distanceUnit = prefs.getString("distance_unit", "meters")
        when (distanceUnit) {
            "meters" -> binding.radioMeters.isChecked = true
            "km" -> binding.radioKm.isChecked = true
            "mi" -> binding.radioMiles.isChecked = true
        }
        
        // Vibración
        binding.switchVibration.isChecked = prefs.getBoolean("vibration_on_cell_change", true)
        
        // Actualización
        val updateInterval = prefs.getString("update_interval", "1")
        when (updateInterval) {
            "1" -> binding.radioUpdate1s.isChecked = true
            "2" -> binding.radioUpdate2s.isChecked = true
            "5" -> binding.radioUpdate5s.isChecked = true
        }
        
        // Mostrar eficiencia espectral
        binding.switchShowEfficiency.isChecked = prefs.getBoolean("show_spectral_efficiency", true)
        
        // Escanear puertos comunes
        binding.switchAutoScanPorts.isChecked = prefs.getBoolean("auto_scan_ports", false)
    }

    private fun setupListeners() {
        // Unidad de distancia
        binding.radioGroupDistance.setOnCheckedChangeListener { _, checkedId ->
            val unit = when (checkedId) {
                binding.radioMeters.id -> "meters"
                binding.radioKm.id -> "km"
                binding.radioMiles.id -> "mi"
                else -> "meters"
            }
            prefs.edit().putString("distance_unit", unit).apply()
        }
        
        // Vibración
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_on_cell_change", isChecked).apply()
        }
        
        // Intervalo de actualización
        binding.radioGroupUpdate.setOnCheckedChangeListener { _, checkedId ->
            val interval = when (checkedId) {
                binding.radioUpdate1s.id -> "1"
                binding.radioUpdate2s.id -> "2"
                binding.radioUpdate5s.id -> "5"
                else -> "1"
            }
            prefs.edit().putString("update_interval", interval).apply()
        }
        
        // Mostrar eficiencia espectral
        binding.switchShowEfficiency.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_spectral_efficiency", isChecked).apply()
        }
        
        // Auto escanear puertos
        binding.switchAutoScanPorts.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_scan_ports", isChecked).apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}