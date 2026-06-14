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
        binding.textOperatorName.text = state.operatorName ?: "Buscando..."
        binding.textNetworkGen.text = state.networkGeneration ?: "?"
        binding.textNetworkType.text = state.networkType ?: "..."

        val connectedCell = state.cells.firstOrNull { it.isRegistered }
        
        // Variables locales para evitar smart cast
        val cellDbm = connectedCell?.dbm
        val cellBand = connectedCell?.band
        
        if (connectedCell != null && cellDbm != null && cellDbm < 0) {
            binding.textPrimaryDbm.text = "$cellDbm dBm"
            binding.textPrimaryBand.text = cellBand ?: "?"
            
            val signalLevel = connectedCell.signalLevel
            binding.textSignalLevel.text = when (signalLevel) {
                5 -> "Excelente"
                4 -> "Buena"
                3 -> "Inestable"
                2 -> "Mala"
                1 -> "Muy débil"
                else -> "Sin señal"
            }
            
            val signalColor = when {
                signalLevel >= 5 -> 0xFF4CAF50.toInt()
                signalLevel == 4 -> 0xFF8BC34A.toInt()
                signalLevel == 3 -> 0xFFFFC107.toInt()
                signalLevel == 2 -> 0xFFFF9800.toInt()
                signalLevel == 1 -> 0xFFF44336.toInt()
                else -> 0xFF888888.toInt()
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

        try {
            val allCells = state.cells.sortedWith(compareByDescending<CellInfo> { it.isRegistered }
                .thenByDescending { it.dbm ?: -200 })
            cellAdapter.submitList(allCells)
            binding.textCellCount.text = "${allCells.size} celdas detectadas"
        } catch (e: Exception) { }
    }

    private fun showCellDetailDialog(cell: CellInfo) {
        try {
            // Variables locales para evitar smart cast
            val type = cell.type
            val dbm = cell.dbm
            val band = cell.band
            val frequency = cell.frequency
            val tac = cell.tac
            val cid = cell.cid
            val lac = cell.lac
            val pci = cell.pci
            val bsic = cell.bsic
            val isConnected = cell.isConnected
            
            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("Detalles de Celda ${type ?: "?"}")
                .setMessage(buildString {
                    append("Tipo: ").append(type ?: "?").append("\n")
                    append("Señal: ").append(dbm ?: "?").append(" dBm\n")
                    if (band != null && band.isNotEmpty() && band != "?") {
                        append("Banda: ").append(band).append("\n")
                    }
                    if (frequency != null && frequency.isNotEmpty() && frequency != "?") {
                        append("Frecuencia: ").append(frequency).append("\n")
                    }
                    if (tac != null && tac.isNotEmpty() && tac != "0") {
                        append("TAC: ").append(tac).append("\n")
                    }
                    if (cid != null && cid.isNotEmpty() && cid != "0") {
                        append("CID: ").append(cid).append("\n")
                    }
                    if (lac != null && lac.isNotEmpty() && lac != "0") {
                        append("LAC: ").append(lac).append("\n")
                    }
                    if (pci != null && pci.isNotEmpty() && pci != "0") {
                        append("PCI: ").append(pci).append("\n")
                    }
                    if (bsic != null && bsic.isNotEmpty() && bsic != "0") {
                        append("BSIC: ").append(bsic).append("\n")
                    }
                    append("Conectado: ").append(if (isConnected) "Sí" else "No")
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