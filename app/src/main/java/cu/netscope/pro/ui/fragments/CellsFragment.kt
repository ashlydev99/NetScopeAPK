package cu.netscope.pro.ui

import android.content.Context
import android.os.Bundle
import android.telephony.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import cu.netscope.pro.databinding.FragmentCellsBinding
import cu.netscope.pro.model.CellInfoModel
import cu.netscope.pro.ui.adapters.CellAdapter

class CellsFragment : Fragment() {

    private var _binding: FragmentCellsBinding? = null
    private val binding get() = _binding!!
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var adapter: CellAdapter

    companion object {
        fun newInstance() = CellsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCellsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        adapter = CellAdapter(emptyList()) { cell -> showDetails(cell) }
        binding.rvCells.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCells.adapter = adapter
        updateNetworkInfo()
    }

    private fun updateNetworkInfo() {
        val operator = telephonyManager.networkOperatorName ?: "?"
        val networkType = when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_UMTS -> "WCDMA"
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> "GSM"
            else -> "?"
        }
        val generation = when (telephonyManager.networkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            else -> "?"
        }
        binding.tvOperator.text = operator
        binding.tvType.text = networkType
        binding.tvGeneration.text = generation

        val cells = mutableListOf<CellInfoModel>()
        try {
            val all = telephonyManager.allCellInfo
            if (all != null) {
                for (ci in all) {
                    val model = parseCellInfo(ci)
                    if (model != null) cells.add(model)
                }
            }
        } catch (e: SecurityException) {
            // permissions missing
        } catch (e: Exception) {
            // defensive
        }

        val sorted = cells.sortedWith(compareByDescending<CellInfoModel> { it.isRegistered == true }.thenBy { it.dbm ?: Int.MIN_VALUE })
        adapter.update(sorted)

        val primary = sorted.firstOrNull()
        binding.tvSignal.text = primary?.dbm?.let { "${it} dBm - ${signalDesc(it)}" } ?: "Sin señal"
        binding.tvBand.text = primary?.band ?: "?"
    }

    private fun parseCellInfo(ci: CellInfo): CellInfoModel? {
        return try {
            when (ci) {
                is CellInfoLte -> {
                    val id = ci.cellIdentity
                    val dbm = ci.cellSignalStrength?.dbm ?: id.hashCode()
                    val band = try {
                        // safe reflection attempt; may fail on some vendors
                        val method = id::class.java.methods.firstOrNull { it.name.equals("getBands", true) }
                        val bands = method?.invoke(id)
                        if (bands is IntArray && bands.isNotEmpty()) "B${bands[0]}" else "?"
                    } catch (e: Exception) {
                        "?"
                    }
                    CellInfoModel(
                        type = "LTE",
                        band = band,
                        dbm = dbm,
                        cid = id.ci?.toLong(),
                        lac = null,
                        tac = id.tac,
                        pci = id.pci,
                        bsic = null,
                        frequency = try { id.earfcn } catch (e: Exception) { null },
                        isRegistered = ci.isRegistered
                    )
                }
                is CellInfoWcdma -> {
                    val id = ci.cellIdentity
                    val dbm = ci.cellSignalStrength?.dbm
                    CellInfoModel(
                        type = "WCDMA",
                        band = "?",
                        dbm = dbm,
                        cid = id.cid?.toLong(),
                        lac = id.lac,
                        tac = null,
                        pci = null,
                        bsic = null,
                        frequency = null,
                        isRegistered = ci.isRegistered
                    )
                }
                is CellInfoGsm -> {
                    val id = ci.cellIdentity
                    val dbm = ci.cellSignalStrength?.dbm
                    CellInfoModel(
                        type = "GSM",
                        band = "?",
                        dbm = dbm,
                        cid = id.cid?.toLong(),
                        lac = id.lac,
                        tac = null,
                        pci = null,
                        bsic = id.bsic,
                        frequency = null,
                        isRegistered = ci.isRegistered
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun signalDesc(dbm: Int): String {
        return when {
            dbm >= -90 -> "Excelente"
            dbm >= -95 -> "Buena"
            dbm >= -105 -> "Inestable"
            dbm >= -115 -> "Mala"
            dbm >= -120 -> "Muy débil"
            else -> "Sin señal"
        }
    }

    private fun showDetails(cell: CellInfoModel) {
        val details = StringBuilder()
        details.append("Tipo: ${cell.type ?: "?"}\n")
        details.append("dBm: ${cell.dbm ?: "?"}\n")
        details.append("Banda: ${cell.band ?: "?"}\n")
        details.append("Frecuencia/EARFCN: ${cell.frequency ?: "?"}\n")
        details.append("TAC: ${cell.tac ?: "?"}\n")
        details.append("CID: ${cell.cid ?: "?"}\n")
        details.append("LAC: ${cell.lac ?: "?"}\n")
        details.append("PCI: ${cell.pci ?: "?"}\n")
        details.append("BSIC: ${cell.bsic ?: "?"}\n")
        details.append("Registrada: ${cell.isRegistered ?: false}\n")

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Detalles de celda")
            .setMessage(details.toString())
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        updateNetworkInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}