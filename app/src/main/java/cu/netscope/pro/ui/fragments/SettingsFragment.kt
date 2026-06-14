package cu.netscope.pro.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
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
        prefs = requireContext().getSharedPreferences("netscope_settings", Context.MODE_PRIVATE)
        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        try {
            val distanceUnit = prefs.getString("distance_unit", "meters")
            when (distanceUnit) {
                "meters" -> binding.radioMeters.isChecked = true
                "km" -> binding.radioKm.isChecked = true
                "mi" -> binding.radioMiles.isChecked = true
            }
        } catch (e: Exception) {
            binding.radioMeters.isChecked = true
        }
        
        try {
            binding.switchVibration.isChecked = prefs.getBoolean("vibration_on_cell_change", true)
        } catch (e: Exception) {
            binding.switchVibration.isChecked = true
        }
        
        try {
            val updateInterval = prefs.getString("update_interval", "1")
            when (updateInterval) {
                "1" -> binding.radioUpdate1s.isChecked = true
                "2" -> binding.radioUpdate2s.isChecked = true
                "5" -> binding.radioUpdate5s.isChecked = true
            }
        } catch (e: Exception) {
            binding.radioUpdate1s.isChecked = true
        }
        
        try {
            binding.switchShowEfficiency.isChecked = prefs.getBoolean("show_spectral_efficiency", true)
        } catch (e: Exception) {
            binding.switchShowEfficiency.isChecked = true
        }
    }

    private fun setupListeners() {
        binding.radioGroupDistance.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            val unit = when (checkedId) {
                R.id.radio_meters -> "meters"
                R.id.radio_km -> "km"
                R.id.radio_miles -> "mi"
                else -> "meters"
            }
            prefs.edit().putString("distance_unit", unit).apply()
        }
        
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_on_cell_change", isChecked).apply()
        }
        
        binding.radioGroupUpdate.setOnCheckedChangeListener { _: RadioGroup, checkedId: Int ->
            val interval = when (checkedId) {
                R.id.radio_update_1s -> "1"
                R.id.radio_update_2s -> "2"
                R.id.radio_update_5s -> "5"
                else -> "1"
            }
            prefs.edit().putString("update_interval", interval).apply()
        }
        
        binding.switchShowEfficiency.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_spectral_efficiency", isChecked).apply()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}