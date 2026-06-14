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
        holder.bind(getItem(position))
    }

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textType: TextView = itemView.findViewById(R.id.text_cell_type)
        private val textBand: TextView = itemView.findViewById(R.id.text_cell_band)
        private val textDbm: TextView = itemView.findViewById(R.id.text_cell_dbm)
        private val textCid: TextView = itemView.findViewById(R.id.text_cell_cid)
        private val textExtra: TextView = itemView.findViewById(R.id.text_cell_extra)
        private val viewIndicator: View = itemView.findViewById(R.id.view_signal_indicator)
        private val viewConnected: View = itemView.findViewById(R.id.view_connected)

        fun bind(cell: CellInfo) {
            textType.text = cell.type
            textBand.text = if (cell.band != "?") cell.band else ""
            textDbm.text = "${cell.dbm} dBm"
            
            val identifier = when {
                cell.cid.isNotEmpty() && cell.cid != "0" -> "CID: ${cell.cid}"
                cell.pci.isNotEmpty() && cell.pci != "0" -> "PCI: ${cell.pci}"
                else -> ""
            }
            textCid.text = identifier
            
            val extraInfo = buildString {
                if (cell.spectralEfficiency > 0) {
                    append("${String.format("%.1f", cell.spectralEfficiency)} bps/Hz")
                }
            }
            textExtra.text = extraInfo
            
            val signalColor = when {
                cell.dbm >= -75 -> 0xFF4CAF50.toInt()
                cell.dbm >= -85 -> 0xFF8BC34A.toInt()
                cell.dbm >= -95 -> 0xFFFFC107.toInt()
                cell.dbm >= -105 -> 0xFFFF9800.toInt()
                cell.dbm >= -115 -> 0xFFFF5722.toInt()
                else -> 0xFFF44336.toInt()
            }
            viewIndicator.setBackgroundColor(signalColor)
            viewConnected.visibility = if (cell.isConnected) View.VISIBLE else View.GONE
            
            itemView.setOnClickListener {
                onCellClick(cell)
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