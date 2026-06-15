package cu.netscope.pro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import cu.netscope.pro.databinding.ActivityMainBinding
import cu.netscope.pro.service.NetMonitorService
import cu.netscope.pro.ui.CellsFragment
import cu.netscope.pro.ui.SpeedFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var showingCells = true

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // fragments handle missing permissions gracefully
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "NetScope Pro"

        // Add icons to toolbar (no menu XML)
        addToolbarIcon(R.drawable.ic_speedometer) { toggleScreen() }
        addToolbarIcon(R.drawable.ic_info) { showAbout() }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                setCustomAnimations(0, 0, 0, 0)
                replace(R.id.container, CellsFragment.newInstance())
            }
        }

        // request permissions
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (needed.isNotEmpty()) {
            requestPermissions.launch(needed.toTypedArray())
        }

        // start foreground service (channel already created in Application)
        val svc = Intent(this, NetMonitorService::class.java)
        ContextCompat.startForegroundService(this, svc)
    }

    private fun toggleScreen() {
        showingCells = !showingCells
        val frag = if (showingCells) CellsFragment.newInstance() else SpeedFragment.newInstance()
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(0, 0, 0, 0)
            replace(R.id.container, frag)
        }
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_text))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun addToolbarIcon(drawableRes: Int, onClick: () -> Unit) {
        val iv = ImageView(this)
        iv.setImageResource(drawableRes)
        val lp = androidx.appcompat.widget.Toolbar.LayoutParams(
            androidx.appcompat.widget.Toolbar.LayoutParams.WRAP_CONTENT,
            androidx.appcompat.widget.Toolbar.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.END }
        iv.layoutParams = lp
        val pad = (resources.displayMetrics.density * 12).toInt()
        iv.setPadding(pad, 0, pad, 0)
        iv.setOnClickListener { onClick() }
        binding.toolbar.addView(iv)
    }
}