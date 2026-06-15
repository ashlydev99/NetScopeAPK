package cu.netscope.pro.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cu.netscope.pro.databinding.ItemCellBinding
import cu.netscope.pro.model.CellInfoModel

class CellAdapter(
    private var items: List<CellInfoModel>,
    private val onClick: (CellInfoModel) -> Unit
) : RecyclerView.Adapter<CellAdapter.VH>() {

    inner class VH(val binding: ItemCellBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(items[pos])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val localItem = items[position]
        holder.binding.tvCellType.text = localItem.type ?: "?"
        val cid = localItem.cid?.toString() ?: "?"
        val pci = localItem.pci?.toString() ?: "?"
        val dbm = localItem.dbm?.let { "$it dBm" } ?: "?"
        val band = localItem.band ?: "?"
        holder.binding.tvCellInfo.text = "$cid / $pci / $dbm / $band"
        holder.binding.tvSignalShort.text = dbm
        if (localItem.isRegistered == true && position == 0) {
            holder.binding.connectedDot.visibility = View.VISIBLE
        } else {
            holder.binding.connectedDot.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    fun update(newItems: List<CellInfoModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}