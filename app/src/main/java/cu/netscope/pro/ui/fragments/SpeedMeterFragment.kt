package cu.netscope.pro.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import cu.netscope.pro.data.model.NetworkState
import cu.netscope.pro.databinding.FragmentSpeedMeterBinding
import cu.netscope.pro.service.NetworkMonitorService

class SpeedMeterFragment : Fragment() {

    private var _binding: FragmentSpeedMeterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedMeterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        observeNetworkState()
    }

    private fun setupChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(false)
            legend.isEnabled = false
            setExtraOffsets(0f, 0f, 0f, 8f)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = Color.parseColor("#B0B0B0")
                textSize = 10f
                labelCount = 6
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#333333")
                textColor = Color.parseColor("#B0B0B0")
                textSize = 10f
                axisMinimum = 0f
            }
            
            axisRight.isEnabled = false
            
            animateX(300)
        }
    }

    private fun observeNetworkState() {
        NetworkMonitorService.networkStateListener = { state ->
            updateSpeedDisplay(state)
            updateChart(state)
        }
    }

    private fun updateSpeedDisplay(state: NetworkState) {
        val downloadMbps = state.downloadSpeedBps / 1_000_000f
        val uploadMbps = state.uploadSpeedBps / 1_000_000f
        
        binding.textDownloadSpeed.text = String.format("%.1f", downloadMbps)
        binding.textUploadSpeed.text = String.format("%.1f", uploadMbps)
        
        // Unidad dinámica
        val unit = if (downloadMbps < 1f) "Kbps" else "Mbps"
        binding.textSpeedUnit.text = unit
        
        if (downloadMbps < 1f) {
            binding.textDownloadSpeed.text = String.format("%.0f", downloadMbps * 1000)
        }
    }

    private fun updateChart(state: NetworkState) {
        val speedHistory = state.speedHistory
        if (speedHistory.isEmpty()) return
        
        val entries = speedHistory.mapIndexed { index, speed ->
            Entry(index.toFloat(), speed)
        }
        
        val dataSet = LineDataSet(entries, "Velocidad").apply {
            color = Color.parseColor("#4CAF50")
            fillColor = Color.parseColor("#1A4CAF50")
            setDrawFilled(true)
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }
        
        val lineData = LineData(dataSet as ILineDataSet)
        binding.lineChart.apply {
            data = lineData
            notifyDataSetChanged()
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}