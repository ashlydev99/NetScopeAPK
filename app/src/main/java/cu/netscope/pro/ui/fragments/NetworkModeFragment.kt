package cu.netscope.pro.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import cu.netscope.pro.R
import cu.netscope.pro.databinding.FragmentNetworkModeBinding

class NetworkModeFragment : Fragment() {

    private var _binding: FragmentNetworkModeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupModeSelector()
        loadCurrentMode()
    }

    private fun setupModeSelector() {
        val modes = listOf(
            "Automático (4G/3G/2G)",
            "Solo 4G (LTE Only)",
            "Solo 3G (WCDMA Only)",
            "Solo 2G (GSM Only)",
            "Preferir 4G/3G",
            "Preferir 3G"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            modes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        binding.spinnerNetworkMode.adapter = adapter
        
        binding.btnEngineeringMenu.setOnClickListener {
            openEngineeringMenu()
        }
        
        binding.btn4gOnly.setOnClickListener {
            openNetworkSettings()
        }
        
        binding.btn5gOnly.setOnClickListener {
            openNetworkSettings()
        }
        
        binding.btnAutoMode.setOnClickListener {
            openNetworkSettings()
        }
        
        binding.btn3gOnly.setOnClickListener {
            openNetworkSettings()
        }
    }

    private fun loadCurrentMode() {
        try {
            val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val currentMode = when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G WCDMA"
                TelephonyManager.NETWORK_TYPE_GSM -> "2G GSM"
                TelephonyManager.NETWORK_TYPE_HSDPA -> "3.5G HSPA"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "3.5G HSPA+"
                else -> "Otro"
            }
            binding.textCurrentMode.text = "Modo actual: $currentMode"
        } catch (e: Exception) {
            binding.textCurrentMode.text = "Modo actual: Desconocido"
        }
    }

    private fun openNetworkSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(intent)
            } catch (e2: Exception) {
                binding.textStatus.text = "No se pudo abrir configuración"
            }
        }
    }

    private fun openEngineeringMenu() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:*#*#4636#*#*")
            startActivity(intent)
        } catch (e: Exception) {
            binding.textStatus.text = "No se pudo abrir el menú de ingeniería"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}