package cu.netscope.pro.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cu.netscope.pro.R
import cu.netscope.pro.data.model.CellInfo

class CellAdapter(
    private val onCellClick: (CellInfo) -> Unit
) : ListAdapter<CellInfo, CellAdapter.CellViewHolder>(CellDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        try {
            holder.bind(getItem(position))
        } catch (e: Exception) { }
    }

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        fun bind(cell: CellInfo) {
            val textType = itemView.findViewById<TextView>(R.id.text_cell_type)
            val textBand = itemView.findViewById<TextView>(R.id.text_cell_band)
            val textDbm = itemView.findViewById<TextView>(R.id.text_cell_dbm)
            val textCid = itemView.findViewById<TextView>(R.id.text_cell_cid)
            val textExtra = itemView.findViewById<TextView>(R.id.text_cell_extra)
            val viewIndicator = itemView.findViewById<View>(R.id.view_signal_indicator)
            val viewConnected = itemView.findViewById<View>(R.id.view_connected)

            // Variables locales para evitar smart cast
            val type = cell.type ?: "?"
            val band = cell.band
            val dbm = cell.dbm ?: 0
            val cid = cell.cid
            val pci = cell.pci
            val spectralEff = cell.spectralEfficiency ?: 0f
            val isConnected = cell.isConnected

            textType?.text = type
            textBand?.text = if (band != null && band != "?") band else ""
            textDbm?.text = "$dbm dBm"
            
            val identifier = when {
                cid != null && cid.isNotEmpty() && cid != "0" -> "CID: $cid"
                pci != null && pci.isNotEmpty() && pci != "0" -> "PCI: $pci"
                else -> ""
            }
            textCid?.text = identifier
            
            val extraInfo = if (spectralEff > 0) {
                "${String.format("%.1f", spectralEff)} bps/Hz"
            } else ""
            textExtra?.text = extraInfo
            
            val signalColor = when {
                dbm >= -75 -> 0xFF4CAF50.toInt()
                dbm >= -85 -> 0xFF8BC34A.toInt()
                dbm >= -95 -> 0xFFFFC107.toInt()
                dbm >= -105 -> 0xFFFF9800.toInt()
                dbm >= -115 -> 0xFFFF5722.toInt()
                else -> 0xFFF44336.toInt()
            }
            viewIndicator?.setBackgroundColor(signalColor)
            viewConnected?.visibility = if (isConnected) View.VISIBLE else View.GONE
            
            itemView.setOnClickListener {
                try {
                    onCellClick(cell)
                } catch (e: Exception) { }
            }
        }
    }

    private class CellDiffCallback : DiffUtil.ItemCallback<CellInfo>() {
        override fun areItemsTheSame(oldItem: CellInfo, newItem: CellInfo): Boolean {
            return oldItem.cid == newItem.cid && oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: CellInfo, newItem: CellInfo): Boolean {
            return oldItem == newItem
        }
    }
}