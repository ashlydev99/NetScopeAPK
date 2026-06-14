package cu.netscope.pro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cu.netscope.pro.databinding.ActivityMainBinding
import cu.netscope.pro.service.NetworkMonitorService
import cu.netscope.pro.ui.fragments.ARFragment
import cu.netscope.pro.ui.fragments.CellsFragment
import cu.netscope.pro.ui.fragments.CompassFragment
import cu.netscope.pro.ui.fragments.DualSIMFragment
import cu.netscope.pro.ui.fragments.NetworkModeFragment
import cu.netscope.pro.ui.fragments.PortScannerFragment
import cu.netscope.pro.ui.fragments.SettingsFragment
import cu.netscope.pro.ui.fragments.SpeedMeterFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    companion object {
        const val TAG_CELLS = "cells"
        const val TAG_SPEED = "speed"
        const val TAG_COMPASS = "compass"
        const val TAG_AR = "ar"
        const val TAG_DUALSIM = "dualsim"
        const val TAG_NETMODE = "netmode"
        const val TAG_PORTSCAN = "portscan"
        const val TAG_SETTINGS = "settings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        requestPermissions()

        // Iniciar con el fragmento de celdas
        if (savedInstanceState == null) {
            navigateToFragment(CellsFragment(), TAG_CELLS)
            binding.navView.setCheckedItem(R.id.nav_cells)
        }
    }

    private fun setupDrawer() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerToggle.isDrawerIndicatorEnabled = true
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_cells -> navigateToFragment(CellsFragment(), TAG_CELLS)
                R.id.nav_speed -> navigateToFragment(SpeedMeterFragment(), TAG_SPEED)
                R.id.nav_compass -> navigateToFragment(CompassFragment(), TAG_COMPASS)
                R.id.nav_ar -> navigateToFragment(ARFragment(), TAG_AR)
                R.id.nav_dualsim -> navigateToFragment(DualSIMFragment(), TAG_DUALSIM)
                R.id.nav_netmode -> navigateToFragment(NetworkModeFragment(), TAG_NETMODE)
                R.id.nav_portscan -> navigateToFragment(PortScannerFragment(), TAG_PORTSCAN)
                R.id.nav_settings -> navigateToFragment(SettingsFragment(), TAG_SETTINGS)
                R.id.nav_about -> showAboutDialog()
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun navigateToFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(0, 0, 0, 0) // Sin animaciones para máxima velocidad
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Acerca de NetScope Pro")
            .setMessage(
                "Creado con amor para la comunidad.\n\n" +
                "Desarrollador: Ashly Dev\n\n" +
                "Copyright © Ashly Dev 2026. Todos los derechos reservados.\n\n" +
                "Contacto: ashlydev99@gmail.com"
            )
            .setPositiveButton("Cerrar", null)
            .show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        // No detenemos el servicio aquí para mantener la notificación
    }
}