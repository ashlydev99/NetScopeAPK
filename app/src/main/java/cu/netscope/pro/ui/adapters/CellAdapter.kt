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
        } catch (e: Exception) {
            // Evitar crash si hay error al bindear
        }
    }

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        fun bind(cell: CellInfo) {
            // Buscar vistas de forma segura
            val textType = itemView.findViewById<TextView>(R.id.text_cell_type)
            val textBand = itemView.findViewById<TextView>(R.id.text_cell_band)
            val textDbm = itemView.findViewById<TextView>(R.id.text_cell_dbm)
            val textCid = itemView.findViewById<TextView>(R.id.text_cell_cid)
            val textExtra = itemView.findViewById<TextView>(R.id.text_cell_extra)
            val viewIndicator = itemView.findViewById<View>(R.id.view_signal_indicator)
            val viewConnected = itemView.findViewById<View>(R.id.view_connected)

            // Asignar valores con comprobación de null
            textType?.text = cell.type ?: "?"
            textBand?.text = if (cell.band != null && cell.band != "?") cell.band else ""
            textDbm?.text = "${cell.dbm} dBm"
            
            val identifier = when {
                cell.cid != null && cell.cid.isNotEmpty() && cell.cid != "0" -> "CID: ${cell.cid}"
                cell.pci != null && cell.pci.isNotEmpty() && cell.pci != "0" -> "PCI: ${cell.pci}"
                else -> ""
            }
            textCid?.text = identifier
            
            val extraInfo = buildString {
                if (cell.spectralEfficiency != null && cell.spectralEfficiency > 0) {
                    append("${String.format("%.1f", cell.spectralEfficiency)} bps/Hz")
                }
            }
            textExtra?.text = extraInfo
            
            // Color según intensidad de señal
            val signalColor = when {
                cell.dbm >= -75 -> 0xFF4CAF50.toInt()
                cell.dbm >= -85 -> 0xFF8BC34A.toInt()
                cell.dbm >= -95 -> 0xFFFFC107.toInt()
                cell.dbm >= -105 -> 0xFFFF9800.toInt()
                cell.dbm >= -115 -> 0xFFFF5722.toInt()
                else -> 0xFFF44336.toInt()
            }
            viewIndicator?.setBackgroundColor(signalColor)
            
            // Indicador de celda conectada
            viewConnected?.visibility = if (cell.isConnected) View.VISIBLE else View.GONE
            
            itemView.setOnClickListener {
                try {
                    onCellClick(cell)
                } catch (e: Exception) {
                    // Evitar crash en el click
                }
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