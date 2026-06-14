package cu.netscope.pro.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cu.netscope.pro.data.model.CellInfo
import cu.netscope.pro.data.model.NetworkState
import cu.netscope.pro.databinding.FragmentCellsBinding
import cu.netscope.pro.service.NetworkMonitorService
import cu.netscope.pro.ui.adapters.CellAdapter

class CellsFragment : Fragment() {

    private var _binding: FragmentCellsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cellAdapter: CellAdapter
    private var allCells: List<CellInfo> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCellsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNetworkState()
    }

    private fun setupRecyclerView() {
        cellAdapter = CellAdapter { cell ->
            showCellDetailDialog(cell)
        }
        
        binding.recyclerCells.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cellAdapter
            setHasFixedSize(true)
            itemAnimator = null // Sin animaciones para máxima velocidad
        }
    }

    private fun observeNetworkState() {
        NetworkMonitorService.networkStateListener = { state ->
            updateUI(state)
        }
    }

    private fun updateUI(state: NetworkState) {
        // Actualizar header con información del operador
        binding.textOperatorName.text = state.operatorName
        binding.textNetworkGen.text = state.networkGeneration
        binding.textNetworkType.text = state.networkType
        
        val primaryCell = state.primaryCell
        if (primaryCell != null) {
            binding.textPrimaryDbm.text = "${primaryCell.dbm} dBm"
            binding.textPrimaryBand.text = "Banda ${primaryCell.band}"
            binding.textPrimaryDistance.text = primaryCell.distanceFormatted
            
            // Indicador visual de intensidad de señal
            val signalLevel = primaryCell.signalLevel
            binding.signalIndicator.text = when (signalLevel) {
                5 -> "█████"
                4 -> "████"
                3 -> "███"
                2 -> "██"
                1 -> "█"
                else -> "Sin señal"
            }
            
            // Color según intensidad
            val signalColor = when {
                signalLevel >= 4 -> 0xFF4CAF50.toInt() // Verde
                signalLevel >= 2 -> 0xFFFFC107.toInt() // Ámbar
                else -> 0xFFF44336.toInt() // Rojo
            }
            binding.signalIndicator.setTextColor(signalColor)
        }

        // Ordenar celdas: primaria primero, luego por intensidad
        allCells = state.cells.sortedWith(compareByDescending<CellInfo> { it.isRegistered }
            .thenByDescending { it.dbm })
        
        cellAdapter.submitList(allCells)
        
        // Actualizar contador
        binding.textCellCount.text = "${allCells.size} celdas detectadas"
    }

    private fun showCellDetailDialog(cell: CellInfo) {
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Detalles de Celda ${cell.type}")
            .setMessage(buildString {
                append("Tipo: ").append(cell.type).append("\n")
                append("MCC: ").append(cell.mcc).append("\n")
                append("MNC: ").append(cell.mnc).append("\n")
                if (cell.tac.isNotEmpty() && cell.tac != "0") {
                    append("TAC: ").append(cell.tac).append("\n")
                }
                if (cell.cid.isNotEmpty() && cell.cid != "0") {
                    append("CID: ").append(cell.cid).append("\n")
                }
                if (cell.lac.isNotEmpty() && cell.lac != "0") {
                    append("LAC: ").append(cell.lac).append("\n")
                }
                if (cell.pci.isNotEmpty() && cell.pci != "0") {
                    append("PCI: ").append(cell.pci).append("\n")
                }
                if (cell.band.isNotEmpty() && cell.band != "?") {
                    append("Banda: ").append(cell.band).append("\n")
                }
                if (cell.frequency.isNotEmpty() && cell.frequency != "?") {
                    append("Frecuencia/EARFCN: ").append(cell.frequency).append("\n")
                }
                append("Señal: ").append(cell.dbm).append(" dBm\n")
                if (cell.rsrp != 0) append("RSRP: ").append(cell.rsrp).append(" dBm\n")
                if (cell.rsrq != 0) append("RSRQ: ").append(cell.rsrq).append(" dB\n")
                if (cell.sinr != 0) append("SINR: ").append(cell.sinr).append(" dB\n")
                if (cell.bsic.isNotEmpty() && cell.bsic != "0") {
                    append("BSIC: ").append(cell.bsic).append("\n")
                }
                append("Distancia est.: ").append(cell.distanceFormatted).append("\n")
                if (cell.spectralEfficiency > 0) {
                    append("Efic. Espectral: ")
                    append(String.format("%.2f", cell.spectralEfficiency))
                    append(" bps/Hz\n")
                }
                if (cell.timingAdvance > 0) {
                    append("Timing Advance: ").append(cell.timingAdvance).append("\n")
                }
                append("Conectado: ").append(if (cell.isConnected) "Sí" else "No")
            })
            .setPositiveButton("Cerrar", null)
            .create()
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}