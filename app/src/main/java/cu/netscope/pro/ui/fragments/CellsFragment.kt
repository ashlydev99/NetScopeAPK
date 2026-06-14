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
            try {
                showCellDetailDialog(cell)
            } catch (e: Exception) { }
        }
        
        binding.recyclerCells.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cellAdapter
            setHasFixedSize(true)
            itemAnimator = null
        }
    }

    private fun observeNetworkState() {
        NetworkMonitorService.networkStateListener = { state ->
            try {
                if (_binding != null && isAdded) {
                    updateUI(state)
                }
            } catch (e: Exception) { }
        }
    }

    private fun updateUI(state: NetworkState) {
        // Operador y tipo de red
        binding.textOperatorName.text = state.operatorName ?: "Buscando..."
        binding.textNetworkGen.text = state.networkGeneration ?: "?"
        binding.textNetworkType.text = state.networkType ?: "..."

        // Buscar la celda conectada (isRegistered = true)
        val connectedCell = state.cells.firstOrNull { it.isRegistered }
        
        if (connectedCell != null && connectedCell.dbm != null && connectedCell.dbm < 0) {
            binding.textPrimaryDbm.text = "${connectedCell.dbm} dBm"
            binding.textPrimaryBand.text = connectedCell.band ?: "?"
            
            val signalLevel = connectedCell.signalLevel
            binding.textSignalLevel.text = when (signalLevel) {
                5 -> "Excelente"
                4 -> "Buena"
                3 -> "Regular"
                2 -> "Mala"
                1 -> "Muy débil"
                else -> "Sin señal"
            }
            
            val signalColor = when {
                signalLevel >= 4 -> 0xFF4CAF50.toInt()
                signalLevel >= 3 -> 0xFF8BC34A.toInt()
                signalLevel >= 2 -> 0xFFFFC107.toInt()
                signalLevel >= 1 -> 0xFFFF9800.toInt()
                else -> 0xFFF44336.toInt()
            }
            binding.textSignalLevel.setTextColor(signalColor)
            binding.textPrimaryDbm.setTextColor(signalColor)
        } else {
            binding.textPrimaryDbm.text = "-- dBm"
            binding.textPrimaryBand.text = "?"
            binding.textSignalLevel.text = "Buscando..."
            binding.textSignalLevel.setTextColor(0xFF888888.toInt())
            binding.textPrimaryDbm.setTextColor(0xFF888888.toInt())
        }

        // Lista de celdas
        try {
            val allCells = state.cells.sortedWith(compareByDescending<CellInfo> { it.isRegistered }
                .thenByDescending { it.dbm ?: -200 })
            cellAdapter.submitList(allCells)
            binding.textCellCount.text = "${allCells.size} celdas detectadas"
        } catch (e: Exception) { }
    }

    private fun showCellDetailDialog(cell: CellInfo) {
        try {
            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("Detalles de Celda ${cell.type}")
                .setMessage(buildString {
                    append("Tipo: ").append(cell.type ?: "?").append("\n")
                    append("Señal: ").append(cell.dbm ?: "?").append(" dBm\n")
                    if (cell.band != null && cell.band.isNotEmpty() && cell.band != "?") {
                        append("Banda: ").append(cell.band).append("\n")
                    }
                    if (cell.tac != null && cell.tac.isNotEmpty() && cell.tac != "0") {
                        append("TAC: ").append(cell.tac).append("\n")
                    }
                    if (cell.cid != null && cell.cid.isNotEmpty() && cell.cid != "0") {
                        append("CID: ").append(cell.cid).append("\n")
                    }
                    if (cell.lac != null && cell.lac.isNotEmpty() && cell.lac != "0") {
                        append("LAC: ").append(cell.lac).append("\n")
                    }
                    if (cell.pci != null && cell.pci.isNotEmpty() && cell.pci != "0") {
                        append("PCI: ").append(cell.pci).append("\n")
                    }
                    if (cell.bsic != null && cell.bsic.isNotEmpty() && cell.bsic != "0") {
                        append("BSIC: ").append(cell.bsic).append("\n")
                    }
                    append("Conectado: ").append(if (cell.isConnected) "Sí" else "No")
                })
                .setPositiveButton("Cerrar", null)
                .create()
            dialog.show()
        } catch (e: Exception) { }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}