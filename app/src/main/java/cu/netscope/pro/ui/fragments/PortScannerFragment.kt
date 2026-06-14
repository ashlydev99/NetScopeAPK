package cu.netscope.pro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cu.netscope.pro.databinding.FragmentPortScannerBinding
import cu.netscope.pro.util.PortScanner
import kotlinx.coroutines.launch

class PortScannerFragment : Fragment() {

    private var _binding: FragmentPortScannerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnScanLocal.setOnClickListener {
            startLocalScan()
        }
        
        binding.btnScanCustom.setOnClickListener {
            val host = binding.editHost.text.toString().trim()
            if (host.isNotEmpty()) {
                startCustomScan(host)
            }
        }
        
        binding.btnScanCommon.setOnClickListener {
            startCommonScan()
        }
    }

    private fun startLocalScan() {
        binding.textResults.text = "Escaneando localhost...\n"
        binding.progressScan.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val results = PortScanner.quickScan()
                displayResults(results)
            } catch (e: Exception) {
                binding.textResults.append("Error: ${e.message}\n")
            } finally {
                binding.progressScan.visibility = View.GONE
            }
        }
    }

    private fun startCustomScan(host: String) {
        binding.textResults.text = "Escaneando $host...\n"
        binding.progressScan.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val results = PortScanner.scanHost(host)
                displayResults(results)
            } catch (e: Exception) {
                binding.textResults.append("Error: ${e.message}\n")
            } finally {
                binding.progressScan.visibility = View.GONE
            }
        }
    }

    private fun startCommonScan() {
        val hosts = listOf(
            "8.8.8.8",
            "8.8.4.4",
            "1.1.1.1",
            "gateway.local"
        )
        
        binding.textResults.text = "Escaneando hosts comunes...\n"
        binding.progressScan.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                for (host in hosts) {
                    binding.textResults.append("\n--- $host ---\n")
                    val results = PortScanner.scanHost(host)
                    val openPorts = results.filter { it.isOpen }
                    if (openPorts.isNotEmpty()) {
                        openPorts.forEach { result ->
                            binding.textResults.append(
                                "  Puerto ${result.port}: ABIERTO (${result.serviceName})\n"
                            )
                        }
                    } else {
                        binding.textResults.append("  Sin puertos abiertos detectados\n")
                    }
                }
            } catch (e: Exception) {
                binding.textResults.append("Error: ${e.message}\n")
            } finally {
                binding.progressScan.visibility = View.GONE
            }
        }
    }

    private fun displayResults(results: List<PortScanner.ScanResult>) {
        val openPorts = results.filter { it.isOpen }
        val closedPorts = results.filter { !it.isOpen }
        
        binding.textResults.append("\n=== Resultados del Escaneo ===\n")
        binding.textResults.append("Total puertos escaneados: ${results.size}\n")
        binding.textResults.append("Puertos abiertos: ${openPorts.size}\n")
        binding.textResults.append("Puertos cerrados: ${closedPorts.size}\n\n")
        
        if (openPorts.isNotEmpty()) {
            binding.textResults.append("--- Puertos Abiertos ---\n")
            openPorts.forEach { result ->
                binding.textResults.append(
                    "  Puerto ${result.port}: ${result.serviceName}\n"
                )
            }
        }
        
        binding.textResults.append("\n--- Todos los Puertos ---\n")
        results.forEach { result ->
            val status = if (result.isOpen) "ABIERTO" else "Cerrado"
            binding.textResults.append(
                "  ${result.port}: $status (${result.serviceName})\n"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}