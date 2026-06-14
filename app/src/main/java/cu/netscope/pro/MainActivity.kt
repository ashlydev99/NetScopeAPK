package cu.netscope.pro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cu.netscope.pro.databinding.ActivityMainBinding
import cu.netscope.pro.service.NetworkMonitorService
import cu.netscope.pro.ui.fragments.CellsFragment
import cu.netscope.pro.ui.fragments.SpeedMeterFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var showingCells = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        requestPermissions()

        // Iniciar con pantalla de celdas
        if (savedInstanceState == null) {
            showCellsFragment()
        }
    }

    private fun setupToolbar() {
        binding.btnSpeedometer.setOnClickListener {
            if (showingCells) {
                showSpeedFragment()
            } else {
                showCellsFragment()
            }
        }
    }

    private fun showCellsFragment() {
        showingCells = true
        binding.toolbar.title = "NetScope Pro"
        binding.btnSpeedometer.setImageResource(R.drawable.ic_speed)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, 0, 0, 0)
            .replace(R.id.fragment_container, CellsFragment())
            .commit()
    }

    private fun showSpeedFragment() {
        showingCells = false
        binding.toolbar.title = "Velocímetro"
        binding.btnSpeedometer.setImageResource(R.drawable.ic_cells)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, 0, 0, 0)
            .replace(R.id.fragment_container, SpeedMeterFragment())
            .commit()
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            startNetworkService()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startNetworkService()
        }
    }

    private fun startNetworkService() {
        NetworkMonitorService.startService(this)
    }
}