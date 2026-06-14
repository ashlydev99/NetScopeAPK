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
            "Automático (5G/4G/3G/2G)",
            "Solo 5G (NR Only)",
            "Solo 4G (LTE Only)",
            "Solo 3G (WCDMA Only)",
            "Solo 2G (GSM Only)",
            "Preferir 4G/3G",
            "Preferir 3G"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            modes
        )
        
        binding.spinnerNetworkMode.adapter = adapter
        binding.spinnerNetworkMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // No forzar en automático
                    applyNetworkMode(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Botón para abrir menú de ingeniería
        binding.btnEngineeringMenu.setOnClickListener {
            openEngineeringMenu()
        }
    }

    private fun loadCurrentMode() {
        val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val currentMode = when (tm.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G WCDMA"
            TelephonyManager.NETWORK_TYPE_GSM -> "2G GSM"
            else -> "Otro"
        }
        
        binding.textCurrentMode.text = "Modo actual: $currentMode"
    }

    private fun applyNetworkMode(position: Int) {
        // La API pública no permite forzar modos sin root
        // Abrimos el menú de configuración de red del sistema
        val intent = Intent(android.provider.Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
        startActivity(intent)
        
        binding.textStatus.text = "Redirigiendo a configuración de red..."
    }

    private fun openEngineeringMenu() {
        // Intentar abrir el menú de ingeniería mediante código USSD
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