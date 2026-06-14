package cu.netscope.pro.ui.fragments

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import cu.netscope.pro.data.model.CellInfo
import cu.netscope.pro.data.model.NetworkState
import cu.netscope.pro.databinding.FragmentCompassBinding
import cu.netscope.pro.service.NetworkMonitorService

class CompassFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!
    
    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null
    private var currentAzimuth = 0f
    private var targetBearing = 0f
    private var currentLocation: Location? = null
    private var targetCell: CellInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSensors()
        observeNetworkState()
    }

    private fun setupSensors() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Registrar sensores de orientación y acelerómetro
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        rotationSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        
        // Si no hay sensor de rotación, usar acelerómetro + magnetómetro
        if (rotationSensor == null) {
            val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            accelSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = location
            updateCompassDirection()
        }
    }

    private fun observeNetworkState() {
        NetworkMonitorService.networkStateListener = { state ->
            targetCell = state.primaryCell
            updateTargetInfo(state)
            
            // Iniciar GPS si está disponible
            try {
                locationManager?.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    locationListener,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                // GPS no disponible
            }
        }
    }

    private fun updateTargetInfo(state: NetworkState) {
        val cell = state.primaryCell
        binding.textTargetOperator.text = state.operatorName
        binding.textTargetBand.text = if (cell?.band != null) "Banda ${cell.band}" else ""
        binding.textTargetDbm.text = "${cell?.dbm ?: "?"} dBm"
        binding.textTargetDistance.text = cell?.distanceFormatted ?: "N/A"
    }

    private fun updateCompassDirection() {
        // Si tenemos ubicación y celda, podemos estimar dirección
        // Como no tenemos ubicación real de la torre, usamos el bearing simulado
        targetBearing = currentAzimuth // Apunta en la dirección del dispositivo
        
        val rotation = RotateAnimation(
            currentAzimuth,
            targetBearing,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f,
            RotateAnimation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 500
            fillAfter = true
        }
        
        binding.imageCompass.startAnimation(rotation)
        binding.textBearing.text = "${Math.round(targetBearing)}°"
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                currentAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (currentAzimuth < 0) currentAzimuth += 360f
                updateCompassDirection()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(
            this,
            sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}