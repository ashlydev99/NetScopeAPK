package cu.netscope.pro.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import cu.netscope.pro.R
import cu.netscope.pro.databinding.FragmentSpeedBinding
import android.net.TrafficStats
import kotlin.math.max

class SpeedFragment : Fragment() {

    private var _binding: FragmentSpeedBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private var lastRx: Long = 0
    private var lastTx: Long = 0
    private val entries = ArrayList<Entry>()
    private var timeIndex = 0f

    companion object {
        fun newInstance() = SpeedFragment()
    }

    private val tick = object : Runnable {
        override fun run() {
            val rx = TrafficStats.getTotalRxBytes()
            val tx = TrafficStats.getTotalTxBytes()
            if (lastRx == 0L) lastRx = rx
            if (lastTx == 0L) lastTx = tx
            val dRx = max(0L, rx - lastRx)
            val dTx = max(0L, tx - lastTx)
            lastRx = rx
            lastTx = tx
            val kbpsRx = dRx * 8 / 1024
            val kbpsTx = dTx * 8 / 1024
            binding.tvDownload.text = "DL: ${formatSpeed(kbpsRx)}"
            binding.tvUpload.text = "UL: ${formatSpeed(kbpsTx)}"

            if (entries.size >= 30) entries.removeAt(0)
            entries.add(Entry(timeIndex, kbpsRx.toFloat()))
            timeIndex += 1f

            val set = LineDataSet(entries, "DL")
            val accent = ContextCompat.getColor(requireContext(), R.color.accent)
            set.color = accent
            set.setDrawFilled(true)
            set.fillAlpha = 80
            set.fillColor = accent
            set.setDrawCircles(false)
            set.lineWidth = 2f

            val data = LineData(set)
            data.setDrawValues(false)
            binding.chart.data = data
            binding.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            binding.chart.axisRight.isEnabled = false
            binding.chart.description.isEnabled = false
            binding.chart.legend.isEnabled = false
            binding.chart.invalidate()

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSpeedBinding.inflate(inflater, container, false)
        // initial chart setup
        binding.chart.setTouchEnabled(false)
        binding.chart.setScaleEnabled(false)
        binding.chart.axisRight.isEnabled = false
        binding.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chart.description.isEnabled = false
        binding.chart.legend.isEnabled = false
        return binding.root
    }

    private fun formatSpeed(kbps: Long): String {
        return if (kbps >= 1024) {
            val mb = kbps / 1024.0
            String.format("%.2f Mbps", mb)
        } else {
            "$kbps kbps"
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(tick)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(tick)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}