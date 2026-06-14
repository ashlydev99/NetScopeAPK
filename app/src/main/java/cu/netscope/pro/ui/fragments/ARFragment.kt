package cu.netscope.pro.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cu.netscope.pro.data.model.NetworkState
import cu.netscope.pro.databinding.FragmentArBinding
import cu.netscope.pro.service.NetworkMonitorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ARFragment : Fragment() {

    private var _binding: FragmentArBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cameraExecutor: ExecutorService
    private var overlayViews: MutableList<TextView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        checkCameraPermission()
        observeNetworkState()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                binding.textArStatus.text = "Cámara no disponible"
                binding.textArStatus.visibility = View.VISIBLE
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun observeNetworkState() {
        NetworkMonitorService.networkStateListener = { state ->
            updateOverlay(state)
        }
    }

    private fun updateOverlay(state: NetworkState) {
        // Limpiar overlays anteriores
        overlayViews.forEach { 
            (it.parent as? ViewGroup)?.removeView(it) 
        }
        overlayViews.clear()
        
        val primaryCell = state.primaryCell ?: return
        
        val overlayContainer = binding.overlayContainer
        if (overlayContainer == null) return
        
        val overlayText = buildString {
            append(state.operatorName)
            append(" | ")
            append(state.networkGeneration)
            append(" B")
            append(primaryCell.band)
            append("\n")
            append(primaryCell.dbm)
            append(" dBm | ")
            append(primaryCell.distanceFormatted)
            if (primaryCell.spectralEfficiency > 0) {
                append("\nEfic: ")
                append(String.format("%.2f", primaryCell.spectralEfficiency))
                append(" bps/Hz")
            }
        }
        
        val overlayView = TextView(requireContext()).apply {
            text = overlayText
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(16, 12, 16, 12)
            
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 80
                leftMargin = 16
            }
        }
        
        overlayContainer.addView(overlayView)
        overlayViews.add(overlayView)
        
        // Si hay más celdas, mostrarlas como puntos en la pantalla
        val cells = state.cells.filter { !it.isRegistered }.take(5)
        cells.forEachIndexed { index, cell ->
            val cellOverlay = TextView(requireContext()).apply {
                text = "${cell.type} B${cell.band}: ${cell.dbm}dBm"
                textSize = 10f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#99000000"))
                setPadding(8, 4, 8, 4)
                
                val yOffset = 200 + (index * 60)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = yOffset
                    leftMargin = 16
                }
            }
            
            overlayContainer.addView(cellOverlay)
            overlayViews.add(cellOverlay)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}