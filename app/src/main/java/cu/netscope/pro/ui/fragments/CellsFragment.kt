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
            showCellDetailDialog(cell)
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
            if (_binding != null && isAdded) {
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: NetworkState) {
        binding.textOperatorName.text = state.operatorName ?: "Buscando..."
        binding.textNetworkGen.text = state.networkGeneration ?: "?"
        binding.textNetworkType.text = state.networkType ?: "?"

        val primaryCell = state.primaryCell
        val cellDbm = primaryCell?.dbm
        
        if (primaryCell != null && cellDbm != null && cellDbm < 0) {
            binding.textPrimaryDbm.text = "${primaryCell.dbm} dBm"
            binding.textPrimaryBand.text = primaryCell.band ?: "?"
            
            val signalLevel = primaryCell.signalLevel
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

        val allCells = state.cells.sortedWith(compareByDescending<CellInfo> { it.isRegistered }
            .thenByDescending { it.dbm ?: -200 })
        cellAdapter.submitList(allCells)
        binding.textCellCount.text = "${allCells.size} celdas detectadas"
    }

    private fun showCellDetailDialog(cell: CellInfo) {
        val type = cell.type ?: "?"
        val dbm = cell.dbm ?: 0
        val band = cell.band ?: "?"
        val frequency = cell.frequency ?: "?"
        val tac = cell.tac ?: ""
        val cid = cell.cid ?: ""
        val lac = cell.lac ?: ""
        val pci = cell.pci ?: ""
        val bsic = cell.bsic ?: ""
        
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Detalles de Celda $type")
            .setMessage(buildString {
                append("Tipo: $type\n")
                append("Señal: $dbm dBm\n")
                if (band != "?" && band.isNotEmpty()) append("Banda: $band\n")
                if (frequency != "?" && frequency.isNotEmpty()) append("Frecuencia/EARFCN: $frequency\n")
                if (tac != "0" && tac.isNotEmpty()) append("TAC: $tac\n")
                if (cid != "0" && cid.isNotEmpty()) append("CID: $cid\n")
                if (lac != "0" && lac.isNotEmpty()) append("LAC: $lac\n")
                if (pci != "0" && pci.isNotEmpty()) append("PCI: $pci\n")
                if (bsic != "0" && bsic.isNotEmpty()) append("BSIC: $bsic\n")
                append("Conectado: ${if (cell.isConnected) "Sí" else "No"}")
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