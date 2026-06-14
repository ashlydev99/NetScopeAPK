package cu.netscope.pro.ui.fragments

import android.content.Context
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cu.netscope.pro.databinding.FragmentDualsimBinding

class DualSIMFragment : Fragment() {

    private var _binding: FragmentDualsimBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDualsimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSimInfo()
    }

    private fun loadSimInfo() {
        val subscriptionManager = requireContext()
            .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        
        val activeSubscriptions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            subscriptionManager.activeSubscriptionInfoList
        } else {
            null
        }
        
        if (activeSubscriptions.isNullOrEmpty()) {
            binding.textSim1Info.text = "No se encontraron SIMs activas"
            binding.textSim2Info.text = ""
            return
        }
        
        // SIM 1
        if (activeSubscriptions.size >= 1) {
            val sim1 = activeSubscriptions[0]
            binding.textSim1Info.text = buildSimInfo(sim1, 0)
        } else {
            binding.textSim1Info.text = "SIM 1 no disponible"
        }
        
        // SIM 2
        if (activeSubscriptions.size >= 2) {
            val sim2 = activeSubscriptions[1]
            binding.textSim2Info.text = buildSimInfo(sim2, 1)
        } else {
            binding.textSim2Info.text = "SIM 2 no disponible"
        }
    }

    private fun buildSimInfo(subInfo: SubscriptionInfo, slot: Int): String {
        val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        val slotTm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tm.createForSubscriptionId(subInfo.subscriptionId)
        } else {
            tm
        }
        
        return buildString {
            append("Slot ${slot + 1}\n")
            append("Operador: ${subInfo.carrierName ?: "Desconocido"}\n")
            append("MCC: ${subInfo.mccString ?: "?"}\n")
            append("MNC: ${subInfo.mncString ?: "?"}\n")
            append("Número: ${subInfo.number ?: "No disponible"}\n")
            append("ICCID: ${subInfo.iccId ?: "?"}\n")
            append("Roaming: ${if (subInfo.dataRoaming > 0) "Sí" else "No"}\n")
            
            val networkType = when (slotTm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                else -> "Otro"
            }
            append("Red datos: $networkType")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}